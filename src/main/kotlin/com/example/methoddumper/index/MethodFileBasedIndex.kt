package com.example.methoddumper.index

import com.example.methoddumper.model.MethodEntry
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import kotlin.math.abs

class MethodFileBasedIndex : FileBasedIndexExtension<String, MethodEntry>() {

    override fun getName(): ID<String, MethodEntry> = INDEX_ID

    override fun getVersion(): Int = 1

    override fun dependsOnFileContent(): Boolean = true

    override fun getIndexer(): DataIndexer<String, MethodEntry, FileContent> = DataIndexer { inputData ->
        val psiFile = inputData.psiFile
        
        if (psiFile !is PsiJavaFile || psiFile.language != JavaLanguage.INSTANCE) {
            return@DataIndexer emptyMap()
        }

        val result = LinkedHashMap<String, MethodEntry>()
        psiFile.accept(MethodVisitor(result))
        result
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): MethodDataExternalizer = MethodDataExternalizer

    override fun getInputFilter(): DefaultFileTypeSpecificInputFilter = FILE_FILTER

    override fun hasSnapshotMapping(): Boolean = true

    private class MethodVisitor(
        private val target: MutableMap<String, MethodEntry>
    ) : JavaRecursiveElementVisitor() {

        override fun visitMethod(method: PsiMethod) {
            super.visitMethod(method)
            
            val body = method.body ?: return

            val key = computeKey(method)
            val bodyText = body.text ?: ""
            target[key] = MethodEntry(name = method.name, body = bodyText)
        }

        private fun computeKey(method: PsiMethod): String {
            val containingClass = method.containingClass
            val className = containingClass?.qualifiedName ?: fallbackClassName(containingClass)
            val parameters = method.parameterList.parameters.joinToString(",", prefix = "(", postfix = ")") {
                it.type.presentableText
            }
            val offset = method.textRange?.hashComponent()
            return buildString {
                append(className)
                append("#")
                append(method.name)
                append(parameters)
                append("@")
                append(offset)
            }
        }

        private fun fallbackClassName(psiClass: PsiClass?): String = when (psiClass) {
            null -> "<no-class>"
            else -> psiClass.name ?: "<anonymous>"
        }

        private fun TextRange?.hashComponent(): Int = when (this) {
            null -> 0
            else -> abs((startOffset * 31 + endOffset) * 31)
        }
    }

    companion object {
        val INDEX_ID: ID<String, MethodEntry> = ID.create("MethodFileBasedIndex")
        private val FILE_FILTER = DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE)

        fun requestRebuild(project: Project) {
            FileBasedIndex.getInstance().requestRebuild(INDEX_ID)
        }
    }
}
