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
package com.seagate.kinetic.example.heartbeat.rest;

import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * Start simple kinetic heart beat monitor HTTP service.
 * <p>
 * Applications may use a browser to see the available kinetic services. The default URL for the service is:
 * <p>
 * http://<hostName>:<port>
 * 
 * <p>
 * For example, if the service is running on the same machine, the default url would be as follows.
 * 
 * http://localhost:8080
 * 
 * 
 * @author chiaming
 *
 */
public class RestHeartbeatService {
    
    private final static Logger logger = Logger
            .getLogger(RestHeartbeatService.class.getName());

    public static void main(String[] args) throws Exception {
        
        int port = 8080;
        
        if (args.length >0) {
            port = Integer.parseInt(args[0]);
        }

        HeartbeatCollector hbc = null;

        if (args.length > 1) {
            hbc = new HeartbeatCollector(args[1]);
        } else {
            hbc = new HeartbeatCollector();
        }
        
        HeartbeatHandler handler = new HeartbeatHandler (hbc);
        
        Server server = new Server(port);
        
        ContextHandler hbcontext = new ContextHandler("/");
        hbcontext.setContextPath("/");
        hbcontext.setHandler(handler);
        
        server.setHandler(hbcontext);
        
        server.start();
        
        logger.info("heartbeat rest service is ready ...");
        
        server.join();
    }
}
