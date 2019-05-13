# Collectors
Collectors use connectors to read data on remote elements and return some values as key-value or table of values.
To ease story comprehension, basic CRON and storage has been defined in samples below.


## JDBC Collectors
````
RUN ON CRON 24,56 * * * *
GIVEN COLLECTOR JDBC WITH url=jdbc:oracle:thin:@myserver:1528/myinstance,user=someuser,password=somepassword
    QUERY "select count(case when mydata.status='Nova' then 1 end) APP_PR_NB_NOVA,
                  count(case when mydata.status='Erare' then 1 end) APP_PR_NB_ERARE,
                  count(case when mydata.status='Kompletigita' then 1 end) APP_PR_NB_KOMPLETIGITA
           from someapp.mydata"
THEN STORE VALUES
````

Some rules do apply:
* special ,user=...,password=... has been added to the URL definition.
* the Query part begins and ends with apostrophe.
* spaces and end of lines could be added after 'JDBC', 'WITH', before 'user', before 'password', before 'QUERY'.
* Stored values are query result's column names.


##  JMX Collectors
````
RUN ON CRON 24,56 * * * *
GIVEN COLLECTOR JMX WITH url=service:jmx:rmi:///jndi/rmi://servername:1539/jmxrmi,user=myjmxuser,password=mypassword
    QUERY com.something.dummy:name=Monitoring,app=Application FOR Sxtato AS APP_PR_SXTATO
    QUERY com.something.dummy:name=Monitoring,app=Application FOR Version AS APP_PR_VERSIO
THEN STORE VALUES
````

Some rules do apply:
* special ,user=...,password=... has been added to the URL definition.
* multiple values can be queried, by path and attribute name.
* spaces and end of lines could be added after 'JMX', 'WITH', before 'user', before 'password', after 'QUERY' line.


## WEB Collectors
````
RUN ON CRON */5 7-18 * * 1-5
GIVEN COLLECTOR WEB WITH url=http://localhost:1530/ws/infra/status
    KEEP body AS WEB_BODY
    KEEP contentLength AS WEB_BODY_SIZE
    KEEP contentType AS WEB_CONTENT_TYPE
    KEEP responseCode AS WEB_RETURN_CODE
    KEEP regexp:abc(?<capture>[def012]+)ghi AS WEB_CAPTURE
    KEEP $.value.*.[?(@['Name']=='app.WS_Services')].Version AS JSON_VERSION
THEN STORE VALUES
````
Syntax: KEEP (* | body | contentLength | contentType | responseCode | json expression | regex) AS variable
Here, JSON Value match {'value':{'0':{'Name':'app.WS_Services', 'Version':'1.2.3'}}}.

Some rules do apply:
* Regex capture begins with "regexp". The captured group must be named 'capture'
* JSON expression is evaluated by JSONPath (com.jayway implementation)
* \* and body return all the web page body


