# Method Dumper IntelliJ Plugin

Plugin for IntelliJ IDEA 2025.2.4 (Build #IU-252.27397.103) that indexes Java methods in an open project and dumps them into a single JSON file for future processing.

## Features
- **File-Based Index**: Efficiently stores method information per file using IntelliJ's indexing infrastructure
- **PSI-based parsing**: Uses Program Structure Interface to extract method names and bodies from Java files
- **Automatic dumping**: Generates `methods.json` on project open (after indexing completes)
- **Manual action**: `Tools → Dump Java Methods to JSON` to regenerate the file on demand
- **JSON output**: List of objects with `{ "name": "...", "body": "..." }` format ready for AI Assistant context processing

## Requirements
- **IntelliJ IDEA Ultimate** 2025.2.4 (or compatible with build 252)
- **JDK 17** or higher
- **Gradle 8.14** (wrapper included)

## Building the Plugin

```bash
cd jet-brain-task
./gradlew clean buildPlugin
```

The plugin ZIP will be generated at: `build/distributions/Method Dumper-0.1.0.zip`

> **Note**: If you encounter issues with `./gradlew`, you may need to run `gradle wrapper` first to generate the wrapper files.

## Installation

### Option 1: Install from Disk (Recommended)
1. Build the plugin using the command above
2. In IntelliJ IDEA: `Settings/Preferences → Plugins → ⚙️ → Install Plugin from Disk...`
3. Select the ZIP file: `build/distributions/Method Dumper-0.1.0.zip`
4. Restart IntelliJ IDEA
5. Open a Java project (e.g., `spring-petclinic`)
6. Wait for indexing to complete
7. Check the generated JSON at: `.idea/method-dump/methods.json`

### Option 2: Run in Development Mode
```bash
./gradlew runIde
```

This will start a new IntelliJ IDEA instance with the plugin installed. Note: This may require setting `IDEA_HOME` environment variable if you want to use an existing installation.

> **Important**: Due to compatibility issues with the Gradle IntelliJ Plugin and the bundled Kotlin plugin in IntelliJ 2025.2.4, the `runIde` task may fail. Use **Option 1** (install from disk) instead.

## Usage

### Automatic Generation
When you open a Java project:
1. The plugin waits for indexing to complete using:
   - `Observation.awaitConfiguration(project)`
   - `project.waitForSmartMode()`
2. Automatically generates `methods.json` in `.idea/method-dump/`

### Manual Generation
To regenerate the file at any time:
1. Go to `Tools → Dump Java Methods to JSON`
2. Wait for the notification: "Method dump written to ... (N methods)"
3. The file will be updated with the latest indexed methods

### Verifying Updates
To verify that method updates are reflected in the JSON:
1. Modify a Java method in your project
2. Wait for re-indexing to complete
3. Run `Tools → Dump Java Methods to JSON`
4. Check that the updated method body is reflected in the JSON file

## Project Structure

```
jet-brain-task/
├── src/main/kotlin/com/example/methoddumper/
│   ├── model/
│   │   └── MethodEntry.kt              # Data class for method entries
│   ├── index/
│   │   ├── MethodFileBasedIndex.kt     # File-based index implementation
│   │   └── MethodDataExternalizer.kt   # Serialization for index storage
│   ├── service/
│   │   └── MethodDumpService.kt        # Service to collect and write JSON
│   ├── activity/
│   │   └── MethodDumpProjectActivity.kt # Runs on project open
│   └── actions/
│       └── DumpMethodsAction.kt        # Manual action in Tools menu
├── src/main/resources/META-INF/
│   └── plugin.xml                      # Plugin configuration
├── samples/
│   └── spring-petclinic-methods.json   # Sample output (234 methods)
├── build.gradle.kts                    # Build configuration
├── gradle.properties                   # Plugin metadata
└── README.md                           # This file
```

## Architecture

### MethodFileBasedIndex
- Implements `FileBasedIndexExtension<String, MethodEntry>`
- Uses a `JavaRecursiveElementVisitor` to traverse PSI tree and extract methods
- Stores method entries with unique keys based on: class name + method name + parameters + offset
- Only indexes methods with bodies (skips abstract methods and interfaces)

### MethodDumpService
- Project-level service (`@Service(Service.Level.PROJECT)`)
- Aggregates all indexed methods using `FileBasedIndex.getInstance()`
- Filters methods to only include project scope (excludes external libraries)
- Writes sorted JSON output using Gson with pretty printing

### MethodDumpProjectActivity
- Implements `ProjectActivity` for startup execution
- Waits for proper indexing state before dumping
- Runs asynchronously to avoid blocking project open

### DumpMethodsAction
- Extends `DumbAwareAction` to work during indexing
- Runs in background task with progress indicator
- Shows notification with result (success or error)

## Sample Output

The plugin was tested with the [spring-petclinic](https://github.com/spring-projects/spring-petclinic) project and generated **234 methods** from 30 Java files.

Sample JSON structure:
```json
[
  {
    "name": "findOwner",
    "body": "{\n\t\tOptional<Owner> optionalOwner = this.owners.findById(ownerId);\n\t\tOwner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(\n\t\t\t\t\"Owner not found with id: \" + ownerId + \". Please ensure the ID is correct \"));\n\t\treturn owner;\n\t}"
  },
  {
    "name": "addPet",
    "body": "{\n\t\tif (pet.isNew()) {\n\t\t\tgetPets().add(pet);\n\t\t}\n\t}"
  }
]
```

See full output in `samples/spring-petclinic-methods.json`.

## Known Limitations

1. **Gradle Wrapper**: The `gradle-wrapper.jar` is included in the repository
2. **runIde Task**: May fail due to Kotlin plugin compatibility issues with IntelliJ 2025.2.4. Use manual installation instead.
3. **buildSearchableOptions**: Disabled in `build.gradle.kts` due to the same compatibility issues
4. **Java Only**: Only indexes `.java` files. Kotlin and other JVM languages are not supported
5. **Method Bodies**: Serialized as raw text with original formatting (braces, indentation). No minification or normalization applied
6. **Scope Filtering**: Uses `GlobalSearchScope.projectScope()` to exclude external libraries, but this may include test files

## Technical Details

### Dependencies
- **Kotlin**: 2.2.0 (to match IntelliJ IDEA 2025.2.4)
- **Gson**: 2.11.0 (for JSON serialization)
- **IntelliJ Platform SDK**: 252.27397.103
- **Gradle IntelliJ Plugin**: 1.17.3

### Configuration
Key settings in `gradle.properties`:
```properties
pluginGroup=com.example.methoddumper
pluginName=Method Dumper
pluginVersion=0.1.0
javaVersion=17
intellijVersion=252.27397.103
intellijType=IU
kotlinVersion=2.2.0
```

### Compatibility
- **Since Build**: 252.27397
- **Until Build**: 252.*

## Troubleshooting

### Empty JSON Output
If the generated JSON is empty (`[]`):
1. Verify that the project has Java source files
2. Wait for full indexing to complete (check the progress bar at the bottom)
3. Try `File → Invalidate Caches → Invalidate and Restart`
4. Manually run `Tools → Dump Java Methods to JSON`
5. Check IntelliJ logs for `[MethodDumper]` messages

### Build Errors
If you encounter build errors:
1. Ensure you have JDK 17 installed: `java --version`
2. Clear Gradle caches: `./gradlew clean`
3. Regenerate wrapper: `gradle wrapper`
4. Try building again: `./gradlew buildPlugin`

### Plugin Not Loading
If the plugin doesn't appear after installation:
1. Verify installation in `Settings → Plugins → Installed`
2. Check that "Method Dumper" is enabled
3. Restart IntelliJ IDEA
4. Check if `Java` plugin is enabled (required dependency)

## License
Educational use.
