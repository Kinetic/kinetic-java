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
package com.seagate.kinetic.simulator.persist;

import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

/**
 * Create a new instance of persistent store
 * <p>
 * If "kietic.db.class" Java System property is defined, then the defined class
 * fullname is loaded and a new instance of the defined store is instantiated.
 * <p>
 * For example: -Dkinetic.db.class=
 * "com.seagate.kinetic.simulator.persist.newDb.NewDbStoreImplementation"
 * <p>
 * The above will instruct the simulator to instantiate a new instance of the
 * NewDbStoreImplementation.
 * <p>
 * The persistent store implementation must implement the following Store
 * interface:
 * <p>
 * com.seagate.kinetic.simulator.persist.Store
 * <p>
 * If "kietic.db.class" is not defined and the "kinetic.db.leveldb" Java System
 * Property is set to true, then leveldb is used.
 * <p>
 * The default store is set to MemoryStore by the simulator if no store property
 * (as described above) is set.
 * <p>
 * 
 * @author Chenchong(Emma) Li
 */
public class StoreFactory {

	// default db store implementation
	private static final String DEFAULT_DB_CLASS = "com.seagate.kinetic.simulator.persist.leveldb.LevelDbStore";

	private final static Logger logger = Logger.getLogger(StoreFactory.class
			.getName());

	/**
	 * Create a new instance of persistent store.
	 * <p>
	 * 
	 * @param config
	 *            server configuration
	 * 
	 * @return a new instance of persistent store.
	 */
	public static Store<?, ?, ?> createInstance(SimulatorConfiguration config) {

		// store interface
		Store<?, ?, ?> store = null;

		// get package name
		String packageName = StoreFactory.class.getPackage().getName();

		// default store class full name
		String dbFullName = DEFAULT_DB_CLASS;

		// get system property to see if user override the default
		String userDefinedDbClass = System.getProperty("kinetic.db.class");
		if (userDefinedDbClass != null) {
			// use user defined class
			dbFullName = userDefinedDbClass;
			logger.info("Using user defined Db class., name="
					+ userDefinedDbClass);
		} else {

			// check if config is set
			boolean isMemory = config.getUseMemoryStore();

			// check if property is set to use memory store
			// XXX chiaming 10/06/2013: to be removed. Config API should be used
			// instead.
			if (isMemory == false) {
				isMemory = Boolean.parseBoolean(config.getProperty(
						"kinetic.db.memory", "false"));
			}

			if (isMemory) {
				// use memory store
				dbFullName = packageName + ".memory.MemoryStore";
			}
		}

		try {

			logger.info("instantiating db, name=" + dbFullName);
			// load store class and instantiate an instance.
			store = (Store<?, ?, ?>) Class.forName(dbFullName).newInstance();
			// initialize the store.
			store.init(config);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			throw new RuntimeException(e);
		}

		// the actual store implementation
		return store;
	}

}
