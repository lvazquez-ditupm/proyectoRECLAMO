/**
 * "InfoNetworkAnDetector" Java class is free software: you can redistribute
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

package networkContext.anomalyDetector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import networkContext.anomalyDetector.CorrelatorEngine.CorrelationManager;
import networkContext.anomalyDetector.DAO.BDManagerIF;
import networkContext.anomalyDetector.DAO.DAOException;
import networkContext.anomalyDetector.DAO.DataManagerFactory;
import networkContext.anomalyDetector.util.NetworkAddress;

/**
 * This class represents the information of the instant network traffic of a 
 * specific subnetwork.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class InfoNetworkAnDetector {
        private Date date;
        private int duration, eth_proto, ip_proto, portSrc, portDes, src_pkts, dst_pkts, src_bytes, dst_bytes, total_bytes;
        private String ipSrc, ipDes, subnetworkName;
        
    public InfoNetworkAnDetector(Date date, int duration, int eth_proto, int ip_proto, int portSrc, int portDes, int src_pkts, int dst_pkts, int src_bytes, int dst_bytes, int total_bytes, String ipSrc, String ipDes, String subName) {
        this.date = date;
        this.duration = duration;
        this.eth_proto = eth_proto;
        this.ip_proto = ip_proto;
        this.portSrc = portSrc;
        this.portDes = portDes;
        this.src_pkts = src_pkts;
        this.dst_pkts = dst_pkts;
        this.src_bytes = src_bytes;
        this.dst_bytes = dst_bytes;
        this.total_bytes = total_bytes;
        this.ipSrc = ipSrc;
        this.ipDes = ipDes;
        this.subnetworkName = subName;
    }
    
    public void printData(){
        System.out.println("***InfoNetworkContext***");
        System.out.println("Subred monitorizada: "+subnetworkName);
        System.out.println("Fecha: "+date.toString());
        System.out.println("Duracion: "+duration);
        System.out.println("eth_proto: "+eth_proto);
        System.out.println("ip_proto: "+ip_proto);
        System.out.println("Puerto Origen: "+portSrc);
        System.out.println("IP origen: "+ipSrc);
        System.out.println("Puerto Destino: "+portDes);
        System.out.println("IP destino: "+ipDes);
        System.out.println("Paquetes de origen: "+src_pkts);
        System.out.println("Paquetes de destino: "+dst_pkts);
        System.out.println("Bytes de origen: "+src_bytes);
        System.out.println("Bytes de destino: "+dst_bytes);
        System.out.println("Bytes totales: "+total_bytes);
    }
    
    public int compare(InfoNetworkAnDetector i){
        int matches=0;
        if(ipSrc.equals(i.getIpSrc())) matches=matches+4;
        if(ipDes.equals(i.getIpDes())) matches=matches+5;
        if(portSrc==i.getPortSrc()) matches=matches++;
        if(portDes==i.getPortDes()) matches=matches+2;
        if(ip_proto==i.getIp_proto()) matches=matches+2;
        if(eth_proto==i.getEth_proto()) matches ++;
        return matches;
    }
    
    public boolean checkInternalCon(){
        BDManagerIF bd=DataManagerFactory.getInstance().createDataManager();
        List<String> iplist = null;
        String netIPmask = null;
        try {
            iplist = bd.obtainSubNetworkInfo(subnetworkName);
            netIPmask=iplist.get(0)+"/24";
        } catch (SQLException ex) {
            Logger.getLogger(InfoNetworkAnDetector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DAOException ex) {
            Logger.getLogger(InfoNetworkAnDetector.class.getName()).log(Level.SEVERE, null, ex);
        }

        Vector<InfoNetworkAnDetector> internals=new Vector<InfoNetworkAnDetector>();
        NetworkAddress net=null;
        try {
                net = new NetworkAddress(netIPmask);

                if(net.isSameNetwork(InetAddress.getByName(ipSrc)) && net.isSameNetwork(InetAddress.getByName(ipDes))) return true;
                else return false;
        } catch (UnknownHostException ex) {
                Logger.getLogger(InfoNetworkAnDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public int obtainDateNumber(){
       Calendar calendario = new GregorianCalendar();
       calendario.setTime(date);
        int hour = calendario.get(Calendar.HOUR_OF_DAY);
        int minutes = calendario.get(Calendar.MINUTE);
        int time = hour * 60 + minutes;
        return time;
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDst_bytes() {
        return dst_bytes;
    }

    public void setDst_bytes(int dst_bytes) {
        this.dst_bytes = dst_bytes;
    }

    public int getDst_pkts() {
        return dst_pkts;
    }

    public void setDst_pkts(int dst_pkts) {
        this.dst_pkts = dst_pkts;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getEth_proto() {
        return eth_proto;
    }

    public void setEth_proto(int eth_proto) {
        this.eth_proto = eth_proto;
    }

    public String getIpDes() {
        return ipDes;
    }

    public void setIpDes(String ipDes) {
        this.ipDes = ipDes;
    }

    public String getIpSrc() {
        return ipSrc;
    }

    public void setIpSrc(String ipSrc) {
        this.ipSrc = ipSrc;
    }

    public int getIp_proto() {
        return ip_proto;
    }

    public void setIp_proto(int ip_proto) {
        this.ip_proto = ip_proto;
    }

    public int getPortDes() {
        return portDes;
    }

    public void setPortDes(int portDes) {
        this.portDes = portDes;
    }

    public int getPortSrc() {
        return portSrc;
    }

    public void setPortSrc(int portSrc) {
        this.portSrc = portSrc;
    }

    public int getSrc_bytes() {
        return src_bytes;
    }

    public void setSrc_bytes(int src_bytes) {
        this.src_bytes = src_bytes;
    }

    public int getSrc_pkts() {
        return src_pkts;
    }

    public void setSrc_pkts(int src_pkts) {
        this.src_pkts = src_pkts;
    }

    public int getTotal_bytes() {
        return total_bytes;
    }

    public void setTotal_bytes(int total_bytes) {
        this.total_bytes = total_bytes;
    }
    
    public String getSubnetworkName() {
        return subnetworkName;
    }

    public void setSubnetworkName(String subnetworkName) {
        this.subnetworkName = subnetworkName;
    }   
    
}
