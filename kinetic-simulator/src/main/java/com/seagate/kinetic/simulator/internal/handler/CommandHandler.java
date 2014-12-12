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
