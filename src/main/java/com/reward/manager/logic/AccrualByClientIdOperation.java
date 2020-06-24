package com.reward.manager.logic;

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

import static com.reward.manager.utils.Utils.logProcess;

@Component
@RequiredArgsConstructor
public class AccrualByClientIdOperation {

    private final static Logger log = LoggerFactory.getLogger(AccrualByClientIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;
    private final R2dbcHandler r2dbcHandler;

    public Mono<Void> process(AccrualRequest request) {
        return request.asMono()
            .flatMap(r -> r2dbcHandler.inTxMono(h -> {
                var updateTask = r2dbcAdapter.accrualAccount(h, r).cache();
                var insertEvent = updateTask
                    .map(id -> new Event(request.clientId, request.amount, "AccuralAccout", request.initiator))
                    .flatMap(e -> r2dbcAdapter.insertEvent(h, e));
                return Mono.when(updateTask, insertEvent);
            }))
            .as(logProcess(log, "AccrualByClientIdOperation", request));
    }

    @ToString(exclude = "amount")
    @RequiredArgsConstructor
    public static class AccrualRequest {

        public final Long clientId;
        public final Long amount;
        public final String initiator;

        public Query bindOn(Query query) {
            return query
                .bind("$1", this.clientId)
                .bind("$2", this.amount);
        }

        public Mono<AccrualRequest> asMono() {
            return Mono.just(this);
        }
    }
}
