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
