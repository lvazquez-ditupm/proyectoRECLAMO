/**
 * "SidsGroup" Java class is free software: you can redistribute
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

package airs.responses.executor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents a group of intrusion identifiers.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class SidsGroup {
    private String _nameSidsGroup;
    private ArrayList<Integer> _sidsList;

    public SidsGroup() {
        this._nameSidsGroup= "ALL";
        this._sidsList = new ArrayList<Integer>();
    }
    
    public SidsGroup(String _nameSidsGroup, ArrayList<Integer> _sidsList) {
        this._nameSidsGroup = _nameSidsGroup;
        this._sidsList = _sidsList;
    }

    public String getNameSidsGroup() {
        return _nameSidsGroup;
    }

    public ArrayList<Integer> getSidsList() {
        return _sidsList;
    }

    public void setNameSidsGroup(String _nameSidsGroup) {
        this._nameSidsGroup = _nameSidsGroup;
    }

    public void setSidsList(ArrayList<Integer> _sidsList) {
        this._sidsList = _sidsList;
    }
    
    public boolean getExistSid(Integer sid){
        Iterator i = this._sidsList.iterator();
        while(i.hasNext()){
            if(i.next()==sid){
                return true;
            }
        }
        return false;
    }
    
    @Override public String toString(){
        String sids=new String();
        Iterator i=this._sidsList.iterator();
        while(i.hasNext()){
            sids+=i.next()+" ";
        }
        return this._nameSidsGroup +": "+sids;
    }
}
