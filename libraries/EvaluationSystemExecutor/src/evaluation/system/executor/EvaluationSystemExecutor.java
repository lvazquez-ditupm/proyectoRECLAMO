/**
 * "EvaluationSystemExecutor" Java class is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version always keeping the additional
 * terms specified in this license.
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
package evaluation.system.executor;

import backpropagation.RedNeuronal;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import context.entropy.variance.ContextAnomalyIndicator;
import context.entropy.variance.ContextAnomalyIndicatorList;
import context.entropy.variance.ContextEntropyVariance;
import evaluation.system.executor.DAO.BDManagerIF;
import evaluation.system.executor.DAO.DataManagerFactory;
import evaluation.system.executor.utils.DateToXsdDatetimeFormatter;
import evaluation.system.executor.utils.PropsUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.context.mode.selector.NetAnomalyDetectionModeParams;
import network.context.mode.selector.NetworkContextModeSelector;
import system.context.mode.selector.AnomalyDetectionModeParams;
import system.context.mode.selector.SystemContextModeSelector;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;

/**
 * This class represents the component responsible to run the intrusion response
 * evaluation system in execution mode. The instances of this calculate the
 * partial and total efficiency of the execution of a specified response action.
 *
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class EvaluationSystemExecutor {

    private String intrusionType;
    private String responseID;
    private String responseType;
    private HashMap anomalyIntrusionMap;
    private HashMap networkContextAnomaly;
    private String _IP;
    private String[] adParam;
    private ThreatWeights threatWeights;
    private ContextAnomalyIndicatorList intrusionAnomalyList;
    private ContextAnomalyIndicatorList responseAnomalyList;
    public double successLevelEntropy;
    public double[] successLevel_nn;
    public double responseEfficiency;
    private ResponseTotalEfficiency response_total_efficiency;
    private ThreatSuccessThreshold threat_success_thresholds;
    ContextEntropyVariance cevariance;
    private static Logger _log = Logger.getLogger(EvaluationSystemExecutor.class.getName());
    private PropsUtil props = new PropsUtil();
    private String AIRSNAMESPACE = props.getOntAIRSOntologyNamespaceValue();
    private final String ontology_airs_uri = props.getOntAIRSOntologyUriValue();
    private final String ontology_airs_file = props.getOntAIRSOntologyFileValue();
    private OntModel ontologyModel;

    public EvaluationSystemExecutor(String intType, String respID, String respType, String ip, HashMap anomalyT1, String[] addParam) {
        this.intrusionType = intType;
        this.responseID = respID;
        this.responseType = respType;
        this.anomalyIntrusionMap = anomalyT1;
        this.adParam = addParam;
        this._IP = ip;
    }

    /**
     * This method initializes the intrusion response evaluation system.
     *
     * @param: void
     * @return: boolean. This parameters reflects if the instance of the
     * EvaluationSystemExecutor has been started successfully.
     */
    public boolean init() {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("**** INIT EvaluationSystemExecutor ****");
            String file = props.getNNFileValue();
            //System.out.println(file);
            RedNeuronal red_neuronal = new RedNeuronal(file);
            initializeOntModel();
            String hostname;
            String subnetworkName;
            BDManagerIF bd = DataManagerFactory.getInstance().createDataManager();
            try {
                hostname = bd.obtainHostName(this._IP);
                subnetworkName = bd.obtainSubNetworkInfo(this._IP);
            } catch (Exception ex) {
                Logger.getLogger(EvaluationSystemExecutor.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            //Calculamos la variacion de la entropia del contexto
            HashMap anomalyResponseMap = getSystemContextAnomaly(this._IP, hostname);

            /*            if (networkContextAnomaly.containsKey(subnetworkName)){
             net_anomaly = (Integer)networkContextAnomaly.get(subnetworkName);                            
             }
             else{*/
            int net_anomaly = getNetworkContextAnomaly(subnetworkName);
            //networkContextAnomaly.put(subnetworkName, net_anomaly);
            //}
            anomalyResponseMap.put("Network", net_anomaly);
            if (anomalyResponseMap.isEmpty()) {
                return false;
            } else {
                /*
                double totalCEV = getCEVGlobal(anomalyResponseMap);
                System.out.println("Valor de varianza de la entropia del contexto es: " + totalCEV);
                //Comparamos con umbrales superior o inferior
                threat_success_thresholds = getSuccessThreshold(intrusionType);
                double response_partialCEV = getCEVPartial(anomalyResponseMap);
                successLevelEntropy = getSuccessLevelEntropy(totalCEV, response_partialCEV, threat_success_thresholds);
                //success_level_values_entropy.add(successLevel);
                // response_total_efficiency=obtainResponseEfficiency(responseID,responseType,successLevelEntropy);
                System.out.println("Exito PARCIAL ENTROPIA: " + successLevelEntropy);
                */
                /*Calculamos la eficiencia usando redes neuronales*/
                // System.out.println("Exito TOTAL ENTROPIA: "+response_total_efficiency.getResponseEfficiency());
                /*
                double[] dif = getAnomalyVariance(anomalyIntrusionMap, anomalyResponseMap);
                */
                double[] responseAnomaly = getResponseAnomaly(anomalyResponseMap);
                successLevel_nn = red_neuronal.clasificacion(responseAnomaly);//dif);
                // _log.info("El exito parcial de la respuesta utilizando entropía es: "+successLevel_nn);
                //success_level_values_neural_net.add(successLevel_nn);   
                /*
                comentado
                double success_level_average = (successLevelEntropy + successLevel_nn[0]) / 2;
                */
                //response_total_efficiency.setResponseEfficiency(success_level_average);
                System.out.println("Exito RED NEURONAL: " + successLevel_nn[0]);

                //Se calcula la tasa de exito y se actualiza el valor.
                response_total_efficiency = obtainResponseEfficiency(responseID, responseType, successLevel_nn[0]);//success_level_average);
                System.out.println("Exito TOTAL final: " + response_total_efficiency.getResponseEfficiency());
                //actualizamos valor en el fichero o devolvemos valor al razonador ??
                // updateResponseEfficiency(response_total_efficiency);
                long endTime = System.currentTimeMillis();
                System.out.println("**** END EvaluationSystemExecutor *** Total time : " + (endTime - startTime) + " (ms)**** ");
                return true;
            }

        } catch (Exception e) {
            Logger.getLogger(EvaluationSystemExecutor.class.getName()).log(Level.SEVERE, null, e);;
            return false;
        }
    }

    private ThreatWeights getAnomalyWeights(String intrusionType) {
        /*TODO: EN FUNCIÓN DEL TIPO DE INTRUSIÓN, EL ADMINISTRADOR ESTABLECE VALORES DE PESO DE CADA PARÁMETRO DEL CONTEXTO*/

        ThreatWeights threatw = new ThreatWeights();
        //si no introduzco valores se toman los de por dfecto; Estos valores se deben obtener del contexto
       /*  threatw.setCPUAnomalyWeight(0.25);
         threatw.setDiskAnomalyWeight(0.0);
         threatw.setLatencyAnomalyWeight(0.25);
         threatw.setNetworkAnomalyWeight(0.25);
         threatw.setProcessAnomalyWeight(0.0);
         threatw.setSSHFailedAnomalyWeight(0.0);
         threatw.setStatusAnomalyWeight(0.0);
         threatw.setUsersAnomalyWeight(0.25);
         threatw.setZombieAnomalyWeight(0.0);*/
        return threatw;
    }

    private ContextAnomalyIndicatorList getCAI(HashMap anomaly, ThreatWeights tw) {
        ContextAnomalyIndicatorList anomalyIndiList = new ContextAnomalyIndicatorList();
        HashMap anomalymap = anomaly;
        ThreatWeights threatweights = tw;
        HashMap tweights = threatweights.getThreatWeightHashMap();
        Iterator it = anomalymap.keySet().iterator();

        while (it.hasNext()) {
            String indiName = it.next().toString();
            int indiValue = (Integer) anomalymap.get(indiName);
            double indiWeight = (Double) tweights.get(indiName);
            ContextAnomalyIndicator indicator = new ContextAnomalyIndicator(indiName, indiValue, indiWeight);
            anomalyIndiList.addContextAnomalyIndicator(indicator);
        }
        return anomalyIndiList;

    }

    private double getCEVGlobal(HashMap anomalyMap) {
        //Primero calculamos el contexto de red y sistemas en este instante
        HashMap anomalyMapT2 = anomalyMap;

        /*Convertimos cada elemento del mapa en un elemento ContextAnomalyIndicator
         * y generamos una lista de ContextAnomalyIndicator.
         * Paso 1: obtener el peso para cada indicador.
         */
        threatWeights = getAnomalyWeights(intrusionType);

        //Paso 2: construyo el objeto ContextAnomalyIndicatorList para cada contexto;
        intrusionAnomalyList = getCAI(anomalyIntrusionMap, threatWeights);
        responseAnomalyList = getCAI(anomalyMapT2, threatWeights);

        //Paso 3: Se calcula ContextEntropyVariance
        cevariance = new ContextEntropyVariance();
        return cevariance.getTotalContextEntropyVariance(intrusionAnomalyList, responseAnomalyList);
    }

    private double getCEVPartial(HashMap anomalyMap) {
        HashMap anomalyMapT2 = anomalyMap;
        ContextAnomalyIndicatorList respAnList;
        ContextEntropyVariance cev_partial;
        /*Convertimos cada elemento del mapa en un elemento ContextAnomalyIndicator
         * y generamos una lista de ContextAnomalyIndicator.
         * Paso 1: obtener el peso para cada indicador.
         */

        threatWeights = getAnomalyWeights(intrusionType);
        HashMap test = threatWeights.getThreatWeightHashMap();
        Iterator it = test.keySet().iterator();
        while (it.hasNext()) {
            String nombre = it.next().toString();
            String valor = test.get(nombre).toString();
        }
        //Paso 2: construyo el objeto ContextAnomalyIndicatorList para cada contexto;
        respAnList = getCAI(anomalyMapT2, threatWeights);

        // Se calcula ContextEntropyVariance
        cev_partial = new ContextEntropyVariance();
        return cev_partial.getPartialContextEntropyVariance(respAnList);
    }

    private HashMap getSystemContextAnomaly(String IP, String namehost) {

        HashMap contextAnomalyMap = new HashMap();
        //  HashMap contextAnomalyMap = null;
        try {
            SystemAnomaly system_anomaly;

            /* Obtenemos el grado de anomalia del contexto de sistemas
             * Params: targetIP, targetName
             */
            String hostname = namehost;
            String targetIP = IP;
            /*
             BDManagerIF bd=DataManagerFactory.getInstance().createDataManager();
             try {           
             try {
             hostname = bd.obtainHostName(targetIP);
             subnetworkName = bd.obtainSubNetworkInfo(targetIP);
             } catch (SQLException ex) {
             Logger.getLogger(EvaluationSystemExecutor.class.getName()).log(Level.SEVERE, null, ex);
             } catch (DAOException ex) {
             Logger.getLogger(EvaluationSystemExecutor.class.getName()).log(Level.SEVERE, null, ex);
             }            
             }catch (Exception e){}                
             */
            AnomalyDetectionModeParams system_anomaly_params = new AnomalyDetectionModeParams(targetIP, hostname, null);
            SystemContextModeSelector syssel = new SystemContextModeSelector("anomalydetection", system_anomaly_params);
            if (syssel.start()) {
                system_anomaly = syssel.getSystemAnomaly();
                system_anomaly.printAnomaly();
                contextAnomalyMap.put("Process", system_anomaly.getProcesosA());
                contextAnomalyMap.put("CPU", system_anomaly.getCPUA());
                contextAnomalyMap.put("Disk", system_anomaly.getDiscoduroA());
                contextAnomalyMap.put("Latency", system_anomaly.getLatenciaA());
                contextAnomalyMap.put("User", system_anomaly.getUsuariosA());
                contextAnomalyMap.put("Status", system_anomaly.isEstadoA());
                contextAnomalyMap.put("Zombie", system_anomaly.getZombiesA());
                contextAnomalyMap.put("SSHFailed", system_anomaly.getSSHFailedA());
            } else {
                System.out.println("Ha habido un error al obtener la anomalia del contexto de sistemas");
            }
        } catch (Exception e) {
            _log.severe(e.toString());
        }
        return contextAnomalyMap;
    }

    private int getNetworkContextAnomaly(String subnet) {

        String subnetworkName = subnet;
        int network_anomaly = -1;
        /* Obtenemos el grado de anomalia del contexto de red
         * Params: subnetwork name
         */
        try {

            NetAnomalyDetectionModeParams net_anomaly_params = new NetAnomalyDetectionModeParams(subnetworkName, null);
            NetworkContextModeSelector netsel = new NetworkContextModeSelector("anomalydetection", net_anomaly_params);
            if (netsel.start()) {
                network_anomaly = netsel.getNetworkAnomaly();
                //map.put("Network", network_anomaly);
                //       System.out.println("El grado de anomalia del contexto de red es: "+network_anomaly);
            } else {
                System.out.println("Ha habido un error obteniendo la anomalia del contexto de red");
            }

            /* // Se construye el mapa de anomalia cn parametros falsos:hay que poner
             //los de verdad
             contextAnomalyMap.put("Network", 1);
             contextAnomalyMap.put("Process", 1);
             contextAnomalyMap.put("CPU", 0);
             contextAnomalyMap.put("Disk", 0);
             contextAnomalyMap.put("Latency", 0);
             contextAnomalyMap.put("User", 0);
             contextAnomalyMap.put("Status", 0);
             contextAnomalyMap.put("Zombie", 0);
             contextAnomalyMap.put("SSHFailed", 0);*/
            Date responseAnomalyDate = new Date();
            DateToXsdDatetimeFormatter formateador = new DateToXsdDatetimeFormatter();
            String responseAnomalyTime = formateador.format(responseAnomalyDate);
        } catch (Exception e) {
            _log.severe(e.toString());
        }
        return network_anomaly;

    }

    public ResponseTotalEfficiency getRTE() {
        return response_total_efficiency;
    }

    private ResponseTotalEfficiency obtainResponseEfficiency(String respID, String respType, double RPE) {
        String responsID = respID;
        String responsType = respType;
        //_log.info("RESPUESTA: "+responsID.toString());
        double success_level_new = RPE;
        double resultadoEf = -1.0;
        int num_execution = -1;
        ResponseTotalEfficiency resultado = new ResponseTotalEfficiency();
        String respExistingEfficiency = "PREFIX individuos: <" + AIRSNAMESPACE + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + " SELECT ?responseefficiency ?numexecutions"
                + " WHERE {"
                + "?response a individuos:" + responsType + " ."
                + "?response individuos:responseAction ?rid ."
                + "FILTER (?rid = \"" + responsID + "\")."
                + "?response individuos:efficiency ?responseefficiency ."
                + "?response individuos:numExecutions ?numexecutions ."
                + " } ";
        //System.out.println("query: "+respExistingEfficiency);
        Query q = QueryFactory.create(respExistingEfficiency);
        QueryExecution qe = QueryExecutionFactory.create(q, ontologyModel);
        ResultSet rs = qe.execSelect();
        List<QuerySolution> lista = ResultSetFormatter.toList(rs);

        if (lista.size() > 0) {
            for (int i = 0; i < lista.size(); i++) {
                QuerySolution solution = lista.get(i);
                //  _log.info(solution.toString());
                //solution.get("successThresholdHihg");
                String response_existing_efficiency_string = solution.get("responseefficiency").toString();
                int i1 = response_existing_efficiency_string.indexOf("^");
                response_existing_efficiency_string = response_existing_efficiency_string.substring(0, i1);
                double response_existing_efficiency = Double.parseDouble(response_existing_efficiency_string);
                String num_execution_string = solution.get("numexecutions").toString();
                int i2 = num_execution_string.indexOf("^");
                num_execution_string = num_execution_string.substring(0, i2);
                num_execution = Integer.parseInt(num_execution_string);
           //     System.out.println("numero de ejecuciones "+num_execution);
                //   System.out.println("eficiencia "+response_existing_efficiency_string);
                if (num_execution > 0) {
                    resultadoEf = ((((response_existing_efficiency / 100.00) * num_execution) + success_level_new) / (num_execution + 1)) * 100.00;
                } else {
                    resultadoEf = RPE * 100.00;
                }
            }
            resultado.setResponseID(responseID);
            resultado.setResponseEfficiency(resultadoEf);
            resultado.setNumExecutions(num_execution + 1);
        } else {
            //_log.info("Primera ejecucion de la respuesta");
            resultado.setResponseID(responseID);
            resultado.setResponseEfficiency(RPE * 100.00);
            resultado.setNumExecutions(1);
        }
        return resultado;
        //Calculamos la nueva eficiencia

    }

    private double getSuccessLevelEntropy(double cevTotal, double cevResPartial, ThreatSuccessThreshold tst) {
        double response_partial_efficiency = -1;
        double context_entropy_variance_global = cevTotal;
        double context_entropy_variance_partial_response = cevResPartial;
        ThreatSuccessThreshold threat_threshold = tst;
        // double threat_threshold_low = threat_threshold.getThreatThresholdLow();
        //double threat_threshold_high = threat_threshold.getThreatThresholdHigh();
        double threat_threshold_high = 0.5;
        double threat_threshold_low = 0.1;
        double system_threshold_config = Double.parseDouble(props.getSystemEvaluationThresholdLevelSuccessValue());

        String threat_name = threat_threshold.getThreatName();
        System.out.println("CEVTotal;: " + context_entropy_variance_global);
        System.out.println("ThresholdLow;: " + threat_threshold_low);
        System.out.println("ThresholdHogh;: " + threat_threshold_high);
        System.out.println("Beta: " + system_threshold_config);
        //Calculamos valor de nivel de exito
        if ((cevTotal == 0.0) && (threat_threshold_low == 0.0) && (threat_threshold_high == 0.0)) {
             //_log.info("El ataque no se refleja en el contexto-->no varían los indicadores");
            // FALTA EJECUTAR ESTA PARTE DEL CODIGO PARA COMPROBAR LA EXISTENCIA DE INTRUSION O NO EN T2
        } else {
            if (context_entropy_variance_global >= threat_threshold_high) {
                response_partial_efficiency = 1.0;
            } else if ((context_entropy_variance_global <= threat_threshold_low) && (context_entropy_variance_partial_response > system_threshold_config)) {

                response_partial_efficiency = 0.0;
            } else if ((threat_threshold_low < context_entropy_variance_global) && (context_entropy_variance_global < threat_threshold_high)) {
                if (context_entropy_variance_partial_response <= system_threshold_config) {
                    response_partial_efficiency = 1.0;
                } else {
                    response_partial_efficiency = 0.5;
                }
            }
        }
        return response_partial_efficiency;
    }

    private ThreatSuccessThreshold getSuccessThreshold(String intrusionType) {

        //TODO: NO SE SI LEER DE INDIVIDUOS O DE INFERREDINDIVIDUOS
        ThreatSuccessThreshold tst = new ThreatSuccessThreshold();
        String intType = intrusionType;
        String thresholds = "PREFIX individuos: <" + AIRSNAMESPACE + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + " SELECT ?successthresholdlow ?successthresholdhigh"
                + " WHERE {"
                + "?threat a individuos:" + intType + "."
                + "?threat individuos:successThresholdLow ?successthresholdlow ."
                + "?threat individuos:successThresholdHigh ?successthresholdhigh ."
                + " } ";

        Query q = QueryFactory.create(thresholds);
        QueryExecution qe = QueryExecutionFactory.create(q, ontologyModel);
        ResultSet rs = qe.execSelect();

        List<QuerySolution> lista = ResultSetFormatter.toList(rs);

        for (int i = 0; i < lista.size(); i++) {
            QuerySolution solution = lista.get(i);
            //solution.get("successThresholdHihg");
            String threshold_low_string = solution.get("successthresholdlow").toString();
            double threshold_low = Double.parseDouble(threshold_low_string.substring(0, threshold_low_string.indexOf("^")));
            String threshold_high_string = solution.get("successthresholdhigh").toString();
            double threshold_high = Double.parseDouble(threshold_high_string.substring(0, threshold_high_string.indexOf("^")));
            tst.setThreatName(intType);
            tst.setThreatThresholdLow(threshold_low);
            tst.setThreatThresholdHigh(threshold_high);
        }

        return tst;

    }

    private double[] getAnomalyVariance(HashMap intrusionMap, HashMap responseAnomalyMap) {
        HashMap intrusionAn = intrusionMap;
        Iterator it = intrusionAn.keySet().iterator();
        double[] nn_input = new double[9];
        try {
            int num = 0;

            while (it.hasNext()) {
                String indiName = (String) it.next();
                double anomaly_parameter_intrusion = (Integer) intrusionAn.get(indiName);
                double anomaly_parameter_response = (Integer) responseAnomalyMap.get(indiName);
                System.out.println("Anomalia antes--> Parametro: " + indiName + " ; Valor: " + anomaly_parameter_intrusion);
                System.out.println("anomalia despues--> Parametro: " + indiName + " ; Valor: " + anomaly_parameter_response);

                nn_input[num] = (anomaly_parameter_response - anomaly_parameter_intrusion);
                num++;
            }
        } catch (Exception e) {
            Logger.getLogger(EvaluationSystemExecutor.class.getName()).log(Level.SEVERE, null, e);
        }
        return nn_input;

    }
    
    private double[] getResponseAnomaly(HashMap responseAnomalyMap) {
        HashMap responseAn = responseAnomalyMap;
        Iterator it = responseAn.keySet().iterator();
        double[] nn_input = new double[9];
        try {
            int num = 0;

            while (it.hasNext()) {
                String indiName = (String) it.next();
                double anomaly_parameter_response = (Integer) responseAnomalyMap.get(indiName);
                nn_input[num] = anomaly_parameter_response;
                num++;
            }
        } catch (Exception e) {
            Logger.getLogger(EvaluationSystemExecutor.class.getName()).log(Level.SEVERE, null, e);
        }
        return nn_input;

    }

    private void initializeOntModel() {
        synchronized (ontology_airs_uri) {
            ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            RDFReader inf_modelReader = ontologyModel.getReader();
            inf_modelReader.setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE", "EM_IGNORE");
            inf_modelReader.read(ontologyModel, ontology_airs_uri);
        }
        // _log.info("ontologia leida");
    }
}
