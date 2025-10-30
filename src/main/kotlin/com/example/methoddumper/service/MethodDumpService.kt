package com.example.methoddumper.service

import com.example.methoddumper.index.MethodFileBasedIndex
import com.example.methoddumper.model.MethodEntry
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Service(Service.Level.PROJECT)
class MethodDumpService(private val project: Project) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun dumpMethodsToJson(outputPath: Path = defaultOutputPath()): Path {
        val scope = GlobalSearchScope.projectScope(project)
        val methods = collectMethods(scope)
        writeJson(outputPath, methods)
        refreshVirtualFile(outputPath)
        return outputPath
    }

    fun collectMethods(scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)): List<MethodEntry> {
        val index = FileBasedIndex.getInstance()
        return ReadAction.compute<List<MethodEntry>, RuntimeException> {
            val results = mutableListOf<MethodEntry>()
            
            // Process each file in the index that matches our scope
            index.processAllKeys(MethodFileBasedIndex.INDEX_ID, Processor { key ->
                // Get values only for files within the specified scope
                val files = index.getContainingFiles(MethodFileBasedIndex.INDEX_ID, key, scope)
                if (files.isNotEmpty()) {
                    val values = index.getValues(MethodFileBasedIndex.INDEX_ID, key, scope)
                    results.addAll(values)
                }
                true
            }, scope, null)
            
            results.sortBy { it.name }
            results.toList()
        }
    }

    fun defaultOutputPath(): Path {
        val baseDir = project.basePath?.let { Path.of(it) }
            ?: project.guessProjectDir()?.let { Path.of(it.path) }
            ?: Path.of(System.getProperty("java.io.tmpdir"), project.name)
        val targetDir = baseDir.resolve(".idea").resolve("method-dump")
        return targetDir.resolve("methods.json")
    }

    private fun writeJson(path: Path, methods: List<MethodEntry>) {
        val parent = path.parent
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent)
        }

        val json = gson.toJson(methods)
        Files.writeString(
            path,
            json,
            Charsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    }

    private fun refreshVirtualFile(path: Path) {
        val fileSystem = VirtualFileManager.getInstance()
        val parent = path.parent ?: return
        fileSystem.refreshAndFindFileByNioPath(parent)?.refresh(false, true)
    }

    companion object {
        fun getInstance(project: Project): MethodDumpService = project.service()
    }
}
