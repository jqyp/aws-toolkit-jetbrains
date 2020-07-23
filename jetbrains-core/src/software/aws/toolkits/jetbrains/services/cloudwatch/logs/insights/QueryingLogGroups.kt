// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.cloudwatch.logs.insights

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.StartQueryRequest
import software.aws.toolkits.jetbrains.core.awsClient
import software.aws.toolkits.jetbrains.utils.ApplicationThreadPoolScope

class QueryingLogGroups(private val project: Project) : CoroutineScope by ApplicationThreadPoolScope("ExecutingQuery") {
    private var client: CloudWatchLogsClient = project.awsClient()
    public fun executeStartQuery(qEndDate: Long, logGroupName: String, query: String, qStartDate: Long) = launch {
        val request = StartQueryRequest.builder()
            .endTime(qEndDate)
            .logGroupName(logGroupName)
            .queryString(query)
            .startTime(qStartDate)
            .build()
        val response = client.startQuery(request)
        val qid = response.queryId()
    }
}
