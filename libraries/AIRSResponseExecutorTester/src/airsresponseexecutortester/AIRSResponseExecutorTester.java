/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package airsresponseexecutortester;

import airs.responses.executor.CentralModuleExecution;
import airs.responses.executor.ResponseActionParams;

/**
 *
 * @author root
 */
public class AIRSResponseExecutorTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ResponseActionParams params = new ResponseActionParams();
        params.setMainIP("10.1.100.26");
        params.setPeerIP("192.168.100.130");
        params.setPortConn("80");
      
        String respuesta = args[0];
        // TODO code application logic here
        
        
        
        CentralModuleExecution MCER;
        MCER = new CentralModuleExecution();   
        try{
        if (MCER.BuildResponseActionRequest(respuesta, params)){
            System.out.println("RESPUESTA EJECUTADA!!");
            
        }else System.out.println("No ha sido posible ejecutar la respuesta");
    } catch (Exception e) {System.out.println(e);}
    }
}
