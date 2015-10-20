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

    public static synchronized void handleSecurity(
            KineticMessage request, KineticMessage response,
            SimulatorEngine engine)
            throws KVStoreException, IOException {

        Command.Builder commandBuilder = (Command.Builder) response
                .getCommand();
        
        commandBuilder.getHeaderBuilder().setMessageType(MessageType.SECURITY_RESPONSE);
        
        if (request.getIsSecureChannel() == false) {
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.INVALID_REQUEST);
            commandBuilder.getStatusBuilder().setStatusMessage(
                    "TLS channel is required for Security operation");

            return;
        }

        // check if only contains one security component change (ACL or ICE or Lock Pin)
        if (isSecurityMessageValid(request) == false) {
            
            //logger.warning("security not enforced at this time, should not set ACL/ICE/LOCL at the same time ...");
          
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.INVALID_REQUEST);
            commandBuilder
                    .getStatusBuilder()
                    .setStatusMessage(
                            "contains more than one security component change (ACL or ICE or Lock Pin");
            
            return;
        }

        // get ACL list
        List<ACL> requestAclList = request.getCommand().getBody().getSecurity()
                .getAclList();

        // Validate input
        for (ACL acl : requestAclList) {
            // add algorithm check
            if (!acl.hasHmacAlgorithm()
                    || !HMACAlgorithmUtil.isSupported(acl.getHmacAlgorithm())) {
                commandBuilder.getStatusBuilder().setCode(
                        StatusCode.NO_SUCH_HMAC_ALGORITHM);
                return;
            }

            for (ACL.Scope domain : acl.getScopeList()) {
                if (domain.hasOffset() && domain.getOffset() < 0) {
                    // Negative offsets are not allowed
                    commandBuilder.getStatusBuilder().setCode(
                            StatusCode.INVALID_REQUEST);
                    commandBuilder.getStatusBuilder().setStatusMessage(
                            "Offset in domain is less than 0.");
                    return;
                }

                List<Permission> roleOfList = domain.getPermissionList();
                if (null == roleOfList || roleOfList.isEmpty()) {
                    commandBuilder.getStatusBuilder().setCode(
                            StatusCode.INVALID_REQUEST);
                    commandBuilder.getStatusBuilder().setStatusMessage(
                            "No role set in acl");
                    return;
                }

                for (Permission role : roleOfList) {
                    if (!RoleUtil.isValid(role)) {
                        commandBuilder.getStatusBuilder().setCode(
                                StatusCode.INVALID_REQUEST);
                        commandBuilder.getStatusBuilder().setStatusMessage(
                                "Role is invalid in acl. Role is: "
                                        + role.toString());
                        return;
                    }
                }
            }
        }
        
        // construct new security builder to persist
        Security.Builder securityBuilder = Security.newBuilder();
        
        // request security
        Security requestSecurity = request.getCommand().getBody().getSecurity(); 
        
        // init pins
        initSecuirtyBuilderPins (securityBuilder, engine);
        
        // check if this is an ACL update
        if (requestAclList.isEmpty()) {
            // not an ACL update, add/preserve current acl list
            securityBuilder.addAllAcl(engine.getAclMap().values());
        } else {
            // this is an ACL update, add request ack list
            securityBuilder.addAllAcl(requestAclList);
            
            // update engine cache ACL list
            for (ACL acl : requestAclList) {
                engine.getAclMap().put(acl.getIdentity(), acl);
            }
        }
       
        // check if request has ICE pin
        if (requestSecurity.hasNewErasePIN() || requestSecurity.hasOldErasePIN()) {
            
         // get current erase pin
            ByteString currentErasePin = engine.getSecurityPin().getErasePin();
            
            // need to compare if we need the old pin
            if ((currentErasePin != null) && (currentErasePin.isEmpty() == false)) {
                // get old erase pin
                ByteString oldErasePin = requestSecurity.getOldErasePIN();
                
                // compare old with current
                if (currentErasePin.equals(oldErasePin) == false) {
                    commandBuilder.getStatusBuilder().setCode(
                            StatusCode.NOT_AUTHORIZED);
                    commandBuilder.getStatusBuilder().setStatusMessage(
                            "Invalid old erase pin: " + oldErasePin);
                    
                    return;
                } 
            }
            
            // set to builder to be persisted
            securityBuilder.setNewErasePIN(requestSecurity.getNewErasePIN());
            
            // set ICE pin to cache
            engine.getSecurityPin().setErasePin(requestSecurity.getNewErasePIN());  
        } 
        
        if (requestSecurity.hasNewLockPIN() || requestSecurity.hasOldLockPIN()) {
            // get current lock pin
            ByteString currentLockPin = engine.getSecurityPin().getLockPin();
            
            // need to compare if we need the old pin
            if ((currentLockPin != null) && (currentLockPin.isEmpty() == false)) {
                // get old lock pin
                ByteString oldLockPin = requestSecurity.getOldLockPIN();
                
                // compare old with current
                if (currentLockPin.equals(oldLockPin) == false) {
                    commandBuilder.getStatusBuilder().setCode(
                            StatusCode.NOT_AUTHORIZED);
                    commandBuilder.getStatusBuilder().setStatusMessage(
                            "Invalid old lock pin: " + oldLockPin);
                    
                    return;
                } 
            }
            
            // set new LOCK pin to builder to be persisted
            securityBuilder.setNewLockPIN (requestSecurity.getNewLockPIN());
            
            // set LOCK pin to cache
            engine.getSecurityPin().setLockPin(requestSecurity.getNewLockPIN());
        } 
        
        // persist to acl file
        SecurityHandler.persistAcl(securityBuilder.build().toByteArray(), engine.getKineticHome());
        
        // set status code
        commandBuilder.getStatusBuilder().setCode(StatusCode.SUCCESS);

        return;
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
        
        Security security = null;
        
        if (aclFile.exists()) {
            Long fileLength = aclFile.length();
            if (fileLength != 0) {
                byte[] fileContent = new byte[fileLength.intValue()];
                FileInputStream in = new FileInputStream(aclFile);
                in.read(fileContent);
                in.close();
                security = Security.parseFrom(fileContent);
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
    
    /**
     * Check if set Security contains only ACL, or ISE, or Lock pin change.
     * 
     *  Drive only support to change one at a time.
     * 
     * @param request
     * @return true if only one
     */
    private static boolean isSecurityMessageValid (KineticMessage request) {
        
        boolean isValid = false;
        
        int count = 0;
        
        Security security = request.getCommand().getBody().getSecurity();
        
        // if acl has value
        if (security.getAclCount() > 0) {
            count ++;
        }
        
        // if erase pin has value
        if (security.hasNewErasePIN() || security.hasOldErasePIN()) {
            count ++;
        }
        
        // if lock pin has value
        if (security.hasNewLockPIN() || security.hasOldLockPIN()) {
            count ++;
        }
        
        // we only allow to set one at a time
        if (count <= 1) {
            isValid = true;
        }
        
        return isValid;
    }
    
    private static void initSecuirtyBuilderPins (Security.Builder securityBuilder, SimulatorEngine engine) {
        
        //preserv current lock pin
        if (engine.getSecurityPin().getLockPin() != null) {
            securityBuilder.setOldLockPIN(engine.getSecurityPin().getLockPin());
            securityBuilder.setNewLockPIN(engine.getSecurityPin().getLockPin());
        }
        
        // preserv current ICE pin
        if (engine.getSecurityPin().getErasePin() != null) {
            securityBuilder.setOldErasePIN (engine.getSecurityPin().getErasePin());
            securityBuilder.setNewErasePIN(engine.getSecurityPin().getErasePin());
        }
    }
   
}
