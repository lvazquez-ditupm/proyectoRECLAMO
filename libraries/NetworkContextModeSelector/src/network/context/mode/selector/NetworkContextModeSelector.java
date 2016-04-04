/**
 * "NetworkContextModeSelector" Java class is free software: you can redistribute
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

package network.context.mode.selector;

import java.util.logging.Logger;
import networkContext.anomalyDetector.INetworkContext;
import networkContext.anomalyDetector.NetworkContextFactory;
import networkContext.learning.NetControllerLearning;

/**
 * This class represents a component responsible for selecting the operation mode
 * to run the network context module: learning or anomalyDetection. Also, this
 * component checks if the required parameters are provided in the right way.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */

public class NetworkContextModeSelector {
    private static final Logger _log = Logger.getLogger(NetworkContextModeSelector.class.getName());
    private NetAnomalyDetectionModeParams paramsAD;
    private NetLearningModeParams paramsLe;
    private NetControllerLearning controllerlearning;
    private String mode;
    int anomaly;
    
    
    /**
     * This method is the constructor for the learning mode. 
     * @param mode The operation mode: learning.
     * @param paramsLe The parameters necessary to execute the network context 
     *                  module using the learning mode.
     */
    public NetworkContextModeSelector(String mode, NetLearningModeParams paramsLe){
        this.mode = mode;
        this.paramsLe = paramsLe;
    }
    
    /**
     * This method is the constructor for the anomaly detection mode.     
     * @param mode The operation mode: anomalyDetection.
     * @param paramsAD The parameters necessary to execute the network context 
     *                  module using the anomaly detection mode.
     */
    public NetworkContextModeSelector (String mode, NetAnomalyDetectionModeParams paramsAD){
        this.mode = mode;
        this.paramsAD = paramsAD;
    }
    
    /**
     * This method starts the instances of the NetworkContextModeSelector class.
     * First, it checks the provided parameters and then, it invokes to the network
     * context module in the corresponding operation mode. 
     * @return boolean True: the network context module has finished successfully.
     *                 False: some error happened during the invocation or 
     *                 execution of the network context module.
     */
    public boolean start(){
	long startTime = System.currentTimeMillis();
	System.out.println("**** INIT NetworkContextModeSelector at "+mode+ " mode. ****");        
        if(checkModeParams(mode)){
           // _log.info("Los parametros requeridos para el modo de funcionamiento deseado son adecuados");
            if(mode.equalsIgnoreCase("learning")){
                System.out.println("Tomando datos en interfaz "+paramsLe.getInterfaceName()+ " durante un tiempo de "+paramsLe.getLearningTime());
                String iface = paramsLe.getInterfaceName();
                int learningSlot = paramsLe.getLearningTime();
                if(learningSlot>=600){
                    System.out.println("WARNING: El tiempo de entrenamiento no debe ser superior a 10 horas. Por favor, introduzca otra valor");
                    return false;
                }
                else{
                    controllerlearning = new NetControllerLearning (iface, learningSlot);
                    controllerlearning.generateNetworkContextProfile();
		    long endTime = System.currentTimeMillis();
	            System.out.println("**** END NetworkContextModeSelector at "+mode+" mode *** Total time : "+ (endTime - startTime)+ " (ms)****");
                    return true;
                }
                
            }else if(mode.equalsIgnoreCase("anomalydetection")){        
                String subnetworkName = paramsAD.getInterfaceName();
                INetworkContext iNetwork=NetworkContextFactory.getInstance().getInterface();
                anomaly = iNetwork.obtainNetContext(subnetworkName);
		long endTime = System.currentTimeMillis();
                System.out.println("**** END NetworkContextModeSelector at "+mode+" mode *** Total time : "+ (endTime - startTime)+ " (ms)****");
                return true;
            } else return false;
        }else return false;

    }
    
    /**
     * This method checks if the required parameters to execute the network context
     * module in the specified operation mode, are provided in the right way.
     * @param mode. The operation mode: learning or anomalydetection.
     * @return boolean True: the parameters are correct. 
     *                 False: some of the required parameters are wrong, or the
     *                 selected mode is not recognized.
     */
    private boolean checkModeParams(String mode){
        if(mode.equalsIgnoreCase("learning")){
            if ((paramsLe.getInterfaceName()!=null)&&(paramsLe.getLearningTime()>0)){
                return true;
            }else{
                _log.warning ("No se ha especificado alguno de los parametros requeridos");
                return false;
            }
           
        }else if (mode.equalsIgnoreCase("anomalydetection")){
            if (paramsAD.getInterfaceName()!=null){
                return true;
            }else{
                _log.warning ("No se ha especificado alguno de los parametros requeridos");
                return false;
            }
        }
        else {
            _log.warning("El modo seleccionado no es correcto");
            return false;
        }
    }
    
    /**
     * This method gets the anomaly existing in the specified subnetwork when 
     * executing the network context module in anomaly detection mode.
     * @return int The anomaly detected in the traffic network.
     */
    public int getNetworkAnomaly(){
        return anomaly;
    }
    
}
