/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
