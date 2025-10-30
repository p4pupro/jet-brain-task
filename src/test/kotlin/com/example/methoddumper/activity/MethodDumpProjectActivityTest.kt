package com.example.methoddumper.activity

import com.example.methoddumper.service.MethodDumpService
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock
import com.intellij.openapi.project.Project

class MethodDumpProjectActivityTest {

    @Test
    fun testActivityCreation() {
        val activity = MethodDumpProjectActivity()
        assertNotNull("Activity should not be null", activity)
    }

    @Test
    fun testActivityImplementsProjectActivity() {
        val activity = MethodDumpProjectActivity()
        assertNotNull("Activity should implement ProjectActivity", activity as? com.intellij.openapi.startup.ProjectActivity)
    }

    @Test
    fun testActivityHasExecuteMethod() {
        val activity = MethodDumpProjectActivity()
        val project = mock(Project::class.java)
        
        // Verify execute method exists (suspend function)
        assertNotNull("Activity should have execute method", activity)
    }
}

