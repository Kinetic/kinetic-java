// Do NOT modify or remove this copyright and confidentiality notice!
//
// Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
//
// The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
// Portions are also trade secret. Any use, duplication, derivation, distribution
// or disclosure of this code, for any reason, not expressly authorized is
// prohibited. All other rights are expressly reserved by Seagate Technology, LLC.

package com.seagate.kinetic.simulator.lib;

import java.security.Key;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Status;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;

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
		}

	}
}
