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
import java.util.ArrayList;
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
    
    private static long SWEEP_TIME = 120000;
    
    private SortedMap <String, MessageContainer> drives = 
            Collections.synchronizedSortedMap(new TreeMap<String, MessageContainer>());
    
    private long lastSweepTime = System.currentTimeMillis();

    public HeartbeatCollector() throws IOException {
        super();
    }

    public HeartbeatCollector(String netInterfaceName) throws IOException {
        super(netInterfaceName);
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
            
            MessageContainer container = new MessageContainer (hbm, System.currentTimeMillis());
            
            
            this.drives.put(key, container);
            
            
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
    public SortedMap<String, MessageContainer> getHeartBeatMap() {

        // do a bit of clean up
        sweep();

        return new TreeMap<String, MessageContainer>(drives);
    }
    
    /**
     * clean up heartbeat table
     */
    private void sweep() {
        
        long now = System.currentTimeMillis();
        
        synchronized (this) {
            if ((now - this.lastSweepTime) >= SWEEP_TIME) {

                ArrayList<String> keys = new ArrayList<String>();

                // do sweep
                for (String key : drives.keySet()) {
                    if ((now - drives.get(key).getTimestamp()) >= SWEEP_TIME) {
                        keys.add(key);
                    }
                }

                for (String key : keys) {
                    drives.remove(key);
                }

                this.lastSweepTime = now;
            }
        }
        
    }

}
