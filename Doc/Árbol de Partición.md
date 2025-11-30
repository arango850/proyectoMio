# Árbol de particionamiento — Estimación de velocidad promedio por arcos (SITM‑MIO)

**Propósito**

Este documento describe, a partir del diagrama de árbol, la ruptura funcional del subsistema encargado de estimar la velocidad promedio por arco en el SITM‑MIO. El objetivo es generar una descripción textual clara de los componentes, su flujo de datos y recomendaciones prácticas para organizar y particionar la información.

---

## Resumen ejecutivo

El sistema se compone de cuatro dominios principales: 1) origen de datos (buses y captura), 2) centro de datos (ingestión, almacenamiento y procesamiento), 3) modelo geoespacial (rutas, arcos y zonas) y 4) consumidores (usuarios y controladores). Los datagramas de los vehículos se reciben, validan y guardan; luego se mapean sobre el grafo de rutas para producir agregados de velocidad por arco que quedan disponibles mediante APIs y caches para consulta en tiempo real y análisis histórico.

---

## 1. Origen: Buses y captura de datagramas

**Descripción**

Los buses son la fuente primaria de telemetría: envían mensajes periódicos (datagramas) con posición, hora y otros atributos. La capa de captura recibe esos mensajes y los transforma en un formato estandarizado para el resto del sistema.

**Subcomponentes visibles en el árbol**:

* Captura de datos

  * Datagramas (payload bruto)
  * Interfaz de comunicación (puerta de ingestión)
  * ControladorBuses (gestión de la transmisión)

**Consideraciones**:

* Establecer un formato mínimo obligatorio en cada datagrama (id, timestamp, lat/lon, busId).
* Diseñar la interfaz de comunicación con tolerancia a pérdidas y reconexiones (buffering en el cliente si es necesario).

---

## 2. Centro de datos — Ingestión, almacenamiento y procesamiento

El centro de datos es el núcleo que recibe los datagramas, los organiza y ejecuta los procesos analíticos necesarios para calcular las velocidades promedio por arco.

### 2.1 Recepción

**Componentes**:

* Interfaz de comunicación (API/gateway/cola)
* Procesamiento inicial (validación y normalización)
* Controlador de ingestión (encola para persistencia/procesamiento)

**Funciones**:

* Validar campos obligatorios y descartar/etiquetar datagramas corruptos.
* Enriquecer con metadatos mínimos si faltan (por ejemplo, inferir `lineId` si es posible).

### 2.2 Almacenamiento

**Componentes**:

* Repositorio de datagramas (store frío/caliente según políticas)
* Estrategia de particionamiento físico

**Recomendación de organización** (forma general, no prescriptiva):

* Guardar datagramas crudos en un área particionada por periodo temporal (p. ej. mes o semana) y con sub‑carpetas o claves por ruta o vehículo según demanda de consultas.
* Mantener un esquema de metadatos que permita localizar rápidamente particiones por fecha, ruta y/o vehículo.

**Campos clave a persistir**: `datagram_id`, `timestamp`, `busId`, `lineId` (si aplica), `latitude`, `longitude`, `odometer`, `raw_payload`.

### 2.3 Organización y tablas derivadas

**Objetivo**: Transformar datagramas crudos en estructuras que permitan consultas rápidas de velocidad por arco.

* Tabla/colección de agregados por arco con ventanas temporales configurables (p. ej. por minuto/hora/día).
* Tabla de estado por vehículo para mapas en tiempo real (último punto por `busId`).

### 2.4 Procesamiento analítico

**Funciones principales**:

* Mapear cada datagrama a un `arcId` del grafo geoespacial.
* Calcular velocidades instantáneas (cuando sea posible) y actualizar promedios agregados para el arco correspondiente.

**Pasos de alto nivel**:

1. Consumir datagrama validado desde la cola o repositorio caliente.
2. Consultar índice espacial para obtener arcos candidatos.
3. Aplicar criterio geométrico (distancia/ángulo/ventana) para asociar el punto al arco.
4. Emitir un evento de actualización o escribir el registro en la tabla de agregados.

---

## 3. Modelo geoespacial e información de referencia

**Componentes mostrados en el diagrama**:

* Modelo geoespacial

  * Representación de rutas, arcos y nodos
  * Gestión de zonas operativas
  * Algoritmo de mapeo
  * Verificación de trayectorias por bus

**Responsabilidad**:

* Mantener las geometrías (líneas, stops y arcos) y los índices espaciales necesarios para asociar datagramas con arcos.
* Proveer utilidades para validar trayectorias (detección de desvíos, cruces, idas y vueltas).

**Artefactos sugeridos**:

* Tabla/capa `lines` (identificador y metadatos de la ruta)
* Tabla/capa `arcs` (identificador de arco, geometría, lineId)
* Tabla `zones` para agrupar arcos/estaciones por controlador operativo

---

## 4. Consumidores: Usuarios internos y externos

**Descripción del flujo**:
Usuarios (operadores, controladores, aplicaciones externas) acceden mediante servicios de consulta que exponen:

* Consultas históricas de velocidad promedio por arco
* Endpoints para obtener valores recientes (cache)
* Interfaces para controladores con requerimientos de baja latencia

**Componentes**:

* Servicios de consulta (API)
* Controladores (interfaces de operación)
* Capa de cache para resultados frecuentes o ventanas recientes

---

## 5. Estrategias de particionamiento y recomendaciones prácticas

A continuación se entregan pautas generales para organizar particiones sin imponer un único esquema:

**Opción recomendada (balance entre análisis y gestión)**

* Particionar por periodo temporal (p. ej. `YYYY_MM`) como primer nivel para facilitar archivado y limpieza.
* Dentro de cada periodo, segmentar por `lineId` o por `zoneId` si las consultas operativas suelen filtrarse por ruta o zona.

**Alternativa (si la operación prioriza consultas por ruta)**

* Primer nivel por `lineId`, segundo por `arcId` o por periodo temporal cuando se necesitan rangos de fecha.

**Consejos operativos**

* Ajustar la granularidad temporal: meses para volúmenes moderados, semanas/días si el volumen mensual crece demasiado.
* Mantener un índice o catálogo que relacione particiones con metadatos (tamaño, número de registros, rango temporal).
* Implementar caché para ventanas recientes (ej. últimos 5–15 minutos) y tablas de estado para mapas en tiempo real.

**Retención y downsampling**

* Conservar datagramas detallados por un período operativo (p. ej. 6–12 meses), luego reducir la resolución (promedios horarios/diarios) y mover los datos a almacenamiento frío.

**Sharding y balanceo**

* Si se usan shards, una clave prudente es una función hash sobre `lineId` o sobre (`lineId`, `year_month`) para distribuir rutas densas entre nodos.

---

## 6. Consultas típicas y cómo atenderlas

* "Velocidad promedio de un arco entre A y B": leer particiones por rango temporal y filtrar por `arcId`.
* "Estado en tiempo real de la flota": consultar `bus_status` (tabla de último punto) o cache.
* "Indicadores por zona para controladores": usar particionado por `zoneId` o índices secundarios y cache para baja latencia.

---

## 7. Siguientes pasos sugeridos

1. Validar volúmenes esperados (datagramas/día) para elegir granularidad de particiones.
2. Definir esquema final de tablas y el motor de almacenamiento objetivo (Postgres+PostGIS, ClickHouse, TimescaleDB, etc.).
3. Diseñar jobs de downsample y políticas de retención.
4. Implementar pruebas de carga sobre el patrón de particionamiento elegido.

---

**Autor**: Equipo del proyecto — documento generado a partir del árbol de partición entregado.
**Fecha**: 2025‑11‑30
