package io.axoniq.demo.sqwebinar.readside;

import lombok.Value;

@Value
public class CountCardSummariesQuery {

    CardSummaryFilter filter;

}
