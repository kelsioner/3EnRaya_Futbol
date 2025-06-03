# ⚽ Proyecto TresEnRaya_Futbol

---

## 🎯 Propósito y Alcance

Este documento ofrece una introducción de alto nivel al proyecto **3EnRaya_Futbol**, un juego de tres en raya con temática de fútbol que se integra con **MongoDB** para validar el conocimiento de los jugadores.

El sistema combina la jugabilidad tradicional del tres en raya con **trivia de fútbol**, requiriendo que los jugadores nombren futbolistas reales que coincidan con combinaciones de categorías específicas para reclamar posiciones en el tablero.

---

## 🎮 Concepto del Juego

A diferencia del tres en raya tradicional donde los jugadores simplemente colocan X y O, este juego requiere que los jugadores demuestren su conocimiento de fútbol.

Cada celda del tablero representa la intersección de dos categorías (por ejemplo, un equipo y una posición), y los jugadores deben introducir el nombre de un futbolista real que coincida con ambas categorías para reclamar la casilla.

### 🧩 Mecánicas de Juego Principales

* **🔲 Tablero de 3x3** con combinaciones únicas.
* **📚 Categorías horizontales y verticales** (equipos, posiciones, países, dorsales, edades).
* **🧠 Validación de futbolistas reales** usando **MongoDB** con más de **276 registros**.
* **🏆 Reglas clásicas de victoria** del tres en raya + detección de empate/atasco.

---

## 🏗️ Arquitectura del Sistema

```mermaid
graph TD
  A[Sistemas Externos] --> B[Capa de Importación de Datos]
  B --> C[Capa de Acceso a Datos]
  C --> D[Capa de Lógica del Juego]
  D --> E[Capa de Interfaz de Usuario]
  ```

  ### 🧱 Componentes Clave

* **🖼️ FutbolEnRaya** (Interfaz gráfica y lógica del juego)
    * **UI**: `JButton[3][3]`, `JTextField`, `JLabel[]`
    * **Estado**: `String[][] tablero`, `String turnoActual`, `Set jugadoresUsados`
    * **Categorías**: `seleccionarCategoriasParaTableroJugable()`
    * **Validación**: `manejarClick()`, `quedanOpcionesParaJugadorActual()`

* **🗄️ TicTacToeDB** (Base de datos)
    * `precargarJugadores()`
    * `getPlayersByCategories()`

* **👤 Jugador** (Modelo)
    * `nombre`
    * `clubs`: `List<String>`

* **📄 JsonManipulator**
    * Importación desde archivo local `players.json`

* **☁️ MongoDBImporter**
    * Importación desde **MongoDB Atlas**

---

## 🔁 Flujo del Juego y Validación de Movimientos

### 🔄 Inicialización

1.  `precargarJugadores()`: Precarga todos los jugadores.
2.  Se cargan los datos desde **MongoDB** (`futbol_en_raya.jugadores`).
3.  `seleccionarCategoriasParaTableroJugable()`: Selección aleatoria de categorías válidas.
4.  Se llena la matriz `jugadoresDisponiblesPorCasilla[3][3]`.

### 🎯 Movimiento del Jugador

1.  El jugador hace clic en una celda y escribe un nombre.
2.  Se verifica si ya fue usado:
    * ✅ **Válido**: marca celda, cambia turno, evalúa victoria.
    * ❌ **Inválido o repetido**: penalización, cambio de turno.
3.  Se detecta si hay empate o "juego atascado".

---

## 🧮 Sistema de Categorías y Generación del Tablero

### Tipos de Categorías

* **Horizontales**: `PAIS`, `CLUB`
* **Verticales**: `POSICION`, `DORSAL`, `PAIS`, `EDAD_RANGO`

### 🔧 Algoritmo de Selección

1.  Hasta **1000 intentos** para generar tablero jugable.
2.  Se eligen las **3 categorías más frecuentes** (filas y columnas).
3.  Cada celda combina horizontal + vertical.
4.  Se valida que cada celda tenga al menos un jugador.

---

## 🧰 Pila Tecnológica y Dependencias

| 🔧 Componente   | 🛠️ Tecnología        | 🧾 Versión  | 📌 Propósito                      |
| :-------------- | :------------------- | :---------- | :-------------------------------- |
| Plataforma      | Java SE              | 21          | Base del proyecto                 |
| UI              | Swing                | -           | Interfaz gráfica                  |
| Base de Datos   | MongoDB              | 5.4.0       | Jugadores y categorías            |
| Driver MongoDB  | MongoDB Sync Driver  | 5.4.0       | Conexión con MongoDB              |
| Formato Datos   | JSON                 | 20240303    | Entrada/salida de datos           |
| Interno MongoDB | BSON                 | 5.4.0       | Almacenamiento binario            |

### 📦 Librerías JAR

* `bson-5.4.0.jar`
* `mongodb-driver-core-5.4.0.jar`
* `mongodb-driver-sync-5.4.0.jar`
* `json-20240303.jar`

---

## 🧬 Arquitectura de Datos

### Flujo de Datos

1.  **Origen**: Transfermarkt.es (manual)
2.  **Conversión**: Archivo local `players.json`
3.  **Importación**:
    * `JsonManipulator` → local
    * `MongoDBImporter` → Atlas (nube)
4.  **Carga**: Colección `futbol_en_raya.jugadores`
5.  **Caché**: Datos cargados en memoria vía `TicTacToeDB`

### 🗃️ Estructura del Archivo `players.json`

```json
{
  "nombre": "String",
  "nacionalidad": "String",
  "clubs": ["List<String>"],
  "posicion": "String",
  "numero_camiseta": "String",
  "edad": Integer
}
```

---

## 🚀 Puntos de Entrada y Clases Principales

| 📦 Clase          | 💡 Propósito                       | 🔑 Métodos Clave                                |
| :---------------- | :--------------------------------- | :---------------------------------------------- |
| `FutbolEnRaya`    | Aplicación principal y UI          | `main()`, `manejarClick()`, `iniciarNuevaPartida()` |
| `TicTacToeDB`     | Conexión y operaciones en MongoDB  | `precargarJugadores()`, `getPlayersByCategories()` |
| `JsonManipulator` | Importar desde base de datos local | Desde `players.json`                            |
| `MongoDBImporter` | Importar hacia la nube (MongoDB Atlas) | Cargar JSON a MongoDB                           |

### ▶️ Inicio de la Aplicación

La ejecución comienza desde:

```java
FutbolEnRaya.main()
