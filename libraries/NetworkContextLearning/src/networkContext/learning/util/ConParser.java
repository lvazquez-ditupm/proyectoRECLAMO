/**
 * "ConParser" Java class is free software: you can redistribute
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

package networkContext.learning.util;

import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import networkContext.learning.InfoNetwork;

/**
 * This class parses and processes one line of SANCP logs.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class ConParser {
    public static InfoNetwork convertLine(String line){
        //System.out.println("Linea a parsear: "+line);
        //sancp_id,start_time_gmt,duration,eth_proto,ip_proto,src_ip_dotted,src_port,dst_ip_dotted,dst_port,src_pkts,dst_pkts,src_bytes,dst_bytes,total_bytes
        Date date=new Date();
        int duration, eth_proto, ip_proto, portSrc;
        String ipSrc, ipDes;
        int portDes, src_pkts, dst_pkts, src_bytes, dst_bytes, total_bytes;
        
        StringTokenizer tokensLine= new StringTokenizer(line,"|");
        String id=tokensLine.nextToken();
        String date1=tokensLine.nextToken();
        try {
           date=DateFormatter.stringToDate(date1, "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException ex) {
            Logger.getLogger(ConParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        duration=Integer.parseInt(tokensLine.nextToken());
        eth_proto=Integer.parseInt(tokensLine.nextToken());
        ip_proto=Integer.parseInt(tokensLine.nextToken());
        ipSrc=tokensLine.nextToken();
        portSrc=Integer.parseInt(tokensLine.nextToken());
        ipDes=tokensLine.nextToken();
        portDes=Integer.parseInt(tokensLine.nextToken());
        src_pkts=Integer.parseInt(tokensLine.nextToken());
        dst_pkts=Integer.parseInt(tokensLine.nextToken());
        src_bytes=Integer.parseInt(tokensLine.nextToken());
        dst_bytes=Integer.parseInt(tokensLine.nextToken());
        total_bytes=Integer.parseInt(tokensLine.nextToken());
         //public InfoCon(Date date, int duration, int eth_proto, int ip_proto, int portSrc, int portDes, int src_pkts, int dst_pkts, int src_bytes, int dst_bytes, int total_bytes, String ipSrc, String ipDes) {

        return new InfoNetwork(date,duration,eth_proto,ip_proto,portSrc,portDes,src_pkts,dst_pkts,src_bytes,dst_bytes,total_bytes,ipSrc, ipDes);
    }
}
