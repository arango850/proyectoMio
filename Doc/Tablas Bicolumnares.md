## Caso de uso - ID 1

| Campo | Detalle |
|---|---|
| **ID** | 1 |
| **Nombre del caso de uso** | Visualizar movilidad por zona |
| **Actor principal** | Controlador de operación |
| **Actores Secundarios** | Subsistema de visualización<br/>Subsistema de datos |
| **Precondición** | El controlador de operación ya ha iniciado sesión y tiene claro cuales son las zonas y arcos que va a visualizar, y ya hay datos de eventos y datagramas |
| **Contexto** | El controlador de operación quiere visualizar el análisis de movilidad por zona |
| **Acciones del controlador de operación** | 1. El controlador selecciona la opción de visualizar movilidad<br/>2. El sistema muestra un formulario donde se pregunta por la zona a visualizar<br/>3. El controlador digita la zona que quiere visualizar<br/>4. El sistema consulta en el sistema de datos si el controlador tiene asignado esa zona<br/>4a. En caso de estar asignado, el subsistema de visualizaciones solicita información de velocidad promedio de la zona, arcos de la zona y eventos de la zona al subsistema de los datos; en este se consultan estos datos mediante los servicios establecidos, estos ya han sido procesados y analizados mediante la recolección de datos que son actualizados cada 30 segundos. Estos aparecen visualizados en la zona.<br/>4b. Se le manda un mensaje de error pidiendo ingresar nuevamente la zona.<br/>5. El controlador observa la información de movilidad de la zona |
| **Acciones del sistema** | 2. El sistema muestra un formulario donde se pregunta por la zona a visualizar (corresponde a la interacción 2 anterior).<br/>4. El sistema consulta en el sistema de datos si el controlador tiene asignado esa zona (ver interacción 4a/4b). |
| **Restricciones** | - La zona tiene que estar previamente asignada al controlador.<br/>- Solamente se pueden ver los arcos de la zona.<br/>- Los datos deben actualizarse cada 30 segundos. |
| **Requerimientos funcionales** | - El sistema debe permitirle a cada uno de los 40 controladores de operación visualizar en tiempo real sus zonas asignadas, y para cada zona, la velocidad promedio por arco, de los arcos que estén en dicha zona.<br/>- El sistema debe mantener un análisis actualizado del comportamiento del SITM-MIO, adaptándose al crecimiento en el volumen de datos (datagramas) y al número de fuentes conectadas (buses, estaciones, sensores, etc.). Debe poder procesar los nuevos eventos sin afectar la disponibilidad del sistema y asegurar que los resultados analíticos reflejen la realidad operativa.<br/>- El sistema debe permitir asignar rutas y zonas de la ciudad para ser supervisadas a un controlador de operación.<br/>- El sistema debe realizar análisis que permitan estimar variables de interés relacionadas con la movilidad, tales como tiempos promedio de viaje por arco con base en datos históricos. Esta información debe poder actualizarse en tiempo real, considerando los datos históricos y los datos que vayan apareciendo en tiempo real.<br/>- El sistema debe ser capaz de recibir y procesar los grandes volúmenes de datos generados por los buses del SITM-MIO, tales como las posiciones GPS de los buses, eventos operativos y reportes de los controladores, combinando información histórica y los datos que se reciben en tiempo real. Todos estos datos deben ser persistidos en base de datos. |

---

## Caso de uso - ID 2

| Campo | Detalle |
|---|---|
| **ID** | 2 |
| **Nombre del caso de uso** | Consultar información del análisis generado |
| **Actor principal** | Ciudadano, Empresa, Entidad pública |
| **Actores secundarios** | Subsistema de visualización, Subsistema de datos |
| **Precondición** | El portal/servicio está disponible y existen los resultados analíticos publicados (velocidad por arco, tiempo), actualizado con datos históricos y en tiempo real. |
| **Contexto** | El usuario externo quiere conocer el tiempo promedio de viaje entre dos puntos o para una sola ruta y el estado general del sistema |
| **Acciones del Ciudadano / Empresa / Entidad pública** | 1. El usuario abre el portal y hace la acción de "Consultar tiempo promedio".<br/>3. El usuario ingresa origen y destino (o elige una ruta) y envía la consulta.<br/>7. El usuario revisa los resultados. |
| **Acciones del sistema** | 2. El sistema muestra el formulario con campos de origen y destino o selección de ruta y parámetros por defecto.<br/>4. La UI valida entradas y solicita al subsistema de datos el dataset requerido (velocidades por arco/tiempos) mediante servicios interoperables.<br/>4a. Si hay varias rutas candidatas, el sistema calcula tiempo estimado por alternativa y arma un listado ordenado.<br/>4b. Si falta información reciente, el sistema usa histórico como fallback y marca la frescura del dato (última actualización).<br/>6. El subsistema de datos ejecuta la consulta analítica sobre agregados (velocidad promedio por arco) y devuelve tiempo promedio de viaje para el trayecto.<br/>8. La UI presenta el tiempo estimado, ruta sobre mapa, hora de última actualización y nivel de confiabilidad; opcionalmente permite descargar/compartir.<br/>9. El sistema repite 4–8 con los nuevos parámetros. |
| **Restricciones** | - La información se expone mediante interfaces o servicios interoperables, con presentación adecuada y confiable.<br/>- Los datos de base provienen de eventos/datagramas enviados cada 30 s al DataCenter; la actualización depende de esa cadencia.<br/>- No se modifican datos operativos en este CU (solo lectura/consulta). |
| **Requerimientos Funcionales** | - El sistema debe ofrecer servicios para que ciudadanos/empresas/entidades consulten estado y tiempos promedio; acceso vía interfaces/servicios interoperables.<br/>- El sistema debe mostrar el análisis para estimar tiempos promedio de viaje por arco con histórico + actualización en tiempo real.<br/>- El sistema debe recibir/procesar grandes volúmenes y mantener análisis actualizado y escalable con nuevas fuentes/eventos. |
