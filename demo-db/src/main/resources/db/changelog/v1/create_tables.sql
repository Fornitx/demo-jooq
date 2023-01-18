--liquibase formatted sql

--changeset fornit:create_asdk_context_table
create table if not exists asdk_context
(
    id         uuid primary key     default uuid_generate_v4(),
    version    integer     not null default 1,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    msg        varchar(50) not null,
    status     varchar(10)
        constraint status_enum check (status in ('ONE', 'TWO', 'THREE')),
    rules      jsonb,
    config     jsonb
);
--rollback drop table if exists asdk_context

--changeset fornit:create_asdk_context_history_table
create table if not exists asdk_context_history
(
    id         uuid primary key,
    version    integer     not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    msg        varchar(50) not null,
    status     varchar(10)
        constraint status_enum check (status in ('ONE', 'TWO', 'THREE')),
    rules      jsonb,
    config     jsonb
);
--rollback drop table if exists asdk_context_history
