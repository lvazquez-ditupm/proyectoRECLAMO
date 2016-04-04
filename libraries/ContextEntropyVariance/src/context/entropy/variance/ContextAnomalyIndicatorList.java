/**
 * "ContextAnomalyIndicatorList" Java class is free software: you can redistribute
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
 * This class represents a list of ContextAnomalyIndicator instances.
 *   
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class ContextAnomalyIndicatorList {
    //Attributes
    private ArrayList<ContextAnomalyIndicator> _indicatorList;
    //Constructors
    public ContextAnomalyIndicatorList(){
        _indicatorList = new ArrayList<ContextAnomalyIndicator>();
    }
    
    public void addContextAnomalyIndicator(ContextAnomalyIndicator newContextAnomalyIndicator){
        if (newContextAnomalyIndicator != null){
            getContextAnomalyIndicatorList().add((newContextAnomalyIndicator));
        }
        else{
            System.out.println("Error: [ContextAnomalyIndicatorList] [addContextAnomalyIndicator] "
                + "Unable to add the Context Anomaly Indicator:" + newContextAnomalyIndicator.getIndicatorName());
        }
    }
    
    public void printContextAnomalyIndicatorList(){
        Iterator i = getContextAnomalyIndicatorList().iterator();
        while(i.hasNext()){
            ContextAnomalyIndicator indicator = (ContextAnomalyIndicator)i.next();
           // System.out.println(indicator.toString());
        }
    }

    public ContextAnomalyIndicator getExistContextAnomalyIndicators(String valueName){
        ContextAnomalyIndicator contAnomalyIndicator;
        Iterator i = getContextAnomalyIndicatorList().iterator();
        while(i.hasNext()){
            contAnomalyIndicator = (ContextAnomalyIndicator)i.next();
            if (contAnomalyIndicator.getIndicatorName().equalsIgnoreCase(valueName)){
                return contAnomalyIndicator;
            }
        }
        return null;
    }
    
    public ArrayList<ContextAnomalyIndicator> getContextAnomalyIndicatorList(){
        return _indicatorList;
    }

    public void setContextAnomalyIndicatorList(ArrayList<ContextAnomalyIndicator> value){
        _indicatorList = value;
    }

    public void cleanContextAnomalyIndicatorList(){
        _indicatorList.clear();
    }

}
