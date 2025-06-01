import javax.swing.*; // Importa clases para la interfaz gráfica de usuario (GUI) de Swing.
import java.awt.*; // Importa clases para gráficos y manejo de eventos de AWT.
import java.util.List; // Importa la interfaz List para colecciones ordenadas.
import java.util.ArrayList; // Importa la clase ArrayList, una implementación de List.
import java.util.Random; // Importa la clase Random para generar números aleatorios.
import java.util.Arrays; // Importa la clase Arrays para manipulaciones de arrays.
import java.util.Collections; // Importa la clase Collections para utilidades de colecciones.
import java.awt.event.WindowAdapter; // Importa WindowAdapter para manejar eventos de ventana.
import java.awt.event.WindowEvent; // Importa WindowEvent para eventos de ventana.
import java.util.HashSet; // Importa HashSet para colecciones que no permiten duplicados.
import java.util.Map; // Importa la interfaz Map para colecciones de clave-valor.
import java.util.HashMap; // Importa la clase HashMap, una implementación de Map.
import java.util.Set; // Importa la interfaz Set para colecciones sin duplicados.
import java.util.stream.Collectors; // Importa Collectors para operaciones de Stream API.

public class FutbolEnRaya extends JFrame { // Declara la clase FutbolEnRaya que extiende JFrame, creando una ventana.
    private static final long serialVersionUID = 1L; // Número de versión para serialización.
    private JTextField nombreJugadorField; // Campo de texto para que el jugador ingrese su nombre.
    private JButton[][] botones = new JButton[3][3]; // Matriz de botones que representa el tablero de 3x3.
    private String[][] tablero = new String[3][3]; // Matriz de Strings que representa el estado lógico del tablero (X, O, o null).
    private String turnoActual = "X"; // Almacena el jugador actual (X o O).
    private JLabel mensajeLabel; // Etiqueta para mostrar mensajes al usuario (turno, errores).
    private JLabel scoreLabel; // Etiqueta para mostrar la puntuación de los jugadores.
    private JButton reiniciarButton; // Botón para reiniciar el juego.

    private JLabel[] categoriaLabelsVertical = new JLabel[3]; // Etiquetas para mostrar las categorías verticales.
    private JLabel[] categoriaLabelsHorizontal = new JLabel[3]; // Etiquetas para mostrar las categorías horizontales.

    private TicTacToeDB dbManager; // Instancia de la clase TicTacToeDB para interactuar con la base de datos.

    private int victoriasX = 0; // Contador de victorias para el jugador X.
    private int victoriasO = 0; // Contador de victorias para el jugador O.

    // Listas de valores posibles obtenidas de la DB o predefinidas
    private List<String> clubsPosibles; // Lista de todos los clubes distintos obtenidos de la DB.
    private List<String> nacionalidadesPosibles; // Lista de todas las nacionalidades distintas obtenidas de la DB.
    private List<String> posicionesPosibles; // Lista de todas las posiciones distintas obtenidas de la DB.
    private List<String> dorsalesPosibles;     // Lista de todos los dorsales distintos obtenidos de la DB.
    private List<String> edadesPosiblesRango = Arrays.asList("18-23", "24-28", "29-33", "34-40"); // Rangos de edad fijos.

    private String[] categoriasVerticalValores = new String[3]; // Valores específicos seleccionados para las categorías verticales.
    private String[] categoriasHorizontalValores = new String[3]; // Valores específicos seleccionados para las categorías horizontales.
    private String categoriaVerticalTipo; // Almacenará el tipo de categoría para las filas (ej: "PAIS", "POSICION").
    private String categoriaHorizontalTipo; // Almacenará el tipo de categoría para las columnas (ej: "CLUB", "PAIS").

    private Random random = new Random(); // Objeto Random para operaciones aleatorias.

    // Conjunto para almacenar los nombres de los jugadores ya utilizados en la partida actual
    private Set<String> jugadoresUtilizadosEnPartida; // Almacena los nombres de los jugadores ya utilizados en la partida actual para evitar duplicados.

    // Mapa para almacenar la lista de jugadores válidos para cada casilla al inicio de la partida
    private List<String>[][] jugadoresDisponiblesPorCasilla = new ArrayList[3][3]; // Almacena una lista de nombres de jugadores válidos para cada casilla.

    public FutbolEnRaya(TicTacToeDB dbManager) { // Constructor de la clase, recibe una instancia de TicTacToeDB.
        this.dbManager = dbManager; // Asigna el gestor de base de datos.

        // Cargar los valores posibles desde la DB al inicio
        clubsPosibles = dbManager.getDistinctValues("clubs"); // Obtiene valores distintos de clubes.
        nacionalidadesPosibles = dbManager.getDistinctValues("nacionalidad"); // Obtiene valores distintos de nacionalidades.
        posicionesPosibles = dbManager.getDistinctValues("posicion"); // Obtiene valores distintos de posiciones.
        dorsalesPosibles = dbManager.getDistinctValues("numero_camiseta"); // Obtiene valores distintos de dorsales.

        System.out.println("Clubs posibles: " + clubsPosibles.size() + " valores."); // Imprime la cantidad de clubes posibles.
        System.out.println("Nacionalidades posibles: " + nacionalidadesPosibles.size() + " valores."); // Imprime la cantidad de nacionalidades posibles.
        System.out.println("Posiciones posibles: " + posicionesPosibles.size() + " valores."); // Imprime la cantidad de posiciones posibles.
        System.out.println("Dorsales posibles: " + dorsalesPosibles.size() + " valores."); // Imprime la cantidad de dorsales posibles.
        System.out.println("Edades posibles (Rango): " + edadesPosiblesRango.size() + " valores."); // Imprime la cantidad de rangos de edad posibles.

        // Verificar que haya suficientes datos para las categorías principales
        if (nacionalidadesPosibles.size() < 3 || posicionesPosibles.size() < 3 || dorsalesPosibles.size() < 3 || clubsPosibles.size() < 3) { // Comprueba si hay suficientes datos en las categorías.
            JOptionPane.showMessageDialog(null, // Muestra un mensaje de error si no hay suficientes datos.
                    "No se pudieron cargar suficientes datos de categorías desde la base de datos. Asegúrate de que los campos 'nacionalidad', 'posicion', 'numero_camiseta' y 'clubs' tengan al menos 3 valores distintos cada uno.",
                    "Error de Datos", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Sale de la aplicación.
        }

        jugadoresUtilizadosEnPartida = new HashSet<>(); // Inicializa el conjunto de jugadores utilizados en la partida.
        initUI(); // Inicializa la interfaz de usuario.
        iniciarNuevaPartida(); // Inicia una nueva partida.

        addWindowListener(new WindowAdapter() { // Añade un listener para manejar el cierre de la ventana.
            @Override
            public void windowClosing(WindowEvent windowEvent) { // Método que se ejecuta al intentar cerrar la ventana.
                dbManager.closeConnection(); // Cierra la conexión a la base de datos.
                System.exit(0); // Sale de la aplicación.
            }
        });
    }

    private void initUI() { // Método para inicializar la interfaz de usuario.
        setTitle("Fútbol en Raya"); // Establece el título de la ventana.
        setSize(800, 700); // Establece el tamaño de la ventana.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Impide que la ventana se cierre automáticamente.
        setLayout(new BorderLayout(10, 10)); // Establece el layout principal de la ventana.
        getContentPane().setBackground(new Color(0, 128, 0)); // Establece el color de fondo del panel de contenido.

        JPanel topPanel = new JPanel(new BorderLayout(5, 5)); // Crea un panel superior con un layout de borde.
        topPanel.setOpaque(false); // Hace que el panel superior sea transparente.

        JPanel playerNamesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 5)); // Panel para los nombres de los jugadores.
        playerNamesPanel.setOpaque(false); // Hace que el panel de nombres de jugadores sea transparente.

        JLabel jugador1Label = new JLabel("JUGADOR 1 (X)"); // Etiqueta para el Jugador 1.
        jugador1Label.setFont(new Font("Arial", Font.BOLD, 18)); // Establece la fuente de la etiqueta.
        jugador1Label.setForeground(Color.WHITE); // Establece el color del texto.
        jugador1Label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Establece un borde.
        jugador1Label.setPreferredSize(new Dimension(150, 40)); // Establece el tamaño preferido.
        jugador1Label.setHorizontalAlignment(SwingConstants.CENTER); // Alinea el texto al centro.

        JLabel jugador2Label = new JLabel("JUGADOR 2 (O)"); // Etiqueta para el Jugador 2.
        jugador2Label.setFont(new Font("Arial", Font.BOLD, 18)); // Establece la fuente de la etiqueta.
        jugador2Label.setForeground(Color.WHITE); // Establece el color del texto.
        jugador2Label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Establece un borde.
        jugador2Label.setPreferredSize(new Dimension(150, 40)); // Establece el tamaño preferido.
        jugador2Label.setHorizontalAlignment(SwingConstants.CENTER); // Alinea el texto al centro.

        playerNamesPanel.add(jugador1Label); // Añade la etiqueta del Jugador 1 al panel.
        JLabel xMark = new JLabel("X"); // Etiqueta para la marca 'X'.
        xMark.setFont(new Font("Arial", Font.BOLD, 30)); // Establece la fuente.
        xMark.setForeground(Color.RED); // Establece el color.
        playerNamesPanel.add(xMark); // Añade la marca 'X' al panel.

        JLabel oMark = new JLabel("O"); // Etiqueta para la marca 'O'.
        oMark.setFont(new Font("Arial", Font.BOLD, 30)); // Establece la fuente.
        oMark.setForeground(Color.BLUE); // Establece el color.
        playerNamesPanel.add(oMark); // Añade la marca 'O' al panel.

        playerNamesPanel.add(jugador2Label); // Añade la etiqueta del Jugador 2 al panel.
        topPanel.add(playerNamesPanel, BorderLayout.NORTH); // Añade el panel de nombres de jugadores al norte del panel superior.

        nombreJugadorField = new JTextField(); // Inicializa el campo de texto para el nombre del jugador.
        nombreJugadorField.setFont(new Font("Arial", Font.PLAIN, 18)); // Establece la fuente.
        nombreJugadorField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Establece un borde vacío.
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5)); // Panel para el campo de entrada.
        inputPanel.setOpaque(false); // Hace que el panel de entrada sea transparente.

        JTextField inputField = new JTextField(); // Crea un nuevo JTextField.
        inputField.setFont(new Font("Arial", Font.PLAIN, 18)); // Establece la fuente.
        inputField.setBorder(BorderFactory.createTitledBorder("Nombre Jugador")); // Establece un borde con título.
        nombreJugadorField = inputField; // Asigna el nuevo JTextField a nombreJugadorField.

        inputPanel.add(nombreJugadorField, BorderLayout.CENTER); // Añade el campo de nombre de jugador al centro del panel de entrada.
        topPanel.add(inputPanel, BorderLayout.CENTER); // Añade el panel de entrada al centro del panel superior.

        mensajeLabel = new JLabel("Turno de: " + turnoActual + " (X)", SwingConstants.CENTER); // Inicializa la etiqueta de mensaje.
        mensajeLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Establece la fuente.
        mensajeLabel.setForeground(Color.WHITE); // Establece el color del texto.
        topPanel.add(mensajeLabel, BorderLayout.SOUTH); // Añade la etiqueta de mensaje al sur del panel superior.

        add(topPanel, BorderLayout.NORTH); // Añade el panel superior al norte de la ventana principal.

        // Nuevo panel para el contenido principal del juego (vertical categories + horizontal categories + board)
        // Usamos GridBagLayout para un control más fino
        JPanel mainGameContentPanel = new JPanel(new GridBagLayout()); // Crea un panel para el contenido principal con GridBagLayout.
        mainGameContentPanel.setOpaque(false); // Hace que el panel sea transparente.

        GridBagConstraints gbc = new GridBagConstraints(); // Crea un objeto GridBagConstraints para configurar el layout.
        gbc.fill = GridBagConstraints.BOTH; // Los componentes se expanden en ambas direcciones.
        gbc.insets = new Insets(5, 5, 5, 5); // Establece los márgenes internos.

        // Panel para las categorías horizontales
        JPanel horizontalCategoriesPanel = new JPanel(new GridLayout(1, 3, 5, 5)); // Panel para las categorías horizontales con GridLayout.
        horizontalCategoriesPanel.setOpaque(false); // Hace que el panel sea transparente.
        // Sin borde EmptyBorder aquí, los insets del GridBagLayout lo manejan.
        for (int i = 0; i < 3; i++) { // Bucle para crear las etiquetas de categorías horizontales.
            categoriaLabelsHorizontal[i] = new JLabel("", SwingConstants.CENTER); // Inicializa la etiqueta.
            categoriaLabelsHorizontal[i].setFont(new Font("Arial", Font.BOLD, 14)); // Establece la fuente.
            categoriaLabelsHorizontal[i].setBackground(new Color(50, 50, 50)); // Establece el color de fondo.
            categoriaLabelsHorizontal[i].setForeground(Color.WHITE); // Establece el color del texto.
            categoriaLabelsHorizontal[i].setOpaque(true); // Hace que el fondo sea visible.
            categoriaLabelsHorizontal[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 1)); // Establece un borde.
            horizontalCategoriesPanel.add(categoriaLabelsHorizontal[i]); // Añade la etiqueta al panel.
        }
        gbc.gridx = 1; // Columna 1 (después del espacio vacío para la esquina superior izquierda)
        gbc.gridy = 0; // Fila 0
        gbc.gridwidth = 3; // Ocupa 3 columnas
        gbc.weightx = 1.0; // Se expande horizontalmente
        gbc.weighty = 0.1; // Pequeño peso vertical
        mainGameContentPanel.add(horizontalCategoriesPanel, gbc); // Añade el panel de categorías horizontales al panel principal.

        // Espacio vacío para la esquina superior izquierda
        JPanel emptyCorner = new JPanel(); // Crea un panel vacío para la esquina.
        emptyCorner.setOpaque(false); // Hace que el panel sea transparente.
        gbc.gridx = 0; // Columna 0
        gbc.gridy = 0; // Fila 0
        gbc.gridwidth = 1; // Ocupa 1 columna
        gbc.weightx = 0.1; // Para que ocupe un poco de ancho
        gbc.weighty = 0.1; // Pequeño peso vertical
        mainGameContentPanel.add(emptyCorner, gbc); // Añade el panel vacío a la esquina.


        // Panel para las categorías verticales
        JPanel verticalCategoriesPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // Panel para las categorías verticales con GridLayout.
        verticalCategoriesPanel.setOpaque(false); // Hace que el panel sea transparente.
        // Sin borde EmptyBorder aquí, los insets del GridBagLayout lo manejan.
        for (int i = 0; i < 3; i++) { // Bucle para crear las etiquetas de categorías verticales.
            categoriaLabelsVertical[i] = new JLabel("", SwingConstants.CENTER); // Inicializa la etiqueta.
            categoriaLabelsVertical[i].setFont(new Font("Arial", Font.BOLD, 14)); // Establece la fuente.
            categoriaLabelsVertical[i].setBackground(new Color(50, 50, 50)); // Establece el color de fondo.
            categoriaLabelsVertical[i].setForeground(Color.WHITE); // Establece el color del texto.
            categoriaLabelsVertical[i].setOpaque(true); // Hace que el fondo sea visible.
            categoriaLabelsVertical[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 1)); // Establece un borde.
            verticalCategoriesPanel.add(categoriaLabelsVertical[i]); // Añade la etiqueta al panel.
        }
        gbc.gridx = 0; // Columna 0
        gbc.gridy = 1; // Fila 1 (debajo del espacio vacío)
        gbc.gridheight = 3; // Ocupa 3 filas (las del tablero)
        gbc.weightx = 0.1; // Mismo peso que la esquina para mantener la proporción
        gbc.weighty = 1.0; // Se expande verticalmente
        mainGameContentPanel.add(verticalCategoriesPanel, gbc); // Añade el panel de categorías verticales al panel principal.


        // Panel del tablero
        JPanel panelTablero = new JPanel(new GridLayout(3, 3, 5, 5)); // Panel para el tablero con GridLayout.
        panelTablero.setOpaque(false); // Hace que el panel sea transparente.
        panelTablero.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Mantener el borde interno

        for (int i = 0; i < 3; i++) { // Bucle para crear los botones del tablero.
            for (int j = 0; j < 3; j++) {
                int fila = i; // Variable final para usar en el ActionListener.
                int columna = j; // Variable final para usar en el ActionListener.
                botones[i][j] = new JButton(); // Inicializa el botón.
                botones[i][j].setFont(new Font("Arial", Font.BOLD, 40)); // Establece la fuente.
                botones[i][j].setBackground(Color.GRAY); // Establece el color de fondo.
                botones[i][j].setForeground(Color.BLACK); // Establece el color del texto.
                botones[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Establece un borde.
                botones[i][j].addActionListener(e -> manejarClick(fila, columna)); // Añade un ActionListener al botón.
                panelTablero.add(botones[i][j]); // Añade el botón al panel del tablero.
            }
        }
        gbc.gridx = 1; // Columna 1
        gbc.gridy = 1; // Fila 1
        gbc.gridwidth = 3; // Ocupa 3 columnas
        gbc.gridheight = 3; // Ocupa 3 filas
        gbc.weightx = 1.0; // Se expande horizontalmente
        gbc.weighty = 1.0; // Se expande verticalmente
        mainGameContentPanel.add(panelTablero, gbc); // Añade el panel del tablero al panel principal.


        add(mainGameContentPanel, BorderLayout.CENTER); // Añadir el nuevo panel principal al centro del JFrame

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5)); // Panel inferior con un layout de borde.
        bottomPanel.setOpaque(false); // Hace que el panel sea transparente.
        scoreLabel = new JLabel("Puntuación: JUGADOR 1 (X) - " + victoriasX + " | JUGADOR 2 (O) - " + victoriasO, SwingConstants.CENTER); // Inicializa la etiqueta de puntuación.
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Establece la fuente.
        scoreLabel.setForeground(Color.WHITE); // Establece el color del texto.
        bottomPanel.add(scoreLabel, BorderLayout.NORTH); // Añade la etiqueta de puntuación al norte del panel inferior.

        reiniciarButton = new JButton("Reiniciar Juego"); // Inicializa el botón de reiniciar.
        reiniciarButton.setFont(new Font("Arial", Font.BOLD, 18)); // Establece la fuente.
        reiniciarButton.setBackground(new Color(255, 165, 0)); // Establece el color de fondo.
        reiniciarButton.setForeground(Color.WHITE); // Establece el color del texto.
        reiniciarButton.addActionListener(e -> iniciarNuevaPartida()); // Añade un ActionListener al botón.
        reiniciarButton.setVisible(false); // Hace que el botón sea invisible inicialmente.
        bottomPanel.add(reiniciarButton, BorderLayout.SOUTH); // Añade el botón de reiniciar al sur del panel inferior.

        add(bottomPanel, BorderLayout.SOUTH); // Añade el panel inferior al sur de la ventana principal.
    }

    private void iniciarNuevaPartida() { // Método para iniciar una nueva partida.
        for (int i = 0; i < 3; i++) { // Bucle para inicializar el tablero y los botones.
            for (int j = 0; j < 3; j++) {
                tablero[i][j] = null; // Reinicia el estado lógico de la casilla.
                botones[i][j].setText(""); // Borra el texto del botón.
                botones[i][j].setEnabled(true); // Habilita el botón.
                botones[i][j].setBackground(Color.GRAY); // Restablece el color de fondo.
                botones[i][j].setForeground(Color.BLACK); // Restablece el color del texto.
                jugadoresDisponiblesPorCasilla[i][j] = new ArrayList<>(); // Reinicia la lista de jugadores disponibles para la casilla.
            }
        }
        turnoActual = "X"; // Establece el turno inicial a 'X'.
        mensajeLabel.setText("Turno de: JUGADOR 1 (X)"); // Actualiza el mensaje de turno.
        reiniciarButton.setVisible(false); // Oculta el botón de reiniciar.
        jugadoresUtilizadosEnPartida.clear(); // Limpia el conjunto de jugadores utilizados.

        seleccionarCategoriasParaTableroJugable(); // Selecciona nuevas categorías para el tablero.
        actualizarCategoriasUI(); // Actualiza la interfaz de usuario con las nuevas categorías.
    }

    /**
     * Selecciona UN tipo de categoría para las columnas (PAIS o CLUB) y UN tipo diferente
     * para las filas (POSICION, NACIONALIDAD, DORSAL o EDAD_RANGO).
     * Luego, selecciona 3 valores distintos para cada tipo,
     * asegurando que todas las 9 intersecciones tengan al menos un jugador
     * válido y no utilizado al inicio de la partida.
     * Prioriza POSICION, DORSAL, PAIS para el eje vertical sobre EDAD_RANGO.
     */
    private void seleccionarCategoriasParaTableroJugable() { // Método para seleccionar categorías para el tablero.
        List<String> horizontalCategoryTypes = Arrays.asList("PAIS", "CLUB"); // Tipos de categorías posibles para el eje horizontal.
        // Prioridad para categorías verticales: primero las que son más fáciles de adivinar/jugar
        // Luego edad, que es más difícil.
        List<String> prioritizedVerticalTypes = Arrays.asList("POSICION", "DORSAL", "PAIS"); // Tipos de categorías priorizados para el eje vertical.
        List<String> fallbackVerticalType = Collections.singletonList("EDAD_RANGO"); // Tipo de categoría de respaldo para el eje vertical.

        int maxAttempts = 1000; // Número máximo de intentos para encontrar un tablero válido.
        boolean tableroValidoEncontrado = false; // Bandera para indicar si se encontró un tablero válido.

        for (int attempt = 0; attempt < maxAttempts; attempt++) { // Bucle para intentar encontrar un tablero válido.
            boolean currentAttemptValid = true; // Bandera para indicar si el intento actual es válido.

            // 1. Elegir un tipo de categoría aleatorio para las columnas (horizontal)
            Collections.shuffle(horizontalCategoryTypes); // Mezcla los tipos de categorías horizontales.
            categoriaHorizontalTipo = horizontalCategoryTypes.get(0); // Selecciona el primer tipo de categoría horizontal.

            // 2. Elegir un tipo de categoría para las filas (vertical) con prioridad
            List<String> availableVerticalTypes = new ArrayList<>(prioritizedVerticalTypes); // Crea una lista de tipos de categorías verticales disponibles.
            availableVerticalTypes.remove(categoriaHorizontalTipo); // No repetir la categoría horizontal

            if (availableVerticalTypes.isEmpty()) { // Si no hay tipos verticales disponibles después de remover el horizontal.
                // Si todas las opciones priorizadas fueron la horizontal,
                // forzar la vertical a ser la horizontal o edad_rango si son las únicas
                if (prioritizedVerticalTypes.contains(categoriaHorizontalTipo)) { // Si la categoría horizontal está en las priorizadas verticales.
                    categoriaVerticalTipo = categoriaHorizontalTipo; // Permitir que se repita si no hay otra opción
                } else {
                    categoriaVerticalTipo = fallbackVerticalType.get(0); // Forzar EDAD_RANGO
                }
            } else {
                Collections.shuffle(availableVerticalTypes); // Mezcla los tipos de categorías verticales disponibles.
                categoriaVerticalTipo = availableVerticalTypes.get(0); // Selecciona el primer tipo de categoría vertical.
            }

            // Si después de la selección priorizada, la categoría vertical es la misma que la horizontal
            // y no hay otra opción priorizada, intenta con EDAD_RANGO como última opción.
            if (categoriaVerticalTipo.equals(categoriaHorizontalTipo) && availableVerticalTypes.size() == 0) { // Si las categorías son las mismas y no hay más opciones.
                 // Esto ocurrirá si horizontal fue PAIS y PAIS era la única priorizada vertical que quedaba.
                 // En este caso, si no podemos encontrar un tablero con PAIS en ambos, intentamos con EDAD_RANGO.
                System.out.println("Categoría horizontal y vertical son la misma (" + categoriaHorizontalTipo + ") y no hay otras opciones priorizadas. Intentando con EDAD_RANGO para vertical."); // Mensaje de depuración.
                categoriaVerticalTipo = fallbackVerticalType.get(0); // Intenta con EDAD_RANGO
            }


            List<String> valoresVerticalesSeleccionados = new ArrayList<>(); // Lista para almacenar los valores verticales seleccionados.
            List<String> valoresHorizontalesSeleccionados = new ArrayList<>(); // Lista para almacenar los valores horizontales seleccionados.

            // Obtener 3 valores para las categorías seleccionadas
            if (categoriaHorizontalTipo.equals("CLUB")) { // Si la categoría horizontal es "CLUB".
                valoresHorizontalesSeleccionados.addAll(dbManager.getMostFrequentValues("clubs", 3)); // Obtiene los 3 clubes más frecuentes.
            } else if (categoriaHorizontalTipo.equals("PAIS")) { // Si la categoría horizontal es "PAIS".
                valoresHorizontalesSeleccionados.addAll(dbManager.getMostFrequentValues("nacionalidad", 3)); // Obtiene las 3 nacionalidades más frecuentes.
            } else { // Esto no debería pasar con las categorías horizontales actuales
                List<String> allValues = getValueListForCategory(categoriaHorizontalTipo); // Obtiene todos los valores para la categoría horizontal.
                if (allValues.size() >= 3) { // Si hay al menos 3 valores.
                    Collections.shuffle(allValues); // Mezcla los valores.
                    valoresHorizontalesSeleccionados.addAll(allValues.subList(0, 3)); // Selecciona los 3 primeros.
                } else {
                    currentAttemptValid = false; // Marca el intento como inválido.
                    System.out.println("Intento " + (attempt + 1) + " fallido: No hay suficientes valores para la categoría horizontal " + categoriaHorizontalTipo + " (todos los valores). Reintentando..."); // Mensaje de depuración.
                    continue; // Pasa al siguiente intento.
                }
            }

            // Lógica de selección para la categoría vertical
            if (categoriaVerticalTipo.equals("CLUB")) { // Poco probable para vertical, pero por si acaso
                valoresVerticalesSeleccionados.addAll(dbManager.getMostFrequentValues("clubs", 3)); // Obtiene los 3 clubes más frecuentes.
            } else if (categoriaVerticalTipo.equals("PAIS")) { // Si la categoría vertical es "PAIS".
                valoresVerticalesSeleccionados.addAll(dbManager.getMostFrequentValues("nacionalidad", 3)); // Obtiene las 3 nacionalidades más frecuentes.
            } else if (categoriaVerticalTipo.equals("POSICION")) { // Si la categoría vertical es "POSICION".
                List<String> allPosiciones = new ArrayList<>(posicionesPosibles); // Obtiene todas las posiciones posibles.
                if (allPosiciones.size() >= 3) { // Si hay al menos 3 posiciones.
                    Collections.shuffle(allPosiciones); // Mezcla las posiciones.
                    valoresVerticalesSeleccionados.addAll(allPosiciones.subList(0, 3)); // Selecciona las 3 primeras.
                } else {
                    currentAttemptValid = false; // Marca el intento como inválido.
                    System.out.println("Intento " + (attempt + 1) + " fallido: No hay suficientes valores para la categoría vertical POSICION. Reintentando..."); // Mensaje de depuración.
                    continue; // Pasa al siguiente intento.
                }
            } else if (categoriaVerticalTipo.equals("DORSAL")) { // Si la categoría vertical es "DORSAL".
                // Para dorsal, aunque hay muchos, elegir 3 al azar puede ser difícil.
                // Podríamos intentar seleccionar dorsales comunes (1-11, 7, 9, 10) si la DB lo permite
                // o seguir con aleatorio si hay suficientes para no fallar.
                // Por ahora, mantenemos la selección aleatoria de distintos valores.
                List<String> allDorsales = new ArrayList<>(dorsalesPosibles); // Obtiene todos los dorsales posibles.
                if (allDorsales.size() >= 3) { // Si hay al menos 3 dorsales.
                    Collections.shuffle(allDorsales); // Mezcla los dorsales.
                    valoresVerticalesSeleccionados.addAll(allDorsales.subList(0, 3)); // Selecciona los 3 primeros.
                } else {
                    currentAttemptValid = false; // Marca el intento como inválido.
                    System.out.println("Intento " + (attempt + 1) + " fallido: No hay suficientes valores para la categoría vertical DORSAL. Reintentando..."); // Mensaje de depuración.
                    continue; // Pasa al siguiente intento.
                }
            } else if (categoriaVerticalTipo.equals("EDAD_RANGO")) { // Si la categoría vertical es "EDAD_RANGO".
                List<String> allEdades = new ArrayList<>(edadesPosiblesRango); // Obtiene todos los rangos de edad posibles.
                if (allEdades.size() >= 3) { // Si hay al menos 3 rangos de edad.
                    Collections.shuffle(allEdades); // Mezcla los rangos de edad.
                    valoresVerticalesSeleccionados.addAll(allEdades.subList(0, 3)); // Selecciona los 3 primeros.
                } else {
                    currentAttemptValid = false; // Marca el intento como inválido.
                    System.out.println("Intento " + (attempt + 1) + " fallido: No hay suficientes valores para la categoría vertical EDAD_RANGO. Reintentando..."); // Mensaje de depuración.
                    continue; // Pasa al siguiente intento.
                }
            }


            // Verificar que hemos obtenido 3 valores para cada
            if (valoresVerticalesSeleccionados.size() < 3 || valoresHorizontalesSeleccionados.size() < 3) { // Si no se obtuvieron suficientes valores para las categorías.
                System.out.println("Intento " + (attempt + 1) + " fallido: No se pudieron obtener suficientes valores para las categorías. Reintentando..."); // Mensaje de depuración.
                currentAttemptValid = false; // Marca el intento como inválido.
                continue; // Pasa al siguiente intento.
            }

            // Asignar los valores a las categorías del tablero
            for (int i = 0; i < 3; i++) { // Asigna los valores a las categorías verticales.
                categoriasVerticalValores[i] = valoresVerticalesSeleccionados.get(i);
                categoriasHorizontalValores[i] = valoresHorizontalesSeleccionados.get(i); // Asigna los valores a las categorías horizontales.
            }

            // Verificar si cada casilla tiene al menos un jugador DISPONIBLE para empezar
            for (int r = 0; r < 3; r++) { // Bucle para verificar cada casilla del tablero.
                for (int c = 0; c < 3; c++) {
                    List<String> playersForThisCell = dbManager.getPlayersByCategories( // Obtiene los jugadores válidos para la casilla.
                        categoriaVerticalTipo, categoriasVerticalValores[r],
                        categoriaHorizontalTipo, categoriasHorizontalValores[c]
                    );

                    if (playersForThisCell.isEmpty()) { // Si no se encontraron jugadores para la combinación.
                        currentAttemptValid = false; // Marca el intento como inválido.
                        System.out.println("Intento " + (attempt + 1) + " fallido en la casilla (" + r + "," + c + "):"); // Mensaje de depuración.
                        System.out.println("  Tipo Vertical: " + categoriaVerticalTipo + ", Valor: " + categoriasVerticalValores[r]); // Mensaje de depuración.
                        System.out.println("  Tipo Horizontal: " + categoriaHorizontalTipo + ", Valor: " + categoriasHorizontalValores[c]); // Mensaje de depuración.
                        System.out.println("  ¡No se encontraron jugadores para esta combinación! Reintentando..."); // Mensaje de depuración.
                        break; // Sale del bucle interno.
                    }
                    jugadoresDisponiblesPorCasilla[r][c].clear(); // Limpia la lista de jugadores disponibles para la casilla.
                    jugadoresDisponiblesPorCasilla[r][c].addAll(playersForThisCell); // Añade los jugadores encontrados a la lista.
                }
                if (!currentAttemptValid) break; // Si el intento no es válido, sale del bucle externo.
            }

            if (currentAttemptValid) { // Si el intento actual es válido.
                tableroValidoEncontrado = true; // Marca que se encontró un tablero válido.
                System.out.println("\n--- Categorías válidas encontradas en intento " + (attempt + 1) + " ---"); // Mensaje de depuración.
                System.out.println("  Tipo Vertical: " + categoriaVerticalTipo); // Mensaje de depuración.
                for(int i=0; i<3; i++) { // Imprime los valores de las categorías verticales.
                    System.out.println("    Valor Vertical " + i + ": " + categoriasVerticalValores[i]);
                }
                System.out.println("  Tipo Horizontal: " + categoriaHorizontalTipo); // Mensaje de depuración.
                for(int i=0; i<3; i++) { // Imprime los valores de las categorías horizontales.
                    System.out.println("    Valor Horizontal " + i + ": " + categoriasHorizontalValores[i]);
                }

                System.out.println("\nJugadores disponibles por casilla (al inicio de la partida):"); // Mensaje de depuración.
                for (int r = 0; r < 3; r++) { // Imprime la cantidad de jugadores disponibles por casilla.
                    for (int c = 0; c < 3; c++) {
                        System.out.println("  Casilla (" + r + "," + c + "): " + jugadoresDisponiblesPorCasilla[r][c].size() + " jugadores.");
                    }
                }
                System.out.println("---------------------------------------------------\n"); // Mensaje de depuración.
                break; // Sale del bucle de intentos.
            }
        }

        if (!tableroValidoEncontrado) { // Si no se encontró un tablero válido después de todos los intentos.
            JOptionPane.showMessageDialog(this, // Muestra un mensaje de error.
                    "No se pudo generar un tablero jugable con las categorías de la base de datos después de " + maxAttempts + " intentos. " +
                    "Esto ocurre si no hay combinaciones de categorías que resulten en al menos un jugador en cada una de las 9 intersecciones. " +
                    "Considera añadir más datos o relajar las restricciones de categorías.",
                    "Error de Generación de Tablero", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Sale de la aplicación.
        }
    }

    private List<String> getValueListForCategory(String categoryType) { // Método para obtener la lista de valores para un tipo de categoría.
        switch (categoryType) { // Selecciona la lista de valores según el tipo de categoría.
            case "CLUB": return clubsPosibles; // Devuelve la lista de clubes posibles.
            case "PAIS": return nacionalidadesPosibles; // Devuelve la lista de nacionalidades posibles.
            case "POSICION": return posicionesPosibles; // Devuelve la lista de posiciones posibles.
            case "DORSAL": return dorsalesPosibles; // Devuelve la lista de dorsales posibles.
            case "EDAD_RANGO": return edadesPosiblesRango; // Devuelve la lista de rangos de edad posibles.
            default: return Collections.emptyList(); // Si el tipo no coincide, devuelve una lista vacía.
        }
    }

    private void actualizarCategoriasUI() { // Método para actualizar la interfaz de usuario con las categorías.
        for (int i = 0; i < 3; i++) { // Bucle para actualizar las etiquetas de categorías verticales.
            String displayValueV = categoriasVerticalValores[i]; // Obtiene el valor de la categoría vertical.
            categoriaLabelsVertical[i].setText(categoriaVerticalTipo + ": " + displayValueV); // Establece el texto de la etiqueta vertical.
        }

        for (int i = 0; i < 3; i++) { // Bucle para actualizar las etiquetas de categorías horizontales.
            String displayValueH = categoriasHorizontalValores[i]; // Obtiene el valor de la categoría horizontal.
            categoriaLabelsHorizontal[i].setText(categoriaHorizontalTipo + ": " + displayValueH); // Establece el texto de la etiqueta horizontal.
        }
    }

    private void manejarClick(int fila, int columna) { // Método que se ejecuta al hacer clic en un botón del tablero.
        if (tablero[fila][columna] != null) { // Si la casilla ya está marcada.
            JOptionPane.showMessageDialog(this, "Esta casilla ya está marcada.", "Casilla Ocupada", JOptionPane.WARNING_MESSAGE); // Muestra un mensaje de advertencia.
            return; // Sale del método.
        }

        String nombreJugador = nombreJugadorField.getText().trim(); // Obtiene el nombre del jugador del campo de texto.
        if (nombreJugador.isEmpty()) { // Si el campo de nombre está vacío.
            JOptionPane.showMessageDialog(this, "Por favor, introduce un nombre de jugador.", "Campo Vacío", JOptionPane.WARNING_MESSAGE); // Muestra un mensaje de advertencia.
            return; // Sale del método.
        }

        // Caso 1: El jugador ya ha sido utilizado en CUALQUIER casilla de esta partida.
        // En este caso, no cambia el turno. Es un error que el jugador no debería cometer.
        if (jugadoresUtilizadosEnPartida.contains(nombreJugador)) { // Si el jugador ya ha sido utilizado en la partida.
            mensajeLabel.setText("¡Error! El jugador '" + nombreJugador + "' ya ha sido utilizado en esta partida. Turno sigue: " + (turnoActual.equals("X") ? "JUGADOR 1 (X)" : "JUGADOR 2 (O)")); // Muestra un mensaje de error.
            nombreJugadorField.setText(""); // Limpia el campo de texto.
            return; // No se cambia el turno.
        }

        List<String> jugadoresValidosParaCasilla = jugadoresDisponiblesPorCasilla[fila][columna]; // Obtiene la lista de jugadores válidos para la casilla.

        // Se usa jugadoresRealmenteDisponibles para la validación interna,
        // pero la condición `jugadoresUtilizadosEnPartida.contains(nombreJugador)` ya la manejamos arriba.
        // Así que aquí solo nos importa si el nombreJugador cumple con las categorías de la casilla.
        
        if (jugadoresValidosParaCasilla.contains(nombreJugador)) { // Si el nombre del jugador es válido para la casilla.
            // El jugador es válido para esta casilla y no ha sido utilizado antes.
            botones[fila][columna].setText(turnoActual); // Establece el texto del botón al turno actual.
            botones[fila][columna].setEnabled(false); // Deshabilita el botón.
            botones[fila][columna].setBackground(turnoActual.equals("X") ? new Color(200, 0, 0) : new Color(0, 0, 200)); // Establece el color de fondo del botón según el turno.
            botones[fila][columna].setForeground(Color.WHITE); // Establece el color del texto a blanco.

            tablero[fila][columna] = turnoActual; // Actualiza el estado lógico del tablero.
            mensajeLabel.setText("¡Correcto! Turno de: " + (turnoActual.equals("X") ? "JUGADOR 2 (O)" : "JUGADOR 1 (X)")); // Actualiza el mensaje de turno.

            jugadoresUtilizadosEnPartida.add(nombreJugador); // Añade el jugador al conjunto de jugadores utilizados.

            if (hayGanador()) { // Si hay un ganador.
                JOptionPane.showMessageDialog(this, "¡Jugador " + turnoActual + " ha ganado!"); // Muestra un mensaje de ganador.
                if (turnoActual.equals("X")) { // Incrementa la puntuación del ganador.
                    victoriasX++;
                } else {
                    victoriasO++;
                }
                actualizarPuntuacion(); // Actualiza la puntuación en la UI.
                reiniciarButton.setVisible(true); // Muestra el botón de reiniciar.
                for (int i = 0; i < 3; i++) { // Deshabilita todos los botones del tablero.
                    for (int j = 0; j < 3; j++) {
                        botones[i][j].setEnabled(false);
                    }
                }
                return; // Sale del método.
            } else if (tableroLleno()) { // Si el tablero está lleno (empate).
                JOptionPane.showMessageDialog(this, "¡Empate!"); // Muestra un mensaje de empate.
                reiniciarButton.setVisible(true); // Muestra el botón de reiniciar.
                return; // Sale del método.
            }

            // Cambiar turno solo si la jugada fue exitosa
            turnoActual = turnoActual.equals("X") ? "O" : "X"; // Cambia el turno.
        } else {
            // El jugador ingresó un nombre INCORRECTO para esta casilla (no cumple las categorías).
            // Aquí es donde queremos que el turno cambie.
            mensajeLabel.setText("¡Incorrecto! El jugador '" + nombreJugador + "' no cumple las categorías de esta casilla. Turno para: " + (turnoActual.equals("X") ? "JUGADOR 2 (O)" : "JUGADOR 1 (X)")); // Muestra un mensaje de error.
            nombreJugadorField.setText(""); // Limpia el campo de texto.
            // Cambiar turno porque el jugador falló
            turnoActual = turnoActual.equals("X") ? "O" : "X"; // Cambia el turno.
        }
        nombreJugadorField.setText(""); // Siempre limpia el campo de texto después del intento.

        // Esta verificación se mantiene al final para ver si el juego se atasca.
        if (!quedanOpcionesParaJugadorActual()) { // Si no quedan opciones válidas para el jugador actual.
            JOptionPane.showMessageDialog(this, "¡No quedan jugadores válidos para las casillas restantes para el " + (turnoActual.equals("X") ? "JUGADOR 1 (X)" : "JUGADOR 2 (O)") + "! La partida termina en empate forzado.", "Juego Atascado", JOptionPane.INFORMATION_MESSAGE); // Muestra un mensaje de juego atascado.
            reiniciarButton.setVisible(true); // Muestra el botón de reiniciar.
            for (int i = 0; i < 3; i++) { // Deshabilita todos los botones del tablero.
                for (int j = 0; j < 3; j++) {
                    botones[i][j].setEnabled(false);
                }
            }
            return; // Sale del método.
        }
    }

    private boolean quedanOpcionesParaJugadorActual() { // Método para verificar si quedan opciones para el jugador actual.
        for (int r = 0; r < 3; r++) { // Itera sobre las filas del tablero.
            for (int c = 0; c < 3; c++) { // Itera sobre las columnas del tablero.
                if (tablero[r][c] == null) { // Si la casilla está vacía.
                    List<String> jugadoresValidosParaCasilla = jugadoresDisponiblesPorCasilla[r][c]; // Obtiene los jugadores válidos para la casilla.
                    List<String> jugadoresRealmenteDisponibles = jugadoresValidosParaCasilla.stream() // Filtra los jugadores que no han sido utilizados.
                        .filter(player -> !jugadoresUtilizadosEnPartida.contains(player))
                        .collect(Collectors.toList());

                    if (!jugadoresRealmenteDisponibles.isEmpty()) { // Si hay al menos un jugador realmente disponible.
                        return true; // Devuelve true.
                    }
                }
            }
        }
        return false; // Si no hay opciones, devuelve false.
    }

    private boolean tableroLleno() { // Método para verificar si el tablero está lleno.
        for (int i = 0; i < 3; i++) { // Itera sobre las filas.
            for (int j = 0; j < 3; j++) { // Itera sobre las columnas.
                if (tablero[i][j] == null) { // Si encuentra una casilla vacía.
                    return false; // El tablero no está lleno.
                }
            }
        }
        return true; // Si no se encontraron casillas vacías, el tablero está lleno.
    }

    private void actualizarPuntuacion() { // Método para actualizar la etiqueta de puntuación.
        scoreLabel.setText("Puntuación: JUGADOR 1 (X) - " + victoriasX + " | JUGADOR 2 (O) - " + victoriasO); // Actualiza el texto de la etiqueta.
    }

    private boolean hayGanador() { // Método para verificar si hay un ganador.
        // Comprobar filas
        for (int i = 0; i < 3; i++) { // Itera sobre las filas.
            if (tablero[i][0] != null && // Si la primera casilla de la fila no es nula.
                tablero[i][0].equals(tablero[i][1]) && // Y las tres casillas de la fila son iguales.
                tablero[i][1].equals(tablero[i][2])) {
                return true; // Hay un ganador.
            }
        }
        // Comprobar columnas
        for (int i = 0; i < 3; i++) { // Itera sobre las columnas.
            if (tablero[0][i] != null && // Si la primera casilla de la columna no es nula.
                tablero[0][i].equals(tablero[1][i]) && // Y las tres casillas de la columna son iguales.
                tablero[1][i].equals(tablero[2][i])) {
                return true; // Hay un ganador.
            }
        }
        // Comprobar diagonales
        if (tablero[0][0] != null && // Si la casilla superior izquierda no es nula.
            tablero[0][0].equals(tablero[1][1]) && // Y las casillas de la diagonal principal son iguales.
            tablero[1][1].equals(tablero[2][2])) {
            return true; // Hay un ganador.
        }
        if (tablero[0][2] != null && // Si la casilla superior derecha no es nula.
            tablero[0][2].equals(tablero[1][1]) && // Y las casillas de la diagonal secundaria son iguales.
            tablero[1][1].equals(tablero[2][0])) {
            return true; // Hay un ganador.
        }
        return false; // No hay ganador.
    }

    public static void main(String[] args) { // Método principal de la aplicación.
        TicTacToeDB dbManager = null; // Declara una instancia de TicTacToeDB.
        try {
            dbManager = new TicTacToeDB(); // Intenta crear una nueva instancia de TicTacToeDB.
            final TicTacToeDB finalDbManager = dbManager; // Crea una copia final para usar en el hilo de Swing.
            SwingUtilities.invokeLater(() -> { // Ejecuta la GUI en el hilo de despacho de eventos de Swing.
                FutbolEnRaya juego = new FutbolEnRaya(finalDbManager); // Crea una nueva instancia del juego FutbolEnRaya.
                juego.setVisible(true); // Hace visible la ventana del juego.
            });
        } catch (RuntimeException e) { // Captura excepciones en tiempo de ejecución.
            JOptionPane.showMessageDialog(null, // Muestra un mensaje de error si falla la conexión a la DB.
                "Error al conectar con la base de datos: " + e.getMessage() + "\n" +
                "Por favor, asegúrate de que MongoDB esté corriendo y la cadena de conexión sea correcta.",
                "Error de Conexión a DB",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Sale de la aplicación.
        }
    }
}