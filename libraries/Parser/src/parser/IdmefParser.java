/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;


/**
 *
 * @author Pilar
 */
public class IdmefParser {
   
    IntrusionAlert intrusionAlert = new IntrusionAlert();
    final String [] severidad = {"info","low","medium","high"};
    
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
                    //Comprobamos cuantos mensajes vienen (número de roots elements
                    if (!str.equals("")){
                        String [] mensajes = splitIDMEF(str);
                        for (int i=0; i < mensajes.length; i++){
                            parser(mensajes[i]);
                        }
                        
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

  public void parser(String input) {
      
      long initParser = System.currentTimeMillis();	  
       System.out.println("*** INIT PARSER IDMEF at "+initParser+" *** ");
       
      //Primero verificar si vienen una o mas alertas
      String [] mensajes = splitIDMEF(input);
      for (int i=0; i < mensajes.length; i++){
       
        SAXBuilder builder = new SAXBuilder();    
        StringReader inputReader = new StringReader(input);
        try {

             //XML parser SAX
           Document doc = builder.build(inputReader);
           Element root = doc.getRootElement();
           Namespace ns = root.getNamespace();
           root = root.getChild("Alert", ns);

           //Cogemos los valores relevantes

           // 0. ID de la alerta
           Attribute attribute = root.getAttribute("messageid");
           intrusionAlert.setIntID(attribute.getValue());

           //1. Datos del analizador
           Element e = root.getChild("Analyzer",ns);
           attribute = e.getAttribute("analyzerid");
           intrusionAlert.setAnalyzerID(attribute.getValue());


           //2. Tiempo de creación
           e = root.getChild("CreateTime",ns);
           Content content = e.getContent(0);
           intrusionAlert.setIntAlertCreateTime(content.getValue());

           //3. Severidad 
           e = root.getChild("Assessment",ns);
           if (e!=null){  
                  e.getChild("Impact",ns);
                 attribute = e.getAttribute("severity");
                 String attributeValue = attribute.getValue();
                 for (int j =0; j< 4; j++){
                     if (attributeValue.equals(severidad[j]))
                         intrusionAlert.setIntSeverity(j);
                 }
                 attribute = e.getAttribute("completion");
                 intrusionAlert.setIntCompletion(attribute.getValue());
           }

           Namespace reclamo = Namespace.getNamespace("http://reclamo.inf.um.es/idmef");
           Element additional=null;
           additional= root.getChild("AdditionalData",ns);
           if (additional!=null){
             //4. Porcentaje de ataque
             additional = additional.getChild("xml",ns);
             System.out.println(additional.getName());

             additional =additional.getChild("IntrusionTrust", reclamo);
              System.out.println(additional.getName());

             e = additional.getChild("AttackPercentage", reclamo);
             String currentName=e.getName();
             System.out.println(currentName);
             content = e.getContent(0);
             System.out.println(content.getValue());

             //5. Certeza
             additional = additional.getChild("AssessmentTrust",reclamo).getChild("Assessment", reclamo);
             e = additional.getChild("Trust",reclamo);
             content = e.getContent(0);
             intrusionAlert.setAnalyzerConfidence(content.getValue());

           }

           //6. Tiempo de detección
           e = root.getChild("DetectTime",ns);
           if (e!=null) {
             content = e.getContent(0);
             intrusionAlert.setIntDetectionTime(content.getValue());

           }else if (additional !=null){//No aparece esta rama, hay que cogerlo del additionaldata
             e = additional.getChild("Timestamp",reclamo);
             content = e.getContent(0);
             intrusionAlert.setIntDetectionTime(content.getValue());

           }

           //7. Recursividad en la rama Target
            IntrusionTarget intrusionTarget = new IntrusionTarget();
           listChildren(root.getChild("Target",ns),intrusionTarget);

           //8. Recursividad en la rama Source
           IntrusionSource intrusionSource = new IntrusionSource();
           listChildren(root.getChild("Source",ns),intrusionSource);

           //9. Classification
           //listChildren(root.getChild("Classification",ns),null);
           e = root.getChild("Classification",ns);
           attribute= e.getAttribute("text");
           String tipo_alert = attribute.getValue();
           intrusionAlert.setIntType(tipo_alert);

          //10. En caso de no tener el tipo de alerta antes:
           if (tipo_alert.equals("unknown")){
             e = root.getChild("CorrelationAlert",ns);
             if (e!=null){
               e = e.getChild("name",ns);
               content = e.getContent(0);
               intrusionAlert.setIntType(content.getValue());

             }
           }

           intrusionAlert.setIntrusionTarget(intrusionTarget);
           intrusionAlert.setIntrusionSource(intrusionSource);
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
    
    return; 
  
  }
   
   
   public void listChildren(Element current, Object o) {
    
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
                    System.out.println(contentName+ " = "+ contentValue);
                   
                    if(!("".equals(contentValue))){
                        addContent(current,contentName,contentValue,object);	
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
    }
    
    //get the children of the current element and call the method recursively.
    List children = current.getChildren();
    Iterator iterator = children.iterator();
    
    while (iterator.hasNext()) {
      Element child = (Element) iterator.next();;
      listChildren(child,o);
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
                      System.out.println(attributeName +" = "+ attributeValue);
                  }
              }
          } else if (elemento.equals("Source")){
              if (o instanceof IntrusionSource){
                  IntrusionSource source = (IntrusionSource) o;
                  if (attributeName.equals("spoofed")){
                    source.setSpoofed(attributeValue);
                    System.out.println(attributeName +" = "+ attributeValue);
                  }else if (attributeName.equals("ident")){
                      source.setIdent(attributeValue);
                      System.out.println(attributeName +" = "+ attributeValue);
                  }else if (attributeName.equals("interface")){
                      source.setInterfaceSource(attributeValue);
                      System.out.println(attributeName +" = "+ attributeValue);
                  }
              }
          }else if (elemento.equals("Address")){
              if (o instanceof Address){
                  Address a = (Address) o;
                  if (attributeName.equals("category")) {
                      a.setCategory(attributeValue);
                      System.out.println(attributeName +" = "+ attributeValue);
                  }else if (attributeName.equals("vlan-name")){
                      a.setVlan_name(attributeValue);
                      System.out.println(attributeName +" = "+ attributeValue);
                  }else if (attributeName.equals("vlan-num")){
                      a.setVlan_num(attributeValue);
                      System.out.println(attributeName +" = "+ attributeValue);
                  }else if (attributeName.equals("ident")){
                      a.setIdent(attributeValue);
                      System.out.println(attributeName +" = "+ attributeValue);
                  }
              } 
          }else if (elemento.equals("Service")){
              if (o instanceof IntrusionTarget){
                  IntrusionTarget target = (IntrusionTarget) o;
                  if (attributeName.equals("ident")){
                      target.setServiceId(attributeValue);
                      System.out.println(attributeName +" = "+ attributeValue);
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
                      System.out.println(contentName +" = "+ contentValue);
                  }else if (contentName.equals("name")){
                      target.setComponentName(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }        
              }else if (o instanceof IntrusionSource){
                  IntrusionSource source = (IntrusionSource) o;
                  if (contentName.equals("Location")){
                      source.setAttackerLocation(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }else if (contentName.equals("name")){
                      source.setSourceName(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }        
              }
          } else if (elemento.equals("Address")){
              if (o instanceof Address){
                  Address target = (Address) o;
                  if (contentName.equals("address")){
                      target.setAddress(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }else if (contentName.equals("netmask")){
                      target.setNetmask(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }        
              }
          }else if (elemento.equals("Service")){
              if (o instanceof IntrusionTarget){
                  IntrusionTarget target = (IntrusionTarget) o;
                  if (contentName.equals("name")){
                      target.setServiceName(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }else if (contentName.equals("port")){
                      target.setServicePort(Integer.parseInt(contentValue));
                      System.out.println(contentName +" = "+ contentValue);
                  }else if (contentName.equals("portlist")){
                      target.setServiceListPort(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }
              }else if (o instanceof IntrusionSource){
                  IntrusionSource source = (IntrusionSource) o;
                  if (contentName.equals("port")){
                      source.setPortSrc(Integer.parseInt(contentValue));
                      System.out.println(contentName +" = "+ contentValue);
                  }else if (contentName.equals("protocol")){
                      source.setProtocol(contentValue);
                      System.out.println(contentName +" = "+ contentValue);
                  }
              }
          }
    }
}
