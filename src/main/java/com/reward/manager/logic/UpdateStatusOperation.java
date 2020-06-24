package com.reward.manager.logic;

import com.reward.manager.model.Account.Status;
import com.reward.manager.service.dao.R2dbcAdapter;
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
public class UpdateStatusOperation {

    private final static Logger log = LoggerFactory.getLogger(UpdateStatusOperation.class);

    public final R2dbcAdapter r2dbcAdapter;

    public Mono<Void> process(UpdateStatusRequest request) {
        return request.asMono()
            .flatMap(r2dbcAdapter::updateStatus)
            .then()
            .as(logProcess(log, "UpdateStatusOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class UpdateStatusRequest {

        public final Long clientId;
        public final Status status;

        public Mono<UpdateStatusRequest> asMono() {
            return Mono.just(this);
        }

        public Query bindOn(Query query) {
            return query
                .bind("$1", this.status.toString())
                .bind("$2", this.clientId);
        }
    }
}
