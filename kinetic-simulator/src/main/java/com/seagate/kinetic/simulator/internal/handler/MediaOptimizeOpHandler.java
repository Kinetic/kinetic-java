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
import com.seagate.kinetic.simulator.internal.BackGroundOpHandler;

public class MediaOptimizeOpHandler extends CommandHandlerBase implements
        CommandHandler {

    @Override
    public void processRequest(KineticMessage request, KineticMessage response)
            throws ServiceException {

        try {
            BackGroundOpHandler.mediaOptimize(request, response, engine);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

}
