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
package com.seagate.kinetic.admin.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kinetic.admin.AdminClientConfiguration;
import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.admin.impl.JsonUtil;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Message.Header;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Security;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;

/**
 *
 * KineticClient command line tool, support setup security getlog and help
 * <p>
 *
 *
 */
public class KineticAdminCLI {
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String ALL = "all";
    private static final String TEMPERATURE = "temperature";
    private static final String CAPACITY = "capacity";
    private static final String UTILIZATION = "utilization";
    private static final String CONFIGURATION = "configuration";
    private static final String MESSAGES = "message";
    private static final String STATISTICS = "statistic";
    private static final int DEFAULT_SSL_PORT = 8443;
    private static final String DEFAULT_HOST = "localhost";
    private static final long CLUSTERVERSION = 0;
    private static final int OK = 0;
    private static final int ERROR = 1;
    private static KineticClient kineticClient = null;
    private final Map<String, List<String>> legalArguments = new HashMap<String, List<String>>();

    public KineticAdminCLI() throws KineticException {
        String rootArg = "-help";
        List<String> subArgs = new ArrayList<String>();
        legalArguments.put(rootArg, subArgs);

        rootArg = "-h";
        subArgs = new ArrayList<String>();
        legalArguments.put(rootArg, subArgs);

        rootArg = "-setup";
        subArgs = initSubArgs();

        subArgs.add("-pin");
        subArgs.add("-setPin");
        subArgs.add("-newclversion");
        subArgs.add("-erase");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-security";
        subArgs = initSubArgs();

        legalArguments.put(rootArg, subArgs);

        rootArg = "-getlog";
        subArgs = initSubArgs();

        subArgs.add("-type");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-firmware";
        subArgs = initSubArgs();

        subArgs.add("-pin");
        legalArguments.put(rootArg, subArgs);
    }

    /*
     * init Kinetic client
     */
    public void init(String host, String tlsPort, String clusterVersion)
            throws KineticException {
        ClientConfiguration clientConfig = new AdminClientConfiguration();
        if (host != null && !host.isEmpty()) {
            validateHost(host);
            clientConfig.setHost(host);
        } else {
            clientConfig.setHost(DEFAULT_HOST);
        }

        if (tlsPort != null && !tlsPort.isEmpty()) {
            validatePort(tlsPort);
            clientConfig.setPort(Integer.parseInt(tlsPort));
        } else {
            clientConfig.setPort(DEFAULT_SSL_PORT);
        }

        if (clusterVersion != null && !clusterVersion.isEmpty()) {
            clientConfig.setClusterVersion(Integer.parseInt(clusterVersion));
        } else {
            clientConfig.setClusterVersion(CLUSTERVERSION);
        }

        kineticClient = KineticClientFactory.createInstance(clientConfig);
    }

    public void close() throws KineticException {
        if (kineticClient != null) {
            kineticClient.close();
        }
    }

    public KineticMessage security(String securityFile) throws IOException,
            KineticException {
        byte[] content = readFile(securityFile);

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder request = (Builder) km.getMessage();

        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
        header.setMessageType(MessageType.SECURITY);
        Security security = JsonUtil.parseSecurity(ByteString.copyFrom(content)
                .toStringUtf8());
        request.getCommandBuilder().getBodyBuilder().setSecurity(security);

        return kineticClient.request(km);
    }

    private byte[] readFile(String securityFile) throws FileNotFoundException,
            IOException {
        File file = new File(securityFile);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        byte[] content = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);
        in.read(content);
        in.close();
        return content;
    }

    /*
     * Parse the getlog type
     */
    public KineticMessage getLog(String type) throws KineticException {
        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        Message.Builder request = (Builder) km.getMessage();
        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
        header.setMessageType(MessageType.GETLOG);
        GetLog.Builder getLog = request.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();

        validateLogType(type);
        if (type.equalsIgnoreCase(ALL)) {
            getLog.addType(Type.UTILIZATIONS);
            getLog.addType(Type.CAPACITIES);
            getLog.addType(Type.TEMPERATURES);
            getLog.addType(Type.CONFIGURATION);
            getLog.addType(Type.MESSAGES);
            getLog.addType(Type.STATISTICS);

        } else if (type.equalsIgnoreCase(UTILIZATION)) {
            getLog.addType(Type.UTILIZATIONS);
        } else if (type.equalsIgnoreCase(CAPACITY)) {
            getLog.addType(Type.CAPACITIES);
        } else if (type.equalsIgnoreCase(TEMPERATURE)) {
            getLog.addType(Type.TEMPERATURES);
        } else if (type.equalsIgnoreCase(CONFIGURATION)) {
            getLog.addType(Type.CONFIGURATION);
        } else if (type.equalsIgnoreCase(MESSAGES)) {
            getLog.addType(Type.MESSAGES);
        } else if (type.equalsIgnoreCase(STATISTICS)) {
            getLog.addType(Type.STATISTICS);
        } else {
            throw new IllegalArgumentException(
                    "Type should be utilization, capacity, temperature, configuration, message, statistic or all");
        }
        return kineticClient.request(km);
    }

    /*
     * Parse the setup argument
     */
    public KineticMessage setup(String pin, String setPin,
            String newClusterVersion, String erase) throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        Message.Builder request = (Builder) km.getMessage();

        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
        header.setMessageType(MessageType.SETUP);
        com.seagate.kinetic.proto.Kinetic.Message.Setup.Builder setup = com.seagate.kinetic.proto.Kinetic.Message.Setup
                .newBuilder();
        if (pin != null) {
            setup.setPin(ByteString.copyFromUtf8(pin));
        }

        if (setPin != null) {
            setup.setSetPin(ByteString.copyFromUtf8(setPin));
        }

        if (newClusterVersion != null) {
            validateClusterVersion(newClusterVersion);
            setup.setNewClusterVersion(Long.parseLong(newClusterVersion));
        }

        if (erase != null) {
            validateErase(erase);
            setup.setInstantSecureErase(Boolean.parseBoolean(erase));
        }

        request.getCommandBuilder().getBodyBuilder().setSetup(setup);

        return kineticClient.request(km);
    }

    public KineticMessage firmwareDownload(String pin, String firmwareFile)
            throws IOException, KineticException {
        byte[] content = readFile(firmwareFile);

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder request = (Builder) km.getMessage();

        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
        header.setMessageType(MessageType.SETUP);
        com.seagate.kinetic.proto.Kinetic.Message.Setup.Builder setup = com.seagate.kinetic.proto.Kinetic.Message.Setup
                .newBuilder();
        if (pin != null) {
            setup.setPin(ByteString.copyFromUtf8(pin));
        }

        setup.setFirmwareDownload(true);

        request.getCommandBuilder().getBodyBuilder().setSetup(setup);

        if (null != content && content.length > 0) {
            km.setValue(content);
        }

        return kineticClient.request(km);
    }

    public static void printHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("Usage: kineticAdmin <-setup|-security|-getlog|-firmware>\n");
        sb.append("kineticAdmin -h|-help\n");
        sb.append("kineticAdmin -setup [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>] [-newclversion <newclusterversion>] [-setpin <setpin>] [-erase <true|false>]\n");
        sb.append("kineticAdmin -security <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -getlog [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-type <utilization|temperature|capacity|configuration|message|statistic|all>]\n");
        sb.append("kineticAdmin -firmware <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>]");
        System.out.println(sb.toString());
    }

    public String getArgValue(String argName, String args[]) {
        if (null == argName || argName.isEmpty() || args.length <= 1) {
            return null;
        }

        int index = -1;
        for (int i = 0; i < args.length; i++) {
            if (argName.equalsIgnoreCase(args[i])) {
                index = i;
                break;
            }
        }

        if (index != -1 && args.length > (index + 1)
                && !args[index + 1].isEmpty()) {
            if (args[index + 1].startsWith("-")) {
                throw new IllegalArgumentException("value can't start with -");
            }
            if (null == args[index + 1]) {
                throw new IllegalArgumentException("value can't be null");
            }
            return args[index + 1].trim();
        }

        return null;
    }

    /*
     * handle the response to print
     */
    public void printResponse(Message response) {
        if (null == response) {
            return;
        }

        MessageType messageType = response.getCommand().getHeader()
                .getMessageType();
        StatusCode statusCode = response.getCommand().getStatus().getCode();
        if (messageType.equals(MessageType.SETUP_RESPONSE)
                || messageType.equals(MessageType.SECURITY_RESPONSE)) {
            if (statusCode.equals(StatusCode.SUCCESS)) {
                System.out.println(StatusCode.SUCCESS);
            } else {
                System.err.println(response.getCommand().getStatus().getCode());
                System.err.println(response.getCommand().getStatus()
                        .getStatusMessage());
            }

        } else if (messageType.equals(MessageType.GETLOG_RESPONSE)) {
            if (statusCode.equals(StatusCode.SUCCESS)) {
                System.out.println(StatusCode.SUCCESS);
                System.out.print(response.getCommand().getBody().getGetLog()
                        .toString());
            } else {
                System.err.println(response.getCommand().getStatus().getCode());
                System.err.println(response.getCommand().getStatus()
                        .getDetailedMessage().toStringUtf8());
            }

        }

    }

    public void validateArgNames(String args[]) throws KineticException {
        if (args == null || args.length <= 0) {
            return;
        }

        String rootArg = null;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (legalArguments.get(arg.toLowerCase()) != null) {
                    rootArg = arg;
                    break;
                }
            }
        }

        if (rootArg == null || !validateArgNames(rootArg, args)) {
            throw new KineticException("wrong commands");
        }
    }

    @SuppressWarnings("static-access")
    public static void main(String args[]) {
        if (args.length < 1) {
            printHelp();
            System.exit(OK);
        }

        KineticMessage response = null;

        KineticAdminCLI kineticAdminCLI = null;
        try {
            kineticAdminCLI = new KineticAdminCLI();
            kineticAdminCLI.validateArgNames(args);
            if (args[0].equalsIgnoreCase("-help")
                    || args[0].equalsIgnoreCase("-h")) {
                kineticAdminCLI.printHelp();
                System.exit(OK);
            } else if (args[0].equalsIgnoreCase("-setup")) {
                initAdminClient(args, kineticAdminCLI);

                String pin = kineticAdminCLI.getArgValue("-pin", args);
                String setPin = kineticAdminCLI.getArgValue("-setpin", args);
                String newClusterVersion = kineticAdminCLI.getArgValue(
                        "-newclversion", args);
                String erase = kineticAdminCLI.getArgValue("-erase", args);
                response = kineticAdminCLI.setup(pin, setPin,
                        newClusterVersion, erase);
                kineticAdminCLI.printResponse((Message) response.getMessage());

            } else if (args[0].equalsIgnoreCase("-security")) {
                initAdminClient(args, kineticAdminCLI);

                String file = kineticAdminCLI.getArgValue("-security", args);
                response = kineticAdminCLI.security(file);
                kineticAdminCLI.printResponse((Message) response.getMessage());

            } else if (args[0].equalsIgnoreCase("-getlog")) {
                initAdminClient(args, kineticAdminCLI);

                String type = kineticAdminCLI.getArgValue("-type", args);
                type = type == null ? ALL : type;
                response = kineticAdminCLI.getLog(type);
                kineticAdminCLI.printResponse((Message) response.getMessage());

            } else if (args[0].equalsIgnoreCase("-firmware")) {
                initAdminClient(args, kineticAdminCLI);

                String pin = kineticAdminCLI.getArgValue("-pin", args);
                String firmwareFile = kineticAdminCLI.getArgValue("-firmware",
                        args);
                response = kineticAdminCLI.firmwareDownload(pin, firmwareFile);
                kineticAdminCLI.printResponse((Message) response.getMessage());

            } else {
                printHelp();
            }
        } catch (Exception e) {
            System.out.println(e);
            printHelp();
        } finally {
            try {
                if (kineticAdminCLI != null) {
                    kineticAdminCLI.close();
                }

            } catch (KineticException e) {
                System.exit(ERROR);
            }
        }
        System.exit(OK);
    }

    private static void initAdminClient(String[] args,
            KineticAdminCLI kineticAdminCLI) throws KineticException {
        String host;
        String port;
        String clusterVersion;
        host = kineticAdminCLI.getArgValue("-host", args);
        port = kineticAdminCLI.getArgValue("-tlsport", args);
        clusterVersion = kineticAdminCLI.getArgValue("-clversion", args);
        kineticAdminCLI.init(host, port, clusterVersion);
    }

    private List<String> initSubArgs() {
        List<String> subArgs;
        subArgs = new ArrayList<String>();
        subArgs.add("-host");
        subArgs.add("-tlsport");
        subArgs.add("-clversion");
        return subArgs;
    }

    private boolean contain(List<String> list, String item) {
        for (String str : list) {
            if (str.equalsIgnoreCase(item)) {
                return true;
            }
        }

        return false;
    }

    private boolean validateArgNames(String rootArg, String args[]) {
        List<String> subArgs = legalArguments.get(rootArg);
        for (String arg : args) {
            if (arg.equals(rootArg)) {
                continue;
            }

            if (arg.startsWith("-") && !contain(subArgs, arg)) {
                return false;
            }
        }

        return true;
    }

    private void validateHost(String host) throws IllegalArgumentException {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Host can not be empty");
        }
    }

    private void validatePort(String port) throws IllegalArgumentException {
        if (port == null || port.isEmpty()) {
            throw new IllegalArgumentException("Port can not be empty");
        }

        Pattern pattern = Pattern
                .compile("^([1-9]|[1-9]\\d{3}|[1-6][0-5][0-5][0-3][0-5])$");
        Matcher matcher = pattern.matcher(port);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Illegal port");
        }
    }

    private void validateLogType(String logType)
            throws IllegalArgumentException {
        if (logType == null || logType.isEmpty()) {
            throw new IllegalArgumentException("Type can not be empty");
        }

        if (!logType.equalsIgnoreCase(CAPACITY)
                && !logType.equalsIgnoreCase(TEMPERATURE)
                && !logType.equalsIgnoreCase(UTILIZATION)
                && !logType.equalsIgnoreCase(CONFIGURATION)
                && !logType.equalsIgnoreCase(MESSAGES)
                && !logType.equalsIgnoreCase(STATISTICS)
                && !logType.equalsIgnoreCase(ALL)) {
            throw new IllegalArgumentException(
                    "Type should be utilization, capacity, temperature, configuration, message, statistic or all");
        }
    }

    private void validateClusterVersion(String clusterVersion)
            throws IllegalArgumentException {
        if (clusterVersion == null || clusterVersion.isEmpty()) {
            throw new IllegalArgumentException(
                    "clusterVersion can not be empty");
        }

        Pattern pattern = Pattern.compile("[0-9]*");
        if (!pattern.matcher(clusterVersion).matches()) {
            throw new IllegalArgumentException(
                    "clusterVersion should be a long number");
        }
    }

    private void validateErase(String erase) throws IllegalArgumentException {
        if (erase == null || erase.isEmpty()) {
            throw new IllegalArgumentException("Erase can not be empty.");
        }

        if (!erase.equalsIgnoreCase(TRUE) && !erase.equalsIgnoreCase(FALSE)) {
            throw new IllegalArgumentException("Erase should be true or false.");
        }
    }
}
