/**
 * "AlertReceiver" Java class is free software: you can redistribute
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

package ontairs;

import airs.responses.executor.CentralModuleExecution;
import ontairs.utils.DateToXsdDatetimeFormatter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ontairs.utils.PropsUtil;

/**
 * This class represents an receiver of intrusion alerts coming from different 
 * IDSs and from the RepCIDN. This receiver maps the received alerts to a IDMEF 
 * format.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class AlertReceiver {
    
    private PropsUtil props = new PropsUtil();
    private String netMask;
    private DatagramSocket socket;
    public static String fname;
    public static Object lck = new Object();
    byte[] alert;
    String alertID;
    boolean received_alert = false;
    CentralModuleExecution MCER;

    public AlertReceiver(int port, String airsIP, String netMask) {
        System.out.println("AlertReceiver--esperando alertas");
        try {
           // socket = new DatagramSocket(port, InetAddress.getByName("192.168.1.39"));
            socket = new DatagramSocket(port, InetAddress.getByName(airsIP));
        } // try
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        } // catch
        
        this.netMask = netMask;
        this.MCER = new CentralModuleExecution();

    } 
    
    public void Start() {

        System.out.println("______________________________________________");
        System.out.println("Start- method. AlertReceiver");
        System.out.println("AlertReceiver arrancado.");
        //ExecutorService exec = Executors.newFixedThreadPool(4);
       // while(true) {
        
        Receiver r;
        r = new Receiver();
        ReceiveSyslogAlert s = new ReceiveSyslogAlert();
        ReceivePreludeAlert p = new ReceivePreludeAlert();

        new Thread(r).start();
        new Thread(s).start();
        new Thread(p).start();

    } // Start
    
    synchronized String getAlert() {
        if(!received_alert){
            try {
                wait();
            } catch(InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
       // System.out.println("Alerta recibida por alguno de los dos medios"+ alertID);
             
        received_alert = false;
        notify();
        return alertID;        
    }
    
    synchronized void putAlert(String alertID) {
        if(received_alert){
            try {
                wait();
            } catch(InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
        this.alertID = alertID;
        received_alert = true;
        //System.out.println("Alerta añadida: " + alertID);
        notify();
    } 
       
    class Receiver implements Runnable {
        ExecutorService exec;
        public Receiver(){
            exec = Executors.newFixedThreadPool(4);
        }        
        public void run() {
           // System.out.println("arrancando Receiver");
            try {
                while (true) {
                    //synchronized (this) {
                    //System.out.println("esperando alertas recibidas");
                    String alertarecibida =getAlert(); 
                    try{
                        DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
                        String currentDate = xdf.format(new Date());
                        System.out.println("AlertReceiver: nueva alerta detectada:" +alertarecibida);
                        Runnable nuevaAlerta = new OntAIRS(alertarecibida, netMask, currentDate, MCER);
                        exec.execute(nuevaAlerta);
                    }
                    catch(Exception e){
                
                    }
                }
            } catch (Exception ex) {}
        }
    }
    
    class ReceiveSyslogAlert implements Runnable {
            public void run() {
             
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    while(true) {                        
                        socket.receive(packet);
                        alert = packet.getData();
                    //    System.out.println("syslogrecibido");
                        String a = new String(alert, 0, alert.length);
                        putAlert(a);
                    } // while
                } // try
                catch (Exception e) {
                    e.printStackTrace();
                    //return false;
                } // catch               
            }
        }
    
    class ReceivePreludeAlert implements Runnable {
              
         
        public void run() {
            fname = props.getPreludeLogFilePathValue();
            //fname = "/var/log/prelude.log";
            boolean isARealAlert = true;
            // File content change listener 
            try
            {
                BufferedReader br = new BufferedReader( new FileReader( fname ) );
                String s;
                StringBuilder buf = new StringBuilder();
                int alertnumber =0;
                int linesnumber=0;
                while(true){                     
                    //System.out.println("receivedPreludeAlert");
                    s = br.readLine();
                    if( s == null ){
                        synchronized( lck ){
                            lck.wait( 500 );
                        }
                    }
                    else{
                       linesnumber++;
                       String prelude_alerts_path = props.getPreludeAlertsPathValue();
                       File f = new File(prelude_alerts_path+"/alert"+alertnumber);
                       if(!f.exists()){
                           f.createNewFile();
                       }
                       FileWriter fw = new FileWriter(f, true);
                       BufferedWriter bw = new BufferedWriter(fw);
                       bw.write(s);
                       bw.newLine();
                       bw.close();                
                      // System.out.println( s );
                       if (linesnumber==2 && !s.startsWith("* Alert")){
                          isARealAlert = false;
                       }
                       if(s.equals("")){
                           linesnumber=0;
                           if(isARealAlert){
                            alertnumber++;
                         //   System.out.println("Alerta real");
                       //     System.out.println("Alerta prelude recibida");
                            putAlert(f.getName());

                           }else{
                           //     System.out.println("NO es una alerta");
                                fw.close();
                                f.delete();
                                isARealAlert = true;
                           }
                       }
                    }
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
             //   return false;
            }
        }
    }
}