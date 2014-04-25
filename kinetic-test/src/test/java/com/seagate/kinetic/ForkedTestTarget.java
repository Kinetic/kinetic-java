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
