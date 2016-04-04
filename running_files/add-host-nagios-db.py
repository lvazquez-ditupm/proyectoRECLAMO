#!/usr/bin/python

# Script to add new hosts located in a specified database that don't exist in nagios config file.
#Input PARAMETERS: 2
#@templatefile: template for each host to add.
#@nagiosconfigfile: Nagios config file. Usually, this file is located in /usr/local/nagios/etc/objects/hosts.cfg
#@output: update Nagios config file.

import MySQLdb as mdb
import sys
import string


configfile = sys.argv[2]
templatefile = sys.argv[1]
#tfile = open(templatefile, 'r')
try:
	con = mdb.connect('localhost', 'root', 'mysqlpass','ontairs')
	cur = con.cursor()
	cur.execute("SELECT * FROM assets")
	rows = cur.fetchall()
	for row in rows:
		hostname = row[1]
		alias = row[2]
		ipaddress = row[5]
		parents = row [8]
		natip = row[9]
		natport = str(row[10])
		hostinfile= False
		nagiosconfigfile = open (configfile, 'r')
		tfile = open (templatefile, 'r')	
		try:
			#nagiosconfigfile = open (configfile, 'r')
			nagiosconfigfile.seek(0)
			for line in nagiosconfigfile:
				if hostname in line:
					hostinfile=True
					break
			if not hostinfile:
				tagfile=open(hostname+".tags", "a")
				tagfile.write("$HOSTNAME="+hostname+"\n")
				tagfile.write("$ALIAS="+alias+"\n")
				tagfile.write("$IPADDRESS="+ipaddress+"\n")
				tagfile.write("$PARENTS="+parents+"\n")
				tagfile.write("$NATIP="+natip+"\n")
				#tagfile.write("$NATPORT="+natport+"\n")
				
				if natip:
					ipaddress=natip
				if natport and natport != '0':
					tagfile.write("$NATPORT="+natport+"\n")
					natport = "-p "+natport
			
				else:
					natport=''
					tagfile.write("$NATPORT="+natport+"\n")	
				print ipaddress
				print natport
				src = string.Template(tfile.read())
				result = src.substitute(HOSTNAME=hostname, ALIAS=alias, IPADDRESS=ipaddress, PARENTS=parents, NATPORT=natport)
				if not parents:
					if "parents" in result:
						result = result.replace("parents", "")				
	
				#nagiosconfigfile.close()
				nagioscfupdate = open (configfile, 'a')	
				try:
					#nagioscfupdate = open (configfile,'a')
					nagioscfupdate.seek(0)
					nagioscfupdate.write(result)
					print "Added host " +hostname+ " to the Nagios config file" 
				finally:
					nagioscfupdate.close()
		except IOError:
			print "error"
			pass
		finally:
			nagiosconfigfile.close()
			tfile.close()	
			
	
except mdb.Error, e:
	print "Error %d: %s" (e.args[0],e.args[1])
	sys.exist(1)
finally:
	if con:
		con.close()
