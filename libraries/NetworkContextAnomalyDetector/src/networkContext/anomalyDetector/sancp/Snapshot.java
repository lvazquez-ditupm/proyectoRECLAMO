/**
 * "Snapshot" Java class is free software: you can redistribute
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

package networkContext.anomalyDetector.sancp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import networkContext.anomalyDetector.CorrelatorEngine.CorrelationManager;
import networkContext.anomalyDetector.DAO.BDManagerIF;
import networkContext.anomalyDetector.DAO.DAOException;
import networkContext.anomalyDetector.DAO.DataManagerFactory;
import networkContext.anomalyDetector.InfoNetworkAnDetector;
import networkContext.anomalyDetector.util.NetworkAddress;

/**
 * This class represents the information about the established network connections
 * in a period of time.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class Snapshot {
    private Vector<InfoNetworkAnDetector> n;
    private String interfaceName;
    private String netIPmask;

    public Snapshot(Vector<InfoNetworkAnDetector> n,String iName) {
        this.n = n;
        this.interfaceName = iName;
    }
    
    public Date[] obtainTimeLimits(){
        Date[] d=new Date[2];
        d[0]=obtainEarliest();
        d[1]=obtainLatest();
        return d;
    }
    
    public Vector<InfoNetworkAnDetector> getInternalCon(){        
        BDManagerIF bd=DataManagerFactory.getInstance().createDataManager();
        List<String> iplist = null;
        try {
            iplist = bd.obtainSubNetworkInfo(interfaceName);
            netIPmask=iplist.get(0)+"/24";
        } catch (SQLException ex) {
            Logger.getLogger(Snapshot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DAOException ex) {
            Logger.getLogger(Snapshot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Vector<InfoNetworkAnDetector> internals=new Vector<InfoNetworkAnDetector>();
        NetworkAddress net=null;
        try {
                net = new NetworkAddress(netIPmask);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Snapshot.class.getName()).log(Level.SEVERE, null, ex);
            }
        for(int i=0;i<n.size();i++){
            try {
                if(net.isSameNetwork(InetAddress.getByName(n.elementAt(i).getIpSrc())) && net.isSameNetwork(InetAddress.getByName(n.elementAt(i).getIpDes()))) internals.add(n.elementAt(i));
            } catch (UnknownHostException ex) {
                Logger.getLogger(Snapshot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return internals;
    }
    
    private Date obtainEarliest(){
        Date d=n.elementAt(0).getDate();

        //System.out.println("obtain earliest: ");
        for(int i=0;i<n.size();i++){
          //  System.out.println("Date :"+i+" "+n.elementAt(i).getDate().toString());
            if(n.elementAt(i).getDate().before(d)) d=n.elementAt(i).getDate();
        }
        //System.out.println("Selected: "+d);
        return d;
    }
    
    private Date obtainLatest(){
        Date d=new Date(0,0,0);
        for(int i=0;i<n.size();i++){
            if(n.elementAt(i).getDate().after(d)) d=n.elementAt(i).getDate();
        }
        return d;

    }
    
    public Vector<InfoNetworkAnDetector> getN() {
        return n;
    }

    public void setN(Vector<InfoNetworkAnDetector> n) {
        this.n = n;
    }

    public double averageDataRate() {
        int total=0;
        for(int i=0;i<n.size();i++){
            total+=n.elementAt(i).getTotal_bytes();
        }
        Date early = this.obtainEarliest();
        Date late = this.obtainLatest();
        double msec = late.getTime() - early.getTime();
        double mins = msec / (1000 * 60);
        if (mins == 0.0) {
            mins = 0.016666667;
        }
        return total / mins;
    }
}
