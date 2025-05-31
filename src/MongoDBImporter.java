import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document; // Clases de MongoDB para representar documentos

import org.json.JSONObject; // De la librería org.json
import org.json.JSONArray;  // De la librería org.json

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/* public class MongoDBImporter {

    public static void main(String[] args) {

        // --- 1. Configuración de MongoDB ---
        // MongoDB se ejecuta en esta dirección y puerto
        String connectionString = "mongodb+srv://kelsioner:3zOSa7Jnw0iJUPY7@proyectointermodular.0czvebk.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoIntermodular\r\n"; 
        String databaseName = "PlayerDB"; // Nombre de mi Base de Datos
        String collectionName = "players"; // Nombre de la colección

        // --- 2. Ruta al archivo players.json ---
        // players.json está en la raíz del proyecto, por eso la ruta es players.json
        String jsonFilePath = "players.json"; 

        // --- 3. Leer el contenido del archivo JSON ---
        String jsonContent = "";
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            System.out.println("DEBUG: Archivo JSON '" + jsonFilePath + "' leído con éxito.");
            // System.out.println("DEBUG: Contenido leído:\n" + jsonContent.substring(0, Math.min(jsonContent.length(), 200)) + "..."); // Mostrar solo un fragmento
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo JSON: " + jsonFilePath);
            System.err.println("Por favor, asegúrate de que el archivo existe y la ruta es correcta.");
            System.err.println("Detalles: " + e.getMessage());
            e.printStackTrace();
            return; // Salir si no se puede leer el archivo
        }

        // --- 4. Parsear el contenido JSON y convertir a Documentos de MongoDB ---
        List<Document> documentsToInsert = new ArrayList<>();
        try {
            // Parsear como un array JSON (ej: [ {...}, {...} ])
            JSONArray jsonArray = new JSONArray(jsonContent); 
            System.out.println("DEBUG: JSON parseado como un array de " + jsonArray.length() + " elementos.");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // Convertir el JSONObject (de org.json) a un Document (de MongoDB)
                documentsToInsert.add(Document.parse(jsonObject.toString())); 
            }

        } catch (org.json.JSONException e) {
            // Si no es un array, parsearlo como un único objeto JSON (ej: { ... } )
            System.out.println("DEBUG: El JSON no es un array. Intentando parsear como un único objeto.");
            try {
                JSONObject jsonObject = new JSONObject(jsonContent);
                documentsToInsert.add(Document.parse(jsonObject.toString()));
                System.out.println("DEBUG: JSON parseado como un único objeto.");
            } catch (org.json.JSONException e2) {
                System.err.println("ERROR: El contenido del archivo JSON no es un formato válido (ni array ni objeto).");
                System.err.println("Detalles: " + e2.getMessage());
                e2.printStackTrace();
                return;
            }
        }

        // --- 5. Conectar a MongoDB e insertar los documentos ---
        System.out.println("DEBUG: Intentando conectar a MongoDB en " + connectionString + "...");
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            if (!documentsToInsert.isEmpty()) {
                if (documentsToInsert.size() == 1) {
                    collection.insertOne(documentsToInsert.get(0));
                    System.out.println("ÉXITO: Un documento insertado en la colección '" + collectionName + "'.");
                } else {
                    collection.insertMany(documentsToInsert);
                    System.out.println("ÉXITO: " + documentsToInsert.size() + " documentos insertados en la colección '" + collectionName + "'.");
                }
            } else {
                System.out.println("INFO: No se encontraron documentos para insertar en el archivo JSON.");
            }

        } catch (Exception e) {
            System.err.println("ERROR: Fallo al conectar o insertar en MongoDB.");
            System.err.println("Asegúrate de que MongoDB esté corriendo y la cadena de conexión sea correcta.");
            System.err.println("Detalles: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("DEBUG: Proceso de volcado finalizado.");
        }
    }
} */


public class MongoDBImporter {
    public static void main(String[] args) {
        try {
            // Línea que se conecta a MongoDB
            MongoClient client = MongoClients.create("mongodb+srv://kelsioner:3zOSa7Jnw0iJUPY7@proyectointermodular.0czvebk.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoIntermodular\r\n");

            // Se conecta a la base de datos
            MongoDatabase database = client.getDatabase("futbol_en_raya");

            // Se obtiene la colección (si no existe, MongoDB la crea al insertar)
            MongoCollection<Document> jugadores = database.getCollection("jugadores");

            // Lee el archivo JSON que contiene los jugadores
            String json = new String(Files.readAllBytes(Paths.get("players.json")));

            // Parsea el contenido como un array JSON
            JSONArray array = new JSONArray(json);

            // Inserta cada jugador como un documento en MongoDB
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Document doc = Document.parse(obj.toString());
                jugadores.insertOne(doc);
            }

            System.out.println("Importación completada exitosamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}