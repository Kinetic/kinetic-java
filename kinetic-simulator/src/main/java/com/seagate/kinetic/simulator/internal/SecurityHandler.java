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
package com.seagate.kinetic.simulator.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.HMACAlgorithmUtil;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.common.lib.RoleUtil;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Security;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.simulator.lib.HmacStore;

/**
 * Security handler prototype.
 *
 * @author chiaming
 *
 */
public abstract class SecurityHandler {
    
    private final static Logger logger = Logger.getLogger(SecurityHandler.class
            .getName());
    
    public static boolean checkPermission(KineticMessage request,
            KineticMessage respond, Map<Long, ACL> currentMap) {
        
        boolean hasPermission = false;
        
        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.SECURITY_RESPONSE);
        
        // set ack sequence
        commandBuilder.getHeaderBuilder()
        .setAckSequence(request.getCommand().getHeader().getSequence());

        // check if has permission to set security
        if (currentMap == null) {
            hasPermission = true;
        } else {
            try {
                // check if client has permission
                Authorizer.checkPermission(currentMap, request.getMessage().getHmacAuth().getIdentity(), 
                        Permission.SECURITY);

                hasPermission = true;
            } catch (KVSecurityException e) {
                commandBuilder.getStatusBuilder()
                .setCode(StatusCode.NOT_AUTHORIZED);
                commandBuilder.getStatusBuilder()
                .setStatusMessage(e.getMessage());
            }
        }
        return hasPermission;

    }

    public static synchronized Map<Long, ACL> handleSecurity(
            KineticMessage request, KineticMessage response,
            SimulatorEngine engine)
            throws KVStoreException, IOException {

        Command.Builder commandBuilder = (Command.Builder) response
                .getCommand();
        
        commandBuilder.getHeaderBuilder().setMessageType(MessageType.SECURITY_RESPONSE);

        List<ACL> aclList = request.getCommand().getBody().getSecurity()
                .getAclList();

        // Validate input
        for (ACL acl : aclList) {
            // add algorithm check
            if (!acl.hasHmacAlgorithm()
                    || !HMACAlgorithmUtil.isSupported(acl.getHmacAlgorithm())) {
                commandBuilder.getStatusBuilder().setCode(
                        StatusCode.NO_SUCH_HMAC_ALGORITHM);
                return engine.getAclMap();
            }

            for (ACL.Scope domain : acl.getScopeList()) {
                if (domain.hasOffset() && domain.getOffset() < 0) {
                    // Negative offsets are not allowed
                    commandBuilder.getStatusBuilder().setCode(
                            StatusCode.INVALID_REQUEST);
                    commandBuilder.getStatusBuilder().setStatusMessage(
                            "Offset in domain is less than 0.");
                    return engine.getAclMap();
                }

                List<Permission> roleOfList = domain.getPermissionList();
                if (null == roleOfList || roleOfList.isEmpty()) {
                    commandBuilder.getStatusBuilder().setCode(
                            StatusCode.INVALID_REQUEST);
                    commandBuilder.getStatusBuilder().setStatusMessage(
                            "No role set in acl");
                    return engine.getAclMap();
                }

                for (Permission role : roleOfList) {
                    if (!RoleUtil.isValid(role)) {
                        commandBuilder.getStatusBuilder().setCode(
                                StatusCode.INVALID_REQUEST);
                        commandBuilder.getStatusBuilder().setStatusMessage(
                                "Role is invalid in acl. Role is: "
                                        + role.toString());
                        return engine.getAclMap();
                    }
                }
            }
        }
        
        // get request security
        Security security = request.getCommand().getBody().getSecurity(); 
        
        // get current erase pin
        ByteString currentErasePin = engine.getSecurityPin().getErasePin();
        
        // need to compare if we need the old pin
        if ((currentErasePin != null) && (currentErasePin.isEmpty() == false)) {
            // get old erase pin
            ByteString oldErasePin = security.getOldErasePIN();
            
            // compare old with current
            if (currentErasePin.equals(oldErasePin) == false) {
                commandBuilder.getStatusBuilder().setCode(
                        StatusCode.NOT_AUTHORIZED);
                commandBuilder.getStatusBuilder().setStatusMessage(
                        "Invalid old erase pin: " + oldErasePin);
                
                return engine.getAclMap();
            } 
        }
        
        // get current lock pin
        ByteString currentLockPin = engine.getSecurityPin().getLockPin();
        
        // need to compare if we need the old pin
        if ((currentLockPin != null) && (currentLockPin.isEmpty() == false)) {
            // get old lock pin
            ByteString oldLockPin = security.getOldLockPIN();
            
            // compare old with current
            if (currentLockPin.equals(oldLockPin) == false) {
                commandBuilder.getStatusBuilder().setCode(
                        StatusCode.NOT_AUTHORIZED);
                commandBuilder.getStatusBuilder().setStatusMessage(
                        "Invalid old lock pin: " + oldLockPin);
                
                return engine.getAclMap();
            } 
        }
  
        // update acl map
        for (ACL acl : aclList) {
            engine.getAclMap().put(acl.getIdentity(), acl);
        }
        
        // set erase pin
        engine.getSecurityPin().setErasePin(security.getNewErasePIN());
              
        //set lock pin
        engine.getSecurityPin().setLockPin(security.getNewLockPIN());
        
        SecurityHandler.persistAcl(request.getCommand().getBody().getSecurity()
                .toByteArray(), engine.getKineticHome());
        
        commandBuilder.getStatusBuilder().setCode(StatusCode.SUCCESS);

        return engine.getAclMap();
    }

    private static void persistAcl(byte[] contents, String kineticHome)
            throws IOException {
        String aclPersistFilePath = kineticHome + File.separator + ".acl";
        String aclPersistBakFilePath = aclPersistFilePath + ".bak";
        // delete backup file
        File aclBakFile = new File(aclPersistBakFilePath);
        if (aclBakFile.exists()) {
            aclBakFile.delete();
        }

        // backup file
        File aclFile = new File(aclPersistFilePath);
        aclFile.renameTo(aclBakFile);

        // save new file
        aclFile = new File(aclPersistFilePath);
        FileOutputStream out = new FileOutputStream(aclFile);
        out.write(contents);
        out.close();
    }

    public static void loadACL(SimulatorEngine engine) throws IOException, KineticException {
        
        String aclPersistFilePath = engine.getKineticHome() + File.separator + ".acl";

        File aclFile = new File(aclPersistFilePath);
        
        Map<Long, ACL> aclmap = new HashMap<Long, ACL>();
        
        if (aclFile.exists()) {
            Long fileLength = aclFile.length();
            if (fileLength != 0) {
                byte[] fileContent = new byte[fileLength.intValue()];
                FileInputStream in = new FileInputStream(aclFile);
                in.read(fileContent);
                in.close();
                Security security = Security.parseFrom(fileContent);
                List<ACL> aclList = security.getAclList();

                for (ACL acl : aclList) {
                    aclmap.put(acl.getIdentity(), acl);
                }
                
                // set erase pin in cache
                engine.getSecurityPin().setErasePin(security.getNewErasePIN());
                
                // set lock pin in cache
                engine.getSecurityPin().setLockPin(security.getNewLockPIN());
                
                // lock the device since it was lock enabled
                if (engine.getSecurityPin().getLockPin().isEmpty() == false) {
                    
                    engine.setDeviceLocked(true);
                    
                    logger.warning ("******* Device is locked ********");
                }
            }
        } 
        
        if (aclmap.size() == 0) {        
            // get default acl map
            aclmap = HmacStore.getAclMap();       
        } 
        
        // set to engine
        engine.setAclMap(aclmap);
        
        // set default hmac key map
        engine.setHmacKeyMap(HmacStore.getHmacKeyMap(aclmap));
        
    }
    
    /**
     * Reset security ACL and pins to default.
     * 
     * @param kineticHome
     * @param securityPin
     * @param aclmap
     * @param hmacKeyMap
     * @throws KineticException
     */
    public static void resetSecurity (SimulatorEngine engine) throws KineticException {
        
        String aclPersistFilePath = engine.getKineticHome() + File.separator + ".acl";

        File aclFile = new File(aclPersistFilePath);
        
        // delete security file
        boolean deleted = aclFile.delete();
        if (deleted) {
            logger.info("removed security data ....");
        }
        
        // clear erase pin
        engine.getSecurityPin().setErasePin(null);
        
        // clear lock pin
        engine.getSecurityPin().setLockPin(null);
        
        // clear acl map
        engine.getAclMap().clear();
        
        // clear key map
        engine.getHmacKeyMap().clear();
        
        Map <Long, ACL> aclmap = HmacStore.getAclMap();
        
        // set default ack map
        engine.setAclMap(aclmap);

        // set default key map
        engine.setHmacKeyMap(HmacStore.getHmacKeyMap(aclmap));
        
        logger.info("reset security data to its factory defaults ...");
    }
   
}
