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

## Create an admin screen
* Open http://localhost:30400
* You can see an edition mode of dashboard. You can switch from running mode to edition mode by keyboard 'e'
* Stop Autorefresh ("Toggle autorefresh")
* Click on "New" to add a box
* Double-click on "Some body and a new line", then enter cpu load:{{cv('SYSTEM_CPU_LOAD').value}}
  (cv means 'copper value'). .value takes data value (you could also take value date)
* Double-click on "aWidget" and enter "SYSTEM"
* Exit edit mode: "e"
* you should see the CPU load
* Enter edit mode: "e"
* Click on "Export": you now have a JSON describing your screen
* You can edit the JSON manually to add new boxes or use the edition mode. If editing manually, check that each ID is different.
* In the JSON, an object can have an attribute "classes", which set CSS classe(s)
* You can add a root object "css", which should be a list of strings, containing CSS syntax,
* And you can add a root object "script", which is also a list of strings, containing javascript syntax
* Click F5, then import, and select the previously exported file
* The screen is loaded. Press F5
* The system ask if the last dashboard should be loaded. The JSON is persisted in the Browser DB.
* Best method to create screens is to bootstrap a new screen using the edit mode, then editing it directly in JSON edition.
