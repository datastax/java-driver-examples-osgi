# Java Driver Tycho / Eclipse RCP Example Application

This project demonstrates how to integrate the Java Driver in an Eclipse RCP application
built with Maven and [Tycho].

The Java Driver artifacts (`driver-core`, `driver-mapping`, `driver-extras`), 
are built and distributed as OSGi bundles in Maven Central. All of their
mandatory dependencies are also available as OSGi bundles.

This example application leverages Tycho's ability to resolve OSGi bundles from
Maven repositories. The dependencies to the driver are thus declared as regular 
Maven dependencies in the parent project's pom file.

*Note: this project has been tested with Eclipse Mars (4.5.2). 
We do not guarantee that it is compatible with
other versions of Eclipse. We do not guarantee either that
this project can be successfully compiled with any other IDE than Eclipse.*

[Tycho]:https://eclipse.org/tycho/

## Overview

There are 3 Maven modules in this project:

1. `com.datastax.driver.examples.rcp.parent`: the parent reactor project;
2. `com.datastax.driver.examples.rcp.mailbox`: a typical Eclipse RCP plug-in project, 
containing production code only and no tests;
3. `com.datastax.driver.examples.rcp.mailbox.tests`: a typical Eclipse RCP plug-in 
fragment project containing unit and integration tests for 
`com.datastax.driver.examples.rcp.mailbox`.

The directory layout adopted throughout this project strives to comply with both Maven
standards and Eclipse RCP standards.

Following Maven conventions, code is divided into three types of source folders:

1. `src/main`: production code;
2. `src/test`: unit tests (do not require to be run inside an OSGi container);
3. `src/it`: plug-in tests, a.k.a. integration tests (require an OSGi container and must be run as "Plug-in Test" in Eclipse).

Following Eclipse RCP conventions, all test code is placed in a special project,
`com.datastax.driver.examples.rcp.mailbox.tests` which is a _test fragment_ to the main project under test,
`com.datastax.driver.examples.rcp.mailbox`.

## Pom-first approach vs Manifest-first approach

Because it uses Maven as its build tool, this project adopts a 
"pom-first" approach to the well-known conflict between
pom and manifest files. 

This means that pom files should be considered as the primary source of truth,
and manifest files should be kept in sync with them.

Unfortunately, Tycho and Eclipse RCP favor a "manifest-first" approach, where
the manifest is the primary source of truth for resolving dependencies.

When a pom file changes (e.g. because a new dependency is added),
one then needs to manually update the corresponding manifest file,
otherwise subsequent builds could fail.

To update the manifest files, run the following command:

    mvn clean compiler:compile bundle:manifest

Note: the above command uses the [Maven bundle plugin] to generate manifests from pom files.
Note that in this command, the projects are compiled with the vanilla Maven
compiler, and not Tycho's compiler, because the latter would have attempted
to read the manifest that we are precisely trying to update.

[Maven bundle plugin]:https://cwiki.apache.org/confluence/display/FELIX/Apache+Felix+Maven+Bundle+Plugin+%28BND%29

## Installing/Running with Maven and Tycho

Following Maven conventions, unit tests are run in the `test` phase, 
while integration tests are run in the `integration-test` phase.

To run unit tests _only_, simply run `mvn clean test`.

To package the project and run _both unit tests and integration tests_, 
run `mvn clean verify`. 

Also note that it is not possible to build only certain Maven submodules;
you should always build the entire project.

Important: integration tests assume that there is a Cassandra node listening on 127.0.0.1:9042.
You can use [CCM] to start a local node. It is also possible to specify different contact points:

    mvn clean verify -Dcassandra.contactpoints=host1,host2

[CCM]:https://github.com/pcmanus/ccm

## Installing/Running with Eclipse

### Creating a target platform

To be able to open and run this application in Eclipse, you will first have to create a target
platform that includes the driver bundles and their dependencies.

1) If your project/company benefits from a hosted P2 repository, the simplest way to achieve this
is to deploy the driver bundles there, along with their dependencies, 
and create a target platform definition that includes that P2 repository.

2) Otherwise, you can create a target platform that includes a local
folder on your machine containing all required dependencies. 

To create such a target platform, do the following steps:

1. Assemble all the project dependencies in a single directory; this can be achieved
by running the following command at the root of this project:
    
    ```
    mvn dependency:copy-dependencies -N -DincludeScope=compile -DexcludeGroupIds=org.osgi -DoutputDirectory=/path/to/directory 
    ```

2. In Eclipse, go to Preferences... -> Plug-in Development -> Target Platform;
3. Click on 'Add...';
4. Choose 'Default' and click 'Next';
5. Name your target platform 'Driver Eclipse RCP Example Target Platform';
6. Under the Locations tab, click on Add...;
7. Choose 'Directory' and click 'Next';
8. Click on 'Browse...' and point the location to the directory where you placed the driver jars with their dependencies;
9. Click 'Next' and check the list of found plugins: you should see about 20 plug-ins in total;
10. Click 'Finish' and click 'Finish' again;
11. Check the newly-created target platform and make sure it becomes the active target platform;
12. Click 'Ok'.

3) A third alternative is to "wrap" the Java Driver bundle, along with its dependencies,
in a standalone plug-in project, then include that plug-in as a requirement in your application
with a `Require-Bundle` manifest directive.

### Importing the projects

1. Import the projects via File -> Import... -> Maven -> Existing Maven projects.
2. Click 'Next';
3. Click 'Browse...': point to the `tycho` folder that contains this README file;
4. Click 'Finish'.

If it is the first time you are importing Maven/Tycho projects into your Eclipse workspace,
Eclipse will prompt you to install additional Maven plugin connectors to handle some
of the Maven plugins used in this project. If this is the case, answer 'Yes' when prompted
to install, and restart Eclipse.

Check that all projects have been correctly imported:

1. All projects should have the Maven nature;
2. All projects but the parent should have the PDE nature (plug-in project) and the Java nature;
3. There should be no compilation errors.

If you have compilation errors, see the Troubleshooting section below.

### Running the tests

To run a unit test such as `MailboxServiceImplTest`, go to 
Run... -> Run Configurations... -> JUnit Test -> MailboxServiceImplTest,
then click on 'Run'.

To run an integration test such as `MailboxServiceIT`,
To run the tests, go to Run... -> Run Configurations... -> JUnit Plug-in Test -> MailboxServiceIT,
then click on 'Run'.

Note: integration tests require an OSGi container; _you cannot run them as regular JUnit tests_.
And again, the integration tests assume that there is a Cassandra node listening on 127.0.0.1:9042.

### Troubleshooting

1) I see compilation errors in some projects in Eclipse.

This is certainly due to a bad target platform definition. Check your active target platform
definition and make sure it includes all the required software: Eclipse runtime plug-ins and,
of course, the driver bundles and their dependencies.

2) Can't see driver logs when running tests with Eclipse, or logs are in DEBUG level.

Add the following VM parameter to your test launch configuration:

    -Dlogback.configurationFile=${resource_loc:/com.datastax.driver.examples.rcp.mailbox.tests/src/it/resources/logback-test.xml}
    
To edit your test launch configuration, choose Run -> Edit Configurations... then 
go to the 'Arguments' tab, then enter the appropriate arguments under VM Arguments.

