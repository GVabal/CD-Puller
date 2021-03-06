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
    //String baseLocation = "/Users/zmru/Desktop";
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

    public void execute() throws IOException {
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

        if (records.get(0).get(1).equals("ssn") && records.get(0).get(2).equals("date")) {
            records.remove(0); // take out [0nr 1ssn 2date 3robot 4comment]
        }

        for (List<String> record : records) {
            lookupLocation = baseLocation + SLASH + workLocation + SLASH + record.get(3) + " " + day + SLASH + record.get(1) + " " + record.get(2);

            if (record.get(4).matches(".*Documents are saved in folder \\(1\\)\\.")) {
                lookupLocation += "(1)";
            }

            File lookupLocationDir = new File(lookupLocation);
            File destinationDir = new File(destination + SLASH + day + SLASH + record.get(0));

            if (!lookupLocationDir.exists()) {
                consoleText += "! " + record.get(0) + " " + record.get(3) + " " + record.get(1) + " " + record.get(2) + " not found.\n";
            } else {
                File[] folderContents = new File(lookupLocation).listFiles();


                if (folderContents != null) {
                    List<String> folderContentsArray = new ArrayList<>();

                    for (File file : folderContents) {
                        String fileName = file.toString().split("\\\\")[12]; //magic number

                        if (!fileName.startsWith("."))
                            folderContentsArray.add(fileName);
                    }

                    for (String file : folderContentsArray) {
                        if (file.matches("Adressbrev Engar[.]pdf")) {
                            FileUtils.copyFile(new File(lookupLocation + SLASH + file), new File(destinationDir + "-1.pdf"));
                            folderContentsArray.remove(file);
                            break;
                        }
                    }
                    if (!folderContentsArray.isEmpty()) {
                        for (String file : folderContentsArray) {
                            if (file.matches("\\d\\d\\d\\d\\d\\d\\d\\d-\\d\\d\\d\\d[.]pdf")) {
                                FileUtils.copyFile(new File(lookupLocation + SLASH + file), new File(destinationDir + "-2.pdf"));
                                folderContentsArray.remove(file);
                                break;
                            }
                        }
                    }
                    if (!folderContentsArray.isEmpty()) {
                        int orderNumber = folderContentsArray.size() + 2;
                        for (String file : folderContentsArray) {
                            FileUtils.copyFile(new File(lookupLocation + SLASH + file), new File(destinationDir + "-" + orderNumber + ".pdf"));
                            orderNumber--;
                        }
                    }
                }

                consoleText += record.get(0) + " " + record.get(3) + " " + record.get(1) + " " + record.get(2) + " found.\n";
            }
            consoleField.setText(consoleText);
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
