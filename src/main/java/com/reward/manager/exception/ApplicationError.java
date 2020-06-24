package com.reward.manager.exception;

import reactor.core.publisher.Mono;

public enum ApplicationError {

    ACCOUNT_NOT_FOUND_BY_ID(400, "Account not found error"),
    NOT_ENOUGH_MONEY(400, "Not enough money"),
    WRONG_STATUS(400, "Account status is not 'ACTIVE'");

    public final int status;
    public final String description;

    ApplicationError(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public ApplicationErrorException exception(String body) {
        return new ApplicationErrorException(this, body);
    }

    public <T> Mono<T> exceptionMono(String body) {
        return Mono.error(exception(body));
    }

    public static class ApplicationErrorException extends HttpCodeException {

        public ApplicationErrorException(ApplicationError error, String args) {
            super(error.status, error.description + ": ".concat(args));
        }
    }
}
