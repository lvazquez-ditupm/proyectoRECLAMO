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

package recparser.ossec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import recparser.Address;
import recparser.DateToXsdDatetimeFormatter;
import recparser.IntrusionAlert;
import recparser.IntrusionSource;
import recparser.IntrusionTarget;
import recparser.idmef.IdmefParser;
import recparser.util.PropsUtil;



/**
 * This class represents an adapter which converts an intrusion alert in ossec
 * format to the idmef format.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class OssecSyslogAdapter {

    private PropsUtil props = new PropsUtil();
    public IntrusionAlert _intAlert;
    private IntrusionTarget _intTarget;
    private IntrusionSource _intSource;
    private String alertFile;
    int portDest;
    int portSrc;
    private String nextIntrusionID;
   
    public OssecSyslogAdapter(String path, String file) {
        _intAlert = new IntrusionAlert();
        this.alertFile = path+"/"+file;
        
        
    } // SnortSyslogAdapter

    public OssecSyslogAdapter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public IntrusionAlert parseAlert(String netMask) {
        long initParser = System.currentTimeMillis();
        System.out.println("*** OssecSyslogAdapter - START ***");

        try{
           // System.out.println("el fichero a procesar es: "+alertFile);
            BufferedReader br = new BufferedReader( new FileReader( alertFile ) );
            String linea;
            boolean fin = false;
            boolean source = false;
            boolean target = false;
            int analyzerNumber = 0;
            String alertident = null;
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
                        alertident = aux[1].trim();
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
//                        _intAlert.setIntStartTime(intDetectionTime);
                        String intrusionCount = currentDate.replace(":", "").replace("-", "");
                        _intAlert.setIntCount(intrusionCount);
                    }
                    if(linea.startsWith("* Analyzer ID:")){
                        analyzerNumber++;
                        if(analyzerNumber>1){
                            String[] aux = linea.split(":");
                            String IDSID = aux[1].trim();
                            _intAlert.setAnalyzerID(IDSID);
                            alertident = IDSID+alertident;
                            _intAlert.setIntID(alertident); //Modificamos el valor de IntrusionID = AnalyzerID+messageID
                            //System.out.println("IDENTIFICADOR DEL ANALIZADOR: "+IDSID);
                            //String IDSconfidence = GetReliability(IDSID);
                            _intAlert.setAnalyzerConfidence(0.0);
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
                        _intSource = new IntrusionSource();
                    }
                    if(linea.startsWith("* Source spoofed:")){
                        String[] aux = linea.split(":");
                        String intrusionSpoofed = aux[1].trim();
                        _intSource.setSpoofed(intrusionSpoofed);
                        //System.out.println("SPOOFED--------------------------------:"+intrusionSpoofed);
                    }
                    if(linea.startsWith("* Node") && source){
                        String[] aux = linea.split(":");
                        if(aux.length>1){
                            String intSourceNode = aux[1].trim();
                        //    System.out.println("sourcenode--------------------------------:"+intSourceNode);
                            _intSource.setSourceName(intSourceNode);
                        }
                                                
                    }
                    if(linea.startsWith("* Addr") && source){
                        String[] aux = linea.split(":");
                        if(aux.length>1){
                            String intSourceID = aux[1].trim();
                        //if (intSourceID!=null){
			    Address add = new Address();
			    add.setAddress(intSourceID);
                            
                            _intSource.setNode(add);
                          //  System.out.println("SOURCEADRR--------------------------------:"+intSourceID);
                        }
                        _intAlert.setIntrusionSource(_intSource);
                    }
                    if(linea.startsWith("*** Target information **")){

                        source=false;
                        target=true;
			_intTarget = new IntrusionTarget();
                        
                    }
                    if(linea.startsWith("* Target decoy:")){
                        String[] aux = linea.split(":");
                        String targetIntDecoy = aux[1].trim();
                       // _intTarget.setTargetIntDecoy(targetIntDecoy);
                        
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
                            Address address = new Address();
                            address.setAddress(targetIP);
                            _intTarget.setNode(address);
                          //  System.out.println("targetIP: "+ targetIP);
                        }
                                      
                        _intAlert.setIntrusionTarget(_intTarget);
                        
                    }
                    if(linea.startsWith("* Service:")){
                        int i1 = linea.indexOf(">", 0);
                        int i2 = linea.indexOf(")", i1 + 1);
                        String targetService = linea.substring(i1 + 1, i2-1);
                    	_intTarget.setServiceName(targetService);
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
                            if (_intAlert.getIntrusionSource().size()==0 && (sourceIP!=null)){
                                IntrusionSource isource=new IntrusionSource();
                                Address source_add = new Address();
				source_add.setAddress(sourceIP);
				isource.setNode(source_add);
                                _intAlert.setIntrusionSource(isource);
                                                   
                            }
                            
                        }catch(Exception ex){
                            Logger.getLogger(OssecSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }                 
                
                }                
            }
            
           // _intAlert.printAlert();
            //_intAlert.setIntImpact(_intAlert.getIntSeverity() * GetRelevance(_intAlert.getIntrusionTarget().getAddressIP()));
            //_intAlert.setAttackerLocation(GetLocation(_intAlert.getSourceIP()));
            if(portDest >0 ){
                 for(int i =0;i<_intAlert.getIntrusionTarget().size();i++){
                    _intAlert.getIntrusionTarget().get(i).setServicePort(portDest);
                }
            }

            if(portSrc > 0){
                for(int i=0;i<_intAlert.getIntrusionSource().size();i++){
                  _intAlert.getIntrusionSource().get(i).setPortSrc(portSrc);
                }
            }
            if(nextIntrusionID!=null){
                _intAlert.setNextIntID(nextIntrusionID);
            }
            if(_intAlert.getIntSeverity()<0){
                _intAlert.setIntSeverity(0); //Asignamos 0 al valor de la severidad
            }
            long endParser = System.currentTimeMillis();
            System.out.println("*** OssecSyslogAdapter END *** Parsing time : "+ (endParser-initParser)+" (ms)*** ");

            //System.out.println("**********OssecSyslogAdapter: he terminado de parsear la alerta***********");
            return _intAlert;
        }catch (Exception ex){
            Logger.getLogger(OssecSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }     

    }

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
                    Logger.getLogger(OssecSyslogAdapter.class.getName()).log(Level.SEVERE, null, ex);

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
         return 2;
     }else return 1;
        
    }

}