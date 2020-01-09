JAR=applications/refmon/deployment/copper.jar
CLASS=ch.mno.copper.CopperApplication
LOG=app/refmon/logs/console.log
PID=0


JMX="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=43479 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
APP="-Dcopper.properties=applications/refmon/config/environment.properties"
OPTIONS="$APP $JMX -classpath ."

################################################################################


# Check the application status
#
# This function checks if the application is running
check_status() {

  # Running ps with some arguments to check if the PID exists
  # -C : specifies the command name
  # -o : determines how columns must be displayed
  # h : hides the data header
  #s=`ps -C 'java -jar $JAR' -o pid h`
  #s=`ps -e -o pid,cmd –sort cmd | grep “java -jar $JAR” | grep -v “grep ” | tail -n1 | awk ‘{ print $1 }`
  #s=`ps -e -o pid,cmd | grep refmon | grep -v grep | grep -v command | tail -n 1 | cut -f 1 -d' '`
  s=`ps -e -o pid,cmd | grep copper | grep jar | grep -v grep | tail -n 1 | awk '{print $1}'`

  # If somethig was returned by the ps command, this function returns the PID
  if [ $s ] ; then
    PID=$s
  fi

}


# Starts the application
start() {

  # At first checks if the application is already started calling the check_status
  # function
  check_status

  # $? is a special variable that hold the "exit status of the most recently executed
  # foreground pipeline"

  if [ $PID -ne 0 ] ; then
    echo "The application is already started"
    exit 0
  fi

  # If the application isn't running, starts it
  echo -n "Starting application: "

  # Redirects default and error output to a log file
  #java $OPTIONS -jar $JAR >> $LOG 2>&1 &
  #java $OPTIONS -classpath .:$JAR $CLASS >> $LOG 2>&1 &

  CMD="nohup java $OPTIONS -jar $JAR > $LOG 2>&1 "
  #CMD="sh $JAR start >> $LOG 2>&1 &"
  echo "$CMD &" > command.log
  $CMD &
  echo "OK"
}


# Stops the application
stop() {

  # Like as the start function, checks the application status
  check_status

  if [ $PID -eq 0 ] ; then
    echo "Application is already stopped"
    exit 0
  fi

  # Kills the application process
  echo -n "Stopping application: "
  kill -9 $PID &
  sleep 3
  echo "OK"
}

# Show the application status
status() {

  # The check_status function, again...
  check_status

  # If the PID was returned means the application is running
  if [ $PID -ne 0 ] ; then
    echo "Application is started"
  else
    echo "Application is stopped"
  fi
}


restart() {
  check_status
  if [ $PID -ne 0 ] ; then
    stop
  fi
  start
}



################################################################################


embeddedstart () {
    start
}
   
embeddedstop () {
    stop
    return 0
}
   
embeddedcheck () {
    status
    return 0
}
