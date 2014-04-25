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
