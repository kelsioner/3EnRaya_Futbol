import com.mongodb.client.MongoClients; // Importa la clase para crear instancias de MongoClient.
import com.mongodb.client.MongoClient; // Importa la interfaz para la conexión al cliente de MongoDB.
import com.mongodb.client.MongoDatabase; // Importa la interfaz para trabajar con bases de datos de MongoDB.
import com.mongodb.client.MongoCollection; // Importa la interfaz para trabajar con colecciones de MongoDB.
import org.bson.Document; // Importa la clase Document de BSON para representar documentos de MongoDB.

import org.json.JSONObject; // Importa la clase JSONObject para manipular objetos JSON.
import org.json.JSONArray;  // Importa la clase JSONArray para manipular arrays JSON.

import java.io.IOException; // Importa IOException para manejar errores de entrada/salida.
import java.nio.file.Files; // Importa Files para operaciones con archivos del sistema.
import java.nio.file.Paths; // Importa Paths para construir rutas de archivos.
import java.util.ArrayList; // Importa ArrayList para listas dinámicas.
import java.util.List; // Importa la interfaz List para colecciones ordenadas.

public class MongoDBImporter { // Declara la clase MongoDBImporter.
    public static void main(String[] args) { // Método principal, punto de entrada de la aplicación.
        try { // Inicia un bloque try-catch para manejar posibles excepciones.
            // Línea que se conecta a MongoDB
            MongoClient client = MongoClients.create("mongodb+srv://kelsioner:3zOSa7Jnw0iJUPY7@proyectointermodular.0czvebk.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoIntermodular\r\n"); // Crea una conexión a una base de datos MongoDB Atlas utilizando la cadena de conexión proporcionada.

            // Se conecta a la base de datos
            MongoDatabase database = client.getDatabase("futbol_en_raya"); // Obtiene una referencia a la base de datos "futbol_en_raya".

            // Se obtiene la colección (si no existe, MongoDB la crea al insertar)
            MongoCollection<Document> jugadores = database.getCollection("jugadores"); // Obtiene una referencia a la colección "jugadores". Si no existe, se creará al insertar documentos.

            // Lee el archivo JSON que contiene los jugadores
            String json = new String(Files.readAllBytes(Paths.get("players.json"))); // Lee todo el contenido del archivo "players.json" en una cadena de texto.

            // Parsea el contenido como un array JSON
            JSONArray array = new JSONArray(json); // Convierte la cadena JSON en un JSONArray.

            // Inserta cada jugador como un documento en MongoDB
            for (int i = 0; i < array.length(); i++) { // Itera sobre cada elemento del JSONArray.
                JSONObject obj = array.getJSONObject(i); // Obtiene el JSONObject en la posición actual del array.
                Document doc = Document.parse(obj.toString()); // Convierte el JSONObject a un documento BSON de MongoDB.
                jugadores.insertOne(doc); // Inserta el documento en la colección "jugadores".
            }

            System.out.println("Importación completada exitosamente."); // Imprime un mensaje de éxito una vez que la importación se completa.
        } catch (IOException e) { // Captura una excepción si ocurre un error de entrada/salida (ej. archivo no encontrado).
            e.printStackTrace(); // Imprime la traza de la pila de la excepción para depuración.
        }
    }
}