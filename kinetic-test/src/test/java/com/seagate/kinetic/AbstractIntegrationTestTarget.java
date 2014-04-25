package com.seagate.kinetic;

import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2PClientFactory;
import kinetic.client.p2p.KineticP2pClient;
import kinetic.client.p2p.Peer;

public abstract class AbstractIntegrationTestTarget {
    protected final String host;
    protected final int port;
    protected final int tlsPort;

    protected AbstractIntegrationTestTarget(String host, int port, int tlsPort) {
        this.host = host;
        this.port = port;
        this.tlsPort = tlsPort;
    }

    public abstract void shutdown() throws Exception;

    public KineticP2pClient createKineticClient() throws KineticException {
        return KineticP2PClientFactory.createP2pClient(getAdminClientConfig());
    }

    protected void waitForServerReady() throws InterruptedException {
        final int pollingIntervalMS = 50;
        final int maxWaitTimeSec = 10;

        long waitStartTime = System.currentTimeMillis();
        float elapsedTimeMS;
        while (true) {
            Thread.sleep(pollingIntervalMS);

            elapsedTimeMS = System.currentTimeMillis() - waitStartTime;
            if (elapsedTimeMS > maxWaitTimeSec * 1000) {
                throw new RuntimeException("Server never became available");
            }
            try {
                KineticClient kineticClient = createKineticClient();
                kineticClient.noop();
                kineticClient.close();
                break;
            } catch (KineticException e) {
                // Since all exceptions get turned into KineticException we have
                // to manually check the message
                if (e.getMessage().contains("Kinetic Command Exception: ")) {
                    break;
                }
                // Ignore this exception because it probably means that the
                // server isn't ready yet so
                // we'll just take a brief nap and try again in a bit
            }
        }
    }

    public AdminClientConfiguration getAdminClientConfig() {
        AdminClientConfiguration clientConfiguration = new AdminClientConfiguration();
        clientConfiguration.setHost(host);
        clientConfiguration.setPort(tlsPort);
        clientConfiguration.setNioServiceThreads(1);
        return clientConfiguration;
    }

    public ClientConfiguration getClientConfig() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setHost(host);
        clientConfiguration.setPort(port);
        clientConfiguration.setNioServiceThreads(1);
        return clientConfiguration;
    }

    public Peer getPeer() {
        Peer peer = new Peer();
        peer.setHost(host);
        peer.setPort(port);
        return peer;
    }

    public Peer getTlsPeer() {
        Peer peer = new Peer();
        peer.setHost(host);
        peer.setPort(tlsPort);
        peer.setUseTls(true);
        return peer;
    }

    protected void performISE() throws KineticException {
        KineticAdminClient kineticAdminClient = KineticAdminClientFactory
                .createInstance(getAdminClientConfig());
        kineticAdminClient.setup(null, null, 0, true);
        kineticAdminClient.close();
    }
}
