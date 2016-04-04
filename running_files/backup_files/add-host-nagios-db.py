#!/usr/bin/python

# Script to add new hosts located in a specified database that don't exist in nagios config file.
#Input PARAMETERS: 2
#@templatefile: template for each host to add.
#@nagiosconfigfile: Nagios config file. Usually, this file is located in /usr/local/nagios/etc/objects/hosts.cfg
#@output: update Nagios config file.

import MySQLdb as mdb
import sys
import string


configfile = sys.argv[1]
templatefile = sys.argv[2]
tfile = open(templatefile, 'r')
try:
	con = mdb.connect('localhost', 'root', 'mysqlpass','ontairs')
	cur = con.cursor()
	cur.execute("SELECT * FROM assets")
	rows = cur.fetchall()
	for row in rows:
		hostname = row[1]
		alias = row[2]
		ipaddress = row[5]
		hostinfile= False	
		try:
			nagiosconfigfile = open (configfile, 'r')
			nagiosconfigfile.seek(0)
			for line in nagiosconfigfile:
				if hostname in line:
					hostinfile=True
					break
			if not hostinfile:
				print hostname	
				tagfile=open(hostname+".tags", "a")
				tagfile.write("$HOSTNAME="+hostname+"\n")
				tagfile.write("$ALIAS="+alias+"\n")
				tagfile.write("$IPADDRESS="+ipaddress+"\n")
				src = string.Template(tfile.read())
				
				result = src.substitute(HOSTNAME=hostname, ALIAS=alias, IPADDRESS=ipaddress, PARENTS="R2")
				print result
				nagiosconfigfile.close()	
				try:
					nagioscfupdate = open (configfile,'a')
					nagioscfupdate.seek(0)
					nagioscfupdate.write(result)
				finally:
					nagioscfupdate.close()
		except IOError:
			print "error"
			pass
		finally:
			nagiosconfigfile.close()	
			
	
except mdb.Error, e:
	print "Error %d: %s" (e.args[0],e.args[1])
	sys.exist(1)
finally:
	if con:
		con.close()
