/**
 * "Statistics" Java class is free software: you can redistribute
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

import java.util.ArrayList;
import java.util.List;
import systemContext.InfoSystem;

/**
 * This class combines stored data about the system context of a specific host 
 * in the form of Infosystem objects, and generates a profile.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class Statistics {
    
    public Profile calculateStats(List<InfoSystem> s) {
        InfoSystem m=null;
        List<Double> estadoS=new ArrayList();
        List<Double> usuariosS=new ArrayList();
        List<Double> procesosS=new ArrayList();
        List<Double> discoduroS=new ArrayList();
        List<Double> CPUS=new ArrayList();
        List<Double> zombiesS=new ArrayList();
        List<Double> latenciaS=new ArrayList();
        List<Double> ssh_failedS=new ArrayList();
        int num=s.size();
        for(int i=0;i<s.size();i++){
            m=s.get(i);
            if(m.getEstado()) estadoS.add((double) 1);
            usuariosS.add((double)m.getUsuarios());
            procesosS.add((double)m.getProcesos());
            discoduroS.add((double)m.getDiscoduro());
            CPUS.add((double)m.getCPU());
            zombiesS.add((double)m.getZombies());
            latenciaS.add((double)m.getLatencia());
            ssh_failedS.add((double)m.getSSHFailed());
        }
        StatsCalculator estadoEs=new StatsCalculator(ListToArray(estadoS));
        StatsCalculator usuariosEs=new StatsCalculator(ListToArray(usuariosS));
        StatsCalculator procesosEs=new StatsCalculator(ListToArray(procesosS));
        StatsCalculator discoduroEs=new StatsCalculator(ListToArray(discoduroS));
        StatsCalculator CPUEs=new StatsCalculator(ListToArray(CPUS));
        StatsCalculator zombiesEs=new StatsCalculator(ListToArray(zombiesS));
        StatsCalculator latenciaEs=new StatsCalculator(ListToArray(latenciaS));
        StatsCalculator ssh_failedEs=new StatsCalculator(ListToArray(ssh_failedS));
        //public Profile(int estadoAv, int usuariosAv, int procesosAv, int discoduroAv, float CPUAv, int zombiesAv, float latenciaAv, int estadoD, int usuariosD, int procesosD, int discoduroD, float CPUD, int zombiesD, float latenciaD) {

        Profile p=new Profile(estadoEs.valorMedio(),usuariosEs.valorMedio(),procesosEs.valorMedio(),discoduroEs.valorMedio(),CPUEs.valorMedio(), zombiesEs.valorMedio(), latenciaEs.valorMedio(), ssh_failedEs.valorMedio(),estadoEs.desviacionCuadratica(),usuariosEs.desviacionCuadratica(),procesosEs.desviacionCuadratica(), discoduroEs.desviacionCuadratica(),CPUEs.desviacionCuadratica(), zombiesEs.desviacionCuadratica(),latenciaEs.desviacionCuadratica(), ssh_failedEs.desviacionCuadratica());
        return p;
    }
    
    private double[] ListToArray(List<Double> v){
        double[] cadena=new double[v.size()];
         int i =0;
            for(double str : v){
                 cadena[i++] = str;
		  }
            return cadena;
    }

}