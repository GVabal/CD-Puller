import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
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

    @FXML
    private TextArea consoleField;
    @FXML
    private TextField workFolderField;
    @FXML
    private TextField destinationField;
    @FXML
    private TextField csvFileField;

    String consoleText = "";
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
        consoleText += "Process started...\n";
        consoleField.setText(consoleText);

        String workLocation = workFolderField.getText();
        String destination = destinationField.getText();

        if (workLocation.isEmpty() || destination.isEmpty()) {
            consoleText += "Stopped. Fill in missing fields\n";
            consoleField.setText(consoleText);
            return;
        }

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
            consoleText += "Stopped. No/Bad CSV file\n";
            consoleField.setText(consoleText);
            return;
        }
        records.remove(0); // take out [0nr 1ssn 2date 3robot 4comment]

        for (List<String> record : records) {
            lookupLocation = baseLocation + SLASH + workLocation + SLASH + record.get(3) + " " + day + SLASH + record.get(1) + " " + record.get(2);

            if (record.get(4).matches(".*Documents are saved in folder \\(1\\)\\.")) {
                lookupLocation += "(1)";
            }

            try {
                File lookupLocationDir = new File(lookupLocation);
                File destinationDir = new File(destination + SLASH + day);
                int orderNumber = 3;

                //check if lookup location exists
                //check contents in location and store to list
                //look for address page with regex
                //move to destination folder with new name $nr-1
                //look for eng page with regex
                // move to destination with new name $nr-2
                //go through the rest of list and move them to destination folder with new name $nr-$orderNumber++

                consoleText += record.get(0) + " " + record.get(3) + " " + record.get(1) + " " + record.get(2) + " found.\n";
                consoleField.setText(consoleText);
            } catch (IOException e) {
                consoleText += "! " + record.get(0) + " " + record.get(3) + " " + record.get(1) + " " + record.get(2) + " not found.\n";
                consoleField.setText(consoleText);
            }
        }
        consoleText += "Done!\n";
        consoleField.setText(consoleText);
    }

    public void generateEmptyCsv() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        String directory = directoryChooser.showDialog(null).toString();
        Path path = Paths.get(directory + SLASH + "List.csv");

         try {
             Files.createFile(path);

             FileWriter writer = new FileWriter(path.toString(), true);
             writer.write("nr;ssn;date;robot;comment");
             writer.close();

             consoleText += "CSV created at " + directory + SLASH + "List.csv\n";
             consoleField.setText(consoleText);
         } catch (IOException e) {
             consoleText += "CSV was not created\n";
             consoleField.setText(consoleText);

         }
    }

    public void openProperties() {
        consoleText += "Does not work yet.\n";
        consoleField.setText(consoleText);

    }
}
