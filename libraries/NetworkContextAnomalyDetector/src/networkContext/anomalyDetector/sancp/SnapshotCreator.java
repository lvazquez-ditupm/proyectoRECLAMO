/**
 * "SnapshotCreator" Java class is free software: you can redistribute
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

import java.io.File;
import java.util.Vector;
import networkContext.anomalyDetector.InfoNetworkAnDetector;
import networkContext.anomalyDetector.infoLoader.FilesProcessor;


/**
 * This class generates the snapshot of the network traffic in a specific subnetwork
 * for a specific period of time, by getting the information recorded in SANCP logs.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */

public class SnapshotCreator {

    public SnapshotCreator() {
    }
    
    public Snapshot createSnap(String sensorPath, String interName){
        File folder = new File(sensorPath);
        String name="";
        String shortname="";
        Vector<File> candidates=new Vector<File>();
        //System.out.println(folder.getPath());
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            name=listOfFiles[i].getName();
            if(name.startsWith("realtime")&& name.length()>8 ){
                shortname=name.substring(9);
                if(shortname.startsWith(interName)){
                //System.out.println("candidate: "+name);
                candidates.add(listOfFiles[i]);
                }
            }
          } 
        }
        File f=obtainMostRecent(candidates,interName);
       // System.out.println("El fichero a procesar es el siguiente: "+ f.getPath());
        Vector<InfoNetworkAnDetector> is=FilesProcessor.processOneFile(f.getPath(),interName);
        
      /*  for(int i=0;i<is.size();i++){
            is.elementAt(i).printData();
        }*/
        return new Snapshot(is,interName);
    }

    private File obtainMostRecent(Vector<File> candidates,String interName) {
        int maximum=0;
        int num;
        int pos=0;
        String name="";
        for(int i=0;i<candidates.size();i++){
            name=candidates.elementAt(i).getName();
            num=Integer.parseInt(name.substring(name.lastIndexOf(interName)+interName.length()+1,name.length()));
            //System.out.println("num: "+num);
            if(num>maximum){
                maximum=num;
                pos=i;
            }
        }
        //System.out.println("maximo: "+maximum);
        return candidates.elementAt(pos);
    }
}
