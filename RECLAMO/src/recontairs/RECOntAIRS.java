/**
 * "RECOntAIRS" Java class is free software: you can redistribute
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
 * along with this program (lgpl.txt).  If not, see
 * <http://www.gnu.org/licenses/>
 */
package recontairs;

import airs.responses.executor.CentralModuleExecution;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.context.mode.selector.NetAnomalyDetectionModeParams;
import network.context.mode.selector.NetworkContextModeSelector;
import recontairs.DAO.BDManagerIF;
import recontairs.DAO.DAOException;
import recontairs.DAO.DataManagerFactory;
import recontairs.utils.PropsUtil;
import recparser.Address;
import recparser.IntrusionAlert;
import recparser.IntrusionTarget;
import recparser.ossec.OssecSyslogAdapter;
import recparser.snort.SnortSyslogAdapter;
import system.context.mode.selector.AnomalyDetectionModeParams;
import system.context.mode.selector.SystemContextModeSelector;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;

/**
 * This class represents an AIRS based on ontologies.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class RECOntAIRS implements Runnable {

    private IntrusionAlert alert;
    private static AnomalyDetectionModeParams system_anomaly_params;
    private static NetAnomalyDetectionModeParams net_anomaly_params;
    private SystemAnomaly system_anomaly;
    private int network_anomaly;
    private final PropsUtil props = new PropsUtil();
    private CentralModuleExecution executor;
    private List _hostnameTargetList;
    private HashMap networkContextAnomaly;
    Object lock = new Object();
    private List<String> responsesSameIntrusion;

    public RECOntAIRS(IntrusionAlert alert, CentralModuleExecution executor) {
        this.alert = alert;
        this.executor = executor;
        this._hostnameTargetList = new ArrayList();
        this.networkContextAnomaly = new HashMap();

    }

    public void run() {
        //IntrusionAlert alert_map = AppendAlertToModel(alert,netMask, detectionTime);
        IntrusionAlert alert_map = alert;
        //alert_map.printAlert();
        if (alert_map == null || alert_map.isEmpty()) {
            return;
        } else if (!alert_map.validate()) {
            System.out.println("WARNING: La alerta recibida no tiene todos los campos necesarios");
        } else {
            alert_map.printAlert();
            long initialTime = System.currentTimeMillis();
            RECOntAIRSReasoner reasoner = new RECOntAIRSReasoner(alert_map, executor);
            //System.out.println(Thread.currentThread()+"Comienza la inferencia de parÃ¡metros de la ontologÃ­a");
            reasoner.inicializarModelos();
            //Actualizalizamos valores de severity e intrusion en caso de no tenerlso
            if (alert_map.getIntSeverity() == 0) {
                int severity = reasoner.getIntrusionDefaultSeverity(alert_map);
                if (severity == 0) {
                    System.out.println(Thread.currentThread() + "OntAIRS no puede determinar la severidad de la intrusion por lo que no es posible inferir la respuesta óptima");
                    return;
                } else {
                    alert_map.setIntSeverity(severity);
                }
            }
            int intrusionImpact = reasoner.getIntrusionImpact(alert_map);
            alert_map.setIntImpact(intrusionImpact);//Calcular mejor

            synchronized (lock) {
                //long timeBeforeAddIntrusion =System.currentTimeMillis ();
                responsesSameIntrusion = reasoner.checkSimilarIntrusion(alert_map);

            }

            if (responsesSameIntrusion != null) {

                boolean discard = true;

                long startInferOptimumTime = System.currentTimeMillis();
                System.out.println("__________________________________________________________________________");
                System.out.println(Thread.currentThread() + " **** INIT EXECUTE LAST RESPONSES ****");

                for (int i = 0; i < responsesSameIntrusion.size(); i++) {
                    if (!"kill".equals(responsesSameIntrusion.get(i))) {
                        synchronized (lock) {
                            reasoner.directExecution(responsesSameIntrusion.get(i));
                        }
                        discard = false;
                    } else {
                        System.out.println("___________________________________________________________________");
                        System.out.println(Thread.currentThread() + " -> LA ALERTA " + alert_map.getIntID() + " SE HA DESCARTADO POR FORMAR PARTE DEL MISMO ATAQUE");
                        System.out.println("___________________________________________________________________");

                        discard = true;
                    }
                }

                long endInferOptimumTime = System.currentTimeMillis();
                System.out.println(Thread.currentThread() + " **** END EXECUTE LAST RESPONSES *** Total time: " + (endInferOptimumTime - startInferOptimumTime) + " (ms)****");

                if (!discard) {

                    long startEfficiencyTime = System.currentTimeMillis();
                    System.out.println("___________________________________________________________");
                    System.out.println(Thread.currentThread() + " **** INIT RESPONSE EFFICIENCY ****");

                    reasoner.getResponseEfficiency(false);

                    long endEfficiencyTime = System.currentTimeMillis();
                    System.out.println(Thread.currentThread() + " **** END RESPONSE EFFICENCY *** Total time: " + (endEfficiencyTime - startEfficiencyTime) + " (ms)****");
                    synchronized (lock) {
                        reasoner.updateIndividuals();
                        reasoner.writeFile();
                    }
                }

                long timeAfter = System.currentTimeMillis();
                System.out.println("___________________________________________________________________");
                System.out.println(Thread.currentThread() + " -> TIEMPO TOTAL: " + (timeAfter - initialTime));

                return;

            }

            long startContextAnomalyTime = System.currentTimeMillis();
            System.out.println("__________________________________________________________");
            System.out.println(Thread.currentThread() + " ****INIT CONTEXT ANOMALY ****");
            String contextInfDate = alert_map.getIntDetectionTime();
            List<IntrusionTarget> intrusion_target_list = alert_map.getIntrusionTarget();

            // String ip = alert_map.getIntrusionTarget().getAddressIP();
            if (intrusion_target_list.size() > 0) {
                for (int j = 0; j < intrusion_target_list.size(); j++) {
                    IntrusionTarget target = intrusion_target_list.get(j);
                    List<Address> intrusion_address_list = target.getAddress();
                    for (int z = 0; z < intrusion_address_list.size(); z++) {
                        String ip = target.getAddress().get(z).getAddress();
                        String hostname = null;
                        String subnetworkName = null;
                        BDManagerIF bd = DataManagerFactory.getInstance().createDataManager();
                        try {
                            try {
                                hostname = bd.obtainHostName(ip);
                                subnetworkName = bd.obtainSubNetworkInfo(ip);
                                System.out.println("TARGET--> hostname: " + hostname + " ;subnetwork: " + subnetworkName);
                            } catch (SQLException ex) {
                                Logger.getLogger(RECOntAIRS.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (DAOException ex) {
                                Logger.getLogger(RECOntAIRS.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } catch (Exception e) {
                            Logger.getLogger(RECOntAIRS.class.getName()).log(Level.SEVERE, null, e);
                        }
                        if (hostname == null && subnetworkName == null) {
                            continue;
                        } else if (_hostnameTargetList.contains(hostname)) {
                            continue;
                        } else {
                            String contextUDN = "context" + alert_map.getIntID() + hostname;
                            _hostnameTargetList.add(hostname);
                            /* Obtenemos el grado de anomalia del contexto de sistemas para este hostname
                                 * Params: targetIP, targetName
                             */
                            system_anomaly_params = new AnomalyDetectionModeParams(ip, hostname, null);
                            SystemContextModeSelector syssel = new SystemContextModeSelector("anomalydetection", system_anomaly_params);
                            if (syssel.start()) {
                                system_anomaly = syssel.getSystemAnomaly();
                                system_anomaly.printAnomaly();
                            } else {
                                System.out.println("Ha habido un error al obtener la anomalia del contexto de sistemas");
                                continue;
                            }
                            /* Obtenemos el grado de anomalia del contexto de red
                                * Params: targetIP, targetName
                             */

                            if (networkContextAnomaly.containsKey(subnetworkName)) {
                                network_anomaly = (Integer) networkContextAnomaly.get(subnetworkName);
                            } else {
                                net_anomaly_params = new NetAnomalyDetectionModeParams(subnetworkName, null);
                                NetworkContextModeSelector netsel = new NetworkContextModeSelector("anomalydetection", net_anomaly_params);
                                if (netsel.start()) {
                                    network_anomaly = netsel.getNetworkAnomaly();
                                    System.out.println("El grado de anomalia del contexto de red es: " + network_anomaly);
                                } else {
                                    System.out.println("Ha habido un error obteniendo la anomalia del contexto de red");
                                    continue;
                                }
                                networkContextAnomaly.put(subnetworkName, network_anomaly);
                            }
                            /* Añado la anomalía de contexto asociada a este Target */
                            reasoner.addContextAnomaly(contextUDN, hostname, ip, subnetworkName, contextInfDate, network_anomaly, system_anomaly);
                        }
                    }
                }

                long endContextAnomalyTime = System.currentTimeMillis();
                System.out.println(Thread.currentThread() + " **** END CONTEXT ANOMALY *** Total time: " + (endContextAnomalyTime - startContextAnomalyTime) + " (ms)****");

                if (_hostnameTargetList.size() > 0) {
                    long startPelletTime = System.currentTimeMillis();
                    System.out.println("__________________________________________________________");
                    System.out.println(Thread.currentThread() + " **** INIT REASONER PELLET ****");
                    reasoner.reasonerPellet();
                    long endPelletTime = System.currentTimeMillis();
                    System.out.println(Thread.currentThread() + " **** END REASONER PELLET *** Total time: " + (endPelletTime - startPelletTime) + " (ms)****");

                    long startInferRecommendedTime = System.currentTimeMillis();
                    System.out.println("__________________________________________________________________");
                    System.out.println(Thread.currentThread() + " **** INIT INFER RECOMMENDED RESPONSES ****");
                    reasoner.inferRecommendedResponses();

                    long endInferRecommendedTime = System.currentTimeMillis();
                    System.out.println(Thread.currentThread() + " **** END INFER RECOMMENDED RESPONSES *** Total time: " + (endInferRecommendedTime - startInferRecommendedTime) + " (ms)****");

                    long startInferOptimumTime = System.currentTimeMillis();
                    System.out.println("__________________________________________________________________________");
                    System.out.println(Thread.currentThread() + " **** INIT INFER and EXECUTE OPTIMUM RESPONSES ****");
                    reasoner.inferOptimumResponses();
                    long endInferOptimumTime = System.currentTimeMillis();
                    System.out.println(Thread.currentThread() + " **** END INFER AND EXECUTE OPTIMUM RESPONSES *** Total time: " + (endInferOptimumTime - startInferOptimumTime) + " (ms)****");

                    long startEfficiencyTime = System.currentTimeMillis();
                    System.out.println("___________________________________________________________");
                    System.out.println(Thread.currentThread() + " **** INIT RESPONSE EFFICIENCY ****");

                    reasoner.getResponseEfficiency(true);

                    long endEfficiencyTime = System.currentTimeMillis();
                    System.out.println(Thread.currentThread() + " **** END RESPONSE EFFICENCY *** Total time: " + (endEfficiencyTime - startEfficiencyTime) + " (ms)****");
                    synchronized (lock) {
                        reasoner.updateIndividuals();
                        reasoner.writeFile();
                    }
                    long timeAfter = System.currentTimeMillis();
                    System.out.println("___________________________________________________________________");
                    System.out.println(Thread.currentThread() + " -> TIEMPO TOTAL: " + (timeAfter - initialTime));
                } else {
                    System.out.println("ningun objetivo esta dentro de la red protegida");
                }
            }
        }
    }

    private IntrusionAlert AppendAlertToModel(String alert, String netMask, String currentDate) {
        IntrusionAlert alertFormatted = new IntrusionAlert();
        try {
            StringTokenizer st = new StringTokenizer(alert);
            if (st.countTokens() > 1) {
                /* if(alert.startsWith("<IDMEF-Message")){
			IdmefParser parser = new IdmefParser();
       			List<IntrusionAlert> intrusiones = parser.parser(alert);
			Iterator i = intrusiones.iterator();
			while (i.hasNext()){
                            
			}
		    }*/
                if (false) {
                } else {
                    int i1 = alert.indexOf(">", 0);
                    int i2 = alert.indexOf(" ", i1 + 1);
                    String process = alert.substring(i1 + 1, i2 - 1);
                    if (process.equals("snort")) {
                        SnortSyslogAdapter snortAdapter = new SnortSyslogAdapter();
                        alertFormatted = snortAdapter.parseAlert(alert, netMask, currentDate);
                    }
                }
            } else if (st.countTokens() == 1) {
                String path = props.getPreludeAlertsPathValue();
                OssecSyslogAdapter ossecAdapter = new OssecSyslogAdapter(path, alert);
                alertFormatted = ossecAdapter.parseAlert(netMask);
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
        }
        return alertFormatted;
    }
    /*      private int GetRelevance(String assetIP){
        int aloi_total=0;
        recparser.DAO.BDManagerIF bd=recparser.DAO.DataManagerFactory.getInstance().createDataManager();
        try {
            Object result = bd.obtainAssetLevelOfImportance(assetIP).get("assetLevelOfImportance");
            if(result instanceof String){
                String resultSt = (String) result;
                if(resultSt.equalsIgnoreCase("high")){
                    aloi_total = ASSET_LEVEL_OF_IMPORTANCE_HIGH_VALUE;
                }
                else if(resultSt.equalsIgnoreCase("medium")){
                    aloi_total = ASSET_LEVEL_OF_IMPORTANCE_MEDIUM_VALUE;
                }
                else if(resultSt.equalsIgnoreCase("low")){
                    aloi_total = ASSET_LEVEL_OF_IMPORTANCE_LOW_VALUE;
                }
            }else if(result instanceof Integer){
                aloi_total = (Integer)  result;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SnortSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (recparser.DAO.DAOException ex) {
            Logger.getLogger(SnortSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }            
        
        return aloi_total;
    } // GetRelevance*/
}
