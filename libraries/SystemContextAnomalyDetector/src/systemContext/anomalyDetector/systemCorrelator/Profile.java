/**
 * "Profile" Java class is free software: you can redistribute
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
 * This class represents the profile of the system context in normal situation.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class Profile {
        private double estadoAv;
        private double usuariosAv;
        private double procesosAv;
        private double discoduroAv;
        private double CPUAv;
        private double zombiesAv;
        private double latenciaAv;
        private double ssh_failedAv;
        private double estadoD;
        private double usuariosD;
        private double procesosD;
        private double discoduroD;
        private double CPUD;
        private double zombiesD;
        private double latenciaD;
        private double ssh_failedD;

    public Profile(double estadoAv, double usuariosAv, double procesosAv, double discoduroAv, double CPUAv, double zombiesAv, double latenciaAv, double ssh_failedAv, double estadoD, double usuariosD, double procesosD, double discoduroD, double CPUD, double zombiesD, double latenciaD, double ssh_failedD) {
        this.estadoAv = estadoAv;
        this.usuariosAv = usuariosAv;
        this.procesosAv = procesosAv;
        this.discoduroAv = discoduroAv;
        this.CPUAv = CPUAv;
        this.zombiesAv = zombiesAv;
        this.latenciaAv = latenciaAv;
        this.ssh_failedAv = ssh_failedAv;
        this.estadoD = estadoD;
        this.usuariosD = usuariosD;
        this.procesosD = procesosD;
        this.discoduroD = discoduroD;
        this.CPUD = CPUD;
        this.zombiesD = zombiesD;
        this.latenciaD = latenciaD;
        this.ssh_failedD = ssh_failedD;
    }
    
    public void printProfile(){
        System.out.println("***Perfil***");
        System.out.println("medias: estado "+estadoAv+", usuarios "+usuariosAv+", proc "+procesosAv+", disco "+discoduroAv+", CPU "+CPUAv+", zombies "+zombiesAv+", latencia "+latenciaAv+" , ssh_failed "+ssh_failedAv);
        System.out.println("desviaciones: estado "+estadoD+", usuarios "+usuariosD+", proc "+procesosD+", disco "+discoduroD+", CPU "+CPUD+", zombies "+zombiesD+", latencia "+latenciaD+" , ssh_failed "+ssh_failedD);

    }

    public double getCPUAv() {
        return CPUAv;
    }

    public void setCPUAv(double CPUAv) {
        this.CPUAv = CPUAv;
    }

    public double getCPUD() {
        return CPUD;
    }

    public void setCPUD(double CPUD) {
        this.CPUD = CPUD;
    }

    public double getDiscoduroAv() {
        return discoduroAv;
    }

    public void setDiscoduroAv(double discoduroAv) {
        this.discoduroAv = discoduroAv;
    }

    public double getDiscoduroD() {
        return discoduroD;
    }

    public void setDiscoduroD(double discoduroD) {
        this.discoduroD = discoduroD;
    }

    public double getEstadoAv() {
        return estadoAv;
    }

    public void setEstadoAv(double estadoAv) {
        this.estadoAv = estadoAv;
    }

    public double getEstadoD() {
        return estadoD;
    }

    public void setEstadoD(double estadoD) {
        this.estadoD = estadoD;
    }

    public double getLatenciaAv() {
        return latenciaAv;
    }

    public void setLatenciaAv(double latenciaAv) {
        this.latenciaAv = latenciaAv;
    }

    public double getLatenciaD() {
        return latenciaD;
    }

    public void setLatenciaD(double latenciaD) {
        this.latenciaD = latenciaD;
    }

    public double getProcesosAv() {
        return procesosAv;
    }

    public void setProcesosAv(double procesosAv) {
        this.procesosAv = procesosAv;
    }

    public double getProcesosD() {
        return procesosD;
    }

    public void setProcesosD(double procesosD) {
        this.procesosD = procesosD;
    }

    public double getUsuariosAv() {
        return usuariosAv;
    }

    public void setUsuariosAv(double usuariosAv) {
        this.usuariosAv = usuariosAv;
    }

    public double getUsuariosD() {
        return usuariosD;
    }

    public void setUsuariosD(double usuariosD) {
        this.usuariosD = usuariosD;
    }

    public double getZombiesAv() {
        return zombiesAv;
    }

    public void setZombiesAv(double zombiesAv) {
        this.zombiesAv = zombiesAv;
    }

    public double getZombiesD() {
        return zombiesD;
    }

    public void setZombiesD(double zombiesD) {
        this.zombiesD = zombiesD;
    }
    
    public double getSSHFailedAv() {
        return ssh_failedAv;
    }

    public void setSSHFailedAv(double ssh_failedAv) {
        this.ssh_failedAv = ssh_failedAv;
    }

    public double getSSHFailedD() {
        return ssh_failedD;
    }

    public void setSSHFailedD(double ssh_failedD) {
        this.ssh_failedD = ssh_failedD;
    }

}