/**
 * "GroupExecutorAgents" Java class is free software: you can redistribute
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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents a group of executor agents.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class GroupExecutorAgents {
    //Attributes
    private String _id;
    private ArrayList<ExecutorAgent> _listExecutors;
    
//Constructors
    public GroupExecutorAgents(){
        _id = "THIS";
        _listExecutors = new ArrayList<ExecutorAgent>();
    }
    
    public GroupExecutorAgents(String initId){
        _id = initId;
        _listExecutors = new ArrayList<ExecutorAgent>();
    }
    
//Procedures and Functions
    public void AddExecutorAgent(ExecutorAgent newExecutorAgent){       
        getListExecutors().add((newExecutorAgent));
       /* if (Operations.DEBUG_AIRS_EXE){
            System.out.println("Debug: [AIRSResponseExecutor] "
                    + "Adding the executor agent:" + newExecutorAgent.getId() +"/" 
                    + newExecutorAgent.getHost() + " to " + getId() +" group.");
        }*/
    }
    
    public String checkExistExecutor(String valueIdExecutor){
        Iterator i = getListExecutors().iterator();
        while(i.hasNext()){ 
            ExecutorAgent executor = (ExecutorAgent)i.next();
            if (executor.getId().equalsIgnoreCase(valueIdExecutor)){
                return executor.getId();
            }
        }
        return null;        
    }
    
    public void PrintExecutorGroup(){
        Iterator i = getListExecutors().iterator();
        System.out.println(getId());
        int j=1;
        while(i.hasNext()){   
            ExecutorAgent executor = (ExecutorAgent)i.next();
            System.out.println("\t"+ j +"."+ executor.toString());
            j++;
        }  
    }
    
    /*It is used to call ./airsexecutor <params> host:port[/key]*/
    public String getExecutorsFormatted(){
        Iterator i = getListExecutors().iterator();
        String executorsFormatted =  new String();
        while(i.hasNext()){   
            ExecutorAgent executor = (ExecutorAgent)i.next();
            if(executor.getKey() != null){
                executorsFormatted += executor.getHost() + ":" + executor.getPort()
                        + "/" + executor.getKey() + " "; 
            }else{
                executorsFormatted += executor.getHost() + ":" + executor.getPort()+" ";
            }
        }
        return executorsFormatted;
    }
    
    @Override public String toString(){
        return getId();
    }
    //Properties
    public String getId(){
        return _id;
    }
    
    public ArrayList getListExecutors(){
        return _listExecutors;
    }
    
    public void setId(String value){
        _id = value;
    }
    
    public void setListExecutors(ArrayList<ExecutorAgent> value){
        _listExecutors = value;
    }
}