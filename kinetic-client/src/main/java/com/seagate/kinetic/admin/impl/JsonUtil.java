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

import java.util.ArrayList;
import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.Domain;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;

/**
 *
 * Change the format between Json and message
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
public class JsonUtil {
    /*
     * change Json to message security
     */
    public static final com.seagate.kinetic.proto.Kinetic.Message.Security parseSecurity(
            String content) {
        Gson gson = new Gson();
        Security security = gson.fromJson(content, Security.class);

        com.seagate.kinetic.proto.Kinetic.Message.Security.Builder retSecurityBuilder = com.seagate.kinetic.proto.Kinetic.Message.Security
                .newBuilder();
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder retAclBuilder = null;
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Scope.Builder retDomainBuilder = null;
        for (ACL acl : security.getAcl()) {
            retAclBuilder = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                    .newBuilder();
            retAclBuilder.setIdentity(acl.getUserId());
            if (null == acl.getAlgorithm() || acl.getAlgorithm().isEmpty()) {
                retAclBuilder.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
            } else {
                retAclBuilder.setHmacAlgorithm(HMACAlgorithm.valueOf(acl
                        .getAlgorithm()));
            }
            retAclBuilder.setKey(ByteString.copyFromUtf8(acl.getKey()));
            for (Domain domain : acl.getDomains()) {
                retDomainBuilder = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Scope
                        .newBuilder();
                retDomainBuilder.setOffset(domain.getOffset());
                retDomainBuilder.setValue(ByteString.copyFromUtf8(domain
                        .getValue()));
                for (kinetic.admin.Role role : domain.getRoles()) {
                    retDomainBuilder.addPermission(Permission.valueOf(role
                            .toString()));
                }
                retAclBuilder.addScope(retDomainBuilder.build());
            }
            retSecurityBuilder.addAcl(retAclBuilder.build());
        }

        return retSecurityBuilder.build();
    }

    /*
     * change Json to message setup
     */
    public static final com.seagate.kinetic.proto.Kinetic.Message.Setup parseSetup(
            String content) {
        Gson gson = new Gson();
        Setup setup = gson.fromJson(content, Setup.class);

        com.seagate.kinetic.proto.Kinetic.Message.Setup.Builder retSetupBuilder = com.seagate.kinetic.proto.Kinetic.Message.Setup
                .newBuilder();
        retSetupBuilder.setNewClusterVersion(setup.getNewClusterVersion());
        retSetupBuilder.setPin(ByteString.copyFromUtf8(setup.getPin()));
        retSetupBuilder.setSetPin(ByteString.copyFromUtf8(setup.getSetPin()));
        retSetupBuilder.setInstantSecureErase(setup.isInstantSecureErase());

        return retSetupBuilder.build();
    }

    /*
     * change Json to message getlog
     */
    public static final com.seagate.kinetic.proto.Kinetic.Message.GetLog parseGetLog(
            String content) {
        Gson gson = new Gson();
        GetLog getLog = gson.fromJson(content, GetLog.class);

        com.seagate.kinetic.proto.Kinetic.Message.GetLog.Builder retGetLogBuilder = com.seagate.kinetic.proto.Kinetic.Message.GetLog
                .newBuilder();
        for (Type type : getLog.getType()) {
            retGetLogBuilder.addType(type);
        }
        return retGetLogBuilder.build();
    }

    /*
     * change message setup to Json class
     */
    public static String toJson(
            com.seagate.kinetic.proto.Kinetic.Message.Setup setup) {
        Gson gson = new Gson();

        Setup mySetup = new Setup();
        mySetup.setInstantSecureErase(setup.getInstantSecureErase());
        mySetup.setNewClusterVersion(setup.getNewClusterVersion());
        mySetup.setPin(setup.getPin().toStringUtf8());
        mySetup.setSetPin(setup.getSetPin().toStringUtf8());

        return gson.toJson(mySetup, Setup.class);
    }

    /*
     * change message getlog to Json class
     */
    public static String toJson(
            com.seagate.kinetic.proto.Kinetic.Message.GetLog getLog) {
        Gson gson = new Gson();

        GetLog myGetLog = new GetLog();
        myGetLog.setType(getLog.getTypeList());

        return gson.toJson(myGetLog, GetLog.class);
    }

    /*
     * change message security to Json class
     */
    public static String toJson(
            com.seagate.kinetic.proto.Kinetic.Message.Security security) {
        Gson gson = new Gson();

        Security mySecurity = new Security();
        ACL myAcl = null;
        Domain myDomain = null;
        List<ACL> myAclList = new ArrayList<ACL>();
        List<Domain> myDomainList = null;
        for (com.seagate.kinetic.proto.Kinetic.Message.Security.ACL acl : security
                .getAclList()) {
            myAcl = new ACL();
            // myAcl.setAlgorithm(acl.getAlgorithmName());

            myAcl.setUserId(acl.getIdentity());
            myAcl.setKey(acl.getKey().toStringUtf8());

            myDomainList = new ArrayList<Domain>();
            List<kinetic.admin.Role> roleList = null;
            for (com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Scope domain : acl
                    .getScopeList()) {
                myDomain = new Domain();
                roleList = new ArrayList<kinetic.admin.Role>();
                myDomain.setOffset(domain.getOffset());
                myDomain.setValue(domain.getValue().toStringUtf8());
                // for (Role role : domain.getRoleList()) {
                for (Permission role : domain.getPermissionList()) {
                    roleList.add(kinetic.admin.Role.valueOf(role
                            .toString()));
                }
                myDomain.setRoles(roleList);
                myDomainList.add(myDomain);
            }
            myAcl.setDomains(myDomainList);

            myAclList.add(myAcl);
        }
        mySecurity.setAcl(myAclList);

        return gson.toJson(mySecurity, Security.class);
    }
}

class Security {
    private List<ACL> acl;

    public List<ACL> getAcl() {
        return acl;
    }

    public void setAcl(List<ACL> acl) {
        this.acl = acl;
    }
}

class GetLog {
    private List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type> type;

    public List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type> getType() {
        return type;
    }

    public void setType(
            List<com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type> type) {
        this.type = type;
    }
}

class Setup {
    private long newClusterVersion;
    private boolean instantSecureErase;
    private String setPin;
    private String pin;

    public long getNewClusterVersion() {
        return newClusterVersion;
    }

    public void setNewClusterVersion(long newClusterVersion) {
        this.newClusterVersion = newClusterVersion;
    }

    public boolean isInstantSecureErase() {
        return instantSecureErase;
    }

    public void setInstantSecureErase(boolean instantSecureErase) {
        this.instantSecureErase = instantSecureErase;
    }

    public String getSetPin() {
        return setPin;
    }

    public void setSetPin(String setPin) {
        this.setPin = setPin;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
