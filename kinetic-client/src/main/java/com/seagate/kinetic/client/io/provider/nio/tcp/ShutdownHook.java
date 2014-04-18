/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
