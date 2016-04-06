/**
 * "TrainingModeParams" Java class is free software: you can redistribute
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

package evaluation.system.mode.selector;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents the required parameters to run the intrusion response
 * evaluation system in training mode.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class TrainingModeParams {

    //Attributes
    private String _intrusionType;
    private int _num;
    private String _trainingSetType;
    private ArrayList<String> _trainingSet;
    private String[] _adParam;
    private static final String mode = "training";
    private int _trainingSystemPort;
    private String _trainingSystemIP;
    private String _trainingSystemNetmask;

    //Constructors
    public TrainingModeParams(){
        _intrusionType = null;
        _num = -1;
        _trainingSetType=null;
        _trainingSet = new ArrayList<String>();
        _adParam = null;
        _trainingSystemPort = -1;
        _trainingSystemIP = null;
        _trainingSystemNetmask = null;
    }

    public TrainingModeParams(String initType, int num, String initTrainingType, ArrayList<String> trainingList, String[] adParam, int port, String systemIP, String systemNetMask){
        this._intrusionType = initType;
        this._num= num;
        this._trainingSetType = initTrainingType;
        this._trainingSet = trainingList;
        _adParam = adParam;
        this._trainingSystemPort = port;
        this._trainingSystemIP = systemIP;
        this._trainingSystemNetmask = systemNetMask;
    }
    
    public void PrintTrainingSet(){
        Iterator i = getTrainingSet().iterator();
        while(i.hasNext()){
            String responseName = i.next().toString();
            System.out.println(responseName);
        }
    }

    public String getMode(){
        return mode;
    }

    public String getIntrusionType(){
        return _intrusionType;
    }

    public int getNumberOfExecution(){
        return _num;
    }

    public String getTrainingSetType(){
        return _trainingSetType;
    }

    public ArrayList<String> getTrainingSet(){
        return _trainingSet;
    }
    
    public String[] getAdParam(){
        return _adParam;
    }

    public String getTrainingSystemIP(){
        return _trainingSystemIP;
    }
    
    public String getTrainingSystemNetmask(){
        return _trainingSystemNetmask;
    }
    
    public int getTrainingSystemPort(){
        return _trainingSystemPort;
    }
    
    public void setIntrusionType(String value){
        _intrusionType = value;
    }

    public void setNumberOfExecution(int value){
        _num =value;
    }

    public void setTrainingSetType(String value){
        _trainingSetType = value;
    }

    public void setTrainingSet(ArrayList<String> value){
        _trainingSet = value;
    }

    public void setAdParam(String[] value){
        _adParam = value;
    }
    
    public void setTrainingSystemIP(String value){
        _trainingSystemIP = value;
    }
    
    public void setTrainingSystemNetmask(String value){
        _trainingSystemNetmask = value;
    }
    
    public void setTrainingSystemPort(int value){
        _trainingSystemPort = value;
    }
    
}