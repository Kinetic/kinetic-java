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
package com.seagate.kinetic;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2PClientFactory;
import kinetic.client.p2p.KineticP2pClient;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import com.google.protobuf.ByteString;
import com.jcraft.jsch.JSchException;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.Header;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;

/**
 * Kinetic client integration test case.
 * <p>
 * The methods are used before and after every kinetic client test case.
 * <p>
 *
 *
 */
public class IntegrationTestCase {
    protected static final String NONNIO_NONSSL_CLIENT = "nonNio_nonSsl";
    protected static final String NIO_NONSSL_CLIENT = "nio_nonSsl";
    protected static final String NONNIO_SSL_CLIENT = "nonNio_ssl";

    protected Map<String, KineticP2pClient> kineticClients = new HashMap<String, KineticP2pClient>();
    protected Map<String, ClientConfiguration> kineticClientConfigutations = new HashMap<String, ClientConfiguration>();
    private KineticAdminClient adminClient;
    private AbstractIntegrationTestTarget testTarget;

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
    @BeforeClass(alwaysRun = true)
    public void startTestServer() throws InterruptedException,
            KineticException, IOException, JSchException, ExecutionException {
        createKineticClientConfigurations();
        testTarget = IntegrationTestTargetFactory.createTestTarget(true);
        kineticClients.clear();
        for (String clientName : kineticClientConfigutations.keySet()) {
            kineticClients.put(clientName, KineticP2PClientFactory
                    .createP2pClient(kineticClientConfigutations
                            .get(clientName)));
        }
        adminClient = KineticAdminClientFactory
                .createInstance(getAdminClientConfig());
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
    @AfterClass(alwaysRun = true)
    public void stopTestServer() throws Exception {
        closeKineticClients();
        if (null != adminClient) {
            adminClient.close();
        }
        testTarget.shutdown();
    }

    @BeforeSuite(alwaysRun = true)
    protected void securityEraseTarget() throws KineticException {
        if (Boolean.parseBoolean(System.getProperty("RUN_AGAINST_EXTERNAL"))) {
            AdminClientConfiguration acc = new AdminClientConfiguration();
            acc.setHost(System.getProperty("KINETIC_HOST", "127.0.0.1"));
            acc.setPort(Integer.parseInt(System.getProperty("KINETIC_SSL_PORT",
                    "8443")));

            KineticAdminClient kac = KineticAdminClientFactory
                    .createInstance(acc);
            String oldErasePin = System.getProperty("OLD_PIN", "");
            String newErasePin = System.getProperty("NEW_PIN", "123");

            kac.setErasePin(toByteArray(oldErasePin), toByteArray(newErasePin));
            kac.instantErase(toByteArray(newErasePin));

            kac.close();
        }
    }

    /**
     * Get a Kinetic client.
     * <p>
     */
    protected KineticP2pClient getClient(String clientName) {
        return kineticClients.get(clientName);
    }

    protected KineticAdminClient getAdminClient() {
        return adminClient;
    }

    /**
     * Get a Kinetic client configuration with default setting.
     * <p>
     */
    protected ClientConfiguration getClientConfig() {
        ClientConfiguration clientConfiguration = IntegrationTestTargetFactory
                .createDefaultClientConfig();
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
     * Get a Kinetic client configuration with default setting.
     * <p>
     */
    protected AdminClientConfiguration getAdminClientConfig() {
        AdminClientConfiguration adminClientConfiguration = testTarget
                .getAdminClientConfig();
        return adminClientConfiguration;
    }

    /**
     * Get a Kinetic client configuration with setting cluster version flexible.
     * <p>
     */
    protected AdminClientConfiguration getAdminClientConfig(long clusterVersion) {
        AdminClientConfiguration adminClientConfiguration = testTarget
                .getAdminClientConfig();
        adminClientConfiguration.setClusterVersion(clusterVersion);
        return adminClientConfiguration;
    }

    /**
     * Get a Kinetic admin client configuration with setting userId and key
     * flexible.
     * <p>
     */
    protected AdminClientConfiguration getAdminClientConfig(int userId,
            String key) {
        AdminClientConfiguration adminClientConfiguration = getAdminClientConfig();
        adminClientConfiguration.setUserId(userId);
        adminClientConfiguration.setKey(key);
        return adminClientConfiguration;
    }

    /**
     * Restart the server and the Kinetic client.
     * <p>
     */
    protected void restartServer() throws Exception {
        closeKineticClients();
        adminClient.close();
        testTarget.shutdown();

        testTarget = IntegrationTestTargetFactory.createTestTarget(false);
        kineticClients.clear();
        for (String clientName : kineticClientConfigutations.keySet()) {
            kineticClients.put(clientName, KineticP2PClientFactory
                    .createP2pClient(kineticClientConfigutations
                            .get(clientName)));
        }
        adminClient = KineticAdminClientFactory
                .createInstance(getAdminClientConfig());
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
        return ("status=success");
    }

    /**
     * Useful for writing tests which exercise permissions corner cases. Creates
     * a new client and adds the given roles to the client's ACL
     *
     * @param clientId
     * @param clientKeyString
     * @param roles
     * @throws KineticException
     */
    public void createClientAclWithRoles(String clientName, int clientId,
            String clientKeyString,
            List<Kinetic.Command.Security.ACL.Permission> roles)
            throws KineticException {

        Kinetic.Command.Security.ACL.Scope.Builder domain = Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        for (Kinetic.Command.Security.ACL.Permission role : roles) {
            domain.addPermission(role);
        }

        createClientAclWithDomains(clientName, clientId, clientKeyString,
                Collections.singletonList(domain.build()));

        // create a admin clientId with all permission to avoid user nor found.
        List<Kinetic.Command.Security.ACL.Permission> rolesAll = new ArrayList<Kinetic.Command.Security.ACL.Permission>();
        rolesAll.add(Permission.DELETE);
        rolesAll.add(Permission.GETLOG);
        rolesAll.add(Permission.P2POP);
        rolesAll.add(Permission.RANGE);
        rolesAll.add(Permission.READ);
        rolesAll.add(Permission.SECURITY);
        rolesAll.add(Permission.SETUP);
        rolesAll.add(Permission.WRITE);

        int clientIdAdmin = 1;
        String clientIdAdminKey = "asdfasdf";
        Kinetic.Command.Security.ACL.Scope.Builder domainAll = Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        for (Kinetic.Command.Security.ACL.Permission role : rolesAll) {
            domainAll.addPermission(role);
        }
        createClientAclWithDomains(clientName, clientIdAdmin, clientIdAdminKey,
                Collections.singletonList(domainAll.build()));

    }

    /**
     * Useful for writing tests which exercise permissions corner cases. Creates
     * a new client and adds the given domains to the client's ACL
     *
     * @param clientId
     * @param clientKeyString
     * @param domains
     * @throws KineticException
     */
    public void createClientAclWithDomains(String clientName, int clientId,
            String clientKeyString,
            List<Kinetic.Command.Security.ACL.Scope> domains)
            throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        // Kinetic.Message.Builder request = (Kinetic.Message.Builder)
        // km.getMessage();

        Kinetic.Command.Builder commandBuilder = (Kinetic.Command.Builder) km
                .getCommand();
        Header.Builder header = commandBuilder.getHeaderBuilder();

        header.setMessageType(Command.MessageType.SECURITY);

        Kinetic.Command.Security.Builder security = commandBuilder
                .getBodyBuilder().getSecurityBuilder();

        Kinetic.Command.Security.ACL.Builder acl = Kinetic.Command.Security.ACL
                .newBuilder();
        acl.setIdentity(clientId);
        acl.setKey(ByteString.copyFromUtf8(clientKeyString));
        acl.setHmacAlgorithm(Kinetic.Command.Security.ACL.HMACAlgorithm.HmacSHA1);

        for (Kinetic.Command.Security.ACL.Scope domain : domains) {
            acl.addScope(domain);
        }
        security.addAcl(acl);

        // KineticMessage response = getClient(clientName).request(km);
        KineticMessage response = getAdminClient().request(km);
        // Ensure setup succeeded, or else fail the calling test.
        assertEquals(Kinetic.Command.Status.StatusCode.SUCCESS, response
                .getCommand().getStatus().getCode());

    }

    /**
     * Utility method to reduce duplicated test code. Creates a new entry for
     * the given key/value pair, and executes a PUT using the given client.
     *
     * @param key
     *            The string representation of the key to put
     * @param value
     *            The string representation of the value to put
     * @param client
     *            The client used to put
     * @return The entry created from the key/value pair
     * @throws kinetic.client.KineticException
     */
    protected Entry buildAndPutEntry(String key, String value,
            KineticClient client) throws KineticException {
        Entry entry = new Entry(toByteArray(key), toByteArray(value));
        client.put(entry, null);
        return entry;
    }

    @DataProvider(name = "transportProtocolOptions")
    public Object[][] createObjectsBasedOnProtocolOptions()
            throws KineticException {

        Object[][] objects = new Object[kineticClientConfigutations.size()][];
        int i = 0;
        for (String clientName : kineticClientConfigutations.keySet()) {
            objects[i++] = new Object[] { clientName };
        }

        return objects;
    }

    private void createKineticClientConfigurations() throws KineticException {
        kineticClientConfigutations.clear();
        ClientConfiguration clientConfiguration;

        if (Boolean.parseBoolean(System.getProperty("RUN_TCP_TEST"))) {
            clientConfiguration = getClientConfig();
            clientConfiguration.setUseSsl(false);
            clientConfiguration.setUseNio(false);
            kineticClientConfigutations.put(NONNIO_NONSSL_CLIENT,
                    clientConfiguration);
        }

        if (Boolean.parseBoolean(System.getProperty("RUN_NIO_TEST"))) {
            clientConfiguration = getClientConfig();
            clientConfiguration.setUseSsl(false);
            clientConfiguration.setUseNio(true);
            kineticClientConfigutations.put(NIO_NONSSL_CLIENT,
                    clientConfiguration);
        }

        if (Boolean.parseBoolean(System.getProperty("RUN_SSL_TEST"))) {
            clientConfiguration = getClientConfig();
            clientConfiguration.setUseSsl(true);
            clientConfiguration.setUseNio(false);
            clientConfiguration
                    .setPort(clientConfiguration.getSSLDefaultPort());
            kineticClientConfigutations.put(NONNIO_SSL_CLIENT,
                    clientConfiguration);
        }

        if (kineticClientConfigutations.size() == 0) {
            clientConfiguration = getClientConfig();
            clientConfiguration.setUseSsl(false);
            clientConfiguration.setUseNio(true);
            kineticClientConfigutations.put(NIO_NONSSL_CLIENT,
                    clientConfiguration);
        }
    }

    private void closeKineticClients() throws KineticException {

        for (KineticP2pClient client : kineticClients.values()) {
            if (null != client) {
                client.close();
            }
        }
    }
}
