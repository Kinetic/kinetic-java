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
package kinetic.client;

import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * 
 * A ConnectionListener is used to receive asynchronous messages from a
 * connected Kinetic service. Such as unsolicitated status message.
 * <p>
 * Upon received an unsolicitated status message, the Java runtime library calls
 * the listener's <code>onMessage</code> if registered.
 * 
 * @author chiaming
 *
 */
public interface ConnectionListener {

    /**
     * Upon received an unsolicitated status message, the Java runtime library
     * calls the listener if registered.
     * <p>
     * The thread execution from the Java client runtime library is serialized
     * such that each sub-sequential messages are delivered only after the
     * previous onMessage call returned.
     * 
     * @param message
     *            the asynchronous message sent from the connected Kinetic
     *            service.
     */
    public void onMessage(KineticMessage message);

}
