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
package com.seagate.kinetic.allTests;

import java.util.ArrayList;
import java.util.List;

import org.testng.TestNG;
import org.testng.reporters.SuiteHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class AdminAPISanityTestsRunner {
    public static void main(String[] args) {
        XmlSuite suite = new XmlSuite();
        suite.setName("SanitySuite");
        suite.setParallel(XmlSuite.PARALLEL_NONE);

        XmlTest test = new XmlTest(suite);
        test.setName("AdminAPISanityTest");
        List<XmlClass> classes = new ArrayList<XmlClass>();
        classes.add(new XmlClass(
                "com.seagate.kinetic.sanityAPI.AdminAPISanityTest"));
        test.setXmlClasses(classes);

        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(suite);
        TestNG tng = new TestNG();
        tng.addListener(new SuiteHTMLReporter());
        tng.setXmlSuites(suites);
        tng.run();

        System.exit(0);
    }
}
