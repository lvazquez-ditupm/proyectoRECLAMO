/**
 * "ExecutionModeParams" Java class is free software: you can redistribute
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents the required parameters to run the intrusion response
 * evaluation system in execution mode.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 * @param _responseID:Identificador de la respuesta que se quiere evaluar.
 * @param _intrusionType:Tipo de intrusion en funcion de la clasificacion
 *                       realizada en la ontologia.
 * @param _anomalyVector:Vector de anomalía que contiene el valor de la anomalía
 *                       de los nuevos indicadores del contexto obtenidos en el
 *                       momento de detección de la intrusión.
 * @param _adParam:ParÃ¡metro adicional.
 */
public class ExecutionModeParams {
    private String _responseID;
    private String _responseType;
    private String _intrusionType;
    private String _targetIP;
    private HashMap _anomalyMap;
    private String[] _adParam;
    private static final String mode = "execution";

    public ExecutionModeParams(){
        _responseID = null;
        _responseType = null;
        _intrusionType = null;
        _targetIP = null;
        _anomalyMap = new HashMap();
        _adParam = null;
    }

    public ExecutionModeParams( String initResponseID, String initResponseType,String initType, String initTarget, HashMap initAnomalyMap, String[] initAdParam){
        _responseID = initResponseID;
        _responseType = initResponseType;
        _intrusionType = initType;
        _targetIP = initTarget;
        _anomalyMap = initAnomalyMap;
        _adParam = initAdParam;
    }

    public void PrintAnomalyMap(){
        HashMap map = getAnomalyMap();
        Iterator anomalyIterator = map.keySet().iterator();
        while (anomalyIterator.hasNext()){
            String anomalyName = anomalyIterator.next().toString();
            int anomalyValue = (Integer)map.get(anomalyName);
            System.out.println(anomalyName+" : "+anomalyValue);
        }
    }

    public String getMode(){
        return mode;
    }
    
    public String getResponseID(){
        return _responseID;
    }
    
    public String getResponseType(){
        return _responseType;
    }
    
    public String getIntrusionType(){
        return _intrusionType;
    }
    
    public String getTargetIP(){
        return _targetIP;
    }
    
    public HashMap getAnomalyMap(){
        return _anomalyMap;
    }
    
    public String[] getAdParam(){
        return _adParam;
    }

    public void setResponseID(String value){
        _responseID = value;
    }
    
    public void setResponseType(String value){
        _responseType = value;
    }
    
    public void setIntrusionType(String value){
        _intrusionType = value;
    }
    
    public void setTargetIP(String value){
        _targetIP =value;
    }
    
    public void setAnomalyMap(HashMap map){
        _anomalyMap = map;
    }  
    
    public void setAdParam(String[] value){
        _adParam = value;
    }
}