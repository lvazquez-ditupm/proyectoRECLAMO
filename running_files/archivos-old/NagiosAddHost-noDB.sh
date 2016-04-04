#!/bin/bash

perl /home/vmateos/Documents/OntAIRS/running_files/make-nagios-file.pl $1 $2 $3 
python /home/vmateos/Documents/OntAIRS/running_files/add-host-nagios.py $3 $4
rm $3


