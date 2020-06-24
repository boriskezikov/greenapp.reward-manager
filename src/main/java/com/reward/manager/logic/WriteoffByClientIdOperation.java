package com.reward.manager.logic;

import com.reward.manager.logic.GetAccountByIdOperation.GetAccountByClientIdRequest;
import com.reward.manager.model.Account.Status;
import com.reward.manager.model.Event;
import com.reward.manager.service.dao.R2dbcAdapter;
import com.reward.manager.service.dao.R2dbcHandler;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.reward.manager.exception.ApplicationError.NOT_ENOUGH_MONEY;
import static com.reward.manager.exception.ApplicationError.WRONG_STATUS;
import static com.reward.manager.utils.Utils.logProcess;
import static java.lang.String.format;

@Component
@RequiredArgsConstructor
public class WriteoffByClientIdOperation {

    private final static Logger log = LoggerFactory.getLogger(WriteoffByClientIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;
    private final R2dbcHandler r2dbcHandler;

    public Mono<Void> process(WriteoffRequest request) {
        return request.asMono()
            .flatMap(r -> r2dbcHandler.inTxMono(h -> {
                var amount = r2dbcAdapter.findById(h, new GetAccountByClientIdRequest(request.clientId));
                var updateTask = amount.flatMap(a -> {
                    if (a.amount < request.amount) {
                        return NOT_ENOUGH_MONEY.exceptionMono(format("Cannot write off %s. Not enough money", request.amount));
                    } else if (!a.status.equals(Status.ACTIVE)) {
                        return WRONG_STATUS.exceptionMono(
                            format("Account status is %s. Cannot write off from this account", a.status.toString()));
                    }
                    return r2dbcAdapter.writeoffAccount(h, r);
                });
                var insertEvent = updateTask
                    .map(id -> new Event(id, request.amount, "WriteoffAccout", request.initiator))
                    .flatMap(e -> r2dbcAdapter.insertEvent(h, e));
                return Mono.when(updateTask, insertEvent);
            }))
            .as(logProcess(log, "WriteoffByClientIdOperation", request));
    }

    @ToString(exclude = "amount")
    @RequiredArgsConstructor
    public static class WriteoffRequest {

        public final Long clientId;
        public final Long amount;
        public final String initiator;

        public Query bindOn(Query query) {
            return query
                .bind("$1", this.clientId)
                .bind("$2", this.amount);
        }

        public Mono<WriteoffRequest> asMono() {
            return Mono.just(this);
        }
    }
}
