To create a runnable jar file with all the dependencies from Maven Central Repository, 
type the following command under '<kinetic-java>/bin/buildFromMvnRepo' folder. 

mvn package

The above will generate a runnable jar file under '<kinetic-java>/bin/buildFromMvnRepo/target' folder.  

For example (assume the version is 0.7.0.1):

<kinetic-java>/bin/buildFromMvnRepo/target/kinetic-all-0.7.0.1-SNAPSHOT-jar-with-dependencies.jar

Once the jar file is generated, you may start a simulator with default settings with the following command:

java -jar kinetic-all-0.7.0.1-SNAPSHOT-jar-with-dependencies.jar
