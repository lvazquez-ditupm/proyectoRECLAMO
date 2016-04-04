
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

import java.util.logging.Logger;

/**
 * This class contains the information stored in the Address element of an Idmef Message
 */
public class Address {
	
	private String ident;
	private String category;
	private String vlan_name;
	private String vlan_num;
	private String address;
	private String netmask;
	
	
	/**
	 * 
	 */
	public Address() {
		ident = null;
                category= null;
                vlan_name = null;
                vlan_num= null;
                address=null;
                netmask =null;
                
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
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		if("unknown".equals(category)||"atm".equals(category)||"e-mail".equals(category)||
				"lotus-notes".equals(category)||"mac".equals(category)||"sna".equals(category)||
				"vm".equals(category)||"ipv4-addr".equals(category)||"ipv4-addr-hex".equals(category)||
				"ipv4-net".equals(category)||"ipv4-net-mask".equals(category)||"ipv6-addr".equals(category)||
				"ipv6-addr-hex".equals(category)||"ipv6-net".equals(category)||"ipv6-net-mask".equals(category))
		this.category = category;
		
	}

	/**
	 * @return the vlan_name
	 */
	public String getVlan_name() {
		return vlan_name;
	}

	/**
	 * @param vlan_name the vlan_name to set
	 */
	public void setVlan_name(String vlan_name) {
		this.vlan_name = vlan_name;
	}

	/**
	 * @return the vlan_num
	 */
	public String getVlan_num() {
		return vlan_num;
	}

	/**
	 * @param vlan_num the vlan_num to set
	 */
	public void setVlan_num(String vlan_num) {
		this.vlan_num = vlan_num;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the netmask
	 */
	public String getNetmask() {
		return netmask;
	}

	/**
	 * @param netmask the netmask to set
	 */
	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}
	
	public String getClassname(){
		return "Address";
	}
	
	

}
