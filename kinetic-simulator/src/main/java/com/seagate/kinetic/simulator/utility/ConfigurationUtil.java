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
    public final static String VENDER = "Seagate";
    public final static String MODEL = "Simulator";
    
    public static final String SERIAL_PREFIX = "SIMULATOR-SN-";
     
    //public final static byte[] SERIAL_NUMBER = "93C3DAFD-C894-3C88-A4B0-632A90D2A04B"
    //        .getBytes(Charset.forName("UTF-8"));
    
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
        
        // get serial no for this instance
        String sn = getSerialNumber(engine);
        
        configuration.setSerialNumber(ByteString.copyFrom(sn, "UTF8"));
        
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
                itf1.setName(ni.getDisplayName());

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
    
    /**
     * calculate serial no.
     * <p>
     * A simulator instance serial number is calculated as follows.
     * <p> 
     * SN = SERIAL_PREFIX + ip + "-" + khomeHash + "-" + persistHome;
     * 
     * @param engine simulator engine
     * 
     * @return serial number for the specified instance of simulator
     */
    private static String getSerialNumber (SimulatorEngine engine) {
        
        SimulatorConfiguration config = engine.getServiceConfiguration();
        
        int khomeHash = Math.abs(engine.getKineticHome().hashCode());
        
        // get persist home name, use port# if not set
        String persistHome = config.getProperty(SimulatorConfiguration.PERSIST_HOME, String.valueOf(config.getPort()));
        
        //int phomeHash = Math.abs(persistHome.hashCode());
        
        // default ip of this instance
        String ip = "127.0.0.1";
        
        try {
            // get from Java API
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            ;
        }
        
        // construct sn
        String sn = SERIAL_PREFIX + ip + "-" + khomeHash + "-" + persistHome;
        
        // replace '_' with '-'
        sn = sn.replace('_', '-');
        
        // replace '.' with '-'
        sn = sn.replace('.', '-');
        
        // convert to upper case
        sn = sn.toUpperCase();
        
        // return sn for this instance
        return sn;
    }
}
