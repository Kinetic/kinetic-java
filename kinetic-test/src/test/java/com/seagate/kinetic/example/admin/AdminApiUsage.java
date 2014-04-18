/**
 * Copyright (c) 2013 Seagate Technology LLC
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:

 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.

 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.

 * 3) Neither the name of Seagate Technology nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission
 * from Seagate Technology.

 * 4) No patent or trade secret license whatsoever, either express or implied, is granted by Seagate
 * Technology or its contributors by this copyright license.

 * 5) All modifications must be reposted in source code form in a manner that allows user to
 * readily access the source code.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, NONINFRINGEMENT AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS DISCLAIM ALL LIABILITY FOR
 * INTELLECTUAL PROPERTY INFRINGEMENT RELATED TO THIS SOFTWARE.
 */
package com.seagate.kinetic.example.admin;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.Capacity;
import kinetic.admin.Domain;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.admin.Role;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.ClientConfiguration;
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

		// client configuration
		ClientConfiguration adminClientConfig = new ClientConfiguration();
		adminClientConfig.setUseSsl(true);
		adminClientConfig.setPort(8443);

		// get admin client instance
		KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(adminClientConfig);

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
		ClientConfiguration adminClientConfig = new ClientConfiguration();
		adminClientConfig.setUseSsl(true);
		adminClientConfig.setPort(8443);
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
		adminClient.setSecurity(aclList);
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
		ClientConfiguration adminClientConfig = new ClientConfiguration();
		adminClientConfig.setUseSsl(true);
		adminClientConfig.setPort(8443);

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
				+ "\nDrive total capacity: " + capacity.getTotal()
				+ "\nDrive remaining capacity: " + capacity.getRemaining()
				+ "\n");

		// get utilization info from kineticLog
		List<Utilization> utils = kineticLog.getUtilization();
		for (Utilization util : utils) {
			System.out.println("Drive utilization info"
					+ "\nName: " + util.getName() + "\nUtilization: "
					+ util.getUtility() + "\n");
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
		ClientConfiguration adminClientConfig = new ClientConfiguration();
		adminClientConfig.setUseSsl(true);
		adminClientConfig.setPort(8443);
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
		ClientConfiguration adminClientConfig = new ClientConfiguration();

		// set configs
		adminClientConfig.setUseSsl(true);
		adminClientConfig.setPort(8443);
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
		ClientConfiguration adminClientConfig = new ClientConfiguration();
		adminClientConfig.setUseSsl(true);
		adminClientConfig.setPort(8443);
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
