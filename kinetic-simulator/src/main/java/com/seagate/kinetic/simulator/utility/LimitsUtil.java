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
