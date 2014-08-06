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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import com.google.gson.stream.JsonReader;
import com.seagate.kinetic.heartbeat.HeartbeatMessage;
import com.seagate.kinetic.monitor.HeartbeatListener;

/**
 * 
 * Collect heart beat information into a sorted map.
 * <p>
 * This is used by the HTTP service to get the drives IP and service ports.
 * 
 * @see RestHeartbeatService
 * @see HeartbeatHandler
 * 
 * @author chiaming
 *
 */
public class HeartbeatCollector extends HeartbeatListener {
    
    private final static Logger logger = Logger
            .getLogger(HeartbeatCollector.class.getName());
    
    private SortedMap <String, HeartbeatMessage> drives = 
            Collections.synchronizedSortedMap(new TreeMap<String, HeartbeatMessage>());

    public HeartbeatCollector() throws IOException {
        super();
    }
    
    @Override
    public void onMessage(byte[] data) {

        try {

            String message = new String(data, "UTF8");

            JsonReader reader = new JsonReader(new StringReader(message));
            reader.setLenient(true);
            
            Gson gson = new Gson();
            
            HeartbeatMessage hbm = gson
                    .fromJson(reader, HeartbeatMessage.class);
            
            String key = hbm.getNetworkInterfaces().get(0).getIpV4Address() + ":" + hbm.getPort();
            
            this.drives.put(key, hbm);

            logger.fine ("received heart beat: " + key);
            

        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

    }
    
    /**
     * Get the heart beat map used by this collector.
     * 
     * @return the heart beat map used by this collector
     */
    public SortedMap<String, HeartbeatMessage> getHeartBeatMap() {
        return new TreeMap<String, HeartbeatMessage> (drives);
    }

}
