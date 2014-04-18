/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
