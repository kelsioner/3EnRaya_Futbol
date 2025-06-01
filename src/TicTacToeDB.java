import com.mongodb.client.MongoClient; // Importa la interfaz para la conexión al cliente de MongoDB.
import com.mongodb.client.MongoClients; // Importa la clase para crear instancias de MongoClient.
import com.mongodb.client.MongoCollection; // Importa la interfaz para trabajar con colecciones de MongoDB.
import com.mongodb.client.MongoDatabase; // Importa la interfaz para trabajar con bases de datos de MongoDB.
import org.bson.Document; // Importa la clase Document de BSON para representar documentos de MongoDB.

import java.util.ArrayList; // Importa ArrayList para listas dinámicas.
import java.util.Arrays; // Importa Arrays para manipular arrays.
import java.util.Collections; // Importa Collections para utilidades de colecciones.
import java.util.Comparator; // Importar Comparator para comparar objetos.
import java.util.HashMap; // Importa HashMap para mapas de clave-valor.
import java.util.HashSet; // Importa HashSet para conjuntos de elementos únicos.
import java.util.LinkedHashMap; // Para mantener el orden de inserción (aunque no se usa directamente en este código visible, es una opción).
import java.util.List; // Importa la interfaz List para colecciones ordenadas.
import java.util.Map; // Importa la interfaz Map para colecciones de clave-valor.
import java.util.Set; // Importa la interfaz Set para colecciones sin duplicados.
import java.util.stream.Collectors; // Importa Collectors para operaciones de Stream API.

public class TicTacToeDB { // Declara la clase TicTacToeDB para gestionar la base de datos del juego.

    private MongoClient mongoClient; // Objeto cliente de MongoDB para la conexión.
    private MongoDatabase database; // Objeto de la base de datos de MongoDB.
    private MongoCollection<Document> playersCollection; // Colección de documentos de jugadores en MongoDB.

    // Cache en memoria de los jugadores
    private List<Jugador> todosLosJugadores = new ArrayList<>(); // Lista que almacena todos los jugadores cargados en memoria.
    private Map<String, List<Jugador>> jugadoresPorClub = new HashMap<>(); // Mapa para almacenar jugadores agrupados por club.
    private Map<String, List<Jugador>> jugadoresPorPais = new HashMap<>(); // Mapa para almacenar jugadores agrupados por nacionalidad (país).
    private Map<String, List<Jugador>> jugadoresPorPosicion = new HashMap<>(); // Mapa para almacenar jugadores agrupados por posición.
    private Map<String, List<Jugador>> jugadoresPorDorsal = new HashMap<>(); // NUEVO: Mapa para almacenar jugadores agrupados por número de camiseta (dorsal).

    public TicTacToeDB() { // Constructor de la clase TicTacToeDB.
        try { // Inicia un bloque try-catch para manejar posibles excepciones durante la inicialización.
            // Reemplaza con tu cadena de conexión de MongoDB
            mongoClient = MongoClients.create("mongodb+srv://kelsioner:3zOSa7Jnw0iJUPY7@proyectointermodular.0czvebk.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoIntermodular"); // Crea una conexión a la base de datos MongoDB Atlas.
            database = mongoClient.getDatabase("futbol_en_raya"); // Obtiene la base de datos "futbol_en_raya".
            playersCollection = database.getCollection("jugadores"); // Obtiene la colección "jugadores".
            precargarJugadores(); // Llama al método para precargar todos los jugadores en memoria.
        } catch (Exception e) { // Captura cualquier excepción que ocurra.
            System.err.println("Error al inicializar la base de datos: " + e.getMessage()); // Imprime un mensaje de error en la consola.
            throw new RuntimeException("No se pudo conectar con la base de datos o precargar jugadores. Asegúrate de que MongoDB esté corriendo y la cadena de conexión sea correcta.", e); // Lanza una excepción en tiempo de ejecución.
        }
    }

    private void precargarJugadores() { // Método para precargar todos los jugadores desde la base de datos a la memoria.
        System.out.println("Precargando jugadores..."); // Mensaje informativo.
        for (Document doc : playersCollection.find()) { // Itera sobre cada documento (jugador) encontrado en la colección.
            try { // Inicia un bloque try-catch para procesar cada documento.
                int id = doc.getInteger("_id"); // Obtiene el ID del jugador.
                Object numCamisetaObj = doc.get("numero_camiseta"); // Obtiene el número de camiseta como Object (puede ser int o String).
                String numero_camiseta = (numCamisetaObj != null) ? String.valueOf(numCamisetaObj) : null; // Convierte el número de camiseta a String.
                String nombre = doc.getString("nombre"); // Obtiene el nombre del jugador.
                String posicion = doc.getString("posicion"); // Obtiene la posición del jugador.
                List<String> nacionalidadList = doc.getList("nacionalidad", String.class); // Obtiene la lista de nacionalidades.
                int edad = doc.getInteger("edad"); // Obtiene la edad del jugador.
                List<String> clubsList = doc.getList("clubs", String.class); // Obtiene la lista de clubes.

                // Convertir la lista de nacionalidades a una sola cadena para facilitar el mapeo
                String nacionalidad = (nacionalidadList != null && !nacionalidadList.isEmpty()) ? nacionalidadList.get(0) : null; // Toma la primera nacionalidad si la lista no está vacía.

                Jugador jugador = new Jugador(id, numero_camiseta, nombre, posicion, nacionalidad, edad, clubsList); // Crea un nuevo objeto Jugador.
                todosLosJugadores.add(jugador); // Añade el jugador a la lista de todos los jugadores.

                if (clubsList != null) { // Si hay clubes asociados al jugador.
                    for (String club : clubsList) { // Itera sobre cada club.
                        jugadoresPorClub.computeIfAbsent(club, k -> new ArrayList<>()).add(jugador); // Añade el jugador al mapa de jugadores por club.
                    }
                }
                if (nacionalidad != null) { // Si el jugador tiene nacionalidad.
                    jugadoresPorPais.computeIfAbsent(nacionalidad, k -> new ArrayList<>()).add(jugador); // Añade el jugador al mapa de jugadores por país.
                }
                if (posicion != null) { // Si el jugador tiene posición.
                    jugadoresPorPosicion.computeIfAbsent(posicion, k -> new ArrayList<>()).add(jugador); // Añade el jugador al mapa de jugadores por posición.
                }
                if (numero_camiseta != null) { // Si el jugador tiene número de camiseta.
                    jugadoresPorDorsal.computeIfAbsent(numero_camiseta, k -> new ArrayList<>()).add(jugador); // Añade el jugador al mapa de jugadores por dorsal.
                }

            } catch (Exception e) { // Captura cualquier excepción durante el procesamiento de un documento.
                System.err.println("Error al procesar documento de jugador: " + doc.toJson() + " - " + e.getMessage()); // Imprime el documento y el mensaje de error.
            }
        }
        System.out.println("Cargados " + todosLosJugadores.size() + " jugadores."); // Mensaje que indica cuántos jugadores se cargaron.
    }

    public List<String> getDistinctValues(String field) { // Método para obtener valores distintos de un campo dado.
        Set<String> distinctValues = new HashSet<>(); // Crea un conjunto para almacenar valores únicos.
        for (Jugador jugador : todosLosJugadores) { // Itera sobre todos los jugadores.
            switch (field) { // Comprueba el campo solicitado.
                case "clubs": // Si el campo es "clubs".
                    if (jugador.getClubs() != null) { // Si la lista de clubes no es nula.
                        distinctValues.addAll(jugador.getClubs()); // Añade todos los clubes a los valores distintos.
                    }
                    break;
                case "nacionalidad": // Si el campo es "nacionalidad".
                    if (jugador.getNacionalidad() != null) { // Si la nacionalidad no es nula.
                        distinctValues.add(jugador.getNacionalidad()); // Añade la nacionalidad a los valores distintos.
                    }
                    break;
                case "posicion": // Si el campo es "posicion".
                    if (jugador.getPosicion() != null) { // Si la posición no es nula.
                        distinctValues.add(jugador.getPosicion()); // Añade la posición a los valores distintos.
                    }
                    break;
                case "numero_camiseta": // Si el campo es "numero_camiseta".
                    if (jugador.getNumeroCamiseta() != null) { // Si el número de camiseta no es nulo.
                        distinctValues.add(jugador.getNumeroCamiseta()); // Añade el número de camiseta a los valores distintos.
                    }
                    break;
                default: // Caso por defecto.
                    break;
            }
        }
        return new ArrayList<>(distinctValues); // Devuelve una lista de los valores distintos.
    }

    /**
     * Obtiene una lista de los N valores más frecuentes para un campo dado.
     * Si no hay suficientes valores, devuelve todos los disponibles.
     *
     * @param field El nombre del campo (e.g., "clubs", "nacionalidad").
     * @param count El número de valores más frecuentes a devolver.
     * @return Una lista de Strings con los valores más frecuentes.
     */
    public List<String> getMostFrequentValues(String field, int count) { // Método para obtener los valores más frecuentes de un campo.
        Map<String, Integer> frequencyMap = new HashMap<>(); // Mapa para almacenar la frecuencia de cada valor.

        for (Jugador jugador : todosLosJugadores) { // Itera sobre todos los jugadores.
            switch (field) { // Comprueba el campo.
                case "clubs": // Si el campo es "clubs".
                    if (jugador.getClubs() != null) { // Si el jugador tiene clubes.
                        for (String club : jugador.getClubs()) { // Itera sobre los clubes del jugador.
                            frequencyMap.put(club, frequencyMap.getOrDefault(club, 0) + 1); // Incrementa la frecuencia del club.
                        }
                    }
                    break;
                case "nacionalidad": // Si el campo es "nacionalidad".
                    if (jugador.getNacionalidad() != null) { // Si el jugador tiene nacionalidad.
                        frequencyMap.put(jugador.getNacionalidad(), frequencyMap.getOrDefault(jugador.getNacionalidad(), 0) + 1); // Incrementa la frecuencia de la nacionalidad.
                    }
                    break;
                case "posicion": // No es necesario para posicion, ya que hay menos opciones
                    if (jugador.getPosicion() != null) { // Si el jugador tiene posición.
                        frequencyMap.put(jugador.getPosicion(), frequencyMap.getOrDefault(jugador.getPosicion(), 0) + 1); // Incrementa la frecuencia de la posición.
                    }
                    break;
                case "numero_camiseta": // No es necesario para dorsal, ya que hay muchos únicos
                    if (jugador.getNumeroCamiseta() != null) { // Si el jugador tiene número de camiseta.
                        frequencyMap.put(jugador.getNumeroCamiseta(), frequencyMap.getOrDefault(jugador.getNumeroCamiseta(), 0) + 1); // Incrementa la frecuencia del dorsal.
                    }
                    break;
            }
        }

        // Ordenar por frecuencia (descendente)
        return frequencyMap.entrySet().stream() // Convierte el mapa de frecuencias a un stream de entradas.
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // Ordena las entradas por valor (frecuencia) en orden descendente.
                .limit(count) // Limita el stream al número deseado de elementos.
                .map(Map.Entry::getKey) // Mapea cada entrada a su clave (el valor del campo).
                .collect(Collectors.toList()); // Recolecta los resultados en una lista.
    }


    public List<String> getPlayersByCategories(String categoryType1, String categoryValue1, String categoryType2, String categoryValue2) { // Método para obtener jugadores que cumplen con dos categorías.
        List<Jugador> filteredPlayers = new ArrayList<>(); // Lista para almacenar los jugadores filtrados.
        // Filtrar por la primera categoría
        List<Jugador> initialFilter; // Lista para el resultado del primer filtro.
        switch (categoryType1) { // Comprueba el tipo de la primera categoría.
            case "CLUB": // Si es "CLUB".
                initialFilter = jugadoresPorClub.getOrDefault(categoryValue1, Collections.emptyList()); // Obtiene jugadores por club.
                break;
            case "PAIS": // Si es "PAIS".
                initialFilter = jugadoresPorPais.getOrDefault(categoryValue1, Collections.emptyList()); // Obtiene jugadores por país.
                break;
            case "POSICION": // Si es "POSICION".
                initialFilter = jugadoresPorPosicion.getOrDefault(categoryValue1, Collections.emptyList()); // Obtiene jugadores por posición.
                break;
            case "DORSAL": // Si es "DORSAL".
                initialFilter = jugadoresPorDorsal.getOrDefault(categoryValue1, Collections.emptyList()); // Obtiene jugadores por dorsal.
                break;
            case "EDAD_RANGO": // Si es "EDAD_RANGO".
                initialFilter = getPlayersByAgeRange(categoryValue1); // Obtiene jugadores por rango de edad.
                break;
            default: // Caso por defecto.
                return Collections.emptyList(); // Devuelve una lista vacía.
        }

        // Aplicar el segundo filtro sobre el resultado del primero
        for (Jugador jugador : initialFilter) { // Itera sobre los jugadores del primer filtro.
            boolean matchesCategory2 = false; // Bandera para indicar si el jugador coincide con la segunda categoría.
            switch (categoryType2) { // Comprueba el tipo de la segunda categoría.
                case "CLUB": // Si es "CLUB".
                    matchesCategory2 = (jugador.getClubs() != null && jugador.getClubs().contains(categoryValue2)); // Comprueba si el jugador pertenece al club.
                    break;
                case "PAIS": // Si es "PAIS".
                    matchesCategory2 = (jugador.getNacionalidad() != null && jugador.getNacionalidad().equals(categoryValue2)); // Comprueba si el jugador tiene la nacionalidad.
                    break;
                case "POSICION": // Si es "POSICION".
                    matchesCategory2 = (jugador.getPosicion() != null && jugador.getPosicion().equals(categoryValue2)); // Comprueba si el jugador tiene la posición.
                    break;
                case "DORSAL": // Si es "DORSAL".
                    matchesCategory2 = (jugador.getNumeroCamiseta() != null && jugador.getNumeroCamiseta().equals(categoryValue2)); // Comprueba si el jugador tiene el dorsal.
                    break;
                case "EDAD_RANGO": // Si es "EDAD_RANGO".
                    matchesCategory2 = isPlayerInAgeRange(jugador, categoryValue2); // Comprueba si el jugador está en el rango de edad.
                    break;
                default: // Caso por defecto.
                    break;
            }
            if (matchesCategory2) { // Si el jugador coincide con la segunda categoría.
                filteredPlayers.add(jugador); // Añade el jugador a la lista de jugadores filtrados.
            }
        }

        // Convertir la lista de objetos Jugador a una lista de nombres de jugadores
        return filteredPlayers.stream() // Convierte la lista de Jugador a un stream.
                .map(Jugador::getNombre) // Mapea cada objeto Jugador a su nombre.
                .collect(Collectors.toList()); // Recolecta los nombres en una nueva lista.
    }

    private List<Jugador> getPlayersByAgeRange(String ageRange) { // Método privado para obtener jugadores por rango de edad.
        List<Jugador> playersInAgeRange = new ArrayList<>(); // Lista para almacenar jugadores dentro del rango de edad.
        try { // Inicia un bloque try-catch.
            String[] parts = ageRange.split("-"); // Divide el rango de edad por el guion.
            int minAge = Integer.parseInt(parts[0]); // Convierte la primera parte a edad mínima.
            int maxAge = Integer.parseInt(parts[1]); // Convierte la segunda parte a edad máxima.

            for (Jugador jugador : todosLosJugadores) { // Itera sobre todos los jugadores.
                if (jugador.getEdad() >= minAge && jugador.getEdad() <= maxAge) { // Si la edad del jugador está dentro del rango.
                    playersInAgeRange.add(jugador); // Añade el jugador a la lista.
                }
            }
        } catch (NumberFormatException e) { // Captura la excepción si el formato del número es inválido.
            System.err.println("Formato de rango de edad inválido: " + ageRange); // Imprime un mensaje de error.
        }
        return playersInAgeRange; // Devuelve la lista de jugadores en el rango de edad.
    }

    private boolean isPlayerInAgeRange(Jugador jugador, String ageRange) { // Método privado para verificar si un jugador está en un rango de edad dado.
        try { // Inicia un bloque try-catch.
            String[] parts = ageRange.split("-"); // Divide el rango de edad.
            int minAge = Integer.parseInt(parts[0]); // Obtiene la edad mínima.
            int maxAge = Integer.parseInt(parts[1]); // Obtiene la edad máxima.
            return jugador.getEdad() >= minAge && jugador.getEdad() <= maxAge; // Retorna true si la edad está en el rango, false de lo contrario.
        } catch (NumberFormatException e) { // Captura la excepción si el formato del número es inválido.
            System.err.println("Formato de rango de edad inválido para la comprobación: " + ageRange); // Imprime un mensaje de error.
            return false; // Retorna false.
        }
    }


    public void closeConnection() { // Método para cerrar la conexión a la base de datos.
        if (mongoClient != null) { // Si el cliente de MongoDB no es nulo.
            mongoClient.close(); // Cierra la conexión.
            System.out.println("Conexión a MongoDB cerrada."); // Mensaje informativo.
        }
    }

    // Clase interna para representar un jugador
    // (Asegúrate de que esta clase Jugador esté definida en tu TicTacToeDB.java)
    public static class Jugador { // Declara la clase interna Jugador para representar un jugador.
        private int id; // ID del jugador.
        private String numeroCamiseta; // Número de camiseta del jugador.
        private String nombre; // Nombre del jugador.
        private String posicion; // Posición del jugador.
        private String nacionalidad; // Nacionalidad del jugador (ahora como String único).
        private int edad; // Edad del jugador.
        private List<String> clubs; // Lista de clubes a los que pertenece el jugador.

        public Jugador(int id, String numeroCamiseta, String nombre, String posicion, String nacionalidad, int edad, List<String> clubs) { // Constructor de la clase Jugador.
            this.id = id; // Inicializa el ID.
            this.numeroCamiseta = numeroCamiseta; // Inicializa el número de camiseta.
            this.nombre = nombre; // Inicializa el nombre.
            this.posicion = posicion; // Inicializa la posición.
            this.nacionalidad = nacionalidad; // Inicializa la nacionalidad.
            this.edad = edad; // Inicializa la edad.
            this.clubs = clubs; // Inicializa la lista de clubes.
        }

        public int getId() { return id; } // Getter para el ID.
        public String getNumeroCamiseta() { return numeroCamiseta; } // Getter para el número de camiseta.
        public String getNombre() { return nombre; } // Getter para el nombre.
        public String getPosicion() { return posicion; } // Getter para la posición.
        public String getNacionalidad() { return nacionalidad; } // Getter para la nacionalidad (String).
        public int getEdad() { return edad; } // Getter para la edad.
        public List<String> getClubs() { return clubs; } // Getter para la lista de clubes.

        @Override
        public String toString() { // Sobreescribe el método toString para una representación de cadena del objeto.
            return "Jugador{" + // Retorna una cadena formateada con los atributos del jugador.
                    "nombre='" + nombre + '\'' +
                    ", nacionalidad='" + nacionalidad + '\'' +
                    ", posicion='" + posicion + '\'' +
                    ", numero_camiseta='" + numeroCamiseta + '\'' +
                    ", edad=" + edad +
                    ", clubs=" + clubs +
                    '}';
        }
    }

    // Clase interna para CategoryCombination (usada para la precarga, no estrictamente necesaria para el juego principal)
    // Pero si la estás usando en algún lado, asegúrate de que esté aquí.
    public static class CategoryCombination { // Declara la clase interna CategoryCombination para representar una combinación de categorías.
        String categoryType1; // Tipo de la primera categoría.
        String categoryValue1; // Valor de la primera categoría.
        String categoryType2; // Tipo de la segunda categoría.
        String categoryValue2; // Valor de la segunda categoría.
        int playerCount; // Cantidad de jugadores que coinciden con esta combinación.

        public CategoryCombination(String categoryType1, String categoryValue1, String categoryType2, String categoryValue2, int playerCount) { // Constructor de CategoryCombination.
            this.categoryType1 = categoryType1; // Inicializa el tipo de la primera categoría.
            this.categoryValue1 = categoryValue1; // Inicializa el valor de la primera categoría.
            this.categoryType2 = categoryType2; // Inicializa el tipo de la segunda categoría.
            this.categoryValue2 = categoryValue2; // Inicializa el valor de la segunda categoría.
            this.playerCount = playerCount; // Inicializa la cantidad de jugadores.
        }

        @Override
        public boolean equals(Object o) { // Sobreescribe el método equals para comparar objetos CategoryCombination.
            if (this == o) return true; // Si es el mismo objeto, retorna true.
            if (o == null || getClass() != o.getClass()) return false; // Si el objeto es nulo o de una clase diferente, retorna false.
            CategoryCombination that = (CategoryCombination) o; // Realiza un casting al objeto.
            return categoryType1.equals(that.categoryType1) && // Compara los tipos y valores de las categorías.
                    categoryValue1.equals(that.categoryValue1) &&
                    categoryType2.equals(that.categoryType2) &&
                    categoryValue2.equals(that.categoryValue2);
        }

        @Override
        public int hashCode() { // Sobreescribe el método hashCode para generar un código hash del objeto.
            return java.util.Objects.hash(categoryType1, categoryValue1, categoryType2, categoryValue2); // Genera un hash basado en los tipos y valores de las categorías.
        }
    }
}