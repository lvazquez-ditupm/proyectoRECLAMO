/**
 * "FixedParamsChecker" Java class is free software: you can redistribute
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

/**
 * This class parses the config files of the responses executor module:
 * airsResponseExecutor.conf and plugin-number.conf.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class FixedParamsChecker {

    
    private static FixedParamsFormatter fixedParams;
    private PropsUtil props = new PropsUtil();


    public FixedParamsChecker() {        

    }

    /***
     * This function parses the config files: airsResponseExecutor.conf and 
     * plugin-number.conf
     * @param: empty
     * @return: void
     */ 
    public  void checkFixedParms(){
//        System.out.println("***************************************************************+entrando en metodo checkFixedParams");
        try {   
  //          System.out.println("Metodo CheckFixedParams");
            
            //System.out.println("AlertReceptor is listening alerts...");
            //syslogAdapter = new SyslogAdapter(new Integer(args[0]).intValue(), "10.0.1.1");
            //MCER
            if(PropsUtil.DEBUG_AIRS_EXE)
                System.out.println("Debug:[MCER] Starting Central Module Execution...");
            fixedParams = new FixedParamsFormatter();
             /*Parse the configuration files*/
            if(PropsUtil.DEBUG_AIRS_EXE)           
                System.out.println("\n\nDebug:[MCER] Parsing "+ props.PLUGIN_NUMBERS_FILE +" config file...");
            fixedParams.ParsePlugin(props.PLUGIN_NUMBERS_FILE); 
            if(PropsUtil.DEBUG_AIRS_EXE)
                System.out.println("Debug:[MCER] Parsing "+ props.CONF_FILE +" config file...");
            fixedParams.ParseConfigFile(props.CONF_FILE);
                System.out.println("\n\n");

        } // try
        catch(Exception e) {
            e.printStackTrace();
        } // catch
    }//checkFixedParms
       
    public static FixedParamsFormatter getFixedParams(){
        return fixedParams;
    }

}//FixedParamsChecker
