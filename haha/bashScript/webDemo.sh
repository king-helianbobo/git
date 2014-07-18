#!/bin/sh
PID_FILE=/tmp/webDemo.pid
launch_daemon()
{
    exec java -Des.pidfile=$PID_FILE -jar soul-client-0.4.jar <&- &
    pid=$!
    echo $pid
    return $?
}
if [ ! -f "$PID_FILE" ]; then
    echo "PID file not Exist, launch it now!"
    launch_daemon
else
    daemon_pid=$(cat $PID_FILE)
    echo $daemon_pid
    if ps -p "${daemon_pid}" 1&>/dev/null 2>&1
    then
	echo "Daemon had started, exit now."
    else
	echo "Daemon had not start, start it now!"
	launch_daemon
    fi
fi 
exit $?
