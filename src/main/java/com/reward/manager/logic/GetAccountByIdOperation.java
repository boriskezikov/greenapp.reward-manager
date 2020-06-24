package com.reward.manager.logic;

import com.reward.manager.model.Account;
import com.reward.manager.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.reward.manager.exception.ApplicationError.ACCOUNT_NOT_FOUND_BY_ID;
import static com.reward.manager.utils.Utils.logProcess;

@Component
@RequiredArgsConstructor
public class GetAccountByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(GetAccountByIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Account> process(GetAccountByClientIdRequest request) {
        return r2dbcAdapter.findById(null, request)
            .switchIfEmpty(ACCOUNT_NOT_FOUND_BY_ID.exceptionMono("No such account exist for client id = " + request.clientId))
            .as(logProcess(log, "GetAccountByIdOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class GetAccountByClientIdRequest {

        public final Long clientId;

        public Query bindOn(Query query) {
            return query.bind("$1", this.clientId);
        }
    }
}
