package com.seagate.kinetic;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2PClientFactory;
import kinetic.client.p2p.KineticP2pClient;

import org.junit.After;
import org.junit.Before;

import com.google.protobuf.ByteString;
import com.jcraft.jsch.JSchException;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.proto.Kinetic.Message;

/**
 * Kinetic client integration test case.
 * <p>
 * The methods are used before and after every kinetic client test case.
 * <p>
 *
 *
 */
public class IntegrationTestCase {

    // This will be assigned by the KineticTestRunner
    public KineticTestRunner.TestClientConfigConfigurator testClientConfigurator;

    private KineticP2pClient kineticClient;
    private DefaultAdminClient adminClient;
    private AbstractIntegrationTestTarget testTarget;

    // private KineticTestSimulator kineticTestServer;

    /**
     * Initialize a test server and a Kinetic client.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     * @throws IOException
     *             if any IO error occurred
     * @throws InterruptedException
     *             if any Interrupt error occurred
     */
    @Before
    public void startTestServer() throws InterruptedException,
    KineticException, IOException, JSchException, ExecutionException {
        testTarget = IntegrationTestTargetFactory.createTestTarget(true);
        kineticClient = KineticP2PClientFactory
                .createP2pClient(getClientConfig());
        adminClient = new DefaultAdminClient(getClientConfig());
    }

    /**
     * Stop a test server and a Kinetic client.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     * @throws IOException
     *             if any IO error occurred.
     */
    @After
    public void stopTestServer() throws Exception {
        kineticClient.close();
        adminClient.close();
        testTarget.shutdown();
    }

    /**
     * Get a Kinetic client.
     * <p>
     */
    protected KineticP2pClient getClient() {
        return kineticClient;
    }

    protected DefaultAdminClient getAdminClient() {
        return adminClient;
    }

    /**
     * Get a Kinetic client configuration with default setting.
     * <p>
     */
    protected ClientConfiguration getClientConfig() {
        ClientConfiguration clientConfiguration = testTarget.getClientConfig();
        // Once all subclasses of IntegrationTestCase use the KineticTestRunner
        // we can
        // assume that testClientFactory will exist. Until then we have to
        // handle the case
        // where it's null
        if (testClientConfigurator != null) {
            testClientConfigurator.modifyClientConfig(clientConfiguration);
        }
        return clientConfiguration;
    }

    /**
     * Get a Kinetic client configuration with setting userId and key flexible.
     * <p>
     */
    protected ClientConfiguration getClientConfig(int userId, String key) {
        ClientConfiguration clientConfiguration = getClientConfig();
        clientConfiguration.setUserId(userId);
        clientConfiguration.setKey(key);
        return clientConfiguration;
    }

    /**
     * Get a Kinetic client configuration with setting cluster version flexible.
     * <p>
     */
    protected ClientConfiguration getClientConfig(long clusterVersion) {
        ClientConfiguration clientConfiguration = getClientConfig();
        clientConfiguration.setClusterVersion(clusterVersion);
        return clientConfiguration;
    }

    /**
     * Restart the server and the Kinetic client.
     * <p>
     */
    protected void restartServer() throws Exception {
        kineticClient.close();
        adminClient.close();
        testTarget.shutdown();

        testTarget = IntegrationTestTargetFactory.createTestTarget(false);
        kineticClient = KineticP2PClientFactory
                .createP2pClient(getClientConfig());
        adminClient = new DefaultAdminClient(getClientConfig());
    }

    /**
     * Test case begin log info.
     * <p>
     */
    protected String testBeginInfo() {
        String className = Thread.currentThread().getStackTrace()[2]
                .getClassName();
        String methodName = Thread.currentThread().getStackTrace()[2]
                .getMethodName();
        return (className + "#" + methodName + " test starting...");
    }

    /**
     * Test case end log info.
     * <p>
     */
    protected String testEndInfo() {
        //		String className = Thread.currentThread().getStackTrace()[2]
        //				.getClassName();
        //		String methodName = Thread.currentThread().getStackTrace()[2]
        //				.getMethodName();
        //		return (className + "#" + methodName + " test finished and passed.");
        return ("status=success");
    }


    /**
     * Useful for writing tests which exercise permissions corner cases.
     * Creates a new client and adds the given roles to the client's ACL
     *
     * @param clientId
     * @param clientKeyString
     * @param roles
     * @throws KineticException
     */
    public void createClientAclWithRoles(int clientId, String clientKeyString,
            List<Kinetic.Message.Security.ACL.Permission> roles)
            throws KineticException {

        Kinetic.Message.Security.ACL.Scope.Builder domain = Kinetic.Message.Security.ACL.Scope
                .newBuilder();
        for (Kinetic.Message.Security.ACL.Permission role : roles) {
            domain.addPermission(role);
        }

        createClientAclWithDomains(clientId, clientKeyString, Collections.singletonList(domain.build()));
    }

    /**
     * Useful for writing tests which exercise permissions corner cases.
     * Creates a new client and adds the given domains to the client's ACL
     *
     * @param clientId
     * @param clientKeyString
     * @param domains
     * @throws KineticException
     */
    public void createClientAclWithDomains(int clientId,
            String clientKeyString,
            List<Kinetic.Message.Security.ACL.Scope> domains)
            throws KineticException {
        Kinetic.Message.Builder request = Kinetic.Message.newBuilder();

        Kinetic.Message.Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
        header.setMessageType(Kinetic.Message.MessageType.SECURITY);

        Kinetic.Message.Security.Builder security = request.getCommandBuilder()
                .getBodyBuilder().getSecurityBuilder();

        Kinetic.Message.Security.ACL.Builder acl = Kinetic.Message.Security.ACL.newBuilder();
        acl.setIdentity(clientId);
        acl.setKey(ByteString.copyFromUtf8(clientKeyString));
        acl.setHmacAlgorithm(Kinetic.Message.Security.ACL.HMACAlgorithm.HmacSHA1);

        for (Kinetic.Message.Security.ACL.Scope domain : domains) {
            acl.addScope(domain);
        }
        security.addAcl(acl);

        KineticMessage km = new KineticMessage();
        km.setMessage(request);

        Kinetic.Message response = (Message) getClient().request(km)
                .getMessage();

        // Ensure setup succeeded, or else fail the calling test.
        assertEquals(Kinetic.Message.Status.StatusCode.SUCCESS, response.getCommand().getStatus().getCode());
    }

    /**
     * Utility method to reduce duplicated test code. Creates a new entry for the given key/value pair, and executes
     * a PUT using the given client.
     *
     * @param key The string representation of the key to put
     * @param value The string representation of the value to put
     * @param client The client used to put
     * @return The entry created from the key/value pair
     * @throws kinetic.client.KineticException
     */
    protected Entry buildAndPutEntry(String key, String value, KineticClient client) throws KineticException {
        Entry entry = new Entry(toByteArray(key), toByteArray(value));
        client.put(entry, null);
        return entry;
    }
}
