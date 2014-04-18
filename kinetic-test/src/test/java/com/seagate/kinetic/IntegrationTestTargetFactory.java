package com.seagate.kinetic;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;

import kinetic.client.KineticException;
import kinetic.simulator.SimulatorConfiguration;

import com.jcraft.jsch.JSchException;

public class IntegrationTestTargetFactory {
    public static AbstractIntegrationTestTarget createTestTarget(
            boolean clearExistingDatabase) throws IOException,
            InterruptedException, KineticException, JSchException,
            ExecutionException {
        String kineticPath = System.getProperty("KINETIC_PATH");
        String kineticHost = System.getProperty("KINETIC_HOST");
        int requestedPort = Integer.parseInt(System.getProperty("KINETIC_PORT",
                "8123"));
        int requestedSslPort = Integer.parseInt(System.getProperty(
                "KINETIC_SSL_PORT", "8443"));

        AbstractIntegrationTestTarget testTarget;

        if (isRunningAgainstSimulator()) {
            testTarget = new SimulatorTestTarget(clearExistingDatabase,
                    requestedPort, requestedSslPort);
        } else if (kineticPath != null) {
            testTarget = new ForkedTestTarget(clearExistingDatabase,
                    kineticPath, requestedPort, requestedSslPort);
        } else {
            testTarget = new ExternalTestTarget(clearExistingDatabase,
                    kineticHost, requestedPort, requestedSslPort);
        }

        testTarget.waitForServerReady();

        return testTarget;
    }

    public static AbstractIntegrationTestTarget createAlternateTestTarget()
            throws IOException, JSchException, InterruptedException,
            ExecutionException, KineticException {
        String kineticPath = System.getProperty("KINETIC_PATH");

        AbstractIntegrationTestTarget testTarget;

        if (isRunningAgainstSimulator()) {
            SimulatorConfiguration secondaryServerConfig = new SimulatorConfiguration();
            secondaryServerConfig.setPort(findUnusedLocalPort());
            secondaryServerConfig.setSslPort(findUnusedLocalPort());
            secondaryServerConfig.put(SimulatorConfiguration.PERSIST_HOME,
                    "instance_" + secondaryServerConfig.getPort());

            testTarget = new SimulatorTestTarget(true, secondaryServerConfig);
        } else if (kineticPath != null) {
            throw new UnsupportedOperationException(
                    "No support for generating alternate test targets when running against a forked test target");
        } else {
            String alternateHost = System.getProperty("KINETIC_ALTERNATE_HOST");
            int alternatePort = Integer.parseInt(System
                    .getProperty("KINETIC_ALTERNATE_PORT"));
            int alternateTlsPort = Integer.parseInt(System
                    .getProperty("KINETIC_ALTERNATE_SSL_PORT"));
            testTarget = new ExternalTestTarget(true, alternateHost,
                    alternatePort, alternateTlsPort);
        }

        testTarget.waitForServerReady();

        return testTarget;
    }

    public static boolean isRunningAgainstSimulator() {
        return !Boolean
                .parseBoolean(System.getProperty("RUN_AGAINST_EXTERNAL"));
    }

    private static int findUnusedLocalPort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        try {
            return serverSocket.getLocalPort();
        } finally {
            serverSocket.close();
        }
    }
}
