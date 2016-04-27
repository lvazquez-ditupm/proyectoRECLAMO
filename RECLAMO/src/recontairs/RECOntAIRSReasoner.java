/**
 * "RECOntAIRSReasoner" Java class is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version always keeping the additional terms
 * specified in this license.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *
 * Additional Terms of this License --------------------------------
 *
 * 1. It is Required the preservation of specified reasonable legal notices and
 * author attributions in that material and in the Appropriate Legal Notices
 * displayed by works containing it.
 *
 * 2. It is limited the use for publicity purposes of names of licensors or
 * authors of the material.
 *
 * 3. It is Required indemnification of licensors and authors of that material
 * by anyone who conveys the material (or modified versions of it) with
 * contractual assumptions of liability to the recipient, for any liability that
 * these contractual assumptions directly impose on those licensors and authors.
 *
 * 4. It is Prohibited misrepresentation of the origin of that material, and it
 * is required that modified versions of such material be marked in reasonable
 * ways as different from the original version.
 *
 * 5. It is Declined to grant rights under trademark law for use of some trade
 * names, trademarks, or service marks.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (lgpl.txt). If not, see
 * <http://www.gnu.org/licenses/>
 */
package recontairs;

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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import context.entropy.variance.ContextAnomalyIndicator;
import context.entropy.variance.ContextAnomalyIndicatorList;
import evaluation.system.executor.ResponseTotalEfficiency;
import evaluation.system.mode.selector.EvaluationSystemModeSelector;
import evaluation.system.mode.selector.ExecutionModeParams;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import recontairs.utils.PropsUtil;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import recontairs.DAO.BDManagerIF;
import recontairs.DAO.DataManagerFactory;
import recontairs.utils.DateToXsdDatetimeFormatter;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;
import recparser.Address;
import recparser.IntrusionAlert;
import recparser.IntrusionSource;
import recparser.IntrusionTarget;

/**
 * This class represents the semantic reasoner of an AIRS based on ontologies.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class RECOntAIRSReasoner {

    private static PropsUtil props = new PropsUtil();
    private static Logger _log = Logger.getLogger(RECOntAIRSReasoner.class.getName());

    /*URI de las cuatro ontologías utilizadas */
    private final String ontology_rules_uri = props.getOntAIRSOntologyRulesNamespaceValue();
    private final String ontology_airs_uri = props.getOntAIRSOntologyAirsNamespaceValue();
    private final String ontology_intrusion_alert_uri = props.getOntAIRSOntologyIntrusionAlertNamespaceValue();
    private final String ontology_result = props.getOntAIRSOntologyResultNamespaceValue();
    private final static String ontology_assessed_alert_uri = props.getOntAIRSOntologyAssessedAlertNamespaceValue();

    /*NAMESPACES DE LAS TRES ONTOLOGÍAS*/
    private final String INTRUSIONALERTNAMESPACE = props.getOntAIRSOntologyIntrusionAlertNamespaceValue() + "#";
    private final String AIRSNAMESPACE = props.getOntAIRSOntologyAirsNamespaceValue() + "#";
    private final String RESULTNAMESPACE = props.getOntAIRSOntologyResultNamespaceValue() + "#";
    //private final String INTRUSIONALERTNAMESPACE = "http://www.dit.upm.es/~reclamo/ontologies/RECIntrusionAlert.owl#";
    //private final String AIRSNAMESPACE = "http://www.dit.upm.es/~reclamo/ontologies/RECAIRS.owl#";
    //private final String RESULTNAMESPACE = "http://www.dit.upm.es/~reclamo/ontologies/RECAIRSResult.owl#";

    /*WINDOWS: PATH a los ficheros de las 3 ontologías */
 /* private String ontology_airs_file="C:\\Users\\Vero\\Desktop\\test\\RECAIRStest.owl";
     private String ontology_assessed_alert_file="C:\\Users\\Vero\\Desktop\\test\\RECIntrusionAlertInstances.owl";
     private String ontology_result_file="C:\\Users\\Vero\\Desktop\\test\\RECAIRSResultTest.owl";ontology_assessed_alert_file
     */
 /*LINUX: PATH a los ficheros de las 3 ontologías */
    private final String ontology_airs_file = props.getOntAIRSOntologyAirsFileValue();
    private final String ontology_assessed_alert_file = props.getOntAIRSOntologyAssessedAlertFileValue();
    private final String inferred_file = props.getInferredFile();
    private final String inferred_uri = props.getInferredURI();
    //private final String ontology_result_file = props.getOntAIRSOntologyResultFileValue();

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
    private CentralModuleExecution MCER;
    private InferredInformationList _infeInfoList;
    List success_level_values_entropy = new ArrayList();
    List success_level_values_neural_net = new ArrayList();

    private static final int ASSET_LEVEL_OF_IMPORTANCE_HIGH_VALUE = 10;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_MEDIUM_VALUE = 5;
    private static final int ASSET_LEVEL_OF_IMPORTANCE_LOW_VALUE = 1;

    /*
     * Creamos un ontology model, extensio de Jena RDF que proporciona habilidades extra para el tratamiento de fuentes de datos ontologicos.
     * El ontology model se crea a traves de Jena ModelFactory. En este caso, el lenguaje usado es OWL.
     * El modelo creado es un modelo para almacenar todos los datos que se pasarán al motor de inferencia: CLases y ejemplares del IRS,
     * y reglas SWRL, así como para almacenar la información sobre las respuestas recomendadas y óptimas inferidas.
     */
    private OntModel ontologyModel;
    private OntModel alert_intrusion_model;
    private Individual resNuevo;
    private OntModel inferedModel;
    private OntModel inferedIndividuosModel;
    private boolean isARepeatedFormattedIntrusion = false;
    private boolean isAExistingFormattedIntrusion = false;
    static ExecutionModeParams para;
    String[] initAdParam;

    public RECOntAIRSReasoner(IntrusionAlert alertMap, CentralModuleExecution exec) {

        this.alertmap = alertMap;
        this.MCER = exec;
        //this.IntrusionAnomaly = new HashMap();
        this._infeInfoList = new InferredInformationList();
    }

    /**
     * addFormattedIntrusion añade un ejemplar de la clase FormattedIntrusion de
     * la ontologia. Añade el dispositivo como propiedad de IDS "generate".
     *
     * @param args: String attackerLocation, int intrusionImpact, String
     * attackerType, String IDSID, String sourceOfIntrusionType, int
     * intrusionSeverity, String intrusionStartTime, String targetOfIntrusionID,
     * String intrusionType, String targetOfIntrusionType, String
     * intrusionConsequence, String intrusionDetectionTime, String
     * intrusionEndingTime, String nextIntrusionID, String intrusionID
     */
    void addFormattedIntrusion(IntrusionAlert map, boolean same, boolean existing, ArrayList sameIndi, ArrayList existingIntrusionIndi) {
        long startTime = System.currentTimeMillis();
        System.out.println(Thread.currentThread() + " **** INIT AddFormattedIntrusion: " + alertmap.getIntID() + " ****");
        this.alertmap = map;
        String status = "Pending";
        if (alertmap.getIntID() == null) {
            return;
        }

        String intrusionNueva = alertmap.getIntID();
        this.ejemplarIntrusionNuevo = intrusionNueva;
        System.out.println(this.ejemplarIntrusionNuevo);
        resNuevo = alert_intrusion_model.getOntClass(INTRUSIONALERTNAMESPACE + "RECIntrusionAlert").createIndividual(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo);
        //System.out.println("resNuevo: "+resNuevo);
        if (same) {
            status = "Complete";
            for (int j = 0; j < sameIndi.size(); j++) {
                Resource sameIndiRes = inferedIndividuosModel.getIndividual(sameIndi.get(j).toString());
                resNuevo.setSameAs(sameIndiRes);
            }
        }
        if (existing) {
            status = "Complete";
            for (int j = 0; j < existingIntrusionIndi.size(); j++) {
                Resource existingIndiRes = inferedIndividuosModel.getIndividual(existingIntrusionIndi.get(j).toString());
                Property isContinuationOf_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "isContinuationOf");
                resNuevo.addProperty(isContinuationOf_prop, existingIndiRes);
            }
        }

        /*Añadimos dataTypeProperties de la nueva instancia de la clase RECIntrusionAlert*/
        addIndividualDateTimeProperty(resNuevo, alertmap.getIntDetectionTime(), "detectTime", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        addIndividualProperty(resNuevo, alertmap.getNextIntID(), "nextIntrusionID", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        addIndividualProperty(resNuevo, alertmap.getIntName(), "description", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        addIndividualProperty(resNuevo, alertmap.getIntID(), "intrusionAlertID", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        addIndividualDateTimeProperty(resNuevo, alertmap.getIntAlertCreateTime(), "createTime", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        addIndividualDateTimeProperty(resNuevo, alertmap.getIntDetectionTime(), "analyzerTime", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        /* Creamos la instancia de la clase Classification
         * Añadimos los valores a los dataTypes correspondientes
         */
        Individual classification = alert_intrusion_model.getOntClass(INTRUSIONALERTNAMESPACE + "Classification").createIndividual(INTRUSIONALERTNAMESPACE + "Classification" + this.ejemplarIntrusionNuevo);
        addIndividualProperty(classification, alertmap.getIntName(), "text", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        addIndividualProperty(classification, this.ejemplarIntrusionNuevo + "Classification", "ident", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        /*Asociamos la instancia de la clase classification al nuevo ejemplar de RECIntrusionAlert*/
        Property hasClassification_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasClassification");
        resNuevo.addProperty(hasClassification_prop, classification);

        /* Añadimos los Targets del ataque
         * Cada Target puede tener un único Nodo y varios Servicios asociados.
         * Propiedades: hasTarget, hasNode, hasService
         */
        try {
            Iterator alert_target_it = alertmap.getIntrusionTarget().iterator();
            int i = 1;
            OntClass nodeclass = ontologyModel.getOntClass(AIRSNAMESPACE + "Node");
            List<Individual> target_Node_list = new ArrayList<Individual>();
            while (alert_target_it.hasNext()) {
                IntrusionTarget _intTarget = (IntrusionTarget) alert_target_it.next();
                Iterator address_target_it = _intTarget.getAddress().iterator();
                int portTarget = _intTarget.getServicePort();
                String portListTarget = _intTarget.getServiceListPort();
                List<Individual> target_Service_list = new ArrayList<Individual>();
                while (address_target_it.hasNext()) {
                    Address address = (Address) address_target_it.next();
                    String targetAddressIP = address.getAddress();
                    Iterator node_instances = nodeclass.listInstances();
                    while (node_instances.hasNext()) {
                        Individual componente = (Individual) node_instances.next();
                        Property node_address = ontologyModel.getObjectProperty(AIRSNAMESPACE + "hasAddress");
                        Iterator address_instances = componente.listPropertyValues(node_address);
                        int v = 0;
                        while (address_instances.hasNext()) {
                            v++;
                            Property ipaddress_prop = ontologyModel.getDatatypeProperty(AIRSNAMESPACE + "addressIP");
                            Resource rr = (Resource) address_instances.next();
                            Individual indiv = ontologyModel.getIndividual(AIRSNAMESPACE + rr.getLocalName());
                            String ipaddress_value_entero = indiv.getPropertyValue(ipaddress_prop).toString();
                            String ipaddress_value = ipaddress_value_entero.substring(0, ipaddress_value_entero.indexOf("^"));
                            if (ipaddress_value.equals(targetAddressIP)) {
                                boolean newNode = true;
                                for (int z = 0; i < target_Node_list.size(); z++) {
                                    Individual existingNode = target_Node_list.get(z);
                                    if (existingNode.isSameAs(componente.asResource())) {
                                        newNode = false;
                                    }
                                }
                                if (newNode) {
                                    Individual alert_target = alert_intrusion_model.getOntClass(INTRUSIONALERTNAMESPACE + "Target").createIndividual(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo + "Target" + i + v);
                                    Property hasNode_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasNode");
                                    alert_target.addProperty(hasNode_prop, componente);
                                    //System.out.println("Añadido Target: "+componente.toString());

                                    /*Añadimos la lista de Servicios asociada a dicho Target*/
                                    if (portTarget > 0) {
                                        List<Individual> service = getService(portTarget);
                                        Property hasService_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasService");
                                        Iterator service_it = service.iterator();
                                        while (service_it.hasNext()) {
                                            Individual se = (Individual) service_it.next();
                                            boolean newService = true;
                                            for (int s = 0; i < target_Service_list.size(); s++) {
                                                Individual existingService = target_Service_list.get(s);
                                                if (existingService.isSameAs(se.asResource())) {
                                                    newService = false;
                                                }
                                            }
                                            if (newService) {
                                                alert_target.addProperty(hasService_prop, se);
                                            }
                                        }
                                    }

                                    /*Añadimos la lista de Servicios asociada a dicho Target*/
                                    if (portListTarget != null) {
                                        List<Integer> portlist = _intTarget.getServiceListPortInt();
                                        for (int a = 0; a <= portlist.size(); a++) {
                                            int portTargetListElement = portlist.get(a);
                                            List<Individual> service = getService(portTargetListElement);
                                            Property hasService_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasService");
                                            Iterator service_it = service.iterator();
                                            while (service_it.hasNext()) {
                                                Individual se = (Individual) service_it.next();
                                                boolean newService = true;
                                                for (int s = 0; i < target_Service_list.size(); s++) {
                                                    Individual existingService = target_Service_list.get(s);
                                                    if (existingService.isSameAs(se.asResource())) {
                                                        newService = false;
                                                    }
                                                }
                                                if (newService) {
                                                    alert_target.addProperty(hasService_prop, se);
                                                }
                                            }
                                        }
                                    }

                                    Property hasTarget_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasTarget");
                                    resNuevo.addProperty(hasTarget_prop, alert_target);

                                }

                            }
                        }
                    }
                }

            }

        } // fin del try que añade el target del ataque
        catch (Exception e) {
            System.out.println(Thread.currentThread() + " Componente no encontrado en el sistema o error añadiendo objetivo");
            _log.log(Level.SEVERE, null, e);
        }

        /* Añadimos los Sources del ataque
         */
        try {
            Iterator alert_source_it = alertmap.getIntrusionSource().iterator();
            int i = 1;
            while (alert_source_it.hasNext()) {
                IntrusionSource _intSource = (IntrusionSource) alert_source_it.next();
                List<Address> address_list = _intSource.getNode();
                String sourceIP = address_list.get(0).getAddress();
                Individual alert_source = alert_intrusion_model.getOntClass(INTRUSIONALERTNAMESPACE + "ExternalSource").createIndividual(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo + "Source" + i);
                addIndividualProperty(alert_source, sourceIP, "addressIP", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
                int portsrc = _intSource.getPortSrc();
                if (portsrc >= 0) {
                    addIndividualProperty(alert_source, portsrc, "port", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
                }

                Property hasSource_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasSource");
                resNuevo.addProperty(hasSource_prop, alert_source);
            }

        } // fin del try que añade el source del ataque
        catch (Exception e) {
            System.out.println(Thread.currentThread() + " Componente no encontrado en el sistema o error añadiendo objetivo");
            _log.log(Level.SEVERE, null, e);
        }

        /* Creamos una instancia de la clase Assessment
         *  Añadimos los valores a las propiedades correspondientes
         */
        Individual assessment = alert_intrusion_model.getOntClass(INTRUSIONALERTNAMESPACE + "Assessment").createIndividual(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo + "Assessment");
        addIndividualProperty(assessment, alertmap.getIntSeverity(), "severity", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        addIndividualProperty(assessment, alertmap.getIntImpact(), "impact", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        if (alertmap.getAnalyzerConfidence() != 0.0) {
            addIndividualProperty(assessment, alertmap.getAnalyzerConfidence(), "intrusionAlertReliability", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        }
        addIndividualProperty(assessment, alertmap.getIntCompletion(), "intrusionCompletion", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        try {
            addIndividualProperty(assessment, status, "responseStatus", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        } catch (Exception e) {
            _log.log(Level.SEVERE, null, e);
        }
        String targetLevelOfImportance = getTotalTargetLevelOfImportance(alertmap);
        addIndividualProperty(assessment, targetLevelOfImportance, "targetLevelOfImportance", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        try {
            double realIntrusionImpact = alertmap.getIntImpact() + alertmap.getAnalyzerConfidence();
            System.out.println("real intrusion impact " + realIntrusionImpact);
            addIndividualProperty(assessment, realIntrusionImpact, "realIntrusionImpact", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        } catch (Exception e) {
            _log.log(Level.SEVERE, null, e);
        }

        /* Determinamos el tipo de intrusion */
        String intrusionTypeFI = alertmap.getIntType();
        try {
            // doy valor al tipo de itnrusión.
            if (intrusionTypeFI.equals("DenialOfService")) {
                intrusionTypeFI = "DoS";
            }
            intrusionType = intrusionTypeFI;
            Resource threat_res = ontologyModel.getResource(AIRSNAMESPACE + "SpecificThreat");
            OntClass threat_class = (OntClass) threat_res.as(OntClass.class);
            Iterator threat_subclass_it = threat_class.listSubClasses();
            while (threat_subclass_it.hasNext()) {
                OntClass threat_subclass = (OntClass) threat_subclass_it.next();
                if (intrusionTypeFI.equals(threat_subclass.getLocalName())) {
                    Iterator threat_indiv_it = threat_subclass.listInstances();
                    while (threat_indiv_it.hasNext()) {
                        Individual threat_indiv = (Individual) threat_indiv_it.next();
                        Property hasIntrusionType_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasIntrusionType");
                        assessment.addProperty(hasIntrusionType_prop, threat_indiv);
                    }
                }
            }
            Resource threat_unde_res = ontologyModel.getResource(AIRSNAMESPACE + "UndefinedThreat");
            OntClass threat_unde_class = (OntClass) threat_unde_res.as(OntClass.class);
            if ((intrusionTypeFI.equalsIgnoreCase("unknown")) || (intrusionTypeFI.equalsIgnoreCase("other"))) {
                Iterator threat_unde_indiv_it = threat_unde_class.listInstances();
                while (threat_unde_indiv_it.hasNext()) {
                    Individual threat_unde_indiv = (Individual) threat_unde_indiv_it.next();
                    Property hasIntrusionType_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasIntrusionType");
                    assessment.addProperty(hasIntrusionType_prop, threat_unde_indiv);
                }
            }

        } catch (Exception e) {
            _log.log(Level.SEVERE, null, e);
        } // fin de añadir tipo de intrusion detectada

        addIndividualProperty(assessment, 0, "numberOfRecommendedResponses", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        addIndividualProperty(assessment, 0, "numberOfPotentialOptimumResponses", alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        Property hasAssessment_prop = alert_intrusion_model.getObjectProperty(INTRUSIONALERTNAMESPACE + "hasAssessment");
        resNuevo.addProperty(hasAssessment_prop, assessment);
        addIndividualProperty(resNuevo, same, "isARepeatedFormattedIntrusion", alert_intrusion_model, INTRUSIONALERTNAMESPACE);

        /* Se añade la alerta recibida como nuevo ejemplar de la propiedad
         * "generate" de la instancia de la clase IDS cuyo IDSID es el incluido en la alerta
         */
        Resource r = ontologyModel.getResource(AIRSNAMESPACE + "Analyzer");

        OntClass class_IDS = (OntClass) r.as(OntClass.class);
        String detecSystemID;
        String formattedIntruIDSID = alertmap.getAnalyzerID();
        Iterator IDS_instances = class_IDS.listInstances();
        int numero = 0;
        Resource resFormattedIntrusion = alert_intrusion_model.getResource(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo);

        while (IDS_instances.hasNext()) {

            Individual IDS = (Individual) IDS_instances.next();

            Property propIDSID = ontologyModel.getProperty(AIRSNAMESPACE + "analyzerid");
            //Obtenemos el valor de la propiedad IDSID del ejemplar de IntrusionDetectionSystem.
            detecSystemID = IDS.getPropertyValue(propIDSID).toString();
            detecSystemID = detecSystemID.substring(0, detecSystemID.indexOf("^"));
            if (detecSystemID.equals(formattedIntruIDSID)) {
                Property generateIDS = ontologyModel.getProperty(AIRSNAMESPACE + "generates");
                IDS.addProperty(generateIDS, resFormattedIntrusion);
                resFormattedIntrusion.addProperty(alert_intrusion_model.getProperty(INTRUSIONALERTNAMESPACE + "isGeneratedBy"), IDS);
                /*    if(IDSconfidence!=null){
                 Literal confidence = ontologyModel.createTypedLiteral(IDSconfidence);
                 Property propIDS = ontologyModel.getProperty(PREFIXIND + "IDSconfidence");
                 IDS.setPropertyValue(propIDS, confidence);
                 }*/
            }
            numero++;
        } // fin de añadir formattedIntrusionID a generates de IDS

        /* Se añade la alerta recibida como nuevo ejemplar de la propiedad
         * "receivedFormattedIntrusion" de la instancia de la clase IntrusionResponseSystem
         */
        Resource res_IRS = ontologyModel.getResource(AIRSNAMESPACE + "AutomatedIntrusionResponseSystem");
        OntClass class_IRS = (OntClass) res_IRS.as(OntClass.class);
        Iterator IRS_instances = class_IRS.listInstances();
        while (IRS_instances.hasNext()) {
            Individual IRS = (Individual) IRS_instances.next();
            Property receivedIntrusion = ontologyModel.getProperty(AIRSNAMESPACE + "receivedFormattedIntrusion");
            Resource resFormattedIntrus = alert_intrusion_model.getResource(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo);
            IRS.addProperty(receivedIntrusion, resFormattedIntrus);
        }
        long endTime = System.currentTimeMillis();
        System.out.println(Thread.currentThread() + " END AddFormattedIntrusion: " + this.ejemplarIntrusionNuevo + " *** Total time: " + (endTime - startTime) + " (ms)****");
        System.out.println("________________________________________________________________________");
    }

    private List<Individual> getService(int servicePort) {
        OntClass serviceclass = ontologyModel.getOntClass(AIRSNAMESPACE + "Service");
        Iterator service_instances = serviceclass.listInstances();
        int v = 0;
        List<Individual> service_list = new ArrayList<Individual>();
        while (service_instances.hasNext()) {
            v++;
            Property port_prop = ontologyModel.getDatatypeProperty(AIRSNAMESPACE + "port");
            Resource rr = (Resource) service_instances.next();
            Individual service = ontologyModel.getIndividual(AIRSNAMESPACE + rr.getLocalName());
            String port_value_entero = service.getPropertyValue(port_prop).toString();
            int port_value = Integer.parseInt(port_value_entero.substring(0, port_value_entero.indexOf("^")));
            if (port_value == servicePort) {
                service_list.add(service);
            }
        }
        return service_list;

    }

    private void addIndividualProperty(Individual individual, Object value, String propName, OntModel model, String PREFIX) {
        //System.out.println("Añadiendo propiedad: "+propName);

        if (value instanceof String) {
            String valueprop = (String) value;
            if (valueprop != null) {
                Literal indi_prop_literal = model.createTypedLiteral(valueprop);
                Property prop = model.getProperty(PREFIX + propName);
                individual.setPropertyValue(prop, indi_prop_literal);
            }
        } else if (value instanceof Integer) {
            int valueprop = (Integer) value;
            if (valueprop >= 0) {
                Literal indi_prop_literal = model.createTypedLiteral(valueprop);
                Property prop = model.getProperty(PREFIX + propName);
                individual.setPropertyValue(prop, indi_prop_literal);
            }
        } else if (value instanceof Double) {
            double valueprop = (Double) value;
            if (valueprop >= 0.0) {
                Literal indi_prop_literal = model.createTypedLiteral(valueprop);
                Property prop = model.getProperty(PREFIX + propName);
                individual.setPropertyValue(prop, indi_prop_literal);
            } else {
                valueprop = 0.0;
                Literal indi_prop_literal = model.createTypedLiteral(valueprop);
                Property prop = model.getProperty(PREFIX + propName);
                individual.setPropertyValue(prop, indi_prop_literal);
            }
        } else if (value instanceof Boolean) {
            boolean valueprop = (Boolean) value;
            Literal indi_prop_literal = model.createTypedLiteral(valueprop);
            Property prop = model.getProperty(PREFIX + propName);
            individual.setPropertyValue(prop, indi_prop_literal);
        }
    }

    private void addIndividualDateTimeProperty(Individual individual, Object value, String propName, OntModel model, String prefix) {
        String valueprop = (String) value;
        if (valueprop != null) {
            Literal indi_prop_literal = model.createTypedLiteral(valueprop, "http://www.w3.org/2001/XMLSchema#dateTime");
            Property prop = model.getProperty(prefix + propName);
            individual.setPropertyValue(prop, indi_prop_literal);
        }
    }

    void addContextAnomaly(String ide, String hostname, String IP, String subnetwork, String contextInfDate, int anomaly, SystemAnomaly sys_anomaly) {
        long startTime = System.currentTimeMillis();
        System.out.println(Thread.currentThread() + " INIT AddContextAnomaly: " + hostname + "-" + subnetwork + " ****");
        ContextAnomalyIndicatorList listContextAnomalyIndicator = new ContextAnomalyIndicatorList();
        addNetworkContext(ide, anomaly, contextInfDate, listContextAnomalyIndicator);
        addSystemContext(ide, sys_anomaly, contextInfDate, listContextAnomalyIndicator);
        addAnomalyIndicator(IP, hostname, subnetwork, listContextAnomalyIndicator);

        long endTime = System.currentTimeMillis();
        System.out.println(Thread.currentThread() + " END AddContextAnomaly: " + hostname + "-" + subnetwork + " *** Total time: " + (endTime - startTime) + " (ms)****");
        System.out.println("________________________________________________________________________");
    }

    private void addNetworkContext(String ide, int anomaly, String contextInfDate, ContextAnomalyIndicatorList indi) {

        ContextAnomalyIndicator context_net = new ContextAnomalyIndicator("Network", anomaly);
        indi.addContextAnomalyIndicator(context_net);
        OntClass class_NetworkContext = ontologyModel.getOntClass(AIRSNAMESPACE + "NetworkContextAnomaly");
        String idnc = "network" + ide;
        Individual netContextNuevo = ontologyModel.createIndividual(AIRSNAMESPACE + idnc, class_NetworkContext);

        ejemplarContextoRedNuevo = idnc;

        if (Integer.toString(anomaly) != null) {
            addIndividualProperty(netContextNuevo, anomaly, "networkAnomaly", ontologyModel, AIRSNAMESPACE);

        }
        if (contextInfDate != null) {
            addIndividualDateTimeProperty(netContextNuevo, contextInfDate, "contextTime", ontologyModel, AIRSNAMESPACE);

        }
        //inferedResponseModel =reasonedModel;
    }

    /**
     * addSystemContext aÃ±ade un ejemplar de la clase SystemContext de la
     * ontologia
     *
     * @param args: int systemActiveProcesses, int systemCPUUsage, int
     * systemFreeSpace, int systemLatency, int systemNumberOfUsersLogged, int
     * systemStatus, systemSSHFailed
     */
    private void addSystemContext(String ide, SystemAnomaly sys_anomaly, String contextInfDate, ContextAnomalyIndicatorList indiList) {
        //System.out.println("____________________________________________________________________________________________");
        //System.out.println("metodo addSystemContext");

        OntClass class_SystemContext = ontologyModel.getOntClass(AIRSNAMESPACE + "SystemContextAnomaly");
        String idnc = "system" + ide;
        Individual sysContextNuevo = ontologyModel.createIndividual(AIRSNAMESPACE + idnc, class_SystemContext);

        ejemplarContextoSistemaNuevo = idnc;

        if (Integer.toString(sys_anomaly.getProcesosA()) != null) {
            addIndividualProperty(sysContextNuevo, sys_anomaly.getProcesosA(), "systemActiveProcesses", ontologyModel, AIRSNAMESPACE);
            ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator("Process", sys_anomaly.getProcesosA());
            indiList.addContextAnomalyIndicator(context_sys);
        }
        if (Integer.toString(sys_anomaly.getCPUA()) != null) {
            addIndividualProperty(sysContextNuevo, sys_anomaly.getCPUA(), "systemCPUUsage", ontologyModel, AIRSNAMESPACE);
            ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator("CPU", sys_anomaly.getCPUA());
            indiList.addContextAnomalyIndicator(context_sys);
        }
        if (Integer.toString(sys_anomaly.getDiscoduroA()) != null) {
            addIndividualProperty(sysContextNuevo, sys_anomaly.getDiscoduroA(), "systemFreeSpace", ontologyModel, AIRSNAMESPACE);
            ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator("Disk", sys_anomaly.getDiscoduroA());
            indiList.addContextAnomalyIndicator(context_sys);
        }
        if (Integer.toString(sys_anomaly.getLatenciaA()) != null) {
            addIndividualProperty(sysContextNuevo, sys_anomaly.getLatenciaA(), "systemLatency", ontologyModel, AIRSNAMESPACE);
            ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator("Latency", sys_anomaly.getLatenciaA());
            indiList.addContextAnomalyIndicator(context_sys);
        }
        if (Integer.toString(sys_anomaly.getUsuariosA()) != null) {
            addIndividualProperty(sysContextNuevo, sys_anomaly.getUsuariosA(), "systemNumberOfUsersLogged", ontologyModel, AIRSNAMESPACE);
            ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator("User", sys_anomaly.getUsuariosA());
            indiList.addContextAnomalyIndicator(context_sys);
        }
        if (Integer.toString(sys_anomaly.isEstadoA()) != null) {
            addIndividualProperty(sysContextNuevo, sys_anomaly.isEstadoA(), "systemStatus", ontologyModel, AIRSNAMESPACE);
            ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator("Status", sys_anomaly.isEstadoA());
            indiList.addContextAnomalyIndicator(context_sys);
        }
        if (Integer.toString(sys_anomaly.getSSHFailedA()) != null) {
            addIndividualProperty(sysContextNuevo, sys_anomaly.getSSHFailedA(), "systemSSHFailed", ontologyModel, AIRSNAMESPACE);
            ContextAnomalyIndicator context_sys = new ContextAnomalyIndicator("SSHFailed", sys_anomaly.getSSHFailedA());
            indiList.addContextAnomalyIndicator(context_sys);
        }
        if (contextInfDate != null) {
            addIndividualDateTimeProperty(sysContextNuevo, contextInfDate, "contextTime", ontologyModel, AIRSNAMESPACE);
        }

        ContextAnomalyIndicator context_sys_zombie = new ContextAnomalyIndicator("Zombie", sys_anomaly.getZombiesA());
        indiList.addContextAnomalyIndicator(context_sys_zombie);

        //System.out.println(Thread.currentThread()+"Contexto de sistemas introducido en el modelo: "+ejemplarContextoSistemaNuevo);
        //System.out.println("________________________________________________________________________");
    }

    void addAnomalyIndicator(String IP, String hostname, String subnetwork, ContextAnomalyIndicatorList listContextAnomalyIndicator) {
        String tloi = getTargetLevelOfImportance(IP);
        InferredInformation newInfo = new InferredInformation(IP, hostname, listContextAnomalyIndicator, subnetwork, null, null, tloi);
        System.out.println("AÑADIENDO INDICADOR DE ANOMALIA DE CONTEXTO: ");
        _infeInfoList.addInferredInformation(newInfo);
        _infeInfoList.printInferredInformationList();
    }

    synchronized boolean checkSimilarIntrusion2(IntrusionAlert map) {
        synchronized (ontology_assessed_alert_uri) {

            System.out.println("____________________________________________________________________________________________");
            System.out.println(Thread.currentThread() + "-------OntAIRReasoner - checkSimilarIntrusion- method-----------");

            ArrayList existingIntrusionIndi = new ArrayList();
            ArrayList sameIndi = new ArrayList();
            inferedIndividuosModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            inferedIndividuosModel.read(ontology_assessed_alert_uri);
            inferedIndividuosModel.add(ontologyModel);
            inferedIndividuosModel.prepare();

            Individual repeatedAlert = inferedIndividuosModel.getIndividual(INTRUSIONALERTNAMESPACE + map.getIntID());

            if (repeatedAlert != null) {
                isARepeatedFormattedIntrusion = true;
            } else {
                String sameAlertDate = null;
                String existingIntrusionDate = null;
                String alertType = map.getIntType();
                if (alertType.equals("DenialOfService")) {
                    alertType = "dos";
                }
                String alertName = map.getIntName();
                String sourceIP = null;
                Iterator alert_source_it = map.getIntrusionSource().iterator();
                while (alert_source_it.hasNext()) {
                    IntrusionSource _intSource = (IntrusionSource) alert_source_it.next();
                    List<Address> address_list = _intSource.getNode();
                    sourceIP = address_list.get(0).getAddress();
                }
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    long time = (simpleDateFormat.parse(map.getIntDetectionTime())).getTime();
                    long timesamealert = time - 2000;
                    long timeexistingalert = time - 5000;
                    DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
                    sameAlertDate = xdf.format(new Date(timesamealert));
                    existingIntrusionDate = xdf.format(new Date(timeexistingalert));

                } catch (Exception e) {
                }
                int numeroAddressDistintos = 0;
                try {
                    Iterator alert_target_it = map.getIntrusionTarget().iterator();
                    while (alert_target_it.hasNext()) {
                        IntrusionTarget _intTarget = (IntrusionTarget) alert_target_it.next();
                        Iterator address_target_it = _intTarget.getAddress().iterator();
                        while (address_target_it.hasNext()) {
                            numeroAddressDistintos++;
                            Address address = (Address) address_target_it.next();
                            String targetAddressIP = address.getAddress();

                            String sameAlert = "PREFIX RECAIRS: <" + AIRSNAMESPACE + ">"
                                    + " PREFIX RECIntrusionAlert: <" + INTRUSIONALERTNAMESPACE + ">"
                                    + " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                                    + " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                                    + " PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                                    + " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                                    + " SELECT DISTINCT ?formattedintrusion"
                                    + " WHERE {"
                                    + "?classification a RECIntrusionAlert:Classification."
                                    + "?classification RECIntrusionAlert:text ?classtext ."
                                    + "FILTER (?classtext = \"" + alertName + "\")."
                                    + "?node a RECAIRS:Node ."
                                    + "?target a RECIntrusionAlert:Target ."
                                    + "?target RECIntrusionAlert:hasNode ?node ."
                                    + "?node RECAIRS:hasAddress ?address ."
                                    + "?address RECAIRS:addressIP ?ip ."
                                    + "FILTER (?ip = \"" + targetAddressIP + "\")."
                                    + "?source a RECIntrusionAlert:ExternalSource ."
                                    + "?source RECIntrusionAlert:addressIP ?sourceip. "
                                    + "FILTER (?sourceip = \"" + sourceIP + "\")."
                                    + "?formattedintrusion RECIntrusionAlert:detectTime ?idt ."
                                    + "FILTER (xsd:dateTime(?idt) >= \"" + sameAlertDate + "\"^^xsd:dateTime) ."
                                    + "?formattedintrusion RECIntrusionAlert:hasClassification ?classification ;"
                                    + "RECIntrusionAlert:hasTarget ?target ;"
                                    + "RECIntrusionAlert:hasSource ?source ."
                                    + " } ";

                            Query qsisa = QueryFactory.create(sameAlert);
                            QueryExecution qesisa = QueryExecutionFactory.create(qsisa, inferedIndividuosModel);
                            ResultSet rssisa = qesisa.execSelect();
                            List<QuerySolution> listaSisa = ResultSetFormatter.toList(rssisa);
                            for (int j = 0; j < listaSisa.size(); j++) {
                                QuerySolution solution = listaSisa.get(j);
                                isARepeatedFormattedIntrusion = solution.contains("formattedintrusion");
                                Individual indsame = inferedIndividuosModel.getIndividual(solution.get("formattedintrusion").toString());
                                sameIndi.add(indsame);
                            }
                            if (!isARepeatedFormattedIntrusion) {
                                String existingAlert = "PREFIX RECAIRS: <" + AIRSNAMESPACE + ">"
                                        + " PREFIX RECIntrusionAlert: <" + INTRUSIONALERTNAMESPACE + ">"
                                        + " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                                        + " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                                        + " PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                                        + " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                                        + " SELECT DISTINCT ?formattedintrusion"
                                        + " WHERE {"
                                        + "?classification a RECIntrusionAlert:Classification."
                                        + "?classification RECIntrusionAlert:text ?classtext ."
                                        + "FILTER (?classtext = \"" + alertName + "\")."
                                        + "?node a RECAIRS:Node ."
                                        + "?target a RECIntrusionAlert:Target ."
                                        + "?target RECIntrusionAlert:hasNode ?node ."
                                        + "?node RECAIRS:hasAddress ?address ."
                                        + "?address RECAIRS:addressIP ?ip ."
                                        + "FILTER (?ip = \"" + targetAddressIP + "\")."
                                        + "?source a RECIntrusionAlert:ExternalSource ."
                                        + "?source RECIntrusionAlert:addressIP ?sourceip. "
                                        + "FILTER (?sourceip = \"" + sourceIP + "\")."
                                        + "?formattedintrusion RECIntrusionAlert:detectTime ?idt ."
                                        + "FILTER (xsd:dateTime(?idt) >= \"" + existingIntrusionDate + "\"^^xsd:dateTime) ."
                                        + "?formattedintrusion RECIntrusionAlert:hasClassification ?classification ;"
                                        + "RECIntrusionAlert:hasTarget ?target ;"
                                        + "RECIntrusionAlert:hasSource ?source ."
                                        + " } ";

                                Query qex = QueryFactory.create(existingAlert);
                                QueryExecution qeex = QueryExecutionFactory.create(qex, inferedIndividuosModel);
                                ResultSet rseex = qeex.execSelect();
                                List<QuerySolution> listaEex = ResultSetFormatter.toList(rseex);
                                for (int j = 0; j < listaEex.size(); j++) {
                                    QuerySolution solution = listaEex.get(j);
                                    isAExistingFormattedIntrusion = solution.contains("formattedintrusion");
                                    Individual indexisting = inferedIndividuosModel.getIndividual(solution.get("formattedintrusion").toString());
                                    existingIntrusionIndi.add(indexisting);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }

            }

            addFormattedIntrusion(map, isARepeatedFormattedIntrusion, isAExistingFormattedIntrusion, sameIndi, existingIntrusionIndi);
            updateIndividuals();
            return isARepeatedFormattedIntrusion;
        }

    }

    boolean checkSimilarIntrusion(IntrusionAlert map) {
        synchronized (ontology_assessed_alert_uri) {

            System.out.println("____________________________________________________________________________________________");
            System.out.println(Thread.currentThread() + "-------OntAIRReasoner - checkSimilarIntrusion- method-----------");
            String sameAlertDate = null;
            String existingIntrusionDate = null;
            String sourceIP = null;
            ArrayList existingIntrusionIndi = new ArrayList();
            ArrayList sameIndi = new ArrayList();
            Iterator alert_source_it = map.getIntrusionSource().iterator();
            while (alert_source_it.hasNext()) {
                IntrusionSource _intSource = (IntrusionSource) alert_source_it.next();
                List<Address> address_list = _intSource.getNode();
                sourceIP = address_list.get(0).getAddress();
            }
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                long time = (simpleDateFormat.parse(map.getIntDetectionTime())).getTime();
                //MIGUEL long timesamealert= time -2000;
                long timesamealert = time;
                //MIGUEL long timeexistingalert = time - 5000;
                long timeexistingalert = time;
                DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
                sameAlertDate = xdf.format(new Date(timesamealert));
                existingIntrusionDate = xdf.format(new Date(timeexistingalert));

            } catch (Exception e) {
            }

            HashMap sameIndiSameAnalyzerCounter = new HashMap();
            HashMap sameIndiDifAnalyzerCounter = new HashMap();
            inferedIndividuosModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            inferedIndividuosModel.read(ontology_assessed_alert_uri);
            int numeroAddressDistintos = 0;
            try {
                Iterator alert_target_it = map.getIntrusionTarget().iterator();

                while (alert_target_it.hasNext()) {
                    IntrusionTarget _intTarget = (IntrusionTarget) alert_target_it.next();
                    Iterator address_target_it = _intTarget.getAddress().iterator();
                    while (address_target_it.hasNext()) {
                        numeroAddressDistintos++;
                        Address address = (Address) address_target_it.next();
                        String targetAddressIP = address.getAddress();
                        String sameAlert_sameAnalyzer = "PREFIX RECAIRS: <" + AIRSNAMESPACE + ">"
                                + " PREFIX RECIntrusionAlert: <" + INTRUSIONALERTNAMESPACE + ">"
                                + " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                                + " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                                + " PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                                + " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                                + " SELECT DISTINCT ?formattedintrusion"
                                + " WHERE {"
                                + "?threat a RECAIRS:" + map.getIntType() + "."
                                + "?node a RECAIRS:Node ."
                                + "?classification a RECIntrusionAlert:Classification."
                                + "?classification RECIntrusionAlert:text ?classtext ."
                                + "FILTER (?classtext = \"" + map.getIntName() + "\")."
                                + "?analyzer a RECAIRS:Analyzer."
                                + "?analyzer RECAIRS:analyzerid ?analyzerid. "
                                + "FILTER (?analyzerid = \"" + map.getAnalyzerID() + "\")."
                                + "?assessment a RECIntrusionAlert:Assessment."
                                + "?assessment RECIntrusionAlert:hasIntrusionType ?threat."
                                + "?target a RECIntrusionAlert:Target ."
                                + "?target RECIntrusionAlert:hasNode ?node ."
                                + "?node RECAIRS:hasAddress ?address ."
                                + "?address RECAIRS:addressIP ?ip ."
                                + "FILTER (?ip = \"" + targetAddressIP + "\")."
                                + "?source a RECIntrusionAlert:Source ."
                                + "?source RECIntrusionAlert:addressIP ?sourceip. "
                                + "FILTER (?sourceip = \"" + sourceIP + "\")."
                                + "?formattedintrusion RECIntrusionAlert:detectTime ?idt ."
                                + "FILTER (xsd:dateTime(?idt) >= \"" + sameAlertDate + "\"^^xsd:dateTime) ."
                                + "?formattedintrusion RECIntrusionAlert:hasAssessment ?assessment ;"
                                + "RECIntrusionAlert:hasClassification ?classification ;"
                                + "RECIntrusionAlert:hasTarget ?target ;"
                                + "RECIntrusionAlert:hasSource ?source ;"
                                + "RECIntrusionAlert:isGeneratedBy ?analyzer ."
                                + " } ";

                        Query qsisa = QueryFactory.create(sameAlert_sameAnalyzer);
                        QueryExecution qesisa = QueryExecutionFactory.create(qsisa, inferedIndividuosModel);
                        ResultSet rssisa = qesisa.execSelect();
                        System.out.println(rssisa);
                        List<QuerySolution> listaSisa = ResultSetFormatter.toList(rssisa);
                        for (int j = 0; j < listaSisa.size(); j++) {
                            QuerySolution solution = listaSisa.get(j);
                            Individual indsame = inferedIndividuosModel.getIndividual(solution.get("formattedintrusion").toString());
                            int contador;
                            if (sameIndiSameAnalyzerCounter.containsKey(indsame)) {
                                contador = (Integer) sameIndiSameAnalyzerCounter.get(indsame) + 1;
                            } else {
                                contador = 1;
                            }
                            sameIndiSameAnalyzerCounter.put(indsame, contador);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            int numSameIndiSameAnalyzer = 0;
            boolean nombreRepetido = false;
            String nombreNuevo = null;
            Iterator it = sameIndiSameAnalyzerCounter.keySet().iterator();
            while (it.hasNext()) {
                Individual indi = (Individual) it.next();
                if ((Integer) sameIndiSameAnalyzerCounter.get(indi) == numeroAddressDistintos) {
                    sameIndi.add(indi);
                    if (indi.getLocalName().equals(map.getIntID())) {
                        nombreRepetido = true;
                    }
                    numSameIndiSameAnalyzer++;
                }
            }

            if (numSameIndiSameAnalyzer > 0) {
                isARepeatedFormattedIntrusion = true;
                if (nombreRepetido) {
                    nombreNuevo = map.getIntID() + "." + numSameIndiSameAnalyzer;
                    map.setIntID(nombreNuevo);
                }
            } else {

                int numeroAddressDistintosDifAna = 0;
                try {
                    Iterator alert_target_it = map.getIntrusionTarget().iterator();
                    while (alert_target_it.hasNext()) {
                        IntrusionTarget _intTarget = (IntrusionTarget) alert_target_it.next();
                        Iterator address_target_it = _intTarget.getAddress().iterator();
                        while (address_target_it.hasNext()) {
                            numeroAddressDistintosDifAna++;
                            Address address = (Address) address_target_it.next();
                            String targetAddressIP = address.getAddress();
                            String sameAlert_differentAnalyzer = "PREFIX RECAIRS: <" + AIRSNAMESPACE + ">"
                                    + " PREFIX RECIntrusionAlert: <" + INTRUSIONALERTNAMESPACE + ">"
                                    + " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                                    + " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                                    + " PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                                    + " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                                    + " SELECT DISTINCT ?formattedintrusion"
                                    + " WHERE {"
                                    + "?threat a RECAIRS:" + map.getIntType() + "."
                                    + "?node a RECAIRS:Node ."
                                    + "?assessment a RECIntrusionAlert:Assessment."
                                    + "?assessment RECIntrusionAlert:hasIntrusionType ?threat."
                                    + "?target a RECIntrusionAlert:Target ."
                                    + "?target RECIntrusionAlert:hasNode ?node ."
                                    + "?node RECAIRS:hasAddress ?address ."
                                    + "?address RECAIRS:addressIP ?ip ."
                                    + "FILTER (?ip = \"" + targetAddressIP + "\")."
                                    + "?source a RECIntrusionAlert:Source ."
                                    + "?source RECIntrusionAlert:addressIP ?sourceip. "
                                    + "FILTER (?sourceip = \"" + sourceIP + "\")."
                                    + "?formattedintrusion RECIntrusionAlert:detectTime ?idt ."
                                    + "FILTER (xsd:dateTime(?idt) >= \"" + sameAlertDate + "\"^^xsd:dateTime) ."
                                    + "?formattedintrusion RECIntrusionAlert:hasAssessment ?assessment ;"
                                    + "RECIntrusionAlert:hasTarget ?target ;"
                                    + "RECIntrusionAlert:hasSource ?source ."
                                    + " } ";

                            Query q = QueryFactory.create(sameAlert_differentAnalyzer);
                            QueryExecution qe = QueryExecutionFactory.create(q, inferedIndividuosModel);
                            ResultSet rs = qe.execSelect();
                            List<QuerySolution> lista = ResultSetFormatter.toList(rs);
                            for (int j = 0; j < lista.size(); j++) {
                                QuerySolution solution = lista.get(j);
                                Individual indsame = inferedIndividuosModel.getIndividual(solution.get("formattedintrusion").toString());
                                int contador;
                                if (sameIndiDifAnalyzerCounter.containsKey(indsame)) {
                                    contador = (Integer) sameIndiDifAnalyzerCounter.get(indsame) + 1;
                                } else {
                                    contador = 1;
                                }
                                sameIndiDifAnalyzerCounter.put(indsame, contador);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
                int numSameIndiDifAnalyzer = 0;
                Iterator it2 = sameIndiDifAnalyzerCounter.keySet().iterator();
                while (it2.hasNext()) {
                    Individual indi = (Individual) it2.next();
                    if ((Integer) sameIndiDifAnalyzerCounter.get(indi) == numeroAddressDistintosDifAna) {
                        sameIndi.add(indi);
                        if (indi.getLocalName().equals(map.getIntID())) {
                            nombreRepetido = true;
                        }
                        numSameIndiDifAnalyzer++;
                    }
                }

                if (numSameIndiDifAnalyzer > 0) {
                    isARepeatedFormattedIntrusion = true;
                    if (nombreRepetido) {
                        nombreNuevo = map.getIntID() + "." + numSameIndiDifAnalyzer;
                        map.setIntID(nombreNuevo);
                    }
                } else {

                    //ERROR: com.hp.hpl.jena.query.QueryParseException: Line 1, column 528: Unresolved prefixed name: individuos:intrusionDetectionTime
                    String existingIntrusion = "PREFIX RECAIRS: <" + AIRSNAMESPACE + ">"
                            + " PREFIX RECIntrusionAlert: <" + INTRUSIONALERTNAMESPACE + ">"
                            + " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                            + " SELECT DISTINCT ?formattedintrusion"
                            + " WHERE {"
                            + "?threat a RECAIRS:" + map.getIntType() + "."
                            + "?assessment a RECIntrusionAlert:Assessment."
                            + "?assessment RECIntrusionAlert:hasIntrusionType ?threat."
                            + "?source a RECIntrusionAlert:Source ."
                            + "?source RECIntrusionAlert:addressIP ?sourceip. "
                            + "FILTER (?sourceip = \"" + sourceIP + "\")."
                            + "?formattedintrusion RECIntrusionAlert:detectTime ?idt ."
                            + //MIGUEL "?formattedintrusion individuos:intrusionDetectionTime ?idt ."+
                            "FILTER (xsd:dateTime(?idt) >= \"" + existingIntrusionDate + "\"^^xsd:dateTime) ."
                            + "?formattedintrusion RECIntrusionAlert:hasAssessment ?assessment ;"
                            + "RECIntrusionAlert:hasSource ?source ."
                            + " } ";

                    Query que = QueryFactory.create(existingIntrusion);
                    QueryExecution quex = QueryExecutionFactory.create(que, inferedIndividuosModel);
                    ResultSet rsex = quex.execSelect();
                    int numex = 0;
                    boolean nombreRepetidoEI = false;
                    String nombreNuevoEI = null;
                    List<QuerySolution> listaEI = ResultSetFormatter.toList(rsex);

                    for (int i = 0; i < listaEI.size(); i++) {
                        QuerySolution solution = listaEI.get(i);
                        Individual indexist = inferedIndividuosModel.getIndividual(solution.get("formattedintrusion").toString());
                        existingIntrusionIndi.add(indexist);
                        if (indexist.getLocalName().equals(map.getIntID())) {
                            nombreRepetidoEI = true;
                        }
                        numex++;
                    }
                    if (numex > 0) {
                        isAExistingFormattedIntrusion = true;
                        if (nombreRepetidoEI) {
                            nombreNuevoEI = map.getIntID() + ".EX" + numex;
                            map.setIntID(nombreNuevoEI);
                        }
                    }
                }
            }

            addFormattedIntrusion(map, isARepeatedFormattedIntrusion, isAExistingFormattedIntrusion, sameIndi, existingIntrusionIndi);
            //updateIndividuals();
            return isARepeatedFormattedIntrusion;
        }
    }

    boolean ExecuteResponse(String respuesta, ResponseActionParams params) {
        System.out.println("PARAMETROS: " + params.getAdParam());
        return this.MCER.BuildResponseActionRequest(respuesta, params);
    }

    private void getOptimumResponses() {

        Resource intrusionOptim = inferedModel.getResource(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo);
        //MIGUEL Individual formattedIntrusionOptim = inferedModel.getIndividual(INTRUSIONALERTNAMESPACE + intrusionOptim.getLocalName());
        Individual formattedIntrusionOptim = inferedModel.getIndividual(intrusionOptim.toString());
        //System.out.println("Calculando las respuestas optimas de la alerta de intrusión:" + formattedIntrusionOptim.getLocalName());
        Property prop_optimum_resp = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "optimumResponse");
        Property prop_optimum_passive_resp = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "optimumPassiveResponses");
        Property prop_has_assessment = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "hasAssessment");
        Property targetLOI = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "targetLevelOfImportance");
        Property response_cost = inferedModel.getProperty(AIRSNAMESPACE + "cost");
        Property response_complexity = inferedModel.getProperty(AIRSNAMESPACE + "complexity");

        Iterator alert_assessment_it = (Iterator) formattedIntrusionOptim.listPropertyValues(prop_has_assessment);
        int v = 0;
        while (alert_assessment_it.hasNext()) {
            v++;
            List<Resource> opt_response_list = new ArrayList();
            List<Resource> opt_response_final_list = new ArrayList();
            Resource alert_assessment_resource = (Resource) alert_assessment_it.next();
            //MIGUEL Individual alert_assessment_individual = inferedModel.getIndividual(INTRUSIONALERTNAMESPACE+alert_assessment_resource.getLocalName());
            Individual alert_assessment_individual = inferedModel.getIndividual(alert_assessment_resource.toString());
            //MIGUEL Individual assessment = alert_intrusion_model.getIndividual(INTRUSIONALERTNAMESPACE+alert_assessment_resource.getLocalName());
            Individual assessment = alert_intrusion_model.getIndividual(alert_assessment_resource.toString());
            //  System.out.println("Infiriendo las respuestas optimas de: "+ alert_assessment_individual.getLocalName());
            Iterator optim_responses_it;
            //while (multiple_opt_active_response){
            //System.out.println("numeros de respuestas potenciales: "+alert_assessment_individual.getPropertyValue(numPotOptResp).asResource().getLocalName());
            optim_responses_it = (Iterator) alert_assessment_individual.listPropertyValues(prop_optimum_resp);
            int z = 0;            //    System.out.println("TAMAÑO DEL IT: ");
            while (optim_responses_it.hasNext()) {
                Resource res_optimum_resource = (Resource) optim_responses_it.next();
                if (res_optimum_resource != null) {
                    System.out.println(Thread.currentThread() + "intrusion: " + formattedIntrusionOptim.toString() + "-> RESPUESTA OPTIMA: " + res_optimum_resource.getLocalName());
                    opt_response_list.add(res_optimum_resource);
                } else {
                    System.out.println(intrusionOptim.getLocalName() + " " + prop_optimum_resp.getLocalName() + ": null");
                }
                z++;
            }

            if (z > 1) {
                String a = alert_assessment_individual.getPropertyValue(targetLOI).toString();
                int i1 = a.indexOf("^");
                String loi = a.substring(0, i1);

                if (loi.equals("high")) {
                    int cost_min = 0;
                    int j = 0;
                    for (j = 0; j < opt_response_list.size(); j++) {
                        Resource resp_op = opt_response_list.get(j);
                        //MIGUEL Individual resp_op_indi = inferedModel.getIndividual(AIRSNAMESPACE + resp_op.getLocalName());
                        Individual resp_op_indi = inferedModel.getIndividual(resp_op.toString());
                        String cost = resp_op_indi.getPropertyValue(response_cost).toString();
                        int i2 = cost.indexOf("^");
                        int cost_number = Integer.parseInt(cost.substring(0, i2));
                        if (cost_min == 0) {
                            cost_min = cost_number;
                        } else if (cost_number <= cost_min) {
                            cost_min = cost_number;
                        }

                    }

                    //  System.out.println("EL coste mínimo de las respuestas optimas es "+cost_min);
                    int x = 0;
                    for (x = 0; x < opt_response_list.size(); x++) {
                        Resource resp_op = opt_response_list.get(x);
                        //MIGUEL Individual resp_op_indi = inferedModel.getIndividual(AIRSNAMESPACE + resp_op.getLocalName());
                        Individual resp_op_indi = inferedModel.getIndividual(resp_op.toString());
                        String cost = resp_op_indi.getPropertyValue(response_cost).toString();
                        int i2 = cost.indexOf("^");
                        int cost_number = Integer.parseInt(cost.substring(0, i2));
                        if (cost_number == cost_min) {
                            opt_response_final_list.add(resp_op);
                        }
                    }

                } else if (loi.equals("medium") || loi.equals("low")) {
                    int complexity_min = 0;
                    int j = 0;
                    for (j = 0; j < opt_response_list.size(); j++) {
                        Resource resp_op = opt_response_list.get(j);
                        //MIGUEL Individual resp_op_indi = inferedModel.getIndividual(AIRSNAMESPACE + resp_op.getLocalName());
                        Individual resp_op_indi = inferedModel.getIndividual(resp_op.toString());
                        String compl = resp_op_indi.getPropertyValue(response_complexity).toString();
                        int i2 = compl.indexOf("^");
                        int complexity_number = Integer.parseInt(compl.substring(0, i2));
                        if (complexity_min == 0) {
                            complexity_min = complexity_number;
                        } else if (complexity_number <= complexity_min) {
                            complexity_min = complexity_number;
                        }

                    }

                    System.out.println("EL coste mínimo de las respuestas optimas es " + complexity_min);
                    int x = 0;
                    for (x = 0; x < opt_response_list.size(); x++) {
                        Resource resp_op = opt_response_list.get(x);
                        //MIGUEL Individual resp_op_indi = inferedModel.getIndividual(AIRSNAMESPACE + resp_op.getLocalName());
                        Individual resp_op_indi = inferedModel.getIndividual(resp_op.toString());
                        String compl = resp_op_indi.getPropertyValue(response_complexity).toString();
                        int i2 = compl.indexOf("^");
                        int complexity_number = Integer.parseInt(compl.substring(0, i2));
                        if (complexity_number == complexity_min) {
                            opt_response_final_list.add(resp_op);
                        }
                    }

                }
            } else {
                opt_response_final_list = opt_response_list;
            }

            //Ejecutamos las respuestas optimas pasivas y las añadimos a ambos modelos
            Iterator optim_passive_responses_it = (Iterator) alert_assessment_individual.listPropertyValues(prop_optimum_passive_resp);
            int n = 0;
            while (optim_passive_responses_it.hasNext()) {
                n++;
                Resource res_optimum_passive_resource = (Resource) optim_passive_responses_it.next();
                if (res_optimum_passive_resource != null) {
                    // System.out.println(Thread.currentThread() + "intrusion: " + formattedIntrusionOptim.toString()+"-> RESPUESTA PASIVA OPTIMA: "+ res_optimum_passive_resource.getLocalName());
                    try {
                        Property responseAction_prop = inferedModel.getProperty(AIRSNAMESPACE + "responseAction");
                        //Est primera linea es la que tengo que poner
                        //Individual res_optimum_resp = inferedResponseModel.getIndividual( PREFIX + res_optimum_resp.getLocalName());
                        //MIGUEL Individual opt_response_individual = inferedModel.getIndividual(AIRSNAMESPACE + res_optimum_passive_resource.getLocalName());
                        Individual opt_response_individual = inferedModel.getIndividual(res_optimum_passive_resource.toString());
                        String a = opt_response_individual.getPropertyValue(responseAction_prop).toString();
                        int i1 = a.indexOf("^");
                        String accion = a.substring(0, i1);
                        System.out.println("Accion: " + accion);
                        //MIGUEL ResponseActionParams params = new ResponseActionParams(hids,"ALL", alertmap.getIntType(), Integer.toString(alertmap.getIntrusionSource().get(0).getPortSrc()), alertmap.getIntrusionTarget().get(0).getAddress().get(0).getAddress(),protocolo, Integer.toString(alertmap.getIntrusionTarget().get(0).getServicePort()), user, "hola");
                        ResponseActionParams params = new ResponseActionParams(hids, "ALL", alertmap.getIntType(), alertmap.getIntrusionSource().get(0).getNode().get(0).getAddress(), alertmap.getIntrusionTarget().get(0).getAddress().get(0).getAddress(), protocolo, Integer.toString(alertmap.getIntrusionTarget().get(0).getServicePort()), user, "hola");
                        if (ExecuteResponse(accion, params)) {
                            //   System.out.println("RESPUESTA PASIVA EJECUTADA!!");
                            assessment.addProperty(prop_optimum_passive_resp, res_optimum_passive_resource);
                            //getSuccess(alertmap, listContextAnomalyIndicator, null, accion, null);
                        } else {
                            System.out.println("No ha sido posible ejecutar la respuesta");
                        }

                    } catch (Exception e) {
                        System.out.println("ERROR EJECUTANDO LAS RESPUESTAS PASIVAS: " + e);
                        Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, e);

                    }
                } else {
                    System.out.println(intrusionOptim.getLocalName() + " " + prop_optimum_resp.getLocalName() + ": null");
                }
            }

            try {
                Property responseAction_prop = inferedModel.getProperty(AIRSNAMESPACE + "responseAction");
                Property responseType_prop = inferedModel.getProperty(AIRSNAMESPACE + "responseType");
                if (!opt_response_final_list.isEmpty()) {
                    int b;
                    for (b = 0; b < opt_response_final_list.size(); b++) {
                        Resource opt_response_resource = opt_response_final_list.get(b);
                        //Est primera linea es la que tengo que poner
                        //Individual res_optimum_resp = inferedResponseModel.getIndividual( PREFIX + res_optimum_resp.getLocalName());
                        //MIGUEL Individual opt_response_individual = inferedModel.getIndividual( AIRSNAMESPACE + opt_response_resource.getLocalName());
                        Individual opt_response_individual = inferedModel.getIndividual(opt_response_resource.toString());

                        System.out.println(Thread.currentThread() + "RESPUESTA OPIMA: " + opt_response_resource.getLocalName());
                        String a = opt_response_individual.getPropertyValue(responseAction_prop).toString();
                        String rT = opt_response_individual.getPropertyValue(responseType_prop).toString();
                        int i1 = a.indexOf("^");
                        String accion = a.substring(0, i1);
                        int i2 = rT.indexOf("^");
                        String responseT = rT.substring(0, i2);
                        //    System.out.println("Accion: " + accion);
                        List test = _infeInfoList.getInferredInformationList();
                        for (int j = 0; j < test.size(); j++) {
                            InferredInformation inferred_info = (InferredInformation) test.get(j);
                            //      System.out.println("NUmero de inferred_info: "+j);
                            //inferred_info.getContextAnomalyIndicatorList();
                            //A todos los elementos del conjunto les asociamos la misma respuesta
                            inferred_info.setResponseID(accion);
                            inferred_info.setResponseType(responseT);
                            String servicePorts = null;
                            for (int c = 0; c < alertmap.getIntrusionTarget().size(); c++) {
                                IntrusionTarget alertTarget = alertmap.getIntrusionTarget().get(c);
                                List<Address> address_list = alertTarget.getAddress();

                                for (int d = 0; d < address_list.size(); d++) {
                                    String address = address_list.get(d).getAddress();
                                    if (inferred_info.getIP() == address) {
                                        servicePorts = alertTarget.getServiceListPort();
                                    }
                                }
                            }

                            //MIGUEL ResponseActionParams params = new ResponseActionParams(hids, "ALL", alertmap.getIntType(), Integer.toString(alertmap.getIntrusionSource().get(0).getPortSrc()), inferred_info.getIP(), protocolo, servicePorts, user, "hola");
                            ResponseActionParams params = new ResponseActionParams(hids, "ALL", alertmap.getIntType(), alertmap.getIntrusionSource().get(0).getNode().get(0).getAddress(), alertmap.getIntrusionTarget().get(0).getAddress().get(0).getAddress(), protocolo, Integer.toString(alertmap.getIntrusionTarget().get(0).getServicePort()), user, "hola");
                            if (ExecuteResponse(accion, params)) {
                                //        System.out.println("RESPUESTA EJECUTADA!!");
                                assessment.addProperty(prop_optimum_resp, opt_response_individual);

                            } else {
                                System.out.println("No ha sido posible ejecutar la respuesta");
                            }
                        }

                    }

                }
            } catch (Exception e) {
                System.out.println("ERROR AL EJECUTAR LAS RESPUESTAS OPTIMAS ACTIVAS: " + e);
                Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, e);
            }

        }
    }

    private void getAvailableRangeOfComponents() {
        OntClass class_Response = inferedModel.getOntClass(AIRSNAMESPACE + "Response");
        Property numAvaiableComponents = inferedModel.getProperty(AIRSNAMESPACE + "availableRangeOfComponents");
        Iterator subclass_Response = class_Response.listSubClasses();
        while (subclass_Response.hasNext()) {
            OntClass response_class_resource = (OntClass) subclass_Response.next();
            Iterator response_subclass_indi_it = response_class_resource.listInstances();

            while (response_subclass_indi_it.hasNext()) {
                Individual response_indi = (Individual) response_subclass_indi_it.next();
                String response = response_indi.getLocalName();
                String query = "PREFIX individuos: <" + AIRSNAMESPACE + ">"
                        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                        + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                        + " SELECT ?responsenode"
                        + " WHERE {"
                        + "?response a individuos:" + response + "."
                        + "?responsenode a individuos:ResponseNodes ."
                        + "?responsenode individuos:isSecurityDeviceAvailable ?available ."
                        + "FILTER (?available = \"yes\") ."
                        + "?response individuos:neededNodes ?responsenode ."
                        + " } ";

                Query q = QueryFactory.create(query);
                QueryExecution qe = QueryExecutionFactory.create(q, inferedModel);
                ResultSet rs = qe.execSelect();

                List<QuerySolution> lista = ResultSetFormatter.toList(rs);
                int v = 0;
                for (int i = 0; i < lista.size(); i++) {
                    QuerySolution solution = lista.get(i);
                    v++;
                }
                response_indi.removeAll(numAvaiableComponents);
                addIndividualProperty(response_indi, v, numAvaiableComponents.getLocalName(), inferedModel, AIRSNAMESPACE);
            }
        }
    }

    private void getNeededRangeOfComponents() {
        OntClass class_Response = inferedModel.getOntClass(AIRSNAMESPACE + "Response");
        Property responseNodes_prop = inferedModel.getProperty(AIRSNAMESPACE + "neededNodes");
        Property numNeededComponents = inferedModel.getProperty(AIRSNAMESPACE + "neededRangeOfComponents");
        Iterator subclass_Response = class_Response.listSubClasses();
        while (subclass_Response.hasNext()) {
            OntClass response_class_resource = (OntClass) subclass_Response.next();
            Iterator response_subclass_indi_it = response_class_resource.listInstances();
            while (response_subclass_indi_it.hasNext()) {
                Individual response_indi = (Individual) response_subclass_indi_it.next();
                Iterator response_nodes_it = (Iterator) response_indi.listPropertyValues(responseNodes_prop);
                int por = 0;
                while (response_nodes_it.hasNext()) {
                    Resource response_nodes_element = (Resource) response_nodes_it.next();
                    if (response_nodes_element != null) {
                        por++;
                    }
                }
                response_indi.removeAll(numNeededComponents);
                addIndividualProperty(response_indi, por, numNeededComponents.getLocalName(), inferedModel, AIRSNAMESPACE);

            }
        }
    }

    private void getRecommendedResponses() {
        getAvailableRangeOfComponents();
        getNeededRangeOfComponents();
        Resource intrusion = inferedModel.getResource(INTRUSIONALERTNAMESPACE + this.ejemplarIntrusionNuevo);
        //MIGUEL Individual formattedIntrusion = inferedModel.getIndividual(INTRUSIONALERTNAMESPACE + intrusion.getLocalName());
        Individual formattedIntrusion = inferedModel.getIndividual(intrusion.toString());

        //MIGUEL Individual networkContext = inferedModel.getIndividual(AIRSNAMESPACE + inferedModel.getResource(AIRSNAMESPACE+ejemplarContextoRedNuevo));
        Resource netContexto = inferedModel.getResource(AIRSNAMESPACE + this.ejemplarIntrusionNuevo);
        Individual networkContext = inferedModel.getIndividual(netContexto.toString());

        Resource systContexto = inferedModel.getResource(AIRSNAMESPACE + ejemplarContextoSistemaNuevo);
        //MIGUEL Individual systemContext = inferedModel.getIndividual(AIRSNAMESPACE + systContexto.getLocalName());
        Individual systemContext = inferedModel.getIndividual(systContexto.toString());

        //OBTENEMOS EL TIPO DE INTRUSION QUE INDICA EL CONTEXTO
        Property prop_indicates = inferedModel.getObjectProperty(AIRSNAMESPACE + "indicates");

        Iterator indicates = null;
        try {
            indicates = (Iterator) systemContext.listPropertyValues(prop_indicates);
        } catch (Exception e) {
        }
        int con = 0;
        while (indicates.hasNext()) {
            con++;
            Resource res_indicates = (Resource) indicates.next();
            if (res_indicates != null) {
                System.out.println(Thread.currentThread() + "Contexto de red y sistemas indica este tipo de intrusión: " + res_indicates.getLocalName());
            } else {
                System.out.println(intrusion.getLocalName() + " " + prop_indicates.getLocalName() + ": null");
            }
        }

        /*   OntClass class_IRS = inferedModel.getOntClass(AIRSNAMESPACE+"AutomatedIntrusionResponseSystem");
       
         // Se obtienen los ejemplares:
         Iterator instances_IRS = class_IRS.listInstances();
         int j = 0;
         // Se cuenta para cada ejemplar de IntrusionResponseSystem el número de Resultados generados.
         System.out.println("Extrayendo resultados generados....");
         while (instances_IRS.hasNext())
         {
         j++;
         Individual IRS = (Individual)instances_IRS.next();
         Resource res_Result = inferedModel.getResource(AIRSNAMESPACE + "Result");
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
         Property numResp = inferedModel.getProperty(AIRSNAMESPACE+"numberOfGeneratedResult");
            
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
         */
        //Extraemos el número de respuestas recomendadas
        System.out.println("Calculando las respuestas recomendadas de la alerta de intrusión:" + formattedIntrusion);
        Property prop_has_assessment = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "hasAssessment");
        Property prop__potential_optimum_resp = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "potentialOptimumResponses");
        Iterator alert_assessment_it = (Iterator) formattedIntrusion.listPropertyValues(prop_has_assessment);
        Property prop_recommended_resp = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "recommendedResponses");
        Property numRespRecom = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "numberOfRecommendedResponses");
        Property numPotOptResp = inferedModel.getProperty(INTRUSIONALERTNAMESPACE + "numberOfPotentialOptimumResponses");
        int v = 0;
        while (alert_assessment_it.hasNext()) {
            v++;
            Resource alert_assessment_resource = (Resource) alert_assessment_it.next();
            //MIGUEL Individual alert_assessment_individual = inferedModel.getIndividual(INTRUSIONALERTNAMESPACE+alert_assessment_resource.getLocalName());
            Individual alert_assessment_individual = inferedModel.getIndividual(alert_assessment_resource.toString());
            Iterator recom_responses_it = (Iterator) alert_assessment_individual.listPropertyValues(prop_recommended_resp);
            //MIGUEL Individual assessment = alert_intrusion_model.getIndividual(INTRUSIONALERTNAMESPACE+alert_assessment_resource.getLocalName());
            Individual assessment = alert_intrusion_model.getIndividual("" + alert_assessment_resource);
            int z = 0;
            while (recom_responses_it.hasNext()) {
                z++;
                Resource recom_responses_element = (Resource) recom_responses_it.next();
                if (recom_responses_element != null) {
                    System.out.println(Thread.currentThread() + "intrusion: " + formattedIntrusion.toString() + "-> RESPUESTA RECOMENDADA: " + recom_responses_element.getLocalName());
                    assessment.addProperty(prop_recommended_resp, recom_responses_element);
                    //alert_assessment_individual.addProperty(prop_recommended_resp, recom_responses_element);
                } else {
                    System.out.println(intrusion.getLocalName() + " " + prop_recommended_resp.getLocalName() + ": null");
                }

            }

            //System.out.println("Numero de respuestas recomendadas "+z);
            alert_assessment_individual.removeAll(numRespRecom);
            assessment.removeAll(numRespRecom);
            addIndividualProperty(alert_assessment_individual, z, numRespRecom.getLocalName(), inferedModel, INTRUSIONALERTNAMESPACE);
            addIndividualProperty(assessment, z, numRespRecom.getLocalName(), alert_intrusion_model, INTRUSIONALERTNAMESPACE);

            //  System.out.println("Calculando las respuestas potenciales optimas de la alerta de intrusión:" + formattedIntrusion);
            Iterator pot_optimum_responses_it = (Iterator) alert_assessment_individual.listPropertyValues(prop__potential_optimum_resp);
            int por = 0;
            while (pot_optimum_responses_it.hasNext()) {
                por++;
                Resource pot_optimum_responses_element = (Resource) pot_optimum_responses_it.next();
                if (pot_optimum_responses_element != null) {
                    System.out.println(Thread.currentThread() + "intrusion: " + formattedIntrusion.toString() + "-> RESPUESTA OPTIMA POTENCIAL: " + pot_optimum_responses_element.getLocalName());
                    assessment.addProperty(prop__potential_optimum_resp, pot_optimum_responses_element);
                } else {
                    System.out.println(intrusion.getLocalName() + " " + pot_optimum_responses_element.getLocalName() + ": null");
                }
            }
            //System.out.println("Numero de respuestas optimas potenciales "+por);
            alert_assessment_individual.removeAll(numPotOptResp);
            assessment.removeAll(numPotOptResp);
            addIndividualProperty(alert_assessment_individual, por, numPotOptResp.getLocalName(), inferedModel, INTRUSIONALERTNAMESPACE);
            addIndividualProperty(assessment, por, numPotOptResp.getLocalName(), alert_intrusion_model, INTRUSIONALERTNAMESPACE);
        }
    }

    public void getResponseEfficiency() {
        List evaluation_individuos_set = _infeInfoList.getInferredInformationList();
        for (int j = 0; j < evaluation_individuos_set.size(); j++) {
            InferredInformation inferred_info = (InferredInformation) evaluation_individuos_set.get(j);
            if (inferred_info.getResponseID() != null) {
                if (getSuccess(alertmap, inferred_info.getContextAnomalyIndicatorList(), inferred_info.getIP(), inferred_info.getResponseID(), inferred_info.getResponseType())) {
                    //            Sytem.out.println("Eficiencia de la respuesta "+inferred_info.getResponseID()+" actualizada con exito");
                } else {
                    System.out.println("Error al actualizar la eficiencia de la respuesta " + inferred_info.getResponseID());
                }
            }
        }

    }

    private boolean getSuccess(IntrusionAlert alertMap, ContextAnomalyIndicatorList cailist, String IP, String responseID, String responseType) {
        Iterator context_it = cailist.getContextAnomalyIndicatorList().iterator();
        HashMap intAnomaly = new HashMap();
        while (context_it.hasNext()) {
            ContextAnomalyIndicator caiindi = (ContextAnomalyIndicator) context_it.next();
            intAnomaly.put(caiindi.getIndicatorName(), caiindi.getIndicatorValue());
        }
        para = new ExecutionModeParams(responseID, responseType, alertMap.getIntType(), IP, intAnomaly, initAdParam);

        EvaluationSystemModeSelector sel = new EvaluationSystemModeSelector("execution", para);
        if (sel.start()) {
            ResponseTotalEfficiency resp = sel.getExecutionModeResult();
            String rid = resp.getResponseID();
            double rteValue = resp.getResponseEfficiency();
            int num_exe_value = resp.getNumExecutions();
            System.out.println("responseID: " + rid + " --> responseEfficiency: " + rteValue + " --> numero de inferencias: "
                    + num_exe_value + ".");
            updateResponseEfficiency(resp);
            return true;
        } else {
            //System.out.println("error calcu exito");
            return false;
        }
    }

    public void inicializarModelos() {
        long startTime = System.currentTimeMillis();
        System.out.println("__________________________________________________________");
        System.out.println(Thread.currentThread() + " **** INIT INICIALIZAR MODELOS ****");

        //Se carga la ontología con Jena, cargando el modelo inferedResponseModel, donde se actualiza la información relacionada con las alertas de intrusion recibidas y las respuestas recomendadas.
        synchronized (ontology_airs_uri) {
            ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = ontologyModel.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(ontologyModel, ontology_airs_uri);
        }

        synchronized (ontology_intrusion_alert_uri) {
            alert_intrusion_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = alert_intrusion_model.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(alert_intrusion_model, ontology_intrusion_alert_uri);
        }

        long endTime = System.currentTimeMillis();
        System.out.println(Thread.currentThread() + " **** END INICIALIZAR MODELOS *** Total time: " + (endTime - startTime) + " (ms)****");
        System.out.println("______________________________________________");
    }

    /**
     * method inferRecommendedResponses() método que infiere las repuestas
     * recomendadas de la intrusión recibida
     *
     * @param args
     */
    void inferRecommendedResponses() {
        getRecommendedResponses();
    }

    void inferOptimumResponses() {
        getOptimumResponses();
    }

    void reasonerPellet() {
        //reasonedModel = inferedResponseModel;
        inferedModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, null);
        inferedModel.read(ontology_rules_uri);
        inferedModel.add(ontologyModel);
        inferedModel.add(alert_intrusion_model);
        inferedModel.prepare();
    }

    void updateIndividuals() {
        //synchronized(ontology_infered_instances){

        synchronized (ontology_assessed_alert_uri) {

            System.out.println("____________________________________________________________________________________________");
            System.out.println(Thread.currentThread() + "-------OntAIRReasoner - updateIndividuals- method-----------");

            OntModel current_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = current_model.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(current_model, ontology_assessed_alert_uri);
            try {
                OntModel aux = this.alert_intrusion_model;
                current_model.add(aux);
                OutputStream out = new FileOutputStream(ontology_assessed_alert_file);
                current_model.write(out);
                out.close();
            } catch (Exception e) {
                System.out.println("Error al actualizar el fichero");
            }
        }

    }

    void writeFile() {
        synchronized (inferred_uri) {
            System.out.println("____________________________________________________________________________________________");
            System.out.println(Thread.currentThread() + "-------OntAIRReasoner - writeFile - method-----------");

            /*  ModelExtractor extractor = new ModelExtractor(inferedModel);
             System.out.println(Thread.currentThread());
             Model inferences = extractor.extractModel();*/
            OntModel current_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = current_model.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(current_model, ontology_rules_uri);
            inferedModel.add(current_model);

            //RDFReader inf_modelReader = current_model.getReader();
            //inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            //inf_modelReader.read(current_model, inferred_uri);
            try {
                OutputStream out = new FileOutputStream(inferred_file);
                //OutputStream out2 = new FileOutputStream(ontology_infered_file);
                inferedModel.write(out);
                //ontologyModel.write(out2);
                out.close();
                //out2.close();
                //out2.close();

            } catch (Exception e) {
                System.out.println("Error al actualizar el fichero");
            }

            /* OntModel current_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
             try {
             System.out.println("COMPROBANDO SI HAY NUEVOS INDIVIDUOS ACTUALES");
             OntModel aux = this.inferedModel;
             current_model.add(aux);
             OutputStream out = new FileOutputStream(inferred_file);
             current_model.write(out);
             out.close();
             } catch (Exception e) {
             System.out.println("Error al actualizar el fichero");
             }*/
        }
    }

    private void updateResponseEfficiency(ResponseTotalEfficiency respEf) {
        System.out.println("____________________________________________________________________________________________");
        System.out.println(Thread.currentThread() + "-------RECOntAIRSReasoner - updateResponseEfficiency- method-----------");
        ResponseTotalEfficiency rte = respEf;
        String rid = rte.getResponseID();
        double rteValue = rte.getResponseEfficiency();
        int num_exe_value = rte.getNumExecutions();
        // _log.info("responseID: "+rid+ " --> responseEfficiency: "+rteValue + " --> numero de inferencias: " +num_exe_value+".");   
        OntModel responseModel;
        synchronized (ontology_airs_uri) {
            responseModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = responseModel.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(responseModel, ontology_airs_uri);
        }
        Resource response_resource = responseModel.getResource(AIRSNAMESPACE + "Response");
        OntClass class_Response = (OntClass) response_resource.as(OntClass.class);
        Property responseEff_prop = responseModel.getProperty(AIRSNAMESPACE + "efficiency");
        Property responseID_prop = responseModel.getProperty(AIRSNAMESPACE + "responseAction");
        Property responseNumExec_prop = responseModel.getProperty(AIRSNAMESPACE + "numExecutions");

        String responseID;
        Iterator response_subclass_it = class_Response.listSubClasses();
        while (response_subclass_it.hasNext()) {
            OntClass response_subclass = (OntClass) response_subclass_it.next();

            Iterator response_instances = response_subclass.listInstances();
            while (response_instances.hasNext()) {
                Individual response = (Individual) response_instances.next();
                //System.out.println("responseID:"+response.getLocalName());
                //Obtenemos el valor de la propiedad IDSID del ejemplar de IntrusionDetectionSystem.
                responseID = response.getPropertyValue(responseID_prop).toString();
                responseID = responseID.substring(0, responseID.indexOf("^"));
                System.out.println("responseID:" + responseID);
                if (responseID.equals(rid)) {
                    response.removeAll(responseEff_prop);
                    addIndividualProperty(response, rteValue, responseEff_prop.getLocalName(), responseModel, AIRSNAMESPACE);
                    response.removeAll(responseNumExec_prop);
                    addIndividualProperty(response, num_exe_value, responseNumExec_prop.getLocalName(), responseModel, AIRSNAMESPACE);
                    break;
                }

            }
        }

        synchronized (ontology_airs_uri) {
            //    OntModel current_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            //  RDFReader inf_modelReader = current_model.getReader();
            //inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            // inf_modelReader.read(current_model, ontology_airs_uri);
            try {
                OntModel aux = this.ontologyModel;
                //   current_model.add(aux);
                OutputStream out = new FileOutputStream(ontology_airs_file);
                responseModel.write(out);
                out.close();
            } catch (Exception e) {
                System.out.println("Error al actualizar el fichero");
            }
            System.out.println("actualizacion realizada con exito");
        }

    }

    /*  synchronized void writeFile(){
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
  
     }}   */
    public int getIntrusionImpact(IntrusionAlert intru) {
        IntrusionAlert alert_map = intru;
        int intrusion_efecto = 0;
        int intrusion_impact = 0;
        int severity = intru.getIntSeverity();
        HashMap security_goal = getSecurityGoalThreaten(intru.getIntType());
        List<IntrusionTarget> intrusion_target_list = alert_map.getIntrusionTarget();
        String[] target_result = {"assetLevelOfImportance_confidentiality", "assetLevelOfImportance_integrity", "assetLevelOfImportance_availability", "assetLevelOfImportance_authenticity"};
        String[] security_goal_result = {"confidentiality", "integrity", "availability", "authenticity"};
        if (intrusion_target_list.size() > 0) {
            for (int j = 0; j < intrusion_target_list.size(); j++) {
                IntrusionTarget target = intrusion_target_list.get(j);
                List<Address> intrusion_address_list = target.getAddress();
                for (int z = 0; z < intrusion_address_list.size(); z++) {
                    String ip = target.getAddress().get(z).getAddress();
                    BDManagerIF bd = DataManagerFactory.getInstance().createDataManager();
                    int aloi_parcial = 0;
                    int ik;
                    try {
                        HashMap result = bd.obtainAssetLevelOfImportance(ip);

                        for (int v = 0; v < 4; v++) {
                            Object security_target_result = result.get(target_result[v]);
                            if (security_goal.get(security_goal_result[v]) != null) {
                                ik = (Integer) security_goal.get(security_goal_result[v]);
                            } else {
                                ik = 0;
                            }
                            //  System.out.println("parametro: "+security_goal_result[v]);
                            //System.out.println("ik "+ik);
                            //System.out.println("target level of importance "+security_target_result.toString())
                            ;
                            if (security_target_result instanceof String) {
                                String resultSt = (String) security_target_result;
                                if (resultSt.equalsIgnoreCase("high")) {
                                    aloi_parcial = ASSET_LEVEL_OF_IMPORTANCE_HIGH_VALUE;
                                } else if (resultSt.equalsIgnoreCase("medium")) {
                                    aloi_parcial = ASSET_LEVEL_OF_IMPORTANCE_MEDIUM_VALUE;
                                } else if (resultSt.equalsIgnoreCase("low")) {
                                    aloi_parcial = ASSET_LEVEL_OF_IMPORTANCE_LOW_VALUE;
                                }
                            } else if (security_target_result instanceof Integer) {
                                aloi_parcial = (Integer) security_target_result;
                            }
                            intrusion_efecto = intrusion_efecto + (ik * aloi_parcial);
                        }
                        intrusion_impact = intrusion_impact + (intrusion_efecto * severity);

                    } catch (SQLException ex) {
                        Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (recontairs.DAO.DAOException ex) {
                        Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        System.out.println("el impacto total de la intrusion es : " + intrusion_impact);
        return intrusion_impact;
    }

    private String getTotalTargetLevelOfImportance(IntrusionAlert intru) {
        IntrusionAlert alert_map = intru;
        int tloi_total = 0;
        int number_of_targets = 0;
        String target_level_of_importance = null;
        List<IntrusionTarget> intrusion_target_list = alert_map.getIntrusionTarget();
        if (intrusion_target_list.size() > 0) {
            for (int j = 0; j < intrusion_target_list.size(); j++) {
                IntrusionTarget target = intrusion_target_list.get(j);
                List<Address> intrusion_address_list = target.getAddress();
                for (int z = 0; z < intrusion_address_list.size(); z++) {
                    String ip = target.getAddress().get(z).getAddress();
                    BDManagerIF bd = DataManagerFactory.getInstance().createDataManager();
                    try {
                        HashMap result = bd.obtainAssetLevelOfImportance(ip);
                        Object levelOfImportance = result.get("assetLevelOfImportance");
                        int importance = 0;
                        if (levelOfImportance instanceof String) {
                            String resultSt = (String) levelOfImportance;
                            if (resultSt.equalsIgnoreCase("high")) {
                                importance = ASSET_LEVEL_OF_IMPORTANCE_HIGH_VALUE;
                            } else if (resultSt.equalsIgnoreCase("medium")) {
                                importance = ASSET_LEVEL_OF_IMPORTANCE_MEDIUM_VALUE;
                            } else if (resultSt.equalsIgnoreCase("low")) {
                                importance = ASSET_LEVEL_OF_IMPORTANCE_LOW_VALUE;
                            }
                        } else if (levelOfImportance instanceof Integer) {
                            importance = (Integer) levelOfImportance;
                        }

                        tloi_total = tloi_total + importance;

                    } catch (SQLException ex) {
                        Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (recontairs.DAO.DAOException ex) {
                        Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    number_of_targets++;
                }
            }
            int importance_total = tloi_total / number_of_targets;
            if (importance_total <= 3) {
                target_level_of_importance = "low";
            } else if (importance_total >= 7) {
                target_level_of_importance = "high";
            } else {
                target_level_of_importance = "medium";
            }
        }
        //   System.out.println("La importancia total del sistema es : "+target_level_of_importance);
        return target_level_of_importance;

    }

    private String getTargetLevelOfImportance(String IP) {
        String target_level_of_importance = null;
        String ip = IP;
        BDManagerIF bd = DataManagerFactory.getInstance().createDataManager();
        try {
            HashMap result = bd.obtainAssetLevelOfImportance(ip);
            Object levelOfImportance = result.get("assetLevelOfImportance");
            if (levelOfImportance instanceof Integer) {
                int resultSt = (Integer) levelOfImportance;
                if (resultSt <= 3) {
                    target_level_of_importance = "low";
                } else if (resultSt >= 7) {
                    target_level_of_importance = "high";
                } else {
                    target_level_of_importance = "medium";
                }
            } else if (levelOfImportance instanceof String) {
                target_level_of_importance = (String) levelOfImportance;
            }
        } catch (SQLException ex) {
            Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (recontairs.DAO.DAOException ex) {
            Logger.getLogger(RECOntAIRSReasoner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return target_level_of_importance;

    }

    private HashMap getSecurityGoalThreaten(String intrusionType) {
        //MIGUEL
        if (intrusionType.equals("DenialOfService")) {
            intrusionType = "DoS";
        }
        HashMap security_goal = new HashMap();
        String intrusion_name = null;
        try {
            Resource threat_res = ontologyModel.getResource(AIRSNAMESPACE + "SpecificThreat");
            OntClass threat_class = (OntClass) threat_res.as(OntClass.class);
            Iterator threat_subclass_it = threat_class.listSubClasses();
            while (threat_subclass_it.hasNext()) {
                OntClass threat_subclass = (OntClass) threat_subclass_it.next();
                if (intrusionType.equals(threat_subclass.getLocalName())) {
                    Iterator threat_indiv_it = threat_subclass.listInstances();
                    while (threat_indiv_it.hasNext()) {
                        Individual threat_indiv = (Individual) threat_indiv_it.next();
                        intrusion_name = threat_indiv.getLocalName();
                    }
                }
            }
            Resource threat_unde_res = ontologyModel.getResource(AIRSNAMESPACE + "UndefinedThreat");
            OntClass threat_unde_class = (OntClass) threat_unde_res.as(OntClass.class);
            if ((intrusionType.equalsIgnoreCase("unknown")) || (intrusionType.equalsIgnoreCase("other")) || (intrusionType.equalsIgnoreCase("UndefinedThreat"))) {
                Iterator threat_unde_indiv_it = threat_unde_class.listInstances();
                while (threat_unde_indiv_it.hasNext()) {
                    Individual threat_unde_indiv = (Individual) threat_unde_indiv_it.next();
                    intrusion_name = threat_unde_indiv.getLocalName();
                }
            }
        } catch (Exception e) {
            _log.log(Level.SEVERE, null, e);
        } // fin de añadir tipo de intrusion detectada

        System.out.println("el tipo de intrusion es: " + intrusion_name);
        System.out.println("el tipo de intrusion es: " + intrusionType);

        String securityGoalThreaten = "PREFIX individuos: <" + AIRSNAMESPACE + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + " SELECT ?securitygoal"
                + " WHERE {"
                + "?threat a individuos:" + intrusionType + " ."
                + "?threat individuos:threatens ?securitygoal ."
                + " } ";

        Query q = QueryFactory.create(securityGoalThreaten);
        QueryExecution qe = QueryExecutionFactory.create(q, ontologyModel);
        ResultSet rs = qe.execSelect();
        List<QuerySolution> lista = ResultSetFormatter.toList(rs);

        if (lista.size() > 0) {
            for (int i = 0; i < lista.size(); i++) {
                QuerySolution solution = lista.get(i);
                // _log.info(solution.toString());
                //solution.get("successThresholdHihg");
                Resource security_goal_result = (Resource) solution.get("securitygoal");
                //int i1 = security_goal_result.indexOf("^");
                //security_goal_result = security_goal_result.substring(0, i1);
                String security_goal_result_string = security_goal_result.getLocalName();
                security_goal.put(security_goal_result_string, 1);
                //System.out.println("LA INTRUSION "+intrusion_name+" amenaza "+security_goal_result_string);
            }
        }

        return security_goal;
    }

    public int getIntrusionDefaultSeverity(IntrusionAlert intru) {

        String intrusion_name = intru.getIntType();
        int defaultSeverity = 0;

        /*String threatType = intru.getIntType();
        //MIGUEL
        if (threatType.equals("DenialOfService")){threatType = "DoS";}
        String intrusion_name = null;
        try {
            Resource threat_res = ontologyModel.getResource(AIRSNAMESPACE + "SomeThreat");
            OntClass threat_class = (OntClass) threat_res.as(OntClass.class);
            Iterator threat_subclass_it = threat_class.listSubClasses();
            while (threat_subclass_it.hasNext()) {
                OntClass threat_subclass = (OntClass) threat_subclass_it.next();
                System.out.println(threat_subclass.getLocalName());
                if (threatType.equals(threat_subclass.getLocalName())) {
                    Iterator threat_indiv_it = threat_subclass.listInstances();
                    while (threat_indiv_it.hasNext()) {
                        Individual threat_indiv = (Individual) threat_indiv_it.next();
                        System.out.println(threat_indiv.getLocalName());
                        //intrusion_name = threat_indiv.getLocalName();
                    }
                }
            }

        } catch (Exception e) {
            _log.log(Level.SEVERE, null, e);
        }*/
        if (intrusion_name != null) {
            String defaultSeverityQuery = "PREFIX individuos: <" + AIRSNAMESPACE + ">"
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                    + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                    + " SELECT ?threatDefaultSeverityValue"
                    + " WHERE {"
                    + "?threat a individuos:" + intrusion_name + " ."
                    + "?threat individuos:threatDefaultSeverity ?threatDefaultSeverityValue ."
                    + " } ";

            Query q = QueryFactory.create(defaultSeverityQuery);
            QueryExecution qe = QueryExecutionFactory.create(q, ontologyModel);
            ResultSet rs = qe.execSelect();
            List<QuerySolution> lista = ResultSetFormatter.toList(rs);

            if (lista.size() > 0) {
                for (int i = 0; i < lista.size(); i++) {
                    QuerySolution solution = lista.get(i);
                    // _log.info(solution.toString());
                    //solution.get("successThresholdHihg");
                    //Resource default_severity_result = (Resource) solution.get("threatDefaultSeverityValue");
                    String severity = solution.getLiteral("threatDefaultSeverityValue").toString();
                    int a = severity.indexOf("^");
                    defaultSeverity = Integer.parseInt(severity.substring(0, a));
                    //System.out.println("LA INTRUSION "+intrusion_name+" amenaza "+security_goal_result_string);
                }
            }
        } else {
            System.out.println("El AIRS no reconoce el tipo de amenaza indicada en la alerta");
        }
        return defaultSeverity;
    }
}
