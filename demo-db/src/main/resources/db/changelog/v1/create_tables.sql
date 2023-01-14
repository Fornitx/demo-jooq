--liquibase formatted sql

--changeset fornit:create_project_rule_table
create table if not exists project_rule
(
    id         uuid primary key     default uuid_generate_v4(),
    msg        varchar(50) not null,
    rules      jsonb,
    config     jsonb,
    status     varchar(10) constraint status_enum check (status in ('ONE', 'TWO', 'THREE')),
    version    integer     not null default 1,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
--rollback drop table if exists project_rule
