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
