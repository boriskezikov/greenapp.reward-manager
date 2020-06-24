CREATE TYPE account_status AS ENUM (
    'FRIEZED',
    'ACTIVE',
    'BLOCKED'
    );

CREATE TABLE public.account
(
    id      SERIAL PRIMARY KEY,
    user_id VARCHAR               NOT NULL,
    amount  BIGINT                NOT NULL DEFAULT (0),
    status  public.account_status NOT NULL,
    updated timestamp,
    created timestamp             NOT NULL DEFAULT now()
);

CREATE TABLE public.event
(
    account_id BIGINT NOT NULL REFERENCES public.reward (id),
    value      BIGINT NOT NULL,
    name       VARCHAR,
    initiator  VARCHAR,
    timestamp  timestamp
);
