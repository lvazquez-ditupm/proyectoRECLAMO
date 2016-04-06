/**
 * "AlertReceiver" Java class is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version always keeping the additional terms
 * specified in this license.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *
 * Additional Terms of this License --------------------------------
 *
 * 1. It is Required the preservation of specified reasonable legal notices and
 * author attributions in that material and in the Appropriate Legal Notices
 * displayed by works containing it.
 *
 * 2. It is limited the use for publicity purposes of names of licensors or
 * authors of the material.
 *
 * 3. It is Required indemnification of licensors and authors of that material
 * by anyone who conveys the material (or modified versions of it) with
 * contractual assumptions of liability to the recipient, for any liability that
 * these contractual assumptions directly impose on those licensors and authors.
 *
 * 4. It is Prohibited misrepresentation of the origin of that material, and it
 * is required that modified versions of such material be marked in reasonable
 * ways as different from the original version.
 *
 * 5. It is Declined to grant rights under trademark law for use of some trade
 * names, trademarks, or service marks.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (lgpl.txt). If not, see
 * <http://www.gnu.org/licenses/>
 */
package recontairs;

import airs.responses.executor.CentralModuleExecution;
import recontairs.utils.DateToXsdDatetimeFormatter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import recontairs.utils.PropsUtil;
import recparser.IntrusionAlert;
import recparser.idmef.IdmefParser;
import recparser.ossec.OssecSyslogAdapter;
//import recparser.ossec.OssecSyslogAdapter_idmef;
import recparser.snort.SnortSyslogAdapter;

/**
 * This class represents an receiver of intrusion alerts coming from different
 * IDSs and from the RepCIDN. This receiver maps the received alerts to alert
 * IDMEF format.
 *
 * @author UPM (member of RECLAMO Development Tealertm)
 * (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class AlertReceiver {

    private PropsUtil props = new PropsUtil();
    private String netMask;
    private DatagramSocket socketUDP;
    private ServerSocket socketTCP;
    public static String fname;
    public static Object lck = new Object();
    String alertID;
    boolean received_alert = false;
    CentralModuleExecution MCER;
    IntrusionAlert intrusion_received;

    public AlertReceiver(int UDPport, int TCPport, String airsIP, String netMask) {
        System.out.println("*** AlertReceiver--esperando alertas ***");
        try {
            // socket = new DatagramSocket(port, InetAddress.getByName("192.168.1.39"));
            socketUDP = new DatagramSocket(UDPport, InetAddress.getByName(airsIP));
            //socketTCP = new ServerSocket(TCPport, 50, InetAddress.getByName(airsIP));
            socketTCP = new ServerSocket(512, 50, InetAddress.getByName(airsIP));

        } // try
        catch (Exception e) {
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
        ReceiveSocketUDPAlert s = new ReceiveSocketUDPAlert();
        ReceiveSocketTCPAlert t = new ReceiveSocketTCPAlert();
        ReceivePreludeAlert p = new ReceivePreludeAlert();
        ReceiveIDMEFFileAlert i = new ReceiveIDMEFFileAlert();

        new Thread(r).start();
        new Thread(s).start();
        new Thread(p).start();
        new Thread(i).start();
        new Thread(t).start();

    } // Start

    synchronized IntrusionAlert getAlert() {
        if (!received_alert) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
        //System.out.println("Alerta recibida por alguno de los dos medios"+ alertID);

        received_alert = false;
        notify();
        return intrusion_received;
    }

    synchronized void putAlert(IntrusionAlert intrusion) {
        if (received_alert) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
        this.intrusion_received = intrusion;
        received_alert = true;
        //System.out.println("Alerta aÃ±adida: " + alertID);
        notify();
    }

    class Receiver implements Runnable {

        ExecutorService exec;

        public Receiver() {
            exec = Executors.newFixedThreadPool(1);

        }

        public void run() {
            // System.out.println("arrancando Receiver");
            try {

                while (true) {
                    //synchronized (this) {
                    //System.out.println("esperando alertas recibidas");
                    IntrusionAlert alertarecibida = getAlert();
                    try {

                        Runnable nuevaAlerta = new RECOntAIRS(alertarecibida, MCER);
                        exec.execute(nuevaAlerta);

                    } catch (Exception e) {
                        Logger.getLogger(AlertReceiver.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    class ReceiveSocketUDPAlert implements Runnable {

        public void run() {

            int threshold = props.getAlertThreshold();

            IntrusionAlert alertFormatted;
            //MIGUEL Borrar para recibir todas las alertas

            int alertaUnica = 0;

            try {
                byte[] buf = new byte[1024];
                //while (alertaUnica < 1) {
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketUDP.receive(packet);
                    String alert = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    System.out.println("Syslog recibido: " + alert);
                    if (alert.contains("IDMEF-Message")) {
                        if (!alert.equals("")) {
                            IdmefParser parser = new IdmefParser();
                            List<IntrusionAlert> intrusiones = parser.parser(alert);
                            Iterator i = intrusiones.iterator();
                            while (i.hasNext()) {
                                alertFormatted = (IntrusionAlert) i.next();
                                putAlert(alertFormatted);
                            }
                        }
                    } else if (alert.contains("snort")) {
                        //Proceso la alerta y la convierto alert formato IntrusionAlert
                        alertFormatted = new IntrusionAlert();
                        //Filtro las alertas por prioridad
                        int i1 = alert.indexOf("Priority:", 0);
                        int i2 = alert.indexOf("]", i1);
                        String process = alert.substring(i1 + 10, i2);
                        if (Integer.parseInt(process) > threshold) {
                            i1 = alert.indexOf(">", 0);
                            i2 = alert.indexOf(" ", i1 + 1);
                            process = alert.substring(i1 + 1, i2 - 1);
                            DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
                            String currentDate = xdf.format(new Date());
                            System.out.println("SyslogAlertReceiver: nueva alerta " + process + " detectada:" + alert);
                            if (process.equals("snort")) {
                                SnortSyslogAdapter snortAdapter = new SnortSyslogAdapter();
                                alertFormatted = snortAdapter.parseAlert(alert, netMask, currentDate);
                            }
                            alertFormatted.printAlert();
                            putAlert(alertFormatted);
                        }
                    }
                    alertaUnica++;
                } //MIGUEL while
            } // try // try
            catch (Exception e) {
                e.printStackTrace();
                //return false;
            } // catch
        }
    }

    class ReceiveSocketTCPAlert implements Runnable {

        Socket socket_cli;

        public void run() {
            IntrusionAlert alertFormatted;
            String a;

            try {
                socket_cli = socketTCP.accept();
                System.out.println("TCP " + socket_cli);
                BufferedReader b = new BufferedReader(new InputStreamReader(socket_cli.getInputStream()));
                int num = 1;
                while (true) {
                    System.out.println("Alerta recibida Socket TCP numero " + num);
                    a = b.readLine();
                    if (a == null || a.equals("")) {
                        break;
                    } else if (a.contains("IDMEF-Message")) {
                        System.out.println(a);
                        if (!a.equals("")) {
                            IdmefParser parser = new IdmefParser();
                            List<IntrusionAlert> intrusiones = parser.parser(a);
                            Iterator i = intrusiones.iterator();
                            int j = 0;
                            while (i.hasNext()) {
                                alertFormatted = (IntrusionAlert) i.next();
                                j++;
                                putAlert(alertFormatted);
                            }
                            System.out.println("NUmero de alertas IDMEF: " + j);
                            System.out.println("Alertas IDMEF enviadas al AIRS");
                        }
                    }
                    num++;
                } // while
            } // try
            catch (Exception e) {
                e.printStackTrace();

                //return false;
            } // catch
            try {
                socket_cli.close();
                socketTCP.close();
            } catch (Exception e) {
            }
        }
    }

    class ReceiveIDMEFFileAlert implements Runnable {

        public void run() {
            BufferedReader idmefDatos = null;
            IntrusionAlert intrusionalert;
            String xml_file = props.getIDMEFAlertPathValue();
            try {
                idmefDatos = new BufferedReader(new FileReader(xml_file));
                String str;
                while (true) {
                    try {
                        while (idmefDatos.ready()) {
                            str = idmefDatos.readLine();
                            if (!str.equals("")) {
                                //Parseo la alerta alert formato común: IntrusionAlert
                                IdmefParser parser = new IdmefParser();
                                List<IntrusionAlert> intrusiones = parser.parser(str);
                                Iterator i = intrusiones.iterator();
                                while (i.hasNext()) {
                                    intrusionalert = (IntrusionAlert) i.next();
                                    putAlert(intrusionalert);
                                }

                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(AlertReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    class ReceivePreludeAlert implements Runnable {

        public void run() {
            fname = props.getPreludeLogFilePathValue();
            IntrusionAlert alertFormatted = new IntrusionAlert();
            String path = props.getPreludeAlertsPathValue();
            //fname = "/var/log/prelude.log";
            boolean isARealAlert = true;
            // File content change listener 
            try {
                BufferedReader br = new BufferedReader(new FileReader(fname));
                String s;
                StringBuilder buf = new StringBuilder();
                int alertnumber = 0;
                int linesnumber = 0;
                while (true) {
                    //System.out.println("receivedPreludeAlert");
                    s = br.readLine();
                    if (s == null) {
                        synchronized (lck) {
                            lck.wait(500);
                        }
                    } else {
                        linesnumber++;
                        String prelude_alerts_path = props.getPreludeAlertsPathValue();
                        File f = new File(prelude_alerts_path + "/alert" + alertnumber);
                        if (!f.exists()) {
                            f.createNewFile();
                        }
                        FileWriter fw = new FileWriter(f, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(s);
                        bw.newLine();
                        bw.close();
                        // System.out.println( s );
                        if (linesnumber == 2 && !s.startsWith("* Alert")) {
                            isARealAlert = false;
                        }
                        if (s.equals("")) {
                            linesnumber = 0;
                            if (isARealAlert) {
                                alertnumber++;
                                //   System.out.println("Alerta real");
                                //     System.out.println("Alerta prelude recibida");
                                OssecSyslogAdapter ossecAdapter = new OssecSyslogAdapter(path, f.getName());
                                alertFormatted = ossecAdapter.parseAlert(netMask);

                                putAlert(alertFormatted);

                            } else {
                                //     System.out.println("NO es una alerta");
                                fw.close();
                                f.delete();
                                isARealAlert = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                //   return false;
            }
        }
    }
}
