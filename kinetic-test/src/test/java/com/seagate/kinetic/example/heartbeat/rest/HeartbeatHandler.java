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

import java.io.IOException;
import java.util.Date;

import java.util.SortedMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Rest Heartbeat handler handles HTTP request.
 * <p>
 * The response is a HTML table that contains all the collected kinetic heartbeats.
 * 
 * @see RestHeartbeatService
 * 
 * @author chiaming
 *
 */
public class HeartbeatHandler extends AbstractHandler {
    
    private final static Logger logger = Logger
            .getLogger(HeartbeatHandler.class.getName());
    
    private HeartbeatCollector hbc = null;
    
    public HeartbeatHandler(HeartbeatCollector hbc) { 
        this.hbc = hbc;
    }
    
    @Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Hearbeat Table</h1>");
        
        response.getWriter().println("<br>update time: " + new Date() + "</br>");
        
        SortedMap <String, MessageContainer> map = hbc.getHeartBeatMap();
        
       int index = 0;
       
       String table = "<table border=\"1\" style=\"width:300px\">";
       response.getWriter().println(table);
       
       response.getWriter().println("<tr>");
       response.getWriter().println("<td>Num</td>");
       response.getWriter().println("<td>IP Address</td>");
       response.getWriter().println("<td>TCP Port</td>");
       response.getWriter().println("<td>TLS Port</td>");
       response.getWriter().println("<td>Timestamp</td>");
       response.getWriter().println("</tr>");
       
        synchronized (this) {
            
            for (String key: map.keySet()) {
                //response.getWriter().println("<br>" + index + ":    " + key);
                response.getWriter().println("<tr>");
                response.getWriter().println("<td>" + index +"</td>");
                response.getWriter().println("<td>" + map.get(key).getHeartbeatMessage().getNetworkInterfaces().get(0).getIpV4Address() +"</td>");
                response.getWriter().println("<td>" + map.get(key).getHeartbeatMessage().getPort() +"</td>");
                response.getWriter().println("<td>" + map.get(key).getHeartbeatMessage().getTlsPort() +"</td>");
                response.getWriter().println("<td>" + new Date (map.get(key).getTimestamp()) +"</td>");
                response.getWriter().println("</tr>");
                index ++;
            }
        }
        
        response.getWriter().println("</table>");
        
        logger.info("total kinetic services collected: " + map.size());
    }

}
