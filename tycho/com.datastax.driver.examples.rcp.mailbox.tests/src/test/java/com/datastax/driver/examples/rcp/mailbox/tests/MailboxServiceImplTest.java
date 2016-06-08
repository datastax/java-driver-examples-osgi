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

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.examples.rcp.mailbox.MailboxMessage;
import com.datastax.driver.examples.rcp.mailbox.impl.MailboxServiceImpl;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MailboxServiceImplTest {

    private final MailboxMessage message = new MailboxMessage("recipient@test.com", 12345L, "sender@test.com", "body");

    private final String keyspace = "test";

    @Mock
    private Session session;

    @Mock
    private Mapper<MailboxMessage> mapper;

    @Mock
    private PreparedStatement saveStatement;

    @Mock
    private PreparedStatement retrieveStatement;

    @Mock
    private PreparedStatement deleteStatement;

    @Mock
    private ColumnDefinitions variables;

    @Mock
    private PreparedId preparedId;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Result<MailboxMessage> result;

    @Before
    public void prepareMocks() throws Exception {
        given(session.prepare(any(BuiltStatement.class))).willReturn(retrieveStatement, deleteStatement);
        given(session.prepareAsync(any(SimpleStatement.class))).willReturn(Futures.immediateFuture(saveStatement));
        given(retrieveStatement.getVariables()).willReturn(variables);
        given(retrieveStatement.getPreparedId()).willReturn(preparedId);
        given(retrieveStatement.getCodecRegistry()).willReturn(CodecRegistry.DEFAULT_INSTANCE);
        given(deleteStatement.getVariables()).willReturn(variables);
        given(deleteStatement.getPreparedId()).willReturn(preparedId);
        given(deleteStatement.getCodecRegistry()).willReturn(CodecRegistry.DEFAULT_INSTANCE);
        given(variables.getType(0)).willReturn(DataType.text());
        given(variables.size()).willReturn(1);
        given(session.execute(any(BoundStatement.class))).willReturn(resultSet);
        given(mapper.map(resultSet)).willReturn(result);
        given(result.iterator()).willAnswer(invocation -> Iterators.singletonIterator(message));
    }

    @Test
    public void should_send_message() throws Exception {
        // given
        MailboxServiceImpl mailbox = new MailboxServiceImpl(session, keyspace, mapper);
        mailbox.init();
        // when
        long dateSent = mailbox.sendMessage(message);
        // then
        assertThat(dateSent).isEqualTo(12345L);
        verify(mapper).save(message);
    }

    @Test
    public void should_retrieve_message() throws Exception {
        // given
        MailboxServiceImpl mailbox = new MailboxServiceImpl(session, keyspace, mapper);
        mailbox.init();
        // when
        Iterable<MailboxMessage> messages = mailbox.getMessages("recipient@test.com");
        // then
        assertThat(messages).containsExactly(message);
        ArgumentCaptor<BoundStatement> arg = ArgumentCaptor.forClass(BoundStatement.class);
        verify(session).execute(arg.capture());
        BoundStatement bs = arg.getValue();
        assertThat(bs.getString(0)).isEqualTo("recipient@test.com");
        assertThat(bs.preparedStatement()).isSameAs(retrieveStatement);
    }

    @Test
    public void should_delete_message() throws Exception {
        // given
        MailboxServiceImpl mailbox = new MailboxServiceImpl(session, keyspace, mapper);
        mailbox.init();
        // when
        mailbox.clearMailbox("recipient@test.com");
        // then
        ArgumentCaptor<BoundStatement> arg = ArgumentCaptor.forClass(BoundStatement.class);
        verify(session).execute(arg.capture());
        BoundStatement bs = arg.getValue();
        assertThat(bs.getString(0)).isEqualTo("recipient@test.com");
        assertThat(bs.preparedStatement()).isSameAs(deleteStatement);
    }

}
