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
package com.seagate.kinetic.example.admin;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.Capacity;
import kinetic.admin.Domain;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.admin.Role;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.EntryNotFoundException;
import kinetic.client.KineticException;

/**
 * Kinetic admin API usage sample code.
 * <p>
 * This example demonstrates how to use admin API to configure a Kinetic drive.
 * This assumes that a simulator is running on the localhost:8443 (SSL/TLS port)
 * <p>
 * This example performs the following operations
 * <ul>
 * <li>1. setup for the drive, including set pin, set new cluster version and
 * erase data on the drive;
 * <li>2. set security information, including ACLs;
 * <li>3. get log information from drive;
 * <li>4. erase all the data in the drive.
 * <li>5. change the old cluster version to a new one.
 * <li>6. change the pin to use a new pin.
 * </ul>
 */
public class AdminApiUsage {
	public static final String UTF8 = "utf8";

	/**
	 * 
	 * Kinetic Admin API usage example - setup operation.
	 * <p>
	 * <ul>
	 * <li>1. Start admin client.
	 * <li>2. Setup for the drive, including set pin, set new cluster version
	 * and erase the db data. For the first time, the pin could be any thing,
	 * because the new drive does not have pin. when setup finished, the
	 * "setpin" will be set the pin of the drive.
	 * <li>3. Close admin client.
	 * </ul>
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 * @throws UnsupportedEncodingException
	 *             if any error of getBytes() to utf8 format occurred.
	 */
	public void setup() throws UnsupportedEncodingException, KineticException {
		// get admin client instance
		KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(new AdminClientConfiguration());

		// pin on drive
		byte[] pin = null;

		// new pin
		byte[] setPin = "first-pin".getBytes(UTF8);

		// new cluster version
		long newClusterVersion = Long.valueOf(1);

		// set to true to rease all data on drive
		boolean secureErase = true;

		// perform set up operation
		adminClient.setup(pin, setPin, newClusterVersion, secureErase);
		System.out.println("Setup info: pin=" + new String(setPin)
		+ ", clusterVersion=" + newClusterVersion
		+ ", erase the data in DB");

		// close admin client
		adminClient.close();
	}

	/**
	 * Kinetic Admin API usage example - set security (ACL) operation.
	 * <ul>
	 * <li>1. Start admin client. Assume the admin client has permission to
	 * perform the operation.
	 * <li>2. Set security informations, including ACLs.
	 * <li>3. Close admin client.
	 * </ul>
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 */
	public void setSecurity() throws KineticException {

		// client configuration
		AdminClientConfiguration adminClientConfig = new AdminClientConfiguration();
		adminClientConfig.setClusterVersion(1);

		// get admin client instance
		KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(adminClientConfig);

		// construct roles for user
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.READ);
		roles.add(Role.WRITE);
		roles.add(Role.SECURITY);
		roles.add(Role.DELETE);
		roles.add(Role.GETLOG);
		roles.add(Role.SETUP);
		roles.add(Role.P2POP);
		roles.add(Role.RANGE);

		// domains associate with the roles
		List<Domain> domains = new ArrayList<Domain>();

		// construct a new domain
		Domain domain = new Domain();

		// set roles for the domain
		domain.setRoles(roles);

		// add the domain
		domains.add(domain);

		// acl list
		List<ACL> aclList = new ArrayList<ACL>();

		// new ACL instance
		ACL acl = new ACL();

		// set user associate with this ACL
		acl.setUserId(1);

		// set key
		acl.setKey("asdfasdf");

		// set domains associate with this ACL
		acl.setDomains(domains);

		// add ACL instance to acl list
		aclList.add(acl);

		// perform set security operation
		byte[] pin = "1234".getBytes();
		
		adminClient.setSecurity(aclList, null, pin, null, pin);
		System.out.println("Set the security info for client with all roles");

		// close admin client
		adminClient.close();
	}

	/**
	 * Kinetic Admin API usage example - get log operation.
	 * <p>
	 * <ul>
	 * <li>1. Start admin client. Assume the admin client has permission to
	 * perform the operation.
	 * <li>2. Get log informations, return KineticLog, you can get utilization
	 * capacity and temperature informations.
	 * <li>3. Close admin client.
	 * </ul>
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 */
	public KineticLog getLog() throws KineticException {

		// admin client configuration
		AdminClientConfiguration adminClientConfig = new AdminClientConfiguration();
		
		// set cluster version
		adminClientConfig.setClusterVersion(1);

		// set user id
		adminClientConfig.setUserId(1);

		// set key
		adminClientConfig.setKey("asdfasdf");

		// construct a new instance of admin client
		KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(adminClientConfig);

		// get kinetic log
		KineticLog kineticLog = adminClient.getLog();

		// get temperature info from kineticLog
		List<Temperature> temps = kineticLog.getTemperature();
		for (Temperature temp : temps) {

			System.out.println("Drive temperature (Celsius) info"
					+ "\nName: " + temp.getName()
					+ "\nMax temperature: " + temp.getMax()
					+ "\nMin temperature: " + temp.getMin()
					+ "\nTarget temperature: " + temp.getTarget()
					+ "\nCurrent temperature: " + temp.getCurrent() + "\n");
		}

		// get capacity info from kineticLog
		Capacity capacity = kineticLog.getCapacity();
		System.out.println("Drive capacity (MB) info"
				+ "\nDrive total capacity: " + capacity.getNominalCapacityInBytes()
				+ "\nDrive remaining capacity: " + capacity.getPortionFull()
				+ "\n");

		// get utilization info from kineticLog
		List<Utilization> utils = kineticLog.getUtilization();
		for (Utilization util : utils) {
			System.out.println("Drive utilization info"
					+ "\nName: " + util.getName() + "\nUtilization: "
					+ util.getUtility() + "\n");
		}
		
        try {
            // get vendor specific device log from simulator.
            // the name "com.seagate.simulator:dummy" is only supported by
            // the simulator.
            kinetic.admin.Device device = adminClient
                    .getVendorSpecificDeviceLog("com.seagate.simulator:dummy"
                            .getBytes("utf8"));
            System.out.println("**** got device value., size = "
                    + device.getValue().length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // attempt to get unsupported vendor specific log
            // this will cause EntryNotFoundException to be raised
            adminClient
                    .getVendorSpecificDeviceLog("com.seagate.simulator:badName"
                            .getBytes("utf8"));

            // should not enter here
            throw new RuntimeException("EntryNotFoundException not caught");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (EntryNotFoundException enfe) {
            System.out.println("caught expected exception., this is OK.");
        }

		// close admin client
		adminClient.close();

		// return kinetic log
		return kineticLog;
	}

	/**
	 * Kinetic Admin API usage example - secure erase operation.
	 * <ul>
	 * <li>1. Start admin client. Assume the admin client has permission to
	 * perform secure erase operation.
	 * <li>2. Erase all data in DB, keep pin and cluster version are consistent
	 * with the drive, make sure setPin is the same as pin.
	 * <li>3. Close admin client.
	 * </ul>
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 * @throws UnsupportedEncodingException
	 *             if any error of getBytes() to utf8 format occurred.
	 */
	public void secureErase() throws UnsupportedEncodingException,
	KineticException {

		// admin client config
		AdminClientConfiguration adminClientConfig = new AdminClientConfiguration();
		adminClientConfig.setClusterVersion(1);
		adminClientConfig.setUserId(1);
		adminClientConfig.setKey("asdfasdf");

		// get admin client instance
		KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(adminClientConfig);

		// pin on drive
		byte[] pin = "first-pin".getBytes(UTF8);

		// new pin
		byte[] setPin = "first-pin".getBytes(UTF8);

		// new cluster version
		long newClusterVersion = Long.valueOf(1);

		// set to true if to erase all data on drive
		boolean secureErase = true;

		// perform setup
		adminClient.setup(pin, setPin, newClusterVersion, secureErase);
		System.out.println("Erase all data in database");

		// close admin client
		adminClient.close();

	}

	/**
	 * Kinetic Admin API usage example - change cluster version operation.
	 * <p>
	 * <ul>
	 * <li>1. Start admin client. Assume the admin client has permission to make
	 * changes.
	 * <li>2. Change the cluster version.
	 * <li>3. Close the admin client.
	 * </ul>
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 * @throws UnsupportedEncodingException
	 *             if any error of getBytes() to utf8 format occurred.
	 */
	public void changeClusterVersion() throws UnsupportedEncodingException,
	KineticException {

		// admi client config
		AdminClientConfiguration adminClientConfig = new AdminClientConfiguration();

		// set configs
		adminClientConfig.setClusterVersion(1);
		adminClientConfig.setUserId(1);
		adminClientConfig.setKey("asdfasdf");

		// get admi client instance
		KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(adminClientConfig);

		// old pin on drive
		byte[] pin = "first-pin".getBytes(UTF8);

		// new pin on drive
		byte[] setPin = "first-pin".getBytes(UTF8);

		// new cluster version
		long newClusterVersion = Long.valueOf(0);

		// set to true to rease all data on drive
		boolean secureErase = false;

		// perform set up operation - this changes the cluster version
		adminClient.setup(pin, setPin, newClusterVersion, secureErase);
		System.out
		.println("cluster version is changed to a new one, new cluster version is "
				+ newClusterVersion);

		// close admin client
		adminClient.close();

	}

	/**
	 * Kinetic Admin API usage example - change pin operation.
	 * <p>
	 * <ul>
	 * <li>1. Start admin client. Assume the admin client has permission to make
	 * changes.
	 * <li>2. Change pin.
	 * <li>3. Close admin client.
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 * @throws UnsupportedEncodingException
	 *             if any error of getBytes() to utf8 format occurred.
	 */
	public void changePin() throws UnsupportedEncodingException,
	KineticException {

		// admin client configuration
		AdminClientConfiguration adminClientConfig = new AdminClientConfiguration();
		adminClientConfig.setClusterVersion(0);
		adminClientConfig.setUserId(1);
		adminClientConfig.setKey("asdfasdf");

		// get admin client instance
		KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(adminClientConfig);

		// old pin on drive
		byte[] pin = "first-pin".getBytes(UTF8);

		// new pin to set to the drive
		byte[] setPin = null;

		// new cluster version
		long newClusterVersion = Long.valueOf(0);

		// set to true if to rease all data on drive
		boolean secureErase = false;

		// perform admin setup operation
		adminClient.setup(pin, setPin, newClusterVersion, secureErase);
		System.out.println("Pin is reset from old pin=" + new String(pin)
		+ " to no pin");

		// close
		adminClient.close();

	}

	/**
	 * 
	 * Kinetic Admin API usage example.
	 * <p>
	 * This example performs the following operations
	 * <ul>
	 * <li>1. setup for the drive, including set pin, set new cluster version
	 * and erase data on the drive.
	 * <li>2. set security (ACL) to the drive.
	 * <li>3. get log information from drive;
	 * <li>4. erase all the data on the drive.
	 * <li>5. change the old cluster version to a new one.
	 * <li>6. change the pin to use a new pin.
	 * </ul>
	 * 
	 * @param args
	 *            not used.
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 * @throws UnsupportedEncodingException
	 *             if any error of getBytes() to utf8 format occurred.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws KineticException,
	UnsupportedEncodingException {

		// new instance of the example.
		AdminApiUsage kineticDrive = new AdminApiUsage();

		// 1. setup for the drive
		kineticDrive.setup();

		// 2. set security (ACL)
		kineticDrive.setSecurity();

		// 3. get log from drive
		KineticLog kineticLog = kineticDrive.getLog();

		// 4. erase all the data in the drive
		kineticDrive.secureErase();

		// 5. change the old cluster version to a new one
		kineticDrive.changeClusterVersion();

		// 6. change the pin to a new pin
		kineticDrive.changePin();
	}

}
