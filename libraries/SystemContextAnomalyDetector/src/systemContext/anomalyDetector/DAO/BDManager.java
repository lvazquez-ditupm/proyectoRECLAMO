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
package systemContext.anomalyDetector.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import systemContext.InfoSystem;
import systemContext.anomalyDetector.util.PropsUtil;

/**
 * This class manages the data base where the profile of the system context is
 * stored. This class establishes a connection with the database and obtaines
 * the information stored during the learning phase of the System Context
 * Module. It implements the BDManagerIF interface.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class BDManager implements BDManagerIF {

    private Connection conSys;
    private PropsUtil props = new PropsUtil();
    private final String dataSource = props.getOntairsDatabaseNameValue();
    private final String username = props.getMysqlConnectionUsernameValue();
    private final String password = props.getMysqlConnectionPasswordValue();
    private final String mysql_server = props.getMysqlServerNameValue();
    private final String system_context_profile_table = props.getSystemContextProfileTableNameValue();
    private final Object lock = new Object();

    /**
     * This method establishes a connection with the database used to store the
     * system context profile.
     *
     * @throws DAOException
     */
    public void connectDataSource() throws DAOException {
        /*if (conSys != null) {
         return;
         }*/

        while (conSys == null) {
            try {
                conSys = DriverManager.getConnection("jdbc:mysql://" + mysql_server + "/" + dataSource, username, password);
            } catch (SQLException ex) {
                System.out.println("Error al conectar " + ex.getMessage());
            }
        }
    }

    /**
     * This method closes the open connection with the database used to store
     * the system context profile.
     *
     * @throws DAOException
     */
    public void disconnectDataSource() throws DAOException {
        try {
            conSys.close();
            conSys = null;
        } catch (SQLException sqlE) {
            throw new DAOException(sqlE);
        }
    }

    /**
     * This method gets the monitored context variables for a specific system or
     * host and a specific period of time.
     *
     * @param name. The name of the system or host.
     * @return List<InfoSystem>. The list of InfoSystem in the specified period
     * of time.
     * @throws DAOException
     * @throws SQLException
     */
    public List<InfoSystem> obtainRelevantInfo(String name) throws SQLException, DAOException {
        synchronized (lock) {
            // connectDataSource();
            List<InfoSystem> infoSystems = new ArrayList();

            Calendar calendario = new GregorianCalendar();
            int hora = calendario.get(Calendar.HOUR_OF_DAY);
            int minutos = calendario.get(Calendar.MINUTE);
            long t = hora * 60 + minutos;
            int day = calendario.get(Calendar.DAY_OF_WEEK);
            long t1 = t + 30;
            long t2 = t - 30;
            //String select = "SELECT S.`id`, S.`day_week`, S.`time`, S.`name`, S.`state`, S.`users`, S.`CPU`, S.`processes`, S.`free_space`, S.`latency`, S.`zombies` FROM `STATUS` S";
            String select = "SELECT S.`id`, S.`day_week`, S.`time`, S.`name`, S.`state`, S.`users`, S.`CPU`, S.`processes`, S.`free_space`, S.`latency`, S.`zombies`, S.`ssh_failed` FROM `" + system_context_profile_table + "` S WHERE day_week = " + day + " AND time < " + t1 + " AND time > " + t2 + " AND name ='" + name + "'"; //+ la IP del sistema atacado
            //SELECT S.`id`, S.`day_week`, S.`time`, S.`name`, S.`state`, S.`users`, S.`CPU`, S.`processes`, S.`free_space`, S.`latency`, S.`zombies` FROM `STATUS` S
            // System.out.println(select);
            try {
                if (conSys == null) {
                    connectDataSource();
                }
                Statement stmt = null;
                while (stmt == null) {
                    stmt = conSys.createStatement();
                }
                ResultSet rs = null;
                while (rs == null) {
                    rs = stmt.executeQuery(select);
                }
                String codigo = null;
                while (rs.next()) {
                    boolean st = true;
                    if (rs.getString("state").equals("working")) {
                        st = true;
                    } else {
                        st = false;
                    }
                    infoSystems.add(new InfoSystem(rs.getString("name"), st, rs.getInt("users"), rs.getInt("processes"), rs.getInt("free_space"), rs.getFloat("CPU"), rs.getInt("zombies"), rs.getFloat("latency"), rs.getInt("ssh_failed")));
                }
                rs.close();
                stmt.close();
                disconnectDataSource();

            } catch (SQLException e) {
                disconnectDataSource();
                throw new DAOException(e);
            }

            return infoSystems;

        }
    }
}
