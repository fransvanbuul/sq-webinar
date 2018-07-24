package io.axoniq.demo.sqwebinar.readside;

import lombok.Value;

@Value
public class FetchCardSummariesQuery {

    int offset;
    int limit;
    CardSummaryFilter filter;

}
