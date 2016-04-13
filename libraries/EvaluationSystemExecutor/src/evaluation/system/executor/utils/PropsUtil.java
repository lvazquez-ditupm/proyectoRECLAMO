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

package evaluation.system.executor.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * This class allows to get the required configuration parameters to run the 
 * evaluationSystemExecutor package.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class PropsUtil {

    private static  Properties evaluationsystemexecutorproperties;


    private static final String URI_PROP = "ontologies.uri";
    private static String EVALUATION_SYSTEM_EXECUTOR_PROPERTIES_FILE;
    private static final String SYSTEM_EVALUATION_THRESHOLD_LEVEL_SUCCESS_PROP="system.evaluation.threshold.level.success";
    private static final String ONTAIRS_ONTOLOGY_URI_PROP= "ontairs.airs.ontology.uri";
    private static final String ONTAIRS_ONTOLOGY_FILE_PROP= "ontairs.airs.ontology.file";
    private static final String ONTAIRS_ONTOLOGY_NAMESPACE_PROP= "ontairs.airs.ontology.namespace";
    private static final String RED_NEURONAL_FILE_PATH_PROP="red.neuronal.file";

    private static String URI_VALUE;
    private static String SYSTEM_EVALUATION_THRESHOLD_LEVEL_SUCCESS_VALUE;
    private static String ONTAIRS_ONTOLOGY_URI_VALUE;
    private static String ONTAIRS_ONTOLOGY_NAMESPACE_VALUE;
    private static String ONTAIRS_ONTOLOGY_FILE_VALUE;
    private static String RED_NEURONAL_FILE_PATH_VALUE;


    public  PropsUtil() {      
        evaluationsystemexecutorproperties = new Properties();
        InputStream is = null;
        try {
            String configFile = "evaluationSystemExecutor.conf";
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            EVALUATION_SYSTEM_EXECUTOR_PROPERTIES_FILE = (new File(path).getParentFile().getPath()+File.separator+configFile).toString();
            File f = new File (EVALUATION_SYSTEM_EXECUTOR_PROPERTIES_FILE);
            is = new FileInputStream(f);               
           // is = PropsUtil.class.getResourceAsStream(EVALUATION_SYSTEM_EXECUTOR_PROPERTIES_FILE);
            evaluationsystemexecutorproperties.load(is);
        
            URI_VALUE = evaluationsystemexecutorproperties.getProperty(URI_PROP);
            SYSTEM_EVALUATION_THRESHOLD_LEVEL_SUCCESS_VALUE = evaluationsystemexecutorproperties.getProperty(SYSTEM_EVALUATION_THRESHOLD_LEVEL_SUCCESS_PROP);
            ONTAIRS_ONTOLOGY_URI_VALUE = evaluationsystemexecutorproperties.getProperty(ONTAIRS_ONTOLOGY_URI_PROP);
            ONTAIRS_ONTOLOGY_FILE_VALUE = evaluationsystemexecutorproperties.getProperty(ONTAIRS_ONTOLOGY_FILE_PROP);
            ONTAIRS_ONTOLOGY_NAMESPACE_VALUE = evaluationsystemexecutorproperties.getProperty(ONTAIRS_ONTOLOGY_NAMESPACE_PROP);
            RED_NEURONAL_FILE_PATH_VALUE = evaluationsystemexecutorproperties.getProperty(RED_NEURONAL_FILE_PATH_PROP);

            validate();
        } catch (IOException e) {
            throw new RuntimeException("Could not read AIRS config. (File: " + EVALUATION_SYSTEM_EXECUTOR_PROPERTIES_FILE + ")", e);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private  void validate() {
        StringWriter swError = new StringWriter();
        PrintWriter pwError = new PrintWriter(swError);
        if (null == URI_VALUE) {
            System.out.println("File \"" + URI_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_URI_VALUE) {
            System.out.println("File \"" + ONTAIRS_ONTOLOGY_URI_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_FILE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_FILE_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_NAMESPACE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_NAMESPACE_PROP
                    + "\" not defined.");
        }
        if (null == SYSTEM_EVALUATION_THRESHOLD_LEVEL_SUCCESS_VALUE) {
            System.out.println("Property \"" + SYSTEM_EVALUATION_THRESHOLD_LEVEL_SUCCESS_PROP
                    + "\" not defined.");
        }
        if (null == RED_NEURONAL_FILE_PATH_VALUE) {
            System.out.println("Property \"" + RED_NEURONAL_FILE_PATH_PROP
                    + "\" not defined.");
        }
        /*String validationMsg = swError.toString();

        if (validationMsg.length() > 0) {
            throw new RuntimeException("AIRS misconfigured. (Config file \""
                    + AIRS_PROPERTIES_FILE + "\"): " + validationMsg);
        }*/
    }

    public String getSystemEvaluationThresholdLevelSuccessValue(){
        return SYSTEM_EVALUATION_THRESHOLD_LEVEL_SUCCESS_VALUE;
    }
    
    public  String getOntAIRSOntologyUriValue() {
        return URI_VALUE + ONTAIRS_ONTOLOGY_URI_VALUE;
    }
    
    public  String getOntAIRSOntologyFileValue() {
        return ONTAIRS_ONTOLOGY_FILE_VALUE;
    }   
    
    public  String getOntAIRSOntologyNamespaceValue() {
        return ONTAIRS_ONTOLOGY_NAMESPACE_VALUE;
    }
    
    public  String getNNFileValue() {
        return RED_NEURONAL_FILE_PATH_VALUE;
    }
}