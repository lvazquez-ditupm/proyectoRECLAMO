/**
 * "EvaluationSystemTrainer" Java class is free software: you can redistribute
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

package evaluation.system.trainer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents the component to train the intrusion response
 * evaluation system.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class EvaluationSystemTrainerInit {
    EvaluationSystemTrainer alertReceiver;
    private ArrayList<String>  responseList =null;
    private String responseListType;
    private int num;
    private String ip;
    private int port;
    private String netMask;
    private String intrusionType;

    public EvaluationSystemTrainerInit (ArrayList<String>  responseList, int num, String responseListType, String intrusionType, int port, String airsIP, String netMask, String [] add){
        this.responseList = responseList;
        this.num = num;
        this.ip = airsIP;
        this.port = port;
        this.netMask = netMask;
        this.responseListType = responseListType;
        this.intrusionType = intrusionType;
    }
    
    public double  init( ){       
        alertReceiver = new EvaluationSystemTrainer(port, ip, netMask, responseList, num, responseListType, intrusionType);
        double valor = alertReceiver.Start();
        return valor;
    }    
}