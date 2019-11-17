# Copper
Copper is a tool to **C**ollect **OP**erating data, **P**rocess and r**E**po**R**t them.
It's aim is to be a story-based monitoring and alerting tool.

For detailed informations, check [Copper business documentation](business/README.md).

# Quick start
* (Use OpenJDK11 set as JAVA_HOME)
* mvn clean package
* cd sample
* ./sample.sh  or ./sample.bat
* Open browser on http://localhost:30400
* You see dashboard IHM, where you can create dashboards
* Open browser on http://localhost:30400/admin
* You see admin IHM

## Create WEB Story
* In "Stories", click "Create a story", then name it "CopperMonitoringWeb", put this story and save change:
```
RUN ON CRON */5 * * * *
GIVEN COLLECTOR WEB WITH url=http://localhost:30400
    KEEP responseCode AS COPPER_WEB_RETURN_CODE
THEN STORE VALUES
```
* Now you see the story in "stories". Run it.
* Open "Values" menu, there you see some collected values (or refresh after some time)


## Create JMX Story
* In "Stories", click "Create a story", then name it "MonitorSystem", put this story and save change:
```
RUN ON CRON * * * * *
GIVEN COLLECTOR JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:30409/server,user=null,password=null
    QUERY java.lang:type=OperatingSystem FOR SystemCpuLoad AS SYSTEM_CPU_LOAD
    QUERY java.lang:type=OperatingSystem FOR FreePhysicalMemorySize AS SYSTEM_FREE_MEMORY
THEN STORE VALUES

```
* Now you see the story in "stories". Run it.
* Open "Values" menu, there you see some collected values (or refresh after some time)
* Check SYSTEM_CPU_LOAD, then click on "Graph(small)", you can there see plotted values over time
