/**
 * "OntAIRSReasoner" Java class is free software: you can redistribute
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

package ontairs;

import ontairs.utils.DateToXsdDatetimeFormatter;
import airs.responses.executor.CentralModuleExecution;
import airs.responses.executor.ResponseActionParams;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import context.entropy.variance.ContextAnomalyIndicator;
import context.entropy.variance.ContextAnomalyIndicatorList;
import evaluation.system.executor.ResponseTotalEfficiency;
import evaluation.system.mode.selector.EvaluationSystemModeSelector;
import evaluation.system.mode.selector.ExecutionModeParams;


import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;




import java.util.Iterator;
import java.util.List;
import ontairs.utils.PropsUtil;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.jena.ModelExtractor;
import parser.IntrusionAlert;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;

/**
 * This class represents the semantic reasoner of an AIRS based on ontologies.
 *  
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class OntAIRSReasoner{
    
    private PropsUtil props = new PropsUtil();

    private String ontology_rules = props.getOntAIRSOntologyRulesNamespaceValue();
    private String ontology_rules_file = props.getOntAIRSOntologyRulesFileValue();
    private String ontology_infered_file = props.getOntAIRSOntologyInferredFileValue();
    private String PREFIXIND = props.getOntAIRSOntologyPrefixValue();
    private final String ontology_instances = props.getOntAIRSOntologyInstancesNamespaceValue();
    private  String ontology_instances_file = props.getOntAIRSOntologyInstancesFileValue();
    private final String ontology_infered_instances_file = props.getOntAIRSOntologyInferredInstancesFileValue();
    private final String ontology_infered_instances = props.getOntAIRSOntologyInferredInstancesNamespaceValue();
    
    private String ejemplarIntrusionNuevo;
    static String ejemplarContextoRedNuevo;
    static String ejemplarContextoSistemaNuevo;
    static String nextIntID;
    static String ipdest;
    static String iporigen;
    static String protocolo;
    static String pdest;
    //Parametros utilizados al ejecutar la respuesta
    static String user;
    static String intrusionType;
    private Boolean hids = false;
    public static final int IDS_CONFIDENCE_HIGH_VALUE = 3;
    public static final int IDS_CONFIDENCE_MEDIUM_VALUE = 2;
    public static final int IDS_CONFIDENCE_LOW_VALUE = 1;
    static long timebeforeCarga;
    static long timeafterCarga;
    static long timebeforeRazonamiento;
    static long timeafterRazonamiento;
    private IntrusionAlert alertmap;
    private ContextAnomalyIndicator contextAnomalyIndicator;
    private ContextAnomalyIndicatorList listContextAnomalyIndicator;
    private CentralModuleExecution MCER;  

    /*
     * Creamos un ontology model, extensio de Jena RDF que proporciona habilidades extra para el tratamiento de fuentes de datos ontologicos.
     * El ontology model se crea a traves de Jena ModelFactory. En este caso, el lenguaje usado es OWL.
     * El modelo creado es un modelo para almacenar todos los datos que se pasarán al motor de inferencia: CLases y ejemplares del IRS,
     * y reglas SWRL, así como para almacenar la información sobre las respuestas recomendadas y óptimas inferidas.
     */
    private OntModel ontologyModel;
    private OntModel rules_model;
    private OntModel allModel;
    private Individual resNuevo;
    private OntModel inferedModel;
    private OntModel inferedIndividuosModel;
    private boolean isARepeatedFormattedIntrusion= false;
    private boolean isAExistingFormattedIntrusion = false;
    static ExecutionModeParams para;
    String[] initAdParam;

    public OntAIRSReasoner(IntrusionAlert alertMap, CentralModuleExecution exec) {

        this.alertmap = alertMap;
        this.listContextAnomalyIndicator = new ContextAnomalyIndicatorList();
        this.MCER = exec;
    }
    
    /**
     * addFormattedIntrusion añade un ejemplar de la clase FormattedIntrusion
     * de la ontologia. Añade el dispositivo como propiedad de IDS
     * "generate".
     * @param args: String attackerLocation, int intrusionImpact,
            String attackerType, String IDSID, String sourceOfIntrusionType,
            int intrusionSeverity, String intrusionStartTime, String targetOfIntrusionID,
            String intrusionType, String targetOfIntrusionType, String intrusionConsequence,
            String intrusionDetectionTime, String intrusionEndingTime, String nextIntrusionID,
            String intrusionID
     */
    void addFormattedIntrusion(IntrusionAlert map, boolean same, boolean existing, ArrayList sameIndi, ArrayList existingIntrusionIndi ){
        Resource res_classFormattedIntrusion = ontologyModel.getResource(PREFIXIND + "FormattedIntrusion");
        OntClass class_FormattedIntrusion = (OntClass) res_classFormattedIntrusion.as(OntClass.class);
        this.alertmap = map;
        String status="Pending";
        if(alertmap.getIntID()==null){
            return;
        }
        String intrusionNueva = alertmap.getIntID();
        this.ejemplarIntrusionNuevo = intrusionNueva;
        resNuevo=ontologyModel.getOntClass(PREFIXIND+"FormattedIntrusion").createIndividual(PREFIXIND+this.ejemplarIntrusionNuevo);
        if(same){
            status="Complete";
            for(int j=0; j<sameIndi.size();j++){
                Resource sameIndiRes = inferedIndividuosModel.getIndividual(sameIndi.get(j).toString());
                resNuevo.setSameAs(sameIndiRes);
            }
        }
        if(existing){
            status="Complete";
            for(int j=0; j<existingIntrusionIndi.size();j++){
                Resource existingIndiRes = inferedIndividuosModel.getIndividual(existingIntrusionIndi.get(j).toString());
                Property isContinuationOf_prop = ontologyModel.getObjectProperty(PREFIXIND+"isContinuationOf");
                resNuevo.addProperty(isContinuationOf_prop,existingIndiRes);
            }
        }
        
        /* Se añade los valores de las propiedades asociados al nuevo ejemplar de la clase FormattedIntrusion
         * Estos valores serán cruciales para la elección de la respuesta
         */
        
        addIndividualProperty(resNuevo, alertmap.getAttackerLocation(), "attackerLocation");

        addIndividualDateTimeProperty(resNuevo, alertmap.getIntStartTime(),"intrusionStartTime");

        addIndividualDateTimeProperty(resNuevo, alertmap.getIntDetectionTime(),"intrusionDetectionTime");

        addIndividualProperty(resNuevo, alertmap.getNextIntID(),"nextIntrusionID");

        addIndividualProperty(resNuevo, alertmap.getIntID(),"intrusionID");

        addIndividualDateTimeProperty(resNuevo, alertmap.getIntAlertCreateTime(),"intrusionAlertCreateTime");

        addIndividualProperty(resNuevo, alertmap.getSourceIP(),"sourceOfIntrusionID");

        addIndividualProperty(resNuevo, alertmap.getIntImpact(),"intrusionImpact");

        addIndividualProperty(resNuevo, alertmap.getPortDest(),"portDest");

        addIndividualProperty(resNuevo, alertmap.getPortDest(),"portSrc");       

        String IDSconfidence = alertmap.getIDSconfidence().trim();     
        
        
        try {
            int intrusionImpactInt = alertmap.getIntImpact();
            int realIntrusionImpact = 0;
            if(IDSconfidence.equals("high")){
                realIntrusionImpact = intrusionImpactInt * IDS_CONFIDENCE_HIGH_VALUE;
            }
            else if(IDSconfidence.equals("medium")){
                realIntrusionImpact = intrusionImpactInt * IDS_CONFIDENCE_MEDIUM_VALUE;
            }
            else if(IDSconfidence.equals("low")){
                realIntrusionImpact = intrusionImpactInt * IDS_CONFIDENCE_LOW_VALUE;
            }
            
            addIndividualProperty(resNuevo, realIntrusionImpact, "realIntrusionImpact");
        }
        catch(Exception e){
            System.out.println(e);
        }

        addIndividualProperty(resNuevo, alertmap.getIntSeverity(), "intrusionSeverity");
       
       // Añadiendo target de ataque
        try{
            String targetOfIntrusionType = alertmap.getIntrusionTarget().getAddressIP();
            OntClass compclass = ontologyModel.getOntClass(PREFIXIND+"SystemComponent");
            Iterator comp_instances = compclass.listInstances();
            while (comp_instances.hasNext()){
                Individual componente = (Individual) comp_instances.next();
                Property address= ontologyModel.getObjectProperty(PREFIXIND+"hasAddress");
                Iterator address_instances= componente.listPropertyValues(address);

                int v=0;
                while(address_instances.hasNext()){
                    v++;
                    Property ipaddress_prop= ontologyModel.getDatatypeProperty(PREFIXIND+"addressIP");
                    Resource rr = (Resource) address_instances.next();

                    Individual indiv = ontologyModel.getIndividual(PREFIXIND+rr.getLocalName());
                    String ipaddress_value_entero= indiv.getPropertyValue(ipaddress_prop).toString();
                    String ipaddress_value=ipaddress_value_entero.substring(0,ipaddress_value_entero.indexOf("^"));
                    if(ipaddress_value.equals(targetOfIntrusionType)){
                        Property hasTarget_prop = ontologyModel.getObjectProperty(PREFIXIND + "hasTarget");
                        resNuevo.addProperty(hasTarget_prop, componente);
                    }
                }
            }
        } // fin del try que añade el target del ataque
        catch(Exception e){
            System.out.println("Componente no encontrado en el sistema o error añadiendo objetivo");
        }
        
        String intrusionTypeFI = alertmap.getIntType();
        try{
            // doy valor al tipo de itnrusión.
            intrusionType = intrusionTypeFI;
            Resource threat_res = ontologyModel.getResource(PREFIXIND+"SpecificThreat");
            OntClass threat_class = (OntClass) threat_res.as(OntClass.class);
            Iterator threat_subclass_it = threat_class.listSubClasses();
            while(threat_subclass_it.hasNext()){
                OntClass threat_subclass= (OntClass)threat_subclass_it.next();
                if(intrusionTypeFI.equals(threat_subclass.getLocalName())){
                    Iterator threat_indiv_it = threat_subclass.listInstances();
                    while(threat_indiv_it.hasNext()){
                        Individual threat_indiv = (Individual) threat_indiv_it.next();
                        Property hasIntrusionType_prop = ontologyModel.getObjectProperty(PREFIXIND+"hasIntrusionType");
                        resNuevo.addProperty(hasIntrusionType_prop, threat_indiv);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        } // fin de añadir tipo de intrusion detectada

        addIndividualProperty(resNuevo, 0,"numberOfRecommendedResponses");
        
        addIndividualProperty(resNuevo, same, "isARepeatedFormattedIntrusion");
        
        try{
            addIndividualProperty(resNuevo, status,"responseStatus");
        }
        catch(Exception e){
            System.out.println(e);
        }
        
        /* Se añade la alerta recibida como nuevo ejemplar de la propiedad
         * "generate" de la instancia de la clase IDS cuyo IDSID es el incluido en la alerta
         */
        Resource r = ontologyModel.getResource(PREFIXIND + "IntrusionDetectionSystem");

        OntClass class_IDS = (OntClass) r.as(OntClass.class);
        String detecSystemID;
        String formattedIntruIDSID = alertmap.getIDSID();
        Iterator IDS_instances = class_IDS.listInstances();
        int numero = 0;
        Resource resFormattedIntrusion = ontologyModel.getResource(PREFIXIND + this.ejemplarIntrusionNuevo);

        while (IDS_instances.hasNext()){

            Individual IDS = (Individual) IDS_instances.next();

            Property propIDSID = ontologyModel.getProperty(PREFIXIND+ "IDSID");
            //Obtenemos el valor de la propiedad IDSID del ejemplar de IntrusionDetectionSystem.
            detecSystemID =  IDS.getPropertyValue(propIDSID).toString();
            detecSystemID = detecSystemID.substring(0,detecSystemID.indexOf("^"));
            if(detecSystemID.equals(formattedIntruIDSID)){
                Property generateIDS = ontologyModel.getProperty(PREFIXIND + "generates");
                IDS.addProperty(generateIDS, resFormattedIntrusion);
                resFormattedIntrusion.addProperty(ontologyModel.getProperty(PREFIXIND + "isGeneratedBy"), IDS);
                if(IDSconfidence!=null){
                    Literal confidence = ontologyModel.createTypedLiteral(IDSconfidence);
                    Property propIDS = ontologyModel.getProperty(PREFIXIND + "IDSconfidence");
                    IDS.setPropertyValue(propIDS, confidence);
                }
            }
            numero++;
        } // fin de añadir formattedIntrusionID a generates de IDS
        
        /* Se añade la alerta recibida como nuevo ejemplar de la propiedad
         * "receivedFormattedIntrusion" de la instancia de la clase IntrusionResponseSystem
         */
        Resource res_IRS = ontologyModel.getResource(PREFIXIND + "AutomatedIntrusionResponseSystem");
        OntClass class_IRS = (OntClass) res_IRS.as(OntClass.class);
        Iterator IRS_instances = class_IRS.listInstances();
        while (IRS_instances.hasNext()){
            Individual IRS = (Individual) IRS_instances.next();
            Property receivedIntrusion = ontologyModel.getProperty (PREFIXIND + "receivedFormattedIntrusion");
            Resource resFormattedIntrus = ontologyModel.getResource(PREFIXIND +this.ejemplarIntrusionNuevo);
            IRS.addProperty(receivedIntrusion,resFormattedIntrus);
        }
        System.out.println(Thread.currentThread()+"Alerta de intrusion introducido en el model reasonedModel, " + ejemplarIntrusionNuevo);
        System.out.println("________________________________________________________________________");
    }

    private void addIndividualProperty(Individual individual, Object value, String propName){
        if (value instanceof String){
            String valueprop = (String)value;
            if(valueprop != null){
                Literal indi_prop_literal = ontologyModel.createTypedLiteral(valueprop);
                Property prop = ontologyModel.getProperty(PREFIXIND + propName);
                individual.setPropertyValue(prop, indi_prop_literal);
            }   
        }
        else if(value instanceof Integer){
            int valueprop = (Integer) value;
            if(valueprop >=0){
                Literal indi_prop_literal = ontologyModel.createTypedLiteral(valueprop);
                Property prop = ontologyModel.getProperty(PREFIXIND + propName);
                individual.setPropertyValue(prop, indi_prop_literal);   
            }
        }
        else if(value instanceof Double){
            double valueprop = (Double) value;
            if(valueprop >=0.0){
                Literal indi_prop_literal = ontologyModel.createTypedLiteral(valueprop);
                Property prop = ontologyModel.getProperty(PREFIXIND + propName);
                individual.setPropertyValue(prop, indi_prop_literal);   
            }
        }
        else if(value instanceof Boolean){
            boolean valueprop = (Boolean) value;
            Literal indi_prop_literal = ontologyModel.createTypedLiteral(valueprop);
            Property prop = ontologyModel.getProperty(PREFIXIND + propName);
            individual.setPropertyValue(prop, indi_prop_literal);   
        }
    }
    
    private void addIndividualDateTimeProperty(Individual individual, Object value, String propName){
        String valueprop = (String)value;
        if(valueprop != null){
            Literal indi_prop_literal = ontologyModel.createTypedLiteral(valueprop, "http://www.w3.org/2001/XMLSchema#dateTime");
            Property prop = ontologyModel.getProperty(PREFIXIND + propName);
            individual.setPropertyValue(prop, indi_prop_literal); 
        }
    }
    
    void addNetworkContext(String ide,int anomaly, String contextInfDate){
        ContextAnomalyIndicator context_net = new ContextAnomalyIndicator ("Network",anomaly);
        listContextAnomalyIndicator.addContextAnomalyIndicator(context_net);
        System.out.println("____________________________________________________________________________________________");
        System.out.println(Thread.currentThread()+"metodo addNetworkContext");
        OntClass class_NetworkContext = ontologyModel.getOntClass(PREFIXIND+ "NetworkContext");
        String idnc = "network"+ide;
        Individual netContextNuevo = ontologyModel.createIndividual(PREFIXIND + idnc, class_NetworkContext);

        ejemplarContextoRedNuevo = idnc;
        
        if(Integer.toString(anomaly)!= null){
            addIndividualProperty(netContextNuevo, anomaly, "networkAnomaly");

        }
        if(contextInfDate!=null){
            addIndividualDateTimeProperty(netContextNuevo, contextInfDate, "contextInformationDate");

        }
        
        System.out.println(Thread.currentThread()+"Contexto de red introducido en el modelo: ,"+ejemplarContextoRedNuevo);
        System.out.println("________________________________________________________________________");
       
        //inferedResponseModel =reasonedModel;
    }
    
    /**
     * addSystemContext aÃ±ade un ejemplar de la clase SystemContext
     * de la ontologia
     * @param args: int systemActiveProcesses, int systemCPUUsage, int systemFreeSpace, int systemLatency,
     *              int systemNumberOfUsersLogged, int systemStatus, systemSSHFailed
     */
    void addSystemContext(String ide, SystemAnomaly sys_anomaly , String contextInfDate){
        System.out.println("____________________________________________________________________________________________");
        System.out.println("metodo addSystemContext");
        
        OntClass class_SystemContext = ontologyModel.getOntClass(PREFIXIND+"SystemContext");
        String idnc = "system"+ide;
        Individual sysContextNuevo = ontologyModel.createIndividual(PREFIXIND + idnc, class_SystemContext);

        ejemplarContextoSistemaNuevo = idnc;
            
        if(Integer.toString(sys_anomaly.getProcesosA())!= null){
                addIndividualProperty(sysContextNuevo, sys_anomaly.getProcesosA(), "systemActiveProcesses");
                ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator ("Process",sys_anomaly.getProcesosA());
                listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys);
        }
        if(Integer.toString(sys_anomaly.getCPUA())!= null){
                addIndividualProperty(sysContextNuevo, sys_anomaly.getCPUA(), "systemCPUUsage");
                ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator ("CPU",sys_anomaly.getCPUA());
                listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys);
        }
        if(Integer.toString(sys_anomaly.getDiscoduroA())!= null){
                addIndividualProperty(sysContextNuevo, sys_anomaly.getDiscoduroA(), "systemFreeSpace");
                ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator ("Disk",sys_anomaly.getDiscoduroA());
                listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys);
        }
        if(Integer.toString(sys_anomaly.getLatenciaA())!= null){
                addIndividualProperty(sysContextNuevo, sys_anomaly.getLatenciaA(), "systemLatency");
                ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator ("Latency",sys_anomaly.getLatenciaA());
                listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys);
        }
        if(Integer.toString(sys_anomaly.getUsuariosA())!= null){
                addIndividualProperty(sysContextNuevo, sys_anomaly.getUsuariosA(), "systemNumberOfUsersLogged");
                ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator ("User",sys_anomaly.getUsuariosA());
                listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys);
        }
        if(Integer.toString(sys_anomaly.isEstadoA())!= null){
                addIndividualProperty(sysContextNuevo, sys_anomaly.isEstadoA(), "systemStatus");
                ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator ("Status",sys_anomaly.isEstadoA());
                listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys);
        }
        if(Integer.toString(sys_anomaly.getSSHFailedA())!= null){
                addIndividualProperty(sysContextNuevo, sys_anomaly.getSSHFailedA(), "systemSSHFailed");
                ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator ("SSHFailed",sys_anomaly.getSSHFailedA());
                listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys);
        }
        if(contextInfDate!=null){
            addIndividualDateTimeProperty(sysContextNuevo, contextInfDate, "contextInformationDate");
        }
       
        ContextAnomalyIndicator context_sys_zombie = new ContextAnomalyIndicator ("Zombie",sys_anomaly.getZombiesA());
        listContextAnomalyIndicator.addContextAnomalyIndicator(context_sys_zombie);
              
        System.out.println(Thread.currentThread()+"Contexto de sistemas introducido en el modelo: "+ejemplarContextoSistemaNuevo);
        System.out.println("________________________________________________________________________");
    }

    boolean checkSimilarIntrusion(IntrusionAlert map){
        synchronized (ontology_infered_instances) {
                        
            System.out.println("____________________________________________________________________________________________");
            System.out.println( Thread.currentThread() + "-------OntAIRReasoner - checkSimilarIntrusion- method-----------");
            String sameAlertDate= null;
            String existingIntrusionDate = null;
            ArrayList existingIntrusionIndi = new ArrayList();
            try{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                long time =(simpleDateFormat.parse(map.getIntDetectionTime())).getTime();
                long timesamealert= time -2000;
                long timeexistingalert = time - 5000;
                DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
                sameAlertDate = xdf.format(new Date(timesamealert));
                existingIntrusionDate = xdf.format(new Date(timeexistingalert));
            }
            catch(Exception e){         
            }
            String sameAlert = "PREFIX individuos: <"+PREFIXIND+">" +
                     " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
                     " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                     " PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
                     " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
                     " SELECT DISTINCT ?formattedintrusion" +
                     " WHERE {" +
                        "?threat a individuos:"+map.getIntType()+ "."+
                        "?componente a individuos:SystemComponent ."+
                        "?componente individuos:hasAddress ?address ."+
                        "?address individuos:addressIP ?ip ."+
                        "FILTER (?ip = \""+map.getIntrusionTarget().getAddressIP()+"\")."+
                        "?formattedintrusion individuos:intrusionSeverity ?severity."+
                        "FILTER (?severity ="+map.getIntSeverity()+") ."+
                        "?formattedintrusion individuos:portDest ?portd ."+
                        "FILTER (?portd = "+map.getPortDest()+ " )."+
                        "?formattedintrusion individuos:portSrc ?ports ."+
                        "FILTER (?ports = "+map.getPortSrc()+ ") ."+
                        "?formattedintrusion individuos:sourceOfIntrusionID ?source ."+
                        "FILTER (?source =  \""+map.getSourceIP()+"\") ."+
                        "?formattedintrusion individuos:intrusionDetectionTime ?idt ."+
                        "FILTER (xsd:dateTime(?idt) >= \""+sameAlertDate+"\"^^xsd:dateTime) ."+
                        "?formattedintrusion individuos:hasIntrusionType ?threat;"+
                        "individuos:hasTarget ?componente ."+
                     " } ";
            
            String existingIntrusion = "PREFIX individuos: <"+PREFIXIND+">" +
                         " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
                         " SELECT DISTINCT ?formattedintrusion" +
                         " WHERE {" +
                            "?threat a individuos:"+map.getIntType()+ "."+
                            "?componente a individuos:SystemComponent ."+
                            "?componente individuos:hasAddress ?address ."+
                            "?address individuos:addressIP ?ip ."+
                            "FILTER (?ip = \""+map.getIntrusionTarget().getAddressIP()+"\")."+
                            "?formattedintrusion individuos:intrusionDetectionTime ?idt ."+
                            "FILTER (xsd:dateTime(?idt) >= \""+existingIntrusionDate+"\"^^xsd:dateTime) ."+
                            "?formattedintrusion individuos:hasIntrusionType ?threat;"+
                            "individuos:hasTarget ?componente ."+
                         " } ";

            inferedIndividuosModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            inferedIndividuosModel.read(ontology_infered_instances);
            Query q = QueryFactory.create(sameAlert);
            QueryExecution qe = QueryExecutionFactory.create(q, inferedIndividuosModel);
            ResultSet rs = qe.execSelect();
            ArrayList sameIndi = new ArrayList();
            int num = 0;
            boolean nombreRepetido = false;
            String nombreNuevo= null;
            List<QuerySolution> lista = ResultSetFormatter.toList(rs);
 
            for(int i =0;i<lista.size();i++){
                QuerySolution solution = lista.get(i);
                Individual indsame = inferedIndividuosModel.getIndividual(solution.get("formattedintrusion").toString());
                sameIndi.add(indsame);
                if(indsame.getLocalName().equals(map.getIntID())){
                   nombreRepetido = true;
                }
                num ++;
            }

            if (num>0){
                isARepeatedFormattedIntrusion = true;
                if(nombreRepetido){
                    nombreNuevo = map.getIntID()+"."+num;

                    map.setIntID(nombreNuevo);
                }
            }
            else{
                Query que = QueryFactory.create(existingIntrusion);
                QueryExecution quex = QueryExecutionFactory.create(que, inferedIndividuosModel);
                ResultSet rsex = quex.execSelect();
               
                int numex = 0;
                boolean nombreRepetidoEI = false;
                String nombreNuevoEI= null;
                List<QuerySolution> listaEI = ResultSetFormatter.toList(rsex);

                for(int i =0;i<listaEI.size();i++){
                    QuerySolution solution = listaEI.get(i);
                    Individual indexist = inferedIndividuosModel.getIndividual(solution.get("formattedintrusion").toString());
                    existingIntrusionIndi.add(indexist);
                    if(indexist.getLocalName().equals(map.getIntID())){
                       nombreRepetidoEI = true;
                    }
                    numex ++;
                }

                if (numex>0){
                    isAExistingFormattedIntrusion = true;
                    if(nombreRepetidoEI){
                        nombreNuevoEI = map.getIntID()+".EX"+numex;
                        map.setIntID(nombreNuevoEI);
                    }
                }
            }
            addFormattedIntrusion(map, isARepeatedFormattedIntrusion, isAExistingFormattedIntrusion, sameIndi, existingIntrusionIndi);
            updateIndividuals();
            return isARepeatedFormattedIntrusion;
        }
     }
    
    boolean ExecuteResponse(String respuesta, ResponseActionParams params){
        return this.MCER.BuildResponseActionRequest(respuesta, params);        
    }
    
    void getOptimumResponses(){
        Resource intrusionOptim = inferedModel.getResource(PREFIXIND + this.ejemplarIntrusionNuevo);
        Individual formattedIntrusionOptim = inferedModel.getIndividual(PREFIXIND + intrusionOptim.getLocalName());
        System.out.println("*** FormattedIntrusion: " + formattedIntrusionOptim.getURI() + " ***");

        System.out.println("Calculando las respuestas optimas de la alerta de intrusión:" + formattedIntrusionOptim);
        Property prop_optimum_resp= inferedModel.getProperty(PREFIXIND+ "optimumResponse");
        formattedIntrusionOptim.removeAll(prop_optimum_resp);
        Iterator otpim_responses = (Iterator) formattedIntrusionOptim.listPropertyValues(prop_optimum_resp);
        int z=0;
        while(otpim_responses.hasNext()){
            z++;
            Resource res_optimum_resp = (Resource) otpim_responses.next();
            if (res_optimum_resp!=null){
                System.out.println("RESPUESTA OPTIMA: "+ res_optimum_resp.getLocalName());
                 try{
                     
                    Property prop_optim_resp = inferedModel.getProperty (PREFIXIND + "responseAction");
                    //Est primera linea es la que tengo que poner
                    //Individual res_optimum_resp = inferedResponseModel.getIndividual( PREFIX + res_optimum_resp.getLocalName());
                    Individual Ind_optimum_resp = ontologyModel.getIndividual( PREFIXIND + res_optimum_resp.getLocalName());
                    String a = Ind_optimum_resp.getPropertyValue(prop_optim_resp).toString();
                    int i1 = a.indexOf("^");
                    String accion = a.substring(0, i1);
                    System.out.println("Accion: " + accion);
                    ResponseActionParams params = new ResponseActionParams(hids,"ALL", alertmap.getIntType(), alertmap.getSourceIP(), alertmap.getIntrusionTarget().getAddressIP(),protocolo, Integer.toString(alertmap.getPortDest()), user, "hola");
                    if (ExecuteResponse (accion, params)){
                        System.out.println("RESPUESTA EJECUTADA!!");
                        
                        getSuccess(alertmap, listContextAnomalyIndicator, accion, null);
                    }else System.out.println("No ha sido posible ejecutar la respuesta");
                    
                }
                catch(Exception e){
                    System.out.println("grrrrrrrrrr: " +e);
                }
            }
            else {
                System.out.println(formattedIntrusionOptim.getLocalName() + " "+ prop_optimum_resp.getLocalName()+": null");
            }
        }

    }
    
    void getRecommendedResponses(){
        
        Resource intrusion = inferedModel.getResource(PREFIXIND + this.ejemplarIntrusionNuevo);
        Individual formattedIntrusion = inferedModel.getIndividual(PREFIXIND + intrusion.getLocalName());
      
        Individual networkContext = inferedModel.getIndividual(PREFIXIND + inferedModel.getResource(PREFIXIND+ejemplarContextoRedNuevo));
        Resource systContexto = inferedModel.getResource(PREFIXIND + ejemplarContextoSistemaNuevo);
        Individual systemContext = inferedModel.getIndividual(PREFIXIND + systContexto.getLocalName());
       
        //OBTENEMOS EL TIPO DE INTRUSION QUE INDICA EL CONTEXTO
        Property prop_indicates= inferedModel.getObjectProperty(PREFIXIND + "indicates");
        Iterator indicates = (Iterator) systemContext.listPropertyValues(prop_indicates);
        int con=0;
        while(indicates.hasNext()){
            con++;
            Resource res_indicates = (Resource) indicates.next();
            if (res_indicates!=null){
                System.out.println(Thread.currentThread() + "Contexto de red y sistemas indica este tipo de intrusión: "+ res_indicates.getLocalName());
            }
            else {
                System.out.println(intrusion.getLocalName() + " "+ prop_indicates.getLocalName()+": null");
            }
        }       

         OntClass class_IRS = inferedModel.getOntClass(PREFIXIND+"AutomatedIntrusionResponseSystem");
       
        // Se obtienen los ejemplares:
         Iterator instances_IRS = class_IRS.listInstances();
         int j = 0;
         // Se cuenta para cada ejemplar de IntrusionResponseSystem el número de Resultados generados.
         System.out.println("Extrayendo resultados generados....");
         while (instances_IRS.hasNext())
         {
            j++;
            Individual IRS = (Individual)instances_IRS.next();
            Resource res_Result = inferedModel.getResource(PREFIXIND + "Result");
            OntClass class_Result = res_Result.as(OntClass.class);
            Iterator generated_Result= class_Result.listInstances();
            int v=0;
            //System.out.println(generated_Result.toString());
            while(generated_Result.hasNext()){
                v++;
                Resource rr = (Resource) generated_Result.next();                       
            }
            //v++;
            System.out.println("numero de resultados generados "+v);
            Property numResp = inferedModel.getProperty(PREFIXIND+"numberOfGeneratedResult");
            
            IRS.removeAll(numResp);
            Literal numberResult = inferedModel.createTypedLiteral(v);
            IRS.setPropertyValue(numResp, numberResult);
        }
        Property existing_result = inferedModel.getObjectProperty(PREFIXIND+"hasSimilarResult");
        int n=0;
        Iterator ex_result_it = (Iterator) formattedIntrusion.listPropertyValues(existing_result);
        while(ex_result_it.hasNext()){
                        n++;
                        Resource rr = (Resource) ex_result_it.next();

        }
        //v++;
        System.out.println("numero de resultados similares "+n);

        Property numResp = inferedModel.getProperty(PREFIXIND+"existingResult");
        
        if(n!=0){
            formattedIntrusion.removeAll(numResp);
        }

        Literal numberResult = inferedModel.createTypedLiteral(n);
        formattedIntrusion.setPropertyValue(numResp, numberResult);
        Property nri = inferedModel.getProperty(PREFIXIND+"neededRecommendedInference");
        Object re = formattedIntrusion.getPropertyValue(nri);
        if(re==null){
            Literal nrit = inferedModel.createTypedLiteral(true);
            formattedIntrusion.setPropertyValue(nri, nrit);
        }else{
            formattedIntrusion.removeAll(nri);
            RDFNode renode= (RDFNode) re;
            formattedIntrusion.setPropertyValue(nri, renode);
        }

        System.out.println(Thread.currentThread() + "intrusion: " + formattedIntrusion.toString()+" -> Propiedad neededRecommendedInference: "+ formattedIntrusion.getPropertyValue(nri));
        
        //Extraemos el número de respuestas recomendadas

        System.out.println("Calculando las respuestas recomendadas de la alerta de intrusión:" + formattedIntrusion);
        Property prop_recommended_resp= inferedModel.getProperty(PREFIXIND+ "recommendedResponses");
        Iterator recom_responses = (Iterator) formattedIntrusion.listPropertyValues(prop_recommended_resp);
        int z=0;
        while(recom_responses.hasNext()){
            z++;
            Resource res_recommended_resp = (Resource) recom_responses.next();
            if (res_recommended_resp!=null){
                System.out.println(Thread.currentThread() + "intrusion: " + formattedIntrusion.toString()+"-> RESPUESTA RECOMENDADA: "+ res_recommended_resp.getLocalName());
            }
            else {
                System.out.println(intrusion.getLocalName() + " "+ prop_recommended_resp.getLocalName()+": null");
            }

        }
        System.out.println("numero de respuestas recomendadas "+z);
        Property numRespRecom = inferedModel.getProperty(PREFIXIND+"numberOfRecommendedResponses");
        formattedIntrusion.removeAll(numRespRecom);

        Literal numberResponses = inferedModel.createTypedLiteral(z);
        formattedIntrusion.setPropertyValue(numRespRecom, numberResponses);

        Property iar = inferedModel.getProperty(PREFIXIND+"intrusionAlertReliability");

        System.out.println(formattedIntrusion.getURI()+ "->INTRUSION ALERT RELIABILITY "+formattedIntrusion.getPropertyValue(iar));
        System.out.println("Calculando las respuestas potenciales optimas de la alerta de intrusión:" + formattedIntrusion);
        Property prop_por_resp= inferedModel.getProperty(PREFIXIND+ "potentialOptimumResponses");
        Iterator por_responses = (Iterator) formattedIntrusion.listPropertyValues(prop_por_resp);
        int ar=0;

        while(por_responses.hasNext()){
            ar++;
            Resource res_por_resp = (Resource) por_responses.next();
            if (res_por_resp!=null){
                System.out.println(Thread.currentThread() + "intrusion: " + formattedIntrusion.toString()+"-> RESPUESTA OPTIMA POTENCIAL: "+ res_por_resp.getLocalName());
            }
            else {

                System.out.println(intrusion.getLocalName() + " "+ prop_por_resp.getLocalName()+": null");
            }

        }
        System.out.println("numero de respuestas optimas potenciales "+ar);
        Property numRespPO = inferedModel.getProperty(PREFIXIND+"numberOfPotentialOptimumResponses");
        formattedIntrusion.removeAll(numRespPO);
        Literal numberResponsesPotential = inferedModel.createTypedLiteral(ar);
        formattedIntrusion.setPropertyValue(numRespPO, numberResponsesPotential);

     }    
      
    public void getSuccess(IntrusionAlert alertMap, ContextAnomalyIndicatorList cailist, String responseID, String responseType){
        Iterator context_it= cailist.getContextAnomalyIndicatorList().iterator();
        HashMap intAnomaly = new HashMap();
        while(context_it.hasNext()){
            ContextAnomalyIndicator caiindi = (ContextAnomalyIndicator) context_it.next();
            intAnomaly.put(caiindi.getIndicatorName(),caiindi.getIndicatorValue());
        }      
    
        para = new ExecutionModeParams(responseID, responseType, alertMap.getIntType(), alertmap.getIntrusionTarget().getAddressIP(), intAnomaly, initAdParam);

        EvaluationSystemModeSelector sel = new EvaluationSystemModeSelector("execution", para);
        if(sel.start()){
           ResponseTotalEfficiency resp = sel.getExecutionModeResult();
           String rid = resp.getResponseID();
           double rteValue = resp.getResponseEfficiency();
           int num_exe_value = resp.getNumExecutions();
           System.out.println("responseID: "+rid+ " --> responseEfficiency: "+rteValue + " --> numero de inferencias: " +
             num_exe_value+".");
        }
        else {
            System.out.println("error calcu exito");
        }
    }
    
    public void inicializarModelos() {
        
        System.out.println("______________________________________________");
        System.out.println(Thread.currentThread()+" inicializarModelos- method");

        //Se carga la ontología con Jena, cargando el modelo inferedResponseModel, donde se actualiza la información relacionada con las alertas de intrusion recibidas y las respuestas recomendadas.
        System.out.println("Leyendo la ontología ...");
        timebeforeCarga = System.currentTimeMillis ();
        synchronized (ontology_instances) {
            ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = ontologyModel.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(ontologyModel, ontology_instances);
        }
        timeafterCarga = System.currentTimeMillis ();
        System.out.println("Ontología Leida");
        System.out.println("______________________________________________");
    }

    /**
     * method inferRecommendedResponses()
     * método que infiere las repuestas recomendadas de la intrusión recibida
     * @param args
     */
    void inferRecommendedResponses(){
        System.out.println("____________________________________________________________________________________________");
        System.out.println("inferRecommendedResponses- method");
        long timeRecommendedInicial;
        long timeRecommendedFinal;
        timeRecommendedInicial = System.currentTimeMillis();
        getRecommendedResponses();
        timeRecommendedFinal = System.currentTimeMillis();
        System.out.println("Respuestas recomendadas de la intrusión recibida inferidas y actualizadas con éxito.");
        System.out.println("Tiempo tardado en inferir las respuestas recomendadas: "+ (timeRecommendedFinal - timeRecommendedInicial ) + " mseg");
        System.out.println("____________________________________________________________________________________________");
    }
    
    void inferOptimumResponses(){
        System.out.println("____________________________________________________________________________________________");
        System.out.println("inferOptimumResponses- method");
        long timeOptimumInicial;
        long timeOptimumFinal;
        timeOptimumInicial = System.currentTimeMillis();
        getOptimumResponses();
        timeOptimumFinal = System.currentTimeMillis();
        System.out.println("Respuestas proactivas de la intrusión recibida inferidas y actualizadas con éxito.");
        System.out.println("Tiempo tardado en inferir la respuesta optima: "+ (timeOptimumFinal - timeOptimumInicial ) + " mseg");
        System.out.println("____________________________________________________________________________________________");


    }
        
    void reasonerPellet(){
                //reasonedModel = inferedResponseModel;
        
        timebeforeRazonamiento = System.currentTimeMillis();
        
        inferedModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, null );
        inferedModel.read(ontology_rules);
        inferedModel.add(ontologyModel);
        
        inferedModel.prepare();
       
        timeafterCarga = System.currentTimeMillis ();
        timeafterRazonamiento = System.currentTimeMillis();

        System.out.println("*************************TIEMPOS*************************");
        System.out.println("Tiempo carga ontologia: "+ (timeafterCarga - timebeforeCarga));
        System.out.println("Tiempo razonamiento ontologia: "+ (timeafterRazonamiento - timebeforeRazonamiento));

        System.out.println("*****************************");

    }
    
    void updateIndividuals(){
        synchronized(ontology_infered_instances){
            System.out.println("____________________________________________________________________________________________");
            System.out.println( Thread.currentThread() + "-------OntAIRReasoner - updateIndividuals- method-----------");

            OntModel current_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = current_model.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(current_model, ontology_infered_instances);
            try {
                System.out.println("COMPROBANDO SI HAY NUEVOS INDIVIDUOS ACTUALES");
                OntModel aux = this.ontologyModel;
                current_model.add(aux);
                OutputStream out = new FileOutputStream(ontology_infered_instances_file);
                current_model.write(out);
                out.close();
            } catch (Exception e) {
                System.out.println("Error al actualizar el fichero");
            }
            System.out.println("actualizacion realizada con exito");      
        }
    }
    
    synchronized void writeFile(){
        synchronized (ontology_infered_instances) {
            
            try{
                    ModelExtractor extractor = new ModelExtractor(inferedModel);
                    System.out.println(Thread.currentThread());
                    Model inferences = extractor.extractModel();
                    OntModel current_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
                    RDFReader inf_modelReader = current_model.getReader();
                    inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
                    inf_modelReader.read(current_model, ontology_infered_instances);
                    inferences.add(current_model);
                    OutputStream out = new FileOutputStream(ontology_infered_instances_file);
                    OutputStream out2 = new FileOutputStream(ontology_infered_file);
                    System.out.println("Escribiendo en fichero; soy la hebra" + Thread.currentThread() );
                    inferences.write(out);
                    ontologyModel.write(out2);
                    out.close();
                    out2.close();
                    //out2.close();

            }
            catch(Exception e){
                    System.out.println(e);
            }
  
    }}   
}