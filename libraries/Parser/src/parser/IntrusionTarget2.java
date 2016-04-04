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

package parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an intrusion target. This intrusion target is the
 * objective of the intrusion included in one intrusion alert.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class IntrusionTarget2 {
    
    private String _targetID; //<Target ident>
    private String _componentName; //<Node><name>
    private String _componentLocation; //<Node><Location>
    //private String _addressIP; //<Node><Address><address>
    //private String _addressType; //<Node><Address category>
    //private String _addressNetmask; //<Node><Address><netmask>
    //private String _addressVlanName; //<Node><Address vlan-name>
    //private int _addressVlanNumber;//<Node><Address vlan-num>
    private String _serviceID; //<Service ident>
    private String _serviceName; //<Service><name>
    private int _servicePort; //<Service><port>
    private String _serviceListPort; //<Service><portlist>
    private String _targetType; //??
    private List <Address> _nodes;
    
    public IntrusionTarget2(){
        _targetID = null;;
       /* _addressIP = null;
        _addressType = null;
        _addressNetmask = null;
        _addressVlanName = null;
        _addressVlanNumber = -1;*/
        _componentName = null;
        _componentLocation = null;
        _serviceID = null;
        _nodes = new ArrayList<Address>();
    }
        
    public String getTargetID(){
        return _targetID;
    }
    
    public String getTargetType(){
        return _targetType;
    }
    
    public List <Address> getAddress(){
        return _nodes;
    }
    
    /*public String getAddressIP(){
        return _addressIP;
    }

    public String getAddressType(){
        return _addressType;
    }
    
    public String getAddressNetmask(){
        return _addressNetmask;
    }
    
    public String getAddressVlanName(){
        return _addressVlanName;
    }
    
    public int getAddressVlanNumber(){
        return _addressVlanNumber;
    }*/
    
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
        
    public void setTargetID(String value){
        _targetID = value;
    }
    
    public void setTargetType(String value){
        _targetType =value;
    }
    
    public void setNode (Address value){
        _nodes.add(value);
    }
    
  /*  public void setAddressIP(String value){
        _addressIP =value;
    }
    
    public void setAddressType(String value){
        _addressType =value;
    }
    
    public void setAddressNetmask(String value){
        _addressNetmask =value;
    }
    
    public void setAddressVlanName(String value){
        _addressVlanName =value;
    }
    
    public void setAddressVlanNumber(int value){
        _addressVlanNumber =value;
    }*/
    
    public void setComponentName(String value){
        _componentName = value;
    }
    
    public void setComponentLocation(String value){
        _componentLocation = value;
    }
    
    public void setServiceId(String value){
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