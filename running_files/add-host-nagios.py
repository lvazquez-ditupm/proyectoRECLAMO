#!/usr/bin/python

# Script to add a specified new host to the Nagios config file.
# INput PARAMETERS: 3
#@tafsfile: file containing the tags of the new host to add.
#@templatefile: template for each host to add.
#@nagiosconfigfile: Nagios config file. Usually located in /usr/local/nagios/etc/objects/hosts.cfg

import sys
import string

cmdargs = str(sys.argv)
#print ("argumentos: %s" %cmdargs) 
tagsfile = sys.argv[1]
templatefile=sys.argv[2]
outputfile=sys.argv[3]
tagslist=[]

try:
	nagiosconfigfile = open (outputfile, 'r')
	outfile = open(outputfile, 'a')
	newhostfiletags = open (tagsfile,'r')
	tfile= open(templatefile,'r')
	result = tfile.read()
	hostinfile= False
	try:
		for line in newhostfiletags:
			tags= line.split("=")
			tagslist.append(tags)
			if tags[0] == "$HOSTNAME":
				for line in nagiosconfigfile:
					if tags[1] in line:
						hostinfile=True
		if not hostinfile:
			print "Adding new host to Nagios config file"
			for element in tagslist:
				key = element[0]
				value = element[1].replace("\n","")
				if key == "$NATPORT" and value=="" or value=="0":
					value=""
				elif key == "$NATPORT":
					value="-p "+value
				result = result.replace(key,value)
			#print result
			outfile.seek(0)
			outfile.write(result)
			
		
		
	finally:
		nagiosconfigfile.close()
		newhostfiletags.close()
		tfile.close()
		outfile.close()
except IOError as e:
	print "I/O error ({0}): {1}".format(e.errno, e.strerror)
	pass
