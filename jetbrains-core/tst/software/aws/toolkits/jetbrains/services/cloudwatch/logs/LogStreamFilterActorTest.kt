// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.cloudwatch.logs

import com.intellij.testFramework.ProjectRule
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.junit4.CoroutinesTimeout
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent
import software.aws.toolkits.jetbrains.core.MockClientManagerRule
import software.aws.toolkits.jetbrains.utils.waitForModelToBeAtLeast
import software.aws.toolkits.resources.message
import java.util.concurrent.CompletableFuture

// ExperimentalCoroutinesApi is needed for TestCoroutineScope
@ExperimentalCoroutinesApi
class LogStreamFilterActorTest {
    @JvmField
    @Rule
    val projectRule = ProjectRule()

    @JvmField
    @Rule
    val mockClientManagerRule = MockClientManagerRule(projectRule)

    @JvmField
    @Rule
    val timeout = CoroutinesTimeout.seconds(10)

    private val testCoroutineScope: TestCoroutineScope = TestCoroutineScope()

    @After
    fun after() {
        testCoroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun modelIsPopulated() {
        val mockClient = mockClientManagerRule.create<CloudWatchLogsAsyncClient>()
        whenever(mockClient.getLogEvents(Mockito.any<GetLogEventsRequest>()))
            .thenReturn(CompletableFuture.completedFuture(GetLogEventsResponse.builder().events(OutputLogEvent.builder().message("message").build()).build()))
        val tableModel = ListTableModel<LogStreamEntry>()
        val table = TableView<LogStreamEntry>(tableModel)
        val coroutine = LogStreamFilterActor(projectRule.project, table, "abc", "def")
        runBlocking {
            coroutine.channel.send(LogStreamActor.Messages.LOAD_INITIAL_SEARCH("filter query"))
            tableModel.waitForModelToBeAtLeast(1)
        }
        Assertions.assertThat(tableModel.items.size).isOne()
        Assertions.assertThat(tableModel.items.first().message).isEqualTo("message")
        Assertions.assertThat(table.emptyText.text).isEqualTo(message("cloudwatch.logs.no_events"))
    }
}
