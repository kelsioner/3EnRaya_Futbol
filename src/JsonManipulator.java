/* import org.json.JSONObject;
import org.json.JSONArray;

public class JsonManipulator {
    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nombre", "Ejemplo");
        System.out.println(jsonObject.toString());

        JSONArray jsonArray = new JSONArray();
        jsonArray.put("item1");
        jsonArray.put("item2");
        System.out.println(jsonArray.toString());
    }
} */


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonManipulator {

    public static void main(String[] args) {
        // Ruta al archivo JSON (asegúrate que está en el mismo directorio o da ruta completa)
        String rutaArchivo = "players.json";

        try {
            // Leer archivo JSON
            String contenido = new String(Files.readAllBytes(Paths.get(rutaArchivo)));

            // Convertir a array JSON
            JSONArray jugadoresArray = new JSONArray(contenido);

            // Conectar con MongoDB
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("futbol_en_raya");
            MongoCollection<Document> coleccion = database.getCollection("jugadores");

            // Insertar cada jugador como documento
            for (int i = 0; i < jugadoresArray.length(); i++) {
                JSONObject jugadorJSON = jugadoresArray.getJSONObject(i);
                Document jugadorDoc = Document.parse(jugadorJSON.toString());
                coleccion.insertOne(jugadorDoc);
            }

            System.out.println("Jugadores insertados correctamente desde players.json.");
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
        }
    }
}