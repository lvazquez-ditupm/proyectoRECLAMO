/* communication.h
 *
 *
 * Copyright (c) 2001-2009 Frank Knobbe <frank@knobbe.us>
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
 * Comments:
 * 
 * Several modifications were made for Danny Guamán <ds.guaman@alumnos.upm.es, danny.guamanl@gmail.com> 2012
 * It tool was used to build a module part of Based Ontologies Automated Intrusion Detection System. It's part of a project research
 * at Techical University of Madrid.
 *
 *
 */

#ifndef		__COMMUNICATION_H__
#define		__COMMUNICATION_H__

#ifdef	_DEBUG
#define FWSAMDEBUG
#endif
#ifdef	DEBUG
#define FWSAMDEBUG
#endif


#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <string.h>
#include <signal.h>
#include <ctype.h>
#include <assert.h>
#include <errno.h>
#include <sys/types.h>


#ifdef WIN32		/* ------------------ Windows platform specific stuff ----------------------- */

#include "win32_service.h"
#include <winsock.h>

/* 	included to provide compatibility with plugins not written under Windows 
	(although I'm mainly developing under FreeBSD now...)*/

#define SIGKILL				9		/* kill (cannot be caught or ignored) */
#define SIGQUIT				3		/* quit */
#define SIGHUP 				1		/* hangup */
#define SIGUSR1				30		/* user defined signal 1 */
#define SIGUSR2 				31		/* user defined signal 2 */
#define SIGPIPE 				13		/* write on a pipe with no one to read it */
#define strncasecmp			strnicmp
#define strcasecmp			stricmp
#define snprintf 			_snprintf
#define vsnprintf 			_vsnprintf
#define bzero(x, y) 			memset((x), 0, (y))
#define execv    			_execv
#define getpid  				_getpid
#define index  				strchr
#define bcopy(x, y, z) 		memcpy((void *)x, (const void *)y, (size_t) z)
#define mkdir(x, y) 			_mkdir(x)
#define read					_read
#define write				_write
#define lseek				_lseek

#ifndef ssize_t
typedef size_t ssize_t;
#endif
#ifndef pid_t
typedef int pid_t;
#endif
#ifndef pthread_mutex_t
typedef HANDLE pthread_mutex_t;
#endif
#ifndef pthread_t
typedef HANDLE pthread_t;
#endif


#ifndef u_long
typedef unsigned long u_long;
#endif
#ifndef u_int32_t
typedef unsigned long u_int32_t;
#endif
#ifndef u_word
typedef unsigned short u_word;
#endif
#ifndef u_int16_t
typedef unsigned short u_int16_t;
#endif
#ifndef u_char
typedef unsigned char u_char;
#endif
#ifndef u_int8_t
typedef unsigned char u_int8_t;
#endif



#else		/* ------------------ Other platform specific stuff ----------------------- */

#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/ioctl.h>		
#include <netdb.h>
#include <pthread.h>

#ifdef SOLARIS
#include <sys/filio.h>
#ifndef _uint_defined
#include <stdint.h>
typedef uint32_t u_int32_t;
typedef uint16_t u_int16_t;
typedef uint8_t u_int8_t;
#define _uint_defined
#endif /* _uint_defined */
#endif



#define stricmp			strcasecmp
#define strnicmp		strncasecmp		

/* PLUGIN WRITER: Please use the following for socket stuff */
typedef int				SOCKET;
#define ioctlsocket		ioctl
#define closesocket		close

#endif		/* ------------------ End platform specific stuff ----------------------- */



#include "twofish.h"


/* compatibilty stuff */
#ifndef INVALID_SOCKET
#define INVALID_SOCKET	-1
#endif
#ifndef INADDR_NONE
#define INADDR_NONE	-1
#endif

#ifndef	FALSE
#define FALSE	0
#endif
#ifndef	TRUE
#define	TRUE	!FALSE
#endif
#ifndef	bool
#define	bool	int
#endif



#ifndef _MYLIBCSTUFF
#define myisdigit(x) isdigit(x)
#define myisspace(x) isspace(x)
#define mytolower(x) tolower(x)
#endif



/* defines */
#define safecopy(dst,src)		_safecp(dst,sizeof(dst),src)
#define STRBUFSIZE				1024
#define FWSAM_DEFAULTPORT		898	/* Default port if user does not specify one in airsResponseExecutor.conf*/ 		
#define FWSAM_PACKETVERSION		14
#define FWSAM_PACKETVERSION_PERSISTENT_CONN		15

#define FWSAM_STATUS_CHECKIN		1	/* MCER to executorAgent */
#define FWSAM_STATUS_CHECKOUT		2
#define FWSAM_STATUS_BLOCK		3
#define FWSAM_STATUS_UNBLOCK		9

#define FWSAM_STATUS_OK			4	/* executorAgent to MCER */
#define FWSAM_STATUS_ERROR		5
#define FWSAM_STATUS_NEWKEY		6
#define FWSAM_STATUS_RESYNC		7
#define FWSAM_STATUS_HOLD		8

#define FWSAM_FW_BLOCK			0  //modified AIRS 
#define FWSAM_FW_REDIRECT		1  //modified AIRS
#define FWSAM_FW_RESTORE		2  //modified AIRS
#define FWSAM_FW_ICMP			3  //modified AIRS
#define FWSAM_FW_TCP_RST		4  //modified AIRS
#define FWSAM_FW				(FWSAM_FW_BLOCK|FWSAM_FW_REDIRECT|FWSAM_FW_RESTORE|FWSAM_FW_ICMP|FWSAM_FW_TCP_RST) //modified AIRS
#define	FWSAM_WHO_DST			8
#define FWSAM_WHO_SRC			16
#define FWSAM_WHO				(FWSAM_WHO_DST|FWSAM_WHO_SRC)
#define FWSAM_HOW_IN			32
#define FWSAM_HOW_OUT			64
#define FWSAM_HOW_INOUT			(FWSAM_HOW_IN|FWSAM_HOW_OUT)
#define FWSAM_HOW_THIS			128
#define FWSAM_HOW				(FWSAM_HOW_IN|FWSAM_HOW_OUT|FWSAM_HOW_THIS)

#define SAMTOOL_REV                    "$Revision: 1.10 $"

#define NUM_HOSTS				255		/* We cache up to this many IPs for a name */
#define FWSAM_NETWAIT			1000		/* 100th of a second. 10 sec timeout for network connections */
#define FWSAM_NETHOLD			6000		/* 100th of a second. 60 sec timeout for holding */

//DEFINICIÒN DE LONGITUD DE NUEVOS CAMPOS
#define USERLENGTH  			15
#define PLUGINLENGTH 			 2
#define ADLENGTH  				20

/* Variable Definitions */
/*It contains request packet options will be sent to executor agent, if you need to add a new option (filename, PID, etc) y should be added here */
typedef struct _actionRequestPacket				/* 2 blocks (3rd block is header from TwoFish) */
{	unsigned short		endianCheck;		/* 0  */
	unsigned char		srcIP[4];		/* 2  */
	unsigned char		dstIP[4];		/* 6  */
	unsigned char		duration[4];		/* 10 */
	unsigned char		airsSeqNo[2];		/* 14 */
	unsigned char		agentSeqNo[2];		/* 16 */
	unsigned char		srcPort[2];		/* 18 */					
	unsigned char		dstPort[2];		/* 20 */
	unsigned char		protocol[2];		/* 22 */
	unsigned char		responseMode;		/* 24 */
	unsigned char		version;		/* 25 */
	unsigned char		status;			/* 26 */
	unsigned char		sig_id[4];		/* 27 */
	unsigned char		fluff;			/* 31 */
	unsigned char 		plugin[2];          /* 32 */
	char       			user[USERLENGTH];       /*34*/
	char 				adParam[ADLENGTH];		/*49*/
}	ActionRequestPacket;							/* 64 bytes in size */

/* structure of an executor agent, it used for communication module to establish connection from MCER to Executor Agent */
typedef struct _executorAgent		
{	unsigned short 			mySeqNo;
	unsigned short 			agentSeqNo;
	unsigned char			myKeyMod[4];
	unsigned char			agentKeyMod[4];
	unsigned short			agentPort;
	struct in_addr			agentIP;
	struct sockaddr_in		localSocketAddr;
	struct sockaddr_in		stationSocketAddr;
	SOCKET				agentSocket;		/* the socket of that station */
	TWOFISH				*agentFish;
	char				initialKey[TwoFish_KEY_LENGTH+2];
	char				agentKey [TwoFish_KEY_LENGTH+2];
	time_t				lastContact;
	int				persistentSocket; /* Flag for permanent connection */
	unsigned char			packetVersion;	/* The packet version the sensor uses. */
}	ExecutorAgent;


/* Globals */

unsigned long blockIP[NUM_HOSTS +1],blockPeer[NUM_HOSTS +1],blockDuration=0,blockSID=0;
unsigned short blockPort=0,blockProto=0,blockFwType=FWSAM_FW_BLOCK,blockWho=FWSAM_WHO_SRC,blockHow=FWSAM_HOW_INOUT,blockMode=FWSAM_STATUS_BLOCK,blockPlugin=0,verbose=0,checkout=TRUE;
char *blockUser = 0;
char *blockAdditional = 0;

/* Functions */


#ifdef _MYLIBCSTUFF
char mytolower(char c);
int myisspace(unsigned char c);
int myisdigit(char c);
#endif

void waitms(unsigned int);
void _safecp(char *dst,unsigned long max,char *src);
void GenerateNewCipherKey(ExecutorAgent *station,ActionRequestPacket *packet);
void CheckOut(ExecutorAgent *station);
int CheckIn(ExecutorAgent *station);
int ExecuteResponseAction(char *arg);
void remspace(char *str);
unsigned long parseduration(char *p);
char *inettoa(unsigned long ip);
int main(int argc, char **argv);

#endif  /* __AIRSEXECUTOR_H__ */

