/**
 * "BDManager" Java class is free software: you can redistribute it and/or
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
package networkContext.anomalyDetector.DAO;

import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import networkContext.anomalyDetector.InfoNetworkAnDetector;
import networkContext.anomalyDetector.util.DateFormatter;
import networkContext.anomalyDetector.util.PropsUtil;

/**
 * This class manages the data base where the profile of the network context is
 * stored. This class establishes a connection with the database and obtaines
 * the information stored during the learning phase of the Network Context
 * Module. It implements the BDManagerIF interface.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class BDManager implements BDManagerIF {

    private Connection conNet;
    private PropsUtil props = new PropsUtil();
    private final String dataSource = props.getOntairsDatabaseNameValue();
    private final String username = props.getMysqlConnectionUsernameValue();
    private final String password = props.getMysqlConnectionPasswordValue();
    private final String mysql_server = props.getMysqlServerNameValue();
    private final String network_context_profile_table = props.getNetworkContextProfileTableNameValue();
    private final String network_assets_table = props.getNetworkAssetsTableNameValue();
    private final Object lock = new Object();

    /**
     * This method establishes a connection with the database used to store the
     * network context profile.
     *
     * @throws DAOException
     */
    public void connectDataSource() throws DAOException {
        //System.out.println("\\033[32mjdbc:mysql://"+mysql_server+"/" + dataSource +" "+ username +" "+ password);
        /*if (conNet != null) {
         return;
         }*/

        while (conNet == null) {
            try {
                conNet = DriverManager.getConnection("jdbc:mysql://" + mysql_server + "/" + dataSource, username, password);  

            } catch (SQLException ex) {

                System.out.println("Error al conectar " + ex.getMessage());
            }
        }
    }

    /**
     * This method closes the open connection with the database used to store
     * the network context profile.
     *
     * @throws DAOException
     */
    public void disconnectDataSource() throws DAOException {
        try {
            conNet.close();
            conNet = null;
        } catch (SQLException sqlE) {
            throw new DAOException(sqlE);
        }
    }

    /**
     * This method gets the parameters of the network traffic for a specific
     * subnetwork and a specific period of time.
     *
     * @param d. An array of Date which contains the start and the end of the
     * period.
     * @param subName. The name of the subnetwork.
     * @return Vector<InfoNetworkAnDetector>. The vector of
     * InfoNetworkAnDetector.
     * @throws DAOException
     * @throws SQLException
     */
    public Vector<InfoNetworkAnDetector> obtainNormalState(Date[] d, String subName) throws SQLException, DAOException {
        synchronized (lock) {
            //connectDataSource();
            Calendar d1 = new GregorianCalendar();
            Calendar d2 = new GregorianCalendar();
            d1.setTime(d[0]);
            d2.setTime(d[1]);
            int day1 = d1.get(Calendar.DAY_OF_WEEK);
            int hour = d1.get(Calendar.HOUR_OF_DAY);
            int minutes = d1.get(Calendar.MINUTE);
            long time1 = hour * 60 + minutes;
            hour = d2.get(Calendar.HOUR_OF_DAY);
            minutes = d2.get(Calendar.MINUTE);
            long time2 = hour * 60 + minutes;
            int day2 = d2.get(Calendar.DAY_OF_WEEK);
            if (day1 != day2) {
                time2 = 1;
            }
            //String select = "SELECT C.`date`, C.`day_week`, C.`time`, C.`duration`, C.`eth_proto`, C.`ip_proto`, C.`portOr`, C.`portDes`, C.`ipOr`, C.`ipDes`, C.`src_pkts`, C.`dst_pkts`, C.`src_bytes`, C.`dst_bytes`, C.`total_bytes`  FROM `" + network_context_profile_table + "` C  WHERE day_week=" + day1 + " AND time >= " + time1 + " AND time <= " + time2;
            String select = "SELECT C.`date`, C.`day_week`, C.`time`, C.`duration`, C.`eth_proto`, C.`ip_proto`, C.`portOr`, C.`portDes`, C.`ipOr`, C.`ipDes`, C.`src_pkts`, C.`dst_pkts`, C.`src_bytes`, C.`dst_bytes`, C.`total_bytes`  FROM `" + network_context_profile_table + "` C  WHERE day_week= 6 AND time >= 404 AND time <= 1080";
            Vector<InfoNetworkAnDetector> i = new Vector();
            Date dates = null;
            try {
                if (conNet == null) {
                    connectDataSource();
                }
                Statement stmt = null;
                while (stmt == null) {
                    stmt = conNet.createStatement();
                }
                ResultSet rs = null;
                while (rs == null) {
                    rs = stmt.executeQuery(select);
                }
                String codigo = null;
                while (rs.next()) {

                    try {
                        dates = DateFormatter.stringToDate(rs.getString("date"), "yyyy-MM-dd HH:mm:ss");
                    } catch (ParseException ex) {
                        Logger.getLogger(BDManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //Date date, int duration, int eth_proto, int ip_proto, int portSrc, int portDes, int src_pkts, int dst_pkts, int src_bytes, int dst_bytes, int total_bytes, String ipSrc, String ipDes
                    i.add(new InfoNetworkAnDetector(dates, rs.getInt("duration"), rs.getInt("eth_proto"), rs.getInt("ip_proto"), rs.getInt("portOr"), rs.getInt("portDes"), rs.getInt("src_pkts"), rs.getInt("dst_pkts"), rs.getInt("src_bytes"), rs.getInt("dst_bytes"), rs.getInt("total_bytes"), rs.getString("ipOr"), rs.getString("ipDes"), subName));
                }
                rs.close();
                stmt.close();
                disconnectDataSource();

            } catch (SQLException e) {
                disconnectDataSource();
                throw new DAOException(e);
            }
            return i;
        }
    }

    /**
     * This method gets a list of IP Addresses of a specific subnetwork.
     *
     * @param iface. The name of the subnetwork.
     * @return List<String>. The IPAddresses list.
     * @throws DAOException
     * @throws SQLException
     */
    public List<String> obtainSubNetworkInfo(String iface) throws SQLException, DAOException {
        synchronized (lock) {
            String interfaceName = iface;
            List<String> ipAddressList = new ArrayList();
            connectDataSource();
            String select = "SELECT ipAddress FROM " + network_assets_table + " WHERE location='" + interfaceName + "'";
            try {
                while (conNet == null) {
                    connectDataSource();
                }
                Statement stmt = conNet.createStatement();
                ResultSet rs = stmt.executeQuery(select);
                while (rs.next()) {
                    String ipaddress = rs.getString("ipAddress");
                    ipAddressList.add(ipaddress);
                }
                rs.close();
                stmt.close();
                disconnectDataSource();
            } catch (SQLException e) {
                disconnectDataSource();
                throw new DAOException(e);
            }
            return ipAddressList;
        }
    }
}
