/**
 * "ControllerLearning" Java class is free software: you can redistribute
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

package systemContext.learning;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import systemContext.learning.DAO.DAOException;
import systemContext.learning.DAO.DataManagerFactory;
import systemContext.learning.infoLoader.InfoCollector;
import systemContext.learning.util.PropsUtil;

/**
 * This class represents the controller responsible to get the information about the
 * most relevant indicators of the system context (number of users, number of 
 * SSH failed connections, etc.) of a specific host. 
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class ControllerLearning {
    private PropsUtil props = new PropsUtil();
    private String hostname;

    public ControllerLearning(){
        
    }
    
    public void generateSystemContextProfile() {
        while (true) {
            List<String> nomSistemas = new ArrayList();
            try{
                nomSistemas = DataManagerFactory.getInstance().createDataManager().obtainNetworkSystems();
                for (int i=0; i<nomSistemas.size();i++){
                    hostname = nomSistemas.get(i);
                    System.out.println("Obteniendo muestra del contexto de sistemas del host "+hostname);
                    InfoCollector.generateProfile(hostname);
                }
            }catch (SQLException ex) {
                Logger.getLogger(ControllerLearning.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DAOException ex) {
                Logger.getLogger(ControllerLearning.class.getName()).log(Level.SEVERE, null, ex);
            }
            try{
                int monitoring_slot = Integer.parseInt(props.getSystemContextMonitoringSlotValue());
                Thread.sleep(monitoring_slot*60*1000);
            }
            catch (InterruptedException ex) {
                Logger.getLogger(ControllerLearning.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
    
 
