/**
 * "CorrelationManager" Java class is free software: you can redistribute
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

package networkContext.anomalyDetector.CorrelatorEngine;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import networkContext.anomalyDetector.DAO.BDManagerIF;
import networkContext.anomalyDetector.DAO.DAOException;
import networkContext.anomalyDetector.DAO.DataManagerFactory;
import networkContext.anomalyDetector.InfoNetworkAnDetector;
import networkContext.anomalyDetector.sancp.Snapshot;

/**
 * This class correlates the profile of the network traffic for a specific subnetwork,
 * stored in the DDBB, and the current snapshot of the network traffic of the same
 * subnetwork. The result of the correlation is the aggregated network anomaly.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class CorrelationManager {
    Profile longProfile;

    public CorrelationManager() {
    }
    
    private void generateLongProfile(Date[] d, String subnetworkName){
        BDManagerIF bd=DataManagerFactory.getInstance().createDataManager();
        Vector<InfoNetworkAnDetector> is = null;
        Calendar cal = new GregorianCalendar();
        cal.setTime(d[0]);
        cal.add(Calendar.MINUTE, -5);
        d[0]=cal.getTime();
        cal.setTime(d[1]);
        cal.add(Calendar.MINUTE, 5);
        d[1]=cal.getTime();
        try {
            is = bd.obtainNormalState(d,subnetworkName);
        } catch (SQLException ex) {
            Logger.getLogger(CorrelationManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DAOException ex) {
            Logger.getLogger(CorrelationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        longProfile=new Profile(is);
    }
    
    public int correlate(Snapshot snap,Date[] d, String subName){
     //   System.out.println("Generar perfil amplio: ");
        this.generateLongProfile(d,subName);
       // System.out.println("Longitud de la snapshot: "+snap.getN().size());
       // System.out.println("Longitud del perfil amplio: "+longProfile.getN().size());
        Vector results=this.checkMatches(snap, longProfile);
        float a=this.matchAnomaly(results);
        int t=this.loadsMatch(snap,longProfile);
        
        
        System.out.println("Anomalía media: " +a);
        System.out.println("Anomalía inst: " +t);
        return Math.round((float)((a / 3 + t) / 3));
    }

    private Vector checkMatches(Snapshot snap, Profile profile) {
        Vector<InfoNetworkAnDetector> isnap=snap.getN();
        Vector<InfoNetworkAnDetector> iprof=profile.getN();
        int match;
        int max=0;
        Vector results=new Vector();
        for (int i=0;i<isnap.size();i++){
            max=0;
            for(int j=0;j<iprof.size();j++){
                match=iprof.elementAt(j).compare(isnap.elementAt(i));
                if(match>max) max=match;
            }
            if(isnap.elementAt(i).checkInternalCon()) results.add(max*2);
            else results.add(max);
        }
        //System.out.println("Confianzas: "+results.toString());
        return results;
    }

    private int loadsMatch(Snapshot snap, Profile profile) {
        int pkta=this.pktCalculation(snap, profile);
        Vector<Integer> times=new Vector();
        for(int i=0;i<profile.getN().size();i++){
            times.add(profile.getN().elementAt(i).obtainDateNumber());
        }
        int minimum = 999999999;
        int maximum = 0;
        int x;
        for (int i = 0; i < times.size(); i++) {
            x = times.elementAt(i);
            if (x < minimum) {
                minimum = x;
            }
            if (x > maximum) {
                maximum = x;
            }
        }
        Vector<Integer> parcialLoad=new Vector();
        Vector<Integer> completeLoad=new Vector();
        LoadStatistics ls=new LoadStatistics();
        for (int iterator=minimum; iterator<maximum ; iterator++){
            for(int j=0; j<profile.getN().size();j++){
                if(profile.getN().elementAt(j).obtainDateNumber()==iterator) parcialLoad.add(profile.getN().elementAt(j).getTotal_bytes());
            }
            completeLoad.add((int) ls.mean(parcialLoad));
            parcialLoad.removeAllElements();
        }
        double s = snap.averageDataRate();
        double aload=ls.anormalityResult(completeLoad, s);
        return pkta + Math.round((float) aload);
    }
    
    private int pktCalculation(Snapshot snap, Profile profile) {
        Vector profile_src=new Vector();
        Vector profile_dst=new Vector();
        for (int i = 0; i < profile.getN().size(); i++) {
            if (profile.getN().elementAt(i).getSrc_bytes() > 0) {
                profile_src.add(profile.getN().elementAt(i).getSrc_bytes() / profile.getN().elementAt(i).getSrc_pkts());
            }
            if (profile.getN().elementAt(i).getDst_bytes() > 0){
                profile_dst.add(profile.getN().elementAt(i).getDst_bytes() / profile.getN().elementAt(i).getDst_pkts());
            }
        }
        LoadStatistics ls = new LoadStatistics();
        Vector probs=new Vector();
        for (int i = 0; i < snap.getN().size(); i++) {
            if (snap.getN().elementAt(i).getSrc_bytes() > 0) {
                probs.add(ls.anormalityResult(profile_src, snap.getN().elementAt(i).getSrc_bytes()/snap.getN().elementAt(i).getSrc_pkts()));
             } else {
                if (snap.getN().elementAt(i).getDst_bytes() > 0) {
                    probs.add(ls.anormalityResult(profile_dst, snap.getN().elementAt(i).getDst_bytes()/snap.getN().elementAt(i).getDst_pkts()));
             }
            }
         }
        System.out.println("La anomalía del tamaño de los paquetes: "+ls.mean(probs));
        return Math.round((float) ls.mean(probs));
    }

    private float averageAnomaly(Vector<Integer> a){
        float total=0;
        for (int i=0;i<a.size();i++){
            total=total+(float)a.elementAt(i);
        }
        float c=total/a.size();
        return c;
    }
    
    private float deviationAnomaly(Vector<Integer> a) {
        float suma = 0;
        float media = averageAnomaly(a);
        for (int i = 0; i < a.size(); i++) {
            suma += (a.elementAt(i) - media) * (a.elementAt(i) - media);
        }
        return (float) Math.sqrt(suma / a.size());
    }

    private float matchAnomaly(Vector results) {
        float a=this.averageAnomaly(results);
        float d=this.deviationAnomaly(results);
        float ma= (float) (30 - a-d*0.3);
        if(ma<0) ma=0;
        return ma;
    }
}
