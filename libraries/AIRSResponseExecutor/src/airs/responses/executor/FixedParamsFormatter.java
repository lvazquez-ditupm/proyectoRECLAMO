/**
 * "FixedParamsFormatter" Java class is free software: you can redistribute
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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class parses the config files of the responses executor module:
 * airsResponseExecutor.conf and plugin-number.conf.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class FixedParamsFormatter {

    private ExecutorAgentList _executorAgentList;
    private GroupExecutorAgentsList _groupExecutorList;
    private SidsGroupList _sidsGroupList;
    private ResponseActionList _responseActionList;
    private PluginList _pluginList;
    private PropsUtil props = new PropsUtil();

    public FixedParamsFormatter() throws IOException {
        
        _executorAgentList = new ExecutorAgentList();
        _groupExecutorList = new GroupExecutorAgentsList();
        _sidsGroupList = new SidsGroupList();
        _responseActionList = new ResponseActionList();
        _pluginList = new PluginList();
            
    }

    public void ParseConfigFile(String fileName) {
        try {
            BufferedReader buff = new BufferedReader(new FileReader(fileName));
            String line;            
            ExecutorAgent agent;            
            String configLine[];
            String handler;
            String params;
            GroupExecutorAgents group;
            SidsGroup sids;
            ResponseAction response;
            int numberLine = 1;
            
            while ((line = buff.readLine()) != null) {                
                configLine = ParseLine(line, numberLine);
                if (configLine != null) {
                    handler = configLine[0];
                    params = configLine[1];
                    if (handler.equalsIgnoreCase("executor_agent")) {
                        //It's because the executor agent could be defined with port param and it uses ':'
                        if (configLine.length > 2) {
                            params = configLine[1] + ":" + configLine[2];
                        }
                        agent = ParseExecutorAgent(params, numberLine);                        
                        if (agent != null) {
                            getExecutorAgentList().AddExecutorAgent(agent);
                            if(PropsUtil.DEBUG_AIRS_EXE){
                                System.out.println("Debug:[MCER] Parsing a new executor agent:"+agent.toString());
                            }
                        }
                    } else if (handler.equalsIgnoreCase("group")) {
                        group = ParseGroupExecutors(params, numberLine);
                        if (group != null) {
                            getGroupExecutorList().AddGroupExecutorAgent(group);
                            if(PropsUtil.DEBUG_AIRS_EXE){
                                System.out.print("Debug:[MCER] Parsing a new group of executor agents:");
                                group.PrintExecutorGroup();
                            }
                        }
                    } else if (handler.equalsIgnoreCase("sid_group")) {
                        sids = ParseSidsGroup(params, numberLine);
                        if (sids != null) {
                            getSidsGroupList().AddSidsGroup(sids);
                            if(PropsUtil.DEBUG_AIRS_EXE){
                                System.out.println("Debug:[MCER] Parsing a new SIDS group:"+ sids.toString());
                            }
                        }
                    } else if (handler.equalsIgnoreCase("response_action")) {
                        response = ParseResponseAction(params, numberLine);
                        if (response != null) {
                            getResponseActionList().AddsResponseAction(response);
                            if(PropsUtil.DEBUG_AIRS_EXE){
                                System.out.println("Debug:[MCER] Parsing a new response action:"+ response.toString());
                            }
                        }
                    } else if (handler.equalsIgnoreCase("composed")) {
                        //Configline[2] contains defines sids group composed:name>r1,r2,r3:sid-group
                        response = ParseComposed(params, configLine[2].trim(), numberLine);
                        if (response != null){
                            getResponseActionList().AddsResponseAction(response);
                            if(PropsUtil.DEBUG_AIRS_EXE){
                                System.out.println("Debug:[MCER] Parsing a new response action:"+ response.toString());
                            }
                        }
                    } else if (handler.equalsIgnoreCase("logfile")) {
                        PropsUtil.logFile = PropsUtil.removeSpaces(params);
                    } else if (handler.equalsIgnoreCase("loglevel")) {
                        PropsUtil.logLevel = Integer.parseInt(PropsUtil.removeSpaces(params));
                    } else {
                        PropsUtil.LoggMessage(1, "Error:[ParseConfigFile]["+props.CONF_FILE +
                                                ":"+numberLine+"] "+handler+" unknown option", "AIRSResponseExecutor");
                    }
                }
                numberLine++;
            }
        } catch (IOException e) {
            PropsUtil.LoggMessage(1, "Error:[ParseConfigFile] "+fileName+" can't be read or found.", "AIRSResponseExecutor");
        }
    }
    
    /***
     *This function ignores comment and blank lines. Returns only configuration lines.
     * @param line.- Configuration line.
     * @param numberLine .- Number line in the configuration file.
     * @return An String vector (size=2) which contains a handler word and its parameters. Return NULL when
     * line is a comment or blank.
     */
    public String[] ParseLine(String line, int numberLine) {
        try{
            if (line != null && !line.isEmpty()) {
                String options[];
                String option;
                options = line.split(":");
                option = PropsUtil.removeSpaces(options[0]);
                if (option.charAt(0) != '#') {
                    options[0] = PropsUtil.removeSpaces(options[0]);
                    return options;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }catch(StringIndexOutOfBoundsException ex){ //It's used to control the empty lines in configuration file
            return null;
        }
    }

    public ExecutorAgent ParseExecutorAgent(String line, int numberLine) {
        ExecutorAgent executor = new ExecutorAgent();
        String params[];
        String hostport[];
        params = line.split(",");
        if(params.length >= 2){
            executor.setId(params[0].trim());
            hostport=params[1].split(":");
            executor.setHost(hostport[0].trim());
            if(hostport.length==2){
                executor.setPort(Integer.parseInt(hostport[1].trim()));
            }
            if(params.length >=3){
                executor.setKey(params[2].trim());
            }
        }else{
            PropsUtil.LoggMessage(1, "Error:[ParseExecutorAgent]["+props.CONF_FILE+":"+numberLine+"] Undefined hostname or IP parameter.", "AIRSResponseExecutor");
            return null;
        }
        return executor;        
    }

    public GroupExecutorAgents ParseGroupExecutors(String line, int numberLine) {
        GroupExecutorAgents group = new GroupExecutorAgents();
        String params[];
        String agents[];
        String idExecutor;
        params=line.split(">");
        if(params.length==2){
            group.setId(params[0].trim());
            agents=params[1].split(",");
            for(int i=0; i<agents.length;i++){
                idExecutor=agents[i].trim();
                if (group.checkExistExecutor(idExecutor) == null) {
                    ExecutorAgent executor = getExecutorAgentList().getExistExecutorbyid(idExecutor);
                    if (executor != null) {
                        group.AddExecutorAgent(executor);
                    } else {
                        PropsUtil.LoggMessage(1, "Error:[ParseGroupExecutors]["+props.CONF_FILE+":"+numberLine+"] "
                                +idExecutor + " executor agent doesn't exists.", "AIRSResponseExecutor");
                    }
                        
                } else {
                    PropsUtil.LoggMessage(3, "Info:[ParseGroupExecutors]["+props.CONF_FILE+":"+numberLine+"] Skipping "
                            +idExecutor + " executor agent, it already exists.", "AIRSResponseExecutor");
                }
            } 
        }else{
            PropsUtil.LoggMessage(1, "Error:[ParseGroupExecutors["+props.CONF_FILE+":"+numberLine+"] Invalid number of parameters.", "AIRSExecutorAgent");
            return null;
        }
        return group;        
    }
    
    public SidsGroup ParseSidsGroup(String line, int numberLine) {
        SidsGroup sidsgroup = new SidsGroup();
        String params[];
        String sids[];
        params=line.split(">");
        if(params.length==2){
            sidsgroup.setNameSidsGroup(params[0].trim());
            sids=params[1].split(",");
            for(int i=0; i<sids.length;i++){
                sidsgroup.getSidsList().add(Integer.parseInt(sids[i].trim()));
            } 
        }else{
            PropsUtil.LoggMessage(1, "Error:[ParseSidsGroup["+props.CONF_FILE+":"+numberLine+"] Invalid number of parameters.", "AIRSExecutorAgent");
            return null;
        }
        return sidsgroup;        
    }

    public ResponseAction ParseResponseAction(String line, int numberLine) throws IOException {
        ResponseAction response = new ResponseAction();
        GroupExecutorAgents group;
        String groupId = new String();
        Plugin plugin;
        String sidsname;
        SidsGroup sidsgroup;
        String handlerplugin;
        String params[] = line.split(",");
        if (params.length == 8) {
            response.setName(PropsUtil.removeSpaces(params[0]));
            groupId = PropsUtil.removeSpaces(params[1]);
            if (groupId.equalsIgnoreCase("THIS")) {
                group = new GroupExecutorAgents();
            } else {
                group = getGroupExecutorList().getExistGroup(groupId);
                if (group == null) {
                    PropsUtil.LoggMessage(1, "Error:[ParseResponseAction]["+props.CONF_FILE+ ":"+ numberLine + 
                        "] Undefined group of executor agents.", "AIRSResponseExecutor");                    
                    return null;
                }
            }
            response.setGroupExecutorAgents(group);
            handlerplugin = PropsUtil.removeSpaces(params[2]);
            if (handlerplugin.equalsIgnoreCase("ALL")) {
                plugin = new Plugin();
                response.setPlugin(plugin);
            } else {
                plugin = getPluginList().getExistPlugin(handlerplugin);
                if (plugin != null) {
                    response.setPlugin(plugin);
                } else {
                     PropsUtil.LoggMessage(1, "Error:[ParseResponseAction]["+props.CONF_FILE+ ":"+ numberLine + 
                        "] Undefined plugin handler.", "AIRSResponseExecutor");
                    return null;
                }
            }
            response.setWho(PropsUtil.removeSpaces(params[3]));           
            response.setExecuteFlag(PropsUtil.removeSpaces(params[4]));
            response.setDuration(PropsUtil.removeSpaces(params[5]));
            response.setMode(PropsUtil.removeSpaces(params[6]));
            sidsname=params[7].trim();
            if(sidsname.equalsIgnoreCase("ALL")){
                response.setSids(new SidsGroup());
                response.getSids().getSidsList().add(0,0);
            }else{
                sidsgroup = this.getSidsGroupList().getExistSid(sidsname);
                if(sidsgroup != null){
                    response.setSids(sidsgroup);
                }else{
                 PropsUtil.LoggMessage(1, "Error:[ParseResponseAction]["+props.CONF_FILE+ ":"+ numberLine + 
                        "] Undefined SIDs group.", "AIRSResponseExecutor");                    
                    return null;                   
                }
            }
            return response;
        } else {
            PropsUtil.LoggMessage(1, "Error:[ParseResponseAction]["+props.CONF_FILE+ ":"+ numberLine + 
                        "] Invalid number of parameters.", "AIRSResponseExecutor");
            return null;
        }
    }

    public void ParsePlugin(String fileName) throws IOException {
            BufferedReader buff;
            String line;            
            Plugin plugin;
            String params[];
            int numberLine = 1;
            try {
                buff = new BufferedReader(new FileReader(fileName));  
                while ((line = buff.readLine()) != null) {                    
                    params = ParseLine(line, numberLine);
                    if (params != null) {
                        if (params.length == 2) {
                            plugin = new Plugin(PropsUtil.removeSpaces(params[0]), Integer.parseInt(PropsUtil.removeSpaces(params[1])));
                            getPluginList().AddsPlugin(plugin);
                            if(PropsUtil.DEBUG_AIRS_EXE){
                                System.out.println("Debug:[MCER] Adding a new plugin handler: "+plugin.toString());
                            }
                        } else {
                            PropsUtil.LoggMessage(1,"Error:[ParsePlugin]["+props.PLUGIN_NUMBERS_FILE+":"+numberLine+"]"
                        + "Incorrect number of params.","AIRSResponseExecutor");
                        }
                    }
                    numberLine++;
                }
            } catch (FileNotFoundException ex) {
                PropsUtil.LoggMessage(1, "Error:[ParsePlugin] "+fileName +" can't read or found.", "AIRSResponseExecutor");
            }
        


    }
    
    public ResponseAction ParseComposed (String line, String sidsname, int numberLine) throws IOException{
        ResponseAction response;
        String params[];
        String components[];
        String name_response;
        String name_composed;
        ArrayList<String> response_composed;
        SidsGroup sidsgroup;
        if(sidsname!=null){
            params = line.split(">");
            if(params.length != 2){
                PropsUtil.LoggMessage(1,"Error: [ParseComposed] ["+props.CONF_FILE+":"+numberLine+"]"
                        + "Invalid number of params.","AIRSResponseExecutor");
                return null;
            }

            if(!params[1].isEmpty()){
                components=params[1].split(",");
            }else {
                PropsUtil.LoggMessage(1,"Error: [ParseComposed] ["+props.CONF_FILE+":"+numberLine+"]"
                        + "Undefined response actions.","AIRSResponseExecutor");
                return null;
            }

            name_composed=PropsUtil.removeSpaces(params[0]);
            response_composed=new ArrayList<String>();
            for(int i=0; i<components.length; i++){
                name_response = PropsUtil.removeSpaces(components[i]);
                if(this.getResponseActionList().getExistResponseActions(name_response)==null){
                    PropsUtil.LoggMessage(1,"Error: [ParseComposed] ["+props.CONF_FILE+":"+numberLine+"]"
                        + " Undefined \""+ name_response +"\" response action.","AIRSResponseExecutor");
                    return null;
                }else{
                    response_composed.add(name_response);
                }
            }    
            if(sidsname.equalsIgnoreCase("ALL")){
                    sidsgroup = new SidsGroup();
                    sidsgroup.getSidsList().add(0,0);
                }else{
                    sidsgroup = this.getSidsGroupList().getExistSid(sidsname);
                    if(sidsgroup == null){
                        PropsUtil.LoggMessage(1, "Error:[ParseComposed]["+props.CONF_FILE+ ":"+ numberLine + 
                            "] Undefined SIDs group.", "AIRSResponseExecutor");                    
                        return null;                   
                    }
                }
            response=new ResponseAction(name_composed,null,null,null,null,null,null,true,response_composed,sidsgroup);
            return response;
        }else{
            PropsUtil.LoggMessage(1, "Error:[ParseComposed]["+props.CONF_FILE+ ":"+ numberLine + 
                            "] Undefined SIDs group parameter.", "AIRSResponseExecutor");                    
                        return null;
        }
    }

    //Auxiliar functions
    /**
     * 
     * @param parameter
     * @return 
     */
    //Properties
    public ExecutorAgentList getExecutorAgentList() {
        return _executorAgentList;
    }

    public GroupExecutorAgentsList getGroupExecutorList() {
        return _groupExecutorList;
    }

    public SidsGroupList getSidsGroupList() {
        return _sidsGroupList;
    }
    
    public ResponseActionList getResponseActionList() {
        return _responseActionList;
    }

    public PluginList getPluginList() {
        return _pluginList;
    }

    public void setGroupExecutorList(GroupExecutorAgentsList value) {
        _groupExecutorList = value;
    }

    public void setExecutorAgentList(ExecutorAgentList value) {
        _executorAgentList = value;
    }

    public void setSidsGroupList(SidsGroupList _sidsGroupList) {
        this._sidsGroupList = _sidsGroupList;
    }

    public void setResponseActionList(ResponseActionList value) {
        _responseActionList = value;
    }

    public void setPluginList(PluginList value) {
        _pluginList = value;
    }
}