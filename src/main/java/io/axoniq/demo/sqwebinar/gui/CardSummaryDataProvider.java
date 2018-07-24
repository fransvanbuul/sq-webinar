package io.axoniq.demo.sqwebinar.gui;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.Query;
import io.axoniq.demo.sqwebinar.readside.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.axonframework.queryhandling.responsetypes.ResponseTypes;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@XSlf4j
@RequiredArgsConstructor
public class CardSummaryDataProvider extends AbstractBackEndDataProvider<CardSummary, Void> implements Closeable {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final QueryGateway queryGateway;

    private SubscriptionQueryResult<List<CardSummary>, CardSummary> fetchQueryResult;
    private SubscriptionQueryResult<Integer, CardCountChangedUpdate> countQueryResult;

    @Getter
    @Setter
    private CardSummaryFilter filter = new CardSummaryFilter("");

    @Override
    @Synchronized
    public void close() {
        if(fetchQueryResult != null) {
            fetchQueryResult.cancel();
            fetchQueryResult = null;
        }
        if(countQueryResult != null) {
            countQueryResult.cancel();
            countQueryResult = null;
        }
    }

    @Override
    @Synchronized
    protected Stream<CardSummary> fetchFromBackEnd(Query<CardSummary, Void> vaadinQuery) {
        if(fetchQueryResult != null) {
            fetchQueryResult.cancel();
            fetchQueryResult = null;
        }

        log.entry(vaadinQuery);
        FetchCardSummariesQuery fetchCardSummariesQuery =
                new FetchCardSummariesQuery(vaadinQuery.getOffset(), vaadinQuery.getLimit(), filter);
        log.trace("submitting {}", fetchCardSummariesQuery);

        fetchQueryResult = queryGateway.subscriptionQuery(
                fetchCardSummariesQuery,
                ResponseTypes.multipleInstancesOf(CardSummary.class),
                ResponseTypes.instanceOf(CardSummary.class)
        );

        fetchQueryResult.updates().subscribe(cardSummary ->
           fireEvent(new DataChangeEvent.DataRefreshEvent<>(this, cardSummary)));

        return log.exit(fetchQueryResult.initialResult().block().stream());
    }

    @Override
    @Synchronized
    protected int sizeInBackEnd(Query<CardSummary, Void> vaadinQuery) {
        log.entry(vaadinQuery);

        if(countQueryResult != null) {
            countQueryResult.cancel();
            countQueryResult = null;
        }

        CountCardSummariesQuery countCardSummariesQuery = new CountCardSummariesQuery(filter);
        log.trace("submitting {}", countCardSummariesQuery);

        countQueryResult = queryGateway.subscriptionQuery(
                countCardSummariesQuery,
                ResponseTypes.instanceOf(Integer.class),
                ResponseTypes.instanceOf(CardCountChangedUpdate.class)
        );

        countQueryResult.updates().buffer(Duration.ofMillis(250)).subscribe(cardCountChangedUpdate ->
                executorService.execute(() -> fireEvent(new DataChangeEvent<>(this))));

        return log.exit(countQueryResult.initialResult().block());
    }

}
