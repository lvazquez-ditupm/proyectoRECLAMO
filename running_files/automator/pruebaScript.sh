
#ssh root@10.1.100.26 'perl slowloris.pl -dns 192.168.100.130' &
#ssh root@10.1.100.26 'ls -l' > fichero.txt
#sleep 5
#ssh root@10.1.100.26 'killall perl'
#echo "Conseguido"

date
((java -jar /home/vmateos/Documents/NetBeansProjects/ContextModuleTester/dist/ContextModuleTester.jar network anomalydetection DMZ) > fichero.txt) &
sleep 20
killall sancp
echo "Come on"

