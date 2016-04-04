/**
 * "Extractor" Java class is free software: you can redistribute
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

package systemContext.anomalyDetector.infoLoader;

import systemContext.*;
import systemContext.anomalyDetector.util.BashRunner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import systemContext.anomalyDetector.util.PropsUtil;

/**
 * This class extracts and processes the NAGIOS log files (Host Performance Data
 * and Service Performance Data) and returns a list of InfoSystem objects.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class Extractor {
    static List<String> nombreSistemas;
    static File f=null;
    static File f2=null;
    static List<InfoSystem> infoSistemas;
    private static PropsUtil props = new PropsUtil();

    public static List<InfoSystem> leerArchivoEstadoSistemas(List<String> nomSistemas ,String rutaFichero, String rutaHost){
	long startTime=System.currentTimeMillis();
        String pathScript= props.getSystemContextNagiosCheckerPathValue();
        infoSistemas=new ArrayList();
        nombreSistemas=nomSistemas;
        for(int i=0; i<nombreSistemas.size();i++){
            infoSistemas.add(new InfoSystem(nombreSistemas.get(i)));
        }

        limpiarArchivo(rutaFichero);
        limpiarArchivo(rutaHost);
        int v;
        for(v=0;v<nombreSistemas.size();v++){
            String Script="bash "+pathScript+" "+nombreSistemas.get(v);
          //  System.out.println("scrip a ejecutar "+Script);
            new BashRunner(Script);
           // System.out.println("***Ejecutando Bash Shell Script para la actualización de medidas***");
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }

        f2 = new File( rutaHost );
        BufferedReader entradaHost=null;
        try {
            entradaHost = new BufferedReader(new FileReader(f2));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (f2.exists()){
            String texto2=null;
            try {

               do {
                    texto2 = entradaHost.readLine();
                    if (texto2!=null){
                        //procesarLinea(texto2);
                        procesarStatus(texto2);
                    }
                } while(texto2!=null);
            } catch (IOException ex) {
                Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        f = new File( rutaFichero );
        BufferedReader entrada=null;
        try {
            entrada = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (f.exists()){
            String texto=null;
            try {

               do {
                    texto = entrada.readLine();
                    if (texto!=null){
                        procesarLinea(texto);
                    }
                } while(texto!=null);
            } catch (IOException ex) {
                Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

	long endTime = System.currentTimeMillis();
	System.out.println("**** END SystemContextAnomalyDetection - Extractor *** Total time : "+(endTime - startTime)+" (ms) ****"); 
        return infoSistemas;
    }

    private static void limpiarArchivo(String rutaFichero){
        FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
            fichero = new FileWriter(rutaFichero);
            pw = new PrintWriter(fichero);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
    }

    private static void procesarLinea(String linea){
    //    System.out.println("****PROCESANDO LINEA****");
    //    System.out.println("linea procesada "+ linea.toString());
        for(int i=0; i<nombreSistemas.size();i++){   


            if (linea.startsWith(nombreSistemas.get(i))){
    //            System.out.print(nombreSistemas.get(i)+" : ");

                try{                
                    //System.out.print(nombreSistema+" : ");
                    StringTokenizer tokensLinea= new StringTokenizer(linea,"\t");
                    String nombre=tokensLinea.nextToken();
                    String atributo=tokensLinea.nextToken();
                    String contenido=null;
                    if(infoSistemas.get(i).getEstado()){
                        //System.out.println("La máquina está activa");
                        String s;
                        int n1;
                        int n2;
                        switch (atributo){
                            case "CPU Load":
                                s=tokensLinea.nextToken();
                                float cpu2;
                                if (s.trim().startsWith("NRPE: unable") || s.trim().startsWith("CHECK_NRPE: Socket")){
                                    cpu2 = Float.parseFloat(null);
                                }
                                else{
                                    n1=s.indexOf(':')+2;
                                    n2=s.indexOf(',');
                                    String cpu=s.substring(n1, n2);
                                    cpu2=Float.parseFloat(cpu);
                                }                            
                                //System.out.println("El valor de uso de CPU de "+infoSystem.getNombre()+" es:"+cpu2);
                                infoSistemas.get(i).setCPU(cpu2);
                                break;
                            case "/dev/sda1 Free Space":
                                s=tokensLinea.nextToken();
                                int hd1;
                                if (s.trim().startsWith("NRPE: unable") || s.trim().startsWith("CHECK_NRPE: Socket")){
                                    hd1 = Integer.parseInt(null);
                                }
                                else{
                                    n1=s.indexOf('/')+1;
                                    n2=s.indexOf('M')+1;
                                    String hd=s.substring(n1, n2);
                                    int n3=hd.indexOf(' ')+1;
                                    int n4=hd.indexOf('M')-1;
                                    String hds=hd.substring(n3, n4);
                                    //System.out.println(hds);
                                    hd1=Integer.parseInt(hds);
                                }                           
                                //System.out.println("El valor de disco libre es:"+hd1);
                                infoSistemas.get(i).setDiscoduro(hd1);
                                break;
                            case "Zombie Processer":
                                s=tokensLinea.nextToken();
                                int z1;
                                if (s.trim().startsWith("NRPE: unable") || s.trim().startsWith("CHECK_NRPE: Socket")){
                                    z1 = Integer.parseInt(null);
                                }
                                else{
                                    n1=s.indexOf(':')+2;
                                    n2=s.indexOf('p')-1;
                                    String z=s.substring(n1, n2);
                                    z1=Integer.parseInt(z);
                                }
                                //System.out.println("El numero de procesos zombies es:"+z1);
                                infoSistemas.get(i).setZombies(z1);
                                break;
                            case "Current Users":
                                s=tokensLinea.nextToken();
                                int us1;
                                if (s.trim().startsWith("NRPE: unable") || s.trim().startsWith("CHECK_NRPE: Socket")){
                                    us1 = Integer.parseInt(null);
                                }
                                else{
                                    n1=s.indexOf('-')+2;
                                    n2=s.indexOf('u')-1;
                                    String us=s.substring(n1, n2);
                                    us1=Integer.parseInt(us);
                                }
                                //System.out.println("El numero de usuarios es:"+us1);
                                infoSistemas.get(i).setUsuarios(us1);
                                break;
                            case "Total Processes":
                                s=tokensLinea.nextToken();
                                int p1;
                                if (s.trim().startsWith("NRPE: unable") || s.trim().startsWith("CHECK_NRPE: Socket")){
                                    p1 = Integer.parseInt(null);
                                }
                                else{
                                    n1=s.indexOf(':')+2;
                                    n2=s.indexOf('p')-1;
                                    String p=s.substring(n1, n2);
                                    p1=Integer.parseInt(p);
                                    //System.out.println("procesos: "+p1);                                
                                }
                     //           System.out.println("El numero total de procesos es:"+p1);
                                infoSistemas.get(i).setProcesos(p1);
                                break;
                            case "SSH Failed Login":
                                s=tokensLinea.nextToken();
                                int ssh1;
                                if (s.trim().startsWith("NRPE: unable") || s.trim().startsWith("CHECK_NRPE: Socket")){
                                    ssh1 = Integer.parseInt(null);
                                }
                                else{
                                    n1=s.indexOf(':')+2;
                                    n2=s.indexOf('f')-1;
                                    String ssh=s.substring(n1, n2).trim();
                                    ssh1=Integer.parseInt(ssh);
                                }
                                //System.out.println("El numero total de procesos es:"+ssh1);
                                infoSistemas.get(i).setSSHFailed(ssh1);
                                break;
                            default:
                                System.out.println("el atributo no se corresponde con ninguno de los parametros");
                                break;                     
                        }
                    }
                }catch(Exception ex){
                    Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE,null, ex);
                }
            } else{
		//System.out.println("EL host es distinto al analizado");
	    }
        }
    }

    private static void procesarStatus(String linea){
     //   System.out.println("STATUS: "+linea);
        for(int i=0; i<nombreSistemas.size();i++){
             if (linea.startsWith(nombreSistemas.get(i))){
                   // System.out.print(nombreSistemas.get(i)+" : ");
                    StringTokenizer tokensLinea= new StringTokenizer(linea,"\t");
                    String nombre=tokensLinea.nextToken();
                    String atributo=tokensLinea.nextToken();
                   // System.out.println("procesando estado");    
                    if(atributo.startsWith("OK")){  
                        infoSistemas.get(i).setEstado(true);
                        int n1=atributo.lastIndexOf("rta")+4;
                        int n2=atributo.indexOf('m');
                        String RTA=atributo.substring(n1, n2).replaceAll(",", ".");
                       // System.out.println("atributo: "+RTA);
                        float RTA1=Float.parseFloat(RTA); 
                        infoSistemas.get(i).setLatencia(RTA1);
                    }
                    else if(atributo.startsWith("CRITICAL")){
                     //   System.out.println("sistema apagado)");
                        infoSistemas.get(i).setEstado(false);
                    }             

             }
        }
    }
}
