/**
 * "NetControllerLearning" Java class is free software: you can redistribute
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

package networkContext.learning;

import networkContext.learning.infoLoader.InfoNetworkCollector;
import networkContext.learning.sancp.SancpManager;
import networkContext.learning.util.LinuxUtils;
import networkContext.learning.util.PropsUtil;


/**
 * This class represents the controller responsible to get the information about the
 * most relevant indicators of the network traffic of a specific subnetwork. 
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class NetControllerLearning {
    private PropsUtil props = new PropsUtil();
    private String interfaceName;
    private int learningTime;
     
    
    public NetControllerLearning(String interfaceID, int learTime) {
        this.interfaceName=interfaceID;
        this.learningTime = learTime;
    }      
       
    public void generateNetworkContextProfile() {
        SancpManager sm= new SancpManager(interfaceName);
        int processnumber=sm.startSancp();
        int learningTimeMsec = learningTime*60*1000;
        long endLearningTime = System.currentTimeMillis() + learningTimeMsec;
        while (System.currentTimeMillis() < endLearningTime){
            if (LinuxUtils.isSpecificProcessRunning(processnumber, "sancp")){
             //   System.out.println ("el proceso sigue ejecutandose");
            }else {
                System.out.println ("El proceso ha finalizado antes del tiempo ");
                if(sm.stopSancp(processnumber)){
                    //System.out.println("Recuperando datos asociados a esta iteración");
                    try{
                        Thread.sleep(5*1000);
                    }catch(InterruptedException e){     }
                    InfoNetworkCollector.generateProfile(interfaceName);
                    return;
                }
                else{
                    System.out.println("Error en la generación del perfil del tráfico de red asociado a la interfaz: "+interfaceName);
                }
                return;
            }
        }
        System.out.println("El tiempo de aprendizaje ha finalizado para esta iteración");
        if(sm.stopSancp(processnumber)){
            //System.out.println("Recuperando datos asociados a esta iteración");
            try{
                Thread.sleep(5*1000);
            }catch(InterruptedException e){ }
            InfoNetworkCollector.generateProfile(interfaceName);
        } else{
            System.out.println("Error en la generación del perfil del tráfico de red asociado a la interfaz: "+interfaceName);
        }            
    }
}