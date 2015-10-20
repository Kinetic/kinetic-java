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
package com.seagate.kinetic.simulator.internal.p2p;

import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.P2POperation.Peer;

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
    public KineticClient getKineticClient(KineticMessage request)
            throws KineticException {

        // get client instance
        KineticClient client = this.getFromCacheOrCreate(request);

        return client;
    }

    private synchronized KineticClient getFromCacheOrCreate(KineticMessage request)
            throws KineticException {

        // create client
        return this.createClient(request);
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
    private KineticClient createClient(KineticMessage request) throws KineticException {

        // get peer info
        Peer peer = request.getCommand().getBody().getP2POperation().getPeer();

        // client config
        ClientConfiguration config = new ClientConfiguration();

        // set original client user id
        config.setUserId(request.getMessage().getHmacAuth().getIdentity());

        // set peer info
        config.setHost(peer.getHostname());
        config.setPort(peer.getPort());
        config.setUseSsl(peer.getTls());

        logger.info("creating p2p client: " + peer.getHostname() + ":"
                + peer.getPort());

        // create a new instance
        KineticClient client = KineticClientFactory.createInstance(config);

        logger.info("created p2p client: " + peer.getHostname() + ":"
                + peer.getPort());

        return client;
    }

    /**
     * close the pool.
     */
    public void close() {
        ;
    }
}
