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
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

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
            Key key, SimulatorEngine engine) throws HeaderException {

		LOG.fine("Header processing");
		
		Command.Builder respCommandBuilder = (Command.Builder) kmresp.getCommand();

		try {

			if (!km.getCommand().hasHeader()) {
				throw new HeaderException(StatusCode.HEADER_REQUIRED,
						"no header");
			}

            if (km.getIsInvalidRequest()) {
                throw new HeaderException(Status.StatusCode.INVALID_REQUEST,
                        km.getErrorMessage());
            }

			Command.Header in = km.getCommand().getHeader();

			// set ack sequence
			respCommandBuilder.getHeaderBuilder()
			.setAckSequence(in.getSequence());

            if (km.getMessage().getAuthType() == AuthType.PINAUTH) {
                // sanity check only
                if (in.getMessageType() != MessageType.PINOP) {
                    throw new HeaderException(
                            Status.StatusCode.INVALID_REQUEST,
                            "Invalid message type for pin operation.");
                }
            } else {
                // check hmac
                checkHmac(km, key);

                if (in.getClusterVersion() != engine.getClusterVersion()) {

                    // set cluster version in response message
                    respCommandBuilder.getHeaderBuilder().setClusterVersion(
                            engine.getClusterVersion());

                    throw new HeaderException(
                            Status.StatusCode.VERSION_FAILURE,
                            "CLUSTER_VERSION_FAILURE: Simulator cluster version is "
                                    + engine.getClusterVersion()
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

                // set response message type
                int number = km.getCommand().getHeader().getMessageType()
                        .getNumber() - 1;

                respCommandBuilder.getHeaderBuilder().setMessageType(
                        MessageType.valueOf(number));

            } catch (Exception e) {
                LOG.warning(e.getMessage());
            }
		}

	}
}
