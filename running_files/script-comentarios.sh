

#!/bin/bash

#Comprobacion de los parametros de entrada#

#Lectura del fichero linea a linea#


for i in $(ls); do
	sed 's/^# alert/alert/g' $i > ../rules/$i
done


exit 1


