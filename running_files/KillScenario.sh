# Script to kill the main scenario
clear
iptables -F
iptables -X
iptables -Z
iptables -t nat -F
iptables -P INPUT ACCEPT
iptables -P OUTPUT ACCEPT
iptables -P FORWARD ACCEPT
iptables -t nat -P PREROUTING ACCEPT
iptables -t nat -P POSTROUTING ACCEPT
modprobe ipt_MASQUERADE
echo 1 > /proc/sys/net/ipv4/ip_forward
iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE

vnx -f /home/vmateos/Documentos/tesis/scenarios/AIRSNetwork-v03.xml -v -P
vnx -f /home/vmateos/Documentos/tesis/scenarios/AIRSNetwork-v03.xml -v -d
vnx -f /home/vmateos/Documentos/tesis/scenarios/AIRSNetwork-test.xml -v -P
vnumlparser.pl -d /home/vmateos/Documentos/Segura/HoneynetResponse/HoneynetResponse.xml -v -u root
vnx -f /usr/share/vnx/examples/HoneynetResponseAdhoc.xml -d -v -u root
vnx -f /home/vmateos/Documentos/tesis/scenarios/attacker-kali.xml -v -P
vnx -f /home/vmateos/Documentos/tesis/scenarios/attacker-kali.xml -v -d
