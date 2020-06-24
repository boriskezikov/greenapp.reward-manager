package com.reward.manager.model;

import io.r2dbc.client.Query;

import java.time.LocalDateTime;

public class Event {

    public final Long accountId;
    public final Long value;
    public final String name;
    public final String initiator;

    private LocalDateTime timestamp;

    public Event(Long accountId, Long value, String name, String initiator) {
        this.accountId = accountId;
        this.value = value;
        this.name = name;
        this.initiator = initiator;
    }

    public static enum Type {
        ACCRUAL,
        WRITE_OFF;
    }

    public Query bindOn(Query query) {
        return query
            .bind("$1", accountId)
            .bind("$2", value)
            .bind("$3", name)
            .bind("$4", initiator);
    }
}
