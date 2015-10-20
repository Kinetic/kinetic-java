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
package com.seagate.kinetic.simulator.common.lib;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import java.security.Key;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.Hmac.HmacException;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.Body;
import com.seagate.kinetic.proto.Kinetic.Command.Header;
import com.seagate.kinetic.proto.Kinetic.Command.Status;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Message;

/**
 *
 * Hmac test verify calculate result
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
@Test(groups = {"simulator"})
public class HmacTest {
    private static final String DEMO_KEY = "asdfasdf";
    private static final String TEST_KEY = "qwerqwer";

    Key key = new SecretKeySpec(
            ByteString.copyFromUtf8(DEMO_KEY).toByteArray(), "HmacSHA1");
    Key key1 = new SecretKeySpec(ByteString.copyFromUtf8(TEST_KEY)
            .toByteArray(), "HmacSHA1");

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testCalcTag() throws HmacException {

        KineticMessage km1 = MessageFactory.createKineticMessageWithBuilder();
        KineticMessage km2 = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder msg1 = (Message.Builder) km1.getMessage();
        Message.Builder msg2 = (Message.Builder) km2.getMessage();

        Command.Builder commandBuilder1 = (Command.Builder) km1.getCommand();
        Command.Builder commandBuilder2 = (Command.Builder) km2.getCommand();

        Header.Builder header = commandBuilder1.getHeaderBuilder();

        Body.Builder body = commandBuilder1.getBodyBuilder();
        Status.Builder status = (Status.Builder) commandBuilder1
                .getStatusBuilder();

        Command.KeyValue.Builder kv = body.getKeyValueBuilder();
        ByteString value = ByteString.copyFrom("123".getBytes());
        ByteString value1 = ByteString.copyFrom("456".getBytes());

        msg1.getHmacAuthBuilder().setIdentity(1);

        // set header
        header.setClusterVersion(1234);
        header.setConnectionID(1111);
        header.setSequence(1);

        // set body
        kv.setKey(ByteString.copyFrom("abc".getBytes()));
        body.setKeyValue(kv);

        // set status
        status.setCode(Command.Status.StatusCode.SUCCESS);
        status.setStatusMessage("message");

        // assemble the message
        commandBuilder1.setHeader(header);
        commandBuilder1.setBody(body);
        commandBuilder1.setStatus(status);

        km1.setMessage(msg1);
        km1.setValue(value.toByteArray());
        km1.setCommand(commandBuilder1);

        commandBuilder2.setHeader(header);
        commandBuilder2.setBody(body);
        commandBuilder2.setStatus(status);

        km2.setMessage(msg2);
        km2.setValue(value.toByteArray());
        km2.setCommand(commandBuilder2);

        ByteString hmac1 = null;
        ByteString hmac2 = null;

        // calculate the same message and make sure the result equal
        hmac1 = Hmac.calcTag(km1, key);
        hmac2 = Hmac.calcTag(km2, key);
        assertTrue(Arrays.equals(hmac1.toByteArray(), hmac2.toByteArray()));

        // modify the value and then calculate again
        msg2.getHmacAuthBuilder().setIdentity(2);
        km2.setMessage(msg2);
        km2.setValue(value1.toByteArray());
        hmac2 = Hmac.calcTag(km2, key);
        assertFalse(Arrays.equals(hmac1.toByteArray(), hmac2.toByteArray()));
    }

    @Test
    public void testCalc() throws HmacException {
        byte[] bytes1 = "abc".getBytes();
        byte[] bytes2 = "cdf".getBytes();

        ByteString hmac1 = Hmac.calc(bytes1, key);
        ByteString hmac2 = Hmac.calc(bytes2, key);

        assertFalse(Arrays.equals(hmac1.toByteArray(), hmac2.toByteArray()));

        hmac1 = Hmac.calc(bytes1, key);
        hmac2 = Hmac.calc(bytes1, key1);

        if (!key.equals(key1))
            assertFalse(Arrays.equals(hmac1.toByteArray(), hmac2.toByteArray()));

        bytes2 = "abc".getBytes();
        hmac1 = Hmac.calc(bytes1, key);
        hmac2 = Hmac.calc(bytes2, key);

        assertTrue(Arrays.equals(hmac1.toByteArray(), hmac2.toByteArray()));

        hmac1 = Hmac.calc(bytes1, key1);
        hmac2 = Hmac.calc(bytes2, key1);
        assertTrue(Arrays.equals(hmac1.toByteArray(), hmac2.toByteArray()));
    }

    @Test
    public void testCheck() throws HmacException {

        KineticMessage km1 = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder msg1 = (Message.Builder) km1.getMessage();

        Command.Builder commandBuilder1 = (Command.Builder) km1.getCommand();

        Header.Builder header1 = commandBuilder1.getHeaderBuilder();

        Body.Builder body1 = commandBuilder1.getBodyBuilder();
        Status.Builder status1 = (Status.Builder) commandBuilder1
                .getStatusBuilder();

        Command.KeyValue.Builder kv1 = body1.getKeyValueBuilder();
        ByteString value1 = ByteString.copyFrom("123".getBytes());

        msg1.getHmacAuthBuilder().setIdentity(1);

        header1.setClusterVersion(1234);
        header1.setConnectionID(1111);
        header1.setSequence(1);

        body1.setKeyValue(kv1.setKey(ByteString.copyFrom("abc".getBytes())));

        status1.setCode(StatusCode.SUCCESS);
        status1.setStatusMessage("message");

        commandBuilder1.setHeader(header1);
        commandBuilder1.setBody(body1);
        commandBuilder1.setStatus(status1);
        // get command bytes for hmac calculation
        byte[] commandBytes = commandBuilder1.build().toByteString()
                .toByteArray();
        // set command bytes to message
        msg1.setCommandBytes(ByteString.copyFrom(commandBytes));

        km1.setValue(value1.toByteArray());
        km1.setCommand(commandBuilder1.build());
        km1.setMessage(msg1);

        byte[] bytes = km1.getMessage().getCommandBytes().toByteArray();
        ByteString expected = Hmac.calc(bytes, key);
        assertTrue(Hmac.check(bytes, key, expected));

        msg1.getHmacAuthBuilder().setHmac(expected);
        assertTrue(Hmac.check(km1, key));

        body1.setKeyValue(kv1.setKey(ByteString.copyFrom("def".getBytes())));
        commandBuilder1.setBody(body1);
        commandBytes = commandBuilder1.build().toByteString().toByteArray();
        msg1.setCommandBytes(ByteString.copyFrom(commandBytes));
        km1.setCommand(commandBuilder1.build());

        byte[] bytes1 = km1.getMessage().getCommandBytes().toByteArray();
        ByteString expected1 = Hmac.calc(bytes1, key);
        msg1.getHmacAuthBuilder().setHmac(expected1);
        assertTrue(Hmac.check(bytes1, key, expected1));
        assertTrue(Hmac.check(km1, key));

        assertFalse(Arrays.equals(expected.toByteArray(),
                expected1.toByteArray()));
    }

    @Test
    public void concurrentHmacCalcTest() throws InterruptedException {
        int writeThreads = 3;
        int writesEachThread = 5;
        ExecutorService pool = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(writeThreads);
        for (int i = 0; i < writeThreads; i++) {
            pool.execute(new WriteThread(writesEachThread, latch));
        }

        latch.await();
        pool.shutdown();

    }

    class WriteThread implements Runnable {
        private int writeCount = 0;
        private final CountDownLatch latch;

        public WriteThread(int writeCount, CountDownLatch latch) {
            this.writeCount = writeCount;
            this.latch = latch;
        }

        @Override
        public void run() {
            KineticMessage km1 = MessageFactory
                    .createKineticMessageWithBuilder();
            Message.Builder msg1 = (Message.Builder) km1.getMessage();
            Command.Builder commandBuilder1 = (Command.Builder) km1
                    .getCommand();
            Header.Builder header1 = commandBuilder1.getHeaderBuilder();
            Body.Builder body1 = commandBuilder1.getBodyBuilder();
            Status.Builder status1 = (Status.Builder) commandBuilder1
                    .getStatusBuilder();

            Command.KeyValue.Builder kv1 = body1.getKeyValueBuilder();
            ByteString value1 = ByteString.copyFrom("123".getBytes());

            msg1.getHmacAuthBuilder().setIdentity(1);

            header1.setClusterVersion(1234);
            header1.setConnectionID(1111);
            header1.setSequence(1);

            body1.setKeyValue(kv1.setKey(ByteString.copyFrom("abc".getBytes())));

            status1.setCode(StatusCode.SUCCESS);
            status1.setStatusMessage("message");

            commandBuilder1.setHeader(header1);
            commandBuilder1.setBody(body1);
            commandBuilder1.setStatus(status1);
            byte[] commandBytes = commandBuilder1.build().toByteString()
                    .toByteArray();

            // set command bytes to message
            msg1.setCommandBytes(ByteString.copyFrom(commandBytes));

            km1.setValue(value1.toByteArray());
            km1.setCommand(commandBuilder1.build());
            km1.setMessage(msg1);

            byte[] bytes1 = km1.getMessage().getCommandBytes().toByteArray();

            for (int i = 0; i < writeCount; i++) {
                try {
                    header1.setAckSequence((int) (10 * Math.random()));
                    commandBuilder1.setHeader(header1);

                    Hmac.calc(bytes1, key);

                } catch (HmacException e) {
                    Assert.fail("calc hmac failed: " + e.getMessage());
                }
            }

            latch.countDown();
        }
    }

    @Test
    public void loopHmacTest() {
        KineticMessage km1 = MessageFactory.createKineticMessageWithBuilder();
        Message.Builder msg1 = (Message.Builder) km1.getMessage();
        Command.Builder commandBuilder1 = (Command.Builder) km1.getCommand();
        Header.Builder header1 = commandBuilder1.getHeaderBuilder();
        Body.Builder body1 = commandBuilder1.getBodyBuilder();
        Status.Builder status1 = (Status.Builder) commandBuilder1
                .getStatusBuilder();

        Command.KeyValue.Builder kv1 = body1.getKeyValueBuilder();
        ByteString value1 = ByteString.copyFrom("123".getBytes());

        msg1.getHmacAuthBuilder().setIdentity(1);

        header1.setClusterVersion(1234);
        header1.setConnectionID(1111);
        header1.setSequence(1);

        body1.setKeyValue(kv1.setKey(ByteString.copyFrom("abc".getBytes())));

        status1.setCode(StatusCode.SUCCESS);
        status1.setStatusMessage("message");

        commandBuilder1.setHeader(header1);
        commandBuilder1.setBody(body1);
        commandBuilder1.setStatus(status1);
        byte[] commandBytes = commandBuilder1.build().toByteString()
                .toByteArray();

        // set command bytes to message
        msg1.setCommandBytes(ByteString.copyFrom(commandBytes));

        km1.setValue(value1.toByteArray());
        km1.setCommand(commandBuilder1.build());
        km1.setMessage(msg1);

        byte[] bytes1 = km1.getMessage().getCommandBytes().toByteArray();

        int totalLoopCount = 100;

        for (int i = 0; i < totalLoopCount; i++) {
            try {
                Hmac.calc(bytes1, key);
            } catch (HmacException e) {
                Assert.fail("Hamc calc throw exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void hmacWithTagTest() {
        KineticMessage km1 = MessageFactory.createKineticMessageWithBuilder();
        KineticMessage km2 = MessageFactory.createKineticMessageWithBuilder();

        Message.Builder msg1 = (Message.Builder) km1.getMessage();
        Command.Builder commandBuilder1 = (Command.Builder) km1.getCommand();
        Header.Builder header1 = commandBuilder1.getHeaderBuilder();
        Body.Builder body1 = commandBuilder1.getBodyBuilder();
        Status.Builder status1 = (Status.Builder) commandBuilder1
                .getStatusBuilder();

        Command.KeyValue.Builder kv1 = body1.getKeyValueBuilder();
        kv1.setKey(ByteString.copyFrom("123".getBytes()));
        kv1.setTag(ByteString.copyFrom("tag".getBytes()));
        ByteString value1 = ByteString.copyFrom("123".getBytes());
        ByteString value2 = ByteString.copyFrom("456".getBytes());
        
        msg1.getHmacAuthBuilder().setIdentity(1);

        header1.setClusterVersion(1234);
        header1.setConnectionID(1111);
        header1.setSequence(1);

        body1.setKeyValue(kv1);

        status1.setCode(StatusCode.SUCCESS);
        status1.setStatusMessage("message");

        commandBuilder1.setHeader(header1);
        commandBuilder1.setBody(body1);
        commandBuilder1.setStatus(status1);
        byte[] commandBytes = commandBuilder1.build().toByteString()
                .toByteArray();

        // set command bytes to message
        msg1.setCommandBytes(ByteString.copyFrom(commandBytes));

        km1.setValue(value1.toByteArray());
        km1.setCommand(commandBuilder1.build());
        km1.setMessage(msg1);

        byte[] bytes1 = km1.getMessage().getCommandBytes().toByteArray();

        km2.setValue(value2.toByteArray());
        km2.setCommand(commandBuilder1.build());
        km2.setMessage(msg1);

        byte[] bytes2 = km2.getMessage().getCommandBytes().toByteArray();

        ByteString hmacWithTag = null;
        ByteString hmacWithTag1 = null;
        try {
            hmacWithTag = Hmac.calc(bytes1, key);
        } catch (HmacException e) {
            Assert.fail("Hmac calc throw exception: " + e.getMessage());
        }

        try {
            hmacWithTag1 = Hmac.calc(bytes2, key);
        } catch (HmacException e) {
            Assert.fail("Hmac calc throw exception: " + e.getMessage());
        }

        assertTrue(Arrays.equals(hmacWithTag.toByteArray(),
                hmacWithTag1.toByteArray()));
    }
}
