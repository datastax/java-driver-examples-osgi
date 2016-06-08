# Java Driver OSGi Example with Pax and Apache Felix

This project demonstrates how to integrate the Java Driver in an OSGi application,
buit with the [Pax] framework and using Apache [Felix] as the OSGi container.

The Java Driver artifacts (`driver-core`, `driver-mapping`, `driver-extras`), 
are built and distributed as OSGi bundles in Maven Central. All of their
mandatory dependencies are also available as OSGi bundles.

This example application leverages Pax ability to resolve OSGi bundles from
Maven repositories. The dependencies to the driver are thus declared as regular 
Maven dependencies in the project's pom file.

# Overview

Following Maven conventions, code is divided into three types of source folders:

1. `src/main`: production code;
2. `src/test`: unit tests (do not require an OSGi container);
3. `src/it`: integration tests (require an OSGi container).

# Installing/Running with Maven

Following Maven conventions, unit tests are run in the `test` phase, 
while integration tests are run in the `integration-test` phase.

To run unit tests _only_, simply run `mvn clean test`.

To package the project and run _both unit tests and integration tests_, 
run `mvn clean verify`. 

Important: integration tests assume that there is a Cassandra node listening on 127.0.0.1:9042.
You can use [CCM] to start a local node. It is also possible to specify different contact points:

    mvn clean verify -Dcassandra.contactpoints=host1,host2

For more information about how to configure and run OSGi integration tests,
check the [Pax Exam] documentation. 

Note that this project also uses the
[Alta] plugin to make it easier to reference Maven dependencies
when launching tests with Pax Exam.
While convenient, the usage of this plugin requires that
you run `mvn test` at least once, if you change something
in your project's dependencies, in order for the Alta
plugin to update the generated links.

# Troubleshooting

To help troubleshoot provisioning problems, it is possible to
run [Pax Runner] from the command line:

    mvn clean install pax:run -DskipITs

This will launch [Pax Runner] and deploy the application in an 
Apache Felix OSGi container.

Note that Pax Runner only considers Maven _installed_
artifacts, not artifacts being built as part of the
reactor build. Hence the need for `clean install`
before launching Pax Runner with `pax:run`.

The Felix framework has a shell called [Gogo], 
which can be used to inspect the container state.
For example, to list all provisioned bundles with their states,
enter `bundles`; to inspect package requirements of a bundle,
enter:

    inspect req osgi.wiring.package <bundleId>


[Pax]:https://ops4j1.jira.com/wiki/display/ops4j/Pax
[Pax Exam]:https://ops4j1.jira.com/wiki/display/PAXEXAM4
[Pax Runner]:https://ops4j1.jira.com/wiki/display/paxrunner/
[Felix]:https://cwiki.apache.org/confluence/display/FELIX/Index
[CCM]:https://github.com/pcmanus/ccm
[Alta]:http://veithen.github.io/alta/examples/pax-exam.html
[Gogo]:http://felix.apache.org/documentation/subprojects/apache-felix-gogo.html
