/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic;

import java.io.File;

import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

import org.apache.commons.io.FileUtils;

public class SimulatorTestTarget extends AbstractIntegrationTestTarget {
    private final KineticSimulator kineticSimulator;

    public SimulatorTestTarget(boolean clearExistingDatabase,
            SimulatorConfiguration simulatorConfiguration) {
        super("localhost", simulatorConfiguration.getPort(),
                simulatorConfiguration.getSslPort());

        if (clearExistingDatabase) {
            deleteJavaServerAuxilaryData(simulatorConfiguration);
        }

        kineticSimulator = new KineticSimulator(simulatorConfiguration);
    }

    public SimulatorTestTarget(boolean clearExistingDatabase, int port,
            int sslPort) throws KineticException {
        super("localhost", port, sslPort);

        SimulatorConfiguration simulatorConfiguration = new SimulatorConfiguration();
        simulatorConfiguration.setPort(port);
        simulatorConfiguration.setSslPort(tlsPort);
        //simulatorConfiguration.setUseV2Protocol(true);
        simulatorConfiguration.setNioServiceBossThreads(1);
        simulatorConfiguration.setNioServiceWorkerThreads(1);

        if (clearExistingDatabase) {
            deleteJavaServerAuxilaryData(simulatorConfiguration);
        }

        kineticSimulator = new KineticSimulator(simulatorConfiguration);

        if (clearExistingDatabase) {
            // This makes sure the target has been fully reset. If a developer
            // is running kineticd locally
            // it also has the nice side effect of clearing kineticd since just
            // the "rm -rf" approach taken by
            // deleteJavaServerAuxiliaryData() above wouldn't affect kineticd.
            performISE();
        }
    }

    @Override
    public void shutdown() throws Exception {
        kineticSimulator.close();
    }

    private void deleteJavaServerAuxilaryData(
            SimulatorConfiguration simulatorConfiguration) {
        String kineticHome = System.getProperty("user.home") + File.separator
                + "kinetic";
        if (simulatorConfiguration
                .getProperty(SimulatorConfiguration.KINETIC_HOME) != null) {
            kineticHome = simulatorConfiguration
                    .getProperty(SimulatorConfiguration.KINETIC_HOME);
        }

        FileUtils.deleteQuietly(new File(kineticHome));

        String defaultHome = System.getProperty("user.home") + File.separator
                + "leafcutter";

        String leafcutterHome = simulatorConfiguration.getProperty(
                SimulatorConfiguration.KINETIC_HOME, defaultHome);

        FileUtils.deleteQuietly(new File(leafcutterHome));
    }
}
