package io.axoniq.demo.sqwebinar.gui;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import io.axoniq.demo.sqwebinar.readside.CardSummary;
import io.axoniq.demo.sqwebinar.readside.CardSummaryFilter;
import io.axoniq.demo.sqwebinar.readside.CountCardSummariesQuery;
import io.axoniq.demo.sqwebinar.readside.FetchCardSummariesQuery;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.responsetypes.ResponseTypes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@XSlf4j
@RequiredArgsConstructor
public class CardSummaryDataProvider extends AbstractBackEndDataProvider<CardSummary, Void> {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final QueryGateway queryGateway;

    @Getter
    @Setter
    private CardSummaryFilter filter = new CardSummaryFilter("");

    @Override
    protected Stream<CardSummary> fetchFromBackEnd(Query<CardSummary, Void> vaadinQuery) {
        log.entry(vaadinQuery);
        FetchCardSummariesQuery fetchCardSummariesQuery =
                new FetchCardSummariesQuery(vaadinQuery.getOffset(), vaadinQuery.getLimit(), filter);
        log.trace("submitting {}", fetchCardSummariesQuery);
        return log.exit(queryGateway.query(
                fetchCardSummariesQuery,
                ResponseTypes.multipleInstancesOf(CardSummary.class))
                .join().stream());
    }

    @Override
    protected int sizeInBackEnd(Query<CardSummary, Void> vaadinQuery) {
        log.entry(vaadinQuery);
        CountCardSummariesQuery countCardSummariesQuery = new CountCardSummariesQuery(filter);
        log.trace("submitting {}", countCardSummariesQuery);
        return log.exit(queryGateway.query(
                countCardSummariesQuery,
                ResponseTypes.instanceOf(Integer.class))
                .join());
    }

}
