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
package com.seagate.kinetic.allTests;

import java.util.ArrayList;
import java.util.List;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class DriveSmokeTestsRunner {

	public static void main(String[] args) {
		XmlSuite suite = new XmlSuite();
		suite.setName("DriveSmokeSuite");
		suite.setParallel(XmlSuite.PARALLEL_NONE);

		XmlTest test = new XmlTest(suite);
		test.setName("DriveSmokeTest");
		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(new XmlClass(
				"com.seagate.kinetic.sanityAPI.AdminAPISanityTest"));
		classes.add(new XmlClass(
				"com.seagate.kinetic.basicAPI.KineticBasicAPITest"));
		test.setXmlClasses(classes);

		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		TestNG tng = new TestNG();
		tng.setXmlSuites(suites);
		tng.run();

		System.exit(0);
	}
}
