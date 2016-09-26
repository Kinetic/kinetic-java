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
package com.seagate.kinetic.common.lib;

import com.google.protobuf.MessageOrBuilder;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.CommandOrBuilder;
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
	 * @param kineticMessage
	 *            the protocol message to be translated.
	 * @return a readable string format of a kinetic protocol message on the
	 *         wire.
	 */
	public static String toString(KineticMessage kineticMessage) {

	    int vLength = 0;
	    
	    if (kineticMessage.getValue() != null) {
	        vLength = kineticMessage.getValue().length;
	    }
	    
	    Message message = null;
	    MessageOrBuilder messageOrBuilder = kineticMessage.getMessage();
	    
	    if (messageOrBuilder instanceof Message) {
	        message = (Message) messageOrBuilder;
	    } else {
	        message = ((Message.Builder) messageOrBuilder).build();
	    }
	    
	    Command command = null;
	    CommandOrBuilder commandOrBuilder = kineticMessage.getCommand();
	    if (commandOrBuilder instanceof Command) {
	        command = (Command) commandOrBuilder;
	    } else {
	        command = ((Command.Builder) commandOrBuilder).build();
	    }
	    
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
				"[" + message + "]" + "\n" +
				"command: [" + command + "]";

		// value size
		if (vLength > 0) {
			printMsg = printMsg + "\n[vContent (not printed) length=" + vLength
					+ "]";
		}

		printMsg = printMsg + "\n}";

		return printMsg;
	}

}
