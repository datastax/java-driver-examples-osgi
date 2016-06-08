/*
 *      Copyright (C) 2016 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.examples.osgi;

import org.ops4j.pax.exam.options.CompositeOption;

import static org.ops4j.pax.exam.CoreOptions.*;

class ConfigurationOptions {

    static CompositeOption currentProject() {
        return () -> options(
                systemProperty("cassandra.contactpoints").value(System.getProperty("cassandra.contactpoints", "127.0.0.1")),
                bundle("reference:file:target/classes")
        );
    }

    static CompositeOption driver() {
        return () -> options(
                url("link:classpath:com.datastax.driver.core.link"),
                url("link:classpath:com.datastax.driver.extras.link"),
                url("link:classpath:com.datastax.driver.mapping.link"),
                driverDependencies(),
                lz4Compression()
        );
    }

    static CompositeOption driverDependencies() {
        return () -> options(
                url("link:classpath:com.google.guava.link"),
                url("link:classpath:io.dropwizard.metrics.core.link"),
                url("link:classpath:io.netty.buffer.link"),
                url("link:classpath:io.netty.codec.link"),
                url("link:classpath:io.netty.common.link"),
                url("link:classpath:io.netty.handler.link"),
                url("link:classpath:io.netty.transport.link"),
                // some driver dependencies may require or check the availability of sun.misc;
                // this package is exported by default with Pax Exam test profile,
                // but not with default profile
                bootDelegationPackage("sun.misc")
        );
    }

    static CompositeOption lz4Compression() {
        return () -> options(
                url("link:classpath:lz4-java.link")
        );
    }

    static CompositeOption logging() {
        return () -> options(
                url("link:classpath:slf4j.api.link"),
                url("link:classpath:ch.qos.logback.classic.link"),
                url("link:classpath:ch.qos.logback.core.link"),
                systemProperty("logback.configurationFile").value("file:src/it/resources/logback-test.xml")
        );
    }

}
