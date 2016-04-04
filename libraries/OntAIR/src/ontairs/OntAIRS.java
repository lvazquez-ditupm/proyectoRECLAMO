/**
 * "OntAIRS" Java class is free software: you can redistribute
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
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.context.mode.selector.NetAnomalyDetectionModeParams;
import network.context.mode.selector.NetworkContextModeSelector;
import ontairs.DAO.BDManagerIF;
import ontairs.DAO.DAOException;
import ontairs.DAO.DataManagerFactory;
import ontairs.utils.PropsUtil;
import parser.IntrusionAlert;
import parser.ossec.OssecSyslogAdapter;
import parser.snort.SnortSyslogAdapter;
import system.context.mode.selector.AnomalyDetectionModeParams;
import system.context.mode.selector.SystemContextModeSelector;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;

/**
 * This class represents an AIRS based on ontologies.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class OntAIRS implements Runnable{
    private String alert;
    private String netMask;
    private String detectionTime;   
    private static AnomalyDetectionModeParams system_anomaly_params;    
    private static NetAnomalyDetectionModeParams net_anomaly_params;
    private SystemAnomaly system_anomaly;
    private int network_anomaly;
    private PropsUtil props = new PropsUtil();
    private CentralModuleExecution executor;
    
    public OntAIRS(String alert, String netMask, String dt, CentralModuleExecution executor){
        this.alert = alert;
        this.netMask = netMask;
        this.detectionTime = dt;
        this.executor = executor;
        
    }
    
    public void run(){
        IntrusionAlert alert_map = AppendSyslogAlertToModel(alert,netMask, detectionTime);
        alert_map.printAlert();
        if(alert_map == null || alert_map.isEmpty()){
            return;
        }
        else if(!alert_map.validate()){
            System.out.println("La alerta recibida no tiene todos los campos necesarios");
        }else{
            alert_map.printAlert();
            long initialTime = System.currentTimeMillis();
            OntAIRSReasoner reasoner = new OntAIRSReasoner(alert_map, executor);
            //System.out.println(Thread.currentThread()+"Comienza la inferencia de parámetros de la ontología");
            reasoner.inicializarModelos();
            long timeBeforeAddIntrusion =System.currentTimeMillis ();
            boolean sameintrusion = reasoner.checkSimilarIntrusion(alert_map);
            System.out.println(Thread.currentThread()+"Tiempo en añadir intrusion: "+(System.currentTimeMillis() - timeBeforeAddIntrusion));
            if(sameintrusion){
              System.out.println(Thread.currentThread()+ "alerta similar TRUE");  
            } // fin de procesamiento. NO es necesario inferir respuesta
                     
            else{
                String contextUDN = "context"+alert_map.getIntID();
                String contextInfDate =alert_map.getIntDetectionTime();
                String ip = alert_map.getIntrusionTarget().getAddressIP();
                String hostname = null;
                String subnetworkName = null;
                
                BDManagerIF bd=DataManagerFactory.getInstance().createDataManager();
                try {           
                    try {
                        hostname = bd.obtainHostName(ip);
                        subnetworkName = bd.obtainSubNetworkInfo(ip);
                    } catch (SQLException ex) {
                            Logger.getLogger(OntAIRS.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (DAOException ex) {
                            Logger.getLogger(OntAIRS.class.getName()).log(Level.SEVERE, null, ex);
                    }            
                }catch (Exception e){}
                
                /* Obtenemos el grado de anomalia del contexto de sistemas
                 * Params: targetIP, targetName
                 */
                system_anomaly_params = new AnomalyDetectionModeParams(ip, hostname, null);
                SystemContextModeSelector syssel = new SystemContextModeSelector("anomalydetection", system_anomaly_params);
                if(syssel.start()){
                    system_anomaly = syssel.getSystemAnomaly();
                    system_anomaly.printAnomaly();
                }else{
                    System.out.println("Ha habido un error al obtener la anomalia del contexto de sistemas");
                }
                
                 /* Obtenemos el grado de anomalia del contexto de red
                 * Params: targetIP, targetName
                 */
                net_anomaly_params = new NetAnomalyDetectionModeParams (subnetworkName,null);
                NetworkContextModeSelector netsel = new NetworkContextModeSelector("anomalydetection",net_anomaly_params);
                if(netsel.start()){
                    network_anomaly = netsel.getNetworkAnomaly();
                    System.out.println("El grado de anomalia del contexto de red es: "+network_anomaly);
                }else{
                    System.out.println("Ha habido un error obteniendo la anomalia del contexto de red");
                }
            
                reasoner.addNetworkContext(contextUDN, network_anomaly, contextInfDate);
                reasoner.addSystemContext(contextUDN, system_anomaly, contextInfDate);
                reasoner.updateIndividuals();    
                reasoner.reasonerPellet();                
                reasoner.inferRecommendedResponses(); 
                reasoner.inferOptimumResponses();
                reasoner.writeFile();
                long timeAfter =System.currentTimeMillis ();
                System.out.println(Thread.currentThread()+ " -> TIEMPO TOTAL: "+ (timeAfter-initialTime));
            } 
        }
        System.out.println(Thread.currentThread()+"OntAIR -----: He terminado de procesar la alerta");
    }
    
    private IntrusionAlert AppendSyslogAlertToModel(String alert, String netMask, String currentDate){
            IntrusionAlert alertFormatted = new IntrusionAlert();
            try{
                StringTokenizer st = new StringTokenizer (alert);
                if(st.countTokens()>1){
                    int i1 = alert.indexOf(">", 0);
                    int i2 = alert.indexOf(" ", i1 + 1);
                    String process = alert.substring(i1 + 1, i2-1);
                    if (process.equals("snort")){
                        SnortSyslogAdapter snortAdapter = new SnortSyslogAdapter();
                        alertFormatted = snortAdapter.parseAlert(alert, netMask, currentDate);
                    }
                }       
                else if(st.countTokens()==1){
                    String path = props.getPreludeAlertsPathValue();
                    OssecSyslogAdapter ossecAdapter = new OssecSyslogAdapter(path, alert);
                    alertFormatted = ossecAdapter.parseAlert(netMask);
                }
            }
            catch (Exception e){
                System.out.println("ERROR: "+e);
            }
            return alertFormatted;
    }
}