/**
 * "EvaluationSystemTester" Java class is free software: you can redistribute
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

package evaluationsystemtester;

import evaluation.system.executor.ResponseTotalEfficiency;
import evaluation.system.mode.selector.EvaluationSystemModeSelector;
import evaluation.system.mode.selector.ExecutionModeParams;
import evaluation.system.mode.selector.TrainingModeParams;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class tests the Java classes of the intrusion response evaluation
 * module: EvaluationSystemModeSelector, EvaluationSystemExecutor or ContextEntropyVariance.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class EvaluationSystemTester{
    static ExecutionModeParams para;
    static String[] initAdParam;
    static TrainingModeParams train;

    public static  void main(String args [] ) {
    // Los argumentos dan:
    // el nombre de la máquina receptora y 2 mensajes
     String initResponseID = "001Protection";
     String initType ="NetworkAttack";
     String initResponseType = "Protection";
     String initTargetIP = "192.168.100.130";
     HashMap intAnomaly = new HashMap();
     intAnomaly.put("Network", 6);
            intAnomaly.put("Process", 5);
            intAnomaly.put("CPU", 0);
            intAnomaly.put("Disk", 0);
            intAnomaly.put("Latency", 0);
            intAnomaly.put("User", 0);
            intAnomaly.put("Status", 0);
            intAnomaly.put("Zombie", 0);
            intAnomaly.put("SSHFailed", 0);
            
      ArrayList<String> responseSet = new ArrayList();
      responseSet.add("blockInAttack");


    train = new TrainingModeParams( null,4, "success", responseSet, null, 512, "10.0.1.1", "255.255.255.0");
    train.setTrainingSetType("success");
    para = new ExecutionModeParams(initResponseID, initResponseType, initType, initTargetIP, intAnomaly, initAdParam);
    System.out.println("comienza");
    EvaluationSystemModeSelector sel = new EvaluationSystemModeSelector("training", train);
    if(sel.start()){
       /*ResponseTotalEfficiency resp = sel.getExecutionModeResult();

         String rid = resp.getResponseID();
         double rteValue = resp.getResponseEfficiency();
         int num_exe_value = resp.getNumExecutions();
         System.out.println("responseID: "+rid+ " --> responseEfficiency: "+rteValue + " --> numero de inferencias: " +
                 num_exe_value+".");*/
        double threat = sel.getTrainingModeResult();
    }
    
    

    }
}