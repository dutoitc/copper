# Copper
A tool to Collect Operationg data, Process and rEpoRt them. Built as an application monitoring and alerting tool.
It is actually under strong developement, and already working for JMX/DB collectors and Pushover reporter.
It has a Web server to view values, run stories manually, and edit them.
Check the latest version on <https://github.com/dutoitc/copper/releases/tag/v1.0.0>


# How it works ?

The system is based on user stories, in nearly human language.

First, the collect part:
````
RUN ON CRON */5 7-18 * * 1-5
GIVEN COLLECTOR JMX WITH url=service:jmx:rmi://blah-blah,user=xxx,password=yyy
    QUERY objectName1 FOR att1    AS myVar1
    QUERY objectName2 FOR att2 AS myVar2
THEN STORE VALUES
````


Then, the report part
````
RUN ON CRON * * * * *
GIVEN STORED VALUES
WHEN myVar1>0.5
THEN REPORT BY MAIL to aUser@host.com,anotherUser@host.com
    WITH title="Purple alert!"
    WITH message="Holà capt'ain, the application tadah has a load of {{myVar1}} and a memory of {{myVar2}}"
````

Or a simple report
````
RUN ON CRON * * * * *
GIVEN COLLECTOR JDBC
    with url=jdbc://something
    with username=xxx
    with password=yyy
    EXECUTE SQL "select something from somewhere as myVar3"
WHEN CRON 0 6 * * 1-5
THEN REPORT BY MAIL to mycustomer@something.com
    WITH title="Daily reporting"
    WITH message="Dear customer, here is your income for your sells yesterday: {{myVar3}}"
````

Values can be accessed by web: <http://aHost:30400/copper/ws/value/XXX> with all values easily readable at <http://aHost:30400/copper>


# Components
Here is a list of actual components:

## Collectors
* Web collector: get a web page, keep json first-level values
* Jmx collector: get values from JMX MBean server
* Jdbc collector: get values from Jdbc database
* Log collector: get values from a log file (todo; should support scp)

## Triggers
* When trigger: WHEN a>1, WHEN a<22, WHEN a=33, WHEN a>17.22, ... (float are equals if delta<1/25)

## Reporters
* Mail reporter: report values, messages by mail
* Slf4j reporter: report values in a log file
* Pushover reporter: report values on mobile phone via Pushover

# Future
Here is a little wishlist. Add yours (report to dutoitc@shimbawa.ch)
* Read values from property file (like username-passwords)
* service values security
* More collectors (logs)
* More reporters (Jabber, Mail)
* Improved triggering
* Rework story parsing with ebnf compiler ? parsing tree ?
* Generation of monitoring web applications
* Support processing by Groovy, plugins

# Cook book
## Web collector
````
RUN ON CRON */5 7-18 * * 1-5
GIVEN COLLECTOR WEB WITH url=http://localhost:1530/ws/infra/status
    KEEP status AS WEB_STATUS
    KEEP lastReload AS WEB_LAST_RELOAD
THEN STORE VALUES
````

## JMX collector
````
RUN ON CRON */5 * * * *
GIVEN COLLECTOR JMX WITH url=service:jmx:rmi:///jndi/rmi://servername:1539/jmxrmi,user=myjmxuser,password=mypassword
    QUERY com.something.dummy:name=Monitoring,app=Application FOR Status AS MYAPP_PR_STATUS
    QUERY com.something.dummy:name=Monitoring,app=Application FOR Version AS MYAPP_PR_VERSION
THEN STORE VALUES
````

## JDBC collector
````
RUN ON CRON 24,56 * * * *
GIVEN COLLECTOR JDBC WITH url=jdbc:oracle:thin:@myserver:1528/myinstance,user=someuser,password=somepassword
    QUERY "select count(case when notice.status='Nouveau' then 1 end) APP_PR_NB_NOUVEAU,
                  count(case when notice.status='A traiter' then 1 end) APP_PR__A_TRAITER,
                  count(case when notice.status='En cours' then 1 end) APP_PR__EN_COURS,
                  count(case when notice.status='En erreur' then 1 end) APP_PR_T_EN_ERREUR,
                  count(case when notice.status='Traitée' then 1 end) APP_PR_TRAITEE
           from someapp.notice"
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