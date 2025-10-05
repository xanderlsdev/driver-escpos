# driver-escpos

Una librería Java para controlar impresoras térmicas compatibles con **ESC/POS** mediante **Bluetooth**, **USB** o **red (TCP/IP)**.
Actualmente soporta funciones como envío de comandos ESC/POS, procesamiento de imágenes, gestión de trabajos de impresión, descubrimiento de impresoras, entre otras.

---

## Índice

* [Características](#características)
* [Archivos principales](#archivos-principales)
* [Instalación / uso](#instalación--uso)
* [Ejemplos de uso](#ejemplos-de-uso)
* [Contribuir](#contribuir)
* [Licencia](#licencia)

---

## Características

* Conexión vía Bluetooth, USB o red (TCP/IP) para comunicarse con impresoras ESC/POS.
* Soporte para envío de texto, imágenes (procesamiento de imágenes) y comandos ESC/POS (corte de papel, alimentación de líneas, justificación, etc.).
* Cola de impresión (PrintQueue) para gestionar múltiples trabajos.
* Descubrimiento de impresoras en red o por USB.
* Modular: los componentes (procesador de imágenes, gestor de conexiones, comandos ESC/POS) están separados en clases independientes.

---

## Archivos principales

Aquí tienes una descripción de los componentes más importantes del proyecto:

| Archivo / Clase          | Función / Responsabilidad                                                                    |
| ------------------------ | -------------------------------------------------------------------------------------------- |
| `ConnectionManager.java` | Gestiona conexiones genéricas a la impresora (Bluetooth, USB, red).                          |
| `EscPosCommands.java`    | Proporciona los comandos ESC/POS básicos (inicializar, cortar, alimentación, etc.).          |
| `ImageProcessor.java`    | Convierte imágenes para que puedan ser impresas (rasterización, binarización, dither, etc.). |
| `PrintJob.java`          | Representa un trabajo de impresión (contenido, formato, destino).                            |
| `PrintQueueManager.java` | Administra una cola de trabajos de impresión, enviándolos sucesivamente.                     |
| `PrinterConfig.java`     | Configuraciones de impresora (ancho, perfiles, tamaño, etc.).                                |
| `PrinterDiscovery.java`  | Descubre impresoras disponibles (por USB, red, Bluetooth).                                   |
| `PrinterPlugin.java`     | Extensión o interfaz para distintos tipos de impresoras (plugins).                           |
| `PrinterService.java`    | Servicio o fachada para exponer operaciones de impresión al resto de la aplicación.          |
| `PrinterType.java`       | Define tipos de impresoras soportadas.                                                       |
| `UsbDeviceHelper.java`   | Ayudas y utilitarios para dispositivos USB (enumeración, permisos, etc.).                    |
| `UsbOutputStream.java`   | Flujo de salida sobre USB para enviar los bytes a la impresora.                              |
| `PermissionManager.java` | Gestiona permisos necesarios (por ejemplo, en Android, si aplica).                           |
| `MainActivity.java`      | Punto de entrada (usualmente en contexto Android) o ejemplo de uso.                          |

---

## Instalación / uso

1. Agrega el proyecto como dependencia (puede ser mediante **Maven**, **Gradle** o importando directamente el código fuente).
2. Asegúrate de que tu aplicación tenga permisos necesarios (USB, Bluetooth, red) para acceder al hardware.
3. Inicializa la conexión con la impresora (Bluetooth, USB o TCP).
4. Crea un **PrintJob** con el contenido deseado (texto, imagen, etc.).
5. Envía el trabajo de impresión mediante el **PrintQueueManager** o directamente a través del servicio.
6. Utiliza los comandos ESC/POS que necesitas (cortar papel, alimentar líneas, justificación, etc.).

### Dependencias

Sin dependencias

---

## Ejemplos de uso

Aquí tienes un ejemplo hipotético (pseudo-código) de cómo podría utilizarse:

```java
// 1. Descubrir impresora (por red, USB o Bluetooth)
PrinterDiscovery discovery = new PrinterDiscovery();
PrinterInfo printer = discovery.findFirst();

// 2. Configurar la impresora
PrinterConfig config = new PrinterConfig();
config.setWidth(384); // por ejemplo, ancho en puntos
config.setProfile("TM-T88III"); // perfil si aplica

// 3. Crear conexión
ConnectionManager conn = ConnectionManager.open(printer, config);

// 4. Crear trabajo de impresión
PrintJob job = new PrintJob();
job.setText("Hola mundo\n");
job.setImage(myLogoBitmap);  // si incluye logo
job.cutPaper();

// 5. Enviar trabajo
PrintQueueManager queue = new PrintQueueManager(conn);
queue.enqueue(job);
queue.start();  // o de forma síncrona: queue.processAll();
```

También podrías usar directamente `PrinterService` como fachada para ocultar estos detalles internos.

Para el procesamiento de imágenes, `ImageProcessor` se encarga de convertir la imagen en un formato imprimible (binario, adaptar al ancho, dither, etc.).

---

## Contribuir

* Siéntete libre de hacer **fork** del repositorio.
* Abre **issues** para reportar errores o proponer mejoras.
* Envía **pull requests** con nuevas funciones, correcciones o mejoras de rendimiento.
* Es útil documentar cada método nuevo y proporcionar ejemplos de uso.

---

## Licencia

Este proyecto está bajo la licencia **MIT**. ([GitHub][1])
Puedes usar, modificar y distribuir libremente el código siempre que mantengas la atribución de derechos de autor original.
