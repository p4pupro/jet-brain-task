package com.example.methoddumper.activity

import com.example.methoddumper.service.MethodDumpService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.observation.Observation

class MethodDumpProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        Observation.awaitConfiguration(project)
        project.waitForSmartMode()

        val service = project.service<MethodDumpService>()
        service.dumpMethodsToJson()
    }
}
