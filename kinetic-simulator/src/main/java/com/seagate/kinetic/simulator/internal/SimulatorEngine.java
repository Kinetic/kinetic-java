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
package com.seagate.kinetic.simulator.internal;

import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.heartbeat.message.ByteCounter;
import com.seagate.kinetic.heartbeat.message.OperationCounter;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Configuration;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Limits;
import com.seagate.kinetic.proto.Kinetic.Command.PinOperation.PinOpType;

import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Local;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
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
import com.seagate.kinetic.simulator.utility.ConfigurationUtil;
import com.seagate.kinetic.simulator.utility.LimitsUtil;

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
    
    // protocol version
    public static final String PROTOCOL_VERSION = 
            Local.getDefaultInstance().getProtocolVersion();

    private SimulatorConfiguration config = null;

    @SuppressWarnings("rawtypes")
    private Store store = null;

    private final ArrayList<TransportProvider> transports = new ArrayList<TransportProvider>();

    private final TransportProvider sslService = null;

    // ack map
    private Map<Long, ACL> aclmap = null;
    
    private SecurityPin securityPin = new SecurityPin();

    private Map<Long, Key> hmacKeyMap = null;

    private long clusterVersion = 0;

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
    
    private String kineticHome = null;

    // resource for all the simulator instances
    private static ThreadPoolService tpService = new ThreadPoolService();

    // shutdown hook
    private static SimulatorShutdownHook shutdownHook = new SimulatorShutdownHook(
            tpService);
    
    //connection map
    private static ConcurrentHashMap<Object, ConnectionInfo> connectionMap = new ConcurrentHashMap<Object, ConnectionInfo>();
    
    // last connection Id. 
    private static long lastConnectionId = System.currentTimeMillis();
    
    private volatile boolean deviceLocked = false;
    
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
            
            // calculate my home
            kineticHome = kineticHome(config);
            
            // load acl and pins
            SecurityHandler.loadACL(this);
            
            // load set up 
            SetupHandler.loadSetup(this);

            // initialize db store
            this.initStore();

            // init network io service
            this.initIoService();
            
            logger.info("simulator protocol version = " + SimulatorConfiguration.getProtocolVersion());

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
    
    public void setAclMap ( Map<Long, ACL> aclmap) {
        this.aclmap = aclmap;
    }
    
    public void setHmacKeyMap(Map<Long, Key> hmacKeyMap) {
        this.hmacKeyMap = hmacKeyMap;
    }
        
    public Map<Long, Key> getHmacKeyMap() {
        return this.hmacKeyMap;
    }

    @SuppressWarnings("rawtypes")
    public Store getStore() {
        return this.store;
    }
    
    public void setClusterVersion (long cversion) {
        this.clusterVersion = cversion;
    }
    
    public SecurityPin getSecurityPin() {
        return this.securityPin;
    }
    
    public String getKineticHome() {
        return this.kineticHome;
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
        
        // create response message
        KineticMessage kmresp = createKineticMessageWithBuilder();
        
        // get command builder
        Command.Builder commandBuilder = (Command.Builder) kmresp.getCommand();
        
        // get message builder
        Message.Builder messageBuilder = (Message.Builder) kmresp.getMessage();
        
        // get user identity for this message
        long userId = kmreq.getMessage().getHmacAuth().getIdentity();
        
        // get user key
        Key key = this.hmacKeyMap.get(Long.valueOf(userId));
        
        // moved to top as instance variable
        //String kineticHome = kineticHome(config);

        try {
            
            HeaderOp.checkHeader(kmreq, kmresp, key, clusterVersion);
            
            checkDeviceLocked (kmreq, kmresp);
            
            if (kmreq.getMessage().getAuthType() == AuthType.PINAUTH) {
                //perform pin op
                PinOperationHandler.handleOperation(kmreq, kmresp, this); 
            } else if (kmreq.getCommand().getHeader().getMessageType() == MessageType.FLUSHALLDATA) {
                commandBuilder.getHeaderBuilder()
                .setMessageType(MessageType.FLUSHALLDATA_RESPONSE);
                logger.warning("received flush data command, this is a no op on simulator at this time ...");
            } else if (kmreq.getCommand().getHeader().getMessageType() == MessageType.NOOP) {
                commandBuilder.getHeaderBuilder()
                .setMessageType(MessageType.NOOP_RESPONSE);
            } else if (kmreq.getCommand().getBody()
                    .hasKeyValue()) {
                KVOp.Op(aclmap, store, kmreq, kmresp);
            } else if (kmreq.getCommand().getBody().hasRange()) {
                RangeOp.operation(store, kmreq, kmresp, aclmap);
            } else if (kmreq.getCommand().getBody()
                    .hasSecurity()) {
                boolean hasPermission = SecurityHandler.checkPermission(
                        kmreq, kmresp, aclmap);
                if (hasPermission) {
                    synchronized (this.hmacKeyMap) {
                        SecurityHandler.handleSecurity(kmreq,
                                kmresp, this);
                        this.hmacKeyMap = HmacStore.getHmacKeyMap(aclmap);
                    }
                }

            } else if (kmreq.getCommand().getBody().hasSetup()) {
                boolean hasPermission = SetupHandler.checkPermission(kmreq,
                        kmresp, aclmap);
                if (hasPermission) {
                    SetupInfo setupInfo = SetupHandler.handleSetup(kmreq,
                            kmresp, store, kineticHome);
                    if (setupInfo != null) {
                        this.clusterVersion = setupInfo.getClusterVersion();
                        //this.pin = setupInfo.getPin();
                    }
                }
            } else if (kmreq.getCommand().getBody().hasGetLog()) {
                boolean hasPermission = GetLogHandler.checkPermission(kmreq,
                        kmresp, aclmap);
                if (hasPermission) {
                    GetLogHandler.handleGetLog(this, kmreq, kmresp);
                }
            } else if (kmreq.getCommand().getBody()
                    .hasP2POperation()) {

                // check permission
                boolean hasPermission = P2POperationHandler.checkPermission(
                        kmreq, kmresp, aclmap);

                if (hasPermission) {
                    this.p2pHandler.push(aclmap, store, kmreq, kmresp);
                }
            } else if (kmreq.getCommand().getBody().hasBackgroundOperation()) {
                BackGroundOpHandler.handleOperation(kmreq, kmresp, this);
            }
        } catch (DeviceLockedException ire) {
            
            int number = kmreq.getCommand().getHeader()
                    .getMessageType()
                    .getNumber() - 1;

            commandBuilder.getHeaderBuilder()
            .setMessageType(MessageType.valueOf(number));
           
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.DEVICE_LOCKED);
            
            commandBuilder.getStatusBuilder().setStatusMessage("Device is locked");
            
        } catch (Exception e) {
            
            logger.log(Level.WARNING, e.getMessage(), e);

            int number = kmreq.getCommand().getHeader()
                    .getMessageType()
                    .getNumber() - 1;

            commandBuilder.getHeaderBuilder()
            .setMessageType(MessageType.valueOf(number));

            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {

            try {
                // get command byte stirng
                ByteString commandByteString = commandBuilder.build()
                        .toByteString();

                // get command byte[]
                byte[] commandByte = commandByteString.toByteArray();

                // require Hmac calculation ?
                if (kmreq.getMessage().getAuthType() == AuthType.HMACAUTH) {
                    
                    // calculate hmac
                    ByteString hmac = Hmac.calc(commandByte, key);

                    // set identity
                    messageBuilder.getHmacAuthBuilder().setIdentity(userId);

                    // set hmac
                    messageBuilder.getHmacAuthBuilder().setHmac(hmac);
                }

                // set command bytes
                messageBuilder.setCommandBytes(commandByteString);
            } catch (Exception e2) {
                logger.log(Level.WARNING, e2.getMessage(), e2);
            }

            this.addStatisticCounter(kmreq, kmresp);
        }

        return kmresp;
    }
    
    private void checkDeviceLocked(KineticMessage kmreq, KineticMessage kmresp)
            throws DeviceLockedException {

        if (this.deviceLocked == false) {
            return;
        }

        PinOpType pinOpType = kmreq.getCommand().getBody().getPinOp()
                .getPinOpType();

        if (pinOpType != PinOpType.UNLOCK_PINOP
                && pinOpType != PinOpType.LOCK_PINOP) {
            throw new DeviceLockedException ();
        }

    }

    private void addStatisticCounter(KineticMessage kmreq, KineticMessage kmresp) {

        try {
            
            Message request = (Message) kmreq.getMessage();
            
            Message response = ((Message.Builder) kmresp.getMessage()).build();
            
            MessageType mtype = kmreq.getCommand().getHeader()
                    .getMessageType();

            int inCount = 0;

            int outCount = 0;

            if (request != null) {
                inCount = request.getSerializedSize();
                //add in-bound value byte count
                if (kmreq.getValue() != null) {
                    inCount = inCount + kmreq.getValue().length;
                }
            }
            
            if (response != null) {
                outCount = response.getSerializedSize();
              //add out-bound value byte count
                if (kmresp.getValue() != null) {
                    outCount = outCount + kmresp.getValue().length;
                }
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
    
    /**
     * put connection/connection info into the connection map.
     *  
     * @param connection the key for the entry
     * @param cinfo value of the entry
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public static ConnectionInfo putConnectionInfo (Object connection, ConnectionInfo cinfo) {
        return connectionMap.put(connection, cinfo);
    }
    
    /**
     * Get connection info based on the specified key.
     * 
     * @param connection key to get the connection info.
     * 
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    public static ConnectionInfo getConnectionInfo (Object connection) {
        return connectionMap.get(connection);
    }
    
    /**
     * remove the value of the specified key.
     * @param connection the key od the entry that needs to be removed
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public static ConnectionInfo removeConnectionInfo (Object connection) {
        return connectionMap.remove (connection);
    }
    
    /**
     * register a new connection. A new connection info instance is created and associated with the connection.
     * 
     * @param connection the new connection to be added to the connection map.
     * 
     * @return the connection info instance associated with the connection.
     */
    public ConnectionInfo registerNewConnection(ChannelHandlerContext ctx) {
        ConnectionInfo info = newConnectionInfo();
        putConnectionInfo(ctx, info);

        KineticMessage km = new KineticMessage();

        Message.Builder mb = Message.newBuilder();
        mb.setAuthType(AuthType.UNSOLICITEDSTATUS);

        Command.Builder cb = Command.newBuilder();

        // connection id
        cb.getHeaderBuilder().setConnectionID(info.getConnectionId());

        // cluster version
        cb.getHeaderBuilder().setClusterVersion(this.clusterVersion);

        // configurations

        try {
            Configuration configuration = ConfigurationUtil
                    .getConfiguration(this.config);
            cb.getBodyBuilder().getGetLogBuilder().getConfigurationBuilder()
                    .mergeFrom(configuration);
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        // cb.getBodyBuilder().getGetLogBuilder().getConfigurationBuilder().setProtocolVersion(PROTOCOL_VERSION);
        // cb.getBodyBuilder().getGetLogBuilder().getConfigurationBuilder().setCompilationDate(ConfigurationUtil.COMPILATION_DATE);
        // cb.getBodyBuilder().getGetLogBuilder().getConfigurationBuilder().setModel(ConfigurationUtil.MODEL);
        // cb.getBodyBuilder().getGetLogBuilder().getConfigurationBuilder().setVersion(SimulatorConfiguration.getSimulatorVersion());
        // cb.getBodyBuilder().getGetLogBuilder().getConfigurationBuilder().setSerialNumber(ByteString.copyFrom(ConfigurationUtil.SERIAL_NUMBER));
        //
        // limits
        try {
            Limits limits = LimitsUtil.getLimits(this.config);
            cb.getBodyBuilder().getGetLogBuilder().getLimitsBuilder()
                    .mergeFrom(limits);
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        // status
        cb.getStatusBuilder().setCode(StatusCode.SUCCESS);

        mb.setCommandBytes(cb.build().toByteString());

        km.setMessage(mb);
        km.setCommand(cb);

        ctx.writeAndFlush(km);

        logger.info("***** connection registered., sent UNSOLICITEDSTATUS with cid = "
                + info.getConnectionId());

        return info;
    }
    
    /**
     * instantiate a new connection info object with connection id set.
     * 
     * @return a new connection info object with connection id set
     */
    public static ConnectionInfo newConnectionInfo() {
        
        ConnectionInfo info = new ConnectionInfo();
        
        info.setConnectionId(getNextConnectionId());
        
        return info;
    }
    
    /**
     * Get next available unique connection id based on timestamp. The Id is guarantees to be unique for simulators 
     * running within the same JVM.
     * 
     * @return next available unique connection ID based on timestamp.
     */
    private static synchronized long getNextConnectionId() {
        
        // current time
        long id = System.currentTimeMillis();
        
        // check if duplicate.  enforce so that it is later than the time that this JVM is started.
        if (id <= lastConnectionId) {
            // increase one so its unique.
            id = lastConnectionId + 1;
        }
        
        // set last connection id
        lastConnectionId = id;
        
        return id;
    }
    
    /**
     * create an internal message with empty builder message.
     *
     * @return an internal message with empty builder message
     */
    public static KineticMessage createKineticMessageWithBuilder() {

        // new instance of internal message
        KineticMessage kineticMessage = new KineticMessage();

        // new builder message
        Message.Builder message = Message.newBuilder();

        // set to im
        kineticMessage.setMessage(message);
        
        //set hmac auth type
        message.setAuthType(AuthType.HMACAUTH);
        
        // create command builder
        Command.Builder commandBuilder = Command.newBuilder();
        
        // set command
        kineticMessage.setCommand(commandBuilder);
        
        return kineticMessage;
    }
    
    /**
     * Lock/unlock the device/simulator
     * @param flag
     */
    public void setDeviceLocked (boolean flag) {
        this.deviceLocked = flag;
    }
    
    /**
     * Get device lock flag.
     * 
     * @return true if locked. Otherwise, return false.
     */
    public boolean getDeviceLocked() {
        return this.deviceLocked;
    }
    
}
