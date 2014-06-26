/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.seagate.kinetic.monitor.internal.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.client.KineticException;

import com.seagate.kinetic.heartbeat.HeartbeatMessage;
import com.seagate.kinetic.monitor.HeartbeatListener;

public class KineticHeartbeatListenerService extends HeartbeatListener {
    private final static Logger logger = Logger
            .getLogger(KineticHeartbeatListenerService.class.getName());

    private Object lock = new Object();
    private Map<String, NodeInfo> nodesInfo = null;
    private long unavailableThreshold;

    public KineticHeartbeatListenerService(long unavailableThreshold)
            throws IOException {
        super();
        nodesInfo = new LinkedHashMap<String, NodeInfo>();
        this.unavailableThreshold = unavailableThreshold;

        new AvailabilityMonitor().start();
    }

    @Override
    public void onMessage(byte[] data) {
        System.out.println(new String(data).trim());
        HeartbeatMessage msg = HeartbeatMessage.fromJson(new String(data)
                .trim());
        String host = msg.getNetworkInterfaces().get(0).getIpV4Address();
        int port = msg.getTlsPort();
        String key = host + ":" + port;
        NodeInfo nodeInfo = null;
        if (!nodesInfo.containsKey(key)) {
            nodeInfo = new NodeInfo();
            nodeInfo.setHost(host);
            nodeInfo.setPort(msg.getPort());
            nodeInfo.setTlsPort(msg.getTlsPort());
            nodesInfo.put(key, nodeInfo);
        }

        synchronized (lock) {
            nodesInfo.get(key)
                    .setUnavailableTimeInSeconds(unavailableThreshold);
        }
    }

    public String getNodeDetailsAsJson(String key) {
        NodeInfo nodeInfo = nodesInfo.get(key);
        if (nodeInfo == null) {
            return "";
        }

        AdminClientConfiguration clientConfig = new AdminClientConfiguration();
        clientConfig.setHost(nodeInfo.getHost());
        clientConfig.setPort(nodeInfo.getTlsPort());

        // just update the log info when someone asks the detailed info
        KineticAdminClient adminClient = null;
        KineticLog kineticLog = null;
        try {
            adminClient = KineticAdminClientFactory
                    .createInstance(clientConfig);
            if (adminClient != null) {
                kineticLog = adminClient.getLog();
                nodeInfo.setCapacity(kineticLog.getCapacity());
                nodeInfo.setStatistics(kineticLog.getStatistics());
                nodeInfo.setTemperatures(kineticLog.getTemperature());
                nodeInfo.setUtilizations(kineticLog.getUtilization());
            } else {
                synchronized (lock) {
                    nodeInfo.setUnavailableTimeInSeconds(nodeInfo
                            .getUnavailableTimeInSeconds()
                            - unavailableThreshold);
                }
            }
        } catch (Exception e) {
            synchronized (lock) {
                nodeInfo.setUnavailableTimeInSeconds(nodeInfo
                        .getUnavailableTimeInSeconds() - unavailableThreshold);
            }
            e.printStackTrace();
        } finally {
            try {
                if (adminClient != null) {
                    adminClient.close();
                }
            } catch (KineticException e) {
                e.printStackTrace();
            }
        }

        return NodeInfo.toJson(nodeInfo);
    }

    public String listNodesDetailAsJson() {
        String res = "[";
        int i = 0;
        int length = nodesInfo.size();
        String nodeDetails = null;
        for (String key : nodesInfo.keySet()) {
            nodeDetails = getNodeDetailsAsJson(key);
            if (!nodeDetails.isEmpty()) {
                res += nodeDetails;
                if (++i < length) {
                    res += ",";
                }
            }
        }
        res += "]";

        return res;
    }

    public String listNodesAbstractAsJson() {
        String res = "[";
        int i = 0;
        int length = nodesInfo.size();
        NodeInfo nodeInfo = null;
        for (String key : nodesInfo.keySet()) {
            nodeInfo = nodesInfo.get(key);
            res += "{";
            res += "\"host\":" + "\"" + nodeInfo.getHost() + "\",";
            res += "\"port\":" + nodeInfo.getTlsPort() + ",";
            res += "\"status\":" + nodeInfo.getStatus();
            res += "}";
            if (++i < length) {
                res += ",";
            }
        }
        res += "]";

        return res;
    }

    class AvailabilityMonitor extends Thread {
        private static final int INTERVAL = 5;

        @Override
        public void run() {
            while (true) {

                long value = -1;
                for (String key : nodesInfo.keySet()) {
                    synchronized (lock) {
                        value = nodesInfo.get(key)
                                .getUnavailableTimeInSeconds();
                        value -= INTERVAL;
                        if (0 > value) {
                            System.out.println(key + " no heartbeat more than "
                                    + unavailableThreshold + " seconds");
                        }
                        nodesInfo.get(key).setUnavailableTimeInSeconds(value);
                    }
                }

                try {
                    TimeUnit.SECONDS.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }
            }
        }
    }
}
