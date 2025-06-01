/* import org.json.JSONObject; // Comentado: Importa la clase JSONObject de la librería org.json.
import org.json.JSONArray; // Comentado: Importa la clase JSONArray de la librería org.json.

public class JsonManipulator { // Comentado: Declaración de la clase JsonManipulator.
    public static void main(String[] args) { // Comentado: Método principal de la clase.
        JSONObject jsonObject = new JSONObject(); // Comentado: Crea una nueva instancia de JSONObject.
        jsonObject.put("nombre", "Ejemplo"); // Comentado: Añade un par clave-valor al JSONObject.
        System.out.println(jsonObject.toString()); // Comentado: Imprime la representación String del JSONObject.

        JSONArray jsonArray = new JSONArray(); // Comentado: Crea una nueva instancia de JSONArray.
        jsonArray.put("item1"); // Comentado: Añade un elemento al JSONArray.
        jsonArray.put("item2"); // Comentado: Añade otro elemento al JSONArray.
        System.out.println(jsonArray.toString()); // Comentado: Imprime la representación String del JSONArray.
    }
} */


import com.mongodb.client.MongoClient; // Importa la interfaz MongoClient para interactuar con MongoDB.
import com.mongodb.client.MongoClients; // Importa la clase MongoClients para crear instancias de MongoClient.
import com.mongodb.client.MongoCollection; // Importa la interfaz MongoCollection para trabajar con colecciones.
import com.mongodb.client.MongoDatabase; // Importa la interfaz MongoDatabase para trabajar con bases de datos.
import org.bson.Document; // Importa la clase Document para representar documentos BSON (MongoDB).
import org.json.JSONArray; // Importa la clase JSONArray para manipular arrays JSON.
import org.json.JSONObject; // Importa la clase JSONObject para manipular objetos JSON.

import java.io.IOException; // Importa IOException para manejar errores de entrada/salida.
import java.nio.file.Files; // Importa Files para operaciones con archivos.
import java.nio.file.Paths; // Importa Paths para construir rutas de archivos.

public class JsonManipulator { // Declara la clase JsonManipulator.

    public static void main(String[] args) { // Método principal de la clase.
        // Ruta al archivo JSON (asegúrate que está en el mismo directorio o da ruta completa)
        String rutaArchivo = "players.json"; // Define la ruta del archivo JSON.

        try { // Bloque try para manejar posibles excepciones.
            // Leer archivo JSON
            String contenido = new String(Files.readAllBytes(Paths.get(rutaArchivo))); // Lee todo el contenido del archivo JSON a un String.

            // Convertir a array JSON
            JSONArray jugadoresArray = new JSONArray(contenido); // Convierte el contenido String a un JSONArray.

            // Conectar con MongoDB
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017"); // Crea una conexión a MongoDB local.
            MongoDatabase database = mongoClient.getDatabase("futbol_en_raya"); // Obtiene la base de datos "futbol_en_raya".
            MongoCollection<Document> coleccion = database.getCollection("jugadores"); // Obtiene la colección "jugadores".

            // Insertar cada jugador como documento
            for (int i = 0; i < jugadoresArray.length(); i++) { // Itera sobre cada objeto en el JSONArray.
                JSONObject jugadorJSON = jugadoresArray.getJSONObject(i); // Obtiene un objeto JSON de jugador.
                Document jugadorDoc = Document.parse(jugadorJSON.toString()); // Convierte el objeto JSON a un documento MongoDB.
                coleccion.insertOne(jugadorDoc); // Inserta el documento en la colección.
            }

            System.out.println("Jugadores insertados correctamente desde players.json."); // Mensaje de éxito.
        } catch (IOException e) { // Captura la excepción si hay un error de lectura de archivo.
            System.err.println("Error leyendo el archivo: " + e.getMessage()); // Imprime el mensaje de error.
        } catch (Exception e) { // Captura cualquier otra excepción.
            System.err.println("Error general: " + e.getMessage()); // Imprime el mensaje de error general.
        }
    }
}