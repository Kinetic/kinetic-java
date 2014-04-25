/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.admin.impl;

import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.Domain;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.HMACAlgorithmUtil;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Security;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Message.Setup;
import com.seagate.kinetic.proto.Kinetic.Message.Status;

/**
 * This class provides administrative API for a kinetic administrator to
 * configure a kinetic server/service/drive.
 *
 */
public class DefaultAdminClient implements KineticAdminClient {

    private ClientConfiguration clientConfig = null;

    private KineticClient kineticClient = null;

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
        this.clientConfig = config;

        kineticClient = KineticClientFactory.createInstance(clientConfig);
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

        Message.Builder request = (Builder) km.getMessage();

        // set request message type
        request.getCommandBuilder().getHeaderBuilder()
                .setMessageType(MessageType.SECURITY);

        // send security request to server.
        KineticMessage respond = this.kineticClient.request(km);

        // return respond message.

        return respond;
    }

    public Message configureSecurityPolicy(Message.Builder request)
            throws KineticException {

        KineticMessage km = new KineticMessage();
        km.setMessage(request);

        KineticMessage kmresp = this.configureSecurityPolicy(km);

        return (Message) kmresp.getMessage();
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

        Message.Builder request = (Builder) km.getMessage();

        request.getCommandBuilder().getHeaderBuilder()
                .setMessageType(MessageType.SETUP);

        KineticMessage response = this.kineticClient.request(km);
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

        Message.Builder request = (Builder) km.getMessage();

        request.getCommandBuilder().getHeaderBuilder()
                .setMessageType(MessageType.GETLOG);
        KineticMessage response = this.kineticClient.request(km);

        return response;
    }

    @Override
    public void close() throws KineticException {
        this.kineticClient.close();
    }

    @Override
    public void instantErase(byte[] pin) throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder request = (Builder) km.getMessage();

        Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
                .getSetupBuilder();

        if (pin != null && pin.length > 0) {
            setup.setPin(ByteString.copyFrom(pin));
        }

        setup.setInstantSecureErase(true);

        KineticMessage response = configureSetupPolicy(km);

        if (response.getMessage().getCommand().getHeader().getMessageType() != MessageType.SETUP_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (response.getMessage().getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + response.getMessage().getCommand().getStatus().getCode()
                    + ": "
                    + response.getMessage().getCommand().getStatus()
                            .getStatusMessage());
        }

        if (response.getMessage().getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + response.getMessage().getCommand().getStatus().getCode()
                    + ": "
                    + response.getMessage().getCommand().getStatus()
                            .getStatusMessage());
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

    @Override
    public void setSecurity(List<ACL> acls) throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder request = (Builder) km.getMessage();

        Security.Builder security = request.getCommandBuilder()
                .getBodyBuilder().getSecurityBuilder();

        validate(acls);

        for (ACL aclInfo : acls) {
            com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
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
                com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Scope.Builder scope = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Scope
                        .newBuilder();
                scope.setOffset(domainInfo.getOffset());
                scope.setValue(ByteString.copyFromUtf8(domainInfo.getValue()));
                for (kinetic.admin.Role role : domainInfo.getRoles()) {
                    scope.addPermission(com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission
                            .valueOf(role.toString()));
                }

                acl.addScope(scope.build());
            }
            security.addAcl(acl.build());
        }

        KineticMessage response = configureSecurityPolicy(km);

        if (response.getMessage().getCommand().getHeader().getMessageType() != MessageType.SECURITY_RESPONSE) {
            throw new KineticException("received wrong message type.");
        }

        if (response.getMessage().getCommand().getStatus().getCode() == Status.StatusCode.NOT_AUTHORIZED) {

            throw new KineticException("Authorized Exception: "
                    + response.getMessage().getCommand().getStatus().getCode()
                    + ": "
                    + response.getMessage().getCommand().getStatus()
                            .getStatusMessage());
        }

        if (response.getMessage().getCommand().getStatus().getCode() == Status.StatusCode.NO_SUCH_HMAC_ALGORITHM) {

            throw new KineticException("Hmac algorithm Exception: "
                    + response.getMessage().getCommand().getStatus().getCode()
                    + ": "
                    + response.getMessage().getCommand().getStatus()
                            .getStatusMessage());
        }

        if (response.getMessage().getCommand().getStatus().getCode() != Status.StatusCode.SUCCESS) {
            throw new KineticException("Unknown Error: "
                    + response.getMessage().getCommand().getStatus().getCode()
                    + ": "
                    + response.getMessage().getCommand().getStatus()
                            .getStatusMessage());
        }

    }

    @Override
    public void setup(byte[] pin, byte[] setPin, long newClusterVersion,
            boolean secureErase) throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder request = (Builder) km.getMessage();

        Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
                .getSetupBuilder();

        if (pin != null && pin.length > 0) {
            setup.setPin(ByteString.copyFrom(pin));
        }

        if (setPin != null && setPin.length > 0) {
            setup.setSetPin(ByteString.copyFrom(setPin));
        }

        setup.setInstantSecureErase(secureErase);

        if (0 > newClusterVersion) {
            throw new KineticException(
                    "Parameter invalid: new cluster version less than 0.");
        }
        setup.setNewClusterVersion(newClusterVersion);

        KineticMessage kmresp = configureSetupPolicy(km);

        Message response = (Message) kmresp.getMessage();

        if (response.getCommand().getHeader().getMessageType() != MessageType.SETUP_RESPONSE) {
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
    public KineticLog getLog() throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        Message.Builder request = (Builder) km.getMessage();

        GetLog.Builder getLog = request.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog.addType(Type.CAPACITIES);
        getLog.addType(Type.CONFIGURATION);
        getLog.addType(Type.MESSAGES);
        getLog.addType(Type.STATISTICS);
        getLog.addType(Type.TEMPERATURES);
        getLog.addType(Type.UTILIZATIONS);

        KineticMessage kmresp = getLog(km);

        Message response = (Message) kmresp.getMessage();

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

        KineticLog kineticLOG = new DefaultKineticLog(response);
        return kineticLOG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KineticLog getLog(List<KineticLogType> listOfLogType)
            throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder request = (Builder) km.getMessage();

        GetLog.Builder getLog = request.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();

        for (KineticLogType getLogType : listOfLogType) {
            switch (getLogType) {
            case CAPACITIES:
                getLog.addType(Type.CAPACITIES);
                break;

            case TEMPERATURES:
                getLog.addType(Type.TEMPERATURES);
                break;

            case UTILIZATIONS:
                getLog.addType(Type.UTILIZATIONS);
                break;

            case CONFIGURATION:
                getLog.addType(Type.CONFIGURATION);
                break;

            case MESSAGES:
                getLog.addType(Type.MESSAGES);
                break;

            case STATISTICS:
                getLog.addType(Type.STATISTICS);
                break;

            default:
                ;
            }
        }

        KineticMessage kmresp = getLog(km);

        Message response = (Message) kmresp.getMessage();

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

        KineticLog kineticLog = new DefaultKineticLog(response);
        return kineticLog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firmwareDownload(byte[] pin, byte[] bytes)
            throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        Message.Builder request = (Builder) km.getMessage();

        Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
                .getSetupBuilder();

        if (null != pin && pin.length > 0) {
            setup.setPin(ByteString.copyFrom(pin));
        }

        setup.setFirmwareDownload(true);

        if (null != bytes && bytes.length > 0) {
            // request.setValue(ByteString.copyFrom(bytes));
            km.setValue(bytes);
        }

        request.getCommandBuilder().getHeaderBuilder()
                .setMessageType(MessageType.SETUP);

        KineticMessage kmresp = this.kineticClient.request(km);
        Message response = (Message) kmresp.getMessage();

        if (response.getCommand().getHeader().getMessageType() != MessageType.SETUP_RESPONSE) {
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
}
