/**
 * "SancpManager" Java class is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version always keeping the additional terms
 * specified in this license.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *
 * Additional Terms of this License --------------------------------
 *
 * 1. It is Required the preservation of specified reasonable legal notices and
 * author attributions in that material and in the Appropriate Legal Notices
 * displayed by works containing it.
 *
 * 2. It is limited the use for publicity purposes of names of licensors or
 * authors of the material.
 *
 * 3. It is Required indemnification of licensors and authors of that material
 * by anyone who conveys the material (or modified versions of it) with
 * contractual assumptions of liability to the recipient, for any liability that
 * these contractual assumptions directly impose on those licensors and authors.
 *
 * 4. It is Prohibited misrepresentation of the origin of that material, and it
 * is required that modified versions of such material be marked in reasonable
 * ways as different from the original version.
 *
 * 5. It is Declined to grant rights under trademark law for use of some trade
 * names, trademarks, or service marks.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (lgpl.txt). If not, see
 * <http://www.gnu.org/licenses/>
 */
package networkContext.anomalyDetector.sancp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import networkContext.anomalyDetector.util.LinuxUtils;
import networkContext.anomalyDetector.util.PropsUtil;
import java.util.Set;

/**
 * This class starts and stops the SANCP service to capture in real time the
 * network traffic in a specific interface, and invokes the SnapshotCreator
 * class, which generates the snapshot of the network connections during the
 * execution of SANCP.
 *
 * @author UPM (member of RECLAMO Development Team) (http://reclamo.inf.um.es)
 * @version 1.0
 */
public class SancpManager {

    private volatile PropsUtil props = new PropsUtil();
    private Process sancp;
    private static volatile ArrayList<Integer> pids = new ArrayList<>();
    private final String sancpDir;
    private final String interfaceID;
    private final String sancpConf;
    private final String sancpLog;
    private static final Object lock = new Object();

    public SancpManager(String iface) {
        this.interfaceID = iface;
        sancpDir = props.getNetworkContextSancpSensorPathValue() + "/" + Thread.currentThread();
        sancpConf = props.getNetworkContextSancpConfigFilePathValue();
        sancpLog = props.getNetworkContextSancpLogFilePathValue();

    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        folder.delete();
    }

    synchronized private int startSancp() {

        File folder = new File(sancpDir);
        deleteFolder(folder);
        folder.mkdir();
        int pid = -1;
        int equality = 0;
        int testUnique = 0;
        String command = "sudo sancp --human-readable -d " + sancpDir + " -i " + interfaceID + " -c " + sancpConf + " > " + sancpLog;
        try {
            System.out.println("Starting sancp: " + command);
            // Se ejecuta el comando para arrancar sancp sin ningún log interno
            sancp = LinuxUtils.runCommand(command, false, false, false);
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            // Devuelve un set con los PID de todos los procesos de sancp en ejecución y se pasa a un array  
            Set<Integer> setPids = new HashSet<>();
            while (setPids.size() <= pids.size()) {
                setPids = LinuxUtils.getProcessId("sancp");
            }

            int[] ids = new int[setPids.size()];
            int i = 0;
            for (Integer val : setPids) {
                ids[i++] = val;
            }

            // Si la lista está vacía, se añade el único valor del array (el PID creado en este hilo)
            if (pids.isEmpty()) {
                pids.add(ids[0]);
                pid = ids[0];

                // Si tiene valores, se comparan el array y la lista. El valor del array que no está en la lista es
                // el PID creado, que se añade a la lista.
            } else {

                for (int j = 0; j < ids.length; j++) {
                    for (int k = 0; k < pids.size(); k++) {
                        if (pids.get(k) == ids[j]) {
                            equality++;
                        }
                    }
                    if (equality == 0) {
                        testUnique++;
                        if (testUnique == 1) {
                            pids.add(ids[j]);
                            pid = ids[j];

                            // Si hubiera más de un valor distinto (un hilo crea varios procesos), se eliminan.
                        } else {
                            try {
                                sancp.destroy();
                                LinuxUtils.killProcess(ids[j]);
                            } catch (Exception e) {
                            }
                        }
                    }
                    equality = 0;
                }
            }
            if (pid == -1) {
                System.out.println("HOLA");
            }
            return pid;
        }
    }

    private boolean isSancpRunning(int processid) {
        return LinuxUtils.isSpecificProcessRunning(processid, "sancp");
    }

    private boolean stopSancp(int pid) {
        synchronized (pids) {
            try {

                if (isSancpRunning(pid) && sancp != null) {
                    int sancppid = pid;
                    try {
                        try {
                            sancp.destroy();
                            LinuxUtils.killProcess(sancppid);
                        } catch (Exception e) {
                        }

                        // Si se ha matado al proceso, se elimina el PID de la lista.
                        for (int i = 0; i < pids.size(); i++) {
                            if (pids.get(i) == sancppid) {
                                pids.remove(i);
                            }
                        }
                        System.out.println("Mata el proceso sancp " + sancppid);

                        return true;

                    } catch (Exception e) {
                        System.out.println("Error matando el proceso " + e);
                        return false;
                    }
                }
                return true;

            } catch (Exception e) {
                System.err.println(e);
                return false;
            }
        }
    }

    // Se intenta matar al proceso con un determinado PID hasta que se consigue
    private Snapshot tryStopSancp(int pidsancp) {

        Snapshot snap = new SnapshotCreator().createSnap(sancpDir, interfaceID);

        if (snap != null) {
            while (!stopSancp(pidsancp)) {
            }
            return snap;

        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
            return tryStopSancp(pidsancp);
        }
    }

    public Snapshot obtainCurrentValues() {

        int pidsancp = -1;
        synchronized (pids) {
            while (pidsancp == -1) {
                pidsancp = startSancp();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
            }

            System.out.println("Corriendo SANCP con valor :" + pidsancp);
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }

        Snapshot snapshot = tryStopSancp(pidsancp);

        return snapshot;
    }
}
