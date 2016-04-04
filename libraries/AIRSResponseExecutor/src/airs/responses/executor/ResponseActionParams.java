/**
 * "ResponseActionParams" Java class is free software: you can redistribute
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
 * This class represents the parameters which the Reasoner of the AIRS sends to
 * the Responses Executor module.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 *
 * @param _hids:Indica si la alerta proviene de un HIDS (true) o un NIDS (false)
 * @param _sid:Identificador de alerta de intrusión.
 * @param _intrusionType:Tipo de intrusión clasificado de acuerdo a una taxonomía.
 * @param _mainIP:Dirección IP del host atacante.
 * @param _peerIP:Dirección IP de host atacado.
 * @param _protocol:Protocolo empleado en el ataque. 
 * @param _portConn:Puerto del host atacado.
 * @param _user:Usuario con el que se intenta perpetrar un ataque.
 * @param _adParam:Parámetro adicional.
 */
public class ResponseActionParams {
    
    private Boolean _hids; 
    private String _sid; 
    private String _intrusionType; 
    private String _mainIP; 
    private String _peerIP;
    private String _protocol;
    private String _portConn;
    private String _user;
    private String _adParam;
        
    public ResponseActionParams(){
        _hids = false;
        _sid = "ALL";
        _intrusionType = null;
        _mainIP = null;
        _peerIP = null;
        _protocol = null;
        _portConn = null;
        _user = null;
        _adParam = null;
    }
    
    public ResponseActionParams( Boolean initHids,String initSid, String initType, String initMainIP, String initPeerIP, 
            String initProtocol, String initPortConn, String initUser, String initAdParam){
        _hids = initHids;
        _sid = initSid;
        _intrusionType = initType;
        _mainIP = initMainIP;
        _peerIP = initPeerIP;
        _protocol = initProtocol;
        _portConn = initPortConn;
        _user = initUser;   
        _adParam = initAdParam;
    }
    
    public String getSid(){
        return _sid;
    }
    
    public Boolean getHids(){
        return _hids;
    }
    
    public String getIntrusionType(){
        return _intrusionType;
    }
    
    public String getMainIP(){
        return _mainIP;
    }
    
    public String getPeerIP(){
        return _peerIP;
    }
    
    public String getProtocol(){
        return _protocol;
    }
    
    public String getPortConn(){
        return _portConn;
    }
    
    public String getUser(){
        return _user;
    }
    
    public String getAdParam(){
        return _adParam;
    }
    
    public void setHids(Boolean value){
        _hids = value;
    }
    
    public void setSid(String value){
        _sid = value;
    }
    
    public void setIntrusionType(String value){
        _intrusionType = value;
    }
    
    public void setMainIP(String value){
        _mainIP = value;
    }
    
    public void setPeerIP(String value){
        _peerIP = value;
    }
    
    public void setProtocol(String value){
        _protocol = value;
    }
    
    public void setPortConn(String value){
        _portConn = value;
    }
    
    public void setUser(String value){
        _user = value;
    }
    
    public void setAdParam(String value){
        _adParam = value;
    }
}