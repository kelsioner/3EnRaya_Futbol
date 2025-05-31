import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;

public class TicTacToeDB {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> playersCollection;

    // ¡IMPORTANTE! Asegúrate de que esta cadena NO tenga el '\r\n' al final
    // Y que los nombres de DB y Colección coincidan con tu MongoDBImporter
    private static final String CONNECTION_STRING = "mongodb+srv://kelsioner:3zOSa7Jnw0iJUPY7@proyectointermodular.0czvebk.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoIntermodular";
    private static final String DATABASE_NAME = "PlayerDB"; // Nombre de tu Base de Datos
    private static final String COLLECTION_NAME = "players"; // Nombre de la colección

    public TicTacToeDB() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            playersCollection = database.getCollection(COLLECTION_NAME);
            System.out.println("TicTacToeDB: Conexión a MongoDB exitosa.");
        } catch (Exception e) {
            System.err.println("TicTacToeDB: Error al conectar a MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo conectar a la base de datos MongoDB.", e);
        }
    }

    /**
     * Consulta jugadores que cumplen con dos categorías dadas.
     * Este es el método central para el juego.
     *
     * @param category1Type Tipo de la primera categoría (Ej: "EQUIPO", "PAIS", "POSICION", "EDAD_MAYOR_QUE")
     * @param category1Value Valor de la primera categoría (Ej: "Real Madrid", "España", "DC", "25" (para edad))
     * @param category2Type Tipo de la segunda categoría
     * @param category2Value Valor de la segunda categoría
     * @return Una lista de nombres de jugadores que cumplen ambas condiciones.
     */
    public List<String> getPlayersByCategories(String category1Type, String category1Value,
                                               String category2Type, String category2Value) {
        List<String> playerNames = new ArrayList<>();
        try {
            org.bson.conversions.Bson filter1 = buildFilter(category1Type, category1Value);
            org.bson.conversions.Bson filter2 = buildFilter(category2Type, category2Value);
            org.bson.conversions.Bson combinedFilter = Filters.and(filter1, filter2);

            for (Document doc : playersCollection.find(combinedFilter)) {
                String name = doc.getString("nombre");
                if (name != null) {
                    playerNames.add(name);
                }
            }
            System.out.println("TicTacToeDB: Consulta ejecutada para " + category1Type + ":" + category1Value + " y " + category2Type + ":" + category2Value + ". Jugadores encontrados: " + playerNames.size());
        } catch (Exception e) {
            System.err.println("TicTacToeDB: Error al obtener jugadores por categorías: " + e.getMessage());
            e.printStackTrace();
        }
        return playerNames;
    }

    /**
     * Método auxiliar para construir el filtro de MongoDB basado en el tipo de categoría.
     *
     * @param type Tipo de categoría (Ej: "EQUIPO", "PAIS", "POSICION", "EDAD_MAYOR_QUE")
     * @param value Valor de la categoría
     * @return Un objeto Bson (filtro de MongoDB)
     */
    private org.bson.conversions.Bson buildFilter(String type, String value) {
        switch (type.toUpperCase()) {
            case "EQUIPO":
                return Filters.in("equipos", value);
            case "PAIS":
                return Filters.eq("nacionalidad", value);
            case "POSICION":
                return Filters.in("posiciones", value);
            case "EDAD_MAYOR_QUE":
                try {
                    int ageGreaterThan = Integer.parseInt(value);
                    return Filters.gt("edad", ageGreaterThan);
                } catch (NumberFormatException e) {
                    System.err.println("TicTacToeDB: Valor de edad no válido para EDAD_MAYOR_QUE: " + value);
                    return Filters.eq("_id", null);
                }
            case "EDAD_MENOR_QUE":
                try {
                    int ageLessThan = Integer.parseInt(value);
                    return Filters.lt("edad", ageLessThan);
                } catch (NumberFormatException e) {
                    System.err.println("TicTacToeDB: Valor de edad no válido para EDAD_MENOR_QUE: " + value);
                    return Filters.eq("_id", null);
                }
            case "EDAD_EXACTA":
                try {
                    int exactAge = Integer.parseInt(value);
                    return Filters.eq("edad", exactAge);
                } catch (NumberFormatException e) {
                    System.err.println("TicTacToeDB: Valor de edad no válido para EDAD_EXACTA: " + value);
                    return Filters.eq("_id", null);
                }
            default:
                System.err.println("TicTacToeDB: Tipo de categoría desconocido: " + type);
                return Filters.eq("_id", null);
        }
    }

    /**
     * Obtener todos los nombres de jugadores para la funcionalidad de autocompletar.
     * @return Lista de todos los nombres de jugadores en la BD.
     */
    public List<String> getAllPlayerNames() {
        List<String> names = new ArrayList<>();
        try {
            for (Document doc : playersCollection.find()) {
                String name = doc.getString("nombre");
                if (name != null) {
                    names.add(name);
                }
            }
            System.out.println("TicTacToeDB: Se recuperaron " + names.size() + " nombres de jugadores.");
        } catch (Exception e) {
            System.err.println("TicTacToeDB: Error al obtener todos los nombres de jugadores: " + e.getMessage());
        }
        return names;
    }

    /**
     * Método para cerrar la conexión con la base de datos.
     * Es importante llamarlo al finalizar la aplicación.
     */
    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("TicTacToeDB: Conexión a MongoDB cerrada.");
        }
    }
}