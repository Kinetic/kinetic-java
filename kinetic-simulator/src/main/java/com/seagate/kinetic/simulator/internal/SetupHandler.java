/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Message.Setup;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;
import com.seagate.kinetic.simulator.lib.SetupInfo;
import com.seagate.kinetic.simulator.persist.Store;

/**
 *
 * Setup info handler
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
public abstract class SetupHandler {
    private final static Logger logger = Logger.getLogger(SetupHandler.class
            .getName());

    public static boolean checkPermission(Message request,
            Message.Builder respond, Map<Long, ACL> currentMap) {
        boolean hasPermission = false;

        // set reply type
        respond.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.SETUP_RESPONSE);
        // set ack sequence
        respond.getCommandBuilder().getHeaderBuilder()
        .setAckSequence(request.getCommand().getHeader().getSequence());

        // check if has permission to set security
        if (currentMap == null) {
            hasPermission = true;
        } else {
            try {
                // check if client has permission
                Authorizer.checkPermission(currentMap, request.getCommand()
                        .getHeader().getIdentity(), Permission.SETUP);

                hasPermission = true;
            } catch (KVSecurityException e) {
                respond.getCommandBuilder().getStatusBuilder()
                .setCode(StatusCode.NOT_AUTHORIZED);
                respond.getCommandBuilder().getStatusBuilder()
                .setStatusMessage(e.getMessage());
            }
        }

        return hasPermission;
    }

    @SuppressWarnings("rawtypes")
    public static synchronized SetupInfo handleSetup(KineticMessage request,
            Message.Builder respond, byte[] myPin, Store store,
            String kineticHome) throws KVStoreException, IOException {
        SetupInfo setupInfo = null;

        byte[] newPin = request.getMessage().getCommand().getBody().getSetup()
                .getPin()
                .toByteArray();
        if (null == newPin) {
            return setupInfo;
        }

        logger.info("parameterPin=" + new String(newPin) + ", internalPin="
                + new String(myPin));

        if (null == myPin || 0 == myPin.length) {
            setupInfo = handleSetup(request, respond, myPin, newPin, store,
                    kineticHome);
        } else if (Arrays.equals(newPin, myPin)) {
            setupInfo = handleSetup(request, respond, myPin, newPin, store,
                    kineticHome);
        } else {
            respond.getCommandBuilder().getStatusBuilder()
            .setCode(StatusCode.INTERNAL_ERROR);
            respond.getCommandBuilder().getStatusBuilder()
            .setStatusMessage("Pin not match");
        }

        return setupInfo;
    }

    @SuppressWarnings("rawtypes")
    private static SetupInfo handleSetup(KineticMessage request,
            Message.Builder respond, byte[] myPin, byte[] newPin, Store store,
            String kineticHome) throws IOException, KVStoreException {
        SetupInfo setupInfo = new SetupInfo();
        // persist setupInfo
        SetupHandler.persistSetup(request.getMessage().getCommand().getBody()
                .getSetup()
                .toByteArray(), kineticHome);

        // modify clusterVersion
        if (request.getMessage().getCommand().getBody().getSetup()
                .hasNewClusterVersion()) {
            Long newClusterVersion = request.getMessage().getCommand()
                    .getBody().getSetup()
                    .getNewClusterVersion();
            if (null != newClusterVersion) {
                setupInfo.setClusterVersion(newClusterVersion);
                logger.info("the cluster version is set: "
                        + Long.valueOf(newClusterVersion));
            }
        }

        // erase the db data
        if (request.getMessage().getCommand().getBody().getSetup()
                .getInstantSecureErase()) {
            store.reset();
            logger.info("erase db finish!");
        }

        // set pin
        if (request.getMessage().getCommand().getBody().getSetup().hasSetPin()) {
            myPin = request.getMessage().getCommand().getBody().getSetup()
                    .getSetPin()
                    .toByteArray();
            setupInfo.setPin(myPin);
            logger.info("the drive pin is set: " + new String(myPin));
        }

        // persist firmware download
        if (request.getMessage().getCommand().getBody().getSetup()
                .getFirmwareDownload()) {
            if (request.getValue() != null) {
                byte[] firmwareDownloadValue = request.getValue();
                persistFirmwareDownload(firmwareDownloadValue, kineticHome);
            }
        }

        // TODO handle exception
        respond.getCommandBuilder().getStatusBuilder()
        .setCode(StatusCode.SUCCESS);

        return setupInfo;
    }

    private static void persistSetup(byte[] contents, String kineticHome)
            throws IOException {
        String setupPersistFilePath = kineticHome + File.separator + ".setup";
        String setupPersistBakFilePath = setupPersistFilePath + ".bak";

        // delete backup file
        File aclBakFile = new File(setupPersistBakFilePath);
        if (aclBakFile.exists()) {
            aclBakFile.delete();
        }

        // backup file
        File aclFile = new File(setupPersistFilePath);
        aclFile.renameTo(aclBakFile);

        // save new file
        aclFile = new File(setupPersistFilePath);
        FileOutputStream out = new FileOutputStream(aclFile);
        out.write(contents);
        out.close();
    }

    private static void persistFirmwareDownload(byte[] firmwareContent,
            String kineticHome) throws IOException {
        String firmwarePersistFilePath = kineticHome + File.separator;

        Date date = new Date();
        Timestamp timeStamp = new Timestamp(date.getTime());

        // make directory for timestamp
        File file_home = new File(firmwarePersistFilePath + timeStamp);
        if (!file_home.exists()) {
            file_home.mkdirs();
        }

        // save new firmware file
        String file_path = file_home + File.separator + "firmware";
        File firmwareFile = new File(file_path);
        FileOutputStream out = new FileOutputStream(firmwareFile);
        out.write(firmwareContent);
        out.close();
    }

    public static SetupInfo loadSetup(String kineticHome) throws IOException {
        String setupPersistFilePath = kineticHome + File.separator + ".setup";

        File setupFile = new File(setupPersistFilePath);
        SetupInfo setupInfo = new SetupInfo();
        if (setupFile.exists()) {
            Long fileLength = setupFile.length();
            if (fileLength != 0) {
                // read info from file
                byte[] fileContent = new byte[fileLength.intValue()];
                FileInputStream in = new FileInputStream(setupFile);
                in.read(fileContent);
                in.close();
                Setup setup = Setup.parseFrom(fileContent);
                setupInfo.setClusterVersion(setup.getNewClusterVersion());
                if (!setup.getSetPin().isEmpty()) {
                    setupInfo.setPin(setup.getSetPin().toByteArray());
                } else {
                    // setupInfo.setPin(setup.getPin().toByteArray());
                    setupInfo.setPin("".getBytes());
                }
            }
        }

        return setupInfo;
    }
}
