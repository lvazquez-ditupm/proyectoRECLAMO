/**
 * "InfoSystem" Java class is free software: you can redistribute
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

package systemContext.learning;

/**
 * This class represents the information of the instant system context of a 
 * specific host.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class InfoSystem {
    private String nombre=null;
    private boolean estado;
    private int usuarios;
    private int procesos;
    private int discoduro;
    private float CPU;
    private int zombies;
    private float latencia;
    private int ssh_failed;

    public InfoSystem(String nombre, boolean estado, int usuarios, int procesos, int discoduro, float CPU, int zombies, float latencia, int ssh_failed) {
        this.nombre=nombre;
        this.estado = estado;
        this.usuarios = usuarios;
        this.procesos = procesos;
        this.discoduro = discoduro;
        this.CPU = CPU;
        this.zombies = zombies;
        this.latencia=latencia;
        this.ssh_failed = ssh_failed;
    }
    /*
    public void actualizarEstadistico(InfoSystem detectado){
        this.estado = detectado.getEstado();
        this.usuarios = detectado.getUsuarios();
        this.procesos = detectado.getProcesos();
        this.discoduro = detectado.getDiscoduro();
        this.CPU = detectado.getCPU();
        this.zombies = detectado.getZombies();
        this.latencia=detectado.getLatencia();
        usuariosEstad=(float) ((float) (usuariosEstad * 0.9) + (float) (detectado.getUsuarios()) * 0.1);
        procesosEstad=(float) ((float) (procesosEstad * 0.9) + (float) (detectado.getProcesos()) * 0.1);
        discoduroEstad=(float) ((float) (discoduroEstad * 0.9) + (float) (detectado.getDiscoduro()) * 0.1);
        CPUEstad=(float) ((float) (CPUEstad * 0.9) + (float) (detectado.getCPU()) * 0.1);
        zombiesEstad=(float) ((float) (zombiesEstad * 0.9) + (float) (detectado.getZombies()) * 0.1);
        latenciaEstad=(float) ((float) (latenciaEstad * 0.9) + (float) (detectado.getZombies()) * 0.1);
    }
*/
    /*
    public SystemAnomaly obtenerAnomalia(){
        System.out.println("Usuarios:"+usuarios+"  "+usuariosEstad);
        int usuariosA=Math.abs((int) ((usuarios-usuariosEstad)*4));
        if(usuariosA>10)    usuariosA=10;
        System.out.println("Procesos:"+procesos+"  "+procesosEstad);
        int procesosA=Math.abs((int) ((procesos-procesosEstad)*0.8));
        if(procesosA>10)    procesosA=10;
        System.out.println("discoduro:"+discoduro+"  "+discoduroEstad);
        int discoduroA=Math.abs((int) ((discoduro-discoduroEstad)*0.1));
        if(discoduroA>10)    discoduroA=10;
        System.out.println("CPU:"+CPU+"  "+CPUEstad);
        int CPUA=Math.abs((int) ((CPU-CPUEstad)*11));
        if(CPUA>10)    CPUA=10;
        System.out.println("zombies:"+zombies+"  "+zombiesEstad);
        int zombiesA=Math.abs((int) ((zombies-zombiesEstad)*4));
        if(zombiesA>10)    zombiesA=10;
        System.out.println("latencia:"+latencia+"  "+latenciaEstad);
        int latenciaA=Math.abs((int) ((latencia-latenciaEstad)*3));
        if(latenciaA>10)    latenciaA=10;
        return new SystemAnomaly(estado,usuariosA,procesosA,discoduroA,CPUA,zombiesA,latenciaA);
    }
*/
    public InfoSystem(String nombre){
        this.nombre=nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getCPU() {
        return CPU;
    }

    public void setCPU(float CPU) {
        this.CPU = CPU;

    }

    public int getDiscoduro() {
        return discoduro;
    }

    public void setDiscoduro(int discoduro) {
        this.discoduro = discoduro;

    }

    public boolean getEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public int getProcesos() {
        return procesos;
    }

    public void setProcesos(int procesos) {
        this.procesos = procesos;

    }

    public int getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(int usuarios) {
        this.usuarios = usuarios;
    }

    public int getZombies() {
        return zombies;
    }

    public void setZombies(int zombies) {
        this.zombies = zombies;
    }

    public float getLatencia() {
        return latencia;
    }

    public void setLatencia(float latencia) {
        this.latencia = latencia;
    }
    
    public int getSSHFailed() {
        return ssh_failed;
    }

    public void setSSHFailed(int ssh_failed) {
        this.ssh_failed = ssh_failed;
    }
    
    public void printInfoSystem(){
        System.out.println("-----------------------------NUEVO PERFIL GENERADO: "+this.getNombre()+"---------------------------------");
        System.out.println("          Estado: "+this.getEstado());
        System.out.println("          Latencia: "+this.getLatencia());
        System.out.println("          CPU: "+this.getCPU());
        System.out.println("          Disco duro: "+this.getDiscoduro());
        System.out.println("          Usuarios: "+this.getUsuarios());
        System.out.println("          Procesos: "+this.getProcesos());
        System.out.println("          Zombies: "+this.getZombies());
        System.out.println("          SSH Failed: "+this.getSSHFailed());        
    }
}