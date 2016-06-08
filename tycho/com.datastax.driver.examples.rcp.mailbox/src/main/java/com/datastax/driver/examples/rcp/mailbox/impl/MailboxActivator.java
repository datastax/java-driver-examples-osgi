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
package com.datastax.driver.examples.rcp.mailbox.impl;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.examples.rcp.mailbox.MailboxMessage;
import com.datastax.driver.examples.rcp.mailbox.MailboxService;
import com.datastax.driver.extras.codecs.date.SimpleTimestampCodec;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import io.netty.util.ThreadDeathWatcher;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

public class MailboxActivator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailboxActivator.class);

    private Cluster cluster;

    @Override
    public void start(BundleContext context) throws Exception {

        LOGGER.info("Starting bundle {}", context.getBundle().getSymbolicName());

        String contactPointsStr = context.getProperty("cassandra.contactpoints");
        if (contactPointsStr == null) {
            contactPointsStr = "127.0.0.1";
        }
        String[] contactPoints = contactPointsStr.split(",");

        LOGGER.info("Contact points: {}", contactPointsStr);

        String keyspace = context.getProperty("cassandra.keyspace");
        if (keyspace == null) {
            keyspace = "mailbox";
        }
        keyspace = Metadata.quote(keyspace);

        LOGGER.info("Keyspace: {}", keyspace);

        cluster = Cluster.builder()
                .addContactPoints(contactPoints)
                // if you are having trouble loading the metrics library, uncomment the following line
                //.withoutMetrics()
                // demonstrates usage of an optional dependency, namely LZ4
                .withCompression(ProtocolOptions.Compression.LZ4)
                // demonstrates usage of `driver-extras` bundle
                .withCodecRegistry(new CodecRegistry().register(SimpleTimestampCodec.instance))
                .build();

        Session session;
        try {
            session = cluster.connect(keyspace);
        } catch (InvalidQueryException e) {

            LOGGER.warn("Keyspace {} does not exist, creating it", keyspace);

            // Create the schema if it does not exist.
            session = cluster.connect();
            session.execute("CREATE KEYSPACE " + keyspace +
                    " with replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}");
            session.execute("CREATE TABLE " + keyspace + "." + MailboxMessage.TABLE + " (" +
                    "recipient text," +
                    "time timestamp," +
                    "sender text," +
                    "body text," +
                    "PRIMARY KEY (recipient, time))");
            session.execute("USE " + keyspace);

        }

        // demonstrates usage of `driver-mapping` bundle
        MappingManager mappingManager = new MappingManager(session);
        Mapper<MailboxMessage> mapper = mappingManager.mapper(MailboxMessage.class);
        MailboxServiceImpl mailbox = new MailboxServiceImpl(session, keyspace, mapper);
        mailbox.init();

        context.registerService(MailboxService.class.getName(), mailbox, new Hashtable<String, String>());

        LOGGER.info("Bundle {} successfully started", context.getBundle().getSymbolicName());

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOGGER.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        if (cluster != null) {
            cluster.close();
            // Await for Netty threads to stop gracefully
            ThreadDeathWatcher.awaitInactivity(10, TimeUnit.SECONDS);
        }
        LOGGER.info("Bundle {} successfully stopped", context.getBundle().getSymbolicName());
    }
}
