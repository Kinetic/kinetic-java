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
        subArgs.add("-pin");
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
    public void init(String host, String tlsPort, String clusterVersion)
            throws KineticException {
        AdminClientConfiguration adminClientConfig = new AdminClientConfiguration();
        adminClientConfig.setRequestTimeoutMillis(DEFAULT_REQUEST_TIMEOUT);
        if (host != null && !host.isEmpty()) {
            validateHost(host);
            adminClientConfig.setHost(host);
        } else {
            adminClientConfig.setHost(DEFAULT_HOST);
        }

        if (tlsPort != null && !tlsPort.isEmpty()) {
            validatePort(tlsPort);
            adminClientConfig.setPort(Integer.parseInt(tlsPort));
        } else {
            adminClientConfig.setPort(DEFAULT_SSL_PORT);
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

        byte[] content = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);
        in.read(content);
        in.close();
        return content;
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

    public void firmwareDownload(String pin, String firmwareFile)
            throws IOException, KineticException {
        byte[] content = readFile(firmwareFile);

        byte[] pinB = null;
        if (pin != null) {
            pinB = pin.getBytes(Charset.forName("UTF-8"));
        }

        kineticAdminClient.firmwareDownload(pinB, content);
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
        sb.append("kineticAdmin -setclusterversion <-newclversion <newclusterversion>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -seterasepin <-olderasepin <olderasepin>> <-newerasepin <newerasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -setlockpin <-oldlockpin <oldlockpin>> <-newlockpin <newlockpin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -instanterase <-pin <erasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -secureerase <-pin <erasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -security <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -getlog [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-type <utilization|temperature|capacity|configuration|message|statistic|limits|all>]\n");
        sb.append("kineticAdmin -getvendorspecificdevicelog <-name <vendorspecificname>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -firmware <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>]\n");
        sb.append("kineticAdmin -lockdevice <-pin <lockpin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]\n");
        sb.append("kineticAdmin -unlockdevice <-pin <lockpin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]");
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
        System.out.println("device name: " + new String(device.getName()));
        System.out.println("device value: " + new String(device.getValue()));
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
                initAdminClient(args, kineticAdminCLI);

                String pin = kineticAdminCLI.getArgValue("-pin", args);
                String firmwareFile = kineticAdminCLI.getArgValue("-firmware",
                        args);
                kineticAdminCLI.firmwareDownload(pin, firmwareFile);
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
            System.out.println("NominalCapacityInBytes: "
                    + capacity.getNominalCapacityInBytes());
            System.out.println("PortionFull: " + capacity.getPortionFull()
                    + "\n");
        }
    }

    private void printTemperatures(KineticLog kineticLog)
            throws KineticException {
        List<Temperature> temps = new ArrayList<Temperature>();
        temps = kineticLog.getTemperature();
        if (temps != null && !temps.isEmpty()) {
            for (Temperature temp : temps) {
                System.out.println("Name: " + temp.getName());
                System.out.println("Max: " + temp.getMax());
                System.out.println("Min: " + temp.getMin());
                System.out.println("Target: " + temp.getTarget());
                System.out.println("Current: " + temp.getCurrent() + "\n");
            }
        }
    }

    private void printUtilizations(KineticLog kineticLog)
            throws KineticException {
        List<Utilization> utils = new ArrayList<Utilization>();
        utils = kineticLog.getUtilization();
        if (utils != null && !utils.isEmpty()) {
            for (Utilization util : utils) {
                System.out.println("Name: " + util.getName());
                System.out.println("Utility: " + util.getUtility() + "\n");
            }
        }
    }

    private void printStatistics(KineticLog kineticLog) throws KineticException {
        List<Statistics> statis = new ArrayList<Statistics>();
        statis = kineticLog.getStatistics();
        if (statis != null && !statis.isEmpty()) {
            for (Statistics stati : statis) {
                System.out.println("MessageType: " + stati.getMessageType());
                System.out.println("Count: " + stati.getCount());
                System.out.println("Bytes: " + stati.getBytes() + "\n");
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
            System.out.println("CompilationDate: "
                    + config.getCompilationDate());
            System.out.println("Model: " + config.getModel());
            System.out.println("Port: " + config.getPort());
            System.out.println("TlsPort: " + config.getTlsPort());
            System.out.println("ProtocolCompilationDate: "
                    + config.getProtocolCompilationDate());
            System.out.println("ProtocolSourceHash: "
                    + config.getProtocolSourceHash());
            System.out.println("ProtocolVersion: "
                    + config.getProtocolVersion());
            System.out.println("SerialNumber: " + config.getSerialNumber());
            System.out.println("WorldWideName: " + config.getWorldWideName());
            System.out.println("SourceHash: " + config.getSourceHash());
            System.out.println("Vendor: " + config.getVendor());
            System.out.println("Version: " + config.getVersion());

            List<Interface> inets = new ArrayList<Interface>();
            inets = config.getInterfaces();
            if (inets != null && !inets.isEmpty()) {
                for (Interface inet : inets) {
                    System.out.println("Name: " + inet.getName());
                    System.out.println("Mac: " + inet.getMAC());
                    System.out.println("Ipv4Address: " + inet.getIpv4Address());
                    System.out.println("Ipv6Address: " + inet.getIpv6Address()
                            + "\n");
                }
            }
        }
    }

    private void printLimits(KineticLog kineticLog) throws KineticException {
        Limits limits = kineticLog.getLimits();
        if (limits != null) {
            System.out.println("MaxConnections: " + limits.getMaxConnections());
            System.out.println("MaxIdentityCount: "
                    + limits.getMaxIdentityCount());
            System.out.println("MaxKeyRangeCount: "
                    + limits.getMaxKeyRangeCount());
            System.out.println("MaxKeySize: " + limits.getMaxKeySize());
            System.out.println("MaxMessageSize: " + limits.getMaxMessageSize());
            System.out.println("MaxOutstandingReadRequests: "
                    + limits.getMaxOutstandingReadRequests());
            System.out.println("MaxOutstandingWriteRequests: "
                    + limits.getMaxOutstandingWriteRequests());
            System.out.println("MaxTagSize: " + limits.getMaxTagSize());
            System.out.println("MaxValueSize: " + limits.getMaxValueSize());
            System.out.println("MaxVersionSize: " + limits.getMaxVersionSize());
        }
    }

}
