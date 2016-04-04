/**
 * "IndicatorRenyiCrossEntropy" Java class is free software: you can redistribute
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

/**
 * This class represents the partial value of the Renyi cross entropy for a specific 
 * indicator of the context (network or system).
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class IndicatorRenyiCrossEntropy {

    private String _indicatorName;
    private double _entropyValue;
    private double _indicatorWeight;


    // Constructores de la clase
    public IndicatorRenyiCrossEntropy(){
        _indicatorName = null;
        _entropyValue = -1;
        _indicatorWeight = -1;
    }
    
    public IndicatorRenyiCrossEntropy(String initIndicatorName, double initEntropyValue){
        this._indicatorName = initIndicatorName;
        this._entropyValue = initEntropyValue;
        this._indicatorWeight = 1/9;
    }
    
    public IndicatorRenyiCrossEntropy(String initIndicatorName, double initEntropyValue, double initIndicatorWeight){
        this._indicatorName = initIndicatorName;
        this._entropyValue = initEntropyValue;
        this._indicatorWeight = initIndicatorWeight;
    }

    //Métodos
    public String getIndicatorName(){
        return _indicatorName;
    }
   
    public double getEntropyValue(){
        return _entropyValue;
    }
    
    public double getIndicatorWeight(){
        return _indicatorWeight;
    }
    
    public void setIndicatorName(String value){
        _indicatorName = value;
    }
    
    public void setEntropyValue(double value){
        _entropyValue = value;
    }
    
    public void setIndicatorWeight(double value){
        _indicatorWeight = value;
    }

}