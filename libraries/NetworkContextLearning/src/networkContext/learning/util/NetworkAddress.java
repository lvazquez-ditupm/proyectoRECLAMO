/**
 * "NetworkAddress" Java class is free software: you can redistribute
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
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class manages IP addresses and checks if one IP address is located in a
 * specific subnetwork.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class NetworkAddress {
        private final InetAddress ip;
	private final byte mask;

	public NetworkAddress(String s) throws NumberFormatException, UnknownHostException {
		this(InetAddress.getByName(s.split("/")[0]), Byte.parseByte(s.split("/")[1]));
	}

	public NetworkAddress(String ip, String mask) throws UnknownHostException {
		this.ip = InetAddress.getByName(ip);
		int m = 0;
		for(String s : mask.split("\\.")) {
			int mm = Utils.count(Utils.stringToFixedLength(Integer.toBinaryString(Integer.parseInt(s)),
				'0', 8), '1', true);
			m += mm;
			if (mm != 8)
				break;
		}
		this.mask = (byte) m;
	}

	public NetworkAddress(InetAddress ip, byte mask) {
		if (mask < 1 || mask > 32)
			throw new IllegalArgumentException("Mask not valid: " + mask);
		this.ip = ip;
		this.mask = mask;
	}

	/** @return the CIDR format */
	@Override
	public String toString() {
		return ip.getHostAddress() + "/" + mask;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NetworkAddress))
			return false;
		NetworkAddress o = (NetworkAddress) obj;
		if (mask != o.mask)
			return false;
		byte[] b1 = ip.getAddress();
		byte[] b2 = o.ip.getAddress();
		for(int i = 0, m = mask; i < 4; i++, m -= 8) {
			String bs1 = Utils.stringToFixedLength(Integer.toBinaryString(b1[i]), '0', 8);
			String bs2 = Utils.stringToFixedLength(Integer.toBinaryString(b2[i]), '0', 8);
			int t = Math.min(8, m);
			if (!bs1.substring(0, t).equals(bs2.substring(0, t)))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (ip != null? ip.hashCode(): 0);
		hash = 97 * hash + mask;
		return hash;
	}

	public boolean isSameNetwork(InetAddress ip) {
		byte[] b1 = this.ip.getAddress();
		byte[] b2 = ip.getAddress();
		byte m = mask;
		for(int i = 0; i < 4; i++) {
			if (m >= 8 && b1[i] != b2[i])
				return false;
			// TEST
			m -= 8;
		}
		return true;
	}

}
