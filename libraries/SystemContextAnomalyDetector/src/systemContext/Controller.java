/**
 * "Controller" Java class is free software: you can redistribute
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

package systemContext;

import systemContext.anomalyDetector.infoLoader.Extractor;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import systemContext.anomalyDetector.DAO.DAOException;
import systemContext.anomalyDetector.DAO.DataManagerFactory;
import systemContext.anomalyDetector.systemCorrelator.Correlator;
import systemContext.anomalyDetector.util.PropsUtil;

/**
 * This class represents the controller responsible to get the anomaly existing
 * in the compromised system at current time (the current state of the system).
 * This class implements ISystemContext interface.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class Controller implements ISystemContext{
    private PropsUtil props = new PropsUtil();
    private String hostname;

    public SystemAnomaly obtainSystemContext(String IP, String hostname) {
        long initParser = System.currentTimeMillis();
	System.out.println("**** SystemContextAnomalyDetector-Controller - START ****");
	SystemAnomaly an=null;
        this.hostname = hostname; 
        // Lo dejo comentado para la demostración
        try {
            String pathServ=props.getNagiosServicePerfdataFilePathValue();
            String pathHost=props.getNagiosHostPerfdataFilePathValue();
            //System.out.println("DEBUGGING: "+hostname+" ; pathServ: "+pathServ);
            List<String> nomSistemas = new ArrayList();
            //String hostName=FileProcessor.obtainParameter("/home/vmateos/Documentos/NetBeansProjects/Context/src/systemContext/configFiles/systemAdresses", IP);
            nomSistemas.add(this.hostname);
            List<InfoSystem> s=DataManagerFactory.getInstance().createDataManager().obtainRelevantInfo(this.hostname);
            Correlator c=new Correlator();
            
            List<InfoSystem> concreteSystem=Extractor.leerArchivoEstadoSistemas(nomSistemas, pathServ, pathHost);
            InfoSystem finalSystem = concreteSystem.get(0);
            
            /*PRUEBAS PARA VER QUÉ HACE LA HISTORIA ESTA */
          //  InfoSystem testSystem = new InfoSystem(this.hostname, true, 20, 118, 1900, 2, 0, 10, 0);
            an=c.correlate(s,finalSystem);
          //  an = c.correlate(s, testSystem);

        } catch (SQLException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DAOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }         
        //SystemAnomaly(int estadoA, int usuariosA, int procesosA, int discoduroA, int CPUA, int zombiesA, int latenciaA)
        //an=new SystemAnomaly(1,2,3,4,4,2,8);
	long endController = System.currentTimeMillis();
	System.out.println("**** END SystemContextAnomalyDetection - Controller **** Total time : "+(endController - initParser)+" (ms)**** ");
       return an;


    }
}
