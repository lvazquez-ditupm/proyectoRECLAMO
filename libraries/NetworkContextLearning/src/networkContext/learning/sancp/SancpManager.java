/**
 * "SancpManager" Java class is free software: you can redistribute
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
package networkContext.learning.sancp;

import java.lang.reflect.Field;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import networkContext.learning.util.LinuxUtils;
import networkContext.learning.util.PropsUtil;


/**
 * This class starts and stops the SANCP service to capture in real time the 
 * network traffic in a specific interface, and invokes the SnapshotCreator class, 
 * which generates the snapshot of the network connections during the execution
 * of SANCP.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class SancpManager {
    private PropsUtil props = new PropsUtil();
    private Process sancp;
    private String sancpDir;
    private String interfaceID;
    private String sancpConf;
    private String sancpLog;
    public SancpManager(String interfaceName){
        sancpDir= props.getNetworkContextSancpSensorPathValue();
        sancpConf=props.getNetworkContextSancpConfigFilePathValue();
        sancpLog=props.getNetworkContextSancpLogFilePathValue();
        this.interfaceID=interfaceName;
    }

    public int startSancp() {
        int pid = -1;
        String command ="sudo sancp --human-readable -d "+sancpDir+" -i "+interfaceID+" -c "+sancpConf+" > "+sancpLog;
        //String command ="sudo sancp --human-readable -d /var/nsm/sensor -i eth4 -c /etc/sancp/sancp.conf > /var/log/sancp.log";
        try  {
            System.out.println("Starting sancp: " + command);
            sancp = LinuxUtils.runCommand(command);
            if(sancp.getClass().getName().equals("java.lang.UNIXProcess")){
                try{
                    Field f =sancp.getClass().getDeclaredField("pid");
                    f.setAccessible(true);
                    pid = f.getInt(sancp)+4;
                    System.out.println("El PID del proceso arrancado es: "+pid);
                }catch (Throwable e){
                    
                }
            }
            return pid;
       }
       catch (Exception e){
           System.out.println(e);
           return pid;
       }
    }
         
    private boolean isSancpRunning(int processid) {
        return LinuxUtils.isSpecificProcessRunning(processid, "sancp");
    }
    
    public boolean stopSancp(int sancppid) {
        int sancpprocessnumber = sancppid;
        if (!isSancpRunning(sancpprocessnumber)) {
            //System.out.println("el proceso no está ejecutándose, ya ha sido parado");
            return true;
        }
        if (sancp != null) {
            try{
                sancp.destroy();
                return LinuxUtils.killProcess(sancpprocessnumber);
            }
            catch (Exception e){
                System.out.println("Error matando el proceso");
                return false;
            }
        }
        
        System.out.println("mata el proceso sancp");
        return LinuxUtils.killProcess(sancpprocessnumber);
    } 
}
