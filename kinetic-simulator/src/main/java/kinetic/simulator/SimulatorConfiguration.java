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
package kinetic.simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Configuration;
import com.seagate.kinetic.simulator.heartbeat.HeartbeatProvider;
import com.seagate.kinetic.simulator.heartbeat.provider.MulticastHeartbeatProvider;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

/**
 * 
 * Simulator configuration instance.
 * 
 */
public class SimulatorConfiguration extends Properties {

    private final static Logger logger = Logger
            .getLogger(SimulatorConfiguration.class.getName());

    private static final long serialVersionUID = 1132514490479251740L;

    public final static String VENDER = "Seagate";
    public final static String MODEL = "Simulator";

    /**
     * Property name to set kinetic home folder directory under
     * <code>KINETIC_HOME</code>
     * <p>
     * The default is set to USER_HOME/kinetic directory if not set.
     * <p>
     * For example, /user/yourName/kinetic.
     */
    public static final String KINETIC_HOME = "kinetic.home";

    /**
     * Property name to set persist home folder directory under the
     * <code>KINETIC_HOME</code> folder.
     * 
     * The default is set to leveldb.ldb under kinetic home folder. For example:
     * <p>
     * /user/yourName/kinetic/leveldb/leveldb.ldb
     * 
     */
    public static final String PERSIST_HOME = "kinetic.persist.home";

    /**
     * server port.
     */
    private int port = 8123;

    /**
     * use nio flag
     */
    private volatile boolean useNio = true;

    /**
     * use ssl transport as default service.
     */
    private final boolean useSslAsDefault = Boolean
            .getBoolean("kinetic.io.ssl");

    /**
     * Start SSL transport service.
     */
    private volatile boolean startSsl = (useSslAsDefault == false);

    /**
     * ssl service port.
     */
    private int sslPort = 8443;

    /**
     * server nio thread pool exit await timeout in milli secs.
     */
    private long threadPoolAwaitTimeout = 100;

    /**
     * default nio event loop threads
     */
    private static final String DEFAULT_NIO_EVENT_LOOP_THREADS = "0";

    /**
     * nio event loop boss threads
     */
    private int nioEventLoopBossThreads = Integer
            .parseInt(DEFAULT_NIO_EVENT_LOOP_THREADS);

    /**
     * nio event loop worker threads
     */
    private int nioEventLoopWorkerThreads = Integer
            .parseInt(DEFAULT_NIO_EVENT_LOOP_THREADS);

    /**
     * flag to indicate if memory store is used for the simulator
     */
    private volatile boolean useMemoryStore = false;

    /**
     * heartbeat tick time in milli-seconds.
     */
    private long tickTime = 5000;

    /**
     * heart beat destination address
     */
    private String mcastDestination = "239.1.2.3";

    /**
     * heart beat destination port
     */
    private int mcastPort = 8123;

    /**
     * nio resource sharing flag for simulators running within the same JVM,
     * default is set to true.
     */
    // private static boolean nioResourceSharing = Boolean
    // .getBoolean("kinetic.nio.resourceSharing");
    private static boolean nioResourceSharing = true;

    /**
     * enforce command process ordering (in sequence) for messages received
     * within the same connection.
     */
    // private boolean messageOrderinEnforced = Boolean
    // .getBoolean("kinetic.nio.messageOrder.enforced");
    private boolean messageOrderinEnforced = true;

    /**
     * max supported value size in bytes
     */
    private static int maxSupportedValueSize = 1024 * 1024;

    /**
     * max supported key size in bytes.
     */
    private static int maxSupportedKeySize = 4096;

    /**
     * max supported version size in bytes
     */
    private static int maxSupportedVersionSize = 2048;

    /**
     * max supported key range size.
     */
    private static int maxSupportedKeyRangeSize = 200;

    /**
     * max supported tag size. -1 means not enforced (yet).
     */
    private static int maxSupportedTagSize = -1;

    /**
     * max supported concurrent connections to the simulator. -1 means not
     * enforced (yet).
     */
    private static int maxConnections = -1;

    /**
     * max supported outstanding read request. -1 means not enforced (yet).
     */
    private static int maxOutstandingReadRequests = -1;

    /**
     * max supported outstanding write request. -1 means not enforced (yet).
     */
    private static int maxOutstandingWriteRequests = -1;

    /**
     * max supported message size. -1 means not enforced (yet).
     */
    private static int maxMessageSize = -1;

    /**
     * max supported identity cout.
     * 
     * -1 means not enforced.
     */
    private static int maxIdentityCount = -1;

    /**
     * max number of commands per batch.
     */
    private static int maxCommandsPerBatch = 15;

    /**
     * max number of outstanding batch requests per drive.
     */
    private static int maxOutstandingBatches = 5;

    /**
     * current simulator version.
     */
    public static final String SIMULATOR_VERSION = "3.0.7-SNAPSHOT";

    /**
     * simulator source commit hash.
     */
    public static final String SIMULATOR_SOURCE_HASH = "4026da95012a74f137005362a419466dbcb2ae5a";

    /**
     * current supported protocol version defined at kinetic-protocol
     * repository.
     */
    public static final String PROTOCOL_VERSION = Kinetic.Local
            .getDefaultInstance().getProtocolVersion();

    /**
     * current supported protocol source commit hash value obtained from
     * kinetic-protocol repository.
     */
    public static final String PROTOCOL_SOURCE_HASH = "a5e192b2a42e2919ba3bba5916de8a2435f81243";

    /**
     * heart beat provider.
     */
    private HeartbeatProvider heartbeatProvider = null;

    /**
     * Serial number as a string
     */
    private String serialNumber = null;

    /**
     * unique world wide name for each instance of simulator.
     */
    private String worldWideName = null;

    /**
     * flag to indicate if the simulator should enforce connection Id
     * verification. if set to true, simulator will check and enforce that
     * client MUST set the assigned connection Id in all sub-sequential request
     * messages to the simulator/drive.
     * 
     * The connection Id is set by drive/simulator in the first response message
     * after a connection is created.
     * 
     * Default is set to false unless the
     * "kinetic.simulator.connection.id.enforced" system property is set to
     * true.
     * 
     * Default will be set to true after the released drive code enforces the
     * verification.
     */
    private static boolean isConnectionIdCheckEnforced = Boolean
            .getBoolean("kinetic.simulator.connection.id.enforced");

    /**
     * Construct a server configuration instance with the specified defaults.
     * 
     * @param props
     *            default server configuration properties.
     */
    public SimulatorConfiguration(Properties props) {
        super(props);
        init();
    }

    /**
     * Construct a server configuration instance with the system properties as
     * defaults.
     * 
     */
    public SimulatorConfiguration() {
        super(System.getProperties());
        init();
    }

    /**
     * init states.
     */
    private void init() {
        // init # of nio threads
        this.nioEventLoopBossThreads = Integer.parseInt(super.getProperty(
                "kinetic.nio.thread", DEFAULT_NIO_EVENT_LOOP_THREADS));
    }

    /**
     * Set service port.
     * 
     * @param port
     *            server port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get service port.
     * 
     * @return server port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Set SSL service port.
     * 
     * @param sslPort
     *            SSL service port.
     * @see #startSsl
     */
    public void setSslPort(int sslPort) {

        this.sslPort = sslPort;
    }

    /**
     * Get SSL service port.
     * 
     * @return ssl service port.
     */
    public int getSslPort() {
        return this.sslPort;
    }

    /**
     * Set start SSL/TLS service flag.
     * <p>
     * By default, the simulator is started with TCP (8123) and SSL (8443)
     * services.
     * <p>
     * If the parameter is set to true, application may also use
     * {@link #setSslPort(int)} to set SSL service port such that it is
     * different from the default SSL/TLS service port (8443).
     * <p>
     * Applications may define Java System Property "-Dkinetic.io.ssl=true" to
     * use SSL/TLS as the only (default) transport service. Applications should
     * avoid calling this method with parameter (flag) set to true when
     * "-Dkinetic.io.ssl=true" is already defined.
     * <p>
     * 
     * @param flag
     *            Set to false to disable SSL service. Default is set to true.
     * 
     * @see #getUseSslAsDefault()
     * @see #setSslPort(int)
     */
    public void setStartSsl(boolean flag) {
        this.startSsl = flag;
    }

    /**
     * Get start SSL service flag.
     * 
     * @return true if start SSL flag is set to true. Otherwise, return false.
     */
    public boolean getStartSsl() {
        return this.startSsl;
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
     * 
     * Applications may define Java System Property "-Dkinetic.io.ssl=true" and
     * uses SSL/TLS as the default transport service.
     * <p>
     * 
     * @return true if SSL/TLS is used as the default transport service.
     *         Otherwise, return false.
     */
    public boolean getUseSslAsDefault() {
        return this.useSslAsDefault;
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
     *            Java Nio thread pool exit await timeout in milli seconds
     */
    public void setThreadPoolAwaitTimeOut(long millis) {
        this.threadPoolAwaitTimeout = millis;
    }

    /**
     * Get configuration property with the specified property name.
     * <p>
     * If there is a value set to the configuration, the value is returned. If
     * there is no value set to the configuration, the Simulator checks and
     * returns the value if Java System Property is defined for the property
     * name.
     */
    @Override
    public String getProperty(String name) {
        String value = null;

        value = super.getProperty(name);

        if (value == null) {
            value = System.getProperty(name);
        }

        return value;
    }

    /**
     * Set nio service thread pool number for the simulator nio 'boss' (in-bound
     * connection) thread pool.
     * <p>
     * If not set, the default is set to 0 - the system will determine the
     * number based on available processors obtained from <code>Runtime</code>
     * API.
     * 
     * @param nThreads
     *            number of threads for simulator nio in-bound connection thread
     *            pool.
     * 
     * @see Runtime#availableProcessors()
     */
    public void setNioServiceBossThreads(int nThreads) {

        if (nThreads < 0) {
            throw new java.lang.IllegalArgumentException(
                    "nThread must be greater or equal to 0");
        }

        this.nioEventLoopBossThreads = nThreads;
    }

    /**
     * Get nio thread number for the simulator nio service thread pool.
     * 
     * @return nio threads number for the simulator nio service thread pool.
     */
    public int getNioServiceBossThreads() {
        return this.nioEventLoopBossThreads;
    }

    /**
     * Set nio service thread pool number for the simulator nio 'worker' thread
     * pool.
     * <p>
     * If not set, the default is set to 0 - the system will determine the
     * number based on available processors obtained from <code>Runtime</code>
     * API.
     * 
     * @param nThreads
     *            number of threads for simulator nio worker thread pool.
     * 
     * @see Runtime#availableProcessors()
     */
    public void setNioServiceWorkerThreads(int nThreads) {

        if (nThreads < 0) {
            throw new java.lang.IllegalArgumentException(
                    "nThread must be greater or equal to 0");
        }

        this.nioEventLoopWorkerThreads = nThreads;
    }

    /**
     * Get nio thread number for the simulator nio service worker thread pool.
     * 
     * @return nio threads number for the simulator nio service worker thread
     *         pool.
     */
    public int getNioServiceWorkerThreads() {
        return this.nioEventLoopWorkerThreads;
    }

    /**
     * Set to true to instruct the simulator to use memory store.
     * <p>
     * The default is set to false. LevelDB is used by default.
     * 
     * @param useMemoryStore
     *            set to true to instruct the simulator to use memory store.
     */
    public void setUseMemoryStore(boolean useMemoryStore) {
        this.useMemoryStore = useMemoryStore;
    }

    /**
     * Get if memory store is (will be used) used by the simulator.
     * 
     * @return true to use memory store. Otherwise, LevelDB is used.
     */
    public boolean getUseMemoryStore() {
        return this.useMemoryStore;
    }

    /**
     * Set heart beat tick time for the simulator (in milli-seconds).
     * <p>
     * Default is set to 30000 milli-seconds.
     * 
     * @param tickTime
     *            heartbeat for the simulator.
     */
    public void setTickTime(long tickTime) {
        if (this.tickTime < 0) {
            throw new java.lang.IllegalArgumentException(
                    "tick time must be greater or equal to 0");
        }

        this.tickTime = tickTime;
    }

    /**
     * Get heart beat tick time (in milli-seconds) for the simulator.
     * <p>
     * Default is set to 5000 milli-seconds.
     * 
     * @return heart beat tick time for the simulator.
     */
    public long getTickTime() {
        return this.tickTime;
    }

    /**
     * Set heart beat address for the simulator. The heart beat will be sent to
     * the specified multicast address and port.
     * 
     * @param multicastAddress
     *            heart beat address for the simulator.
     * 
     * @see #setHeartBeatPort(int)
     */
    public void setHeartbeatAddress(String multicastAddress) {
        this.mcastDestination = multicastAddress;
    }

    /**
     * Get heart beat address for the simulator. The heart beat will be sent to
     * the specified multicast address and port.
     * 
     * @return heart beat address for the simulator.
     * 
     * @see #setHeartBeatPort(int)
     */
    public String getHeartbeatAddress() {
        return this.mcastDestination;
    }

    /**
     * Set heart beat destination port number. The heart beat will be sent to
     * the specified multicast address and port.
     * 
     * @param multicastPort
     *            heart beat destination port number.
     * 
     * @see #setHeartbeatAddress(String)
     */
    public void setHeartBeatPort(int multicastPort) {
        this.mcastPort = multicastPort;
    }

    /**
     * Get heart beat destination port number. The heart beat will be sent to
     * the specified multicast address and port.
     * 
     * 
     * @see #getHeartbeatAddress()
     * @return heart beat destination port number
     */
    public int getHeartbeatPort() {
        return this.mcastPort;
    }

    /**
     * Set nio resource sharing flag. Set this flag to true to start a large
     * number of simulators (such as thousands) within the same JVM, depending
     * on the runtime operation system configuration.
     * 
     * @param flag
     *            if set to true, nio resources will be shared within the same
     *            JVM for the simulator.
     */
    public static void setNioResourceSharing(boolean flag) {
        nioResourceSharing = flag;
    }

    /**
     * Get nio resource sharing flag. If set to true, applications may be able
     * to start a large number of simulators (such as thousands) within the same
     * JVM, depending on the runtime operation system configuration.
     * 
     * @return true if the simulators share nio resources. Otherwise return
     *         false.
     */
    public static boolean getNioResourceSharing() {
        return nioResourceSharing;
    }

    /**
     * Get if message is processed in received order within the same connection.
     * 
     * @return true if message ordering is enforced. Otherwise, return false.
     */
    public boolean getMessageOrderingEnforced() {
        return this.messageOrderinEnforced;
    }

    /**
     * 
     * Set if message ordering should be enforced for the simulator.
     * <p>
     * This flag must be set before starting/instantiating the simulator.
     * 
     * @param flag
     *            set to true to instruct the simulator to enforce message
     *            ordering within the same connection (a kinetic client
     *            instance)
     */
    public void setMessageOrderingEnforced(boolean flag) {
        this.messageOrderinEnforced = flag;
    }

    /**
     * Max supported value size in bytes. Default is set to 1M bytes (1024 *
     * 1024).
     * 
     * @return max supported value size for the simulator.
     */
    public static int getMaxSupportedValueSize() {
        return maxSupportedValueSize;
    }

    /**
     * Set max supported value size, in bytes.
     * 
     * @param size
     *            set max supported value size.
     * @see #getMaxSupportedValueSize()
     */
    public static void setMaxSupportedValueSize(int size) {
        maxSupportedValueSize = size;
    }

    /**
     * Get max supported key size in bytes. Default is set to 4096 bytes.
     * 
     * @return max supported key size for the simulator.
     */
    public static int getMaxSupportedKeySize() {
        return maxSupportedKeySize;
    }

    /**
     * Set max supported key size, in bytes.
     * 
     * @param size
     *            set max supported key size.
     * @see #getMaxSupportedKeySize()
     */
    public static void setMaxSupportedKeySize(int size) {
        maxSupportedKeySize = size;
    }

    /**
     * Get max supported version size in bytes. Default is set to 2048 bytes.
     * 
     * @return max supported key size for the simulator.
     */
    public static int getMaxSupportedVersionSize() {
        return maxSupportedVersionSize;
    }

    /**
     * Set max supported version size, in bytes.
     * 
     * @param size
     *            set max supported version size.
     * @see #getMaxSupportedVersionSize()
     */
    public static void setMaxSupportedVersionSize(int size) {
        maxSupportedVersionSize = size;
    }

    /**
     * Get max supported key range size. Default is set to 1024.
     *
     * @return max supported key range size for the simulator.
     */
    public static int getMaxSupportedKeyRangeSize() {
        return maxSupportedKeyRangeSize;
    }

    /**
     * Set max supported key range size, in bytes.
     *
     * @param size
     *            set max supported key range size.
     */
    public static void setMaxSupportedKeyRangeSize(int size) {
        maxSupportedKeyRangeSize = size;
    }

    /**
     * Get max supported concurrent connections. There is no enforcement for the
     * simulator at this time.
     * 
     * Returns -1 means no limit is enforced for the current implementation. The
     * number will be adjusted when the limit is enforced.
     *
     * @return default value (-1)
     */
    public static int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Get max supported message size. There is no enforcement for the simulator
     * at this time.
     * 
     * Returns -1 means no limit is enforced for the current implementation. The
     * number will be adjusted when the limit is enforced.
     *
     * @return default value (-1)
     */
    public static int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Get max outstanding read requests. There is no enforcement for the
     * simulator at this time.
     * 
     * Returns -1 means no limit is enforced for the current implementation. The
     * number will be adjusted when the limit is enforced.
     *
     * @return default value (-1)
     */
    public static int getMaxOutstandingReadRequests() {
        return maxOutstandingReadRequests;
    }

    /**
     * Get max outstanding write requests. There is no enforcement for the
     * simulator at this time.
     * 
     * Returns -1 means no limit is enforced for the current implementation. The
     * number will be adjusted when the limit is enforced.
     *
     * @return default value (-1)
     */
    public static int getMaxOutstandingWriteRequests() {
        return maxOutstandingWriteRequests;
    }

    /**
     * Get max supported tag size. There is no enforcement for the simulator at
     * this time.
     * 
     * Returns -1 means no limit is enforced for the current implementation. The
     * number will be adjusted when the limit is enforced.
     *
     * @return default value (-1)
     */
    public static int getMaxSupportedTagSize() {
        return maxSupportedTagSize;
    }

    /**
     * Get maximum identity count.
     * 
     * Returns -1 means not enforced by the current implementation.
     * 
     * @return default value (-1)
     */
    public static int getMaxIdentityCount() {
        return maxIdentityCount;
    }

    /**
     * Get the current simulator version.
     *
     * @return current simulator version.
     */
    public static String getSimulatorVersion() {
        return SIMULATOR_VERSION;
    }

    /**
     * Get the simulator source commit hash at he github repository.
     *
     * @return simulator source hash commit value
     * @see <a
     *      href="https://github.com/Seagate/kinetic-protocol">kinetic-java</a>
     */
    public static String getSimulatorSourceHash() {
        return SIMULATOR_SOURCE_HASH;
    }

    /**
     * Get Kinetic protocol version supported by the current API implementation.
     * The protocol version is defined at the kinetic-protocol repository.
     * <p>
     * <a
     * href="https://github.com/Seagate/kinetic-protocol">kinetic-protocol</a>
     * <p>
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
     * Set the heartbeat provider for the simulator.
     * 
     * @param provider
     *            provider to be used by the current instance of simulator
     */
    public void setHeartbeatProvider(HeartbeatProvider provider) {
        this.heartbeatProvider = provider;
    }

    /**
     * Get heartbeat provider for the simulator instance. The
     * <code>MulticastHeartbeatProvider</code> is used if not set.
     * 
     * @return the heartbeat provider for the simulator instance
     */
    public HeartbeatProvider getHeartbeatProvider() {

        if (this.heartbeatProvider == null) {

            // use default multicast provider
            synchronized (this) {
                this.heartbeatProvider = new MulticastHeartbeatProvider();
            }
        }

        return this.heartbeatProvider;
    }

    /**
     * Set if simulator should enforce connection Id verification.
     * 
     * @param flag
     *            set to true if simulator should enforce connection Id
     *            verification.
     */
    // public synchronized static void setIsConnectionIdCheckEnforced (boolean
    // flag) {
    // isConnectionIdCheckEnforced = flag;
    // }

    /**
     * Get flag that indicates if simulator is enforcing connection Id
     * verification.
     * 
     * @return true if simulator is enforcing connection Id verification
     */
    public static boolean getIsConnectionIdCheckEnforced() {
        return isConnectionIdCheckEnforced;
    }

    /**
     * Get simulator home folder name.
     * 
     * @return simulator home folder name
     */
    public String getSimulatorHome() {

        // get default
        String defaultHome = System.getProperty("user.home") + File.separator
                + "kinetic";

        // use user defined home
        String kineticHome = getProperty(KINETIC_HOME, defaultHome);

        return kineticHome;
    }

    /**
     * Get the serial number of the running instance of simulator.
     * <p>
     * This number is to simulator a drive's serial number.
     * 
     * @return the serial number of the running instance of simulator
     */
    public String getSerialNumber() {

        if (this.worldWideName == null) {
            this.calculateWorldWideName();
        }

        return this.serialNumber;
    }

    private synchronized void calculateWorldWideName() {

        if (this.worldWideName != null) {
            return;
        }

        try {
            // calculate kinetic home
            String khome = SimulatorEngine.kineticHome(this);

            // get wwn path
            String wwnFilePath = khome + File.separator + ".wwn";

            // wwn file instance
            File wwnFile = new File(wwnFilePath);

            if (wwnFile.exists()) {

                /**
                 * The file exists, get wwn from the content.
                 */
                FileInputStream in = new FileInputStream(wwnFile);

                // read contents
                Configuration conf = Configuration.parseFrom(in);
                in.close();

                // get wwn ByteString
                ByteString wwn = conf.getWorldWideName();

                // get wwn Java String type
                this.worldWideName = wwn.toStringUtf8();

                // get serial number
                this.serialNumber = conf.getSerialNumber().toStringUtf8();
            } else {
                // get UUID for this instance
                UUID uuid = UUID.randomUUID();

                // wwn name
                this.worldWideName = uuid.toString();

                // calculate serial number.
                this.serialNumber = "S" + Math.abs(worldWideName.hashCode());

                /**
                 * persist wwn/serial number.
                 */
                FileOutputStream out = new FileOutputStream(wwnFile);
                Configuration.Builder cb = Configuration.newBuilder();

                // set serial number
                cb.setSerialNumber(ByteString.copyFromUtf8(this.serialNumber));

                // set wwn
                cb.setWorldWideName(ByteString.copyFromUtf8(this.worldWideName));

                // persist conf.
                cb.build().writeTo(out);

                out.close();
            }

        } catch (Exception e) {
            this.worldWideName = "SIMULATOR-" + System.nanoTime();
            this.serialNumber = "S" + Math.abs(worldWideName.hashCode());
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            ;
        }

    }

    /**
     * Get world wide name of the running instance of the simulator.
     *
     * @return world wide name of the running instance of the simulato
     */
    public String getWorldWideName() {

        if (this.worldWideName == null) {
            this.calculateWorldWideName();
        }

        return this.worldWideName;
    }

    /**
     * Get maximum number of commands per batch request.
     * 
     * @return maximum number of commands per batch request.
     */
    public static int getMaxCommandsPerBatch() {
        return maxCommandsPerBatch;
    }

    /**
     * Get maximum number of outstanding batch requests per device.
     * 
     * @return maximum number of outstanding batch requests per device.
     */
    public static int getMaxOutstandingBatches() {
        return maxOutstandingBatches;
    }

}
