from httplib import *
import urllib2
connection = HTTPConnection("www.ontairs.org")
connection.request ("GET", "/moodle/index.php")
response = connection.getresponse()
if response.status==200:
	print "Page Found Succesfully"
elif response.status==404:
	print "Page Not Found"
else:
	print response.status, response.reason
connection.close()
