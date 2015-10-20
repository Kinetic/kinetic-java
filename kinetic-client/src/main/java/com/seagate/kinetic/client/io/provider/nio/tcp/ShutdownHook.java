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
package com.seagate.kinetic.client.io.provider.nio.tcp;

import java.util.logging.Logger;

/**
 * 
 * Client runtime library shutdown hook.
 * 
 * @author chiaming
 * 
 */
public class ShutdownHook extends Thread {

	public final Logger logger = Logger.getLogger(ShutdownHook.class.getName());

	public ShutdownHook() {
		// logger.info("shutdown hook loaded ...");
	}

	@Override
	public void run() {

		// logger.info("shutdown hook started ...");

		try {

			// shutdown workers
			NioWorkerGroup.shutdown();

			// logger.info("shutdownhook finished ...");
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}

	}

}
