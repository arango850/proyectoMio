common module

This module contains the Slice (`.ice`) definitions and the Maven configuration to run `slice2java` to generate Java bindings.

Prerequisites
- Java 11+
- Maven 3.6+
- ZeroC ICE installed and `slice2java` available in your PATH, or set the Maven property `-Dslice2java.exec=/path/to/slice2java` when running Maven.

How it works
1. The `exec-maven-plugin` configured in `common/pom.xml` calls `slice2java` in the `generate-sources` phase.
2. Generated Java sources are written to `${project.build.directory}/generated-sources/slice`.
3. `build-helper-maven-plugin` adds that directory to the compilation sources before the compile phase.

Files added
- `src/main/slice/sitm.ice` — Slice definitions for `WorkerService`, `MasterService`, DTOs and structs used across modules.

Placeholders
The runtime endpoints and paths are left as placeholders to be supplied at execution:
- `{{MASTER_ENDPOINT}}`
- `{{WORKER_ENDPOINT_1}}`
- `{{WORKER_ENDPOINT_2}}`
- `{{WORKER_ENDPOINT_3}}`
- `{{STREAM_ENDPOINT}}`
- `{{CSV_PATH}}`
- `{{OUTPUT_PATH}}`
- `{{NUM_PARTITIONS}}`

Build
Run:

```bash
mvn -Dslice2java.exec=slice2java clean package
```

If `slice2java` is not on PATH, provide full path:

```bash
mvn -Dslice2java.exec="C:\\Program Files\\ZeroC\\Ice-3.7.0\\bin\\slice2java" clean package
```

Next steps
- Implement Java utilities and DTOs in `common/src/main/java` (complementing generated classes).
- Create module `master`, `worker` and `streaming` skeletons and their POMs.

Notes
- The `.ice` file intentionally avoids maps for portability and uses sequences of `VelocityDatum` to transfer results.
- Documented patterns (Master–Worker, Producer–Consumer, Asynchronous Queuing, Reliable Messaging, Separable Dependencies) will be added as comments in the Java code where implemented.
