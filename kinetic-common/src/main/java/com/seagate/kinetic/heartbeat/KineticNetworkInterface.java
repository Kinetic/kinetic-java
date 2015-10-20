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
package com.seagate.kinetic.heartbeat;

public class KineticNetworkInterface {

    private String ipv4_addr = "127.0.0.1";

    private String ipv6_addr = null;

    private String mac_addr = null;

    private String name = null;

    public KineticNetworkInterface() {
        ;
    }

    public void setIpV4Address(String ipv4Address) {
        this.ipv4_addr = ipv4Address;
    }

    public String getIpV4Address() {
        return this.ipv4_addr;
    }

    public void setIpV6Address(String ipv6Address) {
        this.ipv6_addr = ipv6Address;
    }

    public String getIpV6Address() {
        return this.ipv6_addr;
    }

    public void setMacAddress(String macAddress) {
        this.mac_addr = macAddress;
    }

    public String getMacAddress() {
        return this.mac_addr;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
