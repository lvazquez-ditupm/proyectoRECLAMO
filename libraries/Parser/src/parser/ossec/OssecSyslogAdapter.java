/**
 * "OssecSyslogAdapter" Java class is free software: you can redistribute
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

package parser.ossec;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import parser.DAO.BDManagerIF;
import parser.DAO.DAOException;
import parser.DAO.DataManagerFactory;
import parser.DateToXsdDatetimeFormatter;
import parser.IntrusionAlert;
import parser.IntrusionTarget;
import parser.snort.SnortSyslogAdapter;
import parser.util.PropsUtil;

/**
 * This class represents an adapter which converts an intrusion alert in ossec
 * format to the idmef format.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class OssecSyslogAdapter {
    private String netMask;
    private PropsUtil props = new PropsUtil();
    public IntrusionAlert _intAlert;
    private IntrusionTarget _intTarget;
    private String alertFile;
    int portDest;
    int portSrc;
    private String nextIntrusionID;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_HIGH_VALUE = 10;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_MEDIUM_VALUE = 5;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_LOW_VALUE = 1;
   
    public OssecSyslogAdapter(String path, String file) {
        _intAlert = new IntrusionAlert();
        this.alertFile = path+"/"+file;
        _intTarget = new IntrusionTarget();
    } // SnortSyslogAdapter

    public IntrusionAlert parseAlert(String netMask) {
        this.netMask = netMask;
        try{
           // System.out.println("el fichero a procesar es: "+alertFile);
            BufferedReader br = new BufferedReader( new FileReader( alertFile ) );
            String linea;
            boolean fin = false;
            boolean source = false;
            boolean target = false;
            int analyzerNumber = 0;
            DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
            String currentDate = xdf.format(new Date());
            while(!fin){
                linea = br.readLine();
                if(linea== null || linea.equals("")){
                    fin = true;
                }else{
                   // System.out.println(linea);
                    if(linea.startsWith("* Alert:")){
                        String[] aux = linea.split("="); 
                        String alertident = aux[1].trim();
                       // System.out.println("IDENTIFICADOR--------------------------------:"+alertident);
                        _intAlert.setIntID(alertident);
                    }
                    if(linea.startsWith("* Classification text:")){
                        String[] aux = linea.split(":"); 
                        String alertName = aux[1].trim();
                        _intAlert.setIntName(alertName);
                        //System.out.println("--------------------------alertName---------"+ alertName);
                        String path = "/"+getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
                        String classtype=this.obtainParameter(path+props.getIdmefIntrusionClassificationFile(), alertName);
                        _intAlert.setIntType(classtype);
                    }
                    if(linea.startsWith("* Creation time:")){
                        int i1 = linea.indexOf("(", 0);
                        int i2 = linea.indexOf(")", i1);
                        //String[] aux = linea.split("");
                        String intDetectionTime_aux =linea.substring(i1+1, i2).trim();
                        int i3= intDetectionTime_aux.indexOf(" ",0);
                        int i4= intDetectionTime_aux.indexOf(".",i3);
                        String intDetecTimeDate=intDetectionTime_aux.substring(0,i3).trim();
                        String intDetectTimeHour=intDetectionTime_aux.substring(i3+1,i4).trim();
                        String intDetectionTime=intDetecTimeDate+"T"+intDetectTimeHour;
                        //System.out.println("TIEMPO: "+intDetectionTime);
                        _intAlert.setIntAlertCreateTime(currentDate);
                        _intAlert.setIntDetectionTime(intDetectionTime);
                        _intAlert.setIntStartTime(intDetectionTime);
                        String intrusionCount = currentDate.replace(":", "").replace("-", "");
                        _intAlert.setIntCount(intrusionCount);
                    }
                    if(linea.startsWith("* Analyzer ID:")){
                        analyzerNumber++;
                        if(analyzerNumber>1){
                            String[] aux = linea.split(":");
                            String IDSID = aux[1].trim();
                            _intAlert.setIDSID(IDSID);
                            //System.out.println("IDENTIFICADOR DEL ANALIZADOR: "+IDSID);
                            String IDSconfidence = GetReliability(IDSID);
                            _intAlert.setIDSconfidence(IDSconfidence);
                         //   System.out.println("IDENTIFICADOR--------------------------------:"+IDSconfidence);
                        }                      
                    }
                    if(linea.startsWith("* Impact severity:")){
                        String[] aux = linea.split(":");
                        String severity = aux[1].trim();
                        int intrusionSeverity = getSeverity(severity);
                        _intAlert.setIntSeverity(intrusionSeverity);
                        //System.out.println("SEVERITY--------------------------------:"+severity);
                    }
                    if(linea.startsWith("* Impact completion:")){
                        String[] aux = linea.split(":");
                        String intrusionCompletion = aux[1].trim();
                        _intAlert.setIntCompletion(intrusionCompletion);
                        //System.out.println("COMPLETION--------------------------------:"+intrusionCompletion);
                    }
                    if(linea.startsWith("*** Source information **")){
                        source=true;
                    }
                    if(linea.startsWith("* Source spoofed:")){
                        String[] aux = linea.split(":");
                        String intrusionSpoofed = aux[1].trim();
                        _intAlert.setIntSpoofed(intrusionSpoofed);
                        //System.out.println("SPOOFED--------------------------------:"+intrusionSpoofed);
                    }
                    if(linea.startsWith("* Node") && source){
                        String[] aux = linea.split(":");
                        if(aux.length>1){
                            String intSourceNode = aux[1].trim();
                        //    System.out.println("sourcenode--------------------------------:"+intSourceNode);
                            _intAlert.setSourceName(intSourceNode);
                        }
                                                
                    }
                    if(linea.startsWith("* Addr") && source){
                        String[] aux = linea.split(":");
                        if(aux.length>1){
                            String intSourceID = aux[1].trim();
                        //if (intSourceID!=null){
                            _intAlert.setSourceIP(intSourceID);
                          //  System.out.println("SOURCEADRR--------------------------------:"+intSourceID);
                        }                    
                    }
                    if(linea.startsWith("*** Target information **")){
                        source=false;
                        target=true;
                        
                    }
                    if(linea.startsWith("* Target decoy:")){
                        String[] aux = linea.split(":");
                        String targetIntDecoy = aux[1].trim();
                        _intAlert.setTargetIntDecoy(targetIntDecoy);
                        
                    }
                    if(linea.startsWith("* Node") && target){
                        String[] aux = linea.split(":");
                        if(aux.length>1){
                            String intTargetNode = aux[1].trim();
                        //if (intTargetNode!=null){
                            _intTarget.setComponentLocation(intTargetNode);
                        }                        
                    }
                    if(linea.startsWith("* Addr") && target){
                        String[] aux = linea.split(":");
                        String intTarget = aux[1].trim();
                        StringTokenizer st2 = new StringTokenizer(intTarget);
                        String targetName = st2.nextToken().trim().replaceAll("[()]", "");
                        //System.out.println("targetName:"+targetName);
                        _intTarget.setComponentName(targetName);
                        if (st2.hasMoreTokens()){
                            String targetIP = st2.nextToken().trim();
                            _intTarget.setAddressIP(targetIP);
                          //  System.out.println("targetIP: "+ targetIP);
                        }
                                      
                        _intAlert.setIntrusionTarget(_intTarget);
                        
                    }
                    if(linea.startsWith("* Service:")){
                        int i1 = linea.indexOf(">", 0);
                        int i2 = linea.indexOf(")", i1 + 1);
                        String targetService = linea.substring(i1 + 1, i2-1);
                    
                    }
                    if(linea.startsWith("*** Additional data")){
                        target = false;
                    }
                    if(!linea.startsWith("*") && !linea.equals("")){
                        //System.out.println("adicional log --> "+linea);
                        try{
                            int i1 = linea.indexOf("rhost",0);
                            int i2 = linea.indexOf(" ", i1);
                            String sourceIP = linea.substring(i1+6,i2).trim();
                          //  System.out.println("origen de adiciontal log: "+ sourceIP);
                            //System.out.println("asa^^"+ _intAlert.getSourceIP());
                            if (_intAlert.getSourceIP()==null && (sourceIP!=null)){
                                _intAlert.setSourceIP(sourceIP);
                                                   
                            }
                            
                        }catch(Exception e){}
                    }                 
                
                }                
            }
            
           // _intAlert.printAlert();
            _intAlert.setIntImpact(_intAlert.getIntSeverity() * GetRelevance(_intAlert.getIntrusionTarget().getAddressIP()));                
            _intAlert.setAttackerLocation(GetLocation(_intAlert.getSourceIP()));
            _intAlert.setIntEndingTime(100);
            if(portDest > 0){_intAlert.setPortDest(portDest);}
            if(portSrc > 0){_intAlert.setPortSrc(portSrc);}
            if(nextIntrusionID!=null){
                _intAlert.setNextIntID(nextIntrusionID);
            }
            //System.out.println("**********OssecSyslogAdapter: he terminado de parsear la alerta***********");
            return _intAlert;
        }catch (Exception e){
            System.out.println (e);
            return null;
        }     

    }

    private int GetRelevance(String assetIP){
        int aloi_total=0;
        BDManagerIF bd=DataManagerFactory.getInstance().createDataManager();
        try {
            Object result = bd.obtainAssetLevelOfImportance(assetIP).get("assetLevelOfImportance");
            if(result instanceof String){
                String resultSt = (String) result;
                if(resultSt.equalsIgnoreCase("high")){
                    aloi_total = ASSET_LEVEL_OF_IMPORTANCE_HIGH_VALUE;
                }
                else if(resultSt.equalsIgnoreCase("medium")){
                    aloi_total = ASSET_LEVEL_OF_IMPORTANCE_MEDIUM_VALUE;
                }
                else if(resultSt.equalsIgnoreCase("low")){
                    aloi_total = ASSET_LEVEL_OF_IMPORTANCE_LOW_VALUE;
                }
            }else if(result instanceof Integer){
                aloi_total = (Integer)  result;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SnortSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DAOException ex) {
            Logger.getLogger(SnortSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }            
        
        return aloi_total;
    } // GetRelevance

    //TODO: Introducir aqui la confianza en el IDS proporcionada por UMU
    private String GetReliability(String idsId) {

        try {
            String path = "/"+getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                    new DataInputStream(
                    new FileInputStream(path+props.getIDSReliabilityFile()))));
            String line;

            while((line = reader.readLine()) != null) {
                int i = line.indexOf(" ");
                String id = line.substring(0, i);
                String reliability = line.substring(i + 1, line.length());

                if(idsId.equals(id))
                    return reliability;
            } // while

            return null;
        } // try
        catch(Exception e) {
            e.printStackTrace();
            return null;
        } // catch
    } // GetReliability

    private String GetLocation(String ip) {
        int maskLength = new Integer(netMask.substring(
                netMask.indexOf("/") + 1, netMask.length())).intValue();
        String mask = netMask.substring(0, netMask.indexOf("/"));

        for(int i = 0; i < (maskLength / 8) + 1; i++) {
            int octect;

            if(i == (maskLength / 8)) {
                octect = GetOctect(i, ip + ".") & GetOctect(maskLength % 8);

                if(octect == GetOctect(i, mask + "."))
                    return "Network";
            } // if
            else {
                octect = GetOctect(i, ip + ".");

                if(octect != GetOctect(i, mask + "."))
                    return "Network";
            } // else


        } // for

        return "Local";
    } // GetLocation

    private int GetOctect(int index, String ip) {
        int i1 = -1;
        int i2 = ip.indexOf(".");

        for(int i = 0; i < index; i++) {
            i1 = i2;
            i2 = ip.indexOf(".", i2 + 1);
        } // for

        return new Integer(ip.substring(i1 + 1, i2)).intValue();
    } // GetOctect

    private int GetOctect(int n) {
        switch(n) {
            case 1:
                return 128;
            case 2:
                return 64;
            case 3:
                return 32;
            case 4:
                return 16;
            case 5:
                return 8;
            case 6:
                return 4;
            case 7:
                return 2;
            case 8:
                return 1;
            default:
                return 0;
        } // switch
    } // GetOctect
    
    private String obtainParameter(String path, String name){
    String s=null;
    File f = new File( path );
    BufferedReader entrada=null;
        try {
            entrada = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException ex) {
            System.out.println("Fichero no encontrado");
            //System.out.println("Could not read AIRS config. (File: " + AIRS_PROPERTIES_FILE );
            throw new RuntimeException("Could not read file config. (File: " + path + ")", ex);
        } 
        if (f.exists()){
        String texto=null;
            do {
                try {
                    texto = entrada.readLine();
                    if (texto != null && !texto.startsWith("#")) {
                        s=procesarLinea(texto,name);
                    }
                } catch (IOException ex) {
                    System.out.println("Excepción al operar con el fichero");
                }
                } while(texto!=null && s==null);
    }
    return s;
}

    private String procesarLinea(String texto, String name) {
        String parameter=null;
        if(texto.startsWith(name)){
            parameter=texto.substring(texto.indexOf("=")+1);
        }
        return parameter;

    }
    
    private int getSeverity (String value){
     if(value.equalsIgnoreCase("high")){
         return 4;
     }
     else if(value.equalsIgnoreCase("medium")){
         return 3;
     }else if(value.equalsIgnoreCase("low")){
         return 1;
     }else return 0;
        
    }

}