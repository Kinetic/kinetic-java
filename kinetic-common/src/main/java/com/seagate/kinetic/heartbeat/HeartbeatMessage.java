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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/**
 * 
 * Heart beat prototype message.
 * 
 */
public class HeartbeatMessage {

    // firmware version
    private String firmware_version = "0.8.0.2";

    // manufacturer
    private String manufacturer = "Seagate";

    // model
    private String model = "Simulator";

    private final List<KineticNetworkInterface> network_interfaces = new ArrayList<KineticNetworkInterface>();

    // message source port
    private int port = 8123;

    private String protocol_version = "3.0.0";

    private String serial_number = "Z3008QP0";

    // message source tls port
    private int tlsPort = 8443;

    private String world_wide_name = "7 000c50 05008d357";

    // gson to
    private static Gson gson = new Gson();

    public HeartbeatMessage() {
        ;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public void setTlsPort(int port) {
        this.tlsPort = port;
    }

    public int getTlsPort() {
        return this.tlsPort;
    }

    public void addNetworkInterface(KineticNetworkInterface networkInterface) {
        this.network_interfaces.add(networkInterface);
    }

    public List<KineticNetworkInterface> getNetworkInterfaces() {
        return this.network_interfaces;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmware_version = firmwareVersion;
    }

    public String getFirmwareVersion() {
        return this.firmware_version;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return this.model;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocol_version = protocolVersion;
    }

    public String getProtocolVersion() {
        return this.protocol_version;
    }

    public void setSerialNumber(String sn) {
        this.serial_number = sn;
    }

    public String getSerialNumber() {
        return this.serial_number;
    }

    public void setWorldWideName(String wwn) {
        this.world_wide_name = wwn;
    }

    public String getWorldWideName() {
        return this.world_wide_name;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj, HeartbeatMessage.class);
    }

    public static HeartbeatMessage fromJson(String str) {
        return gson.fromJson(str, HeartbeatMessage.class);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        HeartbeatMessage hbm = new HeartbeatMessage();

        String msg = HeartbeatMessage.toJson(hbm);

        System.out.println("msg=" + msg + ", size="
                + msg.getBytes("utf8").length);

        byte[] data = msg.getBytes("UTF8");

        String msgX = new String(data, "UTF8");

        HeartbeatMessage newMsg = HeartbeatMessage.fromJson(msgX);

        String msg2 = HeartbeatMessage.toJson(newMsg);

        System.out.println("msg=" + msg2 + ", size="
                + msg2.getBytes("utf8").length);
    }

}
