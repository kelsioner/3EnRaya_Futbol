import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays; // Para usar Arrays.asList

public class FutbolEnRaya extends JFrame {
	private static final long serialVersionUID = 1L; // O un número generado automáticamente
    private JTextField nombreJugadorField;
    private JButton[][] botones = new JButton[3][3];
    private String[][] tablero = new String[3][3]; // Guarda "X", "O" o null
    private String turnoActual = "X";
    private JLabel mensajeLabel;
    private JLabel scoreLabel; // Para mostrar las puntuaciones
    private JButton reiniciarButton; // Botón de reinicio

    // Etiquetas para mostrar las categorías en la interfaz
    private JLabel[] categoriaLabelsVertical = new JLabel[3];
    private JLabel[] categoriaLabelsHorizontal = new JLabel[3];

    private TicTacToeDB dbManager; // Instancia de nuestra clase de gestión de DB

    // Puntuaciones
    private int victoriasX = 0;
    private int victoriasO = 0;

    // Posibles tipos de categorías y sus valores
    private static final String[] TIPOS_CATEGORIA = {"EQUIPO", "PAIS", "POSICION", "EDAD_MAYOR_QUE"};
    // Puedes expandir estas listas con más valores reales de tu BD
    private static final List<String> EQUIPOS_POSIBLES = Arrays.asList(
        "Real Madrid", "Barcelona", "Atlético de Madrid", "Sevilla", "Valencia",
        "Real Betis", "Villarreal", "Real Sociedad", "Athletic Club", "Girona",
        "Osasuna", "Celta de Vigo", "Rayo Vallecano", "Getafe", "Alavés",
        "Mallorca", "Las Palmas", "Cádiz", "Granada", "Almería"
    );
    private static final List<String> PAISES_POSIBLES = Arrays.asList(
        "España", "Portugal", "Francia", "Brasil", "Argentina", "Alemania", "Inglaterra", "Uruguay", "Croacia"
    );
    private static final List<String> POSICIONES_POSIBLES = Arrays.asList(
    	    "Portero",                  // POR
    	    "Lateral Derecho",          // LD
    	    "Lateral Izquierdo",        // LI
    	    "Defensa Central",          // DFC
    	    "Mediocentro Defensivo",    // MCD
    	    "Mediocentro",              // MC
    	    "Mediocentro Ofensivo",     // MCO
    	    "Extremo Izquierdo",        // EI
    	    "Extremo Derecho",          // ED
    	    "Delantero Centro"          // DC
    	);
    
    // Para edades, el valor será un String que se convertirá a int
    private static final List<String> EDADES_POSIBLES_MAYOR_QUE = Arrays.asList(
        "20", "25", "30", "35" // "Mayor que 20", "Mayor que 25", etc.
    );

    private String[] categoriasVerticalValores = new String[3]; // Valores aleatorios para categorías verticales
    private String[] categoriasHorizontalValores = new String[3]; // Valores aleatorios para categorías horizontales
    private String[] categoriasVerticalTipos = new String[3]; // Tipos de categoría (EQUIPO, PAIS, etc.)
    private String[] categoriasHorizontalTipos = new String[3]; // Tipos de categoría (EQUIPO, PAIS, etc.)

    private Random random = new Random();

    public FutbolEnRaya(TicTacToeDB dbManager) {
        this.dbManager = dbManager;
        initUI();
        iniciarNuevaPartida(); // Iniciar la primera partida con categorías aleatorias
    }

    private void initUI() {
        setTitle("Fútbol en Raya");
        setSize(800, 700); // Tamaño más grande para acomodar categorías
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Espaciado entre componentes

        // Panel superior para el campo de texto y mensaje
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        nombreJugadorField = new JTextField();
        nombreJugadorField.setFont(new Font("Arial", Font.PLAIN, 18));
        nombreJugadorField.setBorder(BorderFactory.createTitledBorder("Introduce Nombre del Jugador"));
        topPanel.add(nombreJugadorField, BorderLayout.CENTER);

        mensajeLabel = new JLabel("Turno de: " + turnoActual + " (X)", SwingConstants.CENTER);
        mensajeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(mensajeLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Panel central para el tablero y las categorías
        JPanel gamePanel = new JPanel(new BorderLayout(5, 5));

        // Panel para categorías verticales (izquierda)
        JPanel verticalCategoriesPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        for (int i = 0; i < 3; i++) {
            categoriaLabelsVertical[i] = new JLabel("", SwingConstants.CENTER);
            categoriaLabelsVertical[i].setFont(new Font("Arial", Font.BOLD, 14));
            categoriaLabelsVertical[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            verticalCategoriesPanel.add(categoriaLabelsVertical[i]);
        }
        gamePanel.add(verticalCategoriesPanel, BorderLayout.WEST);

        // Panel para categorías horizontales (arriba)
        JPanel horizontalCategoriesPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        for (int i = 0; i < 3; i++) {
            categoriaLabelsHorizontal[i] = new JLabel("", SwingConstants.CENTER);
            categoriaLabelsHorizontal[i].setFont(new Font("Arial", Font.BOLD, 14));
            categoriaLabelsHorizontal[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            horizontalCategoriesPanel.add(categoriaLabelsHorizontal[i]);
        }
        gamePanel.add(horizontalCategoriesPanel, BorderLayout.NORTH);

        // Panel para el tablero de botones
        JPanel panelTablero = new JPanel(new GridLayout(3, 3, 5, 5)); // Espaciado entre botones
        panelTablero.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margen alrededor del tablero

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int fila = i;
                int columna = j;
                botones[i][j] = new JButton();
                botones[i][j].setFont(new Font("Arial", Font.BOLD, 40)); // Tamaño de la fuente para X/O
                botones[i][j].setBackground(Color.LIGHT_GRAY); // Color de fondo inicial
                botones[i][j].addActionListener(e -> manejarClick(fila, columna));
                panelTablero.add(botones[i][j]);
            }
        }
        gamePanel.add(panelTablero, BorderLayout.CENTER);

        add(gamePanel, BorderLayout.CENTER);

        // Panel inferior para puntuación y botón de reiniciar
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        scoreLabel = new JLabel("Puntuación: X - " + victoriasX + " | O - " + victoriasO, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(scoreLabel, BorderLayout.NORTH);

        reiniciarButton = new JButton("Reiniciar Juego");
        reiniciarButton.setFont(new Font("Arial", Font.BOLD, 18));
        reiniciarButton.addActionListener(e -> iniciarNuevaPartida());
        reiniciarButton.setVisible(false); // Oculto al inicio, solo se muestra al ganar
        bottomPanel.add(reiniciarButton, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Inicia una nueva partida, incluyendo la selección de categorías aleatorias.
     */
    private void iniciarNuevaPartida() {
        // Limpiar tablero
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tablero[i][j] = null;
                botones[i][j].setText("");
                botones[i][j].setEnabled(true); // Habilitar botones
                botones[i][j].setBackground(Color.LIGHT_GRAY); // Restaurar color
            }
        }
        turnoActual = "X";
        mensajeLabel.setText("Turno de: " + turnoActual + " (X)");
        reiniciarButton.setVisible(false); // Ocultar el botón de reiniciar

        // Seleccionar categorías aleatorias
        seleccionarCategoriasAleatorias();
        actualizarCategoriasUI();
    }

    /**
     * Selecciona aleatoriamente los tipos y valores de las categorías para el tablero.
     */
    private void seleccionarCategoriasAleatorias() {
        // Elegir un tipo principal para las categorías verticales (ej. EQUIPO)
        String tipoPrincipalVertical = TIPOS_CATEGORIA[random.nextInt(TIPOS_CATEGORIA.length)];
        // Elegir un tipo principal para las categorías horizontales (ej. POSICION)
        String tipoPrincipalHorizontal = TIPOS_CATEGORIA[random.nextInt(TIPOS_CATEGORIA.length)];

        // Asegurarse de que no sean el mismo tipo si es posible, para más variedad
        if (tipoPrincipalVertical.equals(tipoPrincipalHorizontal) && TIPOS_CATEGORIA.length > 1) {
            String tempType;
            do {
                tempType = TIPOS_CATEGORIA[random.nextInt(TIPOS_CATEGORIA.length)];
            } while (tempType.equals(tipoPrincipalHorizontal));
            tipoPrincipalHorizontal = tempType;
        }


        // Asignar tipos y valores para categorías verticales
        for (int i = 0; i < 3; i++) {
            categoriasVerticalTipos[i] = tipoPrincipalVertical;
            categoriasVerticalValores[i] = getRandomValueForCategory(tipoPrincipalVertical);
        }

        // Asignar tipos y valores para categorías horizontales
        for (int i = 0; i < 3; i++) {
            categoriasHorizontalTipos[i] = tipoPrincipalHorizontal;
            categoriasHorizontalValores[i] = getRandomValueForCategory(tipoPrincipalHorizontal);
        }
    }

    /**
     * Obtiene un valor aleatorio para un tipo de categoría dado.
     * @param categoryType El tipo de categoría (EQUIPO, PAIS, POSICION, EDAD_MAYOR_QUE)
     * @return Un valor String aleatorio para esa categoría.
     */
    private String getRandomValueForCategory(String categoryType) {
        switch (categoryType) {
            case "EQUIPO":
                return EQUIPOS_POSIBLES.get(random.nextInt(EQUIPOS_POSIBLES.size()));
            case "PAIS":
                return PAISES_POSIBLES.get(random.nextInt(PAISES_POSIBLES.size()));
            case "POSICION":
                return POSICIONES_POSIBLES.get(random.nextInt(POSICIONES_POSIBLES.size()));
            case "EDAD_MAYOR_QUE":
                return EDADES_POSIBLES_MAYOR_QUE.get(random.nextInt(EDADES_POSIBLES_MAYOR_QUE.size()));
            // Añadir más casos si añades EDAD_MENOR_QUE, EDAD_EXACTA, etc.
            default:
                return "Desconocido";
        }
    }

    /**
     * Actualiza las etiquetas de la interfaz con las categorías seleccionadas.
     */
    private void actualizarCategoriasUI() {
        for (int i = 0; i < 3; i++) {
            String displayValueV = categoriasVerticalValores[i];
            if (categoriasVerticalTipos[i].equals("EDAD_MAYOR_QUE")) {
                displayValueV = ">" + displayValueV + " años";
            }
            categoriaLabelsVertical[i].setText(categoriasVerticalTipos[i] + ": " + displayValueV);

            String displayValueH = categoriasHorizontalValores[i];
            if (categoriasHorizontalTipos[i].equals("EDAD_MAYOR_QUE")) {
                displayValueH = ">" + displayValueH + " años";
            }
            categoriaLabelsHorizontal[i].setText(categoriasHorizontalTipos[i] + ": " + displayValueH);
        }
    }


    private void manejarClick(int fila, int columna) {
        // Si la casilla ya está marcada, no hacer nada
        if (tablero[fila][columna] != null) {
            JOptionPane.showMessageDialog(this, "Esta casilla ya está marcada.", "Casilla Ocupada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreJugador = nombreJugadorField.getText().trim();
        if (nombreJugador.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce un nombre de jugador.", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener las categorías de la casilla clicada
        String tipoCatVertical = categoriasVerticalTipos[fila]; // Tipo de categoría de la fila (ej. EQUIPO)
        String valorCatVertical = categoriasVerticalValores[fila]; // Valor de categoría de la fila (ej. "Real Madrid")

        String tipoCatHorizontal = categoriasHorizontalTipos[columna]; // Tipo de categoría de la columna (ej. POSICION)
        String valorCatHorizontal = categoriasHorizontalValores[columna]; // Valor de categoría de la columna (ej. "DC")

        // Realizar la consulta a la base de datos usando TicTacToeDB
        List<String> jugadoresValidos = dbManager.getPlayersByCategories(
            tipoCatVertical, valorCatVertical,
            tipoCatHorizontal, valorCatHorizontal
        );

        // Verificar si el nombre introducido por el usuario está en la lista de jugadores válidos
        if (jugadoresValidos.contains(nombreJugador)) {
            // Acierto: Marcar la casilla y cambiar turno
            botones[fila][columna].setText(turnoActual);
            botones[fila][columna].setEnabled(false); // Deshabilitar el botón
            botones[fila][columna].setBackground(turnoActual.equals("X") ? Color.BLUE : Color.RED); // Color para X/O
            botones[fila][columna].setForeground(Color.WHITE); // Texto blanco

            tablero[fila][columna] = turnoActual;
            mensajeLabel.setText("¡Correcto! Turno de: " + (turnoActual.equals("X") ? "O" : "X"));

            if (hayGanador()) {
                JOptionPane.showMessageDialog(this, "¡Jugador " + turnoActual + " ha ganado!");
                if (turnoActual.equals("X")) {
                    victoriasX++;
                } else {
                    victoriasO++;
                }
                actualizarPuntuacion();
                reiniciarButton.setVisible(true); // Mostrar botón de reiniciar
                // Deshabilitar todos los botones del tablero al ganar
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        botones[i][j].setEnabled(false);
                    }
                }
                return; // Salir para no cambiar de turno si ya hay ganador
            } else if (tableroLleno()) {
                JOptionPane.showMessageDialog(this, "¡Empate!");
                reiniciarButton.setVisible(true);
                return;
            }

            // Cambiar de turno si no hay ganador ni empate
            turnoActual = turnoActual.equals("X") ? "O" : "X";
        } else {
            // Fallo: Mostrar mensaje de error
            mensajeLabel.setText("¡Incorrecto! El jugador '" + nombreJugador + "' no cumple las categorías. Turno sigue: " + turnoActual);
            // Opcional: Podrías cambiar el color del botón temporalmente a rojo para indicar un fallo
            // botones[fila][columna].setBackground(Color.RED);
            // Luego restaurarlo después de un breve retraso si no quieres que quede marcado.
        }

        nombreJugadorField.setText(""); // Limpiar el campo de texto
    }

    /**
     * Verifica si el tablero está completamente lleno.
     * @return true si el tablero está lleno, false en caso contrario.
     */
    private boolean tableroLleno() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tablero[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Actualiza la etiqueta de puntuación en la interfaz.
     */
    private void actualizarPuntuacion() {
        scoreLabel.setText("Puntuación: X - " + victoriasX + " | O - " + victoriasO);
    }


    private boolean hayGanador() {
        // Comprobar filas
        for (int i = 0; i < 3; i++) {
            if (tablero[i][0] != null &&
                tablero[i][0].equals(tablero[i][1]) &&
                tablero[i][1].equals(tablero[i][2])) {
                return true;
            }
        }
        // Comprobar columnas
        for (int i = 0; i < 3; i++) {
            if (tablero[0][i] != null &&
                tablero[0][i].equals(tablero[1][i]) &&
                tablero[1][i].equals(tablero[2][i])) {
                return true;
            }
        }
        // Comprobar diagonales
        if (tablero[0][0] != null &&
            tablero[0][0].equals(tablero[1][1]) &&
            tablero[1][1].equals(tablero[2][2])) {
            return true;
        }
        if (tablero[0][2] != null &&
            tablero[0][2].equals(tablero[1][1]) &&
            tablero[1][1].equals(tablero[2][0])) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        TicTacToeDB dbManager = null;
        try {
            // Inicializar la clase de gestión de la base de datos
            dbManager = new TicTacToeDB();

            // Asegurarse de que la interfaz de usuario se ejecute en el Event Dispatch Thread de Swing
            final TicTacToeDB finalDbManager = dbManager; // Necesario para usar en la lambda
            SwingUtilities.invokeLater(() -> {
                FutbolEnRaya juego = new FutbolEnRaya(finalDbManager);
                juego.setVisible(true);
            });

        } catch (RuntimeException e) {
            // Capturar la excepción si la conexión a la DB falla
            JOptionPane.showMessageDialog(null,
                "Error al conectar con la base de datos: " + e.getMessage() + "\n" +
                "Por favor, asegúrate de que MongoDB esté corriendo y la cadena de conexión sea correcta.",
                "Error de Conexión a DB",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Salir de la aplicación si no se puede conectar a la DB
        } finally {
            // NOTA: Cerrar la conexión aquí en el main no es ideal para una app Swing ya que el main termina antes que la ventana.
            
        }
    }
}