#!/bin/bash
#$1: fichero con tags del nuevo host
#$2: template
#$3: fichero configuracion nagios

python /home/vmateos/Documents/OntAIRS/running_files/add-host-nagios.py $1 $2 $3


