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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import javax.inject.Inject;
import java.util.Iterator;

import static com.datastax.driver.examples.osgi.ConfigurationOptions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
public class MailboxServiceIT {

    @Inject
    private MailboxService service;

    @Configuration
    public Option[] config() {
        return options(
                currentProject(),
                driver(),
                logging(),
                junitBundles()
        );
    }

    @Test
    public void should_retrieve_message() throws MailboxException {
        MailboxMessage message = new MailboxMessage("recipient@datastax.com", 12345, "sender@datastax.com", "test");
        service.sendMessage(message);
        Iterator<MailboxMessage> received = service.getMessages("recipient@datastax.com").iterator();
        assertEquals(message, received.next());
        assertFalse(received.hasNext());
    }

    @Before
    @After
    public void clearMailbox() throws Exception {
        service.clearMailbox("recipient@datastax.com");
    }

}
