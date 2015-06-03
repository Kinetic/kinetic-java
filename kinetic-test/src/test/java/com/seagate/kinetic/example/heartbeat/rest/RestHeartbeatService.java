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
