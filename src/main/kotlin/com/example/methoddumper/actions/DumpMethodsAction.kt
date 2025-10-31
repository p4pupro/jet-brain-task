package com.example.methoddumper.actions

import com.example.methoddumper.service.MethodDumpService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.DumbService
import java.nio.file.Path

class DumpMethodsAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val service = project.service<MethodDumpService>()

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Dump Java Methods", false) {
                   override fun run(indicator: ProgressIndicator) {
                       try {
                           indicator.text = "Waiting for indexing to complete"
                           DumbService.getInstance(project).waitForSmartMode()

                           indicator.text = "Collecting indexed methods"
                           val methods = service.collectMethods()
                           
                           val output: Path = service.dumpMethodsToJson()

                           notify(project, "Method dump written to ${output} (${methods.size} methods)")
                       } catch (t: Throwable) {
                           notify(project, "Failed to dump methods: ${t.message}", NotificationType.ERROR)
                       }
                   }
        })
    }

    private fun notify(project: com.intellij.openapi.project.Project, content: String, type: NotificationType = NotificationType.INFORMATION) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
        group.createNotification(content, type).notify(project)
    }

    companion object {
        private const val NOTIFICATION_GROUP_ID = "Method Dumper Notifications"
    }
}
