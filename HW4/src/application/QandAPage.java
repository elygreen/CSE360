package application;

// question and answer loading / storing
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelperReviews;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.geometry.Insets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;

import application.QuestionValidator.ValidationResult;

/**
 * This page handles all Question and Answer functionality, including displaying,
 * creating, updating, and voting on questions and answers.
 * <p>
 * The class manages interactions with the database for storing and retrieving
 * questions, answers, votes, and reviews.
 * </p>
 */
public class QandAPage {
    
    /** Observable list of questions displayed in the UI */
    private ObservableList<Question> questions;
    
    /** Database helper for question and answer operations */
    private DatabaseHelper databaseHelper;
    
    /** Database helper specifically for review operations */
    private DatabaseHelperReviews databaseHelperReviews;
    
    /** Map to store question objects and their corresponding database IDs */
    private Map<Question, Integer> questionIDs;
    
    /** Observable list of reviews */
    private ObservableList<Review> reviews;
    
    /** Map to store review objects and their corresponding database IDs */
    private Map<Review, Integer> reviewIDs;
    
    /** Currently logged in user */
    private User currentUser;
    
    /** Window for displaying and managing reviews */
    private ReviewWindow reviewWindow;
    
    /** Primary stage reference for navigation */
    private Stage primaryStage;
    
    /**
     * Constructs a new QandAPage and initializes database connections.
     * Connects to the database and initializes the necessary database helpers.
     */
    public QandAPage() {
        databaseHelper = new DatabaseHelper();
        questionIDs = new HashMap<>();
        reviewIDs = new HashMap<>();
        try {
            databaseHelper.connectToDatabase();
            // Initialize DatabaseHelperReviews AFTER connecting to the database
            databaseHelperReviews = new DatabaseHelperReviews(databaseHelper);
        } catch (SQLException e) {
            showErrorMessage("connectToDatabase error", "Failed to connect to database");
        }
    }
    
    /**
     * Sets the current user of the application.
     * 
     * @param user The user to set as the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Creates and returns the main scene for the Q and A page.
     * Initializes all UI components, loads questions and reviews from the database,
     * and sets up event handlers for buttons and other interactive elements.
     * 
     * @param primaryStage The primary stage to display the scene on
     * @return A configured Scene object ready to be displayed
     */
    public Scene createScene(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // LOAD QUESTIONS
        questions = loadQuestionsFromDatabase();
        
        // Main layout
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // LOAD REVIEWS
        reviews = loadReviewsFromDatabase();
        
        // Initialize the ReviewWindow
        reviewWindow = new ReviewWindow(reviews, reviewIDs, databaseHelper, currentUser);
        reviewWindow.setPrimaryStage(primaryStage);
        
        ListView<Question> questionListView = new ListView<>(questions);
        questionListView.setPrefSize(600, 400);
        
        ListView<Review> reviewListView = new ListView<>(reviews);
        reviewListView.setPrefSize(600, 400);
        
        /**
         * Sets custom cells to control how questions and their answers are displayed.
         */
        questionListView.setCellFactory(new Callback<ListView<Question>, ListCell<Question>>() {
            @Override
            public ListCell<Question> call(ListView<Question> param) {
                // Return a custom ListCell that is used to display all question objects
                return new ListCell<Question>() {
                    @Override
                    protected void updateItem(Question item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        // if cell is empty / null - clear its graphic
                        if (empty || item == null) {
                            setGraphic(null);
                        }
                        
                        else {
                            // Main container
                            VBox container = new VBox(5);
                            
                            // Question box redone to have vertical voting on left of question with upvotes and downvotes stacked
                            // create upvote counter and button
                            Label upvoteCounter = new Label(String.valueOf(0));
                            Button upvoteButton = new Button("⬆");
                            HBox upvoteBox = new HBox(upvoteCounter, upvoteButton);
                            upvoteBox.setSpacing(5);
                            upvoteBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                            // create downvote counter and button
                            Label downvoteCounter = new Label(String.valueOf(0));
                            Button downvoteButton = new Button("⬇");
                            HBox downvoteBox = new HBox(downvoteCounter, downvoteButton);
                            downvoteBox.setSpacing(5);
                            downvoteBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            
                            // Get question ID from the map
                            Integer questionId = questionIDs.get(item);
                            if (questionId == null) {
                                questionId = -1;
                            }
                            
                            // Initialize vote counters with values from database
                            if (questionId > 0) {
                                int upvotes = databaseHelper.getQuestionUpvotes(questionId);
                                int downvotes = databaseHelper.getQuestionDownvotes(questionId);
                                upvoteCounter.setText(String.valueOf(upvotes));
                                downvoteCounter.setText(String.valueOf(downvotes));
                            }
                            
                            // Track voting state to not allow multiple voting
                            final boolean[] hasUpvoted = {false};
                            final boolean[] hasDownvoted = {false};
                            
                            // Load user's current vote status
                            if (questionId > 0) {
                                String userVoteType = databaseHelper.getUserQuestionVoteType(questionId, currentUser.getUserName());
                                hasUpvoted[0] = "upvote".equals(userVoteType);
                                hasDownvoted[0] = "downvote".equals(userVoteType);
                                
                                // Color buttons based on user's vote
                                if (hasUpvoted[0]) {
                                    upvoteButton.setStyle("-fx-background-color: #4CAF50;");
                                } else if (hasDownvoted[0]) {
                                    downvoteButton.setStyle("-fx-background-color: #F44336;");
                                }
                            }
                            
                            // Action for upvote button on question
                            final Integer finalQuestionId = questionId;
                            upvoteButton.setOnAction(e -> {
                                if (finalQuestionId > 0) {
                                    boolean success;
                                    
                                    // User is removing upvote from quesiton they had previously upvoted
                                    if (hasUpvoted[0]) {
                                        success = databaseHelper.recordQuestionVote(finalQuestionId, currentUser.getUserName(), "upvote");
                                        if (success) {
                                            hasUpvoted[0] = false;
                                            upvoteButton.setStyle("");
                                            
                                            // Update counters
                                            int newUpvotes = databaseHelper.getQuestionUpvotes(finalQuestionId);
                                            upvoteCounter.setText(String.valueOf(newUpvotes));
                                        }
                                    }
                                    // User is upvoting question
                                    else {
                                        success = databaseHelper.recordQuestionVote(finalQuestionId, currentUser.getUserName(), "upvote");
                                        if (success) {
                                            hasUpvoted[0] = true;
                                            upvoteButton.setStyle("-fx-background-color: #4CAF50;");
                                            
                                            // Remove downvote if one exists
                                            if (hasDownvoted[0]) {
                                                hasDownvoted[0] = false;
                                                downvoteButton.setStyle("");
                                            }
                                            
                                            // Update counters
                                            int newUpvotes = databaseHelper.getQuestionUpvotes(finalQuestionId);
                                            int newDownvotes = databaseHelper.getQuestionDownvotes(finalQuestionId);
                                            upvoteCounter.setText(String.valueOf(newUpvotes));
                                            downvoteCounter.setText(String.valueOf(newDownvotes));
                                        }
                                    }
                                    
                                    if (!success) {
                                        showErrorMessage("Database Error", "Failed to save the vote to the database. Errorcode PPAR1948");
                                    }
                                }
                            });
                            
                            // Action for downvote button
                            downvoteButton.setOnAction(e -> {
                                if (finalQuestionId > 0) {
                                    boolean success;
                                    
                                    // User is removing downvote from question
                                    if (hasDownvoted[0]) {
                                        success = databaseHelper.recordQuestionVote(finalQuestionId, currentUser.getUserName(), "downvote");
                                        if (success) {
                                            hasDownvoted[0] = false;
                                            downvoteButton.setStyle("");
                                            
                                            // Update counters
                                            int newDownvotes = databaseHelper.getQuestionDownvotes(finalQuestionId);
                                            downvoteCounter.setText(String.valueOf(newDownvotes));
                                        }
                                    }
                                    // User is downvoting question
                                    else {
                                        success = databaseHelper.recordQuestionVote(finalQuestionId, currentUser.getUserName(), "downvote");
                                        if (success) {
                                            hasDownvoted[0] = true;
                                            downvoteButton.setStyle("-fx-background-color: #F44336;");
                                            
                                            // Remove upvote if there was one
                                            if (hasUpvoted[0]) {
                                                hasUpvoted[0] = false;
                                                upvoteButton.setStyle("");
                                            }
                                            
                                            // Update counters
                                            int newUpvotes = databaseHelper.getQuestionUpvotes(finalQuestionId);
                                            int newDownvotes = databaseHelper.getQuestionDownvotes(finalQuestionId);
                                            upvoteCounter.setText(String.valueOf(newUpvotes));
                                            downvoteCounter.setText(String.valueOf(newDownvotes));
                                        }
                                    }
                                    
                                    if (!success) {
                                        showErrorMessage("Database Error", "Failed to save the vote to the database. Errorcode LHHK5054");
                                    }
                                }
                            });
                            
                            // stack upvote and downvote vertically
                            VBox votingControls = new VBox(upvoteBox, downvoteBox);
                            votingControls.setSpacing(5);
                            votingControls.setAlignment(javafx.geometry.Pos.CENTER);
                            votingControls.setPadding(new javafx.geometry.Insets(0, 10, 0, 0));
                            
                            // Question body
                            Label questionLabel = new Label(item.getBody());
                            questionLabel.setWrapText(true);
                            questionLabel.setMaxWidth(500);
                            questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                            
                            // Asked by label
                            Label askedByLabel = new Label("Asked by: " + item.getAskedBy());
                            askedByLabel.setStyle("-fx-font-style: italic; -fx-font-size: 12px;");
                            
                            VBox questionContent = new VBox(3);
                            
                            // Check if question is marked as sensitive
                            if (item.isSensitive()) {
                                Label sensitiveLabel = new Label("SENSITIVE");
                                sensitiveLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: #FFEEEE; -fx-padding: 2 5; -fx-border-color: red; -fx-border-radius: 3;");
                                
                                HBox questionHeader = new HBox(10, questionLabel, sensitiveLabel);
                                questionContent.getChildren().add(questionHeader);
                            } else {
                                questionContent.getChildren().add(questionLabel);
                            }
                            
                            questionContent.getChildren().add(askedByLabel);
                             
                            // Create check review button for the Question
                            Button questionReviewButton = new Button("Check Reviews");
                            // Set the action for the question review button
                            questionReviewButton.setOnAction(e -> {
                                if (finalQuestionId > 0) {
                                    reviewWindow.showReviewWindow(finalQuestionId);
                                }
                            });

                            // align neatly with sufficient space
                            Region rightSpacer = new Region();
                            HBox.setHgrow(rightSpacer, Priority.ALWAYS);

                            // Combine the voting and question content in an hbox
                            HBox questionBox = new HBox(10, votingControls, questionContent, rightSpacer, questionReviewButton);
                            questionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                            container.getChildren().add(questionBox);
                            
                            
                            // Separate answers with reviews and without reviews
                            List<Answer> answersWithReviews = new ArrayList<>();
                            List<Answer> answersWithoutReviews = new ArrayList<>();

                            // Separate answers into the two lists based on whether they have reviews
                            for (Answer answer : item.getAnswers()) {
                                if (answer.hasReview()) {  // had to create a method to check if an answer has reviews
                                    answersWithReviews.add(answer);
                                } else {
                                    answersWithoutReviews.add(answer);
                                }
                            }
                            
                            
                            // Sort the answers without reviews based on upvotes and downvotes
                            answersWithoutReviews.sort((a1, a2) -> {
                                int votes1 = a1.getUpvotes() - a1.getDownvotes();
                                int votes2 = a2.getUpvotes() - a2.getDownvotes();
                                return Integer.compare(votes2, votes1);  // Sorting in descending order of votes
                            });
                            
                            // Combine the answers with reviews at the top, followed by the sorted answers
                            answersWithReviews.addAll(answersWithoutReviews);

                            // Add answers
                            if (!item.getAnswers().isEmpty()) {
                                Label answersLabel = new Label("Answers:");
                                answersLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 0 0;");
                                container.getChildren().add(answersLabel);
                                // display box for answers
                                for (Answer answer : item.getAnswers()) {
                                    HBox answerBox = new HBox(10);
                                    
                                    // Answer text
                                    Label answerText = new Label("• " + answer.getText() + " - Answered by " + answer.getAnsweredBy());
                                    answerText.setWrapText(true);
                                    answerText.setMaxWidth(500);
                                    
                                    // Load user's current upvote/downvote status for current answer
                                    String userVoteType = databaseHelper.getUserVoteType(answer.getId(), currentUser.getUserName());
                                    hasUpvoted[0] = "upvote".equals(userVoteType);
                                    hasDownvoted[0] = "downvote".equals(userVoteType);
                                    
                                    // Create voting buttons for each answer
                                    Button answerUpvoteBtn = new Button("⬆");
                                    Button answerDownvoteBtn = new Button("⬇");
                                    
                                    // Create check review button
                                    Button reviewButton = new Button("Check Reviews");
                                    // Adjust with answer size
                                    Region spacer = new Region();
                                    HBox.setHgrow(spacer, Priority.ALWAYS);
                                    reviewButton.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                                    
                                    // Use the new ReviewWindow to handle showing reviews
                                    reviewButton.setOnAction(e -> reviewWindow.showReviewWindow(answer.getId()));
                                    
                                    // Color buttons based on the user's vote
                                    // Green for upvote
                                    if (hasUpvoted[0]) {
                                        answerUpvoteBtn.setStyle("-fx-background-color: #4CAF50;");
                                    }
                                    // Red for downvote
                                    else if (hasDownvoted[0]) {
                                        answerDownvoteBtn.setStyle("-fx-background-color: #F44336;");
                                    }
                                    // No current vote = default style
                                    else {
                                        answerUpvoteBtn.setStyle("");
                                    }
                                    
                                    // Initialize votes with straight from database
                                    Label answerVotesLabel = new Label("[Votes: " + (answer.getUpvotes() - answer.getDownvotes()) + "]");
                                    
                                    // Action setter for upvote being pressed
                                    answerUpvoteBtn.setOnAction(e -> {
                                        boolean success;
                                        
                                        // User has already clicked upvote and is clicking it again to remove it to no vote
                                        if (hasUpvoted[0]) {
                                            success = databaseHelper.recordVote(answer.getId(), currentUser.getUserName(), "upvote");
                                            
                                            // Remove upvote style color
                                            if (success) {
                                                hasUpvoted[0] = false;
                                                answerUpvoteBtn.setStyle("");
                                                
                                                // Recalculate votes and update ui
                                                databaseHelper.recalculateAnswerVotes(answer.getId());
                                                answer.setUpvote(databaseHelper.getAnswerUpvotes(answer.getId()));
                                                answer.setDownvote(databaseHelper.getAnswerDownvotes(answer.getId()));
                                                answerVotesLabel.setText("[Votes: " + (answer.getUpvotes() - answer.getDownvotes()) + "]");
                                            }
                                        }
                                        // User is upvoting a nonupvoted answer
                                        else {
                                            success = databaseHelper.recordVote(answer.getId(), currentUser.getUserName(), "upvote");
                                            if (success) {
                                                hasUpvoted[0] = true;
                                                answerUpvoteBtn.setStyle("-fx-background-color: #4CAF50;");
                                                
                                                // If they previously had downvoted, remove red style
                                                if (hasDownvoted[0]) {
                                                    hasDownvoted[0] = false;
                                                    answerDownvoteBtn.setStyle("");
                                                }
                                                
                                                // Recalculate votes and update UI
                                                databaseHelper.recalculateAnswerVotes(answer.getId());
                                                answer.setUpvote(databaseHelper.getAnswerUpvotes(answer.getId()));
                                                answer.setDownvote(databaseHelper.getAnswerDownvotes(answer.getId()));
                                                answerVotesLabel.setText("[Votes: " + (answer.getUpvotes() - answer.getDownvotes()) + "]");
                                            }
                                        }
                                        
                                        if (!success) {
                                            showErrorMessage("Database Error", "Failed to save the vote to the database. Errorcode AOER1349");
                                        }
                                    });
                                    
                                    // Action setter for downvote being pressed
                                    answerDownvoteBtn.setOnAction(e -> {
                                        boolean success;

                                        // User had previously downvoted and is clicking downvote to remove their vote
                                        if (hasDownvoted[0]) {
                                            success = databaseHelper.recordVote(answer.getId(), currentUser.getUserName(), "downvote");
                                            if (success) {
                                                hasDownvoted[0] = false;
                                                answerDownvoteBtn.setStyle(""); // Reset style
                                                
                                                // Recalculate votes and update UI
                                                databaseHelper.recalculateAnswerVotes(answer.getId());
                                                answer.setUpvote(databaseHelper.getAnswerUpvotes(answer.getId()));
                                                answer.setDownvote(databaseHelper.getAnswerDownvotes(answer.getId()));
                                                answerVotesLabel.setText("[Votes: " + (answer.getUpvotes() - answer.getDownvotes()) + "]");
                                            }
                                        }
                                        // User had not downvoted and is downvoting
                                        else {
                                            success = databaseHelper.recordVote(answer.getId(), currentUser.getUserName(), "downvote");
                                            if (success) {
                                                hasDownvoted[0] = true;
                                                answerDownvoteBtn.setStyle("-fx-background-color: #F44336;");
                                                
                                                // Remove upvote style if previously upvoted
                                                if (hasUpvoted[0]) {
                                                    hasUpvoted[0] = false;
                                                    answerUpvoteBtn.setStyle("");
                                                }
                                                
                                                // Recalculate votes and update UI
                                                databaseHelper.recalculateAnswerVotes(answer.getId());
                                                answer.setUpvote(databaseHelper.getAnswerUpvotes(answer.getId()));
                                                answer.setDownvote(databaseHelper.getAnswerDownvotes(answer.getId()));
                                                answerVotesLabel.setText("[Votes: " + (answer.getUpvotes() - answer.getDownvotes()) + "]");
                                            }
                                        }
                                        
                                        if (!success) {
                                            showErrorMessage("Database Error", "Failed to save the vote to the database. Errorcode ORFL0695");
                                        }
                                    });
                                    
                                    HBox answerVotingBox = new HBox(5, answerUpvoteBtn, answerDownvoteBtn, answerVotesLabel);
                                    
                                    // Check if answer is marked as sensitive
                                    if (answer.isSensitive()) {
                                        Label answerSensitiveLabel = new Label("SENSITIVE");
                                        answerSensitiveLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: #FFEEEE; -fx-padding: 2 5; -fx-border-color: red; -fx-border-radius: 3;");
                                        
                                        HBox answerHeader = new HBox(10, answerText, answerSensitiveLabel);
                                        // Replace the answerBox.getChildren().addAll(answerText, ...) with:
                                        answerBox.getChildren().addAll(answerHeader, answerVotingBox, spacer, reviewButton);
                                    } else {
                                        // Original line:
                                        answerBox.getChildren().addAll(answerText, answerVotingBox, spacer, reviewButton);
                                    }
                                    
                                    // Mark if the answer is correct
                                    if (answer.isCorrect()) {
                                        Label correctLabel = new Label("[CORRECT]");
                                        correctLabel.setStyle("-fx-text-fill: green;");
                                        answerBox.getChildren().add(correctLabel);
                                    }
                                    
                                    // Indent the answers
                                    answerBox.setPadding(new javafx.geometry.Insets(0, 0, 0, 20));
                                    container.getChildren().add(answerBox);
                                }
                            }
                            
                            // no answers
                            else {
                                Label noAnswersLabel = new Label("No answers yet");
                                noAnswersLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
                                noAnswersLabel.setPadding(new javafx.geometry.Insets(0, 0, 0, 20));
                                container.getChildren().add(noAnswersLabel);
                            }
                            
                            javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
                            container.getChildren().add(separator);
                            // padding
                            container.setPadding(new javafx.geometry.Insets(5));
                            setGraphic(container);
                        }
                    }
                };
            }
        });
        
        // Search functionality
        TextField searchField = new TextField();
        searchField.setPromptText("Enter search term");
        searchField.setPrefWidth(200);

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        searchButton.setOnAction(e -> {
            String searchTerm = searchField.getText().trim();
            System.out.println("it worked: " + searchTerm);
            
            // Find matching questions
            ObservableList<Question> searchResults = searchQuestions(searchTerm);
            
            // Update the ListView with search results
            questionListView.setItems(searchResults);
            
            // If no results found, show a message
            if (searchResults.isEmpty()) {
                showErrorMessage("Search Results", "No questions or answers found containing: " + searchTerm);
            }
        });
        
        // NEW BUTTON: Search for reviews by trusted reviewers
        Button trustedReviewerSearchButton = new Button("Trusted Reviewer Reviews");
        trustedReviewerSearchButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        trustedReviewerSearchButton.setOnAction(e -> {
            // Find questions with reviews by trusted reviewers
            ObservableList<Question> trustedReviewerResults = searchQuestionsByTrustedReviewer();
            
            // Update the ListView with search results
            questionListView.setItems(trustedReviewerResults);
            
            // If no results found, show a message
            if (trustedReviewerResults.isEmpty()) {
                showErrorMessage("Search Results", "No questions or answers found with reviews by your trusted reviewers.");
            }
        });
        
        Button clearSearchButton = new Button("Clear Search");
        clearSearchButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        clearSearchButton.setOnAction(e -> {
            // Clear the search field
            searchField.clear();
            
            // Reset the list view to show all questions
            questionListView.setItems(questions);
        });
        
        HBox searchBox = new HBox(10); // 10 is the spacing between elements
        searchBox.setStyle("-fx-alignment: center;");
        searchBox.getChildren().addAll(searchField, searchButton, trustedReviewerSearchButton, clearSearchButton);
        
        // Button: Ask question
        Button addButton = new Button("Ask a Question");
        addButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        // Check if user has permission to ask questions
        if (!currentUser.canAskQuestions())
            addButton.setDisable(true);
        addButton.setOnAction(e -> promptForNewQuestion(questionListView));

        // Button: Delete highlighted question
        Button deleteButton = new Button("Delete Question");
        deleteButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        deleteButton.setOnAction(e -> {
            // Get index of question and check if current user owns the question or is staff
            int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                Question selectedQuestion = questions.get(selectedIndex);
                if (selectedQuestion.getAskedBy().equals(currentUser.getUserName()) || currentUser.hasRole("staff")) {
                    // Show confirmation dialog
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Delete Question");
                    confirmDialog.setHeaderText("Are you sure you want to delete this question?");
                    confirmDialog.setContentText("This action cannot be undone.");
                    
                    Optional<ButtonType> result = confirmDialog.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // Delete question from database
                        boolean was_deleted = databaseHelper.deleteQuestion(selectedQuestion);
                        if (was_deleted) {
                            questions.remove(selectedIndex);
                            questionIDs.remove(selectedQuestion);
                            
                            // Show success 
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Success");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Question deleted successfully.");
                            successAlert.showAndWait();
                        }
                        else {
                            showErrorMessage("Error", "Failed to delete question from database");
                        }
                    }
                }
                else {
                    showErrorMessage("Error", "You can only delete questions that you have asked.");
                }
            }
            else {
                showErrorMessage("Error", "Please select a question to delete.");
            }
        });
        
        // Button: Answer question
        Button answerButton = new Button("Answer Question");
        answerButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        // Check if user has permission to answer questions
        if (!currentUser.canAnswerQuestions())
            answerButton.setDisable(true);
        answerButton.setOnAction(e -> {
            int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                promptForAnswer(questionListView, selectedIndex);
            }
        });
        
        
        
        // Button: Update Question
        Button updateButton = new Button("Update Question");
        updateButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        updateButton.setOnAction(e -> {
            int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                Question selectedQuestion = questions.get(selectedIndex);
                promptForUpdatedQuestion(questionListView, selectedQuestion, selectedIndex);
            }
        });
        
        // Button: Refresh DB for displayed questions
        Button refreshButton = new Button("Refresh Questions");
        refreshButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        refreshButton.setOnAction(e -> {
            questions.clear();
            questions.addAll(loadQuestionsFromDatabase());
            questionListView.refresh();
        });
        
        HBox buttonContainer = new HBox(10);
        buttonContainer.setStyle("-fx-alignment: center;");
        buttonContainer.getChildren().addAll(addButton, deleteButton, answerButton, updateButton, refreshButton);
        
        // Back Button
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        backButton.setOnAction(e -> navigateToUserHomePage());
        
        layout.getChildren().addAll(questionListView, searchBox, buttonContainer, backButton);
        
        // Add a quit button to the button container in the createScene method
        Button quitButton = new Button("Quit Application");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        quitButton.setOnAction(e -> {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Exit");
            confirmAlert.setHeaderText("Are you sure you want to quit?");
            confirmAlert.setContentText("Any unsaved changes will be lost.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Close database connections
                closeConnection();
                
                // Exit the application
                Platform.exit();
            }
        });

        // Add the quit button to the buttonContainer
        buttonContainer.getChildren().add(quitButton);
        
        return new Scene(layout, 1024, 600);
    }
    
    /**
     * Searches for questions that have reviews by trusted reviewers.
     * This method identifies questions that have been reviewed by users
     * who are trusted by the current user.
     * 
     * @return An ObservableList of questions that have reviews by trusted reviewers
     */
    private ObservableList<Question> searchQuestionsByTrustedReviewer() {
        ObservableList<Question> results = FXCollections.observableArrayList();
        
        if (currentUser == null) {
            showErrorMessage("Error", "No user is currently logged in.");
            return results;
        }
        
        // Get list of trusted reviewers for the current user
        List<String> trustedReviewers = databaseHelperReviews.getTrustedReviewers(currentUser.getUserName());
        
        // If the user doesn't trust any reviewers, return empty list
        if (trustedReviewers.isEmpty()) {
            showErrorMessage("No Trusted Reviewers", "You haven't added any trusted reviewers yet.");
            return results;
        }
        
        // Set to track IDs of questions that match our criteria
        Set<Integer> matchingQuestionIds = new HashSet<>();
        
        // Load all reviews from the database to ensure we have the latest data
        Map<Review, Integer> reviewsWithIDs = databaseHelper.loadAllReviewsWithIDs();
        
        // For each review, check if the reviewer is trusted
        for (Map.Entry<Review, Integer> entry : reviewsWithIDs.entrySet()) {
            Review review = entry.getKey();
            String reviewer = review.getReviewedBy();
            
            // If the reviewer is trusted by the current user
            if (trustedReviewers.contains(reviewer)) {
                // Get the answer ID from the review
                int answerId = review.getAnswerID();
                
                // Get the question ID for this answer
                int questionId = databaseHelper.getQuestionIdForAnswer(answerId);
                if (questionId > 0) {
                    matchingQuestionIds.add(questionId);
                }
            }
        }
        
        // Now add all matching questions to the results
        for (Question question : questions) {
            Integer questionId = questionIDs.get(question);
            if (questionId != null && matchingQuestionIds.contains(questionId)) {
                results.add(question);
            }
        }
        
        return results;
    }
    
    /**
     * Navigates back to the appropriate home page based on user's role.
     * Routes the user to either the InstructorHomePage, UserHomePage, or
     * ReviewerHomePage depending on their role.
     */
    private void navigateToUserHomePage() {
        if (currentUser == null) {
            showErrorMessage("Error", "Current user is not set.");
            return;
        }

        if (currentUser.hasRole("instructor")) {
            // Route to InstructorHomePage
            InstructorHomePage instructorPage = new InstructorHomePage();
            instructorPage.show(primaryStage, databaseHelper, currentUser);
        } else if (currentUser.hasRole("student")) {
            // Route to UserHomePage
            UserHomePage userPage = new UserHomePage();
            userPage.show(primaryStage, currentUser);
        } else if (currentUser.hasRole("reviewer")) {
        	// Route to UserHomePage
        	ReviewerHomePage reviewerPage = new ReviewerHomePage();
        	reviewerPage.show(primaryStage, currentUser);
        } else {
            // Default fallback - could show error or a generic page
            showErrorMessage("Error", "Unknown user role, can't navigate to appropriate page.");
        }
    }
    
    /**
     * Loads questions from the database and populates the questions list.
     * If no questions exist in the database, creates and saves some default questions.
     * 
     * @return An ObservableList of Question objects loaded from the database
     */
    private ObservableList<Question> loadQuestionsFromDatabase() {
        ObservableList<Question> loadedQuestions = FXCollections.observableArrayList();
        Map<Question, Integer> loadedQuestionIDs = databaseHelper.loadAllQuestionsWithIDs();
        if (loadedQuestionIDs.isEmpty()) {
            Question q1 = new Question("What time is class?", "User A");
            Question q2 = new Question("When is the homework assignment due?", "User B");
            Question q3 = new Question("What computer labs are available to CS students?", "User C");
            //save to database and track IDs
            int id1 = databaseHelper.saveQuestion(q1);
            int id2 = databaseHelper.saveQuestion(q2);
            int id3 = databaseHelper.saveQuestion(q3);
            loadedQuestions.addAll(q1, q2, q3);
            //store IDs for later reference
            questionIDs.put(q1, id1);
            questionIDs.put(q2, id2);
            questionIDs.put(q3, id3);
        }
        else {
            for (Map.Entry<Question, Integer> entry : loadedQuestionIDs.entrySet()) {
                loadedQuestions.add(entry.getKey());
                questionIDs.put(entry.getKey(), entry.getValue());
            }
        }
        return loadedQuestions;
    }
    
    /**
     * Loads reviews from the database and populates the reviews list.
     * 
     * @return An ObservableList of Review objects loaded from the database
     */
    private ObservableList<Review> loadReviewsFromDatabase() {
        ObservableList<Review> loadedReviews = FXCollections.observableArrayList();
        Map<Review, Integer> loadedReviewIDs = databaseHelper.loadAllReviewsWithIDs();
        if (loadedReviewIDs.isEmpty()) {
            // No default reviews to add
        }
        else {
            for (Map.Entry<Review, Integer> entry : loadedReviewIDs.entrySet()) {
                loadedReviews.add(entry.getKey());
                reviewIDs.put(entry.getKey(), entry.getValue());
            }
        }
        return loadedReviews;
    }
    
    /**
     * Displays a dialog prompting the user to enter a new question.
     * Validates the question and saves it to the database if valid.
     * 
     * @param questionListView The ListView to update after adding a new question
     */
    private void promptForNewQuestion(ListView<Question> questionListView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Question");
        dialog.setHeaderText("Add a new question");
        dialog.setContentText("Please enter your question:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(questionText -> {
            // validate question
            QuestionValidator.ValidationResult validationResult = QuestionValidator.validateQuestion(questionText);
            // if question is valid
            if (!validationResult.isValid()) {
                showErrorMessage("Invalid Question", validationResult.getMessage());
            }
            else {
                // Create question object and store to DB
                Question newQuestion = new Question(questionText, currentUser.getUserName());    // obtain username
                int questionID = databaseHelper.saveQuestion(newQuestion);
                if (questionID != -1) {
                    questions.add(newQuestion);
                    questionIDs.put(newQuestion, questionID);
                }
                else {
                    showErrorMessage("Database Error", "Something went wrong trying to store this question to the database");
                }
            }
        });
    }
    
    /**
     * Displays a dialog prompting the user to enter an answer to a question.
     * Validates the answer and saves it to the database if valid.
     * 
     * @param questionListView The ListView containing the question to answer
     * @param selectedIndex The index of the selected question in the ListView
     */
    private void promptForAnswer(ListView<Question> questionListView, int selectedIndex) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Answer Question");
        dialog.setHeaderText("Provide an answer");
        dialog.setContentText("Enter your answer:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(answerText -> {
            // Validate answer
            QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(answerText);
            // Valid answer
            if (!validationResult.isValid()) {
                showErrorMessage("Invalid Answer", validationResult.getMessage());
            }
            // get question ID to update question in DB
            else {
                Question question = questionListView.getItems().get(selectedIndex);
                Answer answer = new Answer(answerText, currentUser.getUserName());
                Integer questionID = questionIDs.get(question);
                System.out.println(questionID);
                // question ID not cached
                if (questionID == null) {
                    questionID = -1;
                }
                if (questionID != -1) {
                    boolean added_answer = databaseHelper.saveAnswer(questionID, answer);
                    // database fetch success
                    if (added_answer) {
                        question.addAnswer(answer);
                        questionListView.refresh();
                    }
                    else {
                        showErrorMessage("Database Error", "Something went wrong saving the answer to the database.");
                    }
                }
                else {
                    showErrorMessage("Database Error #3", "Error finding question in database");
                }
            }
        });
    }
    
    /**
     * Displays a dialog prompting the user to update a question.
     * Validates the updated question and saves it to the database if valid.
     * 
     * @param questionListView The ListView containing the question to update
     * @param question The Question object to update
     * @param index The index of the question in the ListView
     */
    private void promptForUpdatedQuestion(ListView<Question> questionListView, Question question, int index) {
        TextInputDialog dialog = new TextInputDialog(question.getBody());
        dialog.setTitle("Update Question");
        dialog.setHeaderText("Update your question");
        dialog.setContentText("Edit your question:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(updatedText -> {
            if (updatedText.trim().isEmpty()) {
                showErrorMessage("Error", "The updated question cannot be empty.");
            } else {
                Integer questionID = questionIDs.get(question);
                if (questionID != null) {
                    boolean updated = databaseHelper.updateQuestion(questionID, updatedText);
                    if (updated) {
                        question.setBody(updatedText);
                        questionListView.refresh();
                    } else {
                        showErrorMessage("Database Error", "Failed to update the question in the database.");
                    }
                } else {
                    showErrorMessage("Database Error", "Question ID not found.");
                }
            }
        });
    }
    
    /**
     * Searches for questions and answers containing the specified search term.
     * 
     * @param searchTerm The term to search for
     * @return An ObservableList of questions matching the search criteria
     */
    private ObservableList<Question> searchQuestions(String searchTerm) {
        // If search term is empty, return all questions
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return loadQuestionsFromDatabase(); // Return all questions
        }
        
        // Create a new list for search results
        ObservableList<Question> searchResults = FXCollections.observableArrayList();
        
        // Iterate through all questions
        for (Question question : questions) {
            boolean matchFound = false;
            
            // Check if search term is in question body
            if (question.getBody().toLowerCase().contains(searchTerm.toLowerCase())) {
                matchFound = true;
            } else {
                // Check if search term is in any of the answers
                for (Answer answer : question.getAnswers()) {
                    if (answer.getText().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matchFound = true;
                        break; // No need to check other answers
                    }
                }
            }
            
            // Add question to results if it matched
            if (matchFound) {
                searchResults.add(question);
            }
        }
        
        return searchResults;
    }
    
    /**
     * Displays an error message dialog with the specified title and content.
     * 
     * @param title The title of the error dialog
     * @param content The content message to display in the dialog
     */
    private void showErrorMessage(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Closes the database connection when the application is shutting down.
     * This should be called before exiting the application to ensure proper cleanup.
     */
    public void closeConnection() {
        if (databaseHelper != null) {
            databaseHelper.closeConnection();
        }
    }
}