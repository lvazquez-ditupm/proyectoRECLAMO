/**
 * "ResponseAction" Java class is free software: you can redistribute
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents a response action.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class ResponseAction {
    //Attributes
    private String _name;
    private GroupExecutorAgents _groupExecutors;
    private String _duration; // Duraciòn de bloqueo debe ir entre comillas, se puede especificar:min, sec, hours,etc.
    private String _mode; //Puede ser: in, out, both o conn.
    private String _executeFlag; //El valor execute me indicarà que se trata de la ejecuciòn de una acciòn, mientras que undo me indica que hay que deshacer la acciòn.
    private Plugin _plugin;
    private String _who;
    private Boolean _composed;
    private ArrayList<String> _responses;
    private SidsGroup _sids;
    
    //Constructors
    public ResponseAction(){
        _name = null;
        _groupExecutors = null;
        _duration = "\"5min\"";
        _executeFlag = "execute";
        _mode = "conn";
        _plugin = new Plugin();
        _who = "src";
        _composed = false;
        _responses = new ArrayList<String>();
        _sids=new SidsGroup();
    }   
    
    public ResponseAction(String initName, GroupExecutorAgents initGroup, String initDuration, String initExecuteFlag, String initMode, Plugin initPlugin, String initWho, Boolean initComposed, ArrayList<String> initResponses, SidsGroup sids){
        _name = initName;
        _groupExecutors = initGroup;
        _duration = initDuration;
        _executeFlag = initExecuteFlag;
        _mode = initMode;
        _plugin = initPlugin;
        _who =  initWho;
        _composed = initComposed;
        _responses = initResponses;
        _sids=sids;
    }
    
//Procedures and Fuctions
    public boolean SendToCommunicationModule(ResponseActionParams actionParams){
        BufferedReader[] out = {new BufferedReader(new InputStreamReader(System.in)),new BufferedReader(new InputStreamReader(System.in))};
        String linea;
        String cmd;
        Process externalCmd;
        InputStream input;
        InputStream error;
        boolean success = true; 
        
            //Se asignan los paràmetros obligatorios
            cmd = " -v -du " +getDuration() + " -sid " + actionParams.getSid() +
                    " -plugin " + getPlugin().getNumber() + " -who " + getWho() ;
            //Solo se envìan los paràmetros no nulos
            if(actionParams.getPortConn() != null){  //Alerta desde un NIDS
               cmd = cmd + " -port " + actionParams.getPortConn();
            }
            if(actionParams.getMainIP() != null){
                cmd = cmd + " -i " +  actionParams.getMainIP();                
            }
            if(getMode() != null){
                cmd = cmd + " -mo "+getMode();
            }
            if(actionParams.getPeerIP() != null){
                cmd = cmd + " -peer " + actionParams.getPeerIP();
            }
            if(actionParams.getProtocol() != null){
                cmd = cmd + " -proto " + actionParams.getProtocol();
            }
            if(actionParams.getUser()!=null){
                cmd = cmd + " -user " + actionParams.getUser();                   
            }
            if(actionParams.getAdParam() != null){
                cmd = cmd + " -aditional " + actionParams.getAdParam();
            }
            cmd = cmd + " " + getGroupExecutorAgents().getExecutorsFormatted();        
        
        if(getExecuteFlag().equalsIgnoreCase("undo")){
            cmd = "./communication-debug -vv -u " + cmd;
        }else{
            cmd = "./communication-debug -vv -e " + cmd;
        }            
        try{
            System.out.println("Debug:[MCER] Sending request to Communication module:"+cmd);
            externalCmd = Runtime.getRuntime().exec(cmd);            
            //Operations.LoggMessage(3, "Info: "
              //    + "Executing Response Action "+cmd, "ResponseAction");
            input = externalCmd.getInputStream();
            error = externalCmd.getErrorStream();
            out[0] = new BufferedReader(new InputStreamReader(input));
            out[1] = new BufferedReader(new InputStreamReader(error));
            while((linea = out[0].readLine()) != null){
                System.out.println(linea);
            }
            while((linea = out[1].readLine()) != null){
                System.out.println(linea);
                success = false;
            }
            //return true;
            return success;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        
    }
    
    public BufferedReader[] ExecuteExternalCommand(String command){
        BufferedReader[] response = {new BufferedReader(new InputStreamReader(System.in)),new BufferedReader(new InputStreamReader(System.in))};
        try{
            Process externalCmd = Runtime.getRuntime().exec(command);
            InputStream input = externalCmd.getInputStream();
            InputStream error = externalCmd.getErrorStream();
            response[0] = new BufferedReader(new InputStreamReader(input));
            response[1] = new BufferedReader(new InputStreamReader(error));
            return response;
        }catch(Exception e){
            e.printStackTrace();
            return response;
        }
    }
    
    public String IsAllowedExecute(Integer sid){
            Iterator i = this._sids.getSidsList().iterator();
            Integer auxsid;
            while(i.hasNext()){
                auxsid=(Integer)i.next();
                if(auxsid==sid || auxsid==0 ){
                    return this._name;
                }
            }
            return null;
            
    }
   
    @Override public String toString(){
       try{ 
           if(!this.getComposed()){
               return getName() + ", Type:Simple, Group Executors Agents:" 
                        + getGroupExecutorAgents().getId() + ", Plugin:"
                        + getPlugin().getHandler()+ ", Who:" + getWho() 
                         + ", Action:" + getExecuteFlag()+", Duration:" + getDuration() 
                        + ", Mode-Block:" + getMode() +", SIDs group:"+getSids().getNameSidsGroup();
           }
           else{
               Iterator i = this.getResponses().iterator();
               String res = "";
               while(i.hasNext()){
                   res=res+" "+i.next(); 
               }
               return getName() + ", Type:Composed, Responses:"+res;
           }
       }catch(Exception e){
           return null;
       }
    }
   
    //Properties    
    public String getName(){
        return _name;
    }
    
    public GroupExecutorAgents getGroupExecutorAgents(){
        return _groupExecutors;
    }
    
    public String getDuration(){
        return _duration;
    }
    
    public String getExecuteFlag(){
        return _executeFlag;
    }
    
    public String getMode(){
        return _mode;
    }
    
    public String getWho(){
        return _who;
    }
    
    public Plugin getPlugin() {
        return _plugin;
    }
    
    public Boolean getComposed(){
        return _composed;
    }
    
    public ArrayList<String> getResponses(){
        return _responses;
    }
    
    public SidsGroup getSids(){
        return _sids;
    }
    
    public  void setName(String value){
        _name = value;
    }
    
    public void setGroupExecutorAgents(GroupExecutorAgents value){
        _groupExecutors =  value;
    }
    
    public void setDuration(String value){
        _duration = value;
    }
    
    public void setExecuteFlag(String value){
        _executeFlag =  value;
    }
    
    public void setMode(String value){
        _mode = value;
    }
    
    public void setWho(String value){
        _who = value;
    }
    
    public void setPlugin(Plugin value) {
        _plugin = value;
    }
    
    public void setComposed(Boolean value){
        _composed = value;
    }
    
    public void setResponses(ArrayList<String> value){
        _responses = value;
    }
    
    public void setSids(SidsGroup value){
        _sids = value;
    }
}