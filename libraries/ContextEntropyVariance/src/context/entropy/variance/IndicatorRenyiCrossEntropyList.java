/**
 * "IndicatorRenyiCrossEntropyList" Java class is free software: you can redistribute
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

package context.entropy.variance;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents a list of IndicatorRenyiCrossEntropy instances.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class IndicatorRenyiCrossEntropyList {
    //Attributes
    private ArrayList<IndicatorRenyiCrossEntropy> _entropyList;
    //Constructors
    public IndicatorRenyiCrossEntropyList(){
        _entropyList = new ArrayList<IndicatorRenyiCrossEntropy>();
    }

    public void addContextAnomalyIndicator(IndicatorRenyiCrossEntropy newIndicatorRenyiCrossEntropy){
        if (newIndicatorRenyiCrossEntropy != null){
            getIndicatorRenyiCrossEntropyList().add((newIndicatorRenyiCrossEntropy));
        }
        else{
            System.out.println("Error: [IndicatorRenyiCrossEntropyList] [addContextAnomalyIndicator] "
                + "Unable to add the indicator partial entropy:" + newIndicatorRenyiCrossEntropy.getIndicatorName());
        }
    }
   
    public void printIndicatorRenyiCrossEntropyList(){
        Iterator i = getIndicatorRenyiCrossEntropyList().iterator();
        while(i.hasNext()){
            IndicatorRenyiCrossEntropy entropy = (IndicatorRenyiCrossEntropy)i.next();
//            System.out.println(entropy.toString());
        }
    }

    public IndicatorRenyiCrossEntropy getExistRenyiCrossEntropyIndicators(String valueName){
        IndicatorRenyiCrossEntropy entropy;
        Iterator i = getIndicatorRenyiCrossEntropyList().iterator();
        while(i.hasNext()){
            entropy = (IndicatorRenyiCrossEntropy)i.next();
            if (entropy.getIndicatorName().equalsIgnoreCase(valueName)){
                return entropy;
            }
        }
        return null;
    }

    public ArrayList<IndicatorRenyiCrossEntropy> getIndicatorRenyiCrossEntropyList(){
        return _entropyList;
    }

    public void setIndicatorRenyiCrossEntropyList(ArrayList<IndicatorRenyiCrossEntropy> value){
        _entropyList = value;
    }
}
