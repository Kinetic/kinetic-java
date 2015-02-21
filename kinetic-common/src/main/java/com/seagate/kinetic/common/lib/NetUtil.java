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
package com.seagate.kinetic.common.lib;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetUtil {

    private final static Logger logger = Logger.getLogger(NetUtil.class
            .getName());

    /**
     * set interface name to use for multicast heartbeat.
     */
    public final static String NET_INTERFACE_PROP_NAME = "kinetic.net.interface";

    /**
     * Find a valid network interface that supports multicast.
     * 
     * @return a valid network interface that supports multicast. Or null if no
     *         network interface is found or no permission to do the search.
     */
    public static NetworkInterface findMulticastNetworkInterface() {

        NetworkInterface ni = null;

        try {

            String niName = System.getProperty(NET_INTERFACE_PROP_NAME);

            if (niName != null) {
                ni = NetworkInterface.getByName(niName);

                logger.info("user defined multicast interface is used., name="
                        + ni.getDisplayName());
            } else {

                Enumeration<NetworkInterface> nis = NetworkInterface
                        .getNetworkInterfaces();

                while (nis.hasMoreElements()) {
                    ni = nis.nextElement();
                    if (ni.supportsMulticast() && ni.isUp()
                            && ni.isVirtual() == false) {
                        logger.info("found interface that supports multicast: "
                                + ni.getDisplayName());
                        break;
                    }
                }
            }

        } catch (SocketException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        return ni;
    }

}
