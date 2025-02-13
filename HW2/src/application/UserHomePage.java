package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ListCell;
import javafx.util.Callback;

import java.util.Optional;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	
	private ObservableList<Question> questions = FXCollections.observableArrayList(
	    new Question("What time is class?", "User A"),
	    new Question("When is the homework assignment due?", "User B"),
	    new Question("What computer labs are available to CS students?", "User C"));

    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, User!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    ListView<Question> questionListView = new ListView<>(questions);
	    questionListView.setPrefSize(300, 200);
	    questionListView.setCellFactory(new Callback<ListView<Question>, ListCell<Question>>() {
	    	@Override
	    	public ListCell<Question> call(ListView<Question> param) {
	    		return new ListCell<Question>() {
	    			@Override
	    			protected void updateItem(Question item, boolean empty) {
	    				super.updateItem(item, empty);
	    				if (empty || item == null) {
	    					setText(null);
	    				}
	    				else {
	    					StringBuilder displayText = new StringBuilder(item.getBody() + " - Asked by " + item.getAskedBy());
	    					if (!item.getAnswers().isEmpty()) {
	    						item.getAnswers().forEach(answer -> displayText.append("\nAnswer: ").append(answer.getText()));
	    					}
	    					setText(displayText.toString());
	    				}
	    			}
	    		};
	    	}
	    });
	    
	    // Button: Ask question
	    Button addButton = new Button("Ask a Question");
	    addButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
	    addButton.setOnAction(e -> promptForNewQuestion(questionListView));

	    // Button: Delete highlighted question
	    Button deleteButton = new Button("Delete Question");
	    deleteButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
	    deleteButton.setOnAction(e -> {
	    	// Get index of question and check if current user owns the question
	        int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
	        if (selectedIndex != -1) {
	            Question selectedQuestion = questions.get(selectedIndex);
	            if (selectedQuestion.getAskedBy().equals("Current User")) {
	            	questions.remove(selectedIndex);
	            }
	            else {
	            	showErrorMessage("Error", "You can only delete questions that you have asked.");
	            }
	        }
	    });
	    
	    
	    Button answerButton = new Button("Answer Question");
        answerButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        answerButton.setOnAction(e -> {
            int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                promptForAnswer(questionListView, selectedIndex);
            }
        });
	    
	    layout.getChildren().addAll(userLabel, questionListView, addButton, deleteButton, answerButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
    	
    }
    
    // popup window prompting user to enter question text
    private void promptForNewQuestion(ListView<Question> questionListView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Question");
        dialog.setHeaderText("Add a new question");
        dialog.setContentText("Please enter your question:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(question -> {
            if (question.trim().isEmpty()) {
                showErrorMessage("Error", "The question cannot be empty. Please enter a valid question.");
            } else {
                questions.add(new Question(question, "Current User"));  // Assuming the current user is asking.
            }
        });
    }
    
    
    private void promptForAnswer(ListView<Question> questionListView, int selectedIndex) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Answer Question");
        dialog.setHeaderText("Provide an answer");
        dialog.setContentText("Enter your answer:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(answer -> {
            if (answer.trim().isEmpty()) {
                showErrorMessage("Error", "The answer cannot be empty. Please enter a valid answer.");
            } else {
                Question question = questionListView.getItems().get(selectedIndex);
                question.addAnswer(new Answer(answer, "Current User"));  // Add the answer to the selected question
                questionListView.refresh();  // Refresh the list to show updated data
            }
        });
    }
     
    private void showErrorMessage(String title, String content) {
    	Alert alert = new Alert(AlertType.ERROR);
    	alert.setTitle(title);
    	alert.setHeaderText(null);
    	alert.setContentText(content);
    	alert.showAndWait();
    }
    
}