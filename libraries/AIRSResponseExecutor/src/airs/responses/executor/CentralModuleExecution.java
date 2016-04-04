/**
 * "CentralModuleExecution" Java class is free software: you can redistribute
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

import airs.responses.executor.utils.PropsUtil;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents the component responsible to identify and check the 
 * available execution agents, to build requests of response execution and to 
 * send these requests to the corresponding agent.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class CentralModuleExecution {
   
    private FixedParamsFormatter fixedParams;
    private String inferredResponseName;
    private ResponseActionParams paramsAlert;
    private ResponseAction selected_response;
    private ExecutorAgent agent_alert;
    
    private ArrayList<String> responsesbysid = new ArrayList<String>();
    private  Iterator i;
    public FixedParamsChecker fix_checker;
    
    public CentralModuleExecution(){
        this.fix_checker = new FixedParamsChecker();
        fix_checker.checkFixedParms();
        
        //this.fixedParams=FixedParamsChecker.getFixedParams(); 
        
    }
    
    public boolean BuildResponseActionRequest(String inferredResponseName, ResponseActionParams paramsAlert){
        this.inferredResponseName = inferredResponseName;
        this.paramsAlert = paramsAlert;
        if(inferredResponseName==null){
            PropsUtil.LoggMessage(1, "Error:[BuildResponseActionRequest]: Undefined response action.","AIRSResponseExecutor");
            return false;
        }else{
            this.fixedParams = fix_checker.getFixedParams();

            /*Show response action information parsed from the files: CONF_FILE and PLUGIN_NUMBERS_FILE*/
            /*airsExecutor.getExecutorAgentList().PrintExecutorAgentList();
            airsExecutor.getGroupExecutorList().PrintGroupExecutorAgentsList();
            airsExecutor.getResponseActionList().PrintResponseActionList();*/
            /*We define a response action params, these are obtained from alerts*/
            //params_alert = new ResponseActionParams("0", "172.16.131.1", "172.16.131.129","tcp", 21, "file.txt");
            PropsUtil.LoggMessage(3, "Info:[MCER] New request for execution of response action: "
                        + paramsAlert.getMainIP() + "->" + paramsAlert.getPeerIP()+":"+paramsAlert.getPortConn() +
                        " Protocol:"+paramsAlert.getProtocol()+" Intrusion Signature ID (SID):"+paramsAlert.getSid(),
                        "AIRSResponseExecutor");

            //Get the response actions that AIRS Executor could execute based on Intrusion Signature ID*/

            i = fixedParams.getResponseActionList().getResponseActionList().iterator();
            if (paramsAlert.getSid().equalsIgnoreCase("ALL")) {
                    System.out.println("No se proporciona ningún sid asociado a la alerta");
            } else {
                while(i.hasNext()){
                    String nameresponse= ((ResponseAction)i.next()).IsAllowedExecute(Integer.parseInt(paramsAlert.getSid()));
                    if(nameresponse !=null){
                        responsesbysid.add(nameresponse);
                    }
                }
            

            if(PropsUtil.DEBUG_AIRS_EXE){
                System.out.print("Debug:[AIRSResponseExecutor] \""+paramsAlert.getSid() + "\" intrusion SID could execute the "
                        + "following response actions:");
                i = responsesbysid.iterator();
                System.out.print(i.next() + ", "); 
                while(i.hasNext()){                
                    System.out.print(i.next() + ", ");                
                }
                System.out.println();
            }
            }
            /*THis statement let us to get a response action params defined in file configuration, 
             * it should specified as in paramenter*/

            if(PropsUtil.DEBUG_AIRS_EXE)
                System.out.println("Debug:[MCER] Selected response action:" + inferredResponseName);
            selected_response = fixedParams.getResponseActionList().getExistResponseActions(inferredResponseName);

            //fixedParams.getResponseActionList().PrintResponseActionList();

            if (selected_response != null) { //Check if the response action exists.
                if(!selected_response.getComposed()){ //If the response action is simple.
                    PropsUtil.LoggMessage(3,"Info:[Main] Trying to send '"+ inferredResponseName+"' response action request to communication module..." , "AIRSResponseExecutor");
                    if (selected_response.getGroupExecutorAgents().getId().equalsIgnoreCase("THIS")) { //Check if the response action will use a THIS profile
                        if(PropsUtil.DEBUG_AIRS_EXE){
                            System.out.println("Debug:[MCER] 'THIS' profile configured...");
                            System.out.println("Debug:[MCER] Searching an executor agent which matches with "+paramsAlert.getMainIP()+"...");
                        }                 
                        agent_alert = fixedParams.getExecutorAgentList().getExistExecutorbyip(paramsAlert.getMainIP());
                        if (agent_alert != null) { 
                            selected_response.getGroupExecutorAgents().AddExecutorAgent(agent_alert);
                            if(PropsUtil.DEBUG_AIRS_EXE)
                                System.out.println("Debug:[MCER] Executor agent found! Using: "+agent_alert.toString());
                        } else {
                            agent_alert = new ExecutorAgent();
                            agent_alert.setHost(paramsAlert.getMainIP());
                            if(PropsUtil.DEBUG_AIRS_EXE)
                                System.out.println( "Info:[Main] Executor agent not found! Using 'THIS' profile: "+agent_alert.toString());
                            selected_response.getGroupExecutorAgents().AddExecutorAgent(agent_alert);
                        }
                    }
                    if (selected_response.SendToCommunicationModule(paramsAlert)) {
                        PropsUtil.LoggMessage(1, "Info: [Main] '"+ selected_response.getName()
                                    + "' response action request has been executed.", "AIRSResponseExecutor");
                        return true;
                    }
                    else{
                        PropsUtil.LoggMessage(1, "Error:[Main] '"+ selected_response.getName()
                                    + "' response action request has failed.", "AIRSResponseExecutor");
                        return false;
                    }
                }else{ //If the response action is composite.
                    PropsUtil.LoggMessage(3,"Info:[Main] Trying to send "+ inferredResponseName+" composite response action to communication module..." , "AIRSResponseExecutor");
                    i = selected_response.getResponses().iterator();
                    ResponseAction individual_response;
                    String individual_response_name;
                    boolean complex_result = true;
                    while(i.hasNext()){
                        individual_response_name = (String) i.next();
                        individual_response = (ResponseAction) fixedParams.getResponseActionList().getExistResponseActions(individual_response_name);
                        if(individual_response!=null){             
                            if (individual_response.getGroupExecutorAgents().getId().equalsIgnoreCase("THIS")) { //Check if the response action will use a THIS profile
                                if(PropsUtil.DEBUG_AIRS_EXE){
                                    System.out.println("Debug:[MCER][Composite:"+inferredResponseName+"] 'THIS' profile configured...");
                                    System.out.println("Debug:[MCER][Composite:"+inferredResponseName+"] Searching an executor agent which matches with "+paramsAlert.getMainIP()+"...");
                                }
                                agent_alert = fixedParams.getExecutorAgentList().getExistExecutorbyip(paramsAlert.getMainIP());
                                    if (agent_alert != null) { //Check if exists an executor agent which matches IP parms_alert
                                        individual_response.getGroupExecutorAgents().AddExecutorAgent(agent_alert);
                                        if(PropsUtil.DEBUG_AIRS_EXE)
                                             System.out.println("Debug:[MCER][Composite:"+inferredResponseName+"] Executor agent found! Using: "+agent_alert.toString());
                                    } else {
                                        agent_alert = new ExecutorAgent();
                                        agent_alert.setHost(paramsAlert.getMainIP());
                                        if(PropsUtil.DEBUG_AIRS_EXE)
                                            System.out.println( "Debug:[Main][Composite:"+inferredResponseName+"] Executor agent not found! Using 'THIS' profile: "+agent_alert.toString());
                                        individual_response.getGroupExecutorAgents().AddExecutorAgent(agent_alert);
                                    }       
                            }
                            if (individual_response.SendToCommunicationModule(paramsAlert)) {
                                PropsUtil.LoggMessage(1, "Info: [Main] '"+ inferredResponseName
                                    + "' response action request has been executed.", "AIRSResponseExecutor");
                            }else{  
                                PropsUtil.LoggMessage(1, "Error:[Main][Composite:"+inferredResponseName+"]'"+ individual_response.getName()
                                            + "' response action request has failed.", "AIRSResponseExecutor");
                                complex_result = false;
                            }                      
                            
                        }else{
                            PropsUtil.LoggMessage(1, "Error:[Main][Composite:"+inferredResponseName+"] Undefined \""+individual_response_name+"\" "
                                    + " response action.","AIRSResponseExecutor");
                            complex_result = false;
                        }
                    }
                    return complex_result;
                }
            } else {  
                PropsUtil.LoggMessage(1, "Error:[Main] Undefined response action.","AIRSResponseExecutor");
                return false;
            }
        }
    }    
}