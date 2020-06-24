package com.reward.manager.service.dao;

import com.reward.manager.logic.AccrualByClientIdOperation.AccrualRequest;
import com.reward.manager.logic.GetAccountByIdOperation.GetAccountByClientIdRequest;
import com.reward.manager.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.reward.manager.logic.WriteoffByClientIdOperation.WriteoffRequest;
import com.reward.manager.model.Account;
import com.reward.manager.model.Event;
import io.r2dbc.client.Handle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class R2dbcAdapter {

    private final R2dbcHandler handler;

    public Mono<Account> findById(@Nullable Handle handle, GetAccountByClientIdRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> findById(h, request));
        }
        var sql = "SELECT user_id, amount, status, updated, created " +
            "FROM public.account WHERE user_id = $1";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(Account::fromGetByIdRow)
            .next();
    }

    public Mono<Long> insertEvent(@Nullable Handle handle, Event request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> insertEvent(h, request));
        }
        var sql = "INSERT INTO public.account_event (account_id, value, name, type, initiator) " +
            "VALUES($1, $2, $3, $4) RETURNING id";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Long> updateStatus(UpdateStatusRequest request) {
        return handler.inTxMono(h -> {
            var sql = "UPDATE public.account SET status = $1::account_status" +
                "WHERE user_id = $2 RETURNING id";
            return request.bindOn(h.createQuery(sql))
                .mapRow(r -> r.get("id", Long.class))
                .next();
        });
    }

    public Mono<Long> accrualAccount(@Nullable Handle handle, AccrualRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> accrualAccount(h, request));
        }
        var sql = "INSERT INTO public.account (user_id, amount) VALUES($1, $2) ON CONFLICT " +
            "DO UPDATE public.account SET amount = amount + $2, updated = now() " +
            "WHERE user_id = $1 AND status = 'ACTIVE'::account_status RETURNING id";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Long> writeoffAccount(@Nullable Handle handle, WriteoffRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> writeoffAccount(h, request));
        }
        var sql = "INSERT INTO public.account (user_id, amount) VALUES($1, $2) ON CONFLICT " +
            "DO UPDATE public.account SET amount = amount - $2, updated = now() " +
            "WHERE user_id = $1 AND status = 'ACTIVE'::account_status RETURNING id";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }
}
