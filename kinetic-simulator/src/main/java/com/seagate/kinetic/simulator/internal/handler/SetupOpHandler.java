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
import com.seagate.kinetic.simulator.internal.SetupHandler;
import com.seagate.kinetic.simulator.lib.SetupInfo;

public class SetupOpHandler extends CommandHandlerBase implements
        CommandHandler {

    @Override
    public void processRequest(KineticMessage request, KineticMessage response)
            throws ServiceException {

        boolean hasPermission = SetupHandler.checkPermission(request, response,
                engine.getAclMap());

        if (hasPermission) {

            SetupInfo setupInfo = null;

            try {
                setupInfo = SetupHandler.handleSetup(request, response,
                        engine.getStore(), engine.getKineticHome());

                if (setupInfo != null) {
                    this.engine
                            .setClusterVersion(setupInfo.getClusterVersion());
                }

            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }
    }

}
