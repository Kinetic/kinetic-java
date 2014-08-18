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
//import com.seagate.kinetic.proto.Kinetic.Message.Setup;

public class SecurityEraseTest extends IntegrationTestCase {

    String pin = "pin001";

    @After
    public void tearDown() throws Exception {
        cleanPin(pin, getAdminClient());
    }

    @Test
    public void testSecureErase_NoPinSetForDrive() throws KineticException {
//        byte[] pin = null;
//        getAdminClient().instantErase(pin);
    }

    @Test
    public void testSecureErase_WithWrongPinOFDrive() throws KineticException {
//        Message.Builder request = Message.newBuilder();
//        Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
//                .getSetupBuilder();
//        setup.setSetPin(ByteString.copyFromUtf8(pin));
//
//        KineticMessage km = new KineticMessage();
//        km.setMessage(request);
//        getAdminClient().configureSetupPolicy(km);
//
//        try {
//            getAdminClient().instantErase(null);
//            fail();
//        } catch (KineticException e) {
//        }
    }

    @Test
    public void testSecureErase_WithRightPinOFDrive() throws KineticException,
            UnsupportedEncodingException {
//        Message.Builder request = Message.newBuilder();
//        Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
//                .getSetupBuilder();
//        setup.setSetPin(ByteString.copyFromUtf8(pin));
//
//        KineticMessage km = new KineticMessage();
//        km.setMessage(request);
//
//        getAdminClient().configureSetupPolicy(km);
//
//        getAdminClient().instantErase(pin.getBytes("UTF-8"));
    }

    @Test
    public void testSecureErase_WithRightACL() throws KineticException,
            UnsupportedEncodingException {
//        DefaultAdminClient adminClient = new DefaultAdminClient(
//                getClientConfig(1, "asdfasdf"));
//
//        getAdminClient().instantErase(pin.getBytes("UTF-8"));
//        adminClient.close();
    }

    @Test
    public void testSecureErase_WithWrongClientID() throws KineticException,
            UnsupportedEncodingException {
//        DefaultAdminClient adminClient = new DefaultAdminClient(
//                getClientConfig(2, "asdfasdf"));
//
//        try {
//            adminClient.instantErase(pin.getBytes("UTF-8"));
//            fail();
//        } catch (KineticException e) {
//        } finally {
//            adminClient.close();
//        }
    }

    @Test
    public void testSecureErase_WithWrongClientKey() throws KineticException,
            UnsupportedEncodingException {
//        DefaultAdminClient adminClient = new DefaultAdminClient(
//                getClientConfig(1, "asdfasdf1"));
//
//        try {
//            adminClient.instantErase(pin.getBytes("UTF-8"));
//            fail();
//        } catch (KineticException e) {
//        } finally {
//            adminClient.close();
//        }
    }

}
