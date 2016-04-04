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

package recparser.util;

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
 * Parser package.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class PropsUtil {

    private static  Properties parserproperties;

    private static String PARSER_PROPERTIES_FILE;
    //private static final String AIRS_PROPERTIES_FILE = "/resources/airs.properties";
    private static final String RESOURCES_RELEVANCE_FILE = "resources.relevance.file";
    private static final String IDS_RELIABILITY_FILE = "ids.reliability.file";
    private static final String SNORT_INTRUSION_CLASSIFICATION_FILE="snort.intrusion.file";
    private static final String IDMEF_INTRUSION_CLASSIFICATION_FILE="idmef.intrusion.file";

    private static  String RESOURCE_RELEVANCE;
    private static  String IDS_RELIABILITY;
    private static  String SNORT_INTRUSION_CLASSIFICATION;
    private static  String IDMEF_INTRUSION_CLASSIFICATION;

    public  PropsUtil() {
        parserproperties = new Properties();
        InputStream is = null;
        try {
            String configFile = "parser.conf";
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            PARSER_PROPERTIES_FILE = (new File(path).getParentFile().getPath()+File.separator+configFile).toString();
            File f = new File (PARSER_PROPERTIES_FILE);
            is = new FileInputStream(f);
           // is = PropsUtil.class.getResourceAsStream(PARSER_PROPERTIES_FILE);
            parserproperties.load(is);
            RESOURCE_RELEVANCE = parserproperties.getProperty(RESOURCES_RELEVANCE_FILE);
            IDS_RELIABILITY = parserproperties.getProperty(IDS_RELIABILITY_FILE);
            SNORT_INTRUSION_CLASSIFICATION = parserproperties.getProperty(SNORT_INTRUSION_CLASSIFICATION_FILE);
            IDMEF_INTRUSION_CLASSIFICATION = parserproperties.getProperty(IDMEF_INTRUSION_CLASSIFICATION_FILE);
                      
            validate();
        } catch (IOException e) {
            //System.out.println("Could not read AIRS config. (File: " + AIRS_PROPERTIES_FILE );
            throw new RuntimeException("Could not read AIRS config. (File: " + PARSER_PROPERTIES_FILE + ")", e);
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
        if (null == RESOURCE_RELEVANCE) {
            System.out.println("File \"" + RESOURCES_RELEVANCE_FILE
                    + "\" not defined.");
        }
        if (null == IDS_RELIABILITY) {
            System.out.println("Property \"" + IDS_RELIABILITY_FILE
                    + "\" not defined.");
        }
        if (null == SNORT_INTRUSION_CLASSIFICATION) {
            System.out.println("Property \"" + SNORT_INTRUSION_CLASSIFICATION_FILE
                    + "\" not defined.");
        }
        if (null == IDMEF_INTRUSION_CLASSIFICATION) {
            System.out.println("Property \"" + IDMEF_INTRUSION_CLASSIFICATION_FILE
                    + "\" not defined.");
        }
        /*String validationMsg = swError.toString();

        if (validationMsg.length() > 0) {
            throw new RuntimeException("AIRS misconfigured. (Config file \""
                    + AIRS_PROPERTIES_FILE + "\"): " + validationMsg);
        }*/
    }

    public  String getResourceRelevanceFile() {
        return RESOURCE_RELEVANCE;
    }

    public  String getIDSReliabilityFile() {
        return IDS_RELIABILITY;
    }

    public  String getSnortIntrusionClassificationFile() {
        return SNORT_INTRUSION_CLASSIFICATION;
    }
    
    public  String getIdmefIntrusionClassificationFile() {
        return IDMEF_INTRUSION_CLASSIFICATION;
    }   
 
}