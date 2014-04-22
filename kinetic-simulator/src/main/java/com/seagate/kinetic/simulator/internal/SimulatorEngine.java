/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.internal;

import java.io.File;
import java.security.Key;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.heartbeat.message.ByteCounter;
import com.seagate.kinetic.heartbeat.message.OperationCounter;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL;
import com.seagate.kinetic.simulator.heartbeat.Heartbeat;
import com.seagate.kinetic.simulator.internal.p2p.P2POperationHandler;
import com.seagate.kinetic.simulator.io.provider.nio.NioEventLoopGroupManager;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;
import com.seagate.kinetic.simulator.io.provider.spi.TransportProvider;
import com.seagate.kinetic.simulator.lib.HeaderOp;
import com.seagate.kinetic.simulator.lib.HmacStore;
import com.seagate.kinetic.simulator.lib.SetupInfo;
import com.seagate.kinetic.simulator.persist.KVOp;
import com.seagate.kinetic.simulator.persist.RangeOp;
import com.seagate.kinetic.simulator.persist.Store;
import com.seagate.kinetic.simulator.persist.StoreFactory;

/**
 *
 * Simulator boot-strap class.
 * <p>
 * Applications use this class to start new instance(s) of the simulator.
 * <p>
 * There is a main method provided in this class as a reference as well as a
 * default simulator behavior. Applications may also define their own
 * SimulatorConfigration instances and start the simulator with customized
 * configurations.
 * <p>
 * Applications may plug-in persistent store for the simulator based on the
 * contract defined in the Store interface.
 * <p>
 * Applications may also plug-in transport for the simulator based on the
 * contract defined on the TransportProvider interface.
 * <p>
 *
 * @see SimulatorConfiguration
 * @see StoreFactory.
 * @see Store
 * @see MessageService
 *
 * @author James Hughes
 * @author Chiaming Yang
 */
public class SimulatorEngine implements MessageService {

    private final static Logger logger = Logger.getLogger(SimulatorEngine.class
            .getName());

    private SimulatorConfiguration config = null;

    @SuppressWarnings("rawtypes")
    private Store store = null;

    private final ArrayList<TransportProvider> transports = new ArrayList<TransportProvider>();

    private final TransportProvider sslService = null;

    // ack map
    private Map<Long, ACL> aclmap = null;

    private Map<Long, Key> hmacKeyMap = null;

    private Long clusterVersion = null;

    private byte[] pin = null;

    private final boolean isHttp = Boolean.getBoolean("kinetic.io.http");

    private final boolean isHttps = Boolean.getBoolean("kinetic.io.https");

    private final boolean isUdt = Boolean.getBoolean("kinetic.io.udt");

    // define this to load the user defined transport provider
    private final boolean isLoadTransportPlugIn = Boolean
            .getBoolean("kinetic.io.plugin");

    // if isLoadTransportPlugIn is defined to true, specified the transport
    // provider class name with the following property.
    private final String TRANSPORT_PLUGIN_CLASS = "kinetic.io.plugin.class";

    private static final String UDT_TRANSPORT = "com.seagate.kinetic.simulator.io.provider.nio.udt.UdtTransportProvider";

    private static final String HTTP_TRANSPORT = "com.seagate.kinetic.simulator.io.provider.nio.http.HttpTransportProvider";

    private static final String TCP_TRANSPORT = "com.seagate.kinetic.simulator.io.provider.tcp.TcpTransportProvider";

    private static final String TCP_NIO_TRANSPORT = "com.seagate.kinetic.simulator.io.provider.nio.tcp.TcpNioTransportProvider";

    private static final String SSL_NIO_TRANSPORT = "com.seagate.kinetic.simulator.io.provider.nio.ssl.SslNioTransportProvider";

    private P2POperationHandler p2pHandler = null;

    private NioEventLoopGroupManager nioManager = null;

    private Heartbeat heartbeat = null;

    // operation counter
    private final OperationCounter operationCounter = new OperationCounter();

    // byte counter
    private final ByteCounter byteCounter = new ByteCounter();

    // flag to indicate if the simulator is closing
    private volatile boolean isClosing = false;

    // resource for all the simulator instances
    private static ThreadPoolService tpService = new ThreadPoolService();

    // shutdown hook
    private static SimulatorShutdownHook shutdownHook = new SimulatorShutdownHook(
            tpService);

    static {
        // add shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Simulator constructor.
     *
     * @param config
     *            simulator configuration.
     */
    public SimulatorEngine(SimulatorConfiguration config) {

        // config for the current instance
        this.config = config;

        // heart beat
        if (config.getTickTime() > 0) {
            // construct new heart beat instance
            this.heartbeat = new Heartbeat(this);
        }

        // register to use thread pool
        tpService.register(this);

        // p2p op handler.
        p2pHandler = new P2POperationHandler();

        try {
            String kineticHome = kineticHome(config);
            Map<Long, ACL> loadedAclMap = SecurityHandler.loadACL(kineticHome);
            if (loadedAclMap.size() > 0) {
                this.aclmap = loadedAclMap;
                this.hmacKeyMap = HmacStore.getHmacKeyMap(loadedAclMap);
            } else {
                // get ack map
                this.aclmap = HmacStore.getAclMap();

                // get hmac key map
                this.hmacKeyMap = HmacStore.getHmacKeyMap(aclmap);
            }
            SetupInfo setupInfo = SetupHandler.loadSetup(kineticHome);
            clusterVersion = setupInfo.getClusterVersion();
            pin = setupInfo.getPin();

            // initialize db store
            this.initStore();

            // init network io service
            this.initIoService();

        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    /**
     * Load, initialize, and start simulator transport services.
     *
     * @throws Exception
     *             if any exception occurred.
     */
    private void initIoService() throws Exception {

        /**
         * load transports
         */
        this.loadTransports();

        /**
         * start transports
         */
        for (TransportProvider transport : transports) {
            transport.init(this);
            transport.start();
        }
    }

    /**
     * Load transport providers with simple rules.
     * <p>
     * The default is to load tcpnio and sslnio transport providers. These are
     * supported transports and are compatible with the Kinetic drive protocol.
     * <p>
     * The rest of the transport are experimental and are not
     * compatible/supported by Kinetic drive.
     * <p>
     * If users specifies to load from a plug-in class, then it is loaded and
     * used.
     * <p>
     * else the loading logic is checked as defined in-lines.
     */
    private void loadTransports() {

        // if "kinetic.io.plugin" property is defined, then the class of the
        // provider is loaded.
        if (isLoadTransportPlugIn) {
            // get the class name from the property "kinetic.io.plugin.class"
            String plugInName = config.getProperty(TRANSPORT_PLUGIN_CLASS);

            logger.info("loading plugin transport, class name=" + plugInName);

            this.loadProvider(plugInName);
        } else if (this.isUdt) {
            // if "kinetic,io.udt" is set to true, load UDT transport
            this.loadProvider(UDT_TRANSPORT);
        } else if (isHttp) {
            // if "kinetic.io.http" is set to true, load http transport
            this.loadProvider(HTTP_TRANSPORT);
        } else if (isHttps) {
            // if "kinetic.io.https" is set to true, load http transport with
            // ssl
            // filter
            this.loadProvider(HTTP_TRANSPORT);
        } else if (this.config.getUseNio()) {
            // if "set ssl as default" flag is set, load ssl transport as
            // "default" service
            if (this.config.getUseSslAsDefault()) {
                this.loadProvider(SSL_NIO_TRANSPORT);
            } else {
                // load tcpnio transport as "default" service
                this.loadProvider(TCP_NIO_TRANSPORT);
            }
        } else {
            // load tcp transport
            this.loadProvider(TCP_TRANSPORT);
        }

        // load ssl transport, default is set to true.
        if (this.config.getStartSsl()) {
            this.loadProvider(SSL_NIO_TRANSPORT);
        }
    }

    public boolean useNio() {
        return this.config.getUseNio();
    }

    public boolean isStartSsl() {
        return (this.sslService != null);
    }

    public Map<Long, ACL> getAclMap() {
        return this.aclmap;
    }

    public Map<Long, Key> getHmacKeyMap() {
        return this.hmacKeyMap;
    }

    @SuppressWarnings("rawtypes")
    public Store getStore() {
        return this.store;
    }

    /**
     * start new instance of store.
     */
    private void initStore() {
        this.store = StoreFactory.createInstance(this.config);
    }

    /**
     * Get server configuration.
     *
     * @return server configuration.
     */
    @Override
    public SimulatorConfiguration getServiceConfiguration() {
        return this.config;
    }

    public void close() {

        if (this.isClosing) {
            return;
        }

        this.isClosing = true;

        tpService.deregister(this);

        // close p2p handler
        if (this.p2pHandler != null) {
            this.p2pHandler.close();
        }

        if (this.nioManager != null) {
            this.nioManager.close();
        }

        // close transport providers
        this.closeTransportServices();

        // close db store
        if (this.store != null) {
            this.store.close();
        }

    }

    /**
     * close transport provider and release associated resources.
     */
    private void closeTransportServices() {
        for (TransportProvider transport : transports) {
            try {
                transport.stop();
                transport.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public static void logBytes(String name, byte[] b) {
        final int MAX_LENGTH = 50; // only log up to MAX_LENGTH bytes
        int length = b.length;
        if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }

        StringBuffer sb = new StringBuffer(name + ": ");
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x ", b[i]));
        }
        logger.fine(sb.toString());
    }

    private static String kineticHome(SimulatorConfiguration config) {
        String defaultHome = System.getProperty("user.home") + File.separator
                + "kinetic";
        String kineticHome = config.getProperty(
                SimulatorConfiguration.KINETIC_HOME, defaultHome);

        File lchome = new File(kineticHome);
        if (!lchome.exists()) {
            lchome.mkdirs();
        }
        return kineticHome;
    }

    @Override
    @SuppressWarnings("unchecked")
    public KineticMessage processRequest(KineticMessage kmreq) {

        Message request = (Message) kmreq.getMessage();

        KineticMessage kmresp = new KineticMessage();
        Message.Builder response = Message.newBuilder();
        kmresp.setMessage(response);

        long userId = request.getCommand().getHeader().getIdentity();
        Key key = this.hmacKeyMap.get(Long.valueOf(userId));

        String kineticHome = kineticHome(config);

        try {
            HeaderOp.checkHeader(kmreq, response, key, clusterVersion);

            if (request.getCommand().getHeader().getMessageType() == MessageType.NOOP) {
                response.getCommandBuilder().getHeaderBuilder()
                .setMessageType(MessageType.NOOP_RESPONSE);
            } else if (request.getCommand().getBody()
                    .hasKeyValue()) {
                KVOp.Op(aclmap, store, kmreq, kmresp);
            } else if (request.getCommand().getBody().hasRange()) {
                RangeOp.operation(store, request, response, aclmap);
            } else if (request.getCommand().getBody()
                    .hasSecurity()) {
                boolean hasPermission = SecurityHandler.checkPermission(
                        request, response, aclmap);
                if (hasPermission) {
                    synchronized (this.hmacKeyMap) {
                        aclmap = SecurityHandler.handleSecurity(request,
                                response, aclmap, kineticHome);
                        this.hmacKeyMap = HmacStore.getHmacKeyMap(aclmap);
                    }
                }

            } else if (request.getCommand().getBody().hasSetup()) {
                boolean hasPermission = SetupHandler.checkPermission(request,
                        response, aclmap);
                if (hasPermission) {
                    SetupInfo setupInfo = SetupHandler.handleSetup(kmreq,
                            response, pin, store, kineticHome);
                    if (setupInfo != null) {
                        this.clusterVersion = setupInfo.getClusterVersion();
                        this.pin = setupInfo.getPin();
                    }
                }
            } else if (request.getCommand().getBody().hasGetLog()) {
                boolean hasPermission = GetLogHandler.checkPermission(request,
                        response, aclmap);
                if (hasPermission) {
                    GetLogHandler.handleGetLog(this, request, response);
                }
            } else if (request.getCommand().getBody()
                    .hasP2POperation()) {

                // check permission
                boolean hasPermission = P2POperationHandler.checkPermission(
                        request, response, aclmap);

                if (hasPermission) {
                    this.p2pHandler.push(aclmap, store, request, response);
                }
            }

        } catch (Exception e) {

            int number = request.getCommand().getHeader()
                    .getMessageType()
                    .getNumber() - 1;

            response.getCommandBuilder().getHeaderBuilder()
            .setMessageType(MessageType.valueOf(number));

            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {

            try {
                response.setHmac(Hmac.calc(kmresp, key));
            } catch (Exception e2) {
                logger.log(Level.WARNING, e2.getMessage(), e2);
            }

            this.addStatisticCounter(request, response.build());
        }

        return kmresp;
    }

    private void addStatisticCounter(Message request, Message response) {

        try {
            MessageType mtype = request.getCommand().getHeader()
                    .getMessageType();

            int inCount = 0;

            int outCount = 0;

            if (request != null) {
                inCount = request.getSerializedSize();
            }

            if (response != null) {
                outCount = response.getSerializedSize();
            }

            switch (mtype) {
            case GET:
                this.operationCounter.addGetCounter();

                this.byteCounter.addGetCounter(inCount);

                this.byteCounter.addGetCounter(outCount);

                break;
            case PUT:
                this.operationCounter.addPutCounter();

                if (request != null) {
                    this.byteCounter.addPutCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addPutCounter(outCount);
                }

                break;
            case DELETE:
                this.operationCounter.addDeleteCounter();

                if (request != null) {
                    this.byteCounter.addDeleteCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addDeleteCounter(outCount);
                }

                break;
            case GETNEXT:
                this.operationCounter.addGetNextCounter();

                if (request != null) {
                    this.byteCounter.addGetNextCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addGetNextCounter(outCount);
                }

                break;
            case GETPREVIOUS:
                this.operationCounter.addGetPreviousCounter();

                if (request != null) {
                    this.byteCounter.addGetPreviousCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addGetPreviousCounter(outCount);
                }

                break;
            case GETKEYRANGE:
                this.operationCounter.addGetKeyRangeCounter();

                if (request != null) {
                    this.byteCounter.addGetKeyRangeCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addGetKeyRangeCounter(outCount);
                }

                break;
            case GETVERSION:
                this.operationCounter.addGetVersionCounter();

                if (request != null) {
                    this.byteCounter.addGetVersionCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addGetVersionCounter(outCount);
                }

                break;
            case SETUP:
                this.operationCounter.addSetupCounter();

                if (request != null) {
                    this.byteCounter.addSetupCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addSetupCounter(outCount);
                }

                break;
            case GETLOG:
                this.operationCounter.addGetLogCounter();

                if (request != null) {
                    this.byteCounter.addGetLogCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addGetLogCounter(outCount);
                }

                break;
            case SECURITY:
                this.operationCounter.addSecurityCounter();

                if (request != null) {
                    this.byteCounter.addSecurityCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addSecurityCounter(outCount);
                }

                break;
            case PEER2PEERPUSH:
                this.operationCounter.addP2PCounter();

                if (request != null) {
                    this.byteCounter.addP2PCounter(inCount);
                }

                if (response != null) {
                    this.byteCounter.addP2PCounter(outCount);
                }

                break;
            default:
                break;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public synchronized NioEventLoopGroupManager getNioEventLoopGroupManager() {

        if (this.nioManager == null) {
            this.nioManager = new NioEventLoopGroupManager(this.config);
        }

        return this.nioManager;
    }

    @Override
    public void execute(Runnable request) {
        tpService.execute(request);
    }

    public Heartbeat getHearBeat() {
        return this.heartbeat;
    }

    public ByteCounter getByteCounter() {
        return this.byteCounter;
    }

    public OperationCounter getOperationCounter() {
        return this.operationCounter;
    }

    /**
     * load transport provider with the specified class name.
     *
     * @param className
     *            the provider class full name to be loaded.
     */
    private void loadProvider(String className) {

        TransportProvider provider = null;

        try {
            // load class
            provider = (TransportProvider) Class.forName(className)
                    .newInstance();

            // add to transport list
            this.transports.add(provider);

            logger.info("transport provider added., class name=" + className);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

    }
}
