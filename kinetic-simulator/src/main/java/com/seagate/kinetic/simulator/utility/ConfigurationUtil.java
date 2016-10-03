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
package com.seagate.kinetic.simulator.utility;

import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Configuration;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Configuration.Interface;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

public abstract class ConfigurationUtil {

    private final static Logger logger = Logger
            .getLogger(ConfigurationUtil.class.getName());
    public final static String VENDER = SimulatorConfiguration.VENDER;
    public final static String MODEL = SimulatorConfiguration.MODEL;

    // public static final String SERIAL_PREFIX = "SIMULATOR-SN-";

    public final static String COMPILATION_DATE = new Date().toString();
    public final static String PROTOCOL_COMPILATION_DATE = new Date()
            .toString();

    @SuppressWarnings("static-access")
    public static Configuration getConfiguration(SimulatorEngine engine)
            throws UnknownHostException, UnsupportedEncodingException {

        SimulatorConfiguration config = engine.getServiceConfiguration();

        Configuration.Builder configuration = Configuration.newBuilder();
        configuration.setVendor(VENDER);
        configuration.setModel(MODEL);

        configuration.setSerialNumber(ByteString.copyFrom(
                config.getSerialNumber(), "UTF8"));

        configuration.setWorldWideName(ByteString.copyFrom(
                config.getWorldWideName(), "UTF8"));

        configuration.setCompilationDate(COMPILATION_DATE);
        configuration.setProtocolCompilationDate(PROTOCOL_COMPILATION_DATE);

        configuration.setVersion(SimulatorConfiguration.getSimulatorVersion());

        List<Interface> interfaces = new ArrayList<Interface>();
        Interface.Builder itf1 = null;

        try {

            Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                    .getNetworkInterfaces();

            while (netInterfaces.hasMoreElements()) {

                // get next interface
                NetworkInterface ni = netInterfaces.nextElement();

                itf1 = Interface.newBuilder();
                
                // display name could be null returned from NetworkInterface API
                if (ni.getDisplayName() != null) {
                    itf1.setName(ni.getDisplayName());
                }

                // set mac addr
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    String macS = bytesToStringMac(mac);
                    itf1.setMAC(ByteString.copyFromUtf8(macS));
                }

                // get inet addresses on this interface
                Enumeration<InetAddress> addresses = ni.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    // get next inet addr
                    InetAddress addr2 = addresses.nextElement();
                    // get string address
                    String addrString = addr2.getHostAddress();

                    if (addr2 instanceof Inet6Address) {
                        itf1.setIpv6Address(ByteString.copyFromUtf8(addrString));
                    } else {
                        itf1.setIpv4Address(ByteString.copyFromUtf8(addrString));
                    }
                }

                interfaces.add(itf1.build());
            }

        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Can not get the network Interface");
        }

        for (Interface tempItf : interfaces) {
            configuration.addInterface(tempItf);
        }

        configuration.setPort(config.getPort());
        configuration.setTlsPort(config.getSslPort());

        if (null != config.getSimulatorVersion()) {
            configuration.setVersion(config.getSimulatorVersion());
        }

        if (null != config.getSimulatorSourceHash()) {
            configuration.setSourceHash(config.getSimulatorSourceHash());
        }

        if (null != config.getProtocolVersion()) {
            configuration.setProtocolVersion(config.getProtocolVersion());
        }

        if (null != config.getProtocolSourceHash()) {
            configuration.setProtocolSourceHash(config.getProtocolSourceHash());
        }

        return configuration.build();
    }

    private static String bytesToStringMac(byte[] mac) {
        StringBuilder sb = new StringBuilder(18);
        for (byte b : mac) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
