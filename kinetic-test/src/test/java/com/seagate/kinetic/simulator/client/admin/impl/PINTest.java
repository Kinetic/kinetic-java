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
package com.seagate.kinetic.simulator.client.admin.impl;

import static com.seagate.kinetic.KineticTestHelpers.cleanPin;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import kinetic.client.KineticException;

import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Setup;
import com.seagate.kinetic.proto.Kinetic.Message.Status;

/**
 *
 * Setup test
 * <p>
 *
 * @author Chenchong Li
 *
 */
public class PINTest extends IntegrationTestCase {
	@Test
	public void testSetPin() throws KineticException, IOException,
	InterruptedException {
		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup.setNewClusterVersion(0);
		setup.setInstantSecureErase(false);
		String setPin = "pin001";
		setup.setSetPin(ByteString.copyFromUtf8(setPin));

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		Message respond = (Message) getAdminClient().configureSetupPolicy(km)
				.getMessage();
		assertTrue(respond.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));

		cleanPin(setPin, this.getAdminClient());
	}

	@Test
	public void testModifyOldPin_WithRightOldPin() throws KineticException,
	IOException, InterruptedException {
		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup.setNewClusterVersion(0);
		setup.setInstantSecureErase(false);
		String firstPin = "pin001";
		setup.setSetPin(ByteString.copyFromUtf8(firstPin));

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		getAdminClient().configureSetupPolicy(km);

		Message.Builder request1 = Message.newBuilder();
		Setup.Builder setup1 = request1.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup1.setNewClusterVersion(0);
		setup1.setInstantSecureErase(false);
		String secondPin = "pin002";
		setup1.setSetPin(ByteString.copyFromUtf8(secondPin));
		setup1.setPin(ByteString.copyFromUtf8(firstPin));

		KineticMessage km1 = new KineticMessage();
		km1.setMessage(request1);

		Message respond1 = (Message) getAdminClient().configureSetupPolicy(km1)
				.getMessage();
		assertTrue(respond1.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));

		cleanPin(secondPin, this.getAdminClient());
	}

	@Test
	public void testModifyOldPin_WithWrongOldPin() throws KineticException,
	IOException, InterruptedException {
		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup.setNewClusterVersion(0);
		setup.setInstantSecureErase(false);
		String firstPin = "pin001";
		setup.setSetPin(ByteString.copyFromUtf8(firstPin));

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		getAdminClient().configureSetupPolicy(km);

		Message.Builder request1 = Message.newBuilder();
		Setup.Builder setup1 = request1.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup1.setNewClusterVersion(0);
		setup1.setInstantSecureErase(false);
		setup1.setSetPin(ByteString.copyFromUtf8("pin002"));
		setup1.setPin(ByteString.copyFromUtf8("pin003"));

		KineticMessage km1 = new KineticMessage();
		km1.setMessage(request1);

		Message respond1 = (Message) getAdminClient().configureSetupPolicy(km1)
				.getMessage();
		assertTrue(respond1.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.INTERNAL_ERROR));

		cleanPin(firstPin, this.getAdminClient());
	}

	@Test
	public void testModifyOldPinWithRightPin_AfterRestartServer()
			throws Exception {
		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		String firstPin = "pin001";
		setup.setSetPin(ByteString.copyFromUtf8(firstPin));

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		Message respond = (Message) this.getAdminClient()
				.configureSetupPolicy(km).getMessage();
		assertTrue(respond.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));

		// restart server
		restartServer();

		Message.Builder request1 = Message.newBuilder();
		Setup.Builder setup1 = request1.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		String secondPin = "pin002";
		setup1.setSetPin(ByteString.copyFromUtf8(secondPin));
		setup1.setPin(ByteString.copyFromUtf8(firstPin));

		KineticMessage km1 = new KineticMessage();
		km1.setMessage(request1);

		Message respond1 = (Message) getAdminClient().configureSetupPolicy(km1)
				.getMessage();
		assertTrue(respond1.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));

		cleanPin(secondPin, this.getAdminClient());

	}

	@Test
	public void testModifyOldPin_WithWrongPin_AfterRestartServer()
			throws Exception {
		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		String firstPin = "pin001";
		setup.setSetPin(ByteString.copyFromUtf8(firstPin));

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		getAdminClient().configureSetupPolicy(km);

		// restart server
		restartServer();

		Message.Builder request1 = Message.newBuilder();
		Setup.Builder setup1 = request1.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup1.setSetPin(ByteString.copyFromUtf8("pin002"));
		setup1.setPin(ByteString.copyFromUtf8("pin002"));

		KineticMessage km1 = new KineticMessage();
		km1.setMessage(request1);

		Message respond1 = (Message) getAdminClient().configureSetupPolicy(km1)
				.getMessage();
		assertTrue(respond1.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.INTERNAL_ERROR));

		cleanPin(firstPin, this.getAdminClient());
	}

}
