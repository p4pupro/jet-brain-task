package com.example.methoddumper.service

import com.example.methoddumper.model.MethodEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import java.nio.file.Path

class MethodDumpServiceTest {

    @Test
    fun testDefaultOutputPath() {
        val project = mock(Project::class.java)
        val service = MethodDumpService(project)
        
        // Note: This test verifies the structure, actual path depends on project state
        val outputPath = service.defaultOutputPath()
        
        assertNotNull("Output path should not be null", outputPath)
        assertEquals("methods.json", outputPath.fileName.toString())
    }

    @Test
    fun testServiceCompanion() {
        val project = mock(Project::class.java)
        
        // Verify companion object method exists
        val service = MethodDumpService.getInstance(project)
        assertNotNull("Service instance should not be null", service)
    }

    @Test
    fun testMethodEntryStructure() {
        // Test that MethodEntry data class works correctly
        val entry = MethodEntry("testMethod", "public void test() {}")
        
        assertEquals("testMethod", entry.name)
        assertEquals("public void test() {}", entry.body)
    }
}

