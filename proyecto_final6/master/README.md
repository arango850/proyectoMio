master module

Esqueleto del módulo Master responsable de la orquestación del procesamiento histórico y de recibir resultados de streaming.

Uso esperado (final):

java -jar master.jar {{MASTER_ENDPOINT}} {{CSV_PATH}} {{OUTPUT_PATH}} {{NUM_PARTITIONS}} {{WORKER_ENDPOINT_1}},{{WORKER_ENDPOINT_2}}

Contenido creado:
- `master/pom.xml` — POM con dependencias y `maven-shade-plugin` para generar jar ejecutable.
- `MasterApp.java` — App principal que crea particiones y las despacha.
- `Partitioner.java` — Cuenta líneas y genera rangos start..end por partición.
- `Orchestrator.java` — Encargado de asignar particiones a Workers (placeholder para ICE client).
- `MasterServiceImpl.java` — Receptor de resultados parciales y lógica de agregación ponderada.

Notas:
- La integración con ZeroC ICE (clases generadas por `slice2java`) se implementará en `Orchestrator` y en un `MasterService` real cuando los stubs estén disponibles.
- `MasterServiceImpl.writeFinalOutputs()` genera `velocidades_historicas.csv` y `metrics_historico_{timestamp}.txt` en `{{OUTPUT_PATH}}`.

Siguientes pasos sugeridos:
- Implementar `slice2java` y generar stubs from `common/src/main/slice/sitm.ice` ejecutando `mvn -Dslice2java.exec=slice2java package`.
- Añadir llamadas ICE reales en `Orchestrator` y exponer `MasterServiceI` como servant para que los Workers puedan invocar `reportResult`.
- Implementar lógica de reintentos/ACKs en el cliente y servidor ICE para Reliable Messaging.
