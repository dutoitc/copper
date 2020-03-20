# WEB Administration

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
