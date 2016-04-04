/**
 * "IdmefParser" Java class is free software: you can redistribute
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


package recparser.idmef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import recparser.Address;
import recparser.DateToXsdDatetimeFormatter;
import recparser.IntrusionAlert;
import recparser.IntrusionSource;
import recparser.IntrusionTarget;
import recparser.ossec.OssecSyslogAdapter;
import recparser.util.PropsUtil;

/**
 * This class represents an adapter which converts an intrusion alert from cidn 
 * to the common format used in RECLAMO.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */

public class IdmefParser {

   List <IntrusionAlert> intrusionAlerts= new ArrayList<IntrusionAlert>();
   final String [] severidad = {"info","low","medium","high"};
   private PropsUtil props = new PropsUtil();
    
   public IdmefParser(){
	 
   }
   
   private String[] splitIDMEF(String str){
    
   str = str.replaceAll("</IDMEF-Message>","</IDMEF-Message>~");
   String [] mensajes = str.split("~");
   return mensajes;   
}
   
   //Por ahora el método es de espera activa, habría que cambiarlo por un hilo y tenerlo ejecutando en paralelo!!
   public void leeIDMEF(BufferedReader entradaDatos){
    String str;
    while(true) {
        try {
            try {
                while (entradaDatos.ready()) {
                    str=entradaDatos.readLine();
                    
                    if (!str.equals("")){
                        parser(str);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(IdmefParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(IdmefParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  }

   public List <IntrusionAlert> getIntrusionAlerts(){
  	return intrusionAlerts;
  }

   public List <IntrusionAlert> parser(String input) {
      
      long initParser = System.currentTimeMillis();	  
       System.out.println("*** INIT PARSER IDMEF at "+initParser+" *** ");
       
      //Primero verificar si vienen una o mas alertas
      String [] mensajes = splitIDMEF(input);
      for (int i=0; i < mensajes.length; i++){
        IntrusionAlert intrusionAlert = new IntrusionAlert();
        try{
            DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
            String currentDate = xdf.format(new Date());
            String intrusionCount = currentDate.replace(":", "").replace("-", "");
            intrusionAlert.setIntCount(intrusionCount);
        }
        catch (Exception e){
             System.out.println(e.getMessage());
        }
        
        String messageID = null;
        SAXBuilder builder = new SAXBuilder();    
        StringReader inputReader = new StringReader(mensajes[i]);
        try {

             //XML parser SAX
           Document doc = builder.build(inputReader);
           Element root = doc.getRootElement();
           Namespace ns = root.getNamespace();
           root = root.getChild("Alert", ns);
           
           //Es una alerta
           if (root !=null){
          
                //Cogemos los valores relevantes
               Content content;
                // 0. ID de la alerta
                Attribute attribute = root.getAttribute("messageid");
                if (attribute!=null){
                    intrusionAlert.setIntID(attribute.getValue());
                    intrusionAlert.setMessageID(attribute.getValue());
                    messageID = attribute.getValue();
                }
                //1. Datos del analizador
                Element e = root.getChild("Analyzer",ns);
                if (e!=null){
                    attribute = e.getAttribute("analyzerid");
                    if (attribute !=null){
                        intrusionAlert.setAnalyzerID(attribute.getValue());                    
                        messageID = attribute.getValue()+messageID;
                        intrusionAlert.setIntID(messageID); //MOdificamos el valor de IntID = analyzerID+messageID
                    }
                }


                //2. Tiempo de creación
                e = root.getChild("CreateTime",ns);
                if (e!=null){
                    for (int j=0; j< e.getContentSize();j++){
                        content = e.getContent(j);
                        intrusionAlert.setIntAlertCreateTime(content.getValue());
                    }
                }

                //3. Severidad 
                e = root.getChild("Assessment",ns);
                if (e!=null){  
                     attribute = e.getAttribute("completion");
                     if (attribute!=null)
                        intrusionAlert.setIntCompletion(attribute.getValue());
                      e = e.getChild("Impact",ns);
                      if (e!=null){
                        attribute = e.getAttribute("severity");
                        if (attribute !=null){
                            String attributeValue = attribute.getValue();
                            for (int j =0; j< 4; j++){
                                if (attributeValue.equals(severidad[j])){
                                    intrusionAlert.setIntSeverity(j+1);
                                }
                            }
                        }
                      }
                }

                Namespace reclamo = Namespace.getNamespace("http://reclamo.inf.um.es/idmef");
                Element additional=null;
                additional= root.getChild("AdditionalData",ns);
                if (additional!=null){
                  //4. Porcentaje de ataque
                  additional = additional.getChild("xml",ns);
                  if (additional !=null){

                    additional =additional.getChild("IntrusionTrust", reclamo);

                    if (additional!=null){
                        e = additional.getChild("AttackPercentage", reclamo);
                        if (e!=null){
                           for (int j = 0; j< e.getContentSize();j++)
                                content = e.getContent(j);
                        }
                        //5. Certeza
                        additional = additional.getChild("AssessmentTrust",reclamo);
                        if (additional!=null){
                            additional = additional.getChild("Assessment", reclamo);
                            if (additional!=null){
                                e = additional.getChild("Trust",reclamo);
                                if (e!=null){
                                    for (int j=0; j< e.getContentSize();j++){
                                        content = e.getContent(j);
                                        intrusionAlert.setAnalyzerConfidence(Double.parseDouble(content.getValue()));
                                    }
                                }
                            }
                        }
                    }
                  }

                }

                //6. Tiempo de detección
                e = root.getChild("DetectTime",ns);
                if (e!=null) {
                  content = e.getContent(0);
                  intrusionAlert.setIntDetectionTime(content.getValue());

                }else if (additional !=null){//No aparece esta rama, hay que cogerlo del additionaldata
                  e = additional.getChild("Timestamp",reclamo);
                  if (e!=null){
                      for (int j= 0; j< e.getContentSize(); j++){
                        content = e.getContent(j);
                        intrusionAlert.setIntDetectionTime(content.getValue());
                      }
                  }
                }

                //7. Recursividad en la rama Target
                List targets = root.getChildren("Target", ns);
                Iterator it = targets.iterator();
                while (it.hasNext()){
                    IntrusionTarget intrusionTarget = new IntrusionTarget();
                    listChildren((Element)it.next(),intrusionTarget,null);
                    intrusionAlert.setIntrusionTarget(intrusionTarget);

                }

                //8. Recursividad en la rama Source
                List sources = root.getChildren("Source", ns);
                it = sources.iterator();
                while (it.hasNext()){
                    IntrusionSource intrusionSource = new IntrusionSource();
                    listChildren((Element)it.next(),intrusionSource,null);
                    intrusionAlert.setIntrusionSource(intrusionSource);
                }

                //9. Classification
                
                e = root.getChild("Classification",ns);
                String tipo_alert=null;
                if (e!=null){
                    attribute= e.getAttribute("text");
                    if (attribute !=null){
                        tipo_alert = attribute.getValue();
                        intrusionAlert.setIntName(tipo_alert);
                        String path = "/"+getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
                        String classtype=this.obtainParameter(path+props.getIdmefIntrusionClassificationFile(), tipo_alert);
                  
                        intrusionAlert.setIntType(classtype);
                    }
                }

               //10. En caso de no tener el tipo de alerta antes:
                if (tipo_alert!=null && tipo_alert.equals("unknown")){
                  e = root.getChild("CorrelationAlert",ns);
                  if (e!=null){
                    e = e.getChild("name",ns);
                    if (e!=null){
                        for (int j= 0; j < e.getContentSize();j++){
                            content = e.getContent(j);
                            intrusionAlert.setIntType(content.getValue());
                        }
                    }

                  }
                }
                
                if(intrusionAlert.getIntSeverity()<0){
                    intrusionAlert.setIntSeverity(0); //Asignamos 0 al valor de la severidad en caso de que sea -1
                }
                //Insertamos la alerta leida
                intrusionAlerts.add(intrusionAlert);
           }
        }
            // indicates a well-formedness error
           catch (JDOMException e) {
                System.out.println(" is not well-formed.");
                System.out.println(e.getMessage());
                e.printStackTrace();
           }  
           catch (IOException e) { 
                System.out.println(e+"2");
           }  

           long endParser = System.currentTimeMillis();
           System.out.println("*** END PARSER IDMEF *** Parsing time : "+ (endParser-initParser)+" (ms)*** ");
      }
    
    return intrusionAlerts; 
  
  }
   
   
   public void listChildren(Element current, Object o, Element parent) {
    
       if (o!=null){
           Object object;
           String contentName=current.getName();
           Address address=null;
           //Si es Address generamos una nueva instancia
           if (contentName.equals("Address")){
               address= new Address();
               object = address;
           }else{
               object =o;
           }
        //get the attributes of the current element and add them to the corresponding object of idmefModel package
        List attribute_list = current.getAttributes();
        Iterator it_attribute= attribute_list.iterator();
        while (it_attribute.hasNext()){
            Attribute attribute = (Attribute)it_attribute.next();
            addAttribute(current,attribute, object);
        }
        //get the contents of the current element and add them to the corresponding object of idmefModel package
        List contents = current.getContent();
        Iterator it_contents = contents.iterator();

        while (it_contents.hasNext()) {
            Object child = it_contents.next();
            if (child instanceof Text) {
                    Text t=(Text)child;
                    String contentValue=t.getTextTrim();
                   
                    if(!("".equals(contentValue))){
                        addContent(parent,contentName,contentValue,object);	
                    }
            }
        }
        if (contentName.equals("Address")){
            if (o instanceof IntrusionTarget){
                IntrusionTarget target = (IntrusionTarget) o ;
                target.setNode(address);
            }else if (o instanceof IntrusionSource){
                IntrusionSource source = (IntrusionSource) o ;
                source.setNode(address);
            }
        }
    
        //get the children of the current element and call the method recursively.
        List children = current.getChildren();
        Iterator iterator = children.iterator();

        while (iterator.hasNext()) {
          Element child = (Element) iterator.next();;
          listChildren(child,object, current);
        }
        }
 }
   
   private void addAttribute(Element current, Attribute attribute,Object o){
	  
	  String elemento = current.getName();
	  String attributeName= attribute.getName();
	  String attributeValue = attribute.getValue();
          
          if (elemento.equals("Target")){
              if (attributeName.equals("ident")) {
                  if (o instanceof IntrusionTarget){
                      IntrusionTarget target = (IntrusionTarget) o;
                      target.setTargetID(attributeValue);
                  }
              }
          } else if (elemento.equals("Source")){
              if (o instanceof IntrusionSource){
                  IntrusionSource source = (IntrusionSource) o;
                  if (attributeName.equals("spoofed")){
                    source.setSpoofed(attributeValue);
                    
                  }else if (attributeName.equals("ident")){
                      source.setIdent(attributeValue);
                     
                  }else if (attributeName.equals("interface")){
                      source.setInterfaceSource(attributeValue);
                  }
              }
          }else if (elemento.equals("Address")){
              if (o instanceof Address){
                  Address a = (Address) o;
                  if (attributeName.equals("category")) {
                      a.setCategory(attributeValue);
                     
                  }else if (attributeName.equals("vlan-name")){
                      a.setVlan_name(attributeValue);

                  }else if (attributeName.equals("vlan-num")){
                      a.setVlan_num(attributeValue);

                  }else if (attributeName.equals("ident")){
                      a.setIdent(attributeValue);
                  }
              } 
          }else if (elemento.equals("Service")){
              if (o instanceof IntrusionTarget){
                  IntrusionTarget target = (IntrusionTarget) o;
                  if (attributeName.equals("ident")){
                      target.setServiceID(attributeValue);
                  }
              }
          }
            
   }
   
   private static void addContent(Element current, String contentName, String contentValue,Object o){
	  
	  String elemento = current.getName();
          
          if (elemento.equals("Node")){
              if (o instanceof IntrusionTarget){
                  IntrusionTarget target = (IntrusionTarget) o;
                  if (contentName.equals("Location")){
                      target.setComponentLocation(contentValue);
                  
                  }else if (contentName.equals("name")){
                      target.setComponentName(contentValue);
                  
                  }        
              }else if (o instanceof IntrusionSource){
                  IntrusionSource source = (IntrusionSource) o;
                  if (contentName.equals("Location")){
                      source.setAttackerLocation(contentValue);
                  }else if (contentName.equals("name")){
                      source.setSourceName(contentValue);
                  }        
              }
          } else if (elemento.equals("Address")){
              if (o instanceof Address){
                  Address target = (Address) o;
                  if (contentName.equals("address")){
                      target.setAddress(contentValue);
                  }else if (contentName.equals("netmask")){
                      target.setNetmask(contentValue);
                  }        
              }
          }else if (elemento.equals("Service")){
              if (o instanceof IntrusionTarget){
                  IntrusionTarget target = (IntrusionTarget) o;
                  if (contentName.equals("name")){
                      target.setServiceName(contentValue);
                  }else if (contentName.equals("port")){
                      target.setServicePort(Integer.parseInt(contentValue));
                  }else if (contentName.equals("portlist")){
                      target.setServiceListPort(contentValue);
                  }
              }else if (o instanceof IntrusionSource){
                  IntrusionSource source = (IntrusionSource) o;
                  if (contentName.equals("port")){
                      source.setPortSrc(Integer.parseInt(contentValue));
                  }else if (contentName.equals("protocol")){
                      source.setProtocol(contentValue);
                  }
              }
          }
    }
     
   public void print (IntrusionAlert intrusion){
        
        System.out.println("<Alert messageid="+intrusion.getIntID()+">");
        System.out.println("<DetectTime> o <reclamo:Timestamp> " +  intrusion.getIntDetectionTime());
        System.out.println("<CreateTime>"+ intrusion.getIntAlertCreateTime());
        System.out.println("<Analyzer analyzerid ="+intrusion.getAnalyzerID()+">");
        System.out.println("<Classification text> o <CorrelationAlert><name> "+ intrusion.getIntType());
        System.out.println("<Impact severity="+intrusion.getIntSeverity()+">");
        System.out.println("<reclamo:trust>"+intrusion.getAnalyzerConfidence());
        System.out.println("<Assessment completion="+intrusion.getIntCompletion()+">");
        
         List <Address> nodes;
         Iterator it_node;
        //recorremos el origen

        List <IntrusionSource> sources = intrusion.getIntrusionSource();
        Iterator it = sources.iterator();
        while (it.hasNext()){
            IntrusionSource source = (IntrusionSource) it.next();
            System.out.println("<Source ident="+ source.getIdent() +">");
            System.out.println("<Source interface="+source.getInterfaceSource()+">");
            System.out.println("<Source spoofed ="+source.getSpoofed()+">");
            System.out.println("<Node><name>"+ source.getSourceName());
            System.out.println("<Node><Location>"+source.getAttackerLocation());
            System.out.println("<Service><port>"+source.getPortSrc());
            System.out.println("<Service><protocol>"+source.getProtocol());

            nodes = source.getNode();
            it_node= nodes.iterator();
            
            while (it_node.hasNext()){
                printAddress((Address) it_node.next());
            }
       
        }
        //recorremos el destino
        
        List <IntrusionTarget> targets = intrusion.getIntrusionTarget();
        it = targets.iterator();
        while (it.hasNext()){
            IntrusionTarget target = (IntrusionTarget) it.next();
            System.out.println("<Target ident="+ target.getTargetID()+">");
            System.out.println("<Node><name>"+ target.getComponentName());
            System.out.println("<Node><Location>"+target.getComponentLocation());
            System.out.println("<Service ident="+target.getServiceID()+">");
            System.out.println("<Service><name>"+target.getServiceName());
            System.out.println("<Service><port>"+target.getServicePort());
            System.out.println("<Service><portlist>"+target.getServiceListPort());
    
            nodes = target.getAddress();
            it_node= nodes.iterator();
            
            while (it_node.hasNext()){
                printAddress((Address) it_node.next());
            }
    
        }
   
    }

   private void printAddress(Address nodo){
        
        System.out.println("<Address ident="+nodo.getIdent()+">");
        System.out.println("<Address category="+nodo.getCategory()+">");
        System.out.println("<Address vlan-name="+nodo.getVlan_name()+">");
        System.out.println("<Address vlan-num="+nodo.getVlan_num()+">");
        System.out.println("<Address><address>"+nodo.getAddress());
        System.out.println("<Address><netmask>"+nodo.getNetmask());
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

}
