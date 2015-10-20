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
package com.seagate.kinetic.admin.impl;

import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.Device;
import kinetic.admin.Domain;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.client.internal.p2p.DefaultKineticP2pClient;
import com.seagate.kinetic.common.lib.HMACAlgorithmUtil;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Command.Header;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.PinOperation.PinOpType;
import com.seagate.kinetic.proto.Kinetic.Command.Priority;
import com.seagate.kinetic.proto.Kinetic.Command.Range;
import com.seagate.kinetic.proto.Kinetic.Command.Security;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Command.Setup;
import com.seagate.kinetic.proto.Kinetic.Command.Status;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;
import com.seagate.kinetic.proto.Kinetic.Message.PINauth;

/**
 * This class provides administrative API for a kinetic administrator to
 * configure a kinetic server/service/drive.
 *
 */
public class DefaultAdminClient extends DefaultKineticP2pClient implements KineticAdminClient {

    /**
     * Construct a new instance of kinetic admin client.
     *
     * @param config
     *            client configuration, such as server host/port.
     * @throws KineticException
     *             if any internal error occurred.
     */
    public DefaultAdminClient(ClientConfiguration config)
            throws KineticException {
        super (config);
    }

    /**
     * Configure security policies for a kinetic drive.
     *
     * @param request
     *            the request message contains ACL list.
     * @return respond message from the service
     * @throws KineticException
     *             if any internal error occurred.
     */
    public KineticMessage configureSecurityPolicy(KineticMessage km)
            throws KineticException {

        Command.Builder commandBuilder = (Command.Builder) km.getCommand();
        
        // set request message type
        commandBuilder.getHeaderBuilder()
                .setMessageType(MessageType.SECURITY);

        // send security request to server.
        KineticMessage respond = request(km);

        // return respond message.

        return respond;
    }

    /**
     * Configure setup policies for a kinetic drive.
     *
     * @param request
     *            the request message contains setup content.
     * @return respond message.
     * @throws KineticException
     *             if any internal error occurred.
     */
    public KineticMessage configureSetupPolicy(KineticMessage km)
            throws KineticException {

        Command.Builder commandBuilder = (Command.Builder) km.getCommand();

        commandBuilder.getHeaderBuilder()
                .setMessageType(MessageType.SETUP);

        KineticMessage response = request(km);
        return response;
    }

    /**
     * Configure getLog policies for a kinetic drive.
     *
     * @param request
     *            the request message contains getLog content.
     * @return respond message.
     * @throws KineticException
     *             if any internal error occurred.
     */
    public KineticMessage getLog(KineticMessage km) throws KineticException {

        Command.Builder request = (Command.Builder) km.getCommand();

        request.getHeaderBuilder()
                .setMessageType(MessageType.GETLOG);
        
        KineticMessage response = request(km);

        return response;
    }

    @Override
    public void instantErase(byte[] pin) throws KineticException {
        
        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        
        Message.Builder mb = (Message.Builder) km.getMessage();
        mb.setAuthType(AuthType.PINAUTH);
        
        if (pin != null) {
            mb.setPinAuth(PINauth.newBuilder().setPin(ByteString.copyFrom(pin)));
        }
        
        Command.Builder commandBuilder = (Command.Builder) km.getCommand();
        
        commandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.PINOP);
        
        commandBuilder.getBodyBuilder().getPinOpBuilder().setPinOpType(PinOpType.ERASE_PINOP);
        
        KineticMessage response = request(km);
        
        if (response.getCommand().getStatus().getCode() != StatusCode.SUCCESS) {
            
            KineticException ke = new KineticException ("erase db failed.");
            ke.setRequestMessage(km);
            ke.setResponseMessage(response);
            
            throw ke;
        }
    }
    
    

    private void validate(List<ACL> acls) throws KineticException {
        if (null == acls || acls.isEmpty() || 0 == acls.size()) {
            throw new KineticException(
                    "Paramter Exception: acl list is null or empty!");
        }

        for (ACL acl : acls) {
            if (null == acl.getKey()) {
                throw new KineticException(
                        "Paramter Exception: key can't be null.");
            }

            if (0 > acl.getUserId()) {
                throw new KineticException(
                        "Paramter Exception: userid can't be less than 0.");
            }

            if (null != acl.getAlgorithm()) {
                if (!HMACAlgorithmUtil.isSupported(acl.getAlgorithm())) {
                    throw new KineticException(
                            "Parameter Exception: this algorithm is not supported.");
                }
            }

            if (null == acl.getDomains() || acl.getDomains().isEmpty()) {
                throw new KineticException(
                        "Paramter Exception: scope can't be null or empty.");
            }

            for (Domain domainInfo : acl.getDomains()) {
                if (domainInfo.getOffset() < 0) {
                    throw new KineticException(
                            "Paramter Exception: offset can't be less than 0.");
                }

                if (null == domainInfo.getRoles()
                        || domainInfo.getRoles().isEmpty()) {
                    throw new KineticException(
                            "Paramter Exception: role list can't be null or empty.");
                }
            }
        }
    }

    
    public void setSecurity(List<ACL> acls, byte[] oldLockPin,
           byte[] newLockPin, byte[] oldErasePin, byte[] newErasePin) throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) km.getCommand();
        
        Security.Builder security = commandBuilder
                .getBodyBuilder().getSecurityBuilder();

        validate(acls);

        for (ACL aclInfo : acls) {
            com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                    .newBuilder();

            if (aclInfo.getAlgorithm() != null) {
                acl.setHmacAlgorithm(HMACAlgorithm.valueOf(aclInfo
                        .getAlgorithm().toString()));
            } else {
                acl.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
            }

            acl.setIdentity(aclInfo.getUserId());
            acl.setKey(ByteString.copyFromUtf8(aclInfo.getKey()));
            for (Domain domainInfo : aclInfo.getDomains()) {
                com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder scope = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                        .newBuilder();
                scope.setOffset(domainInfo.getOffset());
                scope.setValue(ByteString.copyFromUtf8(domainInfo.getValue()));
                for (kinetic.admin.Role role : domainInfo.getRoles()) {
                    scope.addPermission(com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission
                            .valueOf(role.toString()));
                }

                acl.addScope(scope.build());
            }
            
            // set old lock pin
            if (oldLockPin != null) {
                security.setOldLockPIN(ByteString.copyFrom(oldLockPin));
            }
            
            // set new lock pin
            if (newLockPin != null) {
                security.setNewLockPIN(ByteString.copyFrom(newLockPin));
            }
            
            // set old erase pin
            if (oldErasePin != null) {
                security.setOldErasePIN (ByteString.copyFrom(oldErasePin));
            }
            
            // set new erase pin
            if (newErasePin != null) {
                security.setNewErasePIN (ByteString.copyFrom(newErasePin));
            }
            
            security.addAcl(acl.build());
        }

        KineticMessage response = configureSecurityPolicy(km);

        if (response.getCommand().getHeader().getMessageType() != MessageType.SECURITY_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (response.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + response.getCommand().getStatus().getCode()
                    + ": "
                    + response.getCommand().getStatus()
                            .getStatusMessage());
        }

        if (response.getCommand().getStatus().getCode() == Status.StatusCode.NO_SUCH_HMAC_ALGORITHM) {

            throw new KineticException("Hmac algorithm Exception: "
                    + response.getCommand().getStatus().getCode()
                    + ": "
                    + response.getCommand().getStatus()
                            .getStatusMessage());
        }

        if (response.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + response.getCommand().getStatus().getCode()
                    + ": "
                    + response.getCommand().getStatus()
                            .getStatusMessage());
        }

    }
     
    @Override
    public void setClusterVersion (long newClusterVersion) throws KineticException {
        
        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        
        Command.Builder commandBuilder = (Command.Builder) km.getCommand(); 

        Setup.Builder setup = commandBuilder.getBodyBuilder()
                .getSetupBuilder();
        
        if (0 > newClusterVersion) {
            throw new KineticException(
                    "Parameter invalid: new cluster version less than 0.");
        }
        
        setup.setNewClusterVersion(newClusterVersion);

        KineticMessage kmresp = configureSetupPolicy(km);

        if (kmresp.getCommand().getHeader().getMessageType() != MessageType.SETUP_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (kmresp.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }

        if (kmresp.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }

    }

    @Override
    public KineticLog getLog() throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
       
        Command.Builder commandBuilder = (Command.Builder) km.getCommand();
        
        GetLog.Builder getLog = commandBuilder.getBodyBuilder()
                .getGetLogBuilder();
        getLog.addTypes(Type.CAPACITIES);
        getLog.addTypes(Type.CONFIGURATION);
        getLog.addTypes(Type.MESSAGES);
        getLog.addTypes(Type.STATISTICS);
        getLog.addTypes(Type.TEMPERATURES);
        getLog.addTypes(Type.UTILIZATIONS);
        getLog.addTypes(Type.LIMITS);

        KineticMessage kmresp = getLog(km);

        if (kmresp.getCommand().getHeader().getMessageType() != MessageType.GETLOG_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (kmresp.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }

        if (kmresp.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }

        KineticLog kineticLOG = new DefaultKineticLog(kmresp);
        
        return kineticLOG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KineticLog getLog(List<KineticLogType> listOfLogType)
            throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) km.getCommand();

        GetLog.Builder getLog = commandBuilder.getBodyBuilder()
                .getGetLogBuilder();

        for (KineticLogType getLogType : listOfLogType) {
            switch (getLogType) {
            case CAPACITIES:
                getLog.addTypes(Type.CAPACITIES);
                break;

            case TEMPERATURES:
                getLog.addTypes(Type.TEMPERATURES);
                break;

            case UTILIZATIONS:
                getLog.addTypes(Type.UTILIZATIONS);
                break;

            case CONFIGURATION:
                getLog.addTypes(Type.CONFIGURATION);
                break;

            case MESSAGES:
                getLog.addTypes(Type.MESSAGES);
                break;

            case STATISTICS:
                getLog.addTypes(Type.STATISTICS);
                break;
                
            case LIMITS:
                getLog.addTypes(Type.LIMITS);
                break;
                
            case DEVICE:
                throw new java.lang.UnsupportedOperationException(
                        "Please use #getVendorSpecificDeviceLog() to get vendor specific log.");

            default:
                ;
            }
        }

        KineticMessage kmresp = getLog(km);

        checkGetLogResponse(kmresp);

        KineticLog kineticLog = new DefaultKineticLog(kmresp);
        return kineticLog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firmwareDownload(byte[] bytes)
            throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        
        Command.Builder commandBuilder = (Command.Builder) km.getCommand(); 
        
        Setup.Builder setup = commandBuilder.getBodyBuilder()
                .getSetupBuilder();

        setup.setFirmwareDownload(true);

        if (null != bytes && bytes.length > 0) {
            // request.setValue(ByteString.copyFrom(bytes));
            km.setValue(bytes);
        }

        commandBuilder.getHeaderBuilder()
                .setMessageType(MessageType.SETUP);

        KineticMessage kmresp = request(km);

        if (kmresp.getCommand().getHeader().getMessageType() != MessageType.SETUP_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (kmresp.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }

        if (kmresp.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }
    }

    @Override
    public Device getVendorSpecificDeviceLog(byte[] name)
            throws KineticException {
        
        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) km.getCommand(); 
        
        GetLog.Builder getLog = commandBuilder.getBodyBuilder()
                .getGetLogBuilder();
        
        // add DEVICE type
        getLog.addTypes(Type.DEVICE);
        
        // set vendor specific log name
        getLog.getDeviceBuilder().setName(ByteString.copyFrom(name));
        
        // send getLog/DEVICE message
        KineticMessage kmresp = getLog(km);
        
        // sanity check response 
        checkGetLogResponse(kmresp);
        
        // get vendor specific getLog/DEVICE name/value
        byte[] value = kmresp.getValue();
        
        
        Device device = new Device();
        
        device.setName (name);
        device.setValue(value);
        
        return device;
    }
    
    /**
     * Sanity check getLog response message.
     * 
     * @param response
     * @throws KineticException
     */
    private static void checkGetLogResponse (KineticMessage response) throws KineticException {
        
        if (response.getCommand().getHeader().getMessageType() != MessageType.GETLOG_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (response.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + response.getCommand().getStatus().getCode() + ": "
                    + response.getCommand().getStatus().getStatusMessage());
        }

        if (response.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + response.getCommand().getStatus().getCode() + ": "
                    + response.getCommand().getStatus().getStatusMessage());
        }
    }

    @Override
    public void secureErase(byte[] pin) throws KineticException {
        this.instantErase(pin);
    }

    @Override
    public void lockDevice(byte[] pin) throws KineticException {
        
        if (pin == null || pin.length == 0) {
            throw new KineticException ("Pin mut not be null or empty");
        }
        
        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        
        Message.Builder mb = (Message.Builder) km.getMessage();
        mb.setAuthType(AuthType.PINAUTH);
        
        mb.setPinAuth(PINauth.newBuilder().setPin(ByteString.copyFrom(pin)));
        
        Command.Builder commandBuilder = (Command.Builder) km.getCommand();
        
        commandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.PINOP);
        
        commandBuilder.getBodyBuilder().getPinOpBuilder().setPinOpType(PinOpType.LOCK_PINOP);
        
        KineticMessage response = request(km);
        
        if (response.getCommand().getStatus().getCode() != StatusCode.SUCCESS) {
            
            KineticException ke = new KineticException ("Pin op lock device failed.");
            ke.setRequestMessage(km);
            ke.setResponseMessage(response);
            
            throw ke;
        }
        
    }

    @Override
    public void unLockDevice(byte[] pin) throws KineticException {
        
        if (pin == null || pin.length == 0) {
            throw new KineticException ("Pin mut not be null or empty");
        }
        
        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        
        Message.Builder mb = (Message.Builder) km.getMessage();
        mb.setAuthType(AuthType.PINAUTH);
        
        mb.setPinAuth(PINauth.newBuilder().setPin(ByteString.copyFrom(pin)));
        
        Command.Builder commandBuilder = (Command.Builder) km.getCommand();
        
        commandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.PINOP);
        
        commandBuilder.getBodyBuilder().getPinOpBuilder().setPinOpType(PinOpType.UNLOCK_PINOP);
        
        KineticMessage response = request(km);
        
        if (response.getCommand().getStatus().getCode() != StatusCode.SUCCESS) {
            
            KineticException ke = new KineticException ("Pin op lock device failed.");
            ke.setRequestMessage(km);
            ke.setResponseMessage(response);
            
            throw ke;
        }
        
    }

    @Override
    public void setAcl(List<ACL> acls) throws KineticException {
        this.setSecurity(acls, null, null, null, null);
    }
    
    

    @Override
    public void setLockPin(byte[] oldLockPin, byte[] newLockPin)
            throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) km.getCommand();

        Security.Builder security = commandBuilder.getBodyBuilder()
                .getSecurityBuilder();

        // set old lock pin
        if (oldLockPin != null) {
            security.setOldLockPIN(ByteString.copyFrom(oldLockPin));
        }

        // set new lock pin
        if (newLockPin != null) {
            security.setNewLockPIN(ByteString.copyFrom(newLockPin));
        }

        KineticMessage response = configureSecurityPolicy(km);

        if (response.getCommand().getHeader().getMessageType() != MessageType.SECURITY_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (response.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + response.getCommand().getStatus().getCode() + ": "
                    + response.getCommand().getStatus().getStatusMessage());
        }

        if (response.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + response.getCommand().getStatus().getCode() + ": "
                    + response.getCommand().getStatus().getStatusMessage());
        }

    }

    @Override
    public void setErasePin(byte[] oldErasePin, byte[] newErasePin)
            throws KineticException {
        
        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) km.getCommand();

        Security.Builder security = commandBuilder.getBodyBuilder()
                .getSecurityBuilder();

        // set old erase pin
        if (oldErasePin != null) {
            security.setOldErasePIN (ByteString.copyFrom(oldErasePin));
        }
        
        // set new erase pin
        if (newErasePin != null) {
            security.setNewErasePIN (ByteString.copyFrom(newErasePin));
        }

        KineticMessage response = configureSecurityPolicy(km);

        if (response.getCommand().getHeader().getMessageType() != MessageType.SECURITY_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (response.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + response.getCommand().getStatus().getCode() + ": "
                    + response.getCommand().getStatus().getStatusMessage());
        }

        if (response.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + response.getCommand().getStatus().getCode() + ": "
                    + response.getCommand().getStatus().getStatusMessage());
        }
        
    }


    @Override
    public KineticMessage mediaScan(Range range, Priority priority)
            throws KineticException {
        
        // create request message
        KineticMessage kmreq = MessageFactory.createKineticMessageWithBuilder();
        
        Command.Builder commandBuilder = (Command.Builder) kmreq.getCommand(); 
        
        Header.Builder header = commandBuilder.getHeaderBuilder();
        
        // set message type
        header.setMessageType(MessageType.MEDIASCAN);

        // set priority
        header.setPriority(priority);
        
        // set range
        commandBuilder.getBodyBuilder().setRange(range);
        
        KineticMessage kmresp = request (kmreq);

        if (kmresp.getCommand().getHeader().getMessageType() != MessageType.MEDIASCAN_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (kmresp.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }

        if (kmresp.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }
        
        return kmresp;
    }

    @Override
    public KineticMessage mediaOptimize(Range range, Priority priority)
            throws KineticException {
        
        // create request message
        KineticMessage kmreq = MessageFactory.createKineticMessageWithBuilder();
        
        Command.Builder commandBuilder = (Command.Builder) kmreq.getCommand(); 
        
        Header.Builder header = commandBuilder.getHeaderBuilder();
        
        // set message type
        header.setMessageType(MessageType.MEDIAOPTIMIZE);

        // set priority
        header.setPriority(priority);
        
        // set range
        commandBuilder.getBodyBuilder().setRange(range);
        
        KineticMessage kmresp = request (kmreq);

        if (kmresp.getCommand().getHeader().getMessageType() != MessageType.MEDIAOPTIMIZE_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (kmresp.getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }

        if (kmresp.getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + kmresp.getCommand().getStatus().getCode() + ": "
                    + kmresp.getCommand().getStatus().getStatusMessage());
        }
        
        return kmresp;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public void firmwareDownload(byte[] pin, byte[] bytes)
            throws KineticException {
        this.firmwareDownload(bytes);
    }
    
    
    
}
