/**
 * "ThreatWeights" Java class is free software: you can redistribute
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

package evaluation.system.trainer;

import java.util.HashMap;

/**
 * This class represents the weights of the indicators of the context which are
 * used by the intrusion response evaluation system to get the efficiency of 
 * a response action.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class ThreatWeights {

    private double network_anomaly_weight;
    private double process_anomaly_weight;
    private double CPU_anomaly_weight;
    private double disk_anomaly_weight;
    private double latency_anomaly_weight;
    private double users_anomaly_weight;
    private double status_anomaly_weight;
    private double zombie_anomaly_weight;
    private double sshFailed_anomaly_weight;
    private String network = "Network";
    private String process = "Process";
    private String CPU = "CPU";
    private String disk = "Disk";
    private String Latency = "Latency";
    private String User = "User";
    private String Status = "Status";
    private String Zombie = "Zombie";
    private String SSHFailed  = "SSHFailed";
   
    // Constructores de la clase
    public ThreatWeights(){
        network_anomaly_weight = 0.112;
        process_anomaly_weight = 0.111;
        CPU_anomaly_weight = 0.111;
        disk_anomaly_weight = 0.111;
        latency_anomaly_weight = 0.111;
        users_anomaly_weight = 0.111;
        status_anomaly_weight = 0.111;
        zombie_anomaly_weight = 0.111;
        sshFailed_anomaly_weight = 0.111;
    }
    
    public ThreatWeights(double networkAW, double processAW, double CPUAW, double diskAW,
            double latencyAW, double usersAW, double statusAW, double zombieAW, double sshAW){
        this.network_anomaly_weight = networkAW;
        this.process_anomaly_weight = processAW;
        this.CPU_anomaly_weight = CPUAW;
        this.disk_anomaly_weight = diskAW;
        this.latency_anomaly_weight = latencyAW;
        this.users_anomaly_weight = usersAW;
        this.status_anomaly_weight = statusAW;
        this.zombie_anomaly_weight = zombieAW;
        this.sshFailed_anomaly_weight = sshAW;
    }

    public HashMap getThreatWeightHashMap(){
        HashMap threatwe = new HashMap();
        threatwe.put(network, getNetworkAnomalyWeight());
        threatwe.put(process, getProcessAnomalyWeight());
        threatwe.put(CPU, getCPUAnomalyWeight());
        threatwe.put(disk, getDiskAnomalyWeight());
        threatwe.put(Latency, getLatencyAnomalyWeight());
        threatwe.put(User, getUserAnomalyWeight());
        threatwe.put(Status, getStatusAnomalyWeight());
        threatwe.put(Zombie, getZombieAnomalyWeight());
        threatwe.put(SSHFailed, getSSHFailedAnomalyWeight());
        return threatwe;

    }

    public double getNetworkAnomalyWeight(){
        return network_anomaly_weight;
    }
    
    public double getProcessAnomalyWeight(){
        return process_anomaly_weight;
    }
    
    public double getCPUAnomalyWeight(){
        return CPU_anomaly_weight;
    }
    
    public double getDiskAnomalyWeight(){
        return disk_anomaly_weight;
    }
    
    public double getLatencyAnomalyWeight(){
        return latency_anomaly_weight;
    }
    
    public double getUserAnomalyWeight(){
        return users_anomaly_weight;
    }
    
    public double getStatusAnomalyWeight(){
        return status_anomaly_weight;
    }
    
    public double getZombieAnomalyWeight(){
        return zombie_anomaly_weight;
    }
    
    public double getSSHFailedAnomalyWeight(){
        return sshFailed_anomaly_weight;
    }

    public void setNetworkAnomalyWeight(double value){
        network_anomaly_weight = value;
    }
    
    public void setProcessAnomalyWeight(double value){
        process_anomaly_weight = value;
    }
    
    public void setCPUAnomalyWeight(double value){
        CPU_anomaly_weight = value;
    }
    
    public void setDiskAnomalyWeight(double value){
        disk_anomaly_weight = value;
    }
    
    public void setLatencyAnomalyWeight(double value){
        latency_anomaly_weight = value;
    }
    
    public void setUsersAnomalyWeight(double value){
        users_anomaly_weight = value;
    }
    
    public void setStatusAnomalyWeight(double value){
        status_anomaly_weight = value;
    }
    
    public void setZombieAnomalyWeight(double value){
        zombie_anomaly_weight = value;
    }
    
    public void setSSHFailedAnomalyWeight(double value){
        sshFailed_anomaly_weight = value;
    }
}