package io.axoniq.demo.sqwebinar.restfrontend;

import io.axoniq.demo.sqwebinar.readside.CardSummary;
import io.axoniq.demo.sqwebinar.readside.CardSummaryFilter;
import io.axoniq.demo.sqwebinar.readside.FetchCardSummariesQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.responsetypes.ResponseTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/rest")
@RequiredArgsConstructor
public class GcRestController {

    private final QueryGateway queryGateway;

    /**
     * Simple example of how subscription queries can be used with other front-end
     * technologies. This will provide a stream of card summaries, sending one
     * every time a redeem takes place. (Which is probably of limited usage, but
     * the point here is to illustrate how it could technically work.)
     */
    @GetMapping(value = "/updates",  produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<CardSummary> getUpdates() {
        return queryGateway.subscriptionQuery(
                new FetchCardSummariesQuery(0,1, new CardSummaryFilter("")),
                ResponseTypes.multipleInstancesOf(CardSummary.class),
                ResponseTypes.instanceOf(CardSummary.class))
                        .updates();
    }

}
