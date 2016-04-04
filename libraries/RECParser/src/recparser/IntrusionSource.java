
/*This file is a part of OntOrBAC
 
OntOrBAC is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

OntOrBAC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 */

/**
 * @author Javier Guerra
 * @version 1.0
 **/

package recparser;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * This class contains the information stored in the Source element of an Idmef Message
 */
public class IntrusionSource {
	
	private String ident;
	private String interfaceSource;
	private String spoofed;
        private String _attackerLocation; //<Source><Node><Location>
        private String _sourceName; //<Source><Node><name>
        private int _portSrc; //<Source><Service><port>
        private String _protocol;   //<Source><Service><protocol>
	private List <Address> nodes;
		

	/**
	 * 
	 */
	public IntrusionSource() {
            ident = null;
            interfaceSource = null;
            spoofed = null;
            _attackerLocation= null;
            _sourceName= null;
            _portSrc= -1;
            _protocol= null;
            nodes= new ArrayList<Address>();
	
	}


	/**
	 * @return the ident
	 */
	public String getIdent() {
		return ident;
	}


	/**
	 * @param ident the ident to set
	 */
	public void setIdent(String ident) {
		this.ident = ident;
	}


	/**
	 * @return the interfaceSource
	 */
	public String getInterfaceSource() {
		return interfaceSource;
	}


	/**
	 * @param interfaceSource the interfaceSource to set
	 */
	public void setInterfaceSource(String interfaceSource) {
		this.interfaceSource = interfaceSource;
	}


	/**
	 * @return the spoofed
	 */
	public String getSpoofed() {
		return spoofed;
	}


	/**
	 * @param spoofed the spoofed to set
	 */
	public void setSpoofed(String spoofed) {
		if (spoofed.equals("unknown")||spoofed.equals("no")||spoofed.equals("yes")){
			this.spoofed = spoofed;
		}else System.out.println(spoofed);
		
		
	}


	/**
	 * @return the node
	 */
	public List <Address> getNode() {
		return nodes;
	}


	/**
	 * @param node the node to set
	 */
	public void setNode(Address node) {
		nodes.add(node);
	}


	
	
	public String getClassname(){
		return "Source";
	}

        
        public String getAttackerLocation(){
            return _attackerLocation;
        }    
  

        public String getSourceName(){
            return _sourceName;
        }
     
        public int getPortSrc(){
            return _portSrc;
        }
         
        public String getProtocol(){
            return _protocol;
        }
         public void setAttackerLocation(String value){
            _attackerLocation = value;
        }    
  

        public void setSourceName(String value){
            _sourceName = value;
       }
       public void setPortSrc(int value){
            _portSrc = value;
        }
        public void setProtocol(String value){
            _protocol = value;
        }
}
