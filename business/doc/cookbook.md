
# Cook book
## Web collector
````
RUN ON CRON */5 7-18 * * 1-5
GIVEN COLLECTOR WEB WITH url=http://localhost:1530/ws/infra/status
    KEEP status AS WEB_STATUS
    KEEP lastReload AS WEB_LAST_RELOAD
    KEEP body AS WEB_BODY
    KEEP responseCode as WEB_RETURN_CODE
THEN STORE VALUES
````
Syntax: KEEP (* | body | contentLength | contentType | responseCode | json expression) AS variable


## JMX collector
````
RUN ON CRON 24,56 * * * *
GIVEN COLLECTOR JMX WITH url=service:jmx:rmi:///jndi/rmi://servername:1539/jmxrmi,user=myjmxuser,password=mypassword
    QUERY com.something.dummy:name=Monitoring,app=Application FOR Sxtato AS APP_PR_SXTATO
    QUERY com.something.dummy:name=Monitoring,app=Application FOR Version AS APP_PR_VERSIO
THEN STORE VALUES
````

## JDBC collector
````
RUN ON CRON 24,56 * * * *
GIVEN COLLECTOR JDBC WITH url=jdbc:oracle:thin:@myserver:1528/myinstance,user=someuser,password=somepassword
    QUERY "select count(case when mydata.status='Nova' then 1 end) APP_PR_NB_NOVA,
                  count(case when mydata.status='Erare' then 1 end) APP_PR_NB_ERARE,
                  count(case when mydata.status='Kompletigita' then 1 end) APP_PR_NB_KOMPLETIGITA
           from someapp.mydata"
THEN STORE VALUES
````

## Mail reporter
````
RUN ON CRON 0 8,13 * * *
GIVEN STORED VALUES
THEN REPORT BY MAIL to "user1@myserver.com,user2@myserver.com"
     WITH title="[TEST] some text"
     WITH message="<h3>Some title</h3>
     {{APP_PR_NB_NOTICES}} notices yet in production database<br/>
     {{APP_VA_NB_NOTICES}} notices yet in validation database<br/>
     Production status is {{APP_PR_STATUS}}"
````

## Pushover reporter (smartphone)
````
RUN ON CRON 0 8,13 * * *
GIVEN STORED VALUES
THEN REPORT BY PUSHOVER to "__mypushover_api_key__"
     WITH token="__pushover_dest_token__"
     WITH title="Some Title"
     WITH message="Production data
     {{APP_PR_NB_NOTICES}} notices yet in production database<br/>
     {{APP_PR_NB_ERRORS}} errors to be handled
````

## CSV reporter
````
RUN ON CRON */15 * * * *
GIVEN STORED VALUES
THEN REPORT BY CSV to "prodExtract.csv"
     WITH headers="time;Nb notices;Nb errors"
     WITH line="{{NOW_dd.MM.yyyy_HH:mm:ss}};{{APP_PR_NB_NOTICES}};{{APP_PR_NB_ERRORS}}"
````