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
import com.seagate.kinetic.simulator.internal.p2p.P2POperationHandler;

public class P2POpHandler extends CommandHandlerBase implements
        CommandHandler {

    private P2POperationHandler p2pHandler = new P2POperationHandler();

    @SuppressWarnings("unchecked")
    @Override
    public void processRequest(KineticMessage request, KineticMessage response)
            throws ServiceException {

        boolean hasPermission = P2POperationHandler.checkPermission(request,
                response, engine.getAclMap());

        if (hasPermission) {
            this.p2pHandler.push(engine.getAclMap(), engine.getStore(),
                    request, response);
        }
    }

    @Override
    public void close() {
        if (this.p2pHandler != null) {
            this.p2pHandler.close();
        }
    }

}
