/**
 * "SystemContextModeSelector" Java class is free software: you can redistribute
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

package system.context.mode.selector;

import java.util.logging.Logger;
import systemContext.ISystemContext;
import systemContext.SystemContextFactory;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;
import systemContext.learning.ControllerLearning;

/**
 * This class represents a component responsible for selecting the operation mode
 * to run the systems context module: learning or anomalyDetection. Also, this
 * component checks if the provided parameters are suitable.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */

public class SystemContextModeSelector {
    private static final Logger _log = Logger.getLogger(SystemContextModeSelector.class.getName());
    private AnomalyDetectionModeParams paramsAD;
    private LearningModeParams paramsLe;
    private ControllerLearning controllerlearning;
    private String mode;
    private SystemAnomaly an;
    
    /**
     * This method is the constructor for the learning mode. 
     * @param mode The operation mode: learning.
     * @param paramsLe The parameters necessary to execute the system context 
     *                  module using the learning mode.
     */
    public SystemContextModeSelector(String mode, LearningModeParams paramsLe){
        this.mode = mode;
        this.paramsLe = paramsLe;
    }
    
    /**
     * This method is the constructor for the anomaly detection mode.     
     * @param mode The operation mode: anomalydetection.
     * @param paramsAD The parameters necessary to execute the system context 
     *                  module using the anomaly detection mode.
     */
    public SystemContextModeSelector (String mode, AnomalyDetectionModeParams paramsAD){
        this.mode = mode;
        this.paramsAD = paramsAD;
    }
    
    /**
     * This method starts the instances of the SystemContextModeSelector class.
     * First, it checks the provided parameters and then, it invokes to the system
     * context module in the corresponding operation mode. 
     * @return boolean True: the system context module has finished successfully.
     *                  False: some error happened during the system context 
     *                  module invocation or execution.
     */
    public boolean start(){
        long startTime = System.currentTimeMillis();
	System.out.println("**** INIT SystemContextModeSelector at "+mode+" mode. ****");
        if(checkModeParams(mode)){
            //_log.info("Los parametros requeridos para el modo de funcionamiento deseado son adecuados");
            //Invocamos al modulo encargado de ejecutar el modo seleccionado.
            if(mode.equalsIgnoreCase("learning")){
                controllerlearning = new ControllerLearning ();
                controllerlearning.generateSystemContextProfile();
		long endTime = System.currentTimeMillis();
                System.out.println("**** END SystemContextModeSelector at "+mode+" mode *** Total time : "+ (endTime - startTime)+" (ms)****");
                return true;
            }else if(mode.equalsIgnoreCase("anomalydetection")){
                ISystemContext iSystem=SystemContextFactory.getInstance().getInterface();
                String ip = paramsAD.getTargetIP();
                String hostname = paramsAD.getTargetName();
                an=iSystem.obtainSystemContext(ip,hostname);
		long endTime = System.currentTimeMillis();
		System.out.println("**** END SystemContextModeSelector at "+mode+" mode *** Total time : "+ (endTime - startTime)+" (ms)****");
                return true;
            } else return false;
        }else return false;

    }
    
    /**
     * This method checks if the required parameters to execute the system context
     * module in the specified operation mode, are provided in the right way.
     * @param mode. The operation mode: learning or anomalydetection.
     * @return boolean True: the parameters are correct. 
     *                  False: some of the required parameters are wrong, or the
     *                  selected mode is not recognized.
     */
    private boolean checkModeParams(String mode){
        if(mode.equalsIgnoreCase("learning")){
           return true;
        }else if (mode.equalsIgnoreCase("anomalydetection")){
           if ((paramsAD.getTargetIP()!=null)&&(paramsAD.getTargetName()!=null)){
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
     * This method gets the anomaly existing in the system when executing the 
     * system context module in anomaly detection mode.
     * @return SystemAnomaly The anomaly detected in the system.
     */
    public SystemAnomaly getSystemAnomaly(){
        return an;
    }
  
}
