# Script to throw the main scenario
clear
vnx -f /home/vmateos/Documents/OntAIRS/vnx_scenarios/AIRSNetwork.xml -v -t
vnx -f /home/vmateos/Documents/OntAIRS/vnx_scenarios/attacker-kali.xml -v -t

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

#configuramos NAT
modprobe ipt_MASQUERADE
echo 1 > /proc/sys/net/ipv4/ip_forward
iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
#Restringimos acceso al FW
#Aceptamos entrada del administrador y de la red IDSNet
#iptables -A INPUT -s 10.0.0.103 -j ACCEPT
#iptables -A INPUT -s 10.0.0.0/24 -j DROP
#iptables -A INPUT -s 10.0.1.0/24 -j ACCEPT
#iptables -A INPUT -s 172.16.222.0/24 -j DROP
#iptables -A INPUT -j ACCEPT
#Al IDS solo accede el administrador
#iptables -A FORWARD -s 10.0.0.103 -d 10.0.1.0/24 -j ACCEPT
#iptables -A FORWARD -d 10.0.1.0/24 -j DROP

# Lanzamos daemonlogger
#daemonlogger -i Attacker -o IDSNet1 &
daemonlogger -i AttackerKali -o IDSNet1 &

# REDIRECCION A 10.0.1.1:512
iptables -A PREROUTING -t nat -p udp --dport 514 -j DNAT --to-destination 138.4.7.178:512
#iptables -A OUTPUT -t nat -p udp -o lo --dport 514 -j DNAT --to-destination 10.0.1.1:512
#iptables -A OUTPUT -t nat -p udp -o lo --dport 4690 -j DNAT --to-destination 10.0.1.1:512
sleep 120

vnx -f AIRSNetwork.xml -v -M R2 -x loadcfg
