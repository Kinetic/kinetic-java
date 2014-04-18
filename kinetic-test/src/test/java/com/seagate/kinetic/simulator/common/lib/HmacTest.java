package com.seagate.kinetic.simulator.common.lib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.Key;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.spec.SecretKeySpec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.Hmac.HmacException;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;

/**
 *
 * Hmac test verify calculate result
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
public class HmacTest {
    private final int writeThreads = 100;
    private final int writesEachThread = 1;
    private final int totalLoopCount = 100;

    private static final String DEMO_KEY = "asdfasdf";
    private static final String TEST_KEY = "qwerqwer";

    Key key = new SecretKeySpec(
            ByteString.copyFromUtf8(DEMO_KEY).toByteArray(), "HmacSHA1");
    Key key1 = new SecretKeySpec(ByteString.copyFromUtf8(TEST_KEY)
            .toByteArray(), "HmacSHA1");

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCalc() throws HmacException {
        Message.Builder msg1 = Message.newBuilder();
        Message.Builder msg2 = Message.newBuilder();

        Message.Header.Builder header = Message.Header.newBuilder();
        Message.Body.Builder body = Message.Body.newBuilder();
        Message.Status.Builder status = Message.Status.newBuilder();
        Message.KeyValue.Builder kv = Message.KeyValue.newBuilder();
        ByteString value = ByteString.copyFrom("123".getBytes());

        // set header
        header.setIdentity(1);
        header.setClusterVersion(1234);
        header.setConnectionID(1111);
        header.setSequence(1);

        // set body
        kv.setKey(ByteString.copyFrom("abc".getBytes()));
        body.setKeyValue(kv);

        // set status
        status.setCode(StatusCode.SUCCESS);
        status.setStatusMessage("message");

        // assemble the message
        msg1.getCommandBuilder().setHeader(header);
        msg1.getCommandBuilder().setBody(body);

        KineticMessage km1 = new KineticMessage();
        km1.setMessage(msg1);
        km1.setValue(value.toByteArray());
        // msg1.setValue(value);
        msg1.getCommandBuilder().setStatus(status);

        msg2.getCommandBuilder().setHeader(header);
        msg2.getCommandBuilder().setBody(body);
        // msg2.setValue(value);
        msg2.getCommandBuilder().setStatus(status);

        KineticMessage km2 = new KineticMessage();
        km2.setMessage(msg2);
        km2.setValue(value.toByteArray());

        ByteString hmac1 = null;
        ByteString hmac2 = null;

        // calculate the same message and make sure the result equal
        hmac1 = Hmac.calc(km1, key);
        hmac2 = Hmac.calc(km2, key);
        // print(hmac1.toByteArray());
        assertTrue(hmac1.equals(hmac2));

        // modify the header.User and then calculate again
        header.setIdentity(2);
        msg2.getCommandBuilder().setHeader(header);
        hmac2 = Hmac.calc(km2, key1);
        // print(hmac2.toByteArray());
        assertFalse(hmac1.equals(hmac2));

        // modify the header.ClusterVersion and then calculate again
        header.setIdentity(1);
        header.setClusterVersion(4321);
        msg2.getCommandBuilder().setHeader(header);
        hmac2 = Hmac.calc(km2, key);
        assertFalse(hmac1.equals(hmac2));

        // modify the header.ConnectionID and then calculate again
        header.setClusterVersion(1234);
        header.setConnectionID(2222);
        msg2.getCommandBuilder().setHeader(header);
        hmac2 = Hmac.calc(km2, key);
        assertFalse(hmac1.equals(hmac2));

        // modify the header.ConnectionID and then calculate again
        header.setConnectionID(1111);
        header.setSequence(2);
        msg2.getCommandBuilder().setHeader(header);
        hmac2 = Hmac.calc(km2, key);
        assertFalse(hmac1.equals(hmac2));

        // modify the body.KeyValue and then calculate again
        header.setSequence(1);
        msg2.getCommandBuilder().setHeader(header);

        kv.setKey(ByteString.copyFrom("def".getBytes()));
        body.setKeyValue(kv);
        msg2.getCommandBuilder().setBody(body);

        hmac2 = Hmac.calc(km2, key);
        assertFalse(hmac1.equals(hmac2));

        // modify the status.Code and then calculate again
        kv.setKey(ByteString.copyFrom("abc".getBytes()));
        body.setKeyValue(kv);
        msg2.getCommandBuilder().setBody(body);

        status.setCode(StatusCode.NOT_FOUND);
        msg2.getCommandBuilder().setStatus(status);

        hmac2 = Hmac.calc(km2, key);
        assertFalse(hmac1.equals(hmac2));

        // modify the status.Code and then calculate again
        status.setCode(StatusCode.SUCCESS);
        status.setStatusMessage("asdf");
        msg2.getCommandBuilder().setStatus(status);

        hmac2 = Hmac.calc(km2, key);
        assertFalse(hmac1.equals(hmac2));

        // modify the value and then calculate again
        status.setStatusMessage("message");
        msg2.getCommandBuilder().setStatus(status);

        ByteString value1 = ByteString.copyFrom("456".getBytes());
        km2.setValue(value1.toByteArray());
        hmac2 = Hmac.calc(km2, key);
        assertTrue(hmac1.equals(hmac2));

    }

    // @Test
    public void testCalculateValue() {
        // fail("Not yet implemented");
    }

    @Test
    public void testCheck() throws HmacException {
        Message.Builder msg1 = Message.newBuilder();
        Message.Header.Builder header1 = Message.Header.newBuilder();
        Message.Body.Builder body1 = Message.Body.newBuilder();
        Message.Status.Builder status1 = Message.Status.newBuilder();
        Message.KeyValue.Builder kv1 = Message.KeyValue.newBuilder();
        ByteString value1 = ByteString.copyFrom("123".getBytes());

        header1.setIdentity(1);
        header1.setClusterVersion(1234);
        header1.setConnectionID(1111);
        header1.setSequence(1);

        body1.setKeyValue(kv1.setKey(ByteString.copyFrom("abc".getBytes())));

        status1.setCode(StatusCode.SUCCESS);
        status1.setStatusMessage("message");

        msg1.getCommandBuilder().setHeader(header1);
        msg1.getCommandBuilder().setBody(body1);
        // msg1.setValue(value1);
        msg1.getCommandBuilder().setStatus(status1);

        KineticMessage km1 = new KineticMessage();
        km1.setMessage(msg1);
        km1.setValue(value1.toByteArray());

        ByteString hmac1 = null;

        hmac1 = Hmac.calc(km1, key);
        msg1.setHmac(hmac1);

        assertTrue(Hmac.check(km1, key));

        header1.setSequence(2);
        header1.setClusterVersion(12222);
        msg1.getCommandBuilder().setHeader(header1);
        assertFalse(Hmac.check(km1, key));

    }

    @Test
    public void concurrentHmacCalcTest() throws InterruptedException {
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
            Message.Builder msg = Message.newBuilder();

            Message.Header.Builder header = Message.Header.newBuilder();
            Message.Body.Builder body = Message.Body.newBuilder();
            Message.Status.Builder status = Message.Status.newBuilder();
            Message.KeyValue.Builder kv = Message.KeyValue.newBuilder();
            ByteString value = ByteString.copyFrom("123".getBytes());

            header.setIdentity(1);
            header.setClusterVersion(1234);
            header.setAckSequence(1111);
            header.setSequence(1);

            kv.setKey(ByteString.copyFrom("123".getBytes()));
            body.setKeyValue(kv);

            status.setCode(StatusCode.SUCCESS);
            status.setStatusMessage("message");

            msg.getCommandBuilder().setHeader(header);
            msg.getCommandBuilder().setBody(body);
            msg.getCommandBuilder().setStatus(status);
            // msg.setValue(value);

            KineticMessage km = new KineticMessage();
            km.setMessage(msg);
            km.setValue(value.toByteArray());

            for (int i = 0; i < writeCount; i++) {
                try {
                    header.setAckSequence((int) (10 * Math.random()));
                    msg.getCommandBuilder().setHeader(header);

                    Hmac.calc(km, key);

                } catch (HmacException e) {
                    fail("calc hmac failed: " + e.getMessage());
                }
            }

            latch.countDown();
        }
    }

    @Test
    public void loopHmacTest() throws HmacException {

        Message.Builder msg = Message.newBuilder();

        Message.Header.Builder header = Message.Header.newBuilder();
        Message.Body.Builder body = Message.Body.newBuilder();
        Message.Status.Builder status = Message.Status.newBuilder();
        Message.KeyValue.Builder kv = Message.KeyValue.newBuilder();
        ByteString value = ByteString.copyFrom("123".getBytes());

        header.setIdentity(1);
        header.setClusterVersion(1234);
        header.setAckSequence(1111);
        header.setSequence(1);

        kv.setKey(ByteString.copyFrom("123".getBytes()));
        body.setKeyValue(kv);

        status.setCode(StatusCode.SUCCESS);
        status.setStatusMessage("message");

        msg.getCommandBuilder().setHeader(header);
        msg.getCommandBuilder().setBody(body);
        msg.getCommandBuilder().setStatus(status);

        KineticMessage km = new KineticMessage();
        km.setMessage(msg);
        km.setValue(value.toByteArray());
        // msg.setValue(value);

        for (int i = 0; i < totalLoopCount; i++) {
            Hmac.calc(km, key);
        }
    }

    @Test
    public void hmacWithTagTest() throws HmacException {
        // msg
        Message.Builder msg = Message.newBuilder();

        Message.Header.Builder header = Message.Header.newBuilder();
        Message.Body.Builder body = Message.Body.newBuilder();
        Message.Status.Builder status = Message.Status.newBuilder();
        Message.KeyValue.Builder kv = Message.KeyValue.newBuilder();
        ByteString value = ByteString.copyFrom("123".getBytes());

        header.setIdentity(1);
        header.setClusterVersion(1234);
        header.setAckSequence(1111);
        header.setSequence(1);

        kv.setKey(ByteString.copyFrom("123".getBytes()));
        kv.setTag(ByteString.copyFrom("tag".getBytes()));
        body.setKeyValue(kv);

        status.setCode(StatusCode.SUCCESS);
        status.setStatusMessage("message");

        msg.getCommandBuilder().setHeader(header);
        msg.getCommandBuilder().setBody(body);
        msg.getCommandBuilder().setStatus(status);

        KineticMessage km = new KineticMessage();
        km.setMessage(msg);
        km.setValue(value.toByteArray());
        // msg.setValue(value);

        // msg1
        Message.Builder msg1 = Message.newBuilder();

        Message.Header.Builder header1 = Message.Header.newBuilder();
        Message.Body.Builder body1 = Message.Body.newBuilder();
        Message.Status.Builder status1 = Message.Status.newBuilder();
        Message.KeyValue.Builder kv1 = Message.KeyValue.newBuilder();
        ByteString value1 = ByteString.copyFrom("123".getBytes());

        header1.setIdentity(1);
        header1.setClusterVersion(1234);
        header1.setAckSequence(1111);
        header1.setSequence(1);

        kv1.setKey(ByteString.copyFrom("123".getBytes()));
        body1.setKeyValue(kv1);

        status1.setCode(StatusCode.SUCCESS);
        status1.setStatusMessage("message");

        msg1.getCommandBuilder().setHeader(header1);
        msg1.getCommandBuilder().setBody(body1);
        msg1.getCommandBuilder().setStatus(status1);
        // msg1.setValue(value1);
        KineticMessage km1 = new KineticMessage();
        km1.setMessage(msg1);
        km1.setValue(value1.toByteArray());

        ByteString hmacWithTag = Hmac.calc(km, key);
        ByteString hmacWithTag1 = Hmac.calc(km1, key);

        boolean flag = hmacWithTag.equals(hmacWithTag1);
        assertFalse(flag);
    }

    // // convert byte[] to String
    // private void print(byte[] bs) {
    // StringBuffer sb = new StringBuffer();
    // if (bs != null) {
    // for (byte b : bs) {
    // sb.append(b);
    // }
    // }
    // System.out.println(sb.toString());
    // }

}
