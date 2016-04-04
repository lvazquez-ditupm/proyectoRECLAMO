/* airsexecutor.c
 *
 * Copyright (c) 2005-2008 Frank Knobbe <frank@knobbe.us>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * 
 *
 * Purpose:
 *
 * This tool sends (un)blocking requests to a remote host running SnortSam
 * (the agent) which will (un)block the intruding IP address on a variety of
 * host and network firewalls.
 * The communication over the network is encrypted using two-fish.
 * (Implementation ripped from CryptCat by Farm9 with permission.)
 *
 *
 * Comments:
 * 
 * Some modifications were made for Danny Guamán <ds.guaman@alumnos.upm.es, danny.guamanl@gmail.com> 2012
 * It tool was used to build a module part of Based Ontologies Automated Intrusion Detection System. It's part of a project research
 * at Techical University of Madrid.
 *
*/


#ifndef		__AIRSEXECUTOR_C__
#define		__AIRSEXECUTOR_C__

#include "airsexecutor.h"

		
void waitms(unsigned int dur)
{
#ifdef WIN32
        Sleep(dur);
#else
        usleep(dur*1000);
#endif
}

/*      This function (together with the define in airsexecutor.h) attempts
 *      to prevent buffer overflows by checking the destination buffer size.
*/
void _safecp(char *dst,unsigned long max,char *src)
{	if(dst && src && max)
	{	while(--max>0 && *src)
			*dst++ = *src++;
		*dst=0;
	}
}

/*  Generates a new encryption key for TwoFish based on seq numbers and a random that
 *  the Executor Agents send on checkin (in protocol)
*/
void GenerateNewCipherKey(ExecutorAgent *agent, ActionRequestPacket *packet)
{	unsigned char newkey[TwoFish_KEY_LENGTH+2];
	int i;

	newkey[0]=packet->airsSeqNo[0];		/* current snort seq # (which both know) */
	newkey[1]=packet->airsSeqNo[1];			
	newkey[2]=packet->agentSeqNo[0];			/* current SnortSam seq # (which both know) */
	newkey[3]=packet->agentSeqNo[1];
	newkey[4]=packet->protocol[0];		/* the random SnortSam chose */
	newkey[5]=packet->protocol[1];

	strncpy(newkey+6, agent->agentKey ,TwoFish_KEY_LENGTH-6); /* append old key */
	newkey[TwoFish_KEY_LENGTH]=0;

	newkey[0]^=agent->myKeyMod[0];		/* modify key with key modifiers which were */
	newkey[1]^=agent->myKeyMod[1];		/* exchanged during the check-in handshake. */
	newkey[2]^=agent->myKeyMod[2];
	newkey[3]^=agent->myKeyMod[3];
	newkey[4]^=agent->agentKeyMod[0];
	newkey[5]^=agent->agentKeyMod[1];
	newkey[6]^=agent->agentKeyMod[2];
	newkey[7]^=agent->agentKeyMod[3];

	for(i=0;i<=7;i++)
		if(newkey[i]==0)
			newkey[i]++;

	safecopy(agent->agentKey, newkey);
	TwoFishDestroy(agent->agentFish);
	agent->agentFish=TwoFishInit(newkey);
}


/*  CheckOut will be called when airsExecutor exists. It de-registeres this tool 
 *  from the list of 'AIRS Executors' that the 'Executor Agent' keeps. 
*/
void CheckOut(ExecutorAgent *agent)
{	ActionRequestPacket sampacket;
	int i,len;
	char *encbuf,*decbuf;


	if(!agent->persistentSocket)
	{	agent->agentSocket=socket(PF_INET,SOCK_STREAM,IPPROTO_TCP); 
		if(agent->agentSocket==INVALID_SOCKET)
		{	fprintf(stderr,"Error: [CheckOut] Invalid Socket error!\n");
			return;
		}
		if(bind(agent ->agentSocket,(struct sockaddr *)&(agent ->localSocketAddr),sizeof(struct sockaddr)))
		{	fprintf(stderr,"Error: [CheckOut] Can not bind socket!\n");
			return;
		}
		/* let's connect to the agent */
		i=!connect(agent ->agentSocket,(struct sockaddr *)&agent ->agent SocketAddr,sizeof(struct sockaddr));
	}
	else
		i=TRUE;
	if(i)
	{	if(verbose>0)
			printf("Info: [CheckOut] Disconnecting from host %s.\n",inet_ntoa(agent ->agentIP));
		/* now build the packet */
		agent ->mySeqNo+=agent ->agentSeqNo; /* increase my seqno */
		sampacket.endianCheck=1;
		sampacket.airsSeqNo[0]=(char)agent ->mySeqNo;
		sampacket.airsSeqNo[1]=(char)(agent ->mySeqNo>>8);
		sampacket.agentSeqNo[0]=(char)agent ->agentSeqNo; /* fill agent  seqno */
		sampacket.agentSeqNo[1]=(char)(agent ->agentSeqNo>>8);
		sampacket.status=FWSAM_STATUS_CHECKOUT;  /* checking out... */
		sampacket.version=agent ->packetVersion;

		if(verbose>1)
		{	printf("Debug: [CheckOut] Sending CHECKOUT\n");
			printf("Debug: [CheckOut] Snort SeqNo:  %x\n",agent ->mySeqNo);
			printf("Debug: [CheckOut] Mgmt SeqNo :  %x\n",agent ->agentSeqNo);
			printf("Debug: [CheckOut] Status     :  %i\n",sampacket.status);
		}

		encbuf=TwoFishAlloc(sizeof(ActionRequestPacket),FALSE,FALSE,agent ->agentFish); /* get encryption buffer */
		len=TwoFishEncrypt((char *)&sampacket,(char **)&encbuf,sizeof(ActionRequestPacket),FALSE,agent ->agentFish); /* encrypt packet with current key */

		if(send(agent ->agentSocket,encbuf,len,0)==len)
		{	i=FWSAM_NETWAIT;
			ioctlsocket(agent ->agentSocket,FIONBIO,&i);	/* set non blocking and wait for  */
			while(i-- >1)
			{	waitms(10);					/* ...wait a maximum of 3 secs for response... */
				if(recv(agent ->agentSocket,encbuf,len,0)==len) /* ... for the status packet */
					i=0;
			}
			if(i) /* if we got the packet */
			{	decbuf=(char *)&sampacket;
				len=TwoFishDecrypt(encbuf,(char **)&decbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,FALSE,agent ->agentFish);

				if(len!=sizeof(ActionRequestPacket)) /* invalid decryption */
				{	safecopy(agent ->agentKey ,agent ->initialKey); /* try initial key */
					TwoFishDestroy(agent ->agentFish);			 /* toss this fish */
					agent ->agentFish=TwoFishInit(agent ->agentKey ); /* re-initialze TwoFish with initial key */
					len=TwoFishDecrypt(encbuf,(char **)&decbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,FALSE,agent ->agentFish); /* and try to decrypt again */
					if(verbose>1)
						printf("Debug: [CheckOut] Had to use initial key!\n");
				}
				if(len==sizeof(ActionRequestPacket)) /* valid decryption */
				{	if(sampacket.version!=agent ->packetVersion) /* but don't really care since we are on the way out */
						fprintf(stderr,"Error: [CheckOut] Protocol version error!\n");
				}
				else
					fprintf(stderr,"Error: [CheckOut] Password mismatch!\n");
			}
		}
		free(encbuf); /* release TwoFishAlloc'ed buffer */
	}
	else
		fprintf(stderr,"Error: [CheckOut] Could not connect to host %s for CheckOut. What the hell, we're quitting anyway! :)\n",inet_ntoa(agent ->agentIP));

	closesocket(agent ->agentSocket);
	agent ->persistentSocket=FALSE;
}


/*  This routine registers this tool with SnortSam.
 *  It will also change the encryption key based on some variables.
*/
int CheckIn(ExecutorAgent *station)
{	int i,len,stationok=FALSE,again;
	ActionRequestPacket sampacket;
	char *encbuf,*decbuf;

	do
	{	again=FALSE;
		/* create a socket for the station */
		station->agentSocket=socket(PF_INET,SOCK_STREAM,IPPROTO_TCP); 
		if(station->agentSocket==INVALID_SOCKET)
		{	fprintf(stderr,"Error: [CheckIn] Invalid Socket error!\n");
			return FALSE;
		}
		if(bind(station->agentSocket,(struct sockaddr *)&(station->localSocketAddr),sizeof(struct sockaddr)))
		{	fprintf(stderr,"Error: [CheckIn] Can not bind socket!\n");
			return FALSE;
		}

		/* let's connect to the agent */
		if(connect(station->agentSocket,(struct sockaddr *)&station->stationSocketAddr,sizeof(struct sockaddr)))
		{	fprintf(stderr,"Error: [CheckIn] Could not connect to host %s.\n",inet_ntoa(station->agentIP));
			return FALSE;
		}
		else
		{	if(verbose>0)
				printf("Info: [CheckIn] Connected to host %s.\n",inet_ntoa(station->agentIP));
			/* now build the packet */
			sampacket.endianCheck=1;
			sampacket.airsSeqNo[0]=(char)station->mySeqNo; /* fill my sequence number number */
			sampacket.airsSeqNo[1]=(char)(station->mySeqNo>>8); /* fill my sequence number number */
			sampacket.status=FWSAM_STATUS_CHECKIN; /* let's check in */
			sampacket.version=station->packetVersion; /* set the packet version */
			memcpy(sampacket.duration,station->myKeyMod,4);  /* we'll send SnortSam our key modifier in the duration slot */
												   /* (the checkin packet is just the plain initial key) */
			if(verbose>1)
			{	printf("Debug: [CheckIn] Sending CHECKIN\n");
				printf("Debug: [CheckIn] Snort SeqNo:  %x\n",station->mySeqNo);
				printf("Debug: [CheckIn] Mode       :  %i\n",sampacket.status);
				printf("Debug: [CheckIn] Version    :  %i\n",sampacket.version);
			}

			encbuf=TwoFishAlloc(sizeof(ActionRequestPacket),FALSE,FALSE,station->agentFish); /* get buffer for encryption */
			len=TwoFishEncrypt((char *)&sampacket,(char **)&encbuf,sizeof(ActionRequestPacket),FALSE,station->agentFish); /* encrypt with initial key */
			if(send(station->agentSocket,encbuf,len,0)!=len) /* weird...could not send */
				fprintf(stderr,"Error: [CheckIn] Could not send to host %s.\n",inet_ntoa(station->agentIP));
			else
			{	i=FWSAM_NETWAIT;
				ioctlsocket(station->agentSocket,FIONBIO,&i);	/* set non blocking and wait for  */
				while(i-- >1)
				{	waitms(10); /* wait a maximum of 3 secs for response */
					if(recv(station->agentSocket,encbuf,len,0)==len)
						i=0;
				}
				if(!i) /* time up? */
					fprintf(stderr,"Error: [CheckIn] Did not receive response from host %s.\n",inet_ntoa(station->agentIP));
				else
				{	decbuf=(char *)&sampacket; /* got status packet */
					len=TwoFishDecrypt(encbuf,(char **)&decbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,FALSE,station->agentFish); /* try to decrypt with initial key */
					if(len==sizeof(ActionRequestPacket)) /* valid decryption */
					{	if(verbose>1)
						{
							printf("Debug: [CheckIn] Received %s\n",sampacket.status==FWSAM_STATUS_OK?"OK":
																	   sampacket.status==FWSAM_STATUS_NEWKEY?"NEWKEY":
																	   sampacket.status==FWSAM_STATUS_RESYNC?"RESYNC":
																	   sampacket.status==FWSAM_STATUS_HOLD?"HOLD":"ERROR");
							printf("Debug: [CheckIn] Snort SeqNo:  %x\n",sampacket.airsSeqNo[0]|(sampacket.airsSeqNo[1]<<8));
							printf("Debug: [CheckIn] Mgmt SeqNo :  %x\n",sampacket.agentSeqNo[0]|(sampacket.agentSeqNo[1]<<8));
							printf("Debug: [CheckIn] Status     :  %i\n",sampacket.status);
							printf("Debug: [CheckIn] Version    :  %i\n",sampacket.version);
						}

						if(sampacket.version==FWSAM_PACKETVERSION_PERSISTENT_CONN || sampacket.version==FWSAM_PACKETVERSION) /* master speaks my language */
						{	if(sampacket.status==FWSAM_STATUS_OK || sampacket.status==FWSAM_STATUS_NEWKEY || sampacket.status==FWSAM_STATUS_RESYNC) 
							{	station->agentSeqNo=sampacket.agentSeqNo[0]|(sampacket.agentSeqNo[1]<<8); /* get stations seqno */
								station->lastContact=(unsigned long)time(NULL);
								stationok=TRUE;
								station->packetVersion=sampacket.version;
								if(sampacket.version==FWSAM_PACKETVERSION)
									station->persistentSocket=FALSE;
								
								if(sampacket.status==FWSAM_STATUS_NEWKEY || sampacket.status==FWSAM_STATUS_RESYNC)	/* generate new keys */
								{	memcpy(station->agentKeyMod,sampacket.duration,4); /* note the key modifier */
									GenerateNewCipherKey(station,&sampacket); /* and generate new TwoFish keys (with key modifiers) */
									if(verbose>1)
										printf("Debug: [CheckIn] Generated new encryption key...\n");
								}
							}
							else if(sampacket.status==FWSAM_STATUS_ERROR && sampacket.version==FWSAM_PACKETVERSION) 
							{	if(station->persistentSocket)
								{	fprintf(stderr,"Info: [CheckIn] Host %s doesn't support packet version %i for persistent connections. Trying packet version %i.\n",inet_ntoa(station->agentIP),FWSAM_PACKETVERSION_PERSISTENT_CONN,FWSAM_PACKETVERSION);
									station->persistentSocket=FALSE;
									station->packetVersion=FWSAM_PACKETVERSION;
									again=TRUE;
								}
								else
									fprintf(stderr,"Error: [CheckIn] Protocol version mismatch! Ignoring host %s!\n",inet_ntoa(station->agentIP));
							}
							else /* weird, got a strange status back */
								fprintf(stderr,"Error: [CheckIn] Funky handshake error! Ignoring host %s!\n",inet_ntoa(station->agentIP));
						}
						else /* packet version does not match */
							fprintf(stderr,"Error: [CheckIn] Protocol version error! Ignoring host %s!\n",inet_ntoa(station->agentIP));
					}
					else /* key does not match */
						fprintf(stderr,"Error: [CheckIn] Password mismatch! Ignoring host %s!\n",inet_ntoa(station->agentIP));
				}
			}
			free(encbuf); /* release TwoFishAlloc'ed buffer */
		}
		if(!(stationok && station->persistentSocket))
			closesocket(station->agentSocket);
	}while(again);
	return stationok;
}


/* removes spaces from a string 
*/	
void remspace(char *str)    
{	char *p;

	p=str;
	while(*p)
	{	if(myisspace(*p))		/* normalize spaces (tabs into space, etc) */
			*p=' ';
		p++;
	}
	while((p=strrchr(str,' ')))	/* remove spaces */
		strcpy(p,p+1);
}

/* parses duration arguments and returns seconds 
*/
unsigned long parseduration(char *p)  
{	unsigned long dur=0,tdu;
	char *tok,c1,c2;

	remspace(p);				/* remove spaces from value */
	while(*p)
	{	tok=p;
		while(*p && myisdigit(*p))
			p++;
		if(*p)
		{	c1=mytolower(*p);
			*p=0;
			p++;
			if(*p && !myisdigit(*p))
			{	c2=mytolower(*p++);
				while(*p && !myisdigit(*p))
					p++;
			}
			else
				c2=0;
			tdu=atol(tok);
			switch(c1)
			{	case 'm':	if(c2=='o')				/* for month... */
								tdu*=(60*60*24*30);	/* ...use 30 days */
							else
								tdu*=60;			/* minutes */
				case 's':	break;					/* seconds */
				case 'h':	tdu*=(60*60);			/* hours */
							break;
				case 'd':	tdu*=(60*60*24);		/* days */
							break;
				case 'w':	tdu*=(60*60*24*7);		/* week */
							break;
				case 'y':	tdu*=(60*60*24*365);	/* year */
							break;
			}
			dur+=tdu;
		}
		else
			dur+=atol(tok);
	}

	return dur;
}

/* This does nothing else than inet_ntoa, but it keeps 256 results in a static string
 * unlike inet_ntoa which keeps only one. This is used for (s)printf's were two IP
 * addresses are printed (this has been increased from four while multithreading the app).
*/
char *inettoa(unsigned long ip)
{	struct in_addr ips;
	static char addr[20];

	ips.s_addr=ip;
	strncpy(addr,inet_ntoa(ips),19);
	addr[19]=0;
	return addr;
}

int ExecuteResponseAction(char *arg)
{	char str[512],*p,*encbuf,*decbuf,*samport,*sampass,*samhost;
	int i,error=TRUE,len,ipidx=0,peeridx=0;
	ActionRequestPacket sampacket;
	struct hostent *hoste;
	unsigned long samip;
	ExecutorAgent station;

			

	safecopy(str,arg);
	samhost=str;
	samport=NULL;
	sampass=NULL;
	p=str;
	while(*p && *p!=':' && *p!='/') 
		p++;
	if(*p==':')
	{	*p++=0;
		if(*p)
			samport=p;
		while(*p && *p!='/')
			p++;
	}
	if(*p=='/')
	{	*p++=0;
		if(*p)
			sampass=p;
	}
	samip=0;
	if(inet_addr(samhost)==INADDR_NONE)
	{	hoste=gethostbyname(samhost);
		if(!hoste)
		{	fprintf(stderr,"Error: Unable to resolve host '%s', ignoring entry!\n",samhost);
			return 1;
		}
		else
			samip=*(unsigned long *)hoste->h_addr;
	}
	else
	{	samip=inet_addr(samhost);
		if(!samip)
		{	fprintf(stderr,"Error: Invalid host address '%s', ignoring entry!\n",samhost);
			return 1;
		}
	}
	station.agentIP.s_addr=samip;
	if(samport!=NULL && atoi(samport)>0)
		station.agentPort=atoi(samport);
	else
		station.agentPort=FWSAM_DEFAULTPORT;
	if(sampass!=NULL)
	{	strncpy(station.agentKey ,sampass,TwoFish_KEY_LENGTH);
		station.agentKey [TwoFish_KEY_LENGTH]=0;
	}
	else
		station.agentKey [0]=0;

	safecopy(station.initialKey,station.agentKey );
	station.agentFish=TwoFishInit(station.agentKey );

	station.localSocketAddr.sin_port=htons(0);
	station.localSocketAddr.sin_addr.s_addr=0;
	station.localSocketAddr.sin_family=AF_INET;
	station.stationSocketAddr.sin_port=htons(station.agentPort);
	station.stationSocketAddr.sin_addr=station.agentIP;
	station.stationSocketAddr.sin_family=AF_INET;

	do
		station.mySeqNo=rand();
	while(station.mySeqNo<20 || station.mySeqNo>65500);
	station.myKeyMod[0]=rand();
	station.myKeyMod[1]=rand();
	station.myKeyMod[2]=rand();
	station.myKeyMod[3]=rand();
	station.agentSeqNo=0;
	station.persistentSocket=TRUE;
	station.packetVersion=FWSAM_PACKETVERSION_PERSISTENT_CONN;
	
	if(CheckIn(&station))
	{	error=FALSE;
	
		do
		{	ipidx=0;
			do
			{	if(!station.persistentSocket)
				{	/* create a socket for the station */
					station.agentSocket=socket(PF_INET,SOCK_STREAM,IPPROTO_TCP); 
					if(station.agentSocket==INVALID_SOCKET)
					{	fprintf(stderr,"Error: [ExecuteResponseAction] Invalid Socket error!\n");
						error=TRUE;
					}
					if(bind(station.agentSocket,(struct sockaddr *)&(station.localSocketAddr),sizeof(struct sockaddr)))
					{	fprintf(stderr,"Error: [ExecuteResponseAction] Can not bind socket!\n");
						error=TRUE;
					}
				}
				else
					error=FALSE;
				if(!error)
				{	if(!station.persistentSocket)
					{	/* let's connect to the agent */
						if(connect(station.agentSocket,(struct sockaddr *)&station.stationSocketAddr,sizeof(struct sockaddr)))
						{	fprintf(stderr,"Error: [ExecuteResponseAction] Could not send block to host %s.\n",inet_ntoa(station.agentIP));
							closesocket(station.agentSocket);
							error=TRUE;
						}
					}
					
					if(!error)
					{	if(verbose>0)
							printf("Info: [ExecuteResponseAction] Connected to host %s. %s IP %s.\n",inet_ntoa(station.agentIP),blockMode==FWSAM_STATUS_BLOCK?"Blocking":"Unblocking",inettoa(blockIP[ipidx]));

						/* now build the packet */
						station.mySeqNo+=station.agentSeqNo; /* increase my seqno by adding agent seq no */
						sampacket.endianCheck=1;						/* This is an endian indicator for Snortsam */
						sampacket.airsSeqNo[0]=(char)station.mySeqNo;
						sampacket.airsSeqNo[1]=(char)(station.mySeqNo>>8);
						sampacket.agentSeqNo[0]=(char)station.agentSeqNo;/* fill station seqno */
						sampacket.agentSeqNo[1]=(char)(station.agentSeqNo>>8);	
						sampacket.status=blockMode;			/* set block action */
						sampacket.version=station.packetVersion;			/* set packet version */
						sampacket.duration[0]=(char)blockDuration;		/* set duration */
						sampacket.duration[1]=(char)(blockDuration>>8);
						sampacket.duration[2]=(char)(blockDuration>>16);
						sampacket.duration[3]=(char)(blockDuration>>24);
						sampacket.responseMode=blockLog|blockHow|FWSAM_WHO_SRC; /* set the mode */
						sampacket.dstIP[0]=(char)blockPeer[peeridx]; /* destination IP */
						sampacket.dstIP[1]=(char)(blockPeer[peeridx]>>8);
						sampacket.dstIP[2]=(char)(blockPeer[peeridx]>>16);
						sampacket.dstIP[3]=(char)(blockPeer[peeridx]>>24);
						sampacket.srcIP[0]=(char)blockIP[ipidx];	/* source IP */
						sampacket.srcIP[1]=(char)(blockIP[ipidx]>>8);
						sampacket.srcIP[2]=(char)(blockIP[ipidx]>>16);
						sampacket.srcIP[3]=(char)(blockIP[ipidx]>>24);
						sampacket.protocol[0]=(char)blockProto;	/* protocol */
						sampacket.protocol[1]=(char)(blockProto>>8);/* protocol */

						if(blockProto==6 || blockProto==17)
						{	sampacket.dstPort[0]=(char)blockPort;
							sampacket.dstPort[1]=(char)(blockPort>>8);
						} 
						else
							sampacket.dstPort[0]=sampacket.dstPort[1]=0;
						sampacket.srcPort[0]=sampacket.srcPort[1]=0;

						sampacket.sig_id[0]=(char)blockSID;		/* set signature ID */
						sampacket.sig_id[1]=(char)(blockSID>>8);
						sampacket.sig_id[2]=(char)(blockSID>>16);
						sampacket.sig_id[3]=(char)(blockSID>>24);

						if(verbose>1)
						{	printf("Debug: [ExecuteResponseAction] Sending %s\n",blockMode==FWSAM_STATUS_BLOCK?"BLOCK":"UNBLOCK");
							printf("Debug: [ExecuteResponseAction] Snort SeqNo:  %x\n",station.mySeqNo);
							printf("Debug: [ExecuteResponseAction] Mgmt SeqNo :  %x\n",station.agentSeqNo);
							printf("Debug: [ExecuteResponseAction] Status     :  %i\n",blockMode);
							printf("Debug: [ExecuteResponseAction] Version    :  %i\n",station.packetVersion);
							printf("Debug: [ExecuteResponseAction] Mode       :  %i\n",blockLog|blockHow|FWSAM_WHO_SRC);
							printf("Debug: [ExecuteResponseAction] Duration   :  %li\n",blockDuration);
							printf("Debug: [ExecuteResponseAction] Protocol   :  %i\n",blockProto);
							printf("Debug: [ExecuteResponseAction] Src IP     :  %s\n",inettoa(blockIP[ipidx]));
							printf("Debug: [ExecuteResponseAction] Src Port   :  %i\n",0);
							printf("Debug: [ExecuteResponseAction] Dest IP    :  %s\n",inettoa(blockPeer[peeridx]));
							printf("Debug: [ExecuteResponseAction] Dest Port  :  %i\n",blockPort);
							printf("Debug: [ExecuteResponseAction] Sig_ID     :  %lu\n",blockSID);
						}

						encbuf=TwoFishAlloc(sizeof(ActionRequestPacket),FALSE,FALSE,station.agentFish); /* get the encryption buffer */
						len=TwoFishEncrypt((char *)&sampacket,(char **)&encbuf,sizeof(ActionRequestPacket),FALSE,station.agentFish); /* encrypt the packet with current key */

						if(send(station.agentSocket,encbuf,len,0)!=len) /* weird...could not send */
						{	fprintf(stderr,"Error: [ExecuteResponseAction] Could not send to host %s.\n",inet_ntoa(station.agentIP));
							closesocket(station.agentSocket);
							error=TRUE;
						}
						else
						{	i=FWSAM_NETWAIT;
							ioctlsocket(station.agentSocket,FIONBIO,&i);	/* set non blocking and wait for  */
							while(i-- >1)							/* the response packet	 */
							{	waitms(10); /* wait for response (default maximum 3 secs */
								if(recv(station.agentSocket,encbuf,len,0)==len)
									i=0; /* if we received packet we set the counter to 0. */
										 /* by the time we check with if, it's already dec'ed to -1 */
							}
							if(!i) /* id we timed out (i was one, then dec'ed)... */
							{	fprintf(stderr,"Error: [ExecuteResponseAction] Did not receive response from host %s.\n",inet_ntoa(station.agentIP));
								closesocket(station.agentSocket);
								error=TRUE;
							}
							else /* got a packet */
							{	decbuf=(char *)&sampacket; /* get the pointer to the packet struct */
								len=TwoFishDecrypt(encbuf,(char **)&decbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,FALSE,station.agentFish); /* try to decrypt the packet with current key */

								if(len!=sizeof(ActionRequestPacket)) /* invalid decryption */
								{	safecopy(station.agentKey ,station.initialKey); /* try the intial key */
									TwoFishDestroy(station.agentFish);
									station.agentFish=TwoFishInit(station.agentKey ); /* re-initialize the TwoFish with the intial key */
									len=TwoFishDecrypt(encbuf,(char **)&decbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,FALSE,station.agentFish); /* try again to decrypt */
									if(verbose>1)
										printf("Debug: [CheckOut] Had to use initial key!\n");
								}
								if(len==sizeof(ActionRequestPacket)) /* valid decryption */
								{	if(sampacket.version==station.packetVersion)/* master speaks my language */
									{	if(sampacket.status==FWSAM_STATUS_OK || sampacket.status==FWSAM_STATUS_NEWKEY 
										|| sampacket.status==FWSAM_STATUS_RESYNC || sampacket.status==FWSAM_STATUS_HOLD) 
										{	station.agentSeqNo=sampacket.agentSeqNo[0] | (sampacket.agentSeqNo[1]<<8); /* get stations seqno */
											station.lastContact=(unsigned long)time(NULL); /* set the last contact time (not used yet) */
											if(verbose>1)
											{
												printf("Debug: [ExecuteResponseAction] Received %s\n",sampacket.status==FWSAM_STATUS_OK?"OK":
																						  sampacket.status==FWSAM_STATUS_NEWKEY?"NEWKEY":
																					      sampacket.status==FWSAM_STATUS_RESYNC?"RESYNC":
																					      sampacket.status==FWSAM_STATUS_HOLD?"HOLD":"ERROR");
												printf("Debug: [ExecuteResponseAction] Snort SeqNo:  %x\n",sampacket.airsSeqNo[0]|(sampacket.airsSeqNo[1]<<8));
												printf("Debug: [ExecuteResponseAction] Mgmt SeqNo :  %x\n",station.agentSeqNo);
												printf("Debug: [ExecuteResponseAction] Status     :  %i\n",sampacket.status);
												printf("Debug: [ExecuteResponseAction] Version    :  %i\n",sampacket.version);
											}

											if(sampacket.status==FWSAM_STATUS_HOLD)
											{	i=FWSAM_NETHOLD;			/* Stay on hold for a maximum of 60 secs (default) */
												while(i-- >1)							/* the response packet	 */
												{	waitms(10); /* wait for response  */
													if(recv(station.agentSocket,encbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,0)==sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE)
													  i=0; /* if we received packet we set the counter to 0. */
										 		}
												if(!i) /* id we timed out (i was one, then dec'ed)... */
												{	fprintf(stderr,"Error: [ExecuteResponseAction] Did not receive response from host %s.\n",inet_ntoa(station.agentIP));
													error=TRUE;
													sampacket.status=FWSAM_STATUS_ERROR;
												}
												else /* got a packet */
												{	decbuf=(char *)&sampacket; /* get the pointer to the packet struct */
													len=TwoFishDecrypt(encbuf,(char **)&decbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,FALSE,station.agentFish); /* try to decrypt the packet with current key */

													if(len!=sizeof(ActionRequestPacket)) /* invalid decryption */
													{	safecopy(station.agentKey ,station.initialKey); /* try the intial key */
														TwoFishDestroy(station.agentFish);
														station.agentFish=TwoFishInit(station.agentKey ); /* re-initialize the TwoFish with the intial key */
														len=TwoFishDecrypt(encbuf,(char **)&decbuf,sizeof(ActionRequestPacket)+TwoFish_BLOCK_SIZE,FALSE,station.agentFish); /* try again to decrypt */
														if(verbose>0)
															printf("Info: [ExecuteResponseAction] Had to use initial key again!\n");
													}
													if(verbose>1)
													{	printf("Debug: [ExecuteResponseAction] Received %s\n", sampacket.status==FWSAM_STATUS_OK?"OK":
																								   sampacket.status==FWSAM_STATUS_NEWKEY?"NEWKEY":
																								   sampacket.status==FWSAM_STATUS_RESYNC?"RESYNC":
																								   sampacket.status==FWSAM_STATUS_HOLD?"HOLD":"ERROR");
														printf("Debug: [ExecuteResponseAction] Snort SeqNo:  %x\n",sampacket.airsSeqNo[0]|(sampacket.airsSeqNo[1]<<8));
														printf("Debug: [ExecuteResponseAction] Mgmt SeqNo :  %x\n",station.agentSeqNo);
														printf("Debug: [ExecuteResponseAction] Status     :  %i\n",sampacket.status);
														printf("Debug: [ExecuteResponseAction] Version    :  %i\n",sampacket.version);
													}
													if(len!=sizeof(ActionRequestPacket)) /* invalid decryption */
													{	fprintf(stderr,"Error: [ExecuteResponseAction] Password mismatch! Ignoring host %s.\n",inet_ntoa(station.agentIP));
														error=TRUE;
														sampacket.status=FWSAM_STATUS_ERROR;
													}
													else if(sampacket.version!=station.packetVersion) /* invalid protocol version */
													{	fprintf(stderr,"Error: [ExecuteResponseAction] Protocol version error! Ignoring host %s.\n",inet_ntoa(station.agentIP));
														error=TRUE;
														sampacket.status=FWSAM_STATUS_ERROR;
													}
													else if(sampacket.status!=FWSAM_STATUS_OK && sampacket.status!=FWSAM_STATUS_NEWKEY && sampacket.status!=FWSAM_STATUS_RESYNC) 
													{	fprintf(stderr,"Error: [ExecuteResponseAction] Funky handshake error! Ignoring host %s.\n",inet_ntoa(station.agentIP));
														error=TRUE;
														sampacket.status=FWSAM_STATUS_ERROR;
													}
												}
											}
											if(sampacket.status==FWSAM_STATUS_RESYNC)  /* if station want's to resync... */
											{	safecopy(station.agentKey ,station.initialKey); /* ...we use the intial key... */
												memcpy(station.agentKeyMod,sampacket.duration,4);	 /* and note the random key modifier */
											}
											if(sampacket.status==FWSAM_STATUS_NEWKEY || sampacket.status==FWSAM_STATUS_RESYNC)	
											{	
												GenerateNewCipherKey(&station,&sampacket); /* generate new TwoFish keys */
												if(verbose>1)
													printf("Debug: [ExecuteResponseAction] Generated new encryption key...\n");
											}
											if(!station.persistentSocket)
												closesocket(station.agentSocket);
										}
										else if(sampacket.status==FWSAM_STATUS_ERROR) /* if SnortSam reports an error on second try, */
										{	closesocket(station.agentSocket);				  /* something is messed up and ... */
											error=TRUE;
											fprintf(stderr,"Error: [ExecuteResponseAction] Undetermined error right after CheckIn! Ignoring host %s.",inet_ntoa(station.agentIP));
										}
										else /* an unknown status means trouble... */
										{	fprintf(stderr,"Error: [ExecuteResponseAction] Funky handshake error! Ignoring host %s.",inet_ntoa(station.agentIP));
											closesocket(station.agentSocket);
											error=TRUE;
										}
									}
									else   /* if the SnortSam agent uses a different packet version, we have no choice but to ignore it. */
									{	fprintf(stderr,"Error: [ExecuteResponseAction] Protocol version error! Ignoring host %s.",inet_ntoa(station.agentIP));
										closesocket(station.agentSocket);
										error=TRUE;
									}
								}
								else /* if the intial key failed to decrypt as well, the keys are not configured the same, and we ignore that SnortSam station. */
								{	fprintf(stderr,"Error: [ExecuteResponseAction] Password mismatch! Ignoring host %s.",inet_ntoa(station.agentIP));
									closesocket(station.agentSocket);
									error=TRUE;
								}
							}
						}
						free(encbuf); /* release of the TwoFishAlloc'ed encryption buffer */
					}
				}
				
				ipidx++;
			}while(!error && ipidx<NUM_HOSTS && blockIP[ipidx]);
			peeridx++;
		}while(!error && peeridx<NUM_HOSTS && blockPeer[peeridx]);

		if(!error)
		{	if(checkout)
				CheckOut(&station);
			else
			{	closesocket(station.agentSocket);
				station.persistentSocket=FALSE;
			}
		}
	}
	TwoFishDestroy(station.agentFish);

	return error;
}

void exittool(int err)
{
#ifdef WIN32
	WSACleanup();
#endif
	exit(err);
}

void header(void)
{	char str[52];
	static int printed=FALSE;
	
	if(verbose && !printed)
	{	safecopy(str,SAMTOOL_REV+11);
	    str[strlen(SAMTOOL_REV+11)-2]=0;
		printf("\nsamtool -- A command line tool for SnortSam -- Version: %s\n\nCopyright (c) 2005-2008 Frank Knobbe <frank@knobbe.us>. All rights reserved.\n\n 2012 Danny Guamán. Some modifications were made on the original source code. It was used as part of a research project at the Polytechnic University of Madrid\n\n",str);
		printed=TRUE;
	}
}
	
int main(int argc, char **argv)
{	int curarg,i,retval=0;
	char *p,str[52];
	struct hostent *hoste;
	struct protoent *protoe;
#ifdef WIN32
	struct WSAData wsad;
#endif
	
	curarg=1;

#ifdef WIN32
	if(WSAStartup(MAKEWORD(1,1),&wsad))				/* intialize winsock */
	{	printf("\nCould not initialize Winsock!\n");
		exit(1);
	}
	if(LOBYTE(wsad.wVersion)!=1 || HIBYTE(wsad.wVersion)!=1)
	{	printf("\nThis Winsock version is not supported!\n");
	    exit(1);
	}
#endif
	
	while(curarg<argc)
	{	p=argv[curarg];
		if(*p=='-')
		{	while(*p=='-')
				p++;
			if(!strncmp(p,"b",1))
			{	blockMode=FWSAM_STATUS_BLOCK;
				curarg++;
			}
			else if(!strncmp(p,"u",1))
			{	blockMode=FWSAM_STATUS_UNBLOCK;
				curarg++;
			}
			else if(!strcmp(p,"v"))
			{	verbose=1;
				curarg++;
			}
			else if(!strcmp(p,"vv"))
			{	verbose=2;
				curarg++;
			}
			else if(!strcmp(p,"n"))
			{	checkout=FALSE;
				curarg++;
			}
			else if(!strcmp(p,"h"))
			{	verbose=TRUE;
				header();
				printf("\nParameters:    -b[lock]        Request a block of the specified IP address.\n\n"
			           "               -u[nblock]      Request the removal of a prior block.\n\n"
			           "               -i[p]           IP address (or host name) to be (un-)blocked.\n\n"
			           "               -du[ration]     Amount of seconds for the block. If you\n"
			           "                               enclose in quotes, you can use the usual time\n"
			           "                               abbreviations like sec and min.\n"
			           "                               (Default: 0   which means permanent block)\n\n"
			           "               -di[rection]    Can be:   in         Block inbound only.\n"
			           "                                         out        Block outbound only.\n"
			           "                                         full,both  Block in- and outbound.\n"
			           "                                         this,conn  Block specific connection.\n"
			           "                               (Default: full)\n\n"
			           "                               Note: Only some firewalls support connections or\n"
			           "                                     directional blocking.\n\n"
			           "               -log            Can be:   0      No logs on blocked packets.\n"
			           "                                         1-4    Log packets. (Most firewalls\n"
			           "                                                just log. Firewall-1 has 4 log\n"
			           "                                                options/levels.)\n"
			           "                               (Default: 0)\n\n"
			           "               -sid            Optional SID number to be passed for logging.\n"
			           "                               (Default: 0)\n\n"
			           "               -v[erbose]      Print additional information.\n\n"
			           "               -vv             Very verbose: Print Debug level output.\n\n"
			           "               -n              No disconnect: This tool will check-in into\n"
			           "                               Snortsam, block, and then check-out, thereby\n"
			           "                               removing any tables associated with this IP\n"
			           "                               address. By adding -n, this tool will not\n"
			           "                               check-out, thus preserving any tables (useful\n"
			           "                               when run from a Snort sensor).\n"
			           "                               Note: Causes harmless resync warnings.)\n\n"
			           "If the block type is \"CONNection\", the following options are required:\n\n"
			           "               -pe[er]         The peer IP address in that connecion.\n\n"
			           "               -pr[oto]        The IP protocol of the session (TCP, UDP, 6, 17)\n\n"
			           "               -po[rt]         Destination port of that session.\n\n\n"
			           "The rest of the command line are one or more Snortsam stations listed using the\n"
			           "same syntax as in the Snort configuration:  <host:port/password>\n\n\n"
			           "Examples:   airsExecutor -block -ip 10.1.110.3 -dur 300  fw.upm.es\n"
			           "            airsExecutor -unblock ip 10.1.110.5 fw.upm.es/m1p455w0rd\n"
			           "            airsExecutor -b -ip 1.2.3.4 -dur \"10 min\" -dir conn -peer 10.2.0.4   \\ \n"
			           "                     -proto tcp -port 80 -log 1 -sid 1234  fw.upm.es:908/m1p455w0rd\n"
			           "            airsExecutor -b -ip mail.spam.com -dur 5days fw1.upm.es r1.upm.es\n\n");
				exittool(0);
			}
			else if(!strncmp(p,"i",1) || !strncmp(p,"a",1))
			{	if(++curarg <argc)
				{	if(inet_addr(argv[curarg])==INADDR_NONE)
					{	hoste=gethostbyname(argv[curarg]);
						if (!hoste) 
						{	fprintf(stderr,"Error: Unable to resolve block host '%s'!\n",argv[curarg]);
							exittool(11);
						}
						else
						{	i=0;
							do
							{	if(hoste->h_addr_list[i])
									blockIP[i]=*((unsigned long *)hoste->h_addr_list[i]);
								else
									blockIP[i]=0;
								i++;
							}while(i<NUM_HOSTS && blockIP[i-1]);
						}
					} 
					else
					{	blockIP[0]=inet_addr(argv[curarg]);
						blockIP[1]=0;
						if(!blockIP[0])
						{	fprintf(stderr,"Error: Invalid block address '%s'!\n",argv[curarg]);
							exittool(12);
						}
					}
					curarg++;
				}
				else
				{	fprintf(stderr,"Error: Block host not specified!\n");
					exittool(22);
				}
			}
			else if(!strncmp(p,"pe",2))
			{	if(++curarg <argc)
				{	if(inet_addr(argv[curarg])==INADDR_NONE)
					{	hoste=gethostbyname(argv[curarg]);
						if (!hoste) 
						{	fprintf(stderr,"Error: Unable to resolve peer host '%s'!\n",argv[curarg]);
							exittool(13);
						}
						else
						{	i=0;
							do
							{	if(hoste->h_addr_list[i])
									blockPeer[i]=*((unsigned long *)hoste->h_addr_list[i]);
								else
									blockPeer[i]=0;
								i++;
							}while(i<NUM_HOSTS && blockPeer[i-1]);
						}
					} 
					else
					{	blockPeer[0]=inet_addr(argv[curarg]);
						blockPeer[1]=0;
						if(!blockIP[0])
						{	fprintf(stderr,"Error: Invalid peer address '%s'!\n",argv[curarg]);
							exittool(14);
						}
					}
					curarg++;
				}
				else
				{	fprintf(stderr,"Error: Peer IP not specified!\n");
					exittool(24);
				}
			}
			else if(!strncmp(p,"pr",2))
			{	if(++curarg <argc)
				{	if(atol(argv[curarg])>0)
						blockProto=atol(argv[curarg])&65535;
					else
					{	protoe=getprotobyname(argv[curarg]);
						if(!protoe)
						{	fprintf(stderr,"Error: Invalid protocol '%s'!\n",argv[curarg]);
							exittool(16);
						}
						blockProto=protoe->p_proto;
					}
					curarg++;
				}
				else
				{	fprintf(stderr,"Error: Protocol not specified!\n");
					exittool(26);
				}
			}
			else if(!strncmp(p,"po",2) || !strncmp(p,"dp",2) || !strncmp(p,"dst",3) || !strncmp(p,"dest",4))
			{	if(++curarg <argc)
				{	if(atol(argv[curarg])>0)
						blockPort=atol(argv[curarg])&65535;
					else
					{	fprintf(stderr,"Error: Invalid port specified!\n");
						exittool(15);
					}
					curarg++;
				}
				else
				{	fprintf(stderr,"Error: Port not specified!\n");
					exittool(26);
				}
			}
			else if(!strncmp(p,"du",2))
			{	if(++curarg <argc)
				{	safecopy(str,argv[curarg]);
					blockDuration=parseduration(str);
					curarg++;
				}
				else
				{	fprintf(stderr,"Error: Duration not specified!\n");
					exittool(27);
				}
			}
			else if(!strncmp(p,"sid",3) || !strncmp(p,"id",2))
			{	if(++curarg <argc)
				{	blockSID=atol(argv[curarg]);
					curarg++;
				}
				else
				{	fprintf(stderr,"Error: SID not specified!\n");
					exittool(28);
				}
			}
			else if(!strncmp(p,"di",2))
			{	if(++curarg <argc)
				{	if(!strcmp(argv[curarg],"in"))
						blockHow=FWSAM_HOW_IN;
					else if(!strcmp(argv[curarg],"out"))
						blockHow=FWSAM_HOW_OUT;
					else if(!strcmp(argv[curarg],"inout") || !strcmp(argv[curarg],"both") || !strcmp(argv[curarg],"full"))
						blockHow=FWSAM_HOW_INOUT;
					else if(!strcmp(argv[curarg],"this") || !strncmp(argv[curarg],"conn",4))
						blockHow=FWSAM_HOW_THIS;
					else
					{	fprintf(stderr,"Error: Invalid direction specified!\n");
						exittool(17);
					}
					curarg++;
				}
				else
				{	fprintf(stderr,"Error: Direction not specified!\n");
					exittool(29);
				}
			}
			else if(!strncmp(p,"log",3))
			{	if(++curarg <argc)
				{	snprintf(str,20,"%s1",argv[curarg]);
					i=atol(str);
					if(i>=1 && i<=41)
					{	blockLog=(i/10)&255;
						curarg++;
					}
					else
					{	fprintf(stderr,"Error: Invalid Log level specified!\n");
						exittool(18);
					}
				}
				else
				{	fprintf(stderr,"Error: Log level not specified!\n");
					exittool(30);
				}
			}
			else
			{	fprintf(stderr,"Error: Invalid option specified!\n");
				exittool(19);
			}
		}
		else
		{	if(!blockIP[0])
			{	fprintf(stderr,"Error: Block IP address not specified!\n");
				exittool(40);
			}
			if(blockHow==FWSAM_HOW_THIS)
			{	if(!blockPeer[0])
				{	fprintf(stderr,"Error: Peer IP address not specified!\n");
					exittool(41);
				}
				if(!blockPort)
				{	fprintf(stderr,"Error: Destination port not specified!\n");
					exittool(42);
				}
				if(!blockProto)
				{	fprintf(stderr,"Error: IP protocol not specified!\n");
					exittool(43);
				}
			}
			header();
			retval|=ExecuteResponseAction(argv[curarg]);
			curarg++;
		}
	}
#ifdef WIN32
        WSACleanup();
#endif
	return retval;
}

#undef FWSAMDEBUG
#endif /* __AIRSEXECUTOR_C__ */
