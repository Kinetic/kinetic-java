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
package kinetic.simulator;

import java.util.Properties;

import com.seagate.kinetic.simulator.heartbeat.HeartbeatProvider;
import com.seagate.kinetic.simulator.heartbeat.provider.MulticastHeartbeatProvider;

/**
 * 
 * Simulator configuration instance.
 * 
 */
public class SimulatorConfiguration extends Properties {

	private static final long serialVersionUID = 1132514490479251740L;

	/**
	 * Property name to set kinetic home folder directory under
	 * <code>KINETIC_HOME</code>
	 * <p>
	 * The default is set to USER_HOME/kinetic directory if not set.
	 * <p>
	 * For example, /user/yourName/kinetic.
	 */
	public static final String KINETIC_HOME = "kenitic.home";

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
	private long tickTime = 30000;

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
	private static long maxSupportedValueSize = 1024 * 1024;

	private HeartbeatProvider heartbeatProvider = null;

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
		this.useNio = flag;
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
	 * Default is set to 30000 milli-seconds.
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
	public static long getMaxSupportedValueSize() {
		return maxSupportedValueSize;
	}

	/**
	 * Set max supported value size, in bytes.
	 * 
	 * @param size
	 *            set max supported value size.
	 * @see #getMaxSupportedValueSize()
	 */
	public static void setMaxSupportedValueSize(long size) {
		maxSupportedValueSize = size;
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

}
