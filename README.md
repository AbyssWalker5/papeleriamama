# Papelería POS

Sistema de Punto de Venta (POS) diseñado específicamente para una papelería, construido con Java 21, JavaFX y SQLite.

## Requisitos Previos

- Java 21 (JDK 21)
- Apache Maven 3.9+
- Sistema Operativo Windows, macOS o Linux (Multiplataforma)

## Instalación y Ejecución

1. Clona o descarga el repositorio del proyecto.
2. Abre una terminal en el directorio raíz del proyecto.
3. Para compilar el proyecto:
   ```bash
   mvn compile
   ```
4. Para ejecutar el proyecto en modo desarrollo:
   ```bash
   mvn javafx:run
   ```

## Estructura de Directorios

Al ejecutarse por primera vez, la aplicación crea automáticamente los siguientes archivos en el directorio del usuario (por ejemplo `C:\Users\tu_usuario\.papeleria-pos` en Windows o `~/.papeleria-pos` en Linux/macOS):

- `papeleria.db` : Base de datos SQLite local (con soporte WAL para concurrencia y seguridad).
- `logs/application.log` : Archivo de registro diario con soporte para rotación de logs.
- `backups/` : Directorio donde se guardan los respaldos de la base de datos.
- `tickets/` : Directorio donde se imprimen los recibos o comprobantes en formato texto.

## Empaquetado para Producción

Para compilar el sistema en un solo archivo ejecutable (Fat JAR) que incluya todas las dependencias:

```bash
mvn package
```

Esto generará el archivo `target/pos-system-1.0.0.jar`.

Para ejecutarlo en cualquier máquina que tenga Java instalado:
```bash
java -jar target/pos-system-1.0.0.jar
```

## Arquitectura del Proyecto

El proyecto está diseñado bajo una **arquitectura en capas**:

- **UI / Controladores**: Gestionan la interacción del usuario y delegan operaciones.
- **Servicios**: Contienen las reglas de negocio y transacciones (e.g. `SaleService`, `InventoryService`).
- **DAO**: Manejan el acceso a datos e implementan consultas JDBC preparadas y parametrizadas para SQLite.
- **Modelos (POJOs)**: Representan las entidades sin acoplamiento.

### Gestión de Base de Datos y Transacciones

- **Transacciones Atómicas**: Las ventas y devoluciones se procesan en transacciones ACID. Si alguna parte falla (por ejemplo, inventario insuficiente), la operación completa se revierte automáticamente (`rollback`).
- **Migraciones Automáticas**: La clase `MigrationRunner` inicializa y actualiza la estructura de la base de datos a partir de scripts SQL numerados localizados en `src/main/resources/db/migrations`.
- **Integridad de Datos**: SQLite ha sido configurado con `PRAGMA foreign_keys=ON` por conexión y `PRAGMA journal_mode=WAL` para mejorar la seguridad frente a apagones o interrupciones.

## Módulos Principales

1. **Dashboard**: Resumen general de la actividad del día.
2. **Punto de Venta**: Carrito de compras, integración con escáner de código de barras, confirmación de ventas.
3. **Caja (Módulo de Efectivo)**: Aperturas, cierres de caja, registros de entradas/salidas de efectivo.
4. **Inventario**: Consulta y ajustes de inventario, bajas por merma o pérdida, y alertas de existencias.
5. **Historial de Ventas y Devoluciones**: Consulta inmutable de ventas registradas.
6. **Configuración**: Módulo de administración para información del comercio y mantenimiento.

## Respaldos y Restauración

El sistema cuenta con un módulo de respaldos integrado y un servicio de `BackupService`.

### Crear Respaldo
Puede crearse desde el panel de Configuración de la aplicación o se puede crear de forma manual. El sistema utiliza el comando interno de SQLite `VACUUM INTO` para garantizar que la copia de la base de datos sea íntegra, incluso mientras la base de datos está en uso.

### Restaurar Respaldo
1. Asegúrese de cerrar cualquier sesión de caja activa.
2. Vaya al apartado de Configuración y seleccione la opción "Restaurar Respaldo".
3. Seleccione el archivo `.db` válido.
4. El sistema verificará la integridad (`PRAGMA integrity_check`) del archivo antes de reemplazar la base de datos actual. Una copia de seguridad automática de la base actual se conservará por seguridad.

## Compatibilidad de Lector de Código de Barras

El módulo Punto de Venta es compatible con escáneres de código de barras USB configurados como emulación de teclado. Al posicionar el foco de texto en el buscador de productos y escanear, el escáner enviará el código seguido de un pulso de tecla <ENTER>, procesando el artículo e insertándolo directamente en el carrito.

