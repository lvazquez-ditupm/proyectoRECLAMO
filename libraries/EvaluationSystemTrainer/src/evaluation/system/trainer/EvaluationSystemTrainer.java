/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation.system.trainer;

import airs.responses.executor.CentralModuleExecution;
import airs.responses.executor.ResponseActionParams;
import context.entropy.variance.ContextAnomalyIndicator;
import context.entropy.variance.ContextAnomalyIndicatorList;
import context.entropy.variance.ContextEntropyVariance;
import evaluation.system.trainer.DAO.BDManagerIF;
import evaluation.system.trainer.DAO.DAOException;
import evaluation.system.trainer.DAO.DataManagerFactory;
import evaluation.system.trainer.utils.PropsUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.context.mode.selector.NetAnomalyDetectionModeParams;
import network.context.mode.selector.NetworkContextModeSelector;
import recparser.Address;
import recparser.DateToXsdDatetimeFormatter;
import recparser.IntrusionAlert;
import recparser.IntrusionTarget;
import recparser.idmef.IdmefParser;
import recparser.ossec.OssecSyslogAdapter;
import recparser.snort.SnortSyslogAdapter;

import system.context.mode.selector.AnomalyDetectionModeParams;
import system.context.mode.selector.SystemContextModeSelector;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;

/**
 *
 * @author root
 */
public class EvaluationSystemTrainer {

    private PropsUtil props = new PropsUtil();
    private String netMask;
    private DatagramSocket socket;
    public static String fname;
    public static Object lck = new Object();
    byte[] alert;
    String alertID;
    boolean received_alert = false;
    CentralModuleExecution MCER;
    IntrusionAlert intrusion_received;
    private ArrayList<String> respList;
    private int m;
    private String mode;
    private String intrusionType;
    int numRespEjecutadasTotal = 0;
    List<Double> entropyVarianceList;
    double threshold;
    boolean trainingRunning;

    public EvaluationSystemTrainer(int port, String airsIP, String netMask, ArrayList<String> responseTraining, int num, String responseListType, String intrusionType) {
        System.out.println("*** AlertReceiver--esperando alertas ***");
        try {
            // socket = new DatagramSocket(port, InetAddress.getByName("192.168.1.39"));
            socket = new DatagramSocket(port, InetAddress.getByName(airsIP));

        } // try
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } // catch

        this.netMask = netMask;
        this.MCER = new CentralModuleExecution();
        this.m = num;
        this.respList = responseTraining;
        this.mode = responseListType;
        this.intrusionType = intrusionType;
        this.trainingRunning = true;
    }

    public double Start() {

        System.out.println("______________________________________________");
        System.out.println("Start- method. AlertReceiver");
        System.out.println("AlertReceiver arrancado.");
        //ExecutorService exec = Executors.newFixedThreadPool(4);
        // while(true) {

        Receiver r = new Receiver();
        ReceiveSyslogAlert s = new ReceiveSyslogAlert();
        ReceivePreludeAlert p = new ReceivePreludeAlert();
        ReceiveIDMEFAlert i = new ReceiveIDMEFAlert();

        new Thread(r).start();
        new Thread(s).start();
        new Thread(p).start();
        new Thread(i).start();
        while (trainingRunning) {
        }
        System.out.println("Ha terminado el entrenamiento para la intrusion " + intrusionType + " en el modo " + mode);
        return threshold;
    } // Start

    synchronized IntrusionAlert getAlert() {
        if (!received_alert) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
        // System.out.println("Alerta recibida por alguno de los dos medios"+ alertID);

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
        private List _hostnameTargetList;
        private String protocolo;
        private String user;
        private Boolean hids = false;
        private ThreatWeights threatWeights;
        private ContextAnomalyIndicatorList intrusionAnomalyList;
        private ContextAnomalyIndicatorList responseAnomalyList;

        public Receiver() {
            exec = Executors.newFixedThreadPool(1);
            this._hostnameTargetList = new ArrayList();
        }

        public void run() {
            // System.out.println("arrancando Receiver");
            try {
                int numRespEjecutadas = 0;
                int accionResponse = 0;
                int n = respList.size();
                while (numRespEjecutadasTotal > m * n) {

                    IntrusionAlert alertarecibida = getAlert();
                    try {
                        if (numRespEjecutadas == m) {
                            accionResponse++;
                            numRespEjecutadas = 0;
                        }

                        alertarecibida.printAlert();
                        if (alertarecibida == null || alertarecibida.isEmpty()) {
                            return;
                        } else if (alertarecibida.getIntType() != intrusionType) {
                            System.out.println("WARNING: EL tipo de intrusion no se corresponde con la del conjunto de entrenamientos");
                        } else {
                            long initialTime = System.currentTimeMillis();
                            List<IntrusionTarget> intrusion_target_list = alertarecibida.getIntrusionTarget();
                            if (intrusion_target_list.size() > 0) {
                                for (int j = 0; j < intrusion_target_list.size(); j++) {
                                    IntrusionTarget target = intrusion_target_list.get(j);
                                    List<Address> intrusion_address_list = target.getAddress();
                                    for (int z = 0; z < intrusion_address_list.size(); z++) {
                                        String ip = target.getAddress().get(z).getAddress();
                                        long startContextAnomalyTime = System.currentTimeMillis();
                                        String hostname = null;
                                        String subnetworkName = null;
                                        BDManagerIF bd = DataManagerFactory.getInstance().createDataManager();
                                        try {
                                            try {
                                                hostname = bd.obtainHostName(ip);
                                                subnetworkName = bd.obtainSubNetworkInfo(ip);
                                            } catch (SQLException ex) {
                                                Logger.getLogger(EvaluationSystemTrainer.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (DAOException ex) {
                                                Logger.getLogger(EvaluationSystemTrainer.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        } catch (Exception e) {
                                        }
                                        if (_hostnameTargetList.contains(hostname)) {
                                            continue;
                                        } else {
                                            System.out.println("__________________________________________________________");
                                            System.out.println(Thread.currentThread() + " ****INIT CONTEXT ANOMALY ****");
                                            HashMap T1_Context = getContextAnomaly(ip, hostname, subnetworkName);
                                            long endContextAnomalyTime = System.currentTimeMillis();
                                            System.out.println(Thread.currentThread() + " **** END CONTEXT ANOMALY *** Total time: " + (endContextAnomalyTime - startContextAnomalyTime) + " (ms)****");
                                            if (T1_Context == null || T1_Context.isEmpty() || T1_Context.size() != 9) {
                                                System.out.println("Error al calcular la anomalía del contexto!!");
                                                continue;
                                            } else {
                                                ResponseActionParams params = new ResponseActionParams(hids, "ALL", intrusionType,
                                                        Integer.toString(alertarecibida.getIntrusionSource().get(0).getPortSrc()), ip, protocolo, alertarecibida.getIntrusionTarget().get(0).getServiceListPort(), user, "hola");
                                                if (MCER.BuildResponseActionRequest(respList.get(accionResponse), params)) {
                                                    long startEfficiencyTime = System.currentTimeMillis();
                                                    System.out.println("___________________________________________________________");
                                                    System.out.println(Thread.currentThread() + " **** INIT RESPONSE EFFICIENCY ****");
                                                    HashMap T2_Context = getContextAnomaly(ip, hostname, subnetworkName);
                                                    if (T2_Context == null || T2_Context.isEmpty() || T2_Context.size() != 9) {
                                                        System.out.println("Error al calcular la anomalía del contexto en T2!!");
                                                        continue;
                                                    } else {
                                                        /*
                                                         threatWeights = getAnomalyWeights(intrusionType);
                                                         intrusionAnomalyList = getCAI(T1_Context, threatWeights);
                                                         responseAnomalyList = getCAI(T2_Context, threatWeights);
                                                         ContextEntropyVariance cevariance = new ContextEntropyVariance();
                                                         double totalCEV = cevariance.getTotalContextEntropyVariance(intrusionAnomalyList, responseAnomalyList);
                                                         */
                                                        long endEfficiencyTime = System.currentTimeMillis();
                                                        System.out.println(Thread.currentThread() + " **** END RESPONSE EFFICENCY *** Total time: " + (endEfficiencyTime - startEfficiencyTime) + " (ms)****");
                                                        numRespEjecutadas++;
                                                        numRespEjecutadasTotal++;
                                                        //entropyVarianceList.add(totalCEV);

                                                        double[] neural_training_input = getResponseAnomaly(T2_Context);
                                                                
                                                        writeTrainingInput(neural_training_input, 1);
                                                    }

                                                } else {
                                                    System.out.println("No ha sido posible ejecutar la respuesta");
                                                    continue;
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.getLogger(EvaluationSystemTrainer.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            } catch (Exception ex) {
            }
        }

        private HashMap getContextAnomaly(String ip, String hostname, String subnetworkName) {
            HashMap contextAnomaly = new HashMap();
            SystemAnomaly system_anomaly = null;
            int network_anomaly = -1;
            AnomalyDetectionModeParams system_anomaly_params = new AnomalyDetectionModeParams(ip, hostname, null);
            SystemContextModeSelector syssel = new SystemContextModeSelector("anomalydetection", system_anomaly_params);
            if (syssel.start()) {
                system_anomaly = syssel.getSystemAnomaly();
                //system_anomaly.printAnomaly();
            } else {
                System.out.println("Ha habido un error al obtener la anomalia del contexto de sistemas");
                return null;
            }
            NetAnomalyDetectionModeParams net_anomaly_params = new NetAnomalyDetectionModeParams(subnetworkName, null);
            NetworkContextModeSelector netsel = new NetworkContextModeSelector("anomalydetection", net_anomaly_params);
            if (netsel.start()) {
                network_anomaly = netsel.getNetworkAnomaly();
                //  System.out.println("El grado de anomalia del contexto de red es: "+network_anomaly);
            } else {
                System.out.println("Ha habido un error obteniendo la anomalia del contexto de red");
                return null;
            }
            contextAnomaly.put("Process", system_anomaly.getProcesosA());
            contextAnomaly.put("CPU", system_anomaly.getCPUA());
            contextAnomaly.put("Disk", system_anomaly.getDiscoduroA());
            contextAnomaly.put("Latency", system_anomaly.getLatenciaA());
            contextAnomaly.put("User", system_anomaly.getUsuariosA());
            contextAnomaly.put("Status", system_anomaly.isEstadoA());
            contextAnomaly.put("SSHFailed", system_anomaly.getSSHFailedA());
            contextAnomaly.put("Zombie", system_anomaly.getZombiesA());
            contextAnomaly.put("Network", network_anomaly);
            return contextAnomaly;
        }

        private ThreatWeights getAnomalyWeights(String intrusionType) {
            /*TODO: EN FUNCIÓN DEL TIPO DE INTRUSIÓN, EL ADMINISTRADOR ESTABLECE VALORES DE PESO DE CADA PARÁMETRO DEL CONTEXTO*/

            ThreatWeights threatw = new ThreatWeights();
            //si no introduzco valores se toman los de por dfecto; Estos valores se deben obtener del contexto
       /*  threatw.setCPUAnomalyWeight(0.25);
             threatw.setDiskAnomalyWeight(0.0);
             threatw.setLatencyAnomalyWeight(0.25);
             threatw.setNetworkAnomalyWeight(0.25);
             threatw.setProcessAnomalyWeight(0.0);
             threatw.setSSHFailedAnomalyWeight(0.0);
             threatw.setStatusAnomalyWeight(0.0);
             threatw.setUsersAnomalyWeight(0.25);
             threatw.setZombieAnomalyWeight(0.0);*/
            return threatw;
        }

        private ContextAnomalyIndicatorList getCAI(HashMap anomaly, ThreatWeights tw) {
            ContextAnomalyIndicatorList anomalyIndiList = new ContextAnomalyIndicatorList();
            HashMap anomalymap = anomaly;
            ThreatWeights threatweights = tw;
            HashMap tweights = threatweights.getThreatWeightHashMap();
            Iterator it = anomalymap.keySet().iterator();

            while (it.hasNext()) {
                String indiName = it.next().toString();
                int indiValue = (Integer) anomalymap.get(indiName);
                double indiWeight = (Double) tweights.get(indiName);
                ContextAnomalyIndicator indicator = new ContextAnomalyIndicator(indiName, indiValue, indiWeight);
                anomalyIndiList.addContextAnomalyIndicator(indicator);
            }
            return anomalyIndiList;
        }

        private double[] getAnomalyVariance(HashMap intrusionMap, HashMap responseAnomalyMap) {
            HashMap intrusionAn = intrusionMap;
            Iterator it = intrusionAn.keySet().iterator();
            double[] nn_input = new double[9];
            try {
                int num = 0;

                while (it.hasNext()) {
                    String indiName = (String) it.next();
                    double anomaly_parameter_intrusion = (Integer) intrusionAn.get(indiName);
                    double anomaly_parameter_response = (Integer) responseAnomalyMap.get(indiName);
                    System.out.println("Anomalia antes--> Parametro: " + indiName + " ; Valor: " + anomaly_parameter_intrusion);
                    System.out.println("anomalia despues--> Parametro: " + indiName + " ; Valor: " + anomaly_parameter_response);

                    nn_input[num] = (anomaly_parameter_response - anomaly_parameter_intrusion);
                    num++;
                }
            } catch (Exception e) {
                Logger.getLogger(EvaluationSystemTrainer.class.getName()).log(Level.SEVERE, null, e);
            }
            return nn_input;
        }

        private double[] getResponseAnomaly(HashMap responseAnomalyMap) {
            HashMap responseAn = responseAnomalyMap;
            Iterator it = responseAn.keySet().iterator();
            double[] nn_input = new double[9];
            try {
                int num = 0;

                while (it.hasNext()) {
                    String indiName = (String) it.next();
                    double anomaly_parameter_response = (Integer) responseAnomalyMap.get(indiName);
                    nn_input[num] = anomaly_parameter_response;
                    num++;
                }
            } catch (Exception e) {
                Logger.getLogger(EvaluationSystemTrainer.class.getName()).log(Level.SEVERE, null, e);
            }
            return nn_input;

        }

        private void writeTrainingInput(double[] inputSet, int result) {
            if (inputSet.length < 9 || result == -1) {
                System.out.println("La muestra de datos para el entrenamiento no tiene los parámetros necesarios");

            } else {
                String s = null;
                for (int i = 0; i < inputSet.length; i++) {
                    s = s + " " + inputSet[i];
                }
                s = s + " " + result;
                System.out.println("Muestra entrenamiento: " + s);
                String neural_network_training_path = props.getNNFileValue();
                try {
                    File f = new File(neural_network_training_path);
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                    FileWriter fw = new FileWriter(f, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(s);
                    bw.newLine();
                    bw.close();
                } catch (Exception ex) {
                    Logger.getLogger(EvaluationSystemTrainer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    class ReceiveSyslogAlert implements Runnable {

        public void run() {
            IntrusionAlert alertFormatted = new IntrusionAlert();
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                while (true) {
                    socket.receive(packet);
                    alert = packet.getData();
                    //    System.out.println("syslogrecibido");
                    String a = new String(alert, 0, alert.length);

                    //Proceso la alerta y la convierto a formato IntrusionAlert
                    int i1 = a.indexOf(">", 0);
                    int i2 = a.indexOf(" ", i1 + 1);
                    String process = a.substring(i1 + 1, i2 - 1);
                    DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
                    String currentDate = xdf.format(new Date());
                    System.out.println("SyslogAlertReceiver: nueva alerta detectada:" + a);
                    if (process.equals("snort")) {
                        SnortSyslogAdapter snortAdapter = new SnortSyslogAdapter();
                        alertFormatted = snortAdapter.parseAlert(a, netMask, currentDate);
                    }

                    putAlert(alertFormatted);
                } // while
            } // try
            catch (Exception e) {
                e.printStackTrace();
                //return false;
            } // catch
        }
    }

    class ReceiveIDMEFAlert implements Runnable {

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
                                //Parseo la alerta a formato común: IntrusionAlert
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
                        Logger.getLogger(EvaluationSystemTrainer.class.getName()).log(Level.SEVERE, null, ex);
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

    public double Threshold() {
        int i;
        double threshold = 1.0;
        for (i = 0; i < numRespEjecutadasTotal; i++) {
            double Ii = entropyVarianceList.get(i);
            if (Ii < threshold) {

            }
            System.out.println("El valor de la varianza de la entorpia es : " + Ii);
        }
        return threshold;
    }

}
