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

package networkContext.learning.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import networkContext.learning.InfoNetwork;
import networkContext.learning.util.DateFormatter;
import networkContext.learning.util.PropsUtil;

/**
 * This class manages the data base where the profile of the network context will
 * be stored. This class establishes a connection with the database and inserts
 * the information stored during the learning phase of the Network Context Module.
 * It implements the BDManagerIF interface.
 * 
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class BDManager implements BDManagerIF {

    private static Connection conNet;
    private static PropsUtil props = new PropsUtil();
     
    private final String dataSource = props.getOntairsDatabaseNameValue();
    private final String username = props.getMysqlConnectionUsernameValue();
    private final String password = props.getMysqlConnectionPasswordValue();
    private final String mysql_server = props.getMysqlServerNameValue();
    private final String network_context_profile_table = props.getNetworkContextProfileTableNameValue();
        
    public void connectDataSource() throws DAOException {
        if (conNet != null) {
            disconnectDataSource();
        }
        try {
            conNet = DriverManager.getConnection("jdbc:mysql://"+mysql_server+":3306/" + dataSource, username, password);
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

    public void insertNetworkInfo(InfoNetwork i) throws SQLException, DAOException {
        connectDataSource();
        Date normaldate = i.getDate();
        String stringdate = DateFormatter.dateToString(normaldate, "yyyy-MM-dd HH:mm:ss");
        Calendar calendario = new GregorianCalendar();
        calendario.setTime(normaldate);
        int hour = calendario.get(Calendar.HOUR_OF_DAY);
        int minutes = calendario.get(Calendar.MINUTE);
        long time = hour * 60 + minutes;
        int day = calendario.get(Calendar.DAY_OF_WEEK);
        
        String insert = "insert into "+network_context_profile_table+" (date, day_week, time, duration, eth_proto, ip_proto, portOr,  portDes, ipOr, ipDes, src_pkts, dst_pkts, src_bytes, dst_bytes, total_bytes) VALUES ('" + stringdate + "'," + day + "," + time + ", " + i.getDuration() + ","+i.getEth_proto()+ ", "+i.getIp_proto()+","+ i.getPortSrc() + "," + i.getPortDes() + ",'" + i.getIpSrc() + "','" + i.getIpDes() + "' , "+i.getSrc_pkts()+", "+i.getDst_pkts()+", "+i.getSrc_bytes()+", "+i.getDst_bytes()+", "+i.getTotal_bytes()+");";

        //insert into CONNECTION (date, day_week, time, duration, portOr,  portDes, ipOr, ipDes) VALUES ('Wed Apr 09 14:31:14 EDT 2008',4,600,14, 40, 800, '127.0.0.1', '138.4.7.135');
        System.out.println(insert);
        try {
            Statement stmt = conNet.createStatement();
            stmt.executeUpdate(insert);
            stmt.close();
        } catch (SQLException sqlE) {
            throw new DAOException(sqlE);
        }
    }   
}  