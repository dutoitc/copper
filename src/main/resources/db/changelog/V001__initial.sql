--liquibase formatted sql

--changeset xcijct:V001.11
CREATE TABLE valuestore (
    idvaluestore int NOT NULL,
    vkey varchar(50) NOT NULL,
    vvalue varchar(100000) NOT NULL,
    datefrom timestamp NOT NULL,
    dateto timestamp NOT NULL,
    primary key (idvaluestore));

--changeset xcijct:V001.12
create sequence SEQ_VALUESTORE_ID start with 1;

--changeset xcijct:V001.13
create index if not exists IDX_VS_KEY on valuestore(vkey);

--changeset xcijct:V001.14
create index if not exists IDX_VS_FROM on valuestore(datefrom);

--changeset xcijct:V001.15
create index if not exists IDX_VS_TO on valuestore(dateto);
