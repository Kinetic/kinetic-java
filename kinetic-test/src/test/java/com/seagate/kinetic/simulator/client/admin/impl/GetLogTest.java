package com.seagate.kinetic.simulator.client.admin.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Message.Status;
import com.seagate.kinetic.proto.Kinetic.MessageOrBuilder;

/**
 *
 * getLog test case
 * <p>
 *
 * @author Chenchong Li
 *
 */
public class GetLogTest extends IntegrationTestCase {

	Logger logger = Logger.getLogger(GetLogTest.class.getName());

	@Test
	public void testGetUtilization() throws KineticException {

		Message.Builder request = Message.newBuilder();
		GetLog.Builder getLog = request.getCommandBuilder().getBodyBuilder()
				.getGetLogBuilder();
		getLog.addType(Type.UTILIZATIONS);

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		Message respond = (Message) getAdminClient().getLog(km).getMessage();

		assertTrue(respond.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertEquals("HDA", respond.getCommand().getBody().getGetLog()
				.getUtilizationList().get(0).getName());
		assertEquals("EN0", respond.getCommand().getBody().getGetLog()
				.getUtilizationList().get(1).getName());
	}

	@Test
	public void testGetCapacity() throws KineticException {

		Message.Builder request1 = Message.newBuilder();
		GetLog.Builder getLog1 = request1.getCommandBuilder().getBodyBuilder()
				.getGetLogBuilder();
		getLog1.addType(Type.CAPACITIES);

		KineticMessage km1 = new KineticMessage();
		km1.setMessage(request1);

		Message respond1 = (Message) getAdminClient().getLog(km1).getMessage();

		assertTrue(respond1.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertTrue(0 <= respond1.getCommand().getBody().getGetLog()
				.getCapacity().getTotal());
	}

	@Test
	public void testGetTemperature() throws KineticException {

		Message.Builder request2 = Message.newBuilder();
		GetLog.Builder getLog2 = request2.getCommandBuilder().getBodyBuilder()
				.getGetLogBuilder();
		getLog2.addType(Type.TEMPERATURES);

		KineticMessage km = new KineticMessage();
		km.setMessage(request2);

		Message respond2 = (Message) getAdminClient().getLog(km).getMessage();

		assertTrue(respond2.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertTrue(0 < respond2.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMaximum());
		assertTrue(0 < respond2.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMinimum());
		assertTrue(0 < respond2.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getTarget());
	}

	@Test
	public void testGetTemperatureAndCapacityAndUtilization()
			throws KineticException {

		Message.Builder request3 = Message.newBuilder();
		GetLog.Builder getLog3 = request3.getCommandBuilder().getBodyBuilder()
				.getGetLogBuilder();
		getLog3.addType(Type.TEMPERATURES);
		getLog3.addType(Type.CAPACITIES);
		getLog3.addType(Type.UTILIZATIONS);

		KineticMessage km = new KineticMessage();
		km.setMessage(request3);

		Message respond3 = (Message) getAdminClient().getLog(km).getMessage();

		assertTrue(respond3.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertEquals("HDA", respond3.getCommand().getBody().getGetLog()
				.getUtilizationList().get(0).getName());
		assertEquals("EN0", respond3.getCommand().getBody().getGetLog()
				.getUtilizationList().get(1).getName());
		assertTrue(0 <= respond3.getCommand().getBody().getGetLog()
				.getCapacity().getTotal());
		assertTrue(0 < respond3.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMaximum());
		assertTrue(0 < respond3.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMinimum());
		assertTrue(0 < respond3.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getTarget());
	}

	@Test
	public void testGetTemperatureAndCapacity() throws KineticException {

		Message.Builder request4 = Message.newBuilder();
		GetLog.Builder getLog4 = request4.getCommandBuilder().getBodyBuilder()
				.getGetLogBuilder();
		getLog4.addType(Type.TEMPERATURES);
		getLog4.addType(Type.CAPACITIES);

		KineticMessage km = new KineticMessage();
		km.setMessage(request4);

		Message respond4 = (Message) getAdminClient().getLog(km).getMessage();

		assertTrue(respond4.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
				.getCapacity().getTotal());
		assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMaximum());
		assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMinimum());
		assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getTarget());
	}

	@Test
	public void testGetTemperatureAndUtilization() throws KineticException {

		Message.Builder request5 = Message.newBuilder();
		GetLog.Builder getLog5 = request5.getCommandBuilder().getBodyBuilder()
				.getGetLogBuilder();
		getLog5.addType(Type.TEMPERATURES);
		getLog5.addType(Type.UTILIZATIONS);

		KineticMessage km = new KineticMessage();
		km.setMessage(request5);

		Message respond5 = (Message) getAdminClient().getLog(km).getMessage();

		assertTrue(respond5.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertEquals("HDA", respond5.getCommand().getBody().getGetLog()
				.getUtilizationList().get(0).getName());
		assertEquals("EN0", respond5.getCommand().getBody().getGetLog()
				.getUtilizationList().get(1).getName());
		assertTrue(0 <= respond5.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMaximum());
		assertTrue(0 <= respond5.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getMinimum());
		assertTrue(0 <= respond5.getCommand().getBody().getGetLog()
				.getTemperatureList().get(0).getTarget());
	}

	@Test
	public void testGetCapacityAndUtilization() throws KineticException {

		Message.Builder request6 = Message.newBuilder();
		GetLog.Builder getLog6 = request6.getCommandBuilder().getBodyBuilder()
				.getGetLogBuilder();
		getLog6.addType(Type.CAPACITIES);
		getLog6.addType(Type.UTILIZATIONS);

		KineticMessage km = new KineticMessage();
		km.setMessage(request6);

		MessageOrBuilder respond6 = getAdminClient().getLog(km).getMessage();

		assertTrue(respond6.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertEquals("HDA", respond6.getCommand().getBody().getGetLog()
				.getUtilizationList().get(0).getName());
		assertEquals("EN0", respond6.getCommand().getBody().getGetLog()
				.getUtilizationList().get(1).getName());
		assertTrue(0 <= respond6.getCommand().getBody().getGetLog()
				.getCapacity().getTotal());
	}

}
