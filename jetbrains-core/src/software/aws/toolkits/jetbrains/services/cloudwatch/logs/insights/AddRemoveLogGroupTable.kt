// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.cloudwatch.logs.insights
import com.intellij.ui.table.TableView
import com.intellij.execution.util.ListTableWithButtons
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel
import software.aws.toolkits.resources.message
import javax.swing.table.TableCellEditor

class AddRemoveLogGroupTable(project: Project): ARLogGroupTable<SelectedLogGroups> (emptyTableMainText = "No log groups found",
    addNewEntryText = "Add log groups") {
    override fun cloneElement(variable: SelectedLogGroups): SelectedLogGroups = variable.copy()
    override fun createElement(): SelectedLogGroups = SelectedLogGroups()
    fun getSelLogGroups(): List<SelectedLogGroups> = elements.toList()
    override fun createListModel(): ListTableModel<*> = ListTableModel<SelectedLogGroups>(
        StringColInfo(
            message("cloudwatch.logs.loggq"),
            { it.log_groups },
            { mapping, value -> mapping.log_groups = value }
        )
    )

    private inner class StringColInfo(
        name: String,
        private val retrieveFunc: (SelectedLogGroups) -> String?,
        private val setFunc: (SelectedLogGroups, String?) -> Unit,
        private val editor: () -> TableCellEditor? = { null }
    ) : ListTableWithButtons.ElementsColumnInfoBase<SelectedLogGroups>(name) {
        override fun valueOf(item: SelectedLogGroups): String? = retrieveFunc.invoke(item)

        override fun setValue(item: SelectedLogGroups, value: String?) {
            if (value == valueOf(item)) {
                return
            }
            setFunc.invoke(item, value)
            setModified()
        }

        override fun isCellEditable(item: SelectedLogGroups?): Boolean = false
        override fun getDescription(element: SelectedLogGroups?): String? = null

    }

    override fun canDeleteElement(selection: SelectedLogGroups?): Boolean =true
    override fun isEmpty(element: SelectedLogGroups): Boolean = element.log_groups.isNullOrEmpty()

}

