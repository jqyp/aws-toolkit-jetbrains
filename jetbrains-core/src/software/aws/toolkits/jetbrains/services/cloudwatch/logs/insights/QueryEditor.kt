// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package software.aws.toolkits.jetbrains.services.cloudwatch.logs.insights

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.michaelbaranov.microba.calendar.DatePicker
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JLabel
import javax.swing.JTextArea
import javax.swing.JButton
import javax.swing.JTextField

class QueryEditor internal constructor(private val project: Project) {
    lateinit var absoluteTimeRadioButton: JRadioButton
    lateinit var relativeTimeRadioButton: JRadioButton
    lateinit var searchTerm: JRadioButton
    lateinit var querySearchTerm: JTextField
    lateinit var queryLogGroupsRadioButton: JRadioButton
    var saveQueryButton: JButton? = null
    lateinit var retrieveSavedQueriesButton: JButton
    private lateinit var tablePanel: SimpleToolWindowPanel
    lateinit var queryBox: JTextArea
    lateinit var logGroupLabel: JLabel
    lateinit var endDate: DatePicker
    lateinit var queryEditorBasePanel: JPanel
    lateinit var relativeTimeUnit: JComboBox<*>
    lateinit var relativeTimeNumber: JTextField
    lateinit var startDate: DatePicker
    private lateinit var showLogGroupTable: AddRemoveLogGroupTable

    private fun initArLogGroupTable() {
        showLogGroupTable.tableView.listTableModel
        showLogGroupTable.getSelLogGroups()
    }

    private fun createUIComponents() {
        // TODO: place custom component creation code here
        tablePanel = SimpleToolWindowPanel(false, true)
        showLogGroupTable = AddRemoveLogGroupTable(project)
        initArLogGroupTable()
        tablePanel.setContent(showLogGroupTable.component)
        val timeUnits = arrayOf("Minutes", "Hours", "Days", "Weeks")
        relativeTimeUnit = ComboBox<String>(timeUnits)
    }

    init {
        startDate.isEnabled = false
        endDate.isEnabled = false
        relativeTimeNumber.isEnabled = false
        relativeTimeUnit.isEnabled = false
        querySearchTerm.isEnabled = false
        queryBox.isEnabled = false
    }
}
