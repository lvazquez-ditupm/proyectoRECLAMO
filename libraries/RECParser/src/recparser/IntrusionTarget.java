/**
 * "IntrusionTarget" Java class is free software: you can redistribute
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

package recparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents an intrusion target. This intrusion target is the
 * objective of the intrusion included in one intrusion alert.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class IntrusionTarget {
    
    private String _targetID; //required <Target ident>
    private String _componentName; //optional <Node><name>
    private String _componentLocation; //optional <Node><Location>
    private String _serviceID; //optional <Service ident>
    private String _serviceName; //optional <Service><name>
    private int _servicePort; //optional <Service><port>
    private String _serviceListPort; //optional <Service><portlist>
    private String _targetType; //<Node><category> optional
    private List <Address> _nodes; //required, at least one
    
    public IntrusionTarget(){
        _targetID = null;
        _componentName = null;
        _componentLocation = null;
        _serviceID = null;
        _serviceName = null;
        _nodes = new ArrayList<Address>();
        _servicePort=-1;
        _serviceListPort=null;
        _targetType=null;
    }
        
    public String getTargetID(){
        return _targetID;
    }

    public List <Address> getAddress(){
        return _nodes;
    }
    
    public String getTargetType(){
        return _targetType;
    }
    
    public String getComponentName(){
        return _componentName;
    }
    
    public String getComponentLocation(){
        return _componentLocation;
    }
    
    public String getServiceID(){
        return _serviceID;
    }
    
    public String getServiceName(){
        return _serviceName;
    }
    
    public int getServicePort(){
        return _servicePort;
    }
    
    public String getServiceListPort(){
        return _serviceListPort;
    }

    public List<Integer> getServiceListPortInt(){
        List<Integer> service_list = new ArrayList<Integer>();
        if (_serviceListPort !=null){
            String str = _serviceListPort;
            List<String> tempList = Arrays.asList(str.split(","));
            for (int i =0; i<tempList.size();i++){
                String listElement = tempList.get(i);
                if (listElement.contains("-")){
                    List<String> tempList2 = Arrays.asList(listElement.split("-"));
                    int firstPort = Integer.parseInt(tempList2.get(0));
                    int lastPort = Integer.parseInt(tempList2.get(1));
                    for(int j =firstPort;j<=lastPort;j++){
                        service_list.add(j);
                    }
                }
                else{
                    service_list.add(Integer.parseInt(listElement));
                }
            }
        }
        return service_list;
    }
        
    public void setNode (Address value){
        _nodes.add(value);
    }

    public void setTargetID(String value){
        _targetID = value;
    }
    
    public void setTargetType(String value){
        _targetType =value;
    }
    
    public void setComponentName(String value){
        _componentName = value;
    }
    
    public void setComponentLocation(String value){
        _componentLocation = value;
    }
    
    public void setServiceID(String value){
        _serviceID = value;
    }     
    
    public void setServiceName(String value){
        _serviceName = value;
    }
    
    public void setServicePort(int value){
        _servicePort=value;
    }
    
    public void setServiceListPort(String value){
        _serviceListPort =value;
    }
}
