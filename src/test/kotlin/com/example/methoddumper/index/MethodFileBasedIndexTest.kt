package com.example.methoddumper.index

import com.example.methoddumper.model.MethodEntry
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex

class MethodFileBasedIndexTest : BasePlatformTestCase() {

    fun testIndexIdNotNull() {
        assertNotNull(MethodFileBasedIndex.INDEX_ID)
    }

    fun testIndexVersion() {
        val index = MethodFileBasedIndex()
        assertEquals(1, index.version)
    }

    fun testDependsOnFileContent() {
        val index = MethodFileBasedIndex()
        assertTrue(index.dependsOnFileContent())
    }

    fun testHasSnapshotMapping() {
        val index = MethodFileBasedIndex()
        assertTrue(index.hasSnapshotMapping())
    }

    fun testIndexRegistered() {
        val instance = FileBasedIndex.getInstance()
        val indexId = MethodFileBasedIndex.INDEX_ID
        
        // Verify index is registered (keyDescriptor should not throw)
        assertNotNull(indexId)
        
        // Try to get index extension
        val extensions = FileBasedIndex.EXTENSION_POINT_NAME.extensions
        val ourIndex = extensions.find { it.name == indexId }
        assertNotNull("Index should be registered", ourIndex)
    }
}

