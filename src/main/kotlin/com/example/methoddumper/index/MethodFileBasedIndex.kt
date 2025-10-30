package com.example.methoddumper.index

import com.example.methoddumper.model.MethodEntry
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

class MethodFileBasedIndex : FileBasedIndexExtension<String, MethodEntry>() {

    override fun getName(): ID<String, MethodEntry> = INDEX_ID

    override fun getVersion(): Int = 1

    override fun dependsOnFileContent(): Boolean = true

    override fun getIndexer(): DataIndexer<String, MethodEntry, FileContent> = DataIndexer { inputData ->
        // Skeleton implementation - will be completed in next commit
        emptyMap()
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): MethodDataExternalizer = MethodDataExternalizer

    override fun getInputFilter(): DefaultFileTypeSpecificInputFilter = FILE_FILTER

    override fun hasSnapshotMapping(): Boolean = true

    companion object {
        val INDEX_ID: ID<String, MethodEntry> = ID.create("MethodFileBasedIndex")
        private val FILE_FILTER = DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE)
    }
}
