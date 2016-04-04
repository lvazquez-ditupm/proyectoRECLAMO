# Script to destroy the main scenario
clear

#Destruir OSSEC
#echo POR FAVOR, ELIMINE TODOS LOS AGENTES REGISTRADOS EN EL SERVIDOR OSSEC
#/var/ossec/bin/./manage_agents

vnx -f /root/RECLAMO/vnx_scenarios/AIRSNetwork.xml -v --destroy
vnx -f /root/RECLAMO/vnx_scenarios/attacker-kali.xml -v --destroy
killall executorAgent-debug

