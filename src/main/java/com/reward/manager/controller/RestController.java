package com.reward.manager.controller;

import com.reward.manager.logic.AccrualByClientIdOperation;
import com.reward.manager.logic.AccrualByClientIdOperation.AccrualRequest;
import com.reward.manager.logic.GetAccountByIdOperation;
import com.reward.manager.logic.GetAccountByIdOperation.GetAccountByClientIdRequest;
import com.reward.manager.logic.UpdateStatusOperation;
import com.reward.manager.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.reward.manager.logic.WriteoffByClientIdOperation;
import com.reward.manager.logic.WriteoffByClientIdOperation.WriteoffRequest;
import com.reward.manager.model.Account;
import com.reward.manager.model.Account.Status;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/reward-manager")
@RequiredArgsConstructor
public class RestController {

    private final static Logger log = LoggerFactory.getLogger(RestController.class);

    private final GetAccountByIdOperation getAccountByIdOperation;
    private final AccrualByClientIdOperation accrualByClientIdOperation;
    private final WriteoffByClientIdOperation writeoffByClientIdOperation;
    private final UpdateStatusOperation updateStatusOperation;

    @GetMapping(value = "/account", params = "clientId")
    public Mono<Account> getAccountByClientId(@RequestParam(value = "clientId") Long taskId) {
        return Mono.just(new GetAccountByClientIdRequest(taskId))
            .flatMap(getAccountByIdOperation::process)
            .doOnSubscribe(s -> log.info("RestController.getAccountById.in id = {}", taskId))
            .doOnSuccess(s -> log.info("RestController.getAccountById.out"));
    }

    @PostMapping(value = "/accrual")
    public Mono<Void> accrualByClientId(@RequestBody AccrualRequest request) {
        return Mono.just(request)
            .flatMap(accrualByClientIdOperation::process)
            .doOnSubscribe(s -> log.info("RestController.accrualByClientId.in task = {}", request))
            .doOnSuccess(s -> log.info("RestController.accrualByClientId.out"));
    }

    @PostMapping(value = "/writeoff")
    public Mono<Void> writeoffByClientId(@RequestBody WriteoffRequest request) {
        return Mono.just(request)
            .flatMap(writeoffByClientIdOperation::process)
            .doOnSubscribe(s -> log.info("RestController.writeoffByClientId.in task = {}", request))
            .doOnSuccess(s -> log.info("RestController.writeoffByClientId.out"));
    }

    @PatchMapping(value = "/account/{id}", params = "status")
    public Mono<Void> updateStatus(@PathVariable(value = "id") Long clientId, @RequestParam("status") Status status) {
        return Mono.just(new UpdateStatusRequest(clientId, status))
            .flatMap(updateStatusOperation::process)
            .doOnSubscribe(s -> log.info("RestController.updateStatus.in clientId = {}, status = {}", clientId, status))
            .doOnSuccess(s -> log.info("RestController.updateStatus.out"));
    }
}
