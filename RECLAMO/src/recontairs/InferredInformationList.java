/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package recontairs;

import context.entropy.variance.ContextAnomalyIndicator;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author root
 */
public class InferredInformationList {
    //Attributes
    private ArrayList<InferredInformation> _inferredList;
    //Constructors
    public InferredInformationList(){
        _inferredList = new ArrayList<InferredInformation>();
    }
    
    public void addInferredInformation(InferredInformation newElement){
        if (newElement != null){
            getInferredInformationList().add((newElement));
        }
        else{
            System.out.println("Error: [InferredInformationList] [addINferredInformation] "
                + "Unable to add the Inferred Information element:" + newElement.getIP());
        }
    }
    
    public void printInferredInformationList(){
        Iterator i = getInferredInformationList().iterator();
        while(i.hasNext()){
            InferredInformation indicator = (InferredInformation)i.next();
            System.out.println(indicator.getHostname());
            System.out.println(indicator.getIP());
            System.out.println(indicator.getResponseID());
            System.out.println(indicator.getSubnetwork());
            indicator.getContextAnomalyIndicatorList().printContextAnomalyIndicatorList();
        }
    }

    public InferredInformation getExistInferredInformationIndicators(String valueName){
        InferredInformation contAnomalyIndicator;
        Iterator i = getInferredInformationList().iterator();
        while(i.hasNext()){
            contAnomalyIndicator = (InferredInformation)i.next();
            if (contAnomalyIndicator.getIP().equalsIgnoreCase(valueName)){
                return contAnomalyIndicator;
            }
        }
        return null;
    }
    
    public ArrayList<InferredInformation> getInferredInformationList(){
        return _inferredList;
    }

    public void setInferredInformationList(ArrayList<InferredInformation> value){
        _inferredList = value;
    }

    public void cleanInferredInformationList(){
        _inferredList.clear();
    }
    
}