package gandalf;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Gandalf gandalf;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/daUser.jpg"));
    private Image gandalfImage = new Image(this.getClass().getResourceAsStream("/images/daGandalf.jpg"));

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());

        dialogContainer.getChildren().add(DialogBox.getGandalfDialog("Through fire and shadow, I'm Gandalf. "
                                                                        + "What can i do for you?", gandalfImage));
    }

    public void setGandalf(Gandalf g) {
        gandalf = g;
    }

    public Button getSendButton() {
        return sendButton;
    }

    public TextField getUserInput() {
        return userInput;
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    public void handleUserInput() {
        String input = userInput.getText();
        String response = gandalf.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getGandalfDialog(response, gandalfImage)
        );
        userInput.clear();
    }
}
