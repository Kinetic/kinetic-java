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
package com.seagate.kinetic.simulator.internal;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.heartbeat.message.ByteCounter;
import com.seagate.kinetic.heartbeat.message.OperationCounter;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Capacity;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Configuration;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Limits;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Statistics;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Temperature;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Utilization;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.simulator.utility.CapacityUtil;
import com.seagate.kinetic.simulator.utility.ConfigurationUtil;
import com.seagate.kinetic.simulator.utility.LimitsUtil;
import com.seagate.kinetic.simulator.utility.TemperatureUtil;
import com.seagate.kinetic.simulator.utility.UtilizationUtil;

/**
 *
 * GetLog handler
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
public class GetLogHandler {
    
    public static final String SIMULATOR_DUMMY_LOG_NAME = "com.seagate.simulator:dummy";
    
    public static boolean checkPermission(KineticMessage request,
            KineticMessage respond, Map<Long, ACL> currentMap) {
        
        boolean hasPermission = false;

        Command.Builder respCommandBuilder = (Command.Builder) respond.getCommand();
        
        // set reply type
        respCommandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.GETLOG_RESPONSE);
        
        // set ack sequence
        respCommandBuilder.getHeaderBuilder()
        .setAckSequence(request.getCommand().getHeader().getSequence());

        // check if has permission to set security
        if (currentMap == null) {
            hasPermission = true;
        } else {
            try {
                Authorizer.checkPermission(currentMap, request.getMessage().getHmacAuth().getIdentity(), Permission.GETLOG);
                
                hasPermission = true;
            } catch (KVSecurityException e) {
                respCommandBuilder.getStatusBuilder()
                .setCode(StatusCode.NOT_AUTHORIZED);
                respCommandBuilder.getStatusBuilder()
                .setStatusMessage(e.getMessage());
            }
        }

        return hasPermission;
    }

    public static void handleGetLog(SimulatorEngine engine, KineticMessage request, KineticMessage kmresp) throws UnknownHostException, UnsupportedEncodingException {
        
        Command.Builder respCommandBuilder = (Command.Builder) kmresp.getCommand();
        
        List<Type> types = request.getCommand().getBody().getGetLog()
                .getTypesList();

        Command.GetLog.Builder getLog = respCommandBuilder
                .getBodyBuilder().getGetLogBuilder();

        for (Type type : types) {
            getLog.addTypes(type);

            switch (type) {
            case CAPACITIES:
                Capacity capacity = CapacityUtil.getCapacity(engine);
                getLog.setCapacity(capacity);
                break;
            case UTILIZATIONS:
                List<Utilization> utilizations = UtilizationUtil
                .getUtilization();
                for (Utilization utilization : utilizations) {
                    getLog.addUtilizations(utilization);
                }
                break;
            case TEMPERATURES:
                List<Temperature> temperatures = TemperatureUtil
                .getTemperature();
                for (Temperature temperature : temperatures) {
                    getLog.addTemperatures(temperature);
                }
                break;
            case CONFIGURATION:
                Configuration configuration = ConfigurationUtil.getConfiguration(engine);
                getLog.setConfiguration(configuration);
                break;

            case MESSAGES:
                getLog.setMessages(ByteString.copyFrom("Message from simulator".getBytes()));
                break;

            case STATISTICS:

                OperationCounter opCounter = engine.getOperationCounter();
                ByteCounter byteCounter = engine.getByteCounter();

                Statistics.Builder statisticsPut = Statistics.newBuilder();
                statisticsPut.setCount(opCounter.getPutCounter());
                statisticsPut.setBytes(byteCounter.getPutCounter());
                statisticsPut.setMessageType(MessageType.PUT);
                getLog.addStatistics(statisticsPut.build());

                Statistics.Builder statisticsGet = Statistics.newBuilder();
                statisticsGet.setCount(opCounter.getGetCounter());
                statisticsGet.setBytes(byteCounter.getGetCounter());
                statisticsGet.setMessageType(MessageType.GET);
                getLog.addStatistics(statisticsGet.build());

                Statistics.Builder statisticsDelete = Statistics.newBuilder();
                statisticsDelete.setCount(opCounter.getDeleteCounter());
                statisticsDelete.setBytes(byteCounter.getDeleteCounter());
                statisticsDelete.setMessageType(MessageType.DELETE);
                getLog.addStatistics(statisticsDelete.build());

                Statistics.Builder statisticsGetPrevious = Statistics.newBuilder();
                statisticsGetPrevious.setCount(opCounter.getGetPreviousCounter());
                statisticsGetPrevious.setBytes(byteCounter.getGetPreviousCounter());
                statisticsGetPrevious.setMessageType(MessageType.GETPREVIOUS);
                getLog.addStatistics(statisticsGetPrevious.build());

                Statistics.Builder statisticsGetNext = Statistics.newBuilder();
                statisticsGetNext.setCount(opCounter.getGetNextCounter());
                statisticsGetNext.setBytes(byteCounter.getGetNextCounter());
                statisticsGetNext.setMessageType(MessageType.GETNEXT);
                getLog.addStatistics(statisticsGetNext.build());

                Statistics.Builder statisticsGetKeyRange = Statistics.newBuilder();
                statisticsGetKeyRange.setCount(opCounter.getGetKeyRangeCounter());
                statisticsGetKeyRange.setBytes(byteCounter.getGetKeyRangeCounter());
                statisticsGetKeyRange.setMessageType(MessageType.GETKEYRANGE);
                getLog.addStatistics(statisticsGetKeyRange.build());

                Statistics.Builder statisticsGetVersion = Statistics.newBuilder();
                statisticsGetVersion.setCount(opCounter.getGetVersionCounter());
                statisticsGetVersion.setBytes(byteCounter.getGetVersionCounter());
                statisticsGetVersion.setMessageType(MessageType.GETVERSION);
                getLog.addStatistics(statisticsGetVersion.build());

                Statistics.Builder statisticsGetSecurity = Statistics.newBuilder();
                statisticsGetSecurity.setCount(opCounter.getSecurityCounter());
                statisticsGetSecurity.setBytes(byteCounter.getSecurityCounter());
                statisticsGetSecurity.setMessageType(MessageType.SECURITY);
                getLog.addStatistics(statisticsGetSecurity.build());

                Statistics.Builder statisticsGetSetup = Statistics.newBuilder();
                statisticsGetSetup.setCount(opCounter.getSetupCounter());
                statisticsGetSetup.setBytes(byteCounter.getSetupCounter());
                statisticsGetSetup.setMessageType(MessageType.SETUP);
                getLog.addStatistics(statisticsGetSetup.build());

                Statistics.Builder statisticsGetLog = Statistics.newBuilder();
                statisticsGetLog.setCount(opCounter.getGetLogCounter());
                statisticsGetLog.setBytes(byteCounter.getGetLogCounter());
                statisticsGetLog.setMessageType(MessageType.GETLOG);
                getLog.addStatistics(statisticsGetLog.build());

                Statistics.Builder statisticsGetP2P = Statistics.newBuilder();
                statisticsGetP2P.setCount(opCounter.getP2PCounter());
                statisticsGetP2P.setBytes(byteCounter.getP2PCounter());
                statisticsGetP2P.setMessageType(MessageType.PEER2PEERPUSH);
                getLog.addStatistics(statisticsGetP2P.build());

                break;

            case LIMITS:
                Limits limits = LimitsUtil.getLimits(engine.getServiceConfiguration());
                getLog.setLimits(limits);
                break;
                
            case DEVICE:
                // get if name is set
                boolean hasName = request.getCommand().getBody().getGetLog().getDevice().hasName();
                
                ByteString bs = null;
                if (hasName) {
                    //get name
                    bs = request.getCommand().getBody().getGetLog().getDevice().getName();
                    //check if this is the supported name
                    if (SIMULATOR_DUMMY_LOG_NAME.equals(bs.toStringUtf8())) {
                        byte[] dummyValue = new byte[1024 * 1024];
                        Arrays.fill(dummyValue, (byte) 0);
                        
                        kmresp.setValue(dummyValue);
                    } else {
                        respCommandBuilder.getStatusBuilder()
                        .setCode(StatusCode.NOT_FOUND);
                        respCommandBuilder.getStatusBuilder()
                        .setStatusMessage("No device log for the specified name: " + bs.toStringUtf8());   
                    }
                } else {
                    respCommandBuilder.getStatusBuilder()
                    .setCode(StatusCode.NOT_FOUND);
                    respCommandBuilder.getStatusBuilder()
                    .setStatusMessage("Missing device log name.");  
                }
                
            default:
                ;
            }
        }
    }
}
