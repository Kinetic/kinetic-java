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

import com.seagate.kinetic.adminAPI.KineticAdminTest;
import com.seagate.kinetic.advancedAPI.AdvancedAPITest;
import com.seagate.kinetic.asyncAPI.KineticAsyncAPITest;
import com.seagate.kinetic.basicAPI.KineticBasicAPITest;
import com.seagate.kinetic.boundary.AdvancedAPIBoundaryTest;
import com.seagate.kinetic.boundary.KineticBoundaryTest;
import com.seagate.kinetic.concurrent.KineticClientConcurrentTest;
import com.seagate.kinetic.concurrent.KineticPutConcurrentTest;
import com.seagate.kinetic.performance.microPerfTest;

/**
 * 
 * This class contains the Java smoke/integration test cases.
 * <p>
 * The Java smoke/integration tests are used to verify basic functionality for
 * the Kinetic simulator and drive.
 * 
 * The following are instructions to run test cases against the simulator or
 * drive locally or remotely.
 * 
 * <p>
 * To Run all smoke tests, perform the following steps. <br>
 * ========================================================
 * <p>
 * Make sure you have installed java (jdk1.6 or later, set JAVA_HOME),
 * maven(3.0.3 or later) and git in your environment. <br>
 * Make sure you have access for /Seagate/LC2 in Github. <br>
 * Make sure one instance of simulator or kinetic drive is running.
 * <p>
 * 1, $git clone https://github.com/Kinetic/kinetic-java.git
 * <p>
 * 2, $cd ~/git/kinetic-java
 * <p>
 * 3, $mvn clean package<br>
 * Default nonSSL port is 8123, default SSL port is 8443. <br>
 * If you want to configure nonSSL port or SSL port, add the system properties
 * as blew,<br>
 * mvn clean package -DKINETIC_PORT=non_ssl_port -DKIENTIC_SSL_PORT=ssl_port<br>
 * <p>
 * For example,<br>
 * mvn clean package -DKINETIC_PORT=8127 -DKIENTIC_SSL_PORT=8446<br>
 * <p>
 * 4, $cd ~/git/kinetic-java/bin
 * <p>
 * 5, $sh runSmokeTests.sh [-host host_ip] [-port port] [-home kinetic_home]
 * [-usesocketlog usesocketlog] [-logserverip ip] [-logserverport port]
 * [-logformatter formatter]
 * <p>
 * Parameters are optional, you can use "-help" to see usage. <br>
 * The default values are set as below: <br>
 * host=127.0.0.1<br>
 * port=8123<br>
 * home=user's home directory<br>
 * usesocketlog=false<br>
 * logserverip=127.0.0.1<br>
 * logserverport=60123<br>
 * logformatter=com.seagate.kinetic.socket.DefaultLogFormatter
 * <p>
 * or <br>
 * $python runSmokeTests.py [-host host_ip] [-port port] [-home kinetic_home]
 * [-usesocketlog usesocketlog] [-logserverip ip] [-logserverport port]
 * [-logformatter formatter]
 * <p>
 * 6, end, see the test result.
 * <p>
 * To run a single smoke test class, perform the following steps. <br>
 * ===========================================================
 * <p>
 * 1, $cd ~/git/kinetic-java/kinetic-test
 * <p>
 * 2, $mvn -Dtest="your test class name" test
 * <p>
 * For example: <br>
 * mvn -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=127.0.0.1
 * -Dtest=KineticBasicAPITest test
 * <p>
 * ============================================================
 * <p>
 * 
 * The following are instructions to send test result log info to log socket
 * server locally or remotely.
 * 
 * <p>
 * Run log socket server locally. <br>
 * ============================================================
 * <p>
 * 1, $git clone https://github.com/Kinetic/kinetic-java.git
 * <p>
 * 2, $cd ~/git/kinetic-java
 * <p>
 * 3, $mvn clean package
 * <p>
 * 4, $cd ~/git/kinetic-java/bin
 * <p>
 * 5, $sh startLogServer.sh [-port port]<br>
 * <p>
 * Parameters are optional, you can use "-help" to see usage. <br>
 * The default values are set as below: <br>
 * port=60123<br>
 * <p>
 * If you want to run log server remotely, compile
 * "com.seagate.kinetic.socketlog.LogServer" on remotely machine and modify
 * "$CLASSPATH" in startLogServer.sh<br>
 * <p>
 * 
 * Run all smoke test and send test log result to log socket server. <br>
 * =============================================================
 * <p>
 * 1, $git clone https://github.com/Kinetic/Kinetic-java.git
 * <p>
 * 2, $cd ~/git/kinetic-java
 * <p>
 * 3, $mvn clean package
 * <p>
 * 4, $cd ~/git/kinetic-java/bin
 * <p>
 * 5, $sh runSmokeTests.sh [-host host_ip] [-port port] [-home kinetic_home]
 * [-usesocketlog usesocketlog] [-logserverip ip] [-logserverport port]
 * [-logformatter formatter]
 * <p>
 * Parameters are optional, you can use "-help" to see usage. <br>
 * The default values are set as below: <br>
 * host=127.0.0.1<br>
 * port=8123<br>
 * home=user's home directory<br>
 * usesocketlog=false<br>
 * logserverip=127.0.0.1<br>
 * logserverport=60123<br>
 * logformatter=com.seagate.kinetic.socket.DefaultLogFormatter
 * <p>
 * Now DefaultLogFormatter is:
 * classname=test_classname;methodname=test_methodname;message<br>
 * If test result is success: message format is: status=success<br>
 * If test result is failure: message format is:
 * status=failed;errmsg=detail_error_message<br>
 * If you want to describe a new formatter for yourself, please follow the
 * message rule and take your formatter as a parameter for the script.
 * <p>
 * For example:<br>
 * sh runSmokeTests.sh -usesocketlog true -logserverip 10.24.70.43<br>
 * <p>
 * or <br>
 * $python runSmokeTests.py [-host host_ip] [-port port] [-home kinetic_home]
 * [-usesocketlog usesocketlog] [-logserverip ip] [-logserverport port]
 * [-logformatter formatter]
 * <p>
 * 6, end, see the test result locally and remotely log socket server.
 * <p>
 * 
 * Parse the log formatter locally or remotely. <br>
 * =================================================================
 * <p>
 * 1, Example: com.seagate.kinetic.socketlog.ParseDefaultLogFormatterExample
 * <p>
 * 
 * 
 * @see KineticAdminTest
 * @see AdvancedAPITest
 * @see KineticAsyncAPITest
 * @see KineticBasicAPITest
 * @see AdvancedAPIBoundaryTest
 * @see KineticBoundaryTest
 * @see KineticClientConcurrentTest
 * @see KineticPutConcurrentTest
 * @see microPerfTest
 */
public class AllTestsRunner {

    public static void main(String[] args) {
        XmlSuite suite = new XmlSuite();
        suite.setName("SmokeSuite");
        suite.setParallel(XmlSuite.PARALLEL_NONE);

        XmlTest test = new XmlTest(suite);
        test.setName("SmokeTest");
        List<XmlClass> classes = new ArrayList<XmlClass>();
        classes.add(new XmlClass(
                "com.seagate.kinetic.adminAPI.KineticAdminTest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.advancedAPI.AdvancedAPITest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.asyncAPI.KineticAsyncAPITest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.basicAPI.KineticBasicAPITest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.boundary.AdvancedAPIBoundaryTest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.boundary.KineticBoundaryTest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.concurrent.KineticClientConcurrentTest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.concurrent.KineticPutConcurrentTest"));
        classes.add(new XmlClass(
                "com.seagate.kinetic.performance.microPerfTest"));
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
