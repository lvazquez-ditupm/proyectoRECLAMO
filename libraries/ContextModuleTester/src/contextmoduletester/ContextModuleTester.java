/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package contextmoduletester;

import network.context.mode.selector.NetLearningModeParams;
import network.context.mode.selector.NetworkContextModeSelector;
import system.context.mode.selector.LearningModeParams;
import system.context.mode.selector.AnomalyDetectionModeParams;
import network.context.mode.selector.NetAnomalyDetectionModeParams;
import system.context.mode.selector.SystemContextModeSelector;
import systemContext.anomalyDetector.systemCorrelator.SystemAnomaly;

/**
 *
 * @author root
 */
public class ContextModuleTester {
    static LearningModeParams system_learning_params;
    static AnomalyDetectionModeParams system_anomaly_params;
    static String[] initAdParam;
    static NetLearningModeParams net_learning_params;
    static NetAnomalyDetectionModeParams net_anomaly_params;
    static SystemAnomaly an;

    public static  void main(String args [] ) {
        if (args.length < 2){
            System.out.println("EL número de argumentos es inválido");
        }
        else{
            String context_type = args[0];
            String mode = args[1];
            if (context_type.equalsIgnoreCase("network")){
                if(mode.equalsIgnoreCase("learning")){
                    if (args.length == 4){
                        net_learning_params = new NetLearningModeParams(args[2], Integer.parseInt(args[3]), null);
                        NetworkContextModeSelector netsel = new NetworkContextModeSelector(mode,net_learning_params);
                        if(netsel.start()){}
                        else{
                            System.out.println("Fallo an el proceso de aprendizadje del contexto de red.");
                        }                        
                    }                    
                }
                else if (mode.equalsIgnoreCase("anomalydetection")){
                    if(args.length == 3){
                        net_anomaly_params = new NetAnomalyDetectionModeParams (args[2],null);
                        NetworkContextModeSelector netsel = new NetworkContextModeSelector(mode,net_anomaly_params);
                        if(netsel.start()){
                            int resultado = netsel.getNetworkAnomaly();
                            System.out.println("El grado de anomalia del contexto de red es: "+resultado);
                        }
                    }                    
                }
                else System.out.println ("Modo de ejecución no reconocido por el sistema");
                
            }
            else if(context_type.equalsIgnoreCase("system")){
                if(mode.equalsIgnoreCase("learning")){
                    system_learning_params = new LearningModeParams();
                    SystemContextModeSelector syssel = new SystemContextModeSelector(mode, system_learning_params);
                    if(syssel.start()){}
                    else{
                        System.out.println("Fallo an el proceso de aprendizadje del contexto de red.");
                    }                        
                }
                else if (mode.equalsIgnoreCase("anomalydetection")){
                    if (args.length == 4){
                        system_anomaly_params = new AnomalyDetectionModeParams(args[2], args[3], null);
                        SystemContextModeSelector syssel = new SystemContextModeSelector(mode, system_anomaly_params);
                        if(syssel.start()){
                            an = syssel.getSystemAnomaly();
                            an.printAnomaly();
                        }     
                    }
                }
                else System.out.println ("Modo de ejecución no reconocido por el sistema");
                
            }else{
                System.out.println("Tipo de contexto no reconocido por el sistema");
            }            
        }
    }    
}