/* org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql */
create table if not exists oauth2_authorization
(
    id                            varchar(100) not null,
    registered_client_id          varchar(100) not null,
    principal_name                varchar(200) not null,
    authorization_grant_type      varchar(100) not null,
    authorized_scopes             varchar(1000) default NULL::character varying,
    attributes                    text,
    state                         varchar(500)  default NULL::character varying,
    authorization_code_value      text,
    authorization_code_issued_at  timestamp,
    authorization_code_expires_at timestamp,
    authorization_code_metadata   text,
    access_token_value            text,
    access_token_issued_at        timestamp,
    access_token_expires_at       timestamp,
    access_token_metadata         text,
    access_token_type             varchar(100)  default NULL::character varying,
    access_token_scopes           varchar(1000) default NULL::character varying,
    oidc_id_token_value           text,
    oidc_id_token_issued_at       timestamp,
    oidc_id_token_expires_at      timestamp,
    oidc_id_token_metadata        text,
    refresh_token_value           text,
    refresh_token_issued_at       timestamp,
    refresh_token_expires_at      timestamp,
    refresh_token_metadata        text,
    user_code_value               text,
    user_code_issued_at           timestamp,
    user_code_expires_at          timestamp,
    user_code_metadata            text,
    device_code_value             text,
    device_code_issued_at         timestamp,
    device_code_expires_at        timestamp,
    device_code_metadata          text,
    primary key (id)
);

/* org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql */
create table if not exists oauth2_authorization_consent
(
    registered_client_id varchar(100)  not null,
    principal_name       varchar(200)  not null,
    authorities          varchar(1000) not null,
    primary key (registered_client_id, principal_name)
);

/* org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql */
create table if not exists oauth2_registered_client
(
    id                            varchar(100)                            not null,
    client_id                     varchar(100)                            not null,
    client_id_issued_at           timestamp     default CURRENT_TIMESTAMP not null,
    client_secret                 varchar(200)  default NULL::character varying,
    client_secret_expires_at      timestamp,
    client_name                   varchar(200)                            not null,
    client_authentication_methods varchar(1000)                           not null,
    authorization_grant_types     varchar(1000)                           not null,
    redirect_uris                 varchar(1000) default NULL::character varying,
    post_logout_redirect_uris     varchar(1000) default NULL::character varying,
    scopes                        varchar(1000)                           not null,
    client_settings               varchar(2000)                           not null,
    token_settings                varchar(2000)                           not null,
    primary key (id)
);

/* org/springframework/session/jdbc/schema*.sql */
create table if not exists spring_session
(
    primary_id            char(36) not null,
    session_id            char(36) not null,
    creation_time         bigint   not null,
    last_access_time      bigint   not null,
    max_inactive_interval integer  not null,
    expiry_time           bigint   not null,
    principal_name        varchar(100),
    constraint spring_session_pk
        primary key (primary_id)
);

create unique index if not exists spring_session_ix1
    on spring_session (session_id);

create index if not exists spring_session_ix2
    on spring_session (expiry_time);

create index if not exists spring_session_ix3
    on spring_session (principal_name);

create table if not exists spring_session_attributes
(
    session_primary_id char(36)     not null,
    attribute_name     varchar(200) not null,
    attribute_bytes    bytea        not null,
    constraint spring_session_attributes_pk
        primary key (session_primary_id, attribute_name),
    constraint spring_session_attributes_fk
        foreign key (session_primary_id) references spring_session
            on delete cascade
);

