package com.seagate.kinetic.simulator.client.admin.impl;

import static com.seagate.kinetic.KineticTestHelpers.cleanPin;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import kinetic.client.KineticException;

import org.junit.After;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Setup;

public class SecurityEraseTest extends IntegrationTestCase {

    String pin = "pin001";

    @After
    public void tearDown() throws Exception {
        cleanPin(pin, getAdminClient());
    }

    @Test
    public void testSecureErase_NoPinSetForDrive() throws KineticException {
        byte[] pin = null;
        getAdminClient().instantErase(pin);
    }

    @Test
    public void testSecureErase_WithWrongPinOFDrive() throws KineticException {
        Message.Builder request = Message.newBuilder();
        Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
                .getSetupBuilder();
        setup.setSetPin(ByteString.copyFromUtf8(pin));

        KineticMessage km = new KineticMessage();
        km.setMessage(request);
        getAdminClient().configureSetupPolicy(km);

        try {
            getAdminClient().instantErase(null);
            fail();
        } catch (KineticException e) {
        }
    }

    @Test
    public void testSecureErase_WithRightPinOFDrive() throws KineticException,
            UnsupportedEncodingException {
        Message.Builder request = Message.newBuilder();
        Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
                .getSetupBuilder();
        setup.setSetPin(ByteString.copyFromUtf8(pin));

        KineticMessage km = new KineticMessage();
        km.setMessage(request);

        getAdminClient().configureSetupPolicy(km);

        getAdminClient().instantErase(pin.getBytes("UTF-8"));
    }

    @Test
    public void testSecureErase_WithRightACL() throws KineticException,
            UnsupportedEncodingException {
        DefaultAdminClient adminClient = new DefaultAdminClient(
                getClientConfig(1, "asdfasdf"));

        getAdminClient().instantErase(pin.getBytes("UTF-8"));
        adminClient.close();
    }

    @Test
    public void testSecureErase_WithWrongClientID() throws KineticException,
            UnsupportedEncodingException {
        DefaultAdminClient adminClient = new DefaultAdminClient(
                getClientConfig(2, "asdfasdf"));

        try {
            adminClient.instantErase(pin.getBytes("UTF-8"));
            fail();
        } catch (KineticException e) {
        } finally {
            adminClient.close();
        }
    }

    @Test
    public void testSecureErase_WithWrongClientKey() throws KineticException,
            UnsupportedEncodingException {
        DefaultAdminClient adminClient = new DefaultAdminClient(
                getClientConfig(1, "asdfasdf1"));

        try {
            adminClient.instantErase(pin.getBytes("UTF-8"));
            fail();
        } catch (KineticException e) {
        } finally {
            adminClient.close();
        }
    }

}
