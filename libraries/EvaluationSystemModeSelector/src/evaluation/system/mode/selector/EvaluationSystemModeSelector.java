/**
 * "EvaluationSystemModeSelector" Java class is free software: you can redistribute
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
 * along with this program (lgpl.txt).  If not, see
 * <http://www.gnu.org/licenses/>
 */
package evaluation.system.mode.selector;

import evaluation.system.executor.EvaluationSystemExecutor;
import evaluation.system.executor.ResponseTotalEfficiency;
import evaluation.system.trainer.EvaluationSystemTrainerInit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.logging.Logger;

/**
 * This class represents a component responsible for selecting the operation
 * mode to run the intrusion response evaluation module: training or execution.
 * Also, this component checks if the required parameters are provided in the
 * right way.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class EvaluationSystemModeSelector {

    private static Logger _log = Logger.getLogger(EvaluationSystemModeSelector.class.getName());
    private ExecutionModeParams paramsEx;
    private TrainingModeParams paramsTr;
    private String mode;
    private EvaluationSystemExecutor executor;
    private EvaluationSystemTrainerInit trainer;
    private ResponseTotalEfficiency resultado;
    private double threshold;

    /*Constructor de la clase si los parametros son en modo ejecucion*/
    public EvaluationSystemModeSelector(String mode, ExecutionModeParams paramsEx) {
        this.mode = mode;
        this.paramsEx = paramsEx;
    }

    /* Constructor de la clase si los parametros son en modo entrenamiento */
    public EvaluationSystemModeSelector(String mode, TrainingModeParams paramsTr) {
        this.mode = mode;
        this.paramsTr = paramsTr;

    }

    public boolean start() {
        /*Alguien ha invocado al sistema de evaluación del exito de  la respuesta */
        long startTime = System.currentTimeMillis();
        System.out.println("**** INIT EvaluationSystemModeSelector at " + mode + " mode. ****");

        if (checkModeParams(mode)) {
            //Invocamos al modulo encargado de ejecutar el modo seleccionado.
            if (mode.equalsIgnoreCase("execution")) {
                String intrusionType = paramsEx.getIntrusionType();
                String responseID = paramsEx.getResponseID();
                String responseType = paramsEx.getResponseType();
                String targetip = paramsEx.getTargetIP();
                //HashMap anomalyMap = paramsEx.getAnomalyMap();
                String[] addParam = paramsEx.getAdParam();
                executor = new EvaluationSystemExecutor(intrusionType, responseID, responseType, targetip, null, addParam);
                if (executor.init()) {
                    resultado = executor.getRTE();
                    long endTime = System.currentTimeMillis();
                    System.out.println("**** END EvaluationSystemModeSelector at " + mode + " mode *** Total time : " + (endTime - startTime) + " (ms)****");
                    return true;
                } else {
                    return false;
                }

            } else if (mode.equalsIgnoreCase("training")) {
                String intrusionType = paramsTr.getIntrusionType();
                String systemIP = paramsTr.getTrainingSystemIP();
                String systemNet = paramsTr.getTrainingSystemNetmask();
                String trainingSetType = paramsTr.getTrainingSetType();
                ArrayList<String> trainingSet = paramsTr.getTrainingSet();
                String[] addParam = paramsTr.getAdParam();
                int num = paramsTr.getNumberOfExecution();
                int port = paramsTr.getTrainingSystemPort();
                trainer = new EvaluationSystemTrainerInit(trainingSet, num, trainingSetType, intrusionType, port, systemIP, systemNet, addParam);

                threshold = trainer.init();
                long endTime = System.currentTimeMillis();
                System.out.println("Threshold: " + threshold);
                System.out.println("**** END EvaluationSystemModeSelector at " + mode + " mode *** Total time : " + (endTime - startTime) + " (ms)****");
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    private boolean checkModeParams(String mode) {
        if (mode.equals("execution")) {
            /* if ((paramsEx.getResponseID() != null) && (paramsEx.getIntrusionType() != null) && (paramsEx.getTargetIP() != null)) {
                if (paramsEx.getAnomalyMap().size() == 9) {
                    HashMap map = paramsEx.getAnomalyMap();
                    Iterator anomalyit = map.keySet().iterator();
                    while (anomalyit.hasNext()) {
                        try {
                            int anomalyValue = (Integer) map.get(anomalyit.next().toString());
                        } catch (Exception e) {
                            _log.warning("Ha habido un error obteniendo parámetros de anomalía");
                            return false;
                        }
                    }
                    return true;
                } else {
                    _log.warning("El número de indicadores de contexto no es correcto");
                    return false;
                }
            } else {
                return false;
            }
        } */
            if ((paramsEx.getResponseID() != null) && (paramsEx.getIntrusionType() != null) && (paramsEx.getTargetIP() != null)) {
                return true;
            } else {
                return false;
            }
        } else if (mode.equals("training")) {
            paramsTr.PrintTrainingSet();
            System.out.println("IntrusionTYpe: " + paramsTr.getIntrusionType());
            System.out.println("Mode: " + paramsTr.getMode());
            System.out.println("TrainingSEt: " + paramsTr.getTrainingSetType());
            System.out.println("NumberOfExecution: " + paramsTr.getNumberOfExecution());
            if ((paramsTr.getTrainingSetType() != null) && (paramsTr.getNumberOfExecution() > 0)) {
                if ((paramsTr.getTrainingSet().size() > 0)) {
                    return true;
                } else {
                    _log.warning("El conjunto de entrenamiento está vacio");
                    return false;
                }
            } else {
                _log.warning("no se ha especificado el tipo de conjunto de entrenamiento o tipo de intrusion");
                return false;
            }
        } else {
            _log.warning("El modo seleccionado no es correcto");
            return false;
        }
    }

    public ResponseTotalEfficiency getExecutionModeResult() {
        return resultado;
    }

    public double getTrainingModeResult() {
        return threshold;
    }
}
