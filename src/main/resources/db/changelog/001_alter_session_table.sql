/* necessary for postgres */

alter table spring_session
    alter column primary_id type varchar(36);

alter table spring_session
    alter column session_id type varchar(36);
