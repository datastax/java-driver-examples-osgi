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
package com.datastax.driver.examples.rcp.mailbox.tests;

import com.datastax.driver.examples.rcp.mailbox.MailboxException;
import com.datastax.driver.examples.rcp.mailbox.MailboxMessage;
import com.datastax.driver.examples.rcp.mailbox.MailboxService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * This integration test requires an OSGi container.
 * Under Eclipse, run it as a "JUnit Plug-in Test".
 * <p>
 * This test also requires a running Cassandra node
 * on 127.0.0.1.
 */
public class MailboxServiceIT {

    private MailboxService service;

    @Before
    public void setUp() {
        BundleContext bundleContext = FrameworkUtil.getBundle(MailboxService.class).getBundleContext();
        ServiceReference<?> reference = bundleContext.getServiceReference(MailboxService.class.getName());
        service = (MailboxService) bundleContext.getService(reference);
    }

    @Test
    public void should_retrieve_message() throws MailboxException {
        MailboxMessage message = new MailboxMessage("recipient@datastax.com", 12345, "sender@datastax.com", "test");
        service.sendMessage(message);
        Iterator<MailboxMessage> received = service.getMessages("recipient@datastax.com").iterator();
        assertEquals(message, received.next());
        assertFalse(received.hasNext());
    }

    @After
    public void clearMailbox() throws Exception {
        service.clearMailbox("recipient@datastax.com");
    }

}
