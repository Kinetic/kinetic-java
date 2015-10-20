/**
 * Copyright 2013-2015 Seagate Technology LLC.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at
 * https://mozilla.org/MP:/2.0/.
 * 
 * This program is distributed in the hope that it will be useful,
 * but is provided AS-IS, WITHOUT ANY WARRANTY; including without 
 * the implied warranty of MERCHANTABILITY, NON-INFRINGEMENT or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the Mozilla Public 
 * License for more details.
 *
 * See www.openkinetic.org for more project information
 */
package com.seagate.kinetic.example.simulator;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Start a simulator instance example.
 * <p>
 * This example starts one instance of Kinetic Simulator with default
 * configurations.
 * <p>
 * The started Simulator service listens on port 8123 for (TCP) and 8443 for
 * (SSL/TLS).
 * 
 */
public class SingleKineticSimulator {

    public static void main(String[] args) {

        SimulatorConfiguration serverConfig = new SimulatorConfiguration();

        int port = 8123;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        serverConfig.setPort(port);

        if (args.length > 1) {
            serverConfig.setProperty(SimulatorConfiguration.KINETIC_HOME, args[1]);
        }

        if (args.length > 2) {
            serverConfig.setSslPort(Integer.parseInt(args[2]));
        }

        KineticSimulator simulator = new KineticSimulator(serverConfig);

        System.out.println("Example simulator started on port=" + simulator.getServerConfiguration().getPort()
                + ", SSL/TLS port=" + simulator.getServerConfiguration().getSslPort());
    }

}
