/**
 * "LinuxUtils" Java class is free software: you can redistribute
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
 * This class allows to execute or run external services, in the same way that
 * using the command line.
 * 
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinuxUtils {
	private static Logger logger = Logger.getLogger(LinuxUtils.class.getName());

	public static void setLogger(Logger l) {
		if (l != null)
			logger = l;
	}

	public static boolean killProcess(String programName) {
		Set<Integer> set = getProcessId(programName);
		if (set.size() == 1) {
			System.out.println("Killing process id: " + set.iterator().next());
			killProcess(set.iterator().next());
			return true;
		}
		System.err.println("Not a single match found using program name '" + programName + "': " + set);
		return false;
	}

	public static boolean isSpecificProcessRunning(int processid, String programName){
            if (!programName.matches("[a-z\\-_0-9]+"))
			throw new IllegalArgumentException("Name not valid: " + programName);
	    Process p = runCommand("ps -e | grep " + Integer.toString(processid) + " | grep " + programName + " | awk '{print $1}'", false, false, true);
            try {
                InputStream is = p.getInputStream();
		byte[] buffer = new byte[1024];
		Set<Integer> set = new HashSet<Integer>();
		for(int count = 0; (count = is.read(buffer)) >= 0;)
                    for(String ss : new String(Arrays.copyOf(buffer, count - 1)).split("\\s"))
                        set.add(Integer.parseInt(ss));
		return !set.isEmpty();
		} catch(Exception ex) {
			logger.log(Level.WARNING, "", ex);
		}
		return !Collections.emptySet().isEmpty();
            
        }
        
        public static boolean isProcessRunning(String programName) {
		return !getProcessId(programName).isEmpty();
	}

	/** @return The list of process ids matched by the programName. */
	public static Set<Integer> getProcessId(String programName) {
		if (!programName.matches("[a-z\\-_0-9]+"))
			throw new IllegalArgumentException("Name not valid: " + programName);
		Process p = runCommand("ps -e | grep " + programName + " | awk '{print $1}'", false, false, true);
		try {
			InputStream is = p.getInputStream();
			byte[] buffer = new byte[1024];
			Set<Integer> set = new HashSet<Integer>();
			for(int count = 0; (count = is.read(buffer)) >= 0;)
				for(String ss : new String(Arrays.copyOf(buffer, count - 1)).split("\\s"))
					set.add(Integer.parseInt(ss));
			return set;
		} catch(Exception ex) {
			logger.log(Level.WARNING, "", ex);
		}
		return Collections.emptySet();
	}

	public static boolean killProcess(int processid) {
            try{
                //System.out.println ("*********************************MATANDO PROCESO*******************");
		int result = Runtime.getRuntime().exec("kill " + processid).waitFor();
                if (result == 0){
                    return true;
                }
                else{return false;}
            }
            catch(Exception e){
                logger.log(Level.WARNING, "", e);
                return false;
            }
	}
        
        public static Process runCommand(String cmd) {
		return runCommand(cmd, false, true, true);
	}

	public static Process runCommand(String cmd, boolean wait, boolean printStdOut, boolean printErrOut) {
		try {
			String[] command = {"sh", "-c", cmd};
                        final Process process = Runtime.getRuntime().exec(command);
			if (printStdOut)
				new Thread() {
					@Override
					public void run() {
						try {
                                                    
							InputStream is = process.getInputStream();
							byte[] buffer = new byte[1024];
							for(int count = 0; (count = is.read(buffer)) >= 0;)
								logger.finest("\t" + new String(buffer, 0, count - 1));
                                                        

						} catch(Exception ex) {
							logger.log(Level.WARNING, "", ex);
						}
					}
				}.start();
			if (printErrOut)
				new Thread() {
					@Override
					public void run() {
						try {
							InputStream is = process.getErrorStream();
							byte[] buffer = new byte[1024];
                                                        for(int count = 0; (count = is.read(buffer)) >= 0;)
								logger.warning("\t" + new String(buffer, 0, count - 1));
						} catch(Exception ex) {
                                                        logger.log(Level.WARNING, "", ex);
						}
					}
				}.start();
			if (wait) {
				int returnCode = process.waitFor();
				logger.finer("\tReturn code = " + returnCode);
			}
			return process;
		} catch(Exception ex) {
			logger.log(Level.WARNING, "", ex);
		}
		return null;
	}
      
	public static boolean createNamedPipe(String pathName, Permissions p) {
		String cmd = "mknod " + ((p == null)? "": ("--mode=" + p + " ")) + pathName + " p";
		return runCommand(cmd, true, false, true).exitValue() == 0;
	}

	public static boolean setGroup(File file, String owner, String group) {
		String cmd = "chown " + owner + ":" + group + " " + file.getAbsolutePath();
		return runCommand(cmd, true, false, true).exitValue() == 0;
	}

	// IMPROVE linux permissions managing
	public static class Permissions {
		public final String s;

		public Permissions(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}
}
