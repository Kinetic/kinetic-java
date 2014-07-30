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
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Status;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;
import com.seagate.kinetic.simulator.internal.StatefulMessage;

class HeaderException extends Exception {
	private static final long serialVersionUID = 5201751340412081922L;

	Message.Status.StatusCode status;

	HeaderException(Message.Status.StatusCode status, String s) {
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

	public static void checkHeader(KineticMessage km, Message.Builder respond,
			Key key, long clusterVersion) throws HeaderException {

		LOG.fine("Header processing");

		Message request = (Message) km.getMessage();

		try {

			if (!request.getCommand().hasHeader()) {
				throw new HeaderException(StatusCode.HEADER_REQUIRED,
						"no header");
			}

			Message.Header in = request.getCommand().getHeader();

			// set ack sequence
			respond.getCommandBuilder().getHeaderBuilder()
			.setAckSequence(in.getSequence());

			// check hmac
			checkHmac(km, key);

			if (in.getClusterVersion() != clusterVersion) {
			    
			    //set cluster version in response message
			    respond.getCommandBuilder().getHeaderBuilder().setClusterVersion(clusterVersion);
			    
				throw new HeaderException(
						Message.Status.StatusCode.VERSION_FAILURE,
						"CLUSTER_VERSION_FAILURE: Simulator cluster version is "
								+ clusterVersion
								+ "; Received request cluster version is "
								+ in.getClusterVersion());
			}

			// set status code
			respond.getCommandBuilder().getStatusBuilder()
			.setCode(Status.StatusCode.SUCCESS);
		
			LOG.fine("Header processed successfully. status code="
					+ respond.getCommand().getStatus().getCode());

		} catch (HeaderException he) {
			LOG.fine("Header Processing Failed: " + he.getMessage());

			respond.getCommandBuilder().getStatusBuilder().setCode(he.status);
			respond.getCommandBuilder().getStatusBuilder()
			.setStatusMessage(he.getMessage());

			throw he;
		} catch (Exception ex) {
			respond.getCommandBuilder().getStatusBuilder()
			.setCode(Status.StatusCode.INTERNAL_ERROR);
			respond.getCommandBuilder().getStatusBuilder()
			.setStatusMessage(ex.getMessage());

			throw new HeaderException(StatusCode.INTERNAL_ERROR,
					ex.getMessage());
		} finally {
		    
            try {
                // set connection Id if necessary
                if (km instanceof StatefulMessage) {
                    long cid = ((StatefulMessage) km).getConnectionId();
                    respond.getCommandBuilder().getHeaderBuilder()
                            .setConnectionID(cid);
                }
            } catch (Exception e) {
                LOG.warning(e.getMessage());
            }
		}

	}
}
