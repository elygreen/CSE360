package application;

import databasePart1.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;

/**
 * StaffSensitiveContentManager provides functionality for staff members to manage
 * sensitive content in the system. This includes flagging questions and answers that
 * contain potentially sensitive information.
 * <p>
 * The class offers a UI for viewing all content and toggling sensitivity flags.
 * </p>
 */
public class StaffSensitiveContentManager {
    
    /** The database helper for database operations */
    private DatabaseHelper databaseHelper;
    
    /** The current user (staff member) */
    private User currentUser;
    
    /** The primary stage for navigation */
    private Stage primaryStage;
    
    /** Observable list of questions loaded from the database */
    private ObservableList<Question> questions;
    
    /** Map to store question objects and their database IDs */
    private Map<Question, Integer> questionIDs;
    
    /**
     * Constructs a new StaffSensitiveContentManager with the specified parameters.
     * 
     * @param databaseHelper The database helper for database operations
     * @param currentUser The current staff user
     * @param primaryStage The primary stage for navigation
     */
    public StaffSensitiveContentManager(DatabaseHelper databaseHelper, User currentUser, Stage primaryStage) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
        this.primaryStage = primaryStage;
        
        // Ensure the database has the required columns for sensitive content
        databaseHelper.ensureSensitiveColumnsExist();
    }
    
    /**
     * Shows the Sensitive Content Manager window.
     */
    public void show() {
        Stage managerStage = new Stage();
        managerStage.initModality(Modality.APPLICATION_MODAL);
        managerStage.setTitle("Sensitive Content Manager");
        managerStage.setMinWidth(800);
        managerStage.setMinHeight(600);
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        Label headerLabel = new Label("Sensitive Content Manager");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox headerBox = new HBox(headerLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        loadQuestions();

        ListView<Question> questionListView = new ListView<>(questions);
        questionListView.setCellFactory(lv -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    VBox container = new VBox(8);
                    container.setPadding(new Insets(5));
                    HBox questionBox = new HBox(10);

                    Label questionLabel = new Label(item.getBody());
                    questionLabel.setStyle("-fx-font-weight: bold;");
                    questionLabel.setWrapText(true);
                    questionLabel.setMaxWidth(450);
                    

                    VBox metaBox = new VBox(3);
                    Label authorLabel = new Label("Asked by: " + item.getAskedBy());
                    authorLabel.setStyle("-fx-font-style: italic;");
                    

                    Integer questionId = questionIDs.get(item);
                    boolean isSensitive = questionId != null && databaseHelper.isQuestionSensitive(questionId);
                    
                    Label sensitiveLabel = new Label("SENSITIVE");
                    sensitiveLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    sensitiveLabel.setVisible(isSensitive);
                    
                    metaBox.getChildren().addAll(authorLabel, sensitiveLabel);
                    
                    // Toggle button for question sensitivity
                    CheckBox sensitiveToggle = new CheckBox("Mark as Sensitive");
                    sensitiveToggle.setSelected(isSensitive);
                    
                    sensitiveToggle.setOnAction(e -> {
                        if (questionId != null) {
                            boolean success = databaseHelper.setQuestionSensitivity(
                                    questionId, sensitiveToggle.isSelected());
                            
                            if (success) {
                                sensitiveLabel.setVisible(sensitiveToggle.isSelected());
                            }
                            else {
                                showErrorMessage("Database Error", "Failed to update sensitivity flag.");
                                sensitiveToggle.setSelected(!sensitiveToggle.isSelected());
                            }
                        }
                    });
                    
                    questionBox.getChildren().addAll(questionLabel, metaBox, sensitiveToggle);
                    container.getChildren().add(questionBox);
                    
                    // Add answers
                    if (!item.getAnswers().isEmpty()) {
                        Label answersLabel = new Label("Answers:");
                        answersLabel.setStyle("-fx-font-weight: bold;");
                        container.getChildren().add(answersLabel);
                        
                        for (Answer answer : item.getAnswers()) {
                            HBox answerBox = new HBox(10);
                            answerBox.setPadding(new Insets(0, 0, 0, 20));
                            

                            Label answerLabel = new Label(answer.getText());
                            answerLabel.setWrapText(true);
                            answerLabel.setMaxWidth(400);

                            VBox answerMetaBox = new VBox(3);
                            Label answerAuthorLabel = new Label("By: " + answer.getAnsweredBy());
                            answerAuthorLabel.setStyle("-fx-font-style: italic;");
                            
                            // Sensitive flag
                            boolean isAnswerSensitive = databaseHelper.isAnswerSensitive(answer.getId());
                            
                            Label answerSensitiveLabel = new Label("SENSITIVE");
                            answerSensitiveLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            answerSensitiveLabel.setVisible(isAnswerSensitive);
                            
                            answerMetaBox.getChildren().addAll(answerAuthorLabel, answerSensitiveLabel);

                            CheckBox answerSensitiveToggle = new CheckBox("Mark as Sensitive");
                            answerSensitiveToggle.setSelected(isAnswerSensitive);
                            
                            answerSensitiveToggle.setOnAction(e -> {
                                boolean success = databaseHelper.setAnswerSensitivity(
                                        answer.getId(), answerSensitiveToggle.isSelected());
                                
                                if (success) {
                                    answerSensitiveLabel.setVisible(answerSensitiveToggle.isSelected());
                                }
                                else {
                                    showErrorMessage("Database Error", "Failed to update sensitivity flag.");
                                    answerSensitiveToggle.setSelected(!answerSensitiveToggle.isSelected());
                                }
                            });
                            
                            answerBox.getChildren().addAll(answerLabel, answerMetaBox, answerSensitiveToggle);
                            container.getChildren().add(answerBox);
                        }
                    }
                    Separator separator = new Separator();
                    container.getChildren().add(separator);
                    setGraphic(container);
                }
            }
        });
        
        // Buttons
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            loadQuestions();
            questionListView.setItems(questions);
        });
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> managerStage.close());
        
        HBox buttonBox = new HBox(15, refreshButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        

        mainLayout.setTop(headerBox);
        mainLayout.setCenter(questionListView);
        mainLayout.setBottom(buttonBox);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        managerStage.setScene(scene);
        managerStage.show();
    }
    
    /**
     * Loads questions from the database.
     */
    private void loadQuestions() {
        Map<Question, Integer> loadedQuestionIDs = databaseHelper.loadAllQuestionsWithIDs();
        
        questions = FXCollections.observableArrayList();
        questionIDs = loadedQuestionIDs;
        
        for (Map.Entry<Question, Integer> entry : loadedQuestionIDs.entrySet()) {
            questions.add(entry.getKey());
        }
    }
    
    /**
     * Displays an error message dialog with the specified title and content.
     * 
     * @param title The title of the error dialog
     * @param content The content of the error dialog
     */
    private void showErrorMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}