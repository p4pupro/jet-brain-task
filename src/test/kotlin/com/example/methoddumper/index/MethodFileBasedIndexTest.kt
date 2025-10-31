package com.example.methoddumper.index

import com.intellij.util.indexing.FileBasedIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MethodFileBasedIndexTest {

    @Test
    fun testIndexIdNotNull() {
        assertNotNull(MethodFileBasedIndex.INDEX_ID)
    }

    @Test
    fun testIndexVersion() {
        val index = MethodFileBasedIndex()
        assertEquals(1, index.version)
    }

    @Test
    fun testDependsOnFileContent() {
        val index = MethodFileBasedIndex()
        assertTrue(index.dependsOnFileContent())
    }

    @Test
    fun testHasSnapshotMapping() {
        val index = MethodFileBasedIndex()
        assertTrue(index.hasSnapshotMapping())
    }

    @Test
    fun testIndexIdCreation() {
        val indexId = MethodFileBasedIndex.INDEX_ID
        assertNotNull("Index ID should not be null", indexId)
        assertEquals("MethodFileBasedIndex", indexId.name)
    }
}
