/**
 * "ExecutorAgent" Java class is free software: you can redistribute
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

package airs.responses.executor;

/**
 * This class represents a remote host which executes response actions.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */

public class ExecutorAgent {
    //Attributes
    private String _id;
    private String _host;
    private Integer _port;
    private String _key;
    //Constructors
    public ExecutorAgent(){
        _id = "THIS";
        _host = null;
        _port = 898;
        _key = null;
    }
    
    public ExecutorAgent(String initId, String initHost, Integer initPort, String initKey){
        _id = initId;
        _host = initHost;
        _port = initPort;
        _key = initKey;
    }
    
    public ExecutorAgent(ExecutorAgent copyExecutorStation){
        _id = copyExecutorStation._id;
        _host = copyExecutorStation._host;
        _port = copyExecutorStation._port;
        _key = copyExecutorStation._key;
    }
    
    //Procedures and Functions
    @Override public String toString(){
        return  this._id +"/" + this._host+ ", Port:" + 
                this._port + ", Key:" + this._key;
    }
    
    //Properties
    public String getId(){
        return _id;
    }
    
    public String getHost(){
        return _host;
    }
    
    public Integer getPort(){
        return _port;
    }
    
    public String getKey(){
        return _key;
    }
    
    public void setId(String valueId){
        _id=valueId;
    }
    
    public void setHost(String valueHost){
        _host=valueHost;
    }
    
    public void setPort(Integer valuePort){
        _port=valuePort;
    }
    
    public void setKey(String valueKey){
        _key=valueKey;
    }
}