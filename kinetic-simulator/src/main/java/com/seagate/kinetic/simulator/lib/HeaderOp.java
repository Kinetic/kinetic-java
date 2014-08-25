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

package com.seagate.kinetic.simulator.lib;

import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Status;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

class HeaderException extends Exception {
	private static final long serialVersionUID = 5201751340412081922L;

	Status.StatusCode status;

	HeaderException(Status.StatusCode status, String s) {
		super(s);
		this.status = status;
	}
}

public class HeaderOp {

	private final static Logger LOG = MyLogger.get();

	/**
	 * Hmac check.
	 *
	 * @param m
	 * @param key
	 * @return
	 * @throws HeaderException
	 */
	private static boolean checkHmac(KineticMessage km, Key key)
			throws HeaderException {

		try {
			if (!Hmac.check(km, key)) {
				throw new HeaderException(StatusCode.HMAC_FAILURE,
						"HMAC did not compare");
			} else {
				LOG.fine("validated hmac successfully.");
			}
		} catch (HeaderException he) {
			throw he;
		} catch (Exception e) {
			throw new HeaderException(StatusCode.INTERNAL_ERROR,
					"Internal error: " + e.getMessage());
		}

		return true;

	}

	private HeaderOp() {
	}

	public static void checkHeader(KineticMessage km, KineticMessage kmresp,
			Key key, long clusterVersion) throws HeaderException {

		LOG.fine("Header processing");
		
		Command.Builder respCommandBuilder = (Command.Builder) kmresp.getCommand();

		try {

			if (!km.getCommand().hasHeader()) {
				throw new HeaderException(StatusCode.HEADER_REQUIRED,
						"no header");
			}

			Command.Header in = km.getCommand().getHeader();

			// set ack sequence
			respCommandBuilder.getHeaderBuilder()
			.setAckSequence(in.getSequence());

            if (km.getMessage().hasPinAuth()) {
                // sanity check only
                if (in.getMessageType() != MessageType.PINOP) {
                    throw new HeaderException(
                            Status.StatusCode.INVALID_REQUEST,
                            "Invalid message type for pin operation.");
                }
            } else {
                // check hmac
                checkHmac(km, key);

                if (in.getClusterVersion() != clusterVersion) {

                    // set cluster version in response message
                    respCommandBuilder.getHeaderBuilder().setClusterVersion(
                            clusterVersion);

                    throw new HeaderException(
                            Status.StatusCode.VERSION_FAILURE,
                            "CLUSTER_VERSION_FAILURE: Simulator cluster version is "
                                    + clusterVersion
                                    + "; Received request cluster version is "
                                    + in.getClusterVersion());
                }
            }
			
			// set status code
			respCommandBuilder.getStatusBuilder()
			.setCode(Status.StatusCode.SUCCESS);
		
			LOG.fine("Header processed successfully. status code="
					+ respCommandBuilder.getStatus().getCode());

		} catch (HeaderException he) {
			LOG.fine("Header Processing Failed: " + he.getMessage());

			respCommandBuilder.getStatusBuilder().setCode(he.status);
			respCommandBuilder.getStatusBuilder()
			.setStatusMessage(he.getMessage());

			throw he;
		} catch (Exception ex) {
		    
		    LOG.log(Level.WARNING, ex.getMessage(), ex);
		    respCommandBuilder.getStatusBuilder()
			.setCode(Status.StatusCode.INTERNAL_ERROR);
		    respCommandBuilder.getStatusBuilder()
			.setStatusMessage(ex.getMessage());

			throw new HeaderException(StatusCode.INTERNAL_ERROR,
					ex.getMessage());
		} finally {
		    
            try {
                // set connection Id in the response message
                long cid = km.getCommand().getHeader().getConnectionID();
                respCommandBuilder.getHeaderBuilder().setConnectionID(cid);

            } catch (Exception e) {
                LOG.warning(e.getMessage());
            }
		}

	}
}
