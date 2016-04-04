/**
 * "Utils" Java class is free software: you can redistribute
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

/**
 * This class represents common actions or functions which can be useful for 
 * other Java classes of the same package.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
import java.util.*;

public class Utils {
	private Utils() {/**/}

	public static long toMillis(int seconds) {
		return ((long) seconds) * 1000;
	}

	public static Date toDate(int seconds) {
		return new Date(toMillis(seconds));
	}

	/**
	 * Determines if the given <code>String</code> is <code>null</code> or empty
	 * @param val the <code>String</code> to check
	 * @return <code>true</code> if <code>val</code> is <code>null</code> or empty, <code>false</code>
	 *         otherwise
	 */
	public static boolean isNullOrEmpty(String val) {
		return val == null || val.length() == 0;
	}

	public static int minPositive(int n1, int n2, int n3) {
		return n1 == -1 && n2 == -1? n3: n1 == -1 && n3 == -1? n2: n2 == -1 && n3 == -1? n1
			: n1 == -1? Math.min(n2, n3): n2 == -1? Math.min(n1, n3): n3 == -1? Math.min(n1, n2): min(
				n1, n2, n3);
	}

	public static int min(int... n) {
		Arrays.sort(n);
		return n[0];
	}

	public static int minPositive(int... n) {
		Arrays.sort(n);
		for(int i = n.length - 1; i <= 0; i--)
			if (n[i] >= 0)
				return n[i];
		return Integer.MAX_VALUE;
		// Set<Integer> s = new HashSet<Integer>();
		// for(int nn : n)
		// if (nn >= 0)
		// s.add(nn);
	}

	public static long getInitOfDay(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public static String fillString(String string, char fillCharacter, int desiredLength) {
		if (desiredLength < string.length())
			throw new IllegalArgumentException("String length greater than desired length: " + string
				+ "  " + string.length() + "   " + desiredLength);
		if (desiredLength == string.length())
			return string;
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < (desiredLength - string.length()); i++)
			s.append(fillCharacter);
		return s.toString() + string;
	}

	/** If it needs to trim the string, the final part remains */
	public static String stringToFixedLength(String string, char fillCharacter, int desiredLength) {
		if (desiredLength < string.length())
			return string.substring(string.length() - desiredLength, string.length());
		if (desiredLength == string.length())
			return string;
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < (desiredLength - string.length()); i++)
			s.append(fillCharacter);
		return s.toString() + string;
	}

	public static int count(String string, char character, boolean atstart) {
		int n = 0;
		for(int i = 0; i < string.length(); i++)
			if (string.charAt(i) == character)
				n++;
			else if (atstart)
				break;
		return n;
	}
}
