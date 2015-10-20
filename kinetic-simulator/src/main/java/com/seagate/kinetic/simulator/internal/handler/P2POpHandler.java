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
