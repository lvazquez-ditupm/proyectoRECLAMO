/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package recontairs;

import context.entropy.variance.ContextAnomalyIndicatorList;

/**
 *
 * @author root
 */
public class InferredInformation {

    private String _IP;
    private String _hostname;
    private ContextAnomalyIndicatorList _contAnomalyList;
    private String _subnetwork;
    private String _responseID;
    private String _responseType;
    private String _targetLevelOfImportance;
    

    public InferredInformation() {
        _IP=null;
        _hostname=null;
        _contAnomalyList = null;
        _subnetwork = null;
        _responseID = null;
        _responseType = null;
        _targetLevelOfImportance = null;
    }

    public InferredInformation(String IP, String hostname, ContextAnomalyIndicatorList contextAnomalyIndicatorList, String subnet, String responseID, String responseType, String tloi) {
        _IP=IP;
        _hostname=hostname;
        _contAnomalyList = contextAnomalyIndicatorList;
        _subnetwork = subnet;
        _responseID = responseID;
        _responseType = responseType;
        _targetLevelOfImportance = tloi;
    }

    public String getIP() {
        return _IP;
    }

    public String getHostname() {
        return _hostname;
    }
    
    public String getSubnetwork() {
        return _subnetwork;
    }
    
    public String getResponseID() {
        return _responseID;
    }
    
    public ContextAnomalyIndicatorList getContextAnomalyIndicatorList() {
        return _contAnomalyList;
    }

    public String getResponseType(){
        return _responseType;
    }
    
    public String getLevelOfImportance(){
        return _targetLevelOfImportance;
    }
    
    public void setIP(String value) {
        _IP = value;
    }

    public void setHostname(String value) {
        _hostname = value;
    }
    
    public void setSubnetwork(String value) {
        _subnetwork = value;
    }
    
    public void setResponseID(String value) {
        _responseID =value;
    }
    
    public void setResponseType(String value){
        _responseType = value;
    }
    
    public void setLevelOfImportance(String value){
        _targetLevelOfImportance = value;
    }
    
    public void setContextAnomalyIndicatorList(ContextAnomalyIndicatorList value) {
        _contAnomalyList = value;
    }    
}