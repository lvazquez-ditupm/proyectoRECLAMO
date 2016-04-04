/**
 * "PropsUtil" Java class is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version always keeping the additional terms
 * specified in this license.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *
 * Additional Terms of this License --------------------------------
 *
 * 1. It is Required the preservation of specified reasonable legal notices and
 * author attributions in that material and in the Appropriate Legal Notices
 * displayed by works containing it.
 *
 * 2. It is limited the use for publicity purposes of names of licensors or
 * authors of the material.
 *
 * 3. It is Required indemnification of licensors and authors of that material
 * by anyone who conveys the material (or modified versions of it) with
 * contractual assumptions of liability to the recipient, for any liability that
 * these contractual assumptions directly impose on those licensors and authors.
 *
 * 4. It is Prohibited misrepresentation of the origin of that material, and it
 * is required that modified versions of such material be marked in reasonable
 * ways as different from the original version.
 *
 * 5. It is Declined to grant rights under trademark law for use of some trade
 * names, trademarks, or service marks.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (lgpl.txt). If not, see
 * <http://www.gnu.org/licenses/>
 */
package systemContext.anomalyDetector.util;

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
 * systemContextAnomalyDetector package.
 *
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class PropsUtil {

    private static Properties systemcontextanomalydectectorproperties;

    private static String SYSTEM_CONTEXT_ANOMALY_DETECTOR_PROPERTIES_FILE;
    //private static final String SYSTEM_CONTEXT_ANOMALY_DETECTOR_PROPERTIES_FILE= "/resources/systemContextAnomalyDetector.properties";
    private static final String SYSTEM_CONTEXT_NAGIOS_CHECKER_PATH_PROP = "system.context.checker.script.path";
    private static final String NAGIOS_SERVICE_PERFDATA_FILE_PATH_PROP = "nagios.service-perfdata.path";
    private static final String NAGIOS_HOST_PERFDATA_FILE_PATH_PROP = "nagios.host-perfdata.path";
    private static final String ONTAIRS_DATABASE_NAME_PROP = "airs.database.name";
    private static final String MYSQL_CONNECTION_USERNAME_PROP = "mysql.connection.username";
    private static final String MYSQL_CONNECTION_PASSWORD_PROP = "mysql.connection.password";
    private static final String MYSQL_SERVER_NAME_PROP = "mysql.server.name";
    private static final String SYSTEM_CONTEXT_PROFILE_TABLE_NAME_PROP = "system.context.profile.table.name";

    //Valores de los parametros del fichero properties
    private static String SYSTEM_CONTEXT_NAGIOS_CHECKER_PATH_VALUE;
    private static String NAGIOS_SERVICE_PERFDATA_FILE_PATH_VALUE;
    private static String NAGIOS_HOST_PERFDATA_FILE_PATH_VALUE;
    private static String ONTAIRS_DATABASE_NAME_VALUE;
    private static String MYSQL_CONNECTION_USERNAME_VALUE;
    private static String MYSQL_CONNECTION_PASSWORD_VALUE;
    private static String MYSQL_SERVER_NAME_VALUE;
    private static String SYSTEM_CONTEXT_PROFILE_TABLE_NAME_VALUE;

    private Object lock = new Object();

    public PropsUtil() {
        synchronized (lock) {
            if (systemcontextanomalydectectorproperties != null) {
                return;
            }
            systemcontextanomalydectectorproperties = new Properties();
            InputStream is = null;
            try {
                String configFile = "systemContextAnomalyDetector.conf";
                String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                path = URLDecoder.decode(path, "UTF-8");
                SYSTEM_CONTEXT_ANOMALY_DETECTOR_PROPERTIES_FILE = (new File(path).getParentFile().getPath() + File.separator + configFile).toString();

                File f = new File(SYSTEM_CONTEXT_ANOMALY_DETECTOR_PROPERTIES_FILE);
                is = new FileInputStream(f);
                //is = PropsUtil.class.getResourceAsStream(SYSTEM_CONTEXT_ANOMALY_DETECTOR_PROPERTIES_FILE);

                systemcontextanomalydectectorproperties.load(is);
                SYSTEM_CONTEXT_NAGIOS_CHECKER_PATH_VALUE = systemcontextanomalydectectorproperties.getProperty(SYSTEM_CONTEXT_NAGIOS_CHECKER_PATH_PROP);
                NAGIOS_SERVICE_PERFDATA_FILE_PATH_VALUE = systemcontextanomalydectectorproperties.getProperty(NAGIOS_SERVICE_PERFDATA_FILE_PATH_PROP);
                NAGIOS_HOST_PERFDATA_FILE_PATH_VALUE = systemcontextanomalydectectorproperties.getProperty(NAGIOS_HOST_PERFDATA_FILE_PATH_PROP);
                ONTAIRS_DATABASE_NAME_VALUE = systemcontextanomalydectectorproperties.getProperty(ONTAIRS_DATABASE_NAME_PROP);
                MYSQL_CONNECTION_USERNAME_VALUE = systemcontextanomalydectectorproperties.getProperty(MYSQL_CONNECTION_USERNAME_PROP);
                MYSQL_CONNECTION_PASSWORD_VALUE = systemcontextanomalydectectorproperties.getProperty(MYSQL_CONNECTION_PASSWORD_PROP);
                MYSQL_SERVER_NAME_VALUE = systemcontextanomalydectectorproperties.getProperty(MYSQL_SERVER_NAME_PROP);
                SYSTEM_CONTEXT_PROFILE_TABLE_NAME_VALUE = systemcontextanomalydectectorproperties.getProperty(SYSTEM_CONTEXT_PROFILE_TABLE_NAME_PROP);

                validate();
            } catch (IOException e) {
                //System.out.println("Could not read AIRS config. (File: " + AIRS_PROPERTIES_FILE );
                throw new RuntimeException("Could not read SystemContext module config. (File: " + SYSTEM_CONTEXT_ANOMALY_DETECTOR_PROPERTIES_FILE + ")", e);
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
    }

    private void validate() {
        StringWriter swError = new StringWriter();
        PrintWriter pwError = new PrintWriter(swError);
        if (null == SYSTEM_CONTEXT_NAGIOS_CHECKER_PATH_VALUE) {
            System.out.println("File \"" + SYSTEM_CONTEXT_NAGIOS_CHECKER_PATH_PROP
                    + "\" not defined.");
        }
        if (null == NAGIOS_SERVICE_PERFDATA_FILE_PATH_VALUE) {
            System.out.println("Property \"" + NAGIOS_SERVICE_PERFDATA_FILE_PATH_PROP
                    + "\" not defined.");
        }
        if (null == NAGIOS_HOST_PERFDATA_FILE_PATH_VALUE) {
            System.out.println("Property \"" + NAGIOS_HOST_PERFDATA_FILE_PATH_PROP
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
        if (null == SYSTEM_CONTEXT_PROFILE_TABLE_NAME_VALUE) {
            System.out.println("Property \"" + SYSTEM_CONTEXT_PROFILE_TABLE_NAME_PROP
                    + "\" not defined.");
        }
    }

    public String getSystemContextNagiosCheckerPathValue() {
        return SYSTEM_CONTEXT_NAGIOS_CHECKER_PATH_VALUE;
    }

    public String getNagiosServicePerfdataFilePathValue() {
        return NAGIOS_SERVICE_PERFDATA_FILE_PATH_VALUE;
    }

    public String getNagiosHostPerfdataFilePathValue() {
        return NAGIOS_HOST_PERFDATA_FILE_PATH_VALUE;
    }

    public String getOntairsDatabaseNameValue() {
        return ONTAIRS_DATABASE_NAME_VALUE;
    }

    public String getMysqlConnectionUsernameValue() {
        return MYSQL_CONNECTION_USERNAME_VALUE;
    }

    public String getMysqlConnectionPasswordValue() {
        return MYSQL_CONNECTION_PASSWORD_VALUE;
    }

    public String getMysqlServerNameValue() {
        return MYSQL_SERVER_NAME_VALUE;
    }

    public String getSystemContextProfileTableNameValue() {
        return SYSTEM_CONTEXT_PROFILE_TABLE_NAME_VALUE;
    }
}
