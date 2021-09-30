--liquibase formatted sql

--changeset xcicdt:V1.2.13
alter table valuestore add datelastcheck timestamp;
update valuestore set datelastcheck = datefrom;
alter table valuestore alter datelastcheck set not null;

