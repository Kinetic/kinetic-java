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
package com.seagate.kinetic.simulator.internal.handler;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

/**
 * 
 * Command handler life-cycle and service interface.
 * <p>
 * A operation command handler must provide a no-arg constructor and implements
 * this interface.
 * <p>
 * After a handler is instantiated, the {@link #init(SimulatorEngine)} method is
 * called with the current instance of <code>SiumlatorEngine</code> engine.
 * <p>
 * The handler's {@link #close()} method is called when the simulator is
 * shutdown.
 * 
 * @author chiaming
 *
 */
public interface CommandHandler {

    /**
     * 
     * Initialize this command handler. This is called immediately after a
     * command handler is instantiated.
     * 
     * @param engine
     *            the associated simulator engine.
     */
    public void init(SimulatorEngine engine);

    /**
     * Provide the service for the operation command.
     * 
     * @param request
     *            the request message.
     * @param response
     *            the response message.
     * @throws ServiceException
     *             if any internal error occurred.
     */
    public void processRequest(KineticMessage request, KineticMessage response)
            throws ServiceException;

    /**
     * Close the command handler. All resources associated with this handler are
     * released.
     * <p>
     * This is invoked when the simulator is shutdown.
     */
    public void close();
}
