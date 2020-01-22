import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {

    private ObservableList<String> consoleItems = FXCollections.observableArrayList();
    @FXML
    private ListView<String> consoleField = new ListView<>(consoleItems);
    @FXML
    private TextField workFolderField;
    @FXML
    private TextField destinationField;
    @FXML
    private TextField csvFileField;

    //put this under properties
    String baseLocation = "M:\\Gem enheter\\GSS\\Automation\\Business\\GSS\\Commitments & Deposits\\GSS - Dialogen CD (v3.0)\\Manual Handling";
    String SLASH = "\\";
    String DELIMITER = ";";

    public void selectFile() {

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);

        csvFileField.setText(file.toString());
    }

    public void selectDestination() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        String directory = directoryChooser.showDialog(null).toString();

        destinationField.setText(directory);
    }

    public void execute() {
        consoleItems.add("Process started!");
        consoleField.setItems(consoleItems);

        String workLocation = workFolderField.getText();
        String destination = destinationField.getText();
        String day = workFolderField.getText(0, 10);
        String lookupLocation = "";

        List<List<String>> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFileField.getText()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        records.remove(0); // take out [0nr 1ssn 2date 3robot 4comment]

        for (List<String> record : records) {
            lookupLocation = baseLocation + SLASH + workLocation + SLASH + record.get(3) + " " + day + SLASH + record.get(1) + " " + record.get(2);

            if (record.get(4).matches(".*Documents are saved in folder \\(1\\)\\.")) {
                lookupLocation += "(1)";
            }

            try {
                File lookupLocationDir = new File(lookupLocation);
                File destinationDir = new File(destination + SLASH + record.get(0) + " " + record.get(1) + " " + record.get(2));
                FileUtils.copyDirectory(lookupLocationDir, destinationDir);
                consoleItems.add(record.get(0) + " " + record.get(1) + " " + record.get(2) + " found.");
                consoleField.setItems(consoleItems);
            } catch (IOException e) {
                e.printStackTrace();
                consoleItems.add("! " + record.get(0) + " " + record.get(1) + " " + record.get(2) + " not found.");
                consoleField.setItems(consoleItems);
            }
        }
        consoleItems.add("Done!");
        consoleField.setItems(consoleItems);
    }

    public void generateEmptyCsv() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        String directory = directoryChooser.showDialog(null).toString();
        Path path = Paths.get(directory + SLASH + "File.csv");

         try {
             Files.createFile(path);

             FileWriter writer = new FileWriter(path.toString(), true);
             writer.write("nr;ssn;date;robot;comment");
             writer.close();

             consoleItems.add("CSV generated!");
             consoleField.setItems(consoleItems);
         } catch (IOException e) {
             e.printStackTrace();
             consoleItems.add("CSV did not generate!");
             consoleField.setItems(consoleItems);
         }
    }

    public void openProperties() {
        consoleItems.add("Does not work yet.");
        consoleField.setItems(consoleItems);
    }
}
