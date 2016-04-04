import smtplib

SERVER = "correo.ontairs.org"

FROM = "usertestmail@ontairs.org"
TO = ["veronique84@gmail.com"] # must be a list

SUBJECT = "Test"

TEXT = "Este mail se envia cada hora para simular trafico en una red."

# Prepare actual message

message = """\
From: %s
To: %s
Subject: %s

%s
""" % (FROM, ", ".join(TO), SUBJECT, TEXT)

# Send the mail

server = smtplib.SMTP(SERVER)
server.sendmail(FROM, TO, message)
server.quit()
