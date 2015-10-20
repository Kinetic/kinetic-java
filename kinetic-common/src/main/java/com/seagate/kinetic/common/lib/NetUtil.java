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
