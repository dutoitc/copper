# Reporters
Reporters format and send data to receivers.
For all reporters, values like {{A_VALUE}} are replaced by selected values.
Special value {{NOW_dd.MM.yyyy_HH:mm:ss}} add a formated date.


## Mail reporter
````
RUN ON CRON 0 8,13 * * *
GIVEN STORED VALUES
THEN REPORT BY MAIL to "user1@myserver.com,user2@myserver.com"
     WITH title="[TEST] some text"
     WITH message="<h3>Some title</h3>
     {{APP_PR_NB_DATA}} data yet in production database<br/>
     {{APP_VA_NB_DATA}} data yet in validation database<br/>
     Production status is {{APP_PR_STATUS}}"
````
Mail reporter report values by mail to one or multiple recipients.


## Pushover reporter (smartphone)
````
RUN ON CRON 0 8,13 * * *
GIVEN STORED VALUES
THEN REPORT BY PUSHOVER to "__mypushover_api_key__"
     WITH token="__pushover_dest_token__"
     WITH title="Some Title"
     WITH message="Production data
     {{APP_PR_NB_DATA}} notices yet in production database<br/>
     {{APP_PR_NB_ERRORS}} errors to be handled
````
Pushover is a web service providing notifications to smartphone (Android, iPhone, iPad, Desktop). You need to register for a free account with some monthly data rate, or a business paid service.
(See [Pushover](https://pushover.net/) website)


## CSV reporter
````
RUN ON CRON */15 * * * *
GIVEN STORED VALUES
THEN REPORT BY CSV to "prodExtract.csv"
     WITH headers="time;Nb notices;Nb errors"
     WITH line="{{NOW_dd.MM.yyyy_HH:mm:ss}};{{APP_PR_NB_DATA}};{{APP_PR_NB_ERRORS}}"
````

