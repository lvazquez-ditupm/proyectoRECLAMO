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

package ontairs.utils;

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
    
    private static  Properties ontairsproperties;
    
    private static String ONTAIRS_PROPERTIES_FILE;
    //private static final String ONTAIRS_PROPERTIES_FILE= "/resources/ontairs.properties";
    private static final String PRELUDE_LOG_FILE_PATH_PROP = "prelude.log-file";
    private static final String PRELUDE_ALERTS_PATH_PROP = "prelude.alerts.path";
    private static final String ONTAIRS_ONTOLOGY_PREFIX_PROP= "ontairs.ontology.prefix";
    private static final String ONTAIRS_ONTOLOGY_INFERRED_FILE_PROP= "ontairs.ontology.inferred.file";
    private static final String ONTAIRS_ONTOLOGY_RULES_NAMESPACE_PROP= "ontairs.ontology.rules.namespace";
    private static final String ONTAIRS_ONTOLOGY_RULES_FILE_PROP= "ontairs.ontology.rules.file";
    private static final String ONTAIRS_ONTOLOGY_INSTANCES_NAMESPACE_PROP= "ontairs.ontology.instances.namespace";
    private static final String ONTAIRS_ONTOLOGY_INSTANCES_FILE_PROP= "ontairs.ontology.instances.file";
    private static final String ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_NAMESPACE_PROP= "ontairs.ontology.inferred-instances.namespace";
    private static final String ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_FILE_PROP= "ontairs.ontology.inferred-instances.file";
    
    //Parámetros para interactuar con la base de datos
    private static final String ONTAIRS_DATABASE_NAME_PROP="airs.database.name";
    private static final String MYSQL_CONNECTION_USERNAME_PROP="mysql.connection.username";
    private static final String MYSQL_CONNECTION_PASSWORD_PROP="mysql.connection.password";
    private static final String MYSQL_SERVER_NAME_PROP="mysql.server.name";
    private static final String NETWORK_ASSETS_TABLE_NAME_PROP="network.assets.table.name";
    
    //Valores de los parametros del fichero properties
    private static String PRELUDE_LOG_FILE_PATH_VALUE;
    private static String PRELUDE_ALERTS_PATH_VALUE;
    private static String ONTAIRS_ONTOLOGY_PREFIX_VALUE;
    private static String ONTAIRS_ONTOLOGY_INFERRED_FILE_VALUE;
    private static String ONTAIRS_ONTOLOGY_RULES_NAMESPACE_VALUE;
    private static String ONTAIRS_ONTOLOGY_RULES_FILE_VALUE;
    private static String ONTAIRS_ONTOLOGY_INSTANCES_NAMESPACE_VALUE;
    private static String ONTAIRS_ONTOLOGY_INSTANCES_FILE_VALUE;
    private static String ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_NAMESPACE_VALUE;
    private static String ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_FILE_VALUE;
    private static String ONTAIRS_DATABASE_NAME_VALUE;
    private static String MYSQL_CONNECTION_USERNAME_VALUE;
    private static String MYSQL_CONNECTION_PASSWORD_VALUE;
    private static String MYSQL_SERVER_NAME_VALUE;
    private static String NETWORK_ASSETS_TABLE_NAME_VALUE;
    
    public  PropsUtil() {
        ontairsproperties = new Properties();
        InputStream is = null;
        try {
            String configFile = "ontairs.conf";
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            ONTAIRS_PROPERTIES_FILE = (new File(path).getParentFile().getPath()+File.separator+configFile).toString();
            File f = new File (ONTAIRS_PROPERTIES_FILE);
            is = new FileInputStream(f);               
           // is = PropsUtil.class.getResourceAsStream(ONTAIRS_PROPERTIES_FILE);
            ontairsproperties.load(is);
            PRELUDE_LOG_FILE_PATH_VALUE = ontairsproperties.getProperty(PRELUDE_LOG_FILE_PATH_PROP);
            PRELUDE_ALERTS_PATH_VALUE = ontairsproperties.getProperty(PRELUDE_ALERTS_PATH_PROP);
            ONTAIRS_ONTOLOGY_PREFIX_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_PREFIX_PROP);
            ONTAIRS_ONTOLOGY_INFERRED_FILE_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_INFERRED_FILE_PROP);
            ONTAIRS_ONTOLOGY_RULES_NAMESPACE_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_RULES_NAMESPACE_PROP);
            ONTAIRS_ONTOLOGY_RULES_FILE_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_RULES_FILE_PROP);
            ONTAIRS_ONTOLOGY_INSTANCES_NAMESPACE_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_INSTANCES_NAMESPACE_PROP);
            ONTAIRS_ONTOLOGY_INSTANCES_FILE_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_INSTANCES_FILE_PROP);
            ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_NAMESPACE_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_NAMESPACE_PROP);
            ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_FILE_VALUE = ontairsproperties.getProperty(ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_FILE_PROP);
            ONTAIRS_DATABASE_NAME_VALUE = ontairsproperties.getProperty(ONTAIRS_DATABASE_NAME_PROP);
            MYSQL_CONNECTION_USERNAME_VALUE = ontairsproperties.getProperty(MYSQL_CONNECTION_USERNAME_PROP);
            MYSQL_CONNECTION_PASSWORD_VALUE = ontairsproperties.getProperty(MYSQL_CONNECTION_PASSWORD_PROP);
            MYSQL_SERVER_NAME_VALUE = ontairsproperties.getProperty(MYSQL_SERVER_NAME_PROP);
            NETWORK_ASSETS_TABLE_NAME_VALUE = ontairsproperties.getProperty(NETWORK_ASSETS_TABLE_NAME_PROP);
                      
            validate();
        } catch (IOException e) {
            throw new RuntimeException("Could not read SystemContext module config. (File: " + ONTAIRS_PROPERTIES_FILE + ")", e);
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
        if (null == PRELUDE_LOG_FILE_PATH_VALUE) {
            System.out.println("File \"" + PRELUDE_LOG_FILE_PATH_PROP
                    + "\" not defined.");
        }
        if (null == PRELUDE_ALERTS_PATH_VALUE) {
            System.out.println("Property \"" + PRELUDE_ALERTS_PATH_PROP
                    + "\" not defined.");
        }
        
        if (null == ONTAIRS_ONTOLOGY_PREFIX_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_PREFIX_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_INFERRED_FILE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_INFERRED_FILE_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_RULES_NAMESPACE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_RULES_NAMESPACE_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_RULES_FILE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_RULES_FILE_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_INSTANCES_NAMESPACE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_INSTANCES_NAMESPACE_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_INSTANCES_FILE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_INSTANCES_FILE_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_NAMESPACE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_NAMESPACE_PROP
                    + "\" not defined.");
        }        
        if (null == ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_FILE_VALUE) {
            System.out.println("Property \"" + ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_FILE_PROP
                    + "\" not defined.");
        }
        if (null == ONTAIRS_DATABASE_NAME_VALUE) {
            System.out.println("Property \"" + ONTAIRS_DATABASE_NAME_PROP
                    + "\" not defined.");
        }
        if (null == MYSQL_CONNECTION_USERNAME_VALUE) {
            System.out.println("Property \"" + MYSQL_CONNECTION_USERNAME_PROP
                    + "\" not defined.");
        }
        if (null == MYSQL_CONNECTION_PASSWORD_VALUE) {
            System.out.println("Property \"" + MYSQL_CONNECTION_PASSWORD_PROP
                    + "\" not defined.");
        }
        if (null == MYSQL_SERVER_NAME_VALUE) {
            System.out.println("Property \"" + MYSQL_SERVER_NAME_PROP
                    + "\" not defined.");
        }       
        if (null == NETWORK_ASSETS_TABLE_NAME_VALUE) {
            System.out.println("Property \"" + NETWORK_ASSETS_TABLE_NAME_PROP
                    + "\" not defined.");
        } 
    }

    public  String getPreludeLogFilePathValue(){
        return PRELUDE_LOG_FILE_PATH_VALUE;
    }
    
    public  String getPreludeAlertsPathValue() {
        return PRELUDE_ALERTS_PATH_VALUE;
    }
    
    public  String getOntAIRSOntologyPrefixValue() {
        return ONTAIRS_ONTOLOGY_PREFIX_VALUE;
    }
    
    public  String getOntAIRSOntologyInferredFileValue() {
        return ONTAIRS_ONTOLOGY_INFERRED_FILE_VALUE;
    }
    
    public  String getOntAIRSOntologyRulesNamespaceValue() {
        return ONTAIRS_ONTOLOGY_RULES_NAMESPACE_VALUE;
    }
    
    public  String getOntAIRSOntologyRulesFileValue() {
        return ONTAIRS_ONTOLOGY_RULES_FILE_VALUE;
    }
    
    public  String getOntAIRSOntologyInstancesNamespaceValue() {
        return ONTAIRS_ONTOLOGY_INSTANCES_NAMESPACE_VALUE;
    }
    
    public  String getOntAIRSOntologyInstancesFileValue() {
        return ONTAIRS_ONTOLOGY_INSTANCES_FILE_VALUE;
    }
    
    public  String getOntAIRSOntologyInferredInstancesNamespaceValue() {
        return ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_NAMESPACE_VALUE;
    }
    
    public  String getOntAIRSOntologyInferredInstancesFileValue() {
        return ONTAIRS_ONTOLOGY_INFERRED_INSTANCES_FILE_VALUE;
    }
    
    public  String getOntairsDatabaseNameValue() {
        return ONTAIRS_DATABASE_NAME_VALUE;
    }
    
    public  String getMysqlConnectionUsernameValue() {
        return MYSQL_CONNECTION_USERNAME_VALUE;
    }
    
    public  String getMysqlConnectionPasswordValue() {
        return MYSQL_CONNECTION_PASSWORD_VALUE;
    }
    
    public  String getMysqlServerNameValue() {
        return MYSQL_SERVER_NAME_VALUE;
    }
    
    public  String getNetworkAssetsTableNameValue() {
        return NETWORK_ASSETS_TABLE_NAME_VALUE;
    }   
   
}