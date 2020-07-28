// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.cloudwatch.logs.insights

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent
import software.aws.toolkits.resources.message
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

const val relativeTimeMinutes: String = "Minutes"
const val relativeTimeHours: String = "Hours"
const val relativeTimeDays: String = "Days"
const val relativeTimeWeeks: String = "Weeks"

class QueryEditorDialog(
    private val project: Project,
    private val lGroupName: String,
    private val absoluteTimeSelected: Boolean = false

) : DialogWrapper(project) {
    constructor(project: Project, logGroupName: String) :
        this(project = project, lGroupName = logGroupName)

    private val view = QueryEditor(project)
    private val queryingLogGroupApiCall = QueryingLogGroups(project)
    private val action: OkAction = QueryLogGroupOkAction()
    private val validator = QueryEditorValidator
    init {
        super.init()
        title = "Query Log Groups"
        view.absoluteTimeRadioButton.addActionListener {
            view.startDate.isEnabled = true
            view.endDate.isEnabled = true
            view.relativeTimeNumber.isEnabled = false
            view.relativeTimeUnit.setEnabled(false)
        }
        view.relativeTimeRadioButton.addActionListener {
            view.startDate.isEnabled = false
            view.endDate.isEnabled = false
            view.relativeTimeNumber.isEnabled = true
            view.relativeTimeUnit.setEnabled(true)
        }
        view.queryLogGroupsRadioButton.addActionListener {
            view.queryBox.isEnabled = true
            view.querySearchTerm.isEnabled = false
        }
        view.searchTerm.addActionListener {
            view.queryBox.isEnabled = false
            view.querySearchTerm.isEnabled = true
        }
    }
    override fun createCenterPanel(): JComponent? = view.queryEditorBasePanel
    override fun doValidate(): ValidationInfo? = validator.validateEditorEntries(view)
    override fun getOKAction(): Action = action
    override fun doCancelAction() {
        super.doCancelAction()
    }

    override fun doOKAction() {
        // Do nothing, close logic is handled separately
    }

    private fun getRelativeTime(unitOfTime: String, relTimeNumber: Long): StartEndDate {
        val endDate = Calendar.getInstance().toInstant()
        val startDate = when (unitOfTime) {
            relativeTimeMinutes -> endDate.minus(relTimeNumber, ChronoUnit.MINUTES)
            relativeTimeHours -> endDate.minus(relTimeNumber, ChronoUnit.HOURS)
            relativeTimeDays -> endDate.minus(relTimeNumber, ChronoUnit.DAYS)
            relativeTimeWeeks -> endDate.minus(relTimeNumber, ChronoUnit.WEEKS)
            else -> endDate
        }
        return StartEndDate(startDate, endDate)
    }
    private fun getAbsoluteTime(startDate: Date, endDate: Date): StartEndDate = StartEndDate(startDate.toInstant(), endDate.toInstant())

    private fun getFilterQuery(searchTerm: String): String {
        if (searchTerm.contains("/")) {
            val regexTerm = searchTerm.replace("/", "\\/")
            return "fields @message, @timestamp | filter @message like /$regexTerm/"
        }
        return "fields @message, @timestamp | filter @message like /$searchTerm/"
    }

    private fun beginQuerying() {
        if (!okAction.isEnabled) {
            return
        }
        val funDetails = getFunctionDetails()
        val queryStartEndDate: StartEndDate
        queryStartEndDate = (if (funDetails.absoluteTimeSelected) {
            getAbsoluteTime(funDetails.startDateAbsolute, funDetails.endDateAbsolute)
        } else {
            getRelativeTime(funDetails.relativeTimeUnit, funDetails.relativeTimeNumber.toLong())
        })
        val queryStartDate = queryStartEndDate.startDate.toEpochMilli() / 1000
        val queryEndDate = queryStartEndDate.endDate.toEpochMilli() / 1000
        val query = if (funDetails.queryingLogsSelected) {
            funDetails.query } else {
            getFilterQuery(funDetails.searchTerm)
        }
        close(OK_EXIT_CODE)
        queryingLogGroupApiCall.executeStartQuery(queryEndDate, funDetails.logGroupName, query, queryStartDate)
    }
    private fun getFunctionDetails(): QueryDetails = QueryDetails(
        logGroupName = lGroupName,
        absoluteTimeSelected = view.absoluteTimeRadioButton.isSelected,
        startDateAbsolute = view.startDate.date,
        endDateAbsolute = view.endDate.date,
        relativeTimeSelected = view.relativeTimeRadioButton.isSelected,
        relativeTimeUnit = view.relativeTimeUnit.selectedItem.toString(),
        relativeTimeNumber = view.relativeTimeNumber.text,
        searchTermSelected = view.searchTerm.isSelected,
        searchTerm = view.querySearchTerm.text,
        queryingLogsSelected = view.queryLogGroupsRadioButton.isSelected,
        query = view.queryBox.text
    )

    private inner class QueryLogGroupOkAction : OkAction() {
        init {
            putValue(Action.NAME, "Apply")
        }
        override fun doAction(e: ActionEvent?) {
            super.doAction(e)
            if (doValidateAll().isNotEmpty()) return
            beginQuerying()
        }
    }
}

object QueryEditorValidator {
    fun validateEditorEntries(view: QueryEditor): ValidationInfo? {
        if (!view.absoluteTimeRadioButton.isSelected && !view.relativeTimeRadioButton.isSelected) {
        return ValidationInfo(message("cloudwatch.logs.validation.timerange"), view.absoluteTimeRadioButton) }
        if (view.relativeTimeRadioButton.isSelected && view.relativeTimeNumber.text.isEmpty()) {
            return ValidationInfo(message("cloudwatch.logs.no_relative_time_number"), view.relativeTimeNumber) }
        if (view.absoluteTimeRadioButton.isSelected && view.startDate.date > view.endDate.date) {
            return ValidationInfo(message("cloudwatch.logs.compare.start.end.date"), view.startDate) }
        if (!view.queryLogGroupsRadioButton.isSelected && !view.searchTerm.isSelected) {
            return ValidationInfo(message("cloudwatch.logs.no_query_selected"), view.searchTerm) }
        if (view.queryLogGroupsRadioButton.isSelected && view.queryBox.text.isEmpty()) {
            return ValidationInfo(message("cloudwatch.logs.no_query_entered"), view.queryBox)
        }
        if (view.searchTerm.isSelected && view.querySearchTerm.text.isEmpty()) {
            return ValidationInfo(message("cloudwatch.logs.no_term_entered"), view.querySearchTerm)
        }
        return null
    }
}
