# Development Logbook - Method Dumper Plugin

## Executive Summary

**Method Dumper** is an IntelliJ IDEA plugin that tracks Java method names and bodies within an open project and dumps them into a single JSON file. This document details the complete development process, including problems encountered, diagnostic actions, and implemented solutions.

---

## Phase 1: Project Initialization

### 📅 Step 1.1: Initial Project Setup
**Date**: October 29, 2025 18:00:00

**Objective**: Create the base project structure using Gradle IntelliJ Plugin with Kotlin.

**Actions taken**:
- Created project using Gradle IntelliJ Plugin template
- Configured `build.gradle.kts` with basic dependencies
- Defined `gradle.properties` with plugin metadata
- Configured `settings.gradle.kts`
- Added minimal `plugin.xml` with basic plugin information

**Result**: ✅ Base project created with standard IntelliJ plugin structure

**Files created**:
```
build.gradle.kts
gradle.properties
settings.gradle.kts
gradle/wrapper/
src/main/resources/META-INF/plugin.xml
```

**Build check**: ✅ Project compiles successfully with empty Kotlin source

---

## Phase 2: Data Model Definition

### 📅 Step 2.1: MethodEntry Model Creation
**Date**: October 29, 2025 20:00:00

**Objective**: Define the data structure for storing method information.

**Actions taken**:
- Created `src/main/kotlin/com/example/methoddumper/model/MethodEntry.kt`
- Defined data class with `name` and `body` fields (project requirements)

**Code implemented**:
```kotlin
data class MethodEntry(
    val name: String,
    val body: String
)
```

**Result**: ✅ Simple and effective data model created

**Problems encountered**: None in this initial phase

**Build check**: ✅ Compiles successfully (simple data class with no dependencies)

---

## Phase 3: File-Based Index Implementation

### 📅 Step 3.1: FileBasedIndex Research
**Date**: After Phase 2

**Objective**: Understand how to implement file-based indexes in IntelliJ Platform.

**Actions taken**:
- Reviewed IntelliJ Platform SDK documentation on `FileBasedIndexExtension`
- Analyzed examples in IntelliJ IDEA source code
- Identified required components:
  - `FileBasedIndexExtension<TKey, TValue>`
  - `DataIndexer` for file processing
  - `KeyDescriptor` for key serialization
  - `DataExternalizer` for value serialization

**Result**: ✅ Clear understanding of index architecture

---

### 📅 Step 3.2: MethodFileBasedIndex Implementation (First Version)
**Date**: October 30, 2025 08:00:00

**Objective**: Create the index skeleton that will process Java files and extract methods.

**Actions taken**:
- Created `MethodFileBasedIndex.kt` extending `FileBasedIndexExtension<String, MethodEntry>`
- Basic skeleton implementation of `getIndexer()` using `DataIndexer`
- Created `MethodDataExternalizer.kt` implementing `DataExternalizer<MethodEntry>`
- Used `IOUtil.writeUTF/readUTF` for efficient serialization

**Code implemented**:
```kotlin
class MethodFileBasedIndex : FileBasedIndexExtension<String, MethodEntry>() {
    override fun getIndexer(): DataIndexer<String, MethodEntry, FileContent> = DataIndexer { inputData ->
        // Skeleton implementation - will be completed in next commit
        emptyMap()
    }
    // ... other required overrides
}
```

**Problems encountered**:
1. **Problem A3.2.1**: How to filter only Java files?
   - **Diagnosis**: The index needs to process only `.java` files
   - **Action**: Research on `DefaultFileTypeSpecificInputFilter`
   - **Solution**: ✅ Implementation of `FILE_FILTER` using `JavaFileType.INSTANCE`

2. **Problem A3.4.1**: Which utility to use for UTF-8 serialization?
   - **Diagnosis**: Research on IntelliJ Platform APIs
   - **Action**: Review of examples in IntelliJ source code
   - **Solution**: ✅ Using `IOUtil` which correctly handles UTF-8 and is standard on the platform

**Result**: ✅ Basic index structure that compiles successfully

**Build check**: ✅ Compiles (skeleton implementation with empty indexer)

---

### 📅 Step 3.3: Method Visitor Implementation
**Date**: October 30, 2025 10:00:00

**Objective**: Extract methods from the PSI tree with their complete bodies.

**Actions taken**:
- Created inner class `MethodVisitor` extending `JavaRecursiveElementVisitor`
- Implemented `visitMethod()` to capture methods
- Handled methods without bodies (abstract, interfaces)
- Created `computeKey()` for unique method identification
- Updated `plugin.xml` to register fileBasedIndex extension

**Code implemented**:
```kotlin
private class MethodVisitor(private val target: MutableMap<String, MethodEntry>) 
    : JavaRecursiveElementVisitor() {
    
    override fun visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        val body = method.body ?: return  // Only methods with body
        val key = computeKey(method)
        val bodyText = body.text ?: ""
        target[key] = MethodEntry(name = method.name, body = bodyText)
    }
}
```

**Problems encountered**:
1. **Problem A3.3.1**: Some methods don't have bodies (abstract)
   - **Diagnosis**: `method.body` can be `null` for abstract methods
   - **Action**: Explicit check with early return
   - **Solution**: ✅ Filtered out methods without body

2. **Problem A3.2.2**: How to generate unique keys for methods?
   - **Diagnosis**: Methods can have the same name in different classes or with different parameters
   - **Action**: Analysis of how to uniquely identify a method
   - **Solution**: ✅ Created `computeKey()` that combines: `className#methodName(parameters)@offset`

**Result**: ✅ Functional visitor that extracts methods with their bodies

**Build check**: ✅ Compiles complete index implementation with visitor

---

## Phase 4: Dump Service Implementation

### 📅 Step 4.1: MethodDumpService Creation
**Date**: October 30, 2025 14:00:00

**Objective**: Create a service that aggregates methods from the index and writes them to JSON.

**Actions taken**:
- Created `MethodDumpService.kt` as project-level service
- Used `@Service(Service.Level.PROJECT)` for dependency injection
- Added Gson dependency to `build.gradle.kts` for JSON serialization
- Implemented `collectMethods()` using `FileBasedIndex.getInstance()`
- Implemented `dumpMethodsToJson()` for writing JSON files
- Used `ReadAction.compute()` for thread-safe index access

**Code implemented**:
```kotlin
@Service(Service.Level.PROJECT)
class MethodDumpService(private val project: Project) {
    fun collectMethods(scope: GlobalSearchScope): List<MethodEntry> {
        return ReadAction.compute {
            // Aggregate methods from index
        }
    }
    
    fun dumpMethodsToJson(outputPath: Path): Path {
        val methods = collectMethods()
        writeJson(outputPath, methods)
        return outputPath
    }
}
```

**Problems encountered**:
1. **Problem A4.1.2**: Code must execute in `ReadAction`
   - **Diagnosis**: Index access requires execution within `ReadAction`
   - **Action**: Initial tests showed threading errors
   - **Solution**: ✅ Wrapping in `ReadAction.compute()` for thread-safe access

2. **Problem A4.2.1**: How to filter methods only from project (exclude external libraries)?
   - **Diagnosis**: The index includes methods from all dependencies
   - **Action**: Research on `GlobalSearchScope`
   - **Solution**: ✅ Using `GlobalSearchScope.projectScope(project)` to filter only project code

3. **Problem A4.3.2**: Directory may not exist
   - **Diagnosis**: `Files.writeString()` fails if parent directory doesn't exist
   - **Action**: Tests showed `NoSuchFileException` exceptions
   - **Solution**: ✅ Explicit directory creation with `Files.createDirectories()`

**Result**: ✅ Service capable of collecting all project methods and writing JSON

**Build check**: ✅ Compiles successfully with Gson dependency

---

## Phase 5: Automatic Execution on Project Open

### 📅 Step 5.1: ProjectActivity Research
**Date**: After Phase 4

**Objective**: Implement automatic dump execution when opening a project.

**Actions taken**:
- Researched `ProjectActivity` API in IntelliJ Platform
- Analyzed how to wait for project to be fully indexed
- Identified necessary APIs to wait for correct state

**Problems encountered**:
1. **Problem A5.1.1**: When to execute the dump?
   - **Diagnosis**: Need to wait for index to be complete
   - **Action**: Reviewed documentation on project lifecycle
   - **Solution**: ✅ Decision to use `ProjectActivity` with appropriate waits

---

### 📅 Step 5.2: MethodDumpProjectActivity Implementation
**Date**: October 30, 2025 18:00:00

**Objective**: Create activity that executes on project open and generates JSON.

**Actions taken**:
- Created `MethodDumpProjectActivity.kt` implementing `ProjectActivity`
- Implemented `execute()` with indexing waits
- Used `Observation.awaitConfiguration(project)` to wait for configuration
- Used `project.waitForSmartMode()` to wait for "smart" mode (complete indexing)
- Updated `plugin.xml` to register projectActivity extension

**Code implemented**:
```kotlin
class MethodDumpProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        Observation.awaitConfiguration(project)
        project.waitForSmartMode()
        
        val service = project.service<MethodDumpService>()
        service.dumpMethodsToJson()
    }
}
```

**Problems encountered**:
1. **Problem A5.2.1**: JSON generated before index was complete
   - **Diagnosis**: First version didn't wait for indexing, resulting in empty JSON
   - **Action**: Log analysis and tests with large projects
   - **Solution**: ✅ Added `Observation.awaitConfiguration()` and `waitForSmartMode()`

2. **Problem A5.2.2**: Main thread blocking during indexing
   - **Diagnosis**: `waitForSmartMode()` can block if called on main thread
   - **Action**: Reviewed `ProjectActivity` documentation (it's a suspend function)
   - **Solution**: ✅ `ProjectActivity.execute()` is suspend, allows async execution

**Result**: ✅ Activity that automatically generates JSON after indexing

**Build check**: ✅ Compiles successfully

---

**Last updated**: October 30, 2025  
**Plugin Version**: 0.1.0  
**Target IDE**: IntelliJ IDEA 2025.2.4 (Build #IU-252.27397.103)