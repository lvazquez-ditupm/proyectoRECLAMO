/**
 * "LoadStatistics" Java class is free software: you can redistribute
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

import java.util.Vector;

/**
 * This class calculates the level of anomaly of each of the network parameters 
 * analyzed, with respect to a distribution of the normal traffic profile.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class LoadStatistics {

    public int anormalityResult(Vector a, int x){
        return this.anormalityEstimation(this.mean(a), this.deviation(a), x);
    }
    
    public int anormalityResult(Vector a, double x){
        return this.anormalityEstimation(this.mean(a), this.deviation(a), x);
    }
    
    public double mean(Vector<Integer> a){
   	    double suma=0;
	    for(int i=0; i<a.size(); i++){
                suma+=a.elementAt(i);
        }
	return suma/a.size();
    }
     
    public double deviation(Vector<Integer> a){
  	    double suma=0;
	    double media=mean(a);
	     for(int i=0; i<a.size(); i++){
		    suma+=(a.elementAt(i)-media)*(a.elementAt(i)-media);
        }
	    return Math.sqrt(suma/a.size());
    }
    
    private int anormalityEstimation(double m, double dcuad, double x){
        if(dcuad==0) dcuad=0.0001;
        double prob=0;
        prob=Phi(x,m,dcuad);
        if(x<=m){
            prob=0.5-prob;
        }
        else{
            prob=prob-0.5;
        }
        System.out.println("probabilidad "+prob+" comparando (media, desv, x)"+m+" "+dcuad+" "+x);
        prob=prob*20;
        return (int) Math.round(prob);
    }
    
    private double Phi(double z, double mu, double sigma) {
        return Phi((z - mu) / sigma);
    }
    
    private double Phi(double z) {
        return 0.5 * (1.0 + erf(z / (Math.sqrt(2.0))));
    }
    
    private double erf(double z) {
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
    }
}