#sudo java -jar /home/vmateos/Documents/NetBeansProjects/AIRSResponseExecutorTester/dist/AIRSResponseExecutorTester.jar mailNotification
#echo "Esperando 1 minuto para la siguiente respuesta"
#sleep 20
#sudo java -jar /home/vmateos/Documents/NetBeansProjects/AIRSResponseExecutorTester/dist/AIRSResponseExecutorTester.jar blockInAttack


echo "ANOMALÍAS CON ATAQUE DDOS" > fichero.txt

###### ANTES DEL ATAQUE #########
echo "Calculando anomalia de red antes del ataque" >> fichero.txt
((java -jar /home/vmateos/Documents/NetBeansProjects/ContextModuleTester/dist/ContextModuleTester.jar network anomalydetection DMZ | grep "grado de") >> fichero.txt) &
sleep 20
killall sancp
echo "Calculando anomalia de sistema antes del ataque" >> fichero.txt
java -jar /home/vmateos/Documents/NetBeansProjects/ContextModuleTester/dist/ContextModuleTester.jar system anomalydetection 192.168.100.130 DMZ-S1 | grep 'estado\|procesos' >> fichero.txt
echo "Realizando ataque"
sleep 10



###### DURANTE DEL ATAQUE #########

ssh root@10.1.100.26 'perl slowloris.pl -dns 192.168.100.130' 
echo "Realizando ataque" >> fichero.txt
sleep 20
echo "Calculando anomalia de red durante el ataque" >> fichero.txt
sudo java -jar /home/vmateos/Documents/NetBeansProjects/ContextModuleTester/dist/ContextModuleTester.jar network anomalydetection DMZ | grep "El grado de anomalia" >> fichero.txt
#echo "Calculando anomalia de sistema durante el ataque" >> fichero.txt
#sudo java -jar /home/vmateos/Documents/NetBeansProjects/ContextModuleTester/dist/ContextModuleTester.jar system anomalydetection 192.168.100.130 DMZ-S1 | grep "procesos" >> fichero.txt

sleep 10



###### TRAS LA RESPUESTA #########

sudo java -jar /home/vmateos/Documents/NetBeansProjects/AIRSResponseExecutorTester/dist/AIRSResponseExecutorTester.jar blockInAttack
sleep 20
echo "Calculando anomalia de red despues de la respuesta" >> fichero.txt
sudo java -jar /home/vmateos/Documents/NetBeansProjects/ContextModuleTester/dist/ContextModuleTester.jar network anomalydetection DMZ | grep "El grado de anomalia" >> fichero.txt
#echo "Calculando anomalia de sistema despues de la respuesta" >> fichero.txt
#sudo java -jar /home/vmateos/Documents/NetBeansProjects/ContextModuleTester/dist/ContextModuleTester.jar system anomalydetection 192.168.100.130 DMZ-S1 | grep "procesos" >> fichero.txt
sleep 10
ssh root@10.1.100.26 'killall perl'
echo "Fichero finalizado"
