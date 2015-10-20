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
package com.seagate.kinetic.admin.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kinetic.admin.ACL;
import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.Capacity;
import kinetic.admin.Configuration;
import kinetic.admin.Device;
import kinetic.admin.Domain;
import kinetic.admin.Interface;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.admin.Limits;
import kinetic.admin.Statistics;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.admin.impl.JsonUtil;
import com.seagate.kinetic.proto.Kinetic.Command.Security;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;

/**
 *
 * KineticClient command line tool, support setup security getlog and help
 * <p>
 *
 *
 */
public class KineticAdminCLI {
    private static final String ALL = "all";
    private static final String TEMPERATURE = "temperature";
    private static final String CAPACITY = "capacity";
    private static final String UTILIZATION = "utilization";
    private static final String CONFIGURATION = "configuration";
    private static final String MESSAGES = "message";
    private static final String STATISTICS = "statistic";
    private static final String LIMITS = "limits";
    private static final int DEFAULT_PORT = 8123;
    private static final int DEFAULT_SSL_PORT = 8443;
    private static final String DEFAULT_HOST = "localhost";
    private static final long CLUSTERVERSION = 0;
    private static final int OK = 0;
    private static final int ERROR = 1;
    private static KineticAdminClient kineticAdminClient = null;
    private final Map<String, List<String>> legalArguments = new HashMap<String, List<String>>();

    /**
     * provide a way to override default request timeout at runtime.
     */
    private static long DEFAULT_REQUEST_TIMEOUT = Integer.getInteger(
            ClientConfiguration.DEFAULT_TIMEOUT_PROP_NAME, 180000).longValue();

    public KineticAdminCLI() throws KineticException {
        String rootArg = "-help";
        List<String> subArgs = new ArrayList<String>();
        legalArguments.put(rootArg, subArgs);

        rootArg = "-h";
        subArgs = new ArrayList<String>();
        legalArguments.put(rootArg, subArgs);

        rootArg = "-setclusterversion";
        subArgs = initSubArgs();
        subArgs.add("-newclversion");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-seterasepin";
        subArgs = initSubArgs();
        subArgs.add("-olderasepin");
        subArgs.add("-newerasepin");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-setlockpin";
        subArgs = initSubArgs();
        subArgs.add("-oldlockpin");
        subArgs.add("-newlockpin");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-setlockpin";
        subArgs = initSubArgs();
        subArgs.add("-oldlockpin");
        subArgs.add("-newlockpin");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-instanterase";
        subArgs = initSubArgs();
        subArgs.add("-pin");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-secureerase";
        subArgs = initSubArgs();
        subArgs.add("-pin");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-security";
        subArgs = initSubArgs();
        legalArguments.put(rootArg, subArgs);

        rootArg = "-getlog";
        subArgs = initSubArgs();
        subArgs.add("-type");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-getvendorspecificdevicelog";
        subArgs = initSubArgs();
        subArgs.add("-name");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-firmware";
        subArgs = initSubArgs();
        legalArguments.put(rootArg, subArgs);

        rootArg = "-lockdevice";
        subArgs = initSubArgs();
        subArgs.add("-pin");
        legalArguments.put(rootArg, subArgs);

        rootArg = "-unlockdevice";
        subArgs = initSubArgs();
        subArgs.add("-pin");
        legalArguments.put(rootArg, subArgs);
    }

    /*
     * init Kinetic client
     */
    public void init(String host, String useSsl, String port,
            String clusterVersion) throws KineticException {
        AdminClientConfiguration adminClientConfig = new AdminClientConfiguration();
        adminClientConfig.setRequestTimeoutMillis(DEFAULT_REQUEST_TIMEOUT);
        if (host != null && !host.isEmpty()) {
            validateHost(host);
            adminClientConfig.setHost(host);
        } else {
            adminClientConfig.setHost(DEFAULT_HOST);
        }

        if (useSsl == null || useSsl.isEmpty() || Boolean.parseBoolean(useSsl)) {
            adminClientConfig.setUseSsl(true);
            adminClientConfig.setPort(DEFAULT_SSL_PORT);
        } else {
            adminClientConfig.setUseSsl(false);
            adminClientConfig.setPort(DEFAULT_PORT);
        }

        if (port != null && !port.isEmpty()) {
            validatePort(port);
            adminClientConfig.setPort(Integer.parseInt(port));
        }

        if (clusterVersion != null && !clusterVersion.isEmpty()) {
            adminClientConfig.setClusterVersion(Integer
                    .parseInt(clusterVersion));
        } else {
            adminClientConfig.setClusterVersion(CLUSTERVERSION);
        }

        kineticAdminClient = KineticAdminClientFactory
                .createInstance(adminClientConfig);
    }

    public void close() throws KineticException {
        if (kineticAdminClient != null) {
            kineticAdminClient.close();
        }
    }

    public void security(String securityFile) throws IOException,
            KineticException {
        byte[] content = readFile(securityFile);

        Security security = JsonUtil.parseSecurity(ByteString.copyFrom(content)
                .toStringUtf8());

        ACL myAcl = null;
        Domain myDomain = null;
        List<ACL> myAclList = new ArrayList<ACL>();
        List<Domain> myDomainList = null;
        for (com.seagate.kinetic.proto.Kinetic.Command.Security.ACL acl : security
                .getAclList()) {
            myAcl = new ACL();

            myAcl.setUserId(acl.getIdentity());
            myAcl.setKey(acl.getKey().toStringUtf8());

            myDomainList = new ArrayList<Domain>();
            List<kinetic.admin.Role> roleList = null;
            for (com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope domain : acl
                    .getScopeList()) {
                myDomain = new Domain();
                roleList = new ArrayList<kinetic.admin.Role>();
                myDomain.setOffset(domain.getOffset());
                myDomain.setValue(domain.getValue().toStringUtf8());
                for (Permission role : domain.getPermissionList()) {
                    roleList.add(kinetic.admin.Role.valueOf(role.toString()));
                }
                myDomain.setRoles(roleList);
                myDomainList.add(myDomain);
            }
            myAcl.setDomains(myDomainList);

            myAclList.add(myAcl);
        }

        kineticAdminClient.setAcl(myAclList);
    }

    private byte[] readFile(String securityFile) throws FileNotFoundException,
            IOException {
        File file = new File(securityFile);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        if (file.length() > Integer.MAX_VALUE) {
            throw new IOException("File is too large!");
        }

        InputStream in = null;
        in = new FileInputStream(file);

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = in.read(b)) != -1) {
            content.write(b, 0, n);
        }
        in.close();

        return content.toByteArray();
    }

    public KineticLog getLog(String type) throws KineticException {
        validateLogType(type);

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();

        if (type.equalsIgnoreCase(ALL)) {
            return kineticAdminClient.getLog();

        } else if (type.equalsIgnoreCase(UTILIZATION)) {
            listOfLogType.add(KineticLogType.UTILIZATIONS);
        } else if (type.equalsIgnoreCase(CAPACITY)) {
            listOfLogType.add(KineticLogType.CAPACITIES);
        } else if (type.equalsIgnoreCase(TEMPERATURE)) {
            listOfLogType.add(KineticLogType.TEMPERATURES);
        } else if (type.equalsIgnoreCase(CONFIGURATION)) {
            listOfLogType.add(KineticLogType.CONFIGURATION);
        } else if (type.equalsIgnoreCase(MESSAGES)) {
            listOfLogType.add(KineticLogType.MESSAGES);
        } else if (type.equalsIgnoreCase(STATISTICS)) {
            listOfLogType.add(KineticLogType.STATISTICS);
        } else if (type.equalsIgnoreCase(LIMITS)) {
            listOfLogType.add(KineticLogType.LIMITS);
        } else {
            throw new IllegalArgumentException(
                    "Type should be utilization, capacity, temperature, configuration, message, statistic, limits or all");
        }

        return kineticAdminClient.getLog(listOfLogType);
    }

    public void setClusterVersion(String newClusterVersion)
            throws KineticException {
        if (newClusterVersion != null) {
            validateClusterVersion(newClusterVersion);
            kineticAdminClient.setClusterVersion(Long
                    .parseLong(newClusterVersion));
        }
    }

    public void setErasePin(String oldErasePin, String newErasePin)
            throws KineticException {
        byte[] oldErasePinB = null;
        byte[] newErasePinB = null;

        if (oldErasePin != null) {
            oldErasePinB = oldErasePin.getBytes(Charset.forName("UTF-8"));
            newErasePinB = newErasePin.getBytes(Charset.forName("UTF-8"));
        }

        kineticAdminClient.setErasePin(oldErasePinB, newErasePinB);
    }

    public void setLockPin(String oldLockPin, String newLockPin)
            throws KineticException {
        byte[] oldLockPinB = null;
        byte[] newLockPinB = null;

        if (oldLockPin != null) {
            oldLockPinB = oldLockPin.getBytes(Charset.forName("UTF-8"));
            newLockPinB = newLockPin.getBytes(Charset.forName("UTF-8"));
        }

        kineticAdminClient.setLockPin(oldLockPinB, newLockPinB);
    }

    public void instantErase(String erasePin) throws KineticException {
        byte[] erasePinB = null;

        if (erasePin != null) {
            erasePinB = erasePin.getBytes(Charset.forName("UTF-8"));
        }

        kineticAdminClient.instantErase(erasePinB);
    }

    public void secureErase(String erasePin) throws KineticException {
        byte[] erasePinB = null;

        if (erasePin != null) {
            erasePinB = erasePin.getBytes(Charset.forName("UTF-8"));
        }

        kineticAdminClient.secureErase(erasePinB);
    }

    public Device getvendorspecificdevicelog(String name)
            throws KineticException {
        byte[] nameB = null;

        if (name != null) {
            nameB = name.getBytes(Charset.forName("UTF-8"));
        }

        return kineticAdminClient.getVendorSpecificDeviceLog(nameB);
    }

    public void firmwareDownload(String firmwareFile) throws IOException,
            KineticException {
        byte[] content = readFile(firmwareFile);

        kineticAdminClient.firmwareDownload(content);
    }

    public void lockDevice(String lockPin) throws KineticException {
        byte[] lockPinB = null;

        if (lockPin != null) {
            lockPinB = lockPin.getBytes(Charset.forName("UTF-8"));
        }

        kineticAdminClient.lockDevice(lockPinB);
    }

    public void unLockDevice(String lockPin) throws KineticException {
        byte[] lockPinB = null;

        if (lockPin != null) {
            lockPinB = lockPin.getBytes(Charset.forName("UTF-8"));
        }

        kineticAdminClient.unLockDevice(lockPinB);
    }

    public static void printHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("Usage: kineticAdmin <-setclusterversion|-seterasepin|-setlockpin|-instanterase|-secureerase|-security|-getlog|-getvendorspecificdevicelog|-firmware|-lockdevice|-unlockdevice>\n");
        sb.append("kineticAdmin -h|-help\n");
        sb.append("kineticAdmin -setclusterversion <-newclversion <newclusterversion>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -seterasepin <-olderasepin <olderasepin>> <-newerasepin <newerasepin>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -setlockpin <-oldlockpin <oldlockpin>> <-newlockpin <newlockpin>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -instanterase <-pin <erasepin>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -secureerase <-pin <erasepin>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -security <file> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -getlog [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>] [-type <utilization|temperature|capacity|configuration|message|statistic|limits|all>]\n");
        sb.append("kineticAdmin -getvendorspecificdevicelog <-name <vendorspecificname>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -firmware <file> [-host <ip|hostname>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -lockdevice <-pin <lockpin>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -unlockdevice <-pin <lockpin>> [-host <ip|hostname>] [-usessl <true|false>] [-port <port>] [-clversion <clusterversion>]");
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

        if (index != -1 && args.length > (index + 1)) {
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

    public void printGetLogResult(KineticLog kineticLog)
            throws KineticException {
        if (null == kineticLog) {
            return;
        }

        KineticLogType[] types = kineticLog.getContainedLogTypes();
        if (types != null && types.length > 0) {
            for (KineticLogType type : types) {
                System.out.println("KineticLogType: " + type);
                if (type.equals(KineticLogType.CAPACITIES)) {
                    printCapacity(kineticLog);
                } else if (type.equals(KineticLogType.TEMPERATURES)) {
                    printTemperatures(kineticLog);
                } else if (type.equals(KineticLogType.UTILIZATIONS)) {
                    printUtilizations(kineticLog);
                } else if (type.equals(KineticLogType.STATISTICS)) {
                    printStatistics(kineticLog);
                } else if (type.equals(KineticLogType.MESSAGES)) {
                    printMessages(kineticLog);
                } else if (type.equals(KineticLogType.CONFIGURATION)) {
                    printConfiguration(kineticLog);
                } else if (type.equals(KineticLogType.LIMITS)) {
                    printLimits(kineticLog);
                }
            }
        }
    }

    public void printGetVendorSpecificDeviceLog(Device device) {
        StringBuffer sb = new StringBuffer();
        sb.append("device name: " + new String(device.getName()) + "\n");
        sb.append("device value: " + new String(device.getValue()));
        System.out.println(sb);
    }

    public void printSuccessResult() {
        System.out.println("SUCCESS");
    }

    public void validateArgNames(String args[]) throws Exception {
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
            throw new Exception("wrong commands");
        }
    }

    @SuppressWarnings("static-access")
    public static void main(String args[]) {
        if (args.length < 1) {
            printHelp();
            System.exit(OK);
        }

        KineticAdminCLI kineticAdminCLI = null;
        try {
            kineticAdminCLI = new KineticAdminCLI();
            kineticAdminCLI.validateArgNames(args);
            if (args[0].equalsIgnoreCase("-help")
                    || args[0].equalsIgnoreCase("-h")) {
                kineticAdminCLI.printHelp();
                System.exit(OK);
            } else if (args[0].equalsIgnoreCase("-setclusterversion")) {
                initAdminClient(args, kineticAdminCLI);

                String newClusterVersion = kineticAdminCLI.getArgValue(
                        "-newclversion", args);
                kineticAdminCLI.setClusterVersion(newClusterVersion);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-seterasepin")) {

                initAdminClient(args, kineticAdminCLI);

                String oldErasePin = kineticAdminCLI.getArgValue(
                        "-olderasepin", args);
                String newErasePin = kineticAdminCLI.getArgValue(
                        "-newerasepin", args);

                kineticAdminCLI.setErasePin(oldErasePin, newErasePin);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-setlockpin")) {
                initAdminClient(args, kineticAdminCLI);

                String oldLockPin = kineticAdminCLI.getArgValue("-oldlockpin",
                        args);
                String newLockPin = kineticAdminCLI.getArgValue("-newlockpin",
                        args);

                kineticAdminCLI.setLockPin(oldLockPin, newLockPin);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-instanterase")) {
                initAdminClient(args, kineticAdminCLI);

                String erasePin = kineticAdminCLI.getArgValue("-pin", args);

                kineticAdminCLI.instantErase(erasePin);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-secureerase")) {
                initAdminClient(args, kineticAdminCLI);

                String erasePin = kineticAdminCLI.getArgValue("-pin", args);

                kineticAdminCLI.secureErase(erasePin);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-security")) {
                initAdminClient(args, kineticAdminCLI);

                String file = kineticAdminCLI.getArgValue("-security", args);
                kineticAdminCLI.security(file);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-getlog")) {
                initAdminClient(args, kineticAdminCLI);

                String type = kineticAdminCLI.getArgValue("-type", args);
                type = type == null ? ALL : type;
                KineticLog kineticLog = kineticAdminCLI.getLog(type);
                kineticAdminCLI.printGetLogResult(kineticLog);

            } else if (args[0].equalsIgnoreCase("-getvendorspecificdevicelog")) {
                initAdminClient(args, kineticAdminCLI);

                String name = kineticAdminCLI.getArgValue("-name", args);
                Device device = kineticAdminCLI
                        .getvendorspecificdevicelog(name);
                if (device != null) {
                    kineticAdminCLI.printGetVendorSpecificDeviceLog(device);
                }
            } else if (args[0].equalsIgnoreCase("-firmware")) {
                String host;
                String port;
                String clusterVersion;
                String useSsl = "false";
                host = kineticAdminCLI.getArgValue("-host", args);
                port = kineticAdminCLI.getArgValue("-port", args);
                clusterVersion = kineticAdminCLI
                        .getArgValue("-clversion", args);
                kineticAdminCLI.init(host, useSsl, port, clusterVersion);

                String firmwareFile = kineticAdminCLI.getArgValue("-firmware",
                        args);
                kineticAdminCLI.firmwareDownload(firmwareFile);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-lockdevice")) {
                initAdminClient(args, kineticAdminCLI);

                String lockPin = kineticAdminCLI.getArgValue("-pin", args);
                kineticAdminCLI.lockDevice(lockPin);
                kineticAdminCLI.printSuccessResult();
            } else if (args[0].equalsIgnoreCase("-unlockdevice")) {
                initAdminClient(args, kineticAdminCLI);

                String unLockPin = kineticAdminCLI.getArgValue("-pin", args);
                kineticAdminCLI.unLockDevice(unLockPin);
                kineticAdminCLI.printSuccessResult();
            } else {
                printHelp();
            }
        } catch (KineticException ke) {
            if (ke.getResponseMessage() != null
                    && ke.getResponseMessage().getCommand() != null
                    && ke.getResponseMessage().getCommand().getStatus() != null
                    && ke.getResponseMessage().getCommand().getStatus()
                            .getCode() != null) {
                System.out.println(ke.getResponseMessage().getCommand()
                        .getStatus().getCode());
            } else {
                System.out.println(ke);
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
        String useSsl;
        host = kineticAdminCLI.getArgValue("-host", args);
        useSsl = kineticAdminCLI.getArgValue("-usessl", args);
        port = kineticAdminCLI.getArgValue("-port", args);
        clusterVersion = kineticAdminCLI.getArgValue("-clversion", args);
        kineticAdminCLI.init(host, useSsl, port, clusterVersion);
    }

    private List<String> initSubArgs() {
        List<String> subArgs;
        subArgs = new ArrayList<String>();
        subArgs.add("-host");
        subArgs.add("-port");
        subArgs.add("-usessl");
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
                && !logType.equalsIgnoreCase(LIMITS)
                && !logType.equalsIgnoreCase(ALL)) {
            throw new IllegalArgumentException(
                    "Type should be utilization, capacity, temperature, configuration, message, statistic, limits or all");
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

    private void printCapacity(KineticLog kineticLog) throws KineticException {
        Capacity capacity = kineticLog.getCapacity();
        if (capacity != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("NominalCapacityInBytes: "
                    + capacity.getNominalCapacityInBytes() + "\n");
            sb.append("PortionFull: " + capacity.getPortionFull() + "\n");
            System.out.println(sb);
        }
    }

    private void printTemperatures(KineticLog kineticLog)
            throws KineticException {
        List<Temperature> temps = new ArrayList<Temperature>();
        temps = kineticLog.getTemperature();
        if (temps != null && !temps.isEmpty()) {
            for (Temperature temp : temps) {
                StringBuffer sb = new StringBuffer();
                sb.append("Name: " + temp.getName() + "\n");
                sb.append("Max: " + temp.getMax() + "\n");
                sb.append("Min: " + temp.getMin() + "\n");
                sb.append("Target: " + temp.getTarget() + "\n");
                sb.append("Current: " + temp.getCurrent() + "\n");
                System.out.println(sb);
            }
        }
    }

    private void printUtilizations(KineticLog kineticLog)
            throws KineticException {
        List<Utilization> utils = new ArrayList<Utilization>();
        utils = kineticLog.getUtilization();
        if (utils != null && !utils.isEmpty()) {
            for (Utilization util : utils) {
                StringBuffer sb = new StringBuffer();
                sb.append("Name: " + util.getName() + "\n");
                sb.append("Utility: " + util.getUtility() + "\n");
                System.out.println(sb);
            }
        }
    }

    private void printStatistics(KineticLog kineticLog) throws KineticException {
        List<Statistics> statis = new ArrayList<Statistics>();
        statis = kineticLog.getStatistics();
        if (statis != null && !statis.isEmpty()) {
            for (Statistics stati : statis) {
                StringBuffer sb = new StringBuffer();
                sb.append("MessageType: " + stati.getMessageType() + "\n");
                sb.append("Count: " + stati.getCount() + "\n");
                sb.append("Bytes: " + stati.getBytes() + "\n");
                System.out.println(sb);
            }
        }
    }

    private void printMessages(KineticLog kineticLog) throws KineticException {
        byte[] message = kineticLog.getMessages();
        if (message != null) {
            System.out.println("Message: " + new String(message) + "\n");
        }
    }

    private void printConfiguration(KineticLog kineticLog)
            throws KineticException {
        Configuration config = kineticLog.getConfiguration();
        if (config != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("CompilationDate: " + config.getCompilationDate() + "\n");
            sb.append("Model: " + config.getModel() + "\n");
            sb.append("Port: " + config.getPort() + "\n");
            sb.append("TlsPort: " + config.getTlsPort() + "\n");
            sb.append("ProtocolCompilationDate: "
                    + config.getProtocolCompilationDate() + "\n");
            sb.append("ProtocolSourceHash: " + config.getProtocolSourceHash()
                    + "\n");
            sb.append("ProtocolVersion: " + config.getProtocolVersion() + "\n");
            sb.append("SerialNumber: " + config.getSerialNumber() + "\n");
            sb.append("WorldWideName: " + config.getWorldWideName() + "\n");
            sb.append("SourceHash: " + config.getSourceHash() + "\n");
            sb.append("Vendor: " + config.getVendor() + "\n");
            sb.append("Version: " + config.getVersion() + "\n");
            System.out.println(sb);

            List<Interface> inets = new ArrayList<Interface>();
            inets = config.getInterfaces();
            if (inets != null && !inets.isEmpty()) {
                for (Interface inet : inets) {
                    StringBuffer sbIft = new StringBuffer();
                    sbIft.append("Name: " + inet.getName() + "\n");
                    sbIft.append("Mac: " + inet.getMAC() + "\n");
                    sbIft.append("Ipv4Address: " + inet.getIpv4Address() + "\n");
                    sbIft.append("Ipv6Address: " + inet.getIpv6Address() + "\n");
                    System.out.println(sbIft);
                }
            }
        }
    }

    private void printLimits(KineticLog kineticLog) throws KineticException {
        Limits limits = kineticLog.getLimits();
        if (limits != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("MaxConnections: " + limits.getMaxConnections() + "\n");
            sb.append("MaxIdentityCount: " + limits.getMaxIdentityCount()
                    + "\n");
            sb.append("MaxKeyRangeCount: " + limits.getMaxKeyRangeCount()
                    + "\n");
            sb.append("MaxKeySize: " + limits.getMaxKeySize() + "\n");
            sb.append("MaxMessageSize: " + limits.getMaxMessageSize() + "\n");
            sb.append("MaxOutstandingReadRequests: "
                    + limits.getMaxOutstandingReadRequests() + "\n");
            sb.append("MaxOutstandingWriteRequests: "
                    + limits.getMaxOutstandingWriteRequests() + "\n");
            sb.append("MaxTagSize: " + limits.getMaxTagSize() + "\n");
            sb.append("MaxValueSize: " + limits.getMaxValueSize() + "\n");
            sb.append("MaxVersionSize: " + limits.getMaxVersionSize() + "\n");

            System.out.println(sb);
        }
    }

}
