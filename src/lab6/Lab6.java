/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab6;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Ada
 */
public class Lab6 extends Application implements Initializable{
    @FXML
    private Label czas;
    @FXML
    private Label sciezka;
    @FXML
    private TableView<ImageProcessingJob> tabela;
    @FXML
    TableColumn<ImageProcessingJob, String> kolumnaNazwa;
    @FXML 
    TableColumn<ImageProcessingJob, Double> kolumnaPostep;
    @FXML 
    TableColumn<ImageProcessingJob, String> kolumnaStatus;
    @FXML
    TextField iloscWatkow;
    
    private File folder;
    private ObservableList<ImageProcessingJob> pliki=FXCollections.observableArrayList();
    
    
    @Override
    public void start(Stage stage) throws IOException {
       stage.setTitle("Przetwarzanie obrazow");
        Parent wzor = FXMLLoader.load(getClass().getResource("lab6.fxml"));
        Scene scene = new Scene(wzor);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    public void dodajSciezke(){
         DirectoryChooser nowy =new DirectoryChooser();
         folder=nowy.showDialog(null);
         sciezka.setText(folder.getPath());
    }
    
    private void backgroundJobS(){ 
        Stream<ImageProcessingJob> strumien;
        strumien=tabela.getItems().stream();
        zmien(strumien);  
    }
    
      private void backgroundJobR(){ 
        Stream<ImageProcessingJob> strumien;
        strumien=tabela.getItems().parallelStream();
        zmien(strumien);    
    }
    
    void zmien (Stream<ImageProcessingJob> strumien){
        long start = System.currentTimeMillis(); //zwraca aktualny czas
         strumien.forEach(new Consumer<ImageProcessingJob>() {
            @Override
            public void accept(ImageProcessingJob obiekt) {
                
                if ("Oczekuje".equals(obiekt.getStatus().get())) {
                    convertToGrayscale(obiekt.getPlik(), folder, obiekt.getPostep(),obiekt.getStatus());
                }
            }
        });
        long end = System.currentTimeMillis(); //czas po zakończeniu operacji
        long duration = end-start; //czas przetwarzania
        Platform.runLater(() ->czas.setText("Czas: " +String.valueOf(duration) + " ms"));
    }
    
    public void zamienSekwencyjnie(){
        new Thread(this::backgroundJobS).start();
    }
    
     public void zamienRownolegle(){
         String napis=iloscWatkow.getText();
        ForkJoinPool pool = new ForkJoinPool(Integer.parseInt(napis)); //pożądana liczba wątków
        pool.submit(this::backgroundJobR);
    }
    
    public void wybierzPliki(){
        pliki.clear();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter =new FileChooser.ExtensionFilter("JPG images", "*.jpg");
        fileChooser.getExtensionFilters().add(filter);
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        for(File temp:selectedFiles){
            pliki.add(new ImageProcessingJob(temp, 0.0, "Oczekuje"));
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        kolumnaNazwa.setCellValueFactory( //nazwa pliku
            p -> new SimpleStringProperty(p.getValue().getPlik().getName()));
        kolumnaStatus.setCellValueFactory( //status przetwarzania
            p -> p.getValue().getStatus());
        kolumnaPostep.setCellValueFactory( //postęp przetwarzania
            p -> p.getValue().getPostep().asObject());
        kolumnaPostep.setCellFactory( //wykorzystanie paska postępu
        ProgressBarTableCell.<ImageProcessingJob>forTableColumn()); 
        tabela.setItems(pliki);
    }

   private void convertToGrayscale(File originalFile, File outputDir, DoubleProperty progressProp, SimpleStringProperty stan ) {
     try {
        BufferedImage original = ImageIO.read(originalFile);
        BufferedImage grayscale = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {
                int red = new Color(original.getRGB(i, j)).getRed();
                int green = new Color(original.getRGB(i, j)).getGreen();
                int blue = new Color(original.getRGB(i, j)).getBlue();
                int luminosity = (int) (0.21*red + 0.71*green + 0.07*blue);
                int newPixel = new Color(luminosity, luminosity, luminosity).getRGB();
                grayscale.setRGB(i, j, newPixel);
            }
            double progress = (1.0 + i) / original.getWidth();Platform.runLater(() -> progressProp.set(progress));
            if(progress>0.0&&progress<1){
                Platform.runLater(() -> stan.set("Trwa przetwarzanie"));
            }
            else{
                Platform.runLater(() -> stan.set("Zakonczono"));
            }
        }
        Path outputPath =Paths.get(outputDir.getAbsolutePath(), originalFile.getName());
        ImageIO.write(grayscale, "jpg", outputPath.toFile());
     } catch (IOException ex) {
        throw new RuntimeException(ex);
     }
    } 
    
    
    
}
