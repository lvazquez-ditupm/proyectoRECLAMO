#! /bin/sh

clear

Help(){
	# Mensaje de error que mostrará el script cuando no se le de ningún argumento.
	echo "************AYUDA************"
	echo "Configurar sistema: $ systemControl [IP] [Interfaz red atacante] [Interfaz red IDS]"
	echo "Virtualizar escenario de pruebas: $ systemControl -v"
	echo "Parar sistema: $ systemControl -d"
	exit 0
}

testIP(){
	if [[ $1 =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then 			
			Help	
	fi
}

NetworkStart(){

	echo "************NETWORK START************"

	vnx -f ../vnx_scenarios/AIRSNetwork.xml -v -t
	vnx -f ../vnx_scenarios/attacker-kali.xml -v -t
}

Stop(){

	echo "************NETWORK STOP************"

	#Destruir OSSEC
	#echo POR FAVOR, ELIMINE TODOS LOS AGENTES REGISTRADOS EN EL SERVIDOR OSSEC
	#/var/ossec/bin/./manage_agents

	vnx -f ../vnx_scenarios/AIRSNetwork.xml -v --destroy
	vnx -f ../vnx_scenarios/attacker-kali.xml -v --destroy
	
	echo "************SYSTEM STOP************"

	killall executorAgent-debug
}

SystemStart(){
	
	echo "************SYSTEM START************"

	# Reglas iniciales de filtrado 
	#Politica inicial
	iptables -F
	iptables -X
	iptables -Z
	iptables -t nat -F
	iptables -P INPUT ACCEPT
	iptables -P OUTPUT ACCEPT
	iptables -P FORWARD ACCEPT
	iptables -t nat -P PREROUTING ACCEPT
	iptables -t nat -P POSTROUTING ACCEPT

	#restaruramos los clientes OSSEC
	#cp /var/ossec/etc/client.keys.0 /var/ossec/etc/client.keys
	#/var/ossec/bin/./ossec-control restart

	#configuramos NAT
	modprobe ipt_MASQUERADE
	echo 1 > /proc/sys/net/ipv4/ip_forward
	iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE

	# Lanzamos daemonlogger
	#daemonlogger -i Attacker -o IDSNet1 &
	daemonlogger -i $2 -o $3 &

	# REDIRECCION A IP:512
	iptables -A PREROUTING -t nat -p udp --dport 514 -j DNAT --to-destination $1:512

	sleep 1

	vnx -f ../vnx_scenarios/AIRSNetwork.xml -v -M R2 -x loadcfg

	# Inicia los agentes ejecutores
	../RECLAMO/./executorAgent-debug &

}

case $# in

	1)	if test $1 = -v; then
		NetworkStart
		SystemStart 10.0.1.1 NetATT IDSNet1
		elif test $1 = -d; then
			Stop
		else
			Help
		fi;;


	3)	#testIP $1
		SystemStart $1 $2 $3;;

	*) 	Help;;

esac

exit 0
