CREATE TYPE account_status AS ENUM (
    'FRIEZED',
    'ACTIVE',
    'BLOCKED'
    );

CREATE TABLE public.account
(
    id      SERIAL PRIMARY KEY,
    user_id BIGINT                NOT NULL,
    amount  BIGINT                NOT NULL DEFAULT (0),
    status  public.account_status NOT NULL DEFAULT 'ACTIVE'::account_status,
    updated timestamp,
    created timestamp             NOT NULL DEFAULT now(),
    CONSTRAINT unique_user_id_constr UNIQUE (user_id)
);

CREATE TABLE public.event
(
    account_id BIGINT NOT NULL REFERENCES public.account (id),
    value      BIGINT NOT NULL,
    name       VARCHAR,
    initiator  VARCHAR,
    timestamp  timestamp
);
