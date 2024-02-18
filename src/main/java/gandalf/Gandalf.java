package gandalf;

import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Main class for the Gandalf chatbot
 */
public class Gandalf extends Application {
    private Storage storage;
    private TaskList tasks;
    private Ui ui;

    private ScrollPane scrollPane;
    private VBox dialogContainer;
    private TextField userInput;
    private Button sendButton;
    private Scene scene;

    private Image user = new Image(this.getClass().getResourceAsStream("/images/User.jpg"));
    private Image gandalf = new Image(this.getClass().getResourceAsStream("/images/Gandalf.jpg"));

    public Gandalf() {

    }

    /**
     * Takes in two paths as it uses two files for its store/load feature. One file is for loading any existing lists,
     * and another file is meant to be readable in a .txt file
     * @param filePathMeta
     * @param filePathRead
     */

    public Gandalf(String filePathMeta, String filePathRead) {
        ui = new Ui();
        storage = new Storage(filePathMeta, filePathRead);
        try {
            tasks = new TaskList(storage.load());
        } catch (GandalfException e) {
            //file does not exist, create new list
            tasks = new TaskList();
        }
    }
    public void find(String keyword) {
        ArrayList<Task> filteredList = new ArrayList<>();
        int numOfFiltered = 0;
        for(int i = 0; i < tasks.getList().size(); i++) {
            Task action = tasks.getList().get(i);
            String nameOfTask = action.getNameOfTask();
            if(nameOfTask.contains(keyword)) {
                filteredList.add(numOfFiltered, action);
                numOfFiltered++;
            }
        }
        for(int i = 0; i < filteredList.size(); i++) {
            Task action = filteredList.get(i);
            System.out.println((i + 1) + ". " + action);
        }
    }
    /**
     * Function to run the chatbot, uses a while-loop to constantly allow the chatbot to receive new inputs
     * Also processes inputs to do various things depending on the command
     */
    public void run() {
        ui.welcome();
        Scanner scanner = new Scanner(System.in);
        while(true) {
            String input = scanner.nextLine();
            if(input.length() == 0) { //ignore accidental new lines from user
                continue;
            }
            Parser parser = new Parser(input);
            StringBuilder[] parsedInput = parser.interpret(); //parsedInput = {taskType, taskName, date1, date2}
            if(parsedInput[0].toString().trim().equals("bye")) {
                scanner.close();
                ui.bye();
                break;
            }
            else if(parsedInput[0].toString().trim().equals("list")) {
                for(int i = 0; i < tasks.getList().size(); i++){
                    Task action = tasks.getList().get(i);
                    System.out.println((i + 1) + ". " + action);
                }
                System.out.println("Total number of tasks so far: " + (tasks.getList().size()));
            }
            else if(parsedInput[0].toString().trim().equals("delete")){
                tasks.delete(parsedInput[1].toString().trim());
                storage.store(tasks.getList());
            }
            else if(parsedInput[0].toString().trim().equals("mark")) {
                int taskNumber = Integer.parseInt(parsedInput[1].toString());
                ui.marked();
                tasks.mark(taskNumber);
                storage.store(tasks.getList());
            }
            else if(parsedInput[0].toString().trim().equals("unmark")) {
                int taskNumber = Integer.parseInt(parsedInput[1].toString());
                ui.unmarked();
                tasks.unmark(taskNumber);
                storage.store(tasks.getList());
            } else if(parsedInput[0].toString().trim().equals("find")) {
                String keyword = parsedInput[1].toString().trim();
                find(keyword);
            }
            else {
                try {
                    tasks.add(parsedInput[0].toString().trim(), parsedInput[1].toString().trim(), parsedInput[2].toString().trim(), parsedInput[3].toString().trim());
                    storage.store(tasks.getList());
                    System.out.println("Total number of tasks so far: " + (tasks.getList().size()));
                }
                catch(GandalfException e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        new Gandalf("docs/gandalfMeta.txt", "docs/gandalfRead.txt").run();
    }

    @Override
    public void start(Stage stage) {
        //Step 1. Setting up required components

        //The container for the content of the chat to scroll.
        scrollPane = new ScrollPane();
        dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        userInput = new TextField();
        sendButton = new Button("Send");

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        scene = new Scene(mainLayout);

        stage.setScene(scene);
        stage.show();

        //Step 2. Formatting the window to look as expected
        stage.setTitle("Gandalf");
        stage.setResizable(false);
        stage.setMinHeight(600.0);
        stage.setMinWidth(400.0);

        mainLayout.setPrefSize(400.0, 600.0);

        scrollPane.setPrefSize(385, 535);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        //You will need to import `javafx.scene.layout.Region` for this.
        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialogContainer.setStyle("-fx-background-color: lightgray;");

        userInput.setPrefWidth(325.0);

        sendButton.setPrefWidth(55.0);

        AnchorPane.setTopAnchor(scrollPane, 1.0);

        AnchorPane.setBottomAnchor(sendButton, 1.0);
        AnchorPane.setRightAnchor(sendButton, 1.0);

        AnchorPane.setLeftAnchor(userInput , 1.0);
        AnchorPane.setBottomAnchor(userInput, 1.0);

        //Step 3. Add functionality to handle user input.
        sendButton.setOnMouseClicked((event) -> {
            dialogContainer.getChildren().add(getDialogLabel(userInput.getText()));
            userInput.clear();
        });

        userInput.setOnAction((event) -> {
            dialogContainer.getChildren().add(getDialogLabel(userInput.getText()));
            userInput.clear();
        });

        //Scroll down to the end every time dialogContainer's height changes.
        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));

        //Part 3. Add functionality to handle user input.
        sendButton.setOnMouseClicked((event) -> {
            handleUserInput();
        });

        userInput.setOnAction((event) -> {
            handleUserInput();
        });

    }
    /**
     * Iteration 1:
     * Creates a label with the specified text and adds it to the dialog container.
     * @param text String containing text to add
     * @return a label with the specified text that has word wrap enabled.
     */
    private Label getDialogLabel(String text) {
        // You will need to import `javafx.scene.control.Label`.
        Label textToAdd = new Label(text);
        textToAdd.setWrapText(true);

        return textToAdd;
    }

    /**
     * Iteration 2:
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    private void handleUserInput() {
        Label userText = new Label(userInput.getText());
        Label dukeText = new Label(getResponse(userInput.getText()));
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText, new ImageView(user)),
                DialogBox.getGandalfDialog(dukeText, new ImageView(gandalf))
        );
        userInput.clear();
    }

    /**
     * You should have your own function to generate a response to user input.
     * Replace this stub with your completed method.
     */
    private String getResponse(String input) {
        return "Gandalf heard: " + input;
    }
}
