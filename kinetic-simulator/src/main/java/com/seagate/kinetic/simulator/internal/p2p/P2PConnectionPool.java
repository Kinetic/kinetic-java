/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.internal.p2p;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.P2POperation.Peer;

/**
 *
 * Simple p2p connection pool. No smart reduce connections yet.
 *
 * @author chiaming
 *
 */
public class P2PConnectionPool {

    private final static Logger logger = Logger
            .getLogger(P2PConnectionPool.class.getName());

    // client map
    private final HashMap<String, KineticClient> clientMap = new HashMap<String, KineticClient>();

    public P2PConnectionPool() {
        ;
    }

    /**
     * Get a kinetic client instance from cache or create a new one if none is
     * in cache.
     *
     * @param request
     *            request message from application.
     *
     * @return client instance either from cache map or a new instance if not in
     *         cache.
     *
     * @throws KineticException
     *             if unable to connect to peer.
     */
    public KineticClient getKineticClient(Message request)
            throws KineticException {

        // get client instance
        KineticClient client = this.getFromCacheOrCreate(request);

        return client;
    }

    private synchronized KineticClient getFromCacheOrCreate(Message request)
            throws KineticException {

        // peer info
        Peer peer = request.getCommand().getBody().getP2POperation().getPeer();

        // user id
        long uid = request.getCommand().getHeader().getIdentity();

        // map key
        String key = uid + ":" + peer.getHostname() + ":" + peer.getPort()
                + ":" + peer.getTls();

        // get from pool
        KineticClient client = this.clientMap.get(key);

        if (client == null) {
            // create client instance
            client = this.createClient(request);

            // add to pool
            this.clientMap.put(key, client);

            logger.info("created and put client instance to pool, key=" + key);
        } else {
            logger.info("got client instance from pool, key=" + key);
        }

        return client;
    }

    /**
     *
     * @param request
     *            request message from application.
     * @return a new instance of client on behalf of application.
     *
     * @throws KineticException
     *             if any internal error occured.
     */
    private KineticClient createClient(Message request) throws KineticException {

        // get peer info
        Peer peer = request.getCommand().getBody().getP2POperation().getPeer();

        // client config
        ClientConfiguration config = new ClientConfiguration();

        // set original client user id
        config.setUserId(request.getCommand().getHeader().getIdentity());

        // set peer info
        config.setHost(peer.getHostname());
        config.setPort(peer.getPort());
        config.setUseSsl(peer.getTls());

        // create a new instance
        KineticClient client = KineticClientFactory.createInstance(config);

        return client;
    }

    /**
     * close the pool.
     */
    public void close() {

        for (KineticClient client : clientMap.values()) {
            try {
                client.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

        this.clientMap.clear();
    }
}
