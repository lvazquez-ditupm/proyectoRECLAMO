/**
 * "BDManager" Java class is free software: you can redistribute
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

package parser.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import parser.util.PropsUtil;

/**
 * This class manages the data base where the profile of the network context is
 * stored. This class establishes a connection with the database and obtaines
 * the information stored during the learning phase of the Network Context Module.
 * It implements the BDManagerIF interface.
 * 
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class BDManager implements BDManagerIF {

    private static Connection conNet;
    private PropsUtil props = new PropsUtil();
    private final String dataSource = props.getOntairsDatabaseNameValue();
    private final String username = props.getMysqlConnectionUsernameValue();
    private final String password = props.getMysqlConnectionPasswordValue();
    private final String mysql_server = props.getMysqlServerNameValue();
    private final String network_assets_table = props.getNetworkAssetsTableNameValue();

    public void connectDataSource() throws DAOException {
        if (conNet != null) {
            disconnectDataSource();
        }
        try {
            conNet = DriverManager.getConnection("jdbc:mysql://"+mysql_server+"/" + dataSource, username, password);
        } catch (SQLException ex) {
            Logger.getLogger(BDManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disconnectDataSource() throws DAOException {
        try {
            conNet.close();
            conNet = null;
        } catch (SQLException sqlE) {
            throw new DAOException(sqlE);
        }
    }

    public String obtainSubNetworkInfo(String hostIP) throws SQLException, DAOException {
        connectDataSource();
        String hostIPaddress=hostIP;
        String subnetworkName = null;
               
        String select = "SELECT location FROM "+network_assets_table+ " WHERE ipAddress='"+hostIPaddress+"'";
     //   System.out.println(select);
        try {
            Statement stmt = conNet.createStatement();
            ResultSet rs = stmt.executeQuery(select);
            while (rs.next()) {
                subnetworkName = rs.getString("location");
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return subnetworkName;
    } 
    
    public String obtainHostName(String hostIP) throws SQLException, DAOException{
        connectDataSource();
        String hostIPaddress=hostIP;
        String hostName = null;
               
        String select = "SELECT name FROM "+network_assets_table+ " WHERE ipAddress='"+hostIPaddress+"'";
       // System.out.println(select);
        try {
            Statement stmt = conNet.createStatement();
            ResultSet rs = stmt.executeQuery(select);
            while (rs.next()) {
                hostName = rs.getString("name");
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return hostName;        
    }
    
    public HashMap obtainAssetLevelOfImportance(String hostIP) throws SQLException, DAOException{
        connectDataSource();
        String hostIPaddress=hostIP;
        HashMap aloi = new HashMap();
        String select = "SELECT aloi_conf, aloi_int, aloi_ava, aloi_auth, level_of_importance FROM "+network_assets_table+ " WHERE ipAddress='"+hostIPaddress+"'";
       // System.out.println(select);
        try {
            Statement stmt = conNet.createStatement();
            ResultSet rs = stmt.executeQuery(select);
            while (rs.next()) {
                aloi.put("assetLevelOfImportance_confidentiality", rs.getString("aloi_conf"));
                aloi.put("assetLevelOfImportance_integrity", rs.getString("aloi_int"));
                aloi.put("assetLevelOfImportance_availability", rs.getString("aloi_ava"));
                aloi.put("assetLevelOfImportance_authenticity", rs.getString("aloi_auth"));
                aloi.put("assetLevelOfImportance", rs.getString("level_of_importance"));                    
            }
            rs.close();
            stmt.close(); 

        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return aloi;        
    }
}  