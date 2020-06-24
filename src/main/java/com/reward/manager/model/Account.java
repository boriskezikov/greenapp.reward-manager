package com.reward.manager.model;

import io.r2dbc.spi.Row;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@ToString
@EqualsAndHashCode(exclude = {"updated", "created"})
@RequiredArgsConstructor
public class Account {

    public final Long id;
    public final Long userId;
    public final Long amount;
    public final Status status;
    public final LocalDateTime updated;
    public final LocalDateTime created;

    public static Account fromGetByIdRow(Row row) {
        return Account.builder()
            .userId(row.get("user_id", Long.class))
            .amount(row.get("amount", Long.class))
            .updated(row.get("updated", LocalDateTime.class))
            .created(row.get("created", LocalDateTime.class))
            .build();
    }

    public enum Status {
        FRIEZED,
        ACTIVE,
        BLOCKED;
    }
}
