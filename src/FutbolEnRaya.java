import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FutbolEnRaya extends JFrame {
    private JTextField nombreJugadorField;
    private JButton[][] botones = new JButton[3][3];
    private String[][] tablero = new String[3][3];
    private String turnoActual = "X";
    private JLabel mensajeLabel;
    private String[] categoriasVertical = {"VALENCIA", "OSASUNA", "SEVILLA"};
    private String[] categoriasHorizontal = {"POR", "DC", "MCO"};
    private MongoCollection<Document> coleccionJugadores;

    public FutbolEnRaya(MongoCollection<Document> coleccionJugadores) {
        this.coleccionJugadores = coleccionJugadores;
        initUI();
    }

    private void initUI() {
        setTitle("Fútbol en Raya");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelTablero = new JPanel(new GridLayout(3, 3));
        nombreJugadorField = new JTextField();
        mensajeLabel = new JLabel("Turno de: " + turnoActual);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int fila = i;
                int columna = j;
                botones[i][j] = new JButton();
                botones[i][j].addActionListener(e -> manejarClick(fila, columna));
                panelTablero.add(botones[i][j]);
            }
        }

        add(nombreJugadorField, BorderLayout.NORTH);
        add(panelTablero, BorderLayout.CENTER);
        add(mensajeLabel, BorderLayout.SOUTH);
    }

    private void manejarClick(int fila, int columna) {
        if (tablero[fila][columna] != null) return;

        String nombreJugador = nombreJugadorField.getText().trim();
        if (nombreJugador.isEmpty()) return;

        String categoriaH = categoriasHorizontal[fila];
        String categoriaV = categoriasVertical[columna];

        if (validarJugador(nombreJugador, categoriaH, categoriaV)) {
            botones[fila][columna].setText(turnoActual);
            tablero[fila][columna] = turnoActual;
            mensajeLabel.setText("¡Correcto! Turno de: " + (turnoActual.equals("X") ? "O" : "X"));
            if (hayGanador()) {
                JOptionPane.showMessageDialog(this, "¡Jugador " + turnoActual + " ha ganado!");
                reiniciarJuego();
                return;
            }
            turnoActual = turnoActual.equals("X") ? "O" : "X";
        } else {
            mensajeLabel.setText("¡Incorrecto! Turno sigue: " + turnoActual);
        }

        nombreJugadorField.setText("");
    }

    private boolean validarJugador(String nombre, String categoriaH, String categoriaV) {
        Document query = new Document("nombre", nombre);
        Document jugador = coleccionJugadores.find(query).first();
        if (jugador == null) return false;

        List<String> posiciones = jugador.getList("posiciones", String.class);
        List<String> equipos = jugador.getList("equipos", String.class);
        String nacionalidad = jugador.getString("nacionalidad");
        int edad = jugador.getInteger("edad", 0);

        boolean hCoincide = validarCategoria(categoriaH, posiciones, nacionalidad, edad, equipos);
        boolean vCoincide = validarCategoria(categoriaV, posiciones, nacionalidad, edad, equipos);

        return hCoincide && vCoincide;
    }

    private boolean validarCategoria(String categoria, List<String> posiciones, String nacionalidad, int edad, List<String> equipos) {
        if (posiciones.contains(categoria)) return true;
        if (equipos.contains(categoria)) return true;
        if (nacionalidad.equalsIgnoreCase(categoria)) return true;

        try {
            int edadCategoria = Integer.parseInt(categoria.replaceAll("[^0-9]", ""));
            if (categoria.contains(">")) return edad > edadCategoria;
            if (categoria.contains("<")) return edad < edadCategoria;
        } catch (Exception ignored) {}

        return false;
    }

    private boolean hayGanador() {
        for (int i = 0; i < 3; i++) {
            if (tablero[i][0] != null && tablero[i][0].equals(tablero[i][1]) && tablero[i][1].equals(tablero[i][2])) return true;
            if (tablero[0][i] != null && tablero[0][i].equals(tablero[1][i]) && tablero[1][i].equals(tablero[2][i])) return true;
        }
        return tablero[0][0] != null && tablero[0][0].equals(tablero[1][1]) && tablero[1][1].equals(tablero[2][2]) ||
               tablero[0][2] != null && tablero[0][2].equals(tablero[1][1]) && tablero[1][1].equals(tablero[2][0]);
    }

    private void reiniciarJuego() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tablero[i][j] = null;
                botones[i][j].setText("");
            }
        }
        turnoActual = "X";
        mensajeLabel.setText("Turno de: " + turnoActual);
    }

    public static void main(String[] args) {
        // Asegúrate de que MongoDB esté corriendo y que la URI sea correcta
        MongoClient mongoClient = mongoClient.create("mongodb+srv://kelsioner:3zOSa7Jnw0iJUPY7@proyectointermodular.0czvebk.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoIntermodular\r\n");
        MongoDatabase database = mongoClient.getDatabase("futbol_en_raya");
        MongoCollection<Document> coleccion = database.getCollection("jugadores");

        SwingUtilities.invokeLater(() -> {
            FutbolEnRaya juego = new FutbolEnRaya(coleccion);
            juego.setVisible(true);
        });
    }
}