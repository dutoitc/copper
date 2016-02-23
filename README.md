# copper
A tool to Collect Operationg data, Process and rEpoRt them. Built as an application monitoring and alerting tool.
It is actually under strong developement (24 feb 2016 approx), and already working for JMX/DB collectors and Pushover reporter.


# How it works ?

The system is based on user stories, in nearly human language.

First, the collect part:
````
GIVEN COLLECTOR JMX WITH url=service:jmx:rmi://blah-blah,user=xxx,password=yyy
    QUERY objectName1 FOR att1    AS myVar1
    QUERY objectName2 FOR att2 AS myVar2
WHEN CRON */5 7-18 * * 1-5
THEN STORE VALUES
````


Then, the report part
````
GIVEN STORED VALUES
WHEN myVar1>0.5
THEN REPORT BY MAIL to aUser@host.com,anotherUser@host.com
    with title="Purple alert!"
    with body="Holà capt'ain, the application tadah has a load of {{myVar1}} and a memory of {{myVar2}}"
````

Or a simple report
````
GIVEN COLLECTOR DB ORACLE
    with url=jdbc://something
    with username=xxx
    with password=yyy
    EXECUTE SQL "select something from somewhere as myVar3"
WHEN CRON 0 6 * * 1-5
THEN REPORT BY MAIL to mycustomer@something.com
    with title="Daily reporting"
    with body="Dear customer, here is your income for your sells yesterday: {{myVar3}}"
````

Values can be accessed by web: <http://aHost:30400/copper/ws/value/XXX> with all values easily readable at <http://aHost:30400/copper>


# Components
Here is a list of actual components:

## Collectors
* Http collector: get a web page, then grep page for values production
* Jmx collector: get values from JMX MBean server
* Oracle collector: get values from Oracle database (in progress)
* Log collector: get values from a log file (in progress; should support scp

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