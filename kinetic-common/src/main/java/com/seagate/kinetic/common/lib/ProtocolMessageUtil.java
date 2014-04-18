/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.common.lib;

import com.seagate.kinetic.proto.Kinetic.Message;

/**
 * Utility to translate the kinetic protocol message into a readable string
 * format.
 * 
 * @author chiaming
 */
public class ProtocolMessageUtil {

	/**
	 * Translate a kinetic protocol message into a readable string format.
	 * <p>
	 * 
	 * @param message
	 *            the protocol message to be translated.
	 * 
	 * @param vLength
	 *            the attached value length.
	 * 
	 * @return a readable string format of a kinetic protocol message on the
	 *         wire.
	 */
	public static String toString(Message message, int vLength) {

		/**
		 * This method is intended to be called for debugging purposes. The
		 * String type for printMsg is used intentionally for readability.
		 */
		String printMsg =
				// 9 bytes header
				"Kinetic Message: { \n" + "[magic: " + 'F' + "\npLength: "
				+ message.getSerializedSize() + "\nvLength: " + vLength
				+ "\n] \n" +

				// readable proto message
				"[" + message + "]";

		// value size
		if (vLength > 0) {
			printMsg = printMsg + "\n[vContent (not printed) length=" + vLength
					+ "]";
		}

		printMsg = printMsg + "\n}";

		return printMsg;
	}

}
