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

package systemContext.learning.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import systemContext.learning.InfoSystem;
import systemContext.learning.util.PropsUtil;

/**
 * This class manages the data base where the profile of the system context will
 * be stored. This class establishes a connection with the database and inserts
 * the information stored during the learning phase of the System Context Module.
 * It implements the BDManagerIF interface.
 * 
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class BDManager implements BDManagerIF {
    private static Connection conSys;
    private PropsUtil props = new PropsUtil();
    private final String dataSource = props.getOntairsDatabaseNameValue();
    private final String username = props.getMysqlConnectionUsernameValue();
    private final String password = props.getMysqlConnectionPasswordValue();
    private final String mysql_server = props.getMysqlServerNameValue();
    private final String system_context_profile_table = props.getSystemContextProfileTableNameValue();
    private final String network_assets_table = props.getNetworkAssetsTableNameValue();

    public void connectDataSource() throws DAOException {
        if (conSys != null) {
            disconnectDataSource();
        }
        try {          
            conSys=DriverManager.getConnection("jdbc:mysql://"+mysql_server+":3306/" + dataSource, username, password);
        } catch (SQLException ex) {
            Logger.getLogger(BDManager.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    public void disconnectDataSource() throws DAOException {
        try {
            conSys.close();
            conSys = null;
        } catch (SQLException sqlE) {
            throw new DAOException(sqlE);
        }
    }
    
    public void insertInfoSystem(InfoSystem infoSystem) throws SQLException, DAOException {
        connectDataSource();
        Calendar calendario = new GregorianCalendar();
        int hora =calendario.get(Calendar.HOUR_OF_DAY);
        int minutos = calendario.get(Calendar.MINUTE);
        long time=hora*60+minutos;
        int day=calendario.get(Calendar.DAY_OF_WEEK);
        String e="";
        if(infoSystem.getEstado()) e="working"; else e="down";
        String insert = "insert into "+system_context_profile_table+" (day_week, time, name, state, users,  CPU, processes, free_space, latency, zombies, ssh_failed) VALUES (" + day + "," +time + ", '"+ infoSystem.getNombre() +"','"+e+"',"+infoSystem.getUsuarios()+","+infoSystem.getCPU()+","+infoSystem.getProcesos()+","+infoSystem.getDiscoduro()+","+infoSystem.getLatencia()+","+infoSystem.getZombies()+","+infoSystem.getSSHFailed()+");";
                //insert into STATUS (day_week, time, IP, state, users,  CPU, processes, free_space, latency, zombies) VALUES (2,40000000,'127.0.0.1','working', 3, 0.03, 112, 6003, 0.64,2);
        //System.out.println(insert);
        try {
            Statement stmt = conSys.createStatement();
            stmt.executeUpdate(insert);
            stmt.close();
        } catch (SQLException sqlE) {
            throw new DAOException(sqlE);
        }

    }
    
    public List<String> obtainNetworkSystems() throws SQLException, DAOException {
        List<String> nombresistemas = new ArrayList();
        connectDataSource();
        Calendar calendario = new GregorianCalendar();
        int hora =calendario.get(Calendar.HOUR_OF_DAY);
        int minutos = calendario.get(Calendar.MINUTE);
        long t=hora*60+minutos;
        int day=calendario.get(Calendar.DAY_OF_WEEK);
        long t1=t+30;
        long t2=t-30;
        //String select = "SELECT S.`id`, S.`day_week`, S.`time`, S.`name`, S.`state`, S.`users`, S.`CPU`, S.`processes`, S.`free_space`, S.`latency`, S.`zombies` FROM `STATUS` S";
        //String select = "SELECT S.`name`, S.`ipAddress` FROM `"+network_assets_table+"'"; //+ la IP del sistema atacado
        String select = "SELECT name, ipAddress FROM "+network_assets_table;
        //SELECT S.`id`, S.`day_week`, S.`time`, S.`name`, S.`state`, S.`users`, S.`CPU`, S.`processes`, S.`free_space`, S.`latency`, S.`zombies` FROM `STATUS` S
       // System.out.println(select);
        try {
            Statement stmt = conSys.createStatement();
            ResultSet rs = stmt.executeQuery(select);
            while (rs.next()) {
                String nombreHost = rs.getString("name");
                nombresistemas.add(nombreHost);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            throw new DAOException(e);
        }

        return nombresistemas;

    } 

}