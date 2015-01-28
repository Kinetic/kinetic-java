package com.seagate.kinetic.allTests;

import java.util.ArrayList;
import java.util.List;

import org.testng.TestNG;
import org.testng.reporters.SuiteHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class ConformanceTestRunner {
    public static void main(String[] args) {
        XmlSuite suite = new XmlSuite();
        suite.setName("SmokeSuite");
        suite.setParallel(XmlSuite.PARALLEL_NONE);

        XmlTest test = new XmlTest(suite);
        test.setName("ConformalTest");
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
