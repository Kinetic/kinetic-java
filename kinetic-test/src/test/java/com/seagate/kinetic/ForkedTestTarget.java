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
package com.seagate.kinetic;

import java.io.File;
import java.io.IOException;

import kinetic.client.KineticException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.leacox.process.FinalizedProcess;
import com.leacox.process.FinalizedProcessBuilder;

public class ForkedTestTarget extends AbstractIntegrationTestTarget {
    private final FinalizedProcess externalKineticServer;

    public ForkedTestTarget(boolean clearExistingDatabase, String serverRunnerPath, int port, int tlsPort) throws IOException, InterruptedException, KineticException {
        super("localhost", port, tlsPort);

        FinalizedProcessBuilder finalizedProcessBuilder = new FinalizedProcessBuilder("killall", "-9", "kineticd");
        finalizedProcessBuilder.start().waitFor(10 * 1000);
        Thread.sleep(500);

        // Since the cluster version is checked before performing an ISE we need to manually remove
        // the file used to store the cluster version
        if (clearExistingDatabase) {
            final String workingDirectory = FilenameUtils.getFullPath(serverRunnerPath);
            final String clusterStorePath = FilenameUtils.concat(workingDirectory, "cluster_version");
            FileUtils.deleteQuietly(new File(clusterStorePath));
        }

        finalizedProcessBuilder = new FinalizedProcessBuilder(serverRunnerPath);
        finalizedProcessBuilder.directory(new File("."));
        finalizedProcessBuilder.gobbleStreamsWithLogging(true);

        externalKineticServer = finalizedProcessBuilder.start();
        waitForServerReady();

        // The ForkedTestTarget only runs on x86. The x86 implementations' ISE is almost instant with small DBs so
        // it's OK to issue a kinetic ISE instead of using SSH
        if (clearExistingDatabase) {
            performISE();
        }
    }

    public void shutdown() throws Exception {
        externalKineticServer.close();
    }


}
