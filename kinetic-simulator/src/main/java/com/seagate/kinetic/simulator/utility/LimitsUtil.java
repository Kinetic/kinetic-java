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
package com.seagate.kinetic.simulator.utility;

import java.net.UnknownHostException;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Limits;

public abstract class LimitsUtil {

    @SuppressWarnings("static-access")
    public static Limits getLimits(SimulatorConfiguration config)
            throws UnknownHostException {
        Limits.Builder limits = Limits.newBuilder();

        limits.setMaxKeySize(config.getMaxSupportedKeySize());
        limits.setMaxValueSize(config.getMaxSupportedValueSize());
        limits.setMaxVersionSize(config.getMaxSupportedVersionSize());
        limits.setMaxTagSize(config.getMaxSupportedTagSize());
        limits.setMaxConnections(config.getMaxConnections());

        limits.setMaxOutstandingReadRequests(config
                .getMaxOutstandingReadRequests());
        limits.setMaxOutstandingWriteRequests(config
                .getMaxOutstandingWriteRequests());
        limits.setMaxMessageSize(config.getMaxMessageSize());
        limits.setMaxKeyRangeCount(config.getMaxSupportedKeyRangeSize());
        
        limits.setMaxIdentityCount(config.getMaxIdentityCount());

        limits.setMaxBatchCountPerDevice(config.getMaxOutstandingBatches());

        limits.setMaxOperationCountPerBatch(config.getMaxCommandsPerBatch());

        return limits.build();
    }
}
