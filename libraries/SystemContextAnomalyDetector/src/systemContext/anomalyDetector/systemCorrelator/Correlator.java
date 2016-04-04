/**
 * "Correlator" Java class is free software: you can redistribute
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

package systemContext.anomalyDetector.systemCorrelator;

import java.util.List;
import systemContext.InfoSystem;

/**
 * This class correlates the profile of the system context for a specific host,
 * stored in the DDBB, and the current value of the monitored system parameters.
 * The result of the correlation is an instance of SystemAnomaly which contains
 * information about the anomaly of all the monitored parameters.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class Correlator {
    public SystemAnomaly correlate (List<InfoSystem> s, InfoSystem finalSystem) {
        Profile p=new Statistics().calculateStats(s);
        p.printProfile();
        int e=0;
        if(finalSystem.getEstado())e=1;
        int estadoA=anormalityEstimation(p.getEstadoAv(),p.getEstadoD(),e);
        int usuariosA=anormalityEstimation(p.getUsuariosAv(),p.getUsuariosD(), (double) finalSystem.getUsuarios());
        int procesosA=anormalityEstimation(p.getProcesosAv(),p.getProcesosD(),finalSystem.getProcesos());
        int discoduroA=anormalityEstimation(p.getDiscoduroAv(),p.getDiscoduroD(),finalSystem.getDiscoduro());
        int CPUA=anormalityEstimation(p.getCPUAv(),p.getCPUD(),finalSystem.getCPU());
        int zombiesA=anormalityEstimation(p.getZombiesAv(),p.getZombiesD(),finalSystem.getZombies());
        int latenciaA=anormalityEstimation(p.getLatenciaAv(),p.getLatenciaD(),finalSystem.getLatencia());
        int ssh_failedA = anormalityEstimation(p.getSSHFailedAv(), p.getSSHFailedD(), finalSystem.getSSHFailed());         //public SystemAnomaly(int estadoA, int usuariosA, int procesosA, int discoduroA, int CPUA, int zombiesA, int latenciaA) {
        SystemAnomaly an= new SystemAnomaly(estadoA,usuariosA,procesosA,discoduroA,CPUA,zombiesA,latenciaA,ssh_failedA);
        return an;
    }
    
    private int anormalityEstimation(double m, double dcuad, double x){
        if(dcuad==0) dcuad=0.0001;
        double prob=0;
        int anomaly = -1;
        prob=Phi(x,m,dcuad);

        if (prob <= 0 ){
            anomaly = 0;
        }
        else if (prob > 100.0) {
            anomaly = 10;
        }
        else{
            anomaly = (int) Math.round(prob/10.0);
        }
        
        System.out.println("probabilidad "+prob+" comparando (media, desv, x)"+m+" "+dcuad+" "+x);
        
        return anomaly;
    }
    
    public double Phi (double z, double mu, double sigma){
        double desviation =0;
        if (z == mu) desviation = 0;
        else if (z > mu){
            desviation = ((z - (mu+sigma))/(mu+sigma))*100;
        }else if(z < mu){
            desviation = (((mu-sigma)-z)/(mu-sigma))*100;
        }
        return desviation;
    }
    
    
/*    public double Phi(double z, double mu, double sigma) {
        return Phi((z - mu) / sigma);
    }
    public double Phi(double z) {
        return 0.5 * (1.0 + erf(z / (Math.sqrt(2.0))));
    }
    public double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                                            t * ( 1.00002368 +
                                            t * ( 0.37409196 +
                                            t * ( 0.09678418 +
                                            t * (-0.18628806 +
                                            t * ( 0.27886807 +
                                            t * (-1.13520398 +
                                            t * ( 1.48851587 +
                                            t * (-0.82215223 +
                                            t * ( 0.17087277))))))))));
        if (z >= 0) return  ans;
        else        return -ans;
    }*/

}