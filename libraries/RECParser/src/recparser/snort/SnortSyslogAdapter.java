/**
 * "SnortSyslogAdapter" Java class is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version always keeping
 * the additional terms specified in this license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 *
 * Additional Terms of this License
 * --------------------------------
 *
 * 1. It is Required the preservation of specified reasonable legal notices
 *   and author attributions in that material and in the Appropriate Legal
 *   Notices displayed by works containing it.
 *
 * 2. It is limited the use for publicity purposes of names of licensors or
 *   authors of the material.
 *
 * 3. It is Required indemnification of licensors and authors of that material
 *   by anyone who conveys the material (or modified versions of it) with
 *   contractual assumptions of liability to the recipient, for any liability
 *   that these contractual assumptions directly impose on those licensors
 *   and authors.
 *
 * 4. It is Prohibited misrepresentation of the origin of that material, and it is
 *   required that modified versions of such material be marked in reasonable
 *   ways as different from the original version.
 *
 * 5. It is Declined to grant rights under trademark law for use of some trade
 *   names, trademarks, or service marks.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (lgpl.txt).  If not, see <http://www.gnu.org/licenses/>
 */

package recparser.snort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import recparser.Address;
import recparser.DateToXsdDatetimeFormatter;
import recparser.IntrusionAlert;
import recparser.IntrusionSource;
import recparser.IntrusionTarget;
import recparser.util.PropsUtil;

/**
 * This class represents an adapter which converts an intrusion alert from snort
 * in syslog format to the common format used in RECLAMO.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class SnortSyslogAdapter {

    private String netMask;
    private String intrusionCount;
    private String IDSID;
    private String targetOfIntrusionID;
    private String intrusionDetectionTime;
    private int intrusionSeverity;
    private static String intrusionID;
    private String intrusionType;
    private String nextIntrusionID;
    private String intrusionAlertCreateTime;
    private String sourceOfIntrusionID;
    private int portDest;
    private String detectionTime;
    private int portSrc;
    private PropsUtil props = new PropsUtil();
    public IntrusionAlert _intAlert;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_HIGH_VALUE = 10;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_MEDIUM_VALUE = 5;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_LOW_VALUE = 1;

    public SnortSyslogAdapter() {
        _intAlert = new IntrusionAlert();
    
        
    } // SnortSyslogAdapter

    public IntrusionAlert parseAlert(String alert, String netMask, String detectionTime) {
        long initParser = System.currentTimeMillis();
        System.out.println("*** SnortSyslogAdapter - START ***");
        this.netMask = netMask;
        this.detectionTime = detectionTime;
       
        try {
            int i1 = alert.indexOf(">", 0);
            //System.out.println("SnortSyslogAdapter: alerta recibida para parsear --> "+alert);
            int i2 = alert.indexOf(" ", i1 + 1);
            String process = alert.substring(i1 + 1, i2-1);
            i2 = alert.indexOf("[", i1+1);
            i1 = alert.indexOf("]", i2 + 1);
            String pid = alert.substring(i2 + 1, i1);
            i2 = alert.indexOf(" ", i1 + 1);
            i1 = alert.indexOf("[", i2 + 1);
            String message = alert.substring(i2 + 1, i1);
            if(message.startsWith("(snort_decoder)")){
                System.out.println("the message starts with snort_decoder");
                return _intAlert;
            }
            else if(message.startsWith("(portscan)")){
                System.out.println("the message starts with portscan....");
                return _intAlert;
            }else if(process.startsWith("CRON")){
            //    System.out.println("Internal message");
                return _intAlert;
            }
            else {
            i2 = alert.indexOf("[Classification: ", i1);
            i1 = alert.indexOf("]", i2 + 1);
            String classtype = alert.substring(i2 + 17, i1);
            _intAlert.setIntName(classtype);
            //String path = "/"+getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            InputStream i = SnortSyslogAdapter.class.getClassLoader().getResourceAsStream(props.getSnortIntrusionClassificationFile());
//            String path = SnortSyslogAdapter.class.getClassLoader().getResourceAsStream("/"+props.getSnortIntrusionClassificationFile()).toString();
            //classtype=this.obtainParameter(path+props.getSnortIntrusionClassificationFile(), classtype);
            classtype=this.obtainIntrusionType(i, classtype);
            //System.out.println("Classtype: "+classtype);
            i2 = alert.indexOf("[Priority: ", i1 + 1);
            i1 = alert.indexOf("]",i2+1);
            String priority = alert.substring(i2+11,i1);
            //System.out.println("Priority antes de procesar: "+priority);
            InputStream streamdos = SnortSyslogAdapter.class.getClassLoader().getResourceAsStream(props.getSnortIntrusionClassificationFile());
            priority = this.obtainIntrusionType(streamdos, priority);
            //System.out.println("Prioridad tras procesar: "+priority);
            //priority=this.obtainParameter(path+props.getSnortIntrusionClassificationFile(), priority);
            i2 = alert.indexOf("}", i1 + 1);
            String protocol = alert.substring(i1 + 3, i2);
            i1 = alert.indexOf("->", i2 + 1);
            String source = alert.substring(i2 + 2, i1).trim();

            if(source.indexOf(":") > -1){
                int i3 = source.indexOf(":");
                int i4 = 0;
                String dirIPSrc = source.substring(i4, i3);
                portSrc = Integer.parseInt(source.substring(i3+1, source.length()).trim());
                source = dirIPSrc;
            }
            String destination = alert.substring(i1 + 3, alert.length()).trim();

            if(destination.indexOf(":")> -1){
                int i3 = destination.indexOf(":");
                int i4 = 0;
                String dirIPDest = destination.substring(i4, i3);
                portDest = Integer.parseInt(destination.substring(i3+1, destination.length()).trim());
                destination = dirIPDest;
            }
            

	    //Se crea un nuevo objeto IntrusionAlert y se asignan valores.
	    DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
            String currentDate = xdf.format(new Date());
            intrusionCount = currentDate.replace(":", "").replace("-", "");
            _intAlert.setIntCount ( intrusionCount);
            
            intrusionCount = this.detectionTime.replace(":", "").replace("-", "");
            intrusionDetectionTime = this.detectionTime;
            _intAlert.setIntDetectionTime(intrusionDetectionTime);
            intrusionAlertCreateTime = currentDate;
            _intAlert.setIntAlertCreateTime(intrusionAlertCreateTime);

            IDSID = pid;
            _intAlert.setAnalyzerID(IDSID);

            targetOfIntrusionID = destination;
            IntrusionTarget target1 = new IntrusionTarget();
            Address address = new Address();
            address.setAddress(targetOfIntrusionID);
            target1.setNode(address);
            target1.setServicePort(portDest);
            _intAlert.setIntrusionTarget(target1);

            //intrusionID = classtype+intrusionCount;
            intrusionID = IDSID+intrusionCount; //Modificado: intID = analyzerID+intrusionCount
            _intAlert.setIntID(intrusionID);

            intrusionType = classtype;
            _intAlert.setIntType(intrusionType);

            intrusionSeverity=Integer.parseInt(priority);
            _intAlert.setIntSeverity(intrusionSeverity);
                   
            //intrusionImpact =
              //      intrusionSeverity * GetRelevance(targetOfIntrusionID);
            //_intAlert.setIntImpact(intrusionImpact);

            //IDSconfidence = GetReliability(process);
  //          System.out.println(IDSconfidence);
            _intAlert.setAnalyzerConfidence(0.0); //Valor que indica que hay que calcularlo utilizando info del contexto

           // attackerLocation = GetLocation(source);


            sourceOfIntrusionID = source;
            IntrusionSource intSource = new IntrusionSource();
            Address addressSource = new Address();
            addressSource.setAddress(sourceOfIntrusionID);
            intSource.setNode(addressSource);
            intSource.setPortSrc(portSrc);
            _intAlert.setIntrusionSource(intSource);
            
            //_intAlert.setProtocol(protocol);
            _intAlert.setNextIntID(nextIntrusionID);
            long endParser = System.currentTimeMillis();
            System.out.println("*** SnortSyslogAdapter END *** Parsing time : "+ (endParser-initParser)+" (ms)*** ");
            return _intAlert;

        }} // try
        catch(Exception e) {
            e.printStackTrace();
            IntrusionAlert empty = new IntrusionAlert();
            return empty;
        } // catch

    } // parseAlert

    private int GetIntrusionSeverity(String classtype) {
        if(classtype.equals("attempted-admin"))
            return 4;
        else if(classtype.equals("attempted-user"))
            return 4;
        else if(classtype.equals("kickass-porn"))
            return 4;
        else if(classtype.equals("policy-violation"))
            return 4;
        else if(classtype.equals("shellcode-detect"))
            return 4;
        else if(classtype.equals("successful-admin"))
            return 4;
        else if(classtype.equals("successful-user"))
            return 4;
        else if(classtype.equals("trojan-activity"))
            return 4;
        else if(classtype.equals("unsuccessful-user"))
            return 4;
        else if(classtype.equals("web-application-attack"))
            return 4;
        else if(classtype.equals("attempted-dos"))
            return 3;
        else if(classtype.equals("attempted-recon"))
            return 3;
        else if(classtype.equals("bad-unknown"))
            return 3;
        else if(classtype.equals("default-login-attempt"))
            return 3;
        else if(classtype.equals("denial-of-service"))
            return 3;
        else if(classtype.equals("misc-attack"))
            return 3;
        else if(classtype.equals("non-standard-protocol"))
            return 3;
        else if(classtype.equals("rpc-portmap-decode"))
            return 3;
        else if(classtype.equals("successful-dos"))
            return 3;
        else if(classtype.equals("successful-recon-largescale"))
            return 3;
        else if(classtype.equals("successful-recon-limited"))
            return 3;
        else if(classtype.equals("suspicious-filename-detect"))
            return 3;
        else if(classtype.equals("suspicious-login"))
            return 3;
        else if(classtype.equals("system-call-detect"))
            return 3;
        else if(classtype.equals("unusual-client-port-connection"))
            return 3;
        else if(classtype.equals("web-application-activity"))
            return 3;
        else if(classtype.equals("icmp-event"))
            return 2;
        else if(classtype.equals("misc-activity"))
            return 2;
        else if(classtype.equals("network-scan"))
            return 2;
        else if(classtype.equals("not-suspicious"))
            return 2;
        else if(classtype.equals("protocol-command-decode"))
            return 2;
        else if(classtype.equals("string-detect"))
            return 2;
        else if(classtype.equals("unknown"))
            return 2;
        else if(classtype.equals("tcp-connection"))
            return 1;

        return 0;
    } // GetIntrusionSeverity
   
    private String obtainIntrusionType(InputStream i, String name){
        String s=null;
        //File f = new File( path );
        BufferedReader entrada= new BufferedReader(new InputStreamReader(i));

        //if (entrad.exists()){
        String texto=null;
            do {
                try {
                    texto = entrada.readLine();
                    if (texto != null && !texto.startsWith("#")) {
                        s=procesarLinea(texto,name);
                        //System.out.println("Texto: "+texto+" ; s: "+s);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SnortSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);

                }
            } while(texto!=null && s==null);
    //}
    return s;
}

    private String obtainParameter(String path, String name){
    String s=null;
    File f = new File( path );
    BufferedReader entrada=null;
        try {
            entrada = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException ex) {
             //System.out.println("Could not read AIRS config. (File: " + AIRS_PROPERTIES_FILE );
            throw new RuntimeException("Could not read file config. (File: " + path + ")", ex);
        } 
        if (f.exists()){
        String texto=null;
            do {
                try {
                    texto = entrada.readLine();
                    if (texto != null && !texto.startsWith("#")) {
                        s=procesarLinea(texto,name);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SnortSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);

                }
                } while(texto!=null && s==null);
    }
    return s;
}

    private String procesarLinea(String texto, String name) {
        String parameter=null;
        if(texto.startsWith(name)){
            parameter=texto.substring(texto.indexOf("=")+1);
        }
        return parameter;
    }
}
