/**
 * "SystemAnomaly" Java class is free software: you can redistribute
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

/**
 * This class represents the anomaly of the system context of a specific host.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class SystemAnomaly {
    /* El nivel de anomalía se medirá de 0 a 10 en función a los estadísticos y a los valores instantáneos,
     siendo 10 la máxima anomalía*/
    private int estadoA;
    private int usuariosA;
    private int procesosA;
    private int discoduroA;
    private int CPUA;
    private int zombiesA;
    private int latenciaA;
    private int ssh_failedA;

    public SystemAnomaly(int estadoA, int usuariosA, int procesosA, int discoduroA, int CPUA, int zombiesA, int latenciaA, int ssh_failedA) {
        this.estadoA = estadoA;
        this.usuariosA = usuariosA;
        this.procesosA = procesosA;
        this.discoduroA = discoduroA;
        this.CPUA = CPUA;
        this.zombiesA = zombiesA;
        this.latenciaA = latenciaA;
        this.ssh_failedA = ssh_failedA;
    }

    SystemAnomaly() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int getCPUA() {
        return CPUA;
    }

    public void setCPUA(int CPUA) {
        this.CPUA = CPUA;
    }

    public int getDiscoduroA() {
        return discoduroA;
    }

    public void setDiscoduroA(int discoduroA) {
        this.discoduroA = discoduroA;
    }

    public int isEstadoA() {
        return estadoA;
    }

    public void setEstadoA(int estadoA) {
        this.estadoA = estadoA;
    }

    public int getLatenciaA() {
        return latenciaA;
    }

    public void setLatenciaA(int latenciaA) {
        this.latenciaA = latenciaA;
    }

    public int getProcesosA() {
        return procesosA;
    }

    public void setProcesosA(int procesosA) {
        this.procesosA = procesosA;
    }

    public int getUsuariosA() {
        return usuariosA;
    }

    public void setUsuariosA(int usuariosA) {
        this.usuariosA = usuariosA;
    }

    public int getZombiesA() {
        return zombiesA;
    }

    public void setZombiesA(int zombiesA) {
        this.zombiesA = zombiesA;
    }
    
    public int getSSHFailedA() {
        return ssh_failedA;
    }

    public void setSSHFailedA(int ssh_failedA) {
        this.ssh_failedA = ssh_failedA;
    }

    public void printAnomaly() {
        System.out.println("*** Resultado Anomalia: ***");
        System.out.println("estado: "+estadoA);
        System.out.println("usuarios: "+usuariosA);
        System.out.println("procesos: "+procesosA);
        System.out.println("discoduro: "+discoduroA);
        System.out.println("CPU: "+CPUA);
        System.out.println("zombies: "+zombiesA);
        System.out.println("latencia: "+latenciaA);
        System.out.println("SSH Failed: "+ssh_failedA);
       
    }
}