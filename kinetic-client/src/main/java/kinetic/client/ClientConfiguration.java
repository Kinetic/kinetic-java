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
package kinetic.client;

import java.util.Properties;
import java.util.logging.Logger;

import com.seagate.kinetic.proto.Kinetic;

/**
 * Kinetic Client configuration.
 * <p>
 * Kinetic applications construct a new instance of this instance and set
 * appropriate configurations. Application then calls
 * {@link KineticClientFactory#createInstance(ClientConfiguration)} to create a
 * new instance of {@link KineticClient}
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * 
 * @see KineticClientFactory#createInstance(ClientConfiguration)
 * @see KineticClient
 */
public class ClientConfiguration extends Properties {

    private final static Logger logger = Logger
            .getLogger(ClientConfiguration.class.getName());

    private static final long serialVersionUID = 7330657102192607375L;

    public static final String DEFAULT_TIMEOUT_PROP_NAME = "kinetic.request.timeout";

    // default request timeout is set to 60000 milli seconds
    private static final long DEFAULT_REQUEST_TIMEOUT = Integer.getInteger(
            DEFAULT_TIMEOUT_PROP_NAME, 60000).longValue();

    /**
     * current supported kinetic protocol version on kinetic-protocol repo.
     */
    public static final String PROTOCOL_VERSION = Kinetic.Local
            .getDefaultInstance().getProtocolVersion();

    /**
     * current supported protocol source commit hash on kinetic-protocol repo.
     */
    public static final String PROTOCOL_SOURCE_HASH = "a5e192b2a42e2919ba3bba5916de8a2435f81243";

    // kinetic server host
    private String host = "localhost";

    // kinetic server port
    private int port = 8123;

    // local address used to connect to server
    private String localAddress = null;

    // local port used to connect to server
    private int localPort = 0;

    // user id
    private long userId = 1;

    // key
    private String key = "asdfasdf";

    // cluster version
    private long clusterVersion = 0;

    /**
     * use nio flag
     */
    private volatile boolean useNio = true;

    /**
     * Nio service threads number in service thread pool.
     */
    private int nThreads = 0;

    /**
     * flag to use ssl. if the system property is set, ssl is used.
     */
    private volatile boolean useSsl = Boolean.getBoolean("kinetic.io.ssl");

    /**
     * ssl default port if useSsl is set to true.
     */
    private final static int SSL_DEFAULT_PORT = 8443;

    // socket time out
    private int timeoutMillis = 0;

    /**
     * request timeout in milli seconds. For synchronous request, each request
     * will return immediately when received a response or up to the timeout set
     * in this configuration instance.
     * <p>
     * default is set to 60000 milli seconds
     */
    private long requestTimeout = DEFAULT_REQUEST_TIMEOUT;

    /**
     * client nio thread pool exit await timeout in milli secs.
     */
    private long threadPoolAwaitTimeout = 50;

    /**
     * Asynchronous put queued size.
     */
    private int asyncQueueSize = 10;

    // expected wwn to connect to.
    private String expectedWwn = null;

    // connection listener
    private ConnectionListener listener = null;

    /**
     * Client configuration constructor.
     * 
     * @param props
     *            default initialized properties for the kinetic client.
     */
    public ClientConfiguration(Properties props) {
        super(props);
    }

    /**
     * Client configuration constructor.
     * <p>
     * System properties are used as default configuration properties.
     */
    public ClientConfiguration() {
        super(System.getProperties());
    }

    /**
     * Set kinetic server host name.
     * 
     * @param host
     *            kinetic server host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Set local address client used to connect to server.
     * 
     * @param localAddress
     *            local address client used to connect to server
     */
    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Set local address port client used to connect to the server.
     * 
     * @param localPort
     *            local address port client used to connect to the server
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    /**
     * Get local address client used to connect to server.
     * <p>
     * The local address the socket is bound to, or null for the anyLocal
     * address. Default is set to null.
     * 
     * @return local address client used to connect to server.
     */
    public String getLocalAddress() {
        return this.localAddress;
    }

    /**
     * 
     * Get local port client used to connect to server.
     * <p>
     * The local port the socket is bound to or zero for a system selected free
     * port.
     * 
     * @return local port local port client used to connect to server
     */
    public int getLocalPort() {
        return this.localPort;
    }

    /**
     * Set server port.
     * 
     * @param port
     *            server port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get server host name.
     * 
     * @return server host name.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Get server port number.
     * 
     * @return server port number.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Get user id.
     * 
     * @return user id.
     */
    public long getUserId() {
        return this.userId;
    }

    /**
     * set user/client id.
     * 
     * @param userId
     *            user/client id
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * set user key.
     * 
     * @param key
     *            user key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Get user key.
     * 
     * @return user key.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * set cluster version.
     * 
     * @param clusterVersion
     *            cluster version
     */
    public void setClusterVersion(long clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    /**
     * Get cluster version
     * 
     * @return cluster version
     */
    public long getClusterVersion() {
        return clusterVersion;
    }

    /**
     * Get connection Id.
     * 
     * @return connection id.
     */
    public long getConnectionId() {

        // connection id
        long connectionID = Long.parseLong(getProperty("kinetic.connectionId",
                "-1"));

        // init if not set
        if (connectionID == -1) {
            connectionID = System.nanoTime();
        }

        return connectionID;
    }

    /**
     * Set use Java NIO or not.
     * 
     * @param flag
     *            set to true if use nio. Otherwise, set to false. The default
     *            is set to true if not set.
     * 
     */
    public void setUseNio(boolean flag) {

        // XXX chiaming 07/19/2014: This will be re-enabled when it is
        // compatible with 3.0.0
        logger.warning("Method is disabled., Only NIO is supported.");

        // this.useNio = flag;
    }

    /**
     * Get if the server uses Java NIO.
     * 
     * @return true if the server uses Java NIO. Otherwise, return false.
     */
    public boolean getUseNio() {
        return this.useNio;
    }

    /**
     * set if SSL/TLS socket should be used.
     * 
     * @param flag
     *            set to true to use SSL. Default is set to false.
     * 
     */
    public void setUseSsl(boolean flag) {
        this.useSsl = flag;
    }

    /**
     * Get if SSL/TLS transport is used.
     * 
     * @return true if SSL/TLS transport is used. Otherwise, return false.
     */
    public boolean getUseSsl() {
        return this.useSsl;
    }

    /**
     * Get SSL/TLS transport default port for the kinetic instance if it is not
     * set by application.
     * 
     * @return SSL/TLS transport default port for kinetic instance if it is not
     *         set by application.
     */
    public int getSSLDefaultPort() {
        return SSL_DEFAULT_PORT;
    }

    /**
     * Set socket timeout in milli seconds.
     * 
     * @param millis
     *            socket timeout in milli seconds.
     */
    public void setConnectTimeoutMillis(int millis) {
        this.timeoutMillis = millis;
    }

    /**
     * Get socket timeout used in the current client instance.
     * 
     * @return socket timeout used in the current client instance.
     */
    public int getConnectTimeoutMillis() {
        return this.timeoutMillis;
    }

    /**
     * Set request timeout (in milli seconds) for the client instance.
     * <p>
     * The default request timeout is set to 60000 milli seconds if not set. The
     * default value is used if the specified timeout is equal or less than 0.
     * <p>
     * Applications also can set the default timeout with a Java System Property
     * {@link #DEFAULT_REQUEST_TIMEOUT}
     * <p>
     * This API overrides the value set by the above Java System Property.
     * <p>
     * Setting a small request timeout could introduce unexpected side effects.
     * Such as timed out before a response is received.
     * <p>
     * Applications may set a large timeout value when desired, such as for long
     * running request-response operations.
     * 
     * @param millis
     *            request timeout (in milli seconds) for the current client
     *            instance
     * 
     * @see #setRequestTimeoutMillis(long)
     */
    public void setRequestTimeoutMillis(long millis) {

        if (millis <= 0) {

            logger.warning("Specified timeout value "
                    + millis
                    + " is not supported. Using default request timeout: "
                    + DEFAULT_REQUEST_TIMEOUT + "(milli seconds)");

            millis = DEFAULT_REQUEST_TIMEOUT;
        } else if (millis < DEFAULT_REQUEST_TIMEOUT) {
            logger.warning("request timeout set to "
                    + millis
                    + " milli seconds.  This may cause the client runtime library not to receive responses in time when network/service is slow.");
        }

        this.requestTimeout = millis;

        logger.info("request timeout is set to " + millis + " (milli seconds)");
    }

    /**
     * Get the request timeout (in milli seconds) used for the client instance.
     * 
     * @return the request timeout (in milli seconds) used for the client
     *         instance.
     * 
     * @see #setRequestTimeoutMillis(long)
     */
    public long getRequestTimeoutMillis() {
        return this.requestTimeout;
    }

    /**
     * Set asynchronous operation queued size. For asynchronous operations, a
     * request is blocked when the queued requests reached the count set for
     * this instance. Default is set to 10.
     * 
     * @param qsize
     *            the max queued operations to be set for the current kinetic
     *            client instance.
     */
    public void setAsyncQueueSize(int qsize) {
        this.asyncQueueSize = qsize;
    }

    /**
     * Get asynchronous operation queued size. For asynchronous operations, a
     * request is blocked when the queued requests reached the size set for this
     * instance. Default is set to 10.
     * 
     * @return the max queued operations for the current kinetic instance
     */
    public int getAsyncQueueSize() {
        return this.asyncQueueSize;
    }

    /**
     * Get Java Nio thread pool exit await timeout - used when connection is
     * closed and Java client runtime library waiting for thread pool to exit.
     * 
     * @return time out in milli seconds.
     */
    public long getThreadPoolAwaitTimeout() {
        return this.threadPoolAwaitTimeout;
    }

    /**
     * Set Java Nio thread pool exit await timeout - used when connection is
     * closed and Java client runtime library waiting for thread pool to exit.
     * 
     * @param millis
     *            timeout in milli seconds
     */
    public void setThreadPoolAwaitTimeOut(long millis) {
        this.threadPoolAwaitTimeout = millis;
    }

    /**
     * Set number of thread used in kinetic client nio services.
     * 
     * @param nThreads
     *            number of thread used in kinetic client nio services.
     */
    public void setNioServiceThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    /**
     * Get number of thread used in kinetic client nio services.
     * 
     * @return number of thread used in kinetic client nio services
     */
    public int getNioServiceThreads() {
        return this.nThreads;
    }

    /**
     * Get Kinetic protocol version supported by the current API implementation.
     * The protocol version is defined at the kinetic-protocol repository.
     *
     * @return Kinetic protocol version supported by the current API
     *         implementation.
     * 
     * @see <a
     *      href="https://github.com/Seagate/kinetic-protocol">kinetic-protocol</a>
     */
    public static String getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    /**
     * Get the supported protocol source commit hash at the kinetic-protocol
     * repository.
     * 
     * @return protocol source commit hash value at the kinetic-protocol
     *         repository.
     * 
     * @see <a
     *      href="https://github.com/Seagate/kinetic-protocol">kinetic-protocol</a>
     */
    public static String getProtocolSourceHash() {
        return PROTOCOL_SOURCE_HASH;
    }

    /**
     * Set expected WWN for the connected drive.
     * <p>
     * If set, the drive's WWN will be validated by the Java Client Runtime
     * library with the expected WWN when a connection is created to the drive.
     * <p>
     * A connection creation will fail if the expected WWN is set to a non-empty
     * value and it does not match the drive's WWN. In this case, the connection
     * is closed and KineticException is raised.
     * 
     * @param wwn
     *            the expected drive's WWN to be validated.
     */
    public void setExpectedWwn(String wwn) {
        this.expectedWwn = wwn;
    }

    /**
     * Get expected WWN set by the application.
     * 
     * @return expected WWN set by the application.
     */
    public String getExpectedWwn() {
        return this.expectedWwn;
    }

    /**
     * Set connection listener to the connected Kinetic drive or simulator.
     * <p>
     * Upon received an unsolicitated status message, the Java runtime library
     * calls the listener if registered.
     * <p>
     * The thread execution from the Java client runtime library is serialized
     * such that each sub-sequential messages are delivered only after the
     * previous onMessage call returned.
     * 
     * @param listener
     *            connection listener to the connected service.
     * 
     * @see ConnectionListener
     */
    public void setConnectionListener(ConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Get the current registered connection listener.
     * 
     * @return the current registered connection listener. Return null if not
     *         registered.
     */
    public ConnectionListener getConnectionListener() {
        return this.listener;
    }

}
