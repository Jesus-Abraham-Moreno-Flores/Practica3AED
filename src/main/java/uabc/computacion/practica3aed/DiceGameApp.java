package uabc.computacion.practica3aed;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;

public class DiceGameApp extends Application {
    private Control sistema;

    private final VBox[] panelEstaciones = new VBox[Control.NUM_ESTACIONES];
    private final Label[] labelDado = new Label[Control.NUM_ESTACIONES];
    private final Label[] labelCola = new Label[Control.NUM_ESTACIONES];
    private final Label[] labelMovidas = new Label[Control.NUM_ESTACIONES];
    private final FlowPane[] vistaPersonas = new FlowPane[Control.NUM_ESTACIONES];

    private Label labelRonda, labelEnSistema, labelSalidas, labelPromedio;
    private Button botonRoll, botonMove, botonReset;

    private LineChart<String, Number> throughputChart;
    private XYChart.Series<String, Number> throughputSeries;
    private BarChart<String, Number> activityChart;
    private XYChart.Series<String, Number> activitySeries;
    private LineChart<String, Number> wipChart;
    private XYChart.Series<String, Number> wipSeries;
    private double[] actividadAcumulada = new double[Control.NUM_ESTACIONES];
    private int[] totalDadosPorEstacion = new int[Control.NUM_ESTACIONES];
    private int[] totalMovidosPorEstacion = new int[Control.NUM_ESTACIONES];

    @Override
    public void start(Stage stage){
        sistema = new Control();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Panel Superior y Estaciones
        VBox topContainer = new VBox(10);
        topContainer.getChildren().addAll(crearPanelSuperior(), crearPanelEstaciones());
        root.setTop(topContainer);

        //Panel central con graficas
        GridPane panelGraficas = crearPanelGraficas();
        root.setCenter(panelGraficas);

        root.setBottom(crearPanelBotones());

        Scene scene = new Scene(root, 1280, 850);
        stage.setTitle("The Dice Game - Analítica de Procesos");
        stage.setScene(scene);
        stage.show();

        actualizarVista();
    }

    public VBox crearPanelSuperior(){
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(16, 20, 8, 20));
        panel.setAlignment(Pos.CENTER);

        Label titulo = new Label("The Dice Game");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web("#e94560"));

        Label subtitulo = new Label("Simulación de línea de producción");
        subtitulo.setFont(Font.font("Arial", 13));
        subtitulo.setTextFill(Color.web("#a0a0b0"));

        HBox stats = new HBox(30);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(8, 0, 0, 0));

        labelRonda = crearLabelStat("Ronda", "0 /20");
        labelEnSistema = crearLabelStat("En sistema", "36");
        labelSalidas = crearLabelStat("Salidas", "0");
        labelPromedio = crearLabelStat("Tiempo promedio", "-");

        stats.getChildren().addAll(labelRonda, sep(), labelEnSistema, sep(), labelSalidas, sep(), labelPromedio);
        panel.getChildren().addAll(titulo, subtitulo, stats);
        return panel;
    }

    public GridPane crearPanelGraficas() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10, 20, 10, 20));
        grid.setAlignment(Pos.CENTER);

        //Configuracion de las lineas de Throughput
        CategoryAxis xAxisT = new CategoryAxis();
        NumberAxis yAxisT = new NumberAxis();
        xAxisT.setLabel("Ronda");
        yAxisT.setLabel("Salidas");
        throughputChart = new LineChart<>(xAxisT, yAxisT);
        throughputChart.setTitle("THROUGHPUT (Salidas por Ronda)");
        throughputChart.setCreateSymbols(true);
        throughputChart.setPrefHeight(300);
        throughputSeries = new XYChart.Series<>();
        throughputSeries.setName("Unidades Finalizadas");
        throughputChart.getData().add(throughputSeries);

        //Configuracion de la grafica de Activity
        CategoryAxis xAxisA = new CategoryAxis();
        NumberAxis yAxisA = new NumberAxis();
        xAxisA.setLabel("Estación");
        yAxisA.setLabel("% Eficiencia");
        yAxisA.setAutoRanging(false);
        yAxisA.setUpperBound(100);
        activityChart = new BarChart<>(xAxisA, yAxisA);
        activityChart.setTitle("ACTIVITY (Uso de Capacidad %)");
        activityChart.setPrefHeight(300);
        activitySeries = new XYChart.Series<>();
        activitySeries.setName("Promedio de Actividad Real");
        activityChart.getData().add(activitySeries);

        //Configuracion de number in system o wip
        CategoryAxis xAxisW = new CategoryAxis();
        NumberAxis yAxisW = new NumberAxis();
        xAxisW.setLabel("Ronda");
        yAxisW.setLabel("Personas");
        wipChart = new LineChart<>(xAxisW, yAxisW);
        wipChart.setTitle("NUMBER IN SYSTEM (WIP)");
        wipChart.setCreateSymbols(true);
        wipChart.setPrefHeight(250);
        wipSeries = new XYChart.Series<>();
        wipSeries.setName("Personas en Proceso");
        wipChart.getData().add(wipSeries);

        // Acomodar en el Grid (Columna, Fila)
        grid.add(throughputChart, 0, 0);
        grid.add(activityChart, 1, 0);
        grid.add(wipChart, 0, 1, 2, 1); // La de WIP ocupa las dos columnas abajo

        // Estilo para títulos
        grid.lookupAll(".chart-title").forEach(n -> n.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;"));

        return grid;
    }

    public void actualizarGraficas() {
        int ronda = sistema.getRondaActual();
        if (ronda == 0) return;

        //actualizacion del throughput, obtenemos lo que salio de la ultima estacion en la ronda
        int salidasRonda = sistema.getEstaciones().get(9).getPersonasMovidas();
        throughputSeries.getData().add(new XYChart.Data<>(String.valueOf(ronda), salidasRonda));

        // actualizamos activity
        activitySeries.getData().clear();
        ArrayList<Estacion> estaciones = sistema.getEstaciones();

        for (int i = 0; i < Control.NUM_ESTACIONES; i++) {
            Estacion e = estaciones.get(i);
            totalDadosPorEstacion[i] += e.getUltimoDado();
            totalMovidosPorEstacion[i] += e.getPersonasMovidas();

            double porcentajePromedio = 0;
            if (totalDadosPorEstacion[i] > 0) {
                porcentajePromedio = (totalMovidosPorEstacion[i] * 100.0) / totalDadosPorEstacion[i];
            }

            XYChart.Data<String, Number> data = new XYChart.Data<>("E" + (i + 1), porcentajePromedio);
            activitySeries.getData().add(data);
        }
        int personasEnSistema = sistema.getTotalEnSistema();
        wipSeries.getData().add(new XYChart.Data<>(String.valueOf(ronda), personasEnSistema));
    }

    public Label crearLabelStat(String titulo, String valor) {
        Label label = new Label(titulo + "\n" + valor);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        label.setTextFill(Color.web("#e0e0f0"));
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; -fx-padding: 8 16 8 16;");
        return label;
    }

    public Label sep() {
        Label s = new Label("│");
        s.setTextFill(Color.web("#404060"));
        return s;
    }

    public ScrollPane crearPanelEstaciones() {
        HBox fila = new HBox(10);
        fila.setPadding(new Insets(12, 16, 12, 16));
        fila.setAlignment(Pos.TOP_CENTER);

        ArrayList<Estacion> estaciones = sistema.getEstaciones();
        for (int i = 0; i < sistema.NUM_ESTACIONES; i++) {
            panelEstaciones[i] = crearTarjetaEstacion(i + 1);
            fila.getChildren().add(panelEstaciones[i]);
        }

        ScrollPane scroll = new ScrollPane(fila);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #1a1a2e;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    public VBox crearTarjetaEstacion(int numero) {
        VBox tarjeta = new VBox(6);
        tarjeta.setAlignment(Pos.TOP_CENTER);
        tarjeta.setPadding(new Insets(10));
        tarjeta.setPrefWidth(100);
        tarjeta.setMinWidth(100);
        tarjeta.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-border-color: #0f3460; -fx-border-radius: 10; -fx-border-width: 1.5;");

        int idx = numero - 1;

        Label lblNumero = new Label("Estación " + numero);
        lblNumero.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblNumero.setTextFill(Color.web("#e94560"));
        lblNumero.setAlignment(Pos.CENTER);

        labelDado[idx] = new Label("🎲 —");
        labelDado[idx].setFont(Font.font("Arial", FontWeight.BOLD, 16));
        labelDado[idx].setTextFill(Color.web("#f5f5f5"));
        labelDado[idx].setAlignment(Pos.CENTER);

        labelCola[idx] = new Label("Cola: 4");
        labelCola[idx].setFont(Font.font("Arial", 11));
        labelCola[idx].setTextFill(Color.web("#a0c4ff"));

        labelMovidas[idx] = new Label("Movidas: —");
        labelMovidas[idx].setFont(Font.font("Arial", 11));
        labelMovidas[idx].setTextFill(Color.web("#b9fbc0"));

        vistaPersonas[idx] = new FlowPane(4, 4);
        vistaPersonas[idx].setPrefWrapLength(85);
        vistaPersonas[idx].setAlignment(Pos.TOP_LEFT);
        vistaPersonas[idx].setPadding(new Insets(4));
        vistaPersonas[idx].setMinHeight(60);

        Separator separador = new Separator();
        separador.setStyle("-fx-background-color: #0f3460;");

        tarjeta.getChildren().addAll(lblNumero, labelDado[idx], separador, labelCola[idx], labelMovidas[idx], vistaPersonas[idx]);
        return tarjeta;
    }

    public HBox crearPanelBotones() {
        HBox panel = new HBox(16);
        panel.setPadding(new Insets(12, 20, 16, 20));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #16213e;");

        botonRoll = new Button("🎲  Roll");
        botonMove = new Button("▶  Move");
        botonReset = new Button("↺  Reset");

        estilizarBoton(botonRoll, "#e94560");
        estilizarBoton(botonMove, "#0f3460");
        estilizarBoton(botonReset, "#444466");

        botonMove.setDisable(true); //Habilitado luego de hacer un roll

        botonRoll.setOnAction(e -> onRoll());
        botonMove.setOnAction(e -> onMove());
        botonReset.setOnAction(e -> onReset());

        // Leyenda de colores
        HBox leyenda = new HBox(10);
        leyenda.setAlignment(Pos.CENTER);
        leyenda.getChildren().addAll(
                circulo("#4fc3f7"), new Label("Persona inicial") {{setTextFill(Color.web("#a0a0b0")); setFont(Font.font(11));}},
                circulo("#e94560"), new Label("Persona nueva") {{setTextFill(Color.web("#a0a0b0")); setFont(Font.font(11));}}
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        panel.getChildren().addAll(botonRoll, botonMove, spacer, leyenda, botonReset);
        return panel;
    }

    public void estilizarBoton(Button btn, String color) {
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 28 10 28; -fx-cursor: hand;", color));
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
    }

    public Circle circulo(String color) {
        Circle c = new Circle(6);
        c.setFill(Color.web(color));
        return c;
    }

    public void onRoll() {
        if (sistema.isJuegoTerminado()) return;
        sistema.tirarDados();
        actualizarVista();
        botonRoll.setDisable(true);
        botonMove.setDisable(false);
    }

    public void onMove() {
        sistema.moverPersonas();
        actualizarVista();
        actualizarGraficas();
        botonRoll.setDisable(false);
        botonMove.setDisable(true);

        if (sistema.isJuegoTerminado()) {
            botonRoll.setDisable(true);
            mostrarResultadoFinal();
        }
    }

    public void onReset() {
        sistema = new Control();
        throughputSeries.getData().clear();
        activitySeries.getData().clear();
        actividadAcumulada = new double[Control.NUM_ESTACIONES];
        totalDadosPorEstacion = new int[Control.NUM_ESTACIONES];
        totalMovidosPorEstacion = new int[Control.NUM_ESTACIONES];
        botonRoll.setDisable(false);
        botonMove.setDisable(true);
        actualizarVista();
    }

    public void actualizarVista() {
        ArrayList<Estacion> estaciones = sistema.getEstaciones();

        for (int i = 0; i < Control.NUM_ESTACIONES; i++) {
            Estacion e = estaciones.get(i);

            // Dado
            if (sistema.isDadosTirados()) {
                labelDado[i].setText("🎲 " + e.getUltimoDado());
                labelDado[i].setTextFill(Color.web("#ffd166"));
            } else {
                labelDado[i].setText("🎲 —");
                labelDado[i].setTextFill(Color.web("#f5f5f5"));
            }

            // Cola
            labelCola[i].setText("Cola: " + e.getPersonasEnCola());

            // Movidas
            if (sistema.getRondaActual() > 0) {
                labelMovidas[i].setText("Movidas: " + e.getPersonasMovidas());
            }

            // Vista de personas, o sea los circulos
            actualizarCirculos(i, e.getPersonasEnCola());
        }

        // Estadisticas generales
        actualizarStats();
    }

    public void actualizarCirculos(int idx, int cantidad) {
        vistaPersonas[idx].getChildren().clear();
        int max = Math.min(cantidad, 30); //cantidad maxima de circulos visibles limitada a 30
        for (int i = 0; i < max; i++) {
            Circle c = new Circle(5);
            // Las primeras cuatro personas en cada estacion son azules, las nuevas son de color rojo
            c.setFill(Color.web(i < 4 ? "#4fc3f7" : "#e94560"));
            vistaPersonas[idx].getChildren().add(c);
        }
        if (cantidad > 30) {
            Label mas = new Label("+" + (cantidad - 30));
            mas.setFont(Font.font("Arial", 9));
            mas.setTextFill(Color.web("#a0a0b0"));
            vistaPersonas[idx].getChildren().add(mas);
        }
    }

    public void actualizarStats() {
        labelRonda.setText("Ronda\n" + sistema.getRondaActual() + " / " + Control.MAX_RONDAS);
        labelEnSistema.setText("En sistema\n" + sistema.getTotalEnSistema());
        labelSalidas.setText("Salidas\n" + sistema.getPersonasSalidas());
        double prom = sistema.getTiempoPromedioPaso();
        labelPromedio.setText("Tiempo promedio\n" + (prom > 0 ? String.format("%.1f rondas", prom) : "—"));
    }

    public void mostrarResultadoFinal() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Juego terminado");
        alert.setHeaderText("Resultados finales — 20 rondas completadas");
        alert.setContentText(String.format(
                "Personas que salieron del sistema: %d\n" +
                        "Personas que siguen en el sistema: %d\n" +
                        "Tiempo promedio de paso: %.1f rondas\n\n",
                sistema.getPersonasSalidas(),
                sistema.getTotalEnSistema(),
                sistema.getTiempoPromedioPaso()
        ));
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
