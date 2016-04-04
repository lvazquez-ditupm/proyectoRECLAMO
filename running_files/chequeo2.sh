#!/bin/sh
# This is a sample shell script showing how you can submit the SCHEDULE_FORCED_HOST_CHECK command
# to Nagios.  Adjust variables to fit your environment as necessary.

now=`date +%s`
commandfile='/usr/local/nagios/var/rw/nagios.cmd'
host=$1
#echo "[$now] SCHEDULE_FORCED_SVC_CHECK;INT1-H1;check_nrep!check_users" $now > $commandfile
echo "[%lu] SCHEDULE_FORCED_HOST_CHECK;"$host";"$now > $commandfile
echo "[%lu] SCHEDULE_FORCED_HOST_SVC_CHECKS;"$host";"$now > $commandfile
#echo "[%i] START_EXECUTING_SVC_CHECKS;"$host";1110741500\n" $now > $commandfile
exit 0
