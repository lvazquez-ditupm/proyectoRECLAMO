# Script to throw the main scenario
clear
vnx -f ../vnx_scenarios/AIRSNetwork.xml -v -t
vnx -f ../vnx_scenarios/attacker-kali.xml -v -t

./SystemStart.sh
