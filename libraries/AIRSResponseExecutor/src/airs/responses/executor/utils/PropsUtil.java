/**
 * "PropsUtil" Java class is free software: you can redistribute
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

package airs.responses.executor.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Calendar;

/**
 * This class allows to get the required configuration parameters to run the 
 * AIRSResponseExecutor package.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class PropsUtil {
    
    //Constants varibles 
    public static boolean DEBUG_AIRS_EXE = true;
    public String CONF_FILE;
    public String PLUGIN_NUMBERS_FILE;
    public static String logFile;
    public static int logLevel;
            
    
    public  PropsUtil() {        
        try {
            String configFile = "../../ext_config_files/airsResponseExecutor.conf";
            String pluginNumberFile = "plugin-numbers.conf";
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            CONF_FILE = (new File(path).getParentFile().getPath()+File.separator+configFile).toString();
            PLUGIN_NUMBERS_FILE = (new File(path).getParentFile().getPath()+File.separator+pluginNumberFile).toString();
        
        } catch (IOException e) {
            throw new RuntimeException("Could not read SystemContext module config. (File: " + CONF_FILE + ")", e);
        } 
    }
    
    public static void LoggMessage(int level, String message, String module){
        Calendar date = Calendar.getInstance();
        File file;
        String log = "[" + Integer.toString(date.get(Calendar.DAY_OF_MONTH)) + "/" +
                Integer.toString(date.get(Calendar.MONTH))+"/" + 
                Integer.toString(date.get(Calendar.YEAR))+ " " +
                Integer.toString(date.get(Calendar.HOUR)) + ":" + 
                Integer.toString(date.get(Calendar.MINUTE)) + ":" +
                Integer.toString(date.get(Calendar.SECOND)) + "] [" + level +
                "] [" + module + "] " + message;
        System.out.println(log);
        if(level<= logLevel){            
            if(logFile.indexOf("/") == -1) 
                logFile = "/var/log/" + logFile;                
            file = new File(logFile);
            
            
            try {
                if(!file.exists()){
                    if(!file.createNewFile()){
                        PropsUtil.LoggMessage(1, "Error:[LogMessage] Can not create the log file.", "AIRSResponseExecutor");
                        return;
                    }
                }
                PrintWriter outLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
                outLog.println(log);
                outLog.close();   
            } catch (IOException ex) {
                PropsUtil.LoggMessage(1, "Error:[LogMessage] Fail I/O while recording in the log file.", "AIRSResponseExecutor");
            }
        }
    }
        
    public static String removeSpaces(String parameter){
        if(parameter instanceof String){
            String charReturn = new String();
            for (int i=0; i<parameter.length(); i++){
                if(parameter.charAt(i) != ' '){
                    charReturn += parameter.charAt(i);
                }
            }
            return charReturn;
        }else return null;
    }
}