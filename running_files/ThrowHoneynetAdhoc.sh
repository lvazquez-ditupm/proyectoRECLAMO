# Script to throw the main scenario
clear
sleep 10
vnx -f /home/vmateos/Documents/tesis/scenarios/HoneynetResponseAdhoc.xml --resume -v -u root
vnx -f /home/vmateos/Documents/tesis/scenarios/HoneynetResponseAdhoc.xml --console --cid con0
vnx -f /home/vmateos/Documents/tesis/scenarios/HoneynetResponseAdhoc.xml --console --cid con1 -M R2
#IPORIGEN=$1
#IPDEST=$2
#PROTOCOLO=$3
#if test $# -eq 4
#then
	#PDEST=$4
#else
#	PDEST=111
#fi

#iptables -A FORWARD -s $IPORIGEN -d $IPDEST -p $PROTOCOLO -j DROP
#iptables -t nat -A PREROUTING -s $IPORIGEN -d $IPDEST -p $PROTOCOLO --dport $PDEST -j DNAT --to-destination 10.0.3.132
