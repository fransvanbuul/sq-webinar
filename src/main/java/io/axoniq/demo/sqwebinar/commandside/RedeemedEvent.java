package io.axoniq.demo.sqwebinar.commandside;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class RedeemedEvent {

    String id;
    BigDecimal amount;

}
