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
