package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelperReviews;
import application.ReviewValidator;
import application.QuestionValidator.ValidationResult;

/**
 * This class handles the Review Window functionality, including displaying,
 * creating, updating, and deleting reviews, as well as marking reviewers as trusted
 * and voting on reviews. It provides a UI interface for all review-related operations
 * and manages interactions with the database through helper classes.
 * 
 * @author ApplicationTeam
 * @version 1.0
 */
public class ReviewWindow {
    
    /** The stage containing the review window UI */
    private Stage reviewStage;
    
    /** The main application stage for navigation */
    private Stage primaryStage;
    
    /** Observable list of all reviews in the system */
    private ObservableList<Review> allReviews;
    
    /** Map linking Review objects to their database IDs */
    private Map<Review, Integer> reviewIDs;
    
    /** Helper for general database operations */
    private DatabaseHelper databaseHelper;
    
    /** Helper specifically for review-related database operations */
    private DatabaseHelperReviews reviewsHelper;
    
    /** The currently logged in user */
    private User currentUser;
    
    /**
     * Constructor for ReviewWindow which initializes the review management interface.
     * 
     * @param allReviews ObservableList of all reviews in the system
     * @param reviewIDs Map of reviews to their database IDs
     * @param databaseHelper Database connection helper for general operations
     * @param currentUser Current logged in user
     */
    public ReviewWindow(ObservableList<Review> allReviews, Map<Review, Integer> reviewIDs, 
                        DatabaseHelper databaseHelper, User currentUser) {
        this.allReviews = allReviews;
        this.reviewIDs = reviewIDs;
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
        this.reviewsHelper = new DatabaseHelperReviews(databaseHelper);
    }
    
    /**
     * Sets the primary stage for navigation purposes.
     * This allows the review window to navigate back to the main application.
     * 
     * @param primaryStage The main application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Displays the review window for a specific answer showing all associated reviews.
     * Creates a new stage if one doesn't exist, or brings an existing one to the front.
     * The window includes functionality for creating, updating, and deleting reviews,
     * as well as marking reviewers as trusted and voting on reviews.
     * 
     * @param answerID The ID of the answer to show reviews for
     */
    public void showReviewWindow(int answerID) {
        // Check if review window is already open
        if (reviewStage != null && reviewStage.isShowing()) {
            reviewStage.toFront();
            return;
        }
        
        // Create new stage for reviews
        reviewStage = new Stage();
        reviewStage.setTitle("Reviews");
        
        // Create layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        
        // Create ListView for reviews
        ListView<Review> answerReviewsListView = new ListView<>();
        
        // Filter reviews for this answer
        ObservableList<Review> answerReviews = FXCollections.observableArrayList();
        for (Review review : allReviews) {
            if (review.getAnswerID() == answerID) {
                answerReviews.add(review);
            }
        }
        
        // Custom cell factory to include trust checkbox
        answerReviewsListView.setCellFactory(lv -> new ListCell<Review>() {
            /** Checkbox for marking reviewers as trusted */
            private CheckBox trustCheckBox = new CheckBox();
            
            /** Container for all cell components */
            private HBox cell = new HBox(5);
            
            /** Button to message the reviewer */
            private Button messageButton = new Button("Message");
            
            /** Button for upvoting a review */
            private Button upvoteButton = new Button("👍");
            
            /** Button for downvoting a review */
            private Button downvoteButton = new Button("👎");
            
            /** Label showing voting statistics */
            private Label votesLabel = new Label("[Helpful: 0, Not Helpful: 0, Score: 0%]");
            
            {
                // Set up the checkbox
                trustCheckBox.setOnAction(e -> {
                    Review review = getItem();
                    if (review != null) {
                        String reviewerName = review.getReviewedBy();
                        // Skip if the reviewer is the current user
                        if (reviewerName.equals(currentUser.getUserName())) {
                            trustCheckBox.setSelected(false);
                            showErrorMessage("Cannot Trust Self", "You cannot add yourself to your trusted reviewers list.");
                            return;
                        }
                        
                        boolean isSelected = trustCheckBox.isSelected();
                        boolean success;
                        
                        if (isSelected) {
                            // Add to trusted reviewers
                            success = reviewsHelper.addTrustedReviewer(currentUser.getUserName(), reviewerName);
                            if (!success) {
                                showErrorMessage("Trust Error", "Failed to add " + reviewerName + " to your trusted reviewers list.");
                                trustCheckBox.setSelected(false);
                            }
                        } else {
                            // Remove from trusted reviewers
                            success = reviewsHelper.removeTrustedReviewer(currentUser.getUserName(), reviewerName);
                            if (!success) {
                                showErrorMessage("Trust Error", "Failed to remove " + reviewerName + " from your trusted reviewers list.");
                                trustCheckBox.setSelected(true);
                            }
                        }
                    }
                });
                
                // Set up the message button
                messageButton.setOnAction(e -> {
                    Review review = getItem();
                    if (review != null) {
                        String reviewerName = review.getReviewedBy();
                        if (reviewerName.equals(currentUser.getUserName())) {
                            showErrorMessage("Cannot Message Self", "You cannot send a message to yourself.");
                            return;
                        }
                        openDirectMessageWith(reviewerName);
                    }
                });
                
                // Set up upvote button action
                upvoteButton.setOnAction(e -> {
                    Review review = getItem();
                    if (review != null) {
                        Integer reviewId = reviewIDs.get(review);
                        if (reviewId != null) {
                            // Process the upvote
                            handleReviewVote(review, reviewId, "upvote");
                        }
                    }
                });
                
                // Set up downvote button action
                downvoteButton.setOnAction(e -> {
                    Review review = getItem();
                    if (review != null) {
                        Integer reviewId = reviewIDs.get(review);
                        if (reviewId != null) {
                            // Process the downvote
                            handleReviewVote(review, reviewId, "downvote");
                        }
                    }
                });
                
                // Set up the cell layout
                HBox.setHgrow(cell, Priority.ALWAYS);
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setSpacing(10);
            }
            
            /**
             * Updates the cell content when a review item is assigned to the cell.
             * Sets up the cell display with trust checkbox, review text, voting controls,
             * and message button.
             * 
             * @param item The Review object to display in this cell
             * @param empty Whether the cell is empty
             */
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a container for the review text with some margin
                    VBox reviewTextContainer = new VBox();
                    reviewTextContainer.setPadding(new Insets(0, 10, 0, 5));
                    HBox.setHgrow(reviewTextContainer, Priority.ALWAYS);
                    
                    // Add the review text to the container
                    javafx.scene.text.Text reviewText = new javafx.scene.text.Text(
                            item.getReviewBody() + " - by " + item.getReviewedBy());
                    reviewText.setWrappingWidth(300);
                    reviewTextContainer.getChildren().add(reviewText);
                    
                    // Update checkbox state based on trusted status
                    boolean isTrusted = reviewsHelper.isTrustedReviewer(
                            currentUser.getUserName(), item.getReviewedBy());
                    
                    // Don't allow users to trust themselves
                    if (item.getReviewedBy().equals(currentUser.getUserName())) {
                        trustCheckBox.setDisable(true);
                        trustCheckBox.setSelected(false);
                        
                        // Also disable message button for self
                        messageButton.setDisable(true);
                    } else {
                        trustCheckBox.setDisable(false);
                        trustCheckBox.setSelected(isTrusted);
                        
                        // Enable message button for others
                        messageButton.setDisable(false);
                    }
                    
                    // Get review ID
                    Integer reviewId = reviewIDs.get(item);
                    
                    // Reset button styles for this specific cell
                    upvoteButton.setStyle("");
                    downvoteButton.setStyle("");
                    
                    // Update voting UI based on current votes
                    if (reviewId != null) {
                        // Get the vote counts from the review object
                        int helpfulCount = item.getHelpfulCount();
                        int notHelpfulCount = item.getNotHelpfulCount();
                        int reviewScore = item.getReviewScore();
                        
                        // Set the labels to show helpful score and percentage
                        votesLabel.setText("[Helpful: " + helpfulCount + ", Not Helpful: " + notHelpfulCount + 
                                ", Score: " + reviewScore + "%]");
                        
                        // Check if user has already voted for THIS specific review
                        String userVoteType = databaseHelper.getUserReviewVoteType(reviewId, currentUser.getUserName());
                        
                        // Update button styles based on user's vote for this specific review
                        if ("upvote".equals(userVoteType)) {
                            upvoteButton.setStyle("-fx-background-color: #4CAF50;"); // Green
                            downvoteButton.setStyle("");
                        } else if ("downvote".equals(userVoteType)) {
                            downvoteButton.setStyle("-fx-background-color: #F44336;"); // Red
                            upvoteButton.setStyle("");
                        } else {
                            // No vote yet
                            upvoteButton.setStyle("");
                            downvoteButton.setStyle("");
                        }
                    }
                    
                    // Create vote controls container
                    HBox voteControls = new HBox(5, upvoteButton, downvoteButton, votesLabel);
                    voteControls.setAlignment(Pos.CENTER_LEFT);
                    
                    // Set tooltip text for the buttons
                    trustCheckBox.setTooltip(new javafx.scene.control.Tooltip("Mark as trusted reviewer"));
                    messageButton.setTooltip(new javafx.scene.control.Tooltip("Send a message to this reviewer"));
                    upvoteButton.setTooltip(new javafx.scene.control.Tooltip("Mark as helpful"));
                    downvoteButton.setTooltip(new javafx.scene.control.Tooltip("Mark as not helpful"));
                    
                    // Create a spacer to push message button to the right
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    // Arrange all components in the cell
                    cell.getChildren().clear();
                    cell.getChildren().addAll(trustCheckBox, reviewTextContainer, voteControls, spacer, messageButton);
                    setText(null);
                    setGraphic(cell);
                }
            }
        });
        
        answerReviewsListView.setItems(answerReviews);
        
        // Review Buttons
        HBox reviewButtons = new HBox(10);
        Button createReview = new Button("Create Review");
        Button updateReview = new Button("Update Review");
        Button deleteReview = new Button("Delete Review");
        
        // Set up button actions
        createReview.setOnAction(e -> promptForNewReview(answerReviewsListView, answerID));
        
        updateReview.setOnAction(e -> {
            Review selectedReview = answerReviewsListView.getSelectionModel().getSelectedItem();
            if (selectedReview != null) {
                if (selectedReview.getReviewedBy().equals(currentUser.getUserName())) {
                    promptForUpdatedReview(answerReviewsListView, selectedReview);
                } else {
                    showErrorMessage("Error", "You can only update your own reviews.");
                }
            } else {
                showErrorMessage("Error", "Please select a review to update.");
            }
        });
        
        deleteReview.setOnAction(e -> {
            Review selectedReview = answerReviewsListView.getSelectionModel().getSelectedItem();
            if (selectedReview != null) {
                if (selectedReview.getReviewedBy().equals(currentUser.getUserName())) {
                    Integer reviewID = reviewIDs.get(selectedReview);
                    if (reviewID != null) {
                        boolean deleted = databaseHelper.deleteReview(reviewID);
                        if (deleted) {
                            answerReviews.remove(selectedReview);
                            allReviews.remove(selectedReview);
                            reviewIDs.remove(selectedReview);
                        } else {
                            showErrorMessage("Database Error", "Failed to delete the review from the database.");
                        }
                    }
                } else {
                    showErrorMessage("Error", "You can only delete your own reviews.");
                }
            } else {
                showErrorMessage("Error", "Please select a review to delete.");
            }
        });
        
        // Check if user has reviewer role before adding the buttons
        if (currentUser.hasRole("reviewer")) {
            reviewButtons.getChildren().addAll(createReview, updateReview, deleteReview);
        }
        
        reviewButtons.setAlignment(Pos.CENTER);
        
        // Add components to layout
        layout.getChildren().addAll(answerReviewsListView, reviewButtons);
        
        // Create scene
        Scene reviewScene = new Scene(layout, 800, 400);
        reviewStage.setScene(reviewScene);
        
        // Close event handler
        reviewStage.setOnCloseRequest(event -> reviewStage = null);
        reviewStage.show();
    }
    
    /**
     * Handles the vote action for a review including database updates and UI refresh.
     * Manages different vote scenarios: new votes, changing votes, or removing votes.
     * 
     * @param review The review being voted on
     * @param reviewId The ID of the review in the database
     * @param voteType The type of vote ("upvote" or "downvote")
     */
    private void handleReviewVote(Review review, int reviewId, String voteType) {
        // Get current vote status
        String currentVoteType = databaseHelper.getUserReviewVoteType(reviewId, currentUser.getUserName());
        
        // Record the vote in the database
        boolean success = databaseHelper.recordReviewVote(reviewId, currentUser.getUserName(), voteType);
        
        if (success) {
            // Update the review object's helpfulness counts based on vote action
            if ("upvote".equals(voteType)) {
                // If switching from downvote to upvote
                if ("downvote".equals(currentVoteType)) {
                    // Decrement not helpful and increment helpful
                    int notHelpfulCount = review.getNotHelpfulCount();
                    if (notHelpfulCount > 0) {
                        review.setNotHelpfulCount(notHelpfulCount - 1);
                    }
                    review.incrementHelpful();
                } 
                // If removing an upvote
                else if ("upvote".equals(currentVoteType)) {
                    // Decrement helpful count
                    int helpfulCount = review.getHelpfulCount();
                    if (helpfulCount > 0) {
                        review.setHelpfulCount(helpfulCount - 1);
                    }
                }
                // If new upvote
                else {
                    review.incrementHelpful();
                }
            } else if ("downvote".equals(voteType)) {
                // If switching from upvote to downvote
                if ("upvote".equals(currentVoteType)) {
                    // Decrement helpful and increment not helpful
                    int helpfulCount = review.getHelpfulCount();
                    if (helpfulCount > 0) {
                        review.setHelpfulCount(helpfulCount - 1);
                    }
                    // Fix typo in method name
                    try {
                        // Try to call method with the typo first
                        review.incremenetNotHelpful();
                    } catch (NoSuchMethodError e) {
                        // If that fails, manually increment the count
                        review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
                    }
                } 
                // If removing a downvote
                else if ("downvote".equals(currentVoteType)) {
                    // Decrement not helpful count
                    int notHelpfulCount = review.getNotHelpfulCount();
                    if (notHelpfulCount > 0) {
                        review.setNotHelpfulCount(notHelpfulCount - 1);
                    }
                }
                // If new downvote
                else {
                    // Fix typo in method name
                    try {
                        // Try to call method with the typo first
                        review.incremenetNotHelpful();
                    } catch (NoSuchMethodError e) {
                        // If that fails, manually increment the count
                        review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
                    }
                }
            }
            
            // Recalculate votes in the database
            databaseHelper.recalculateReviewVotes(reviewId);
            
            // Refresh the list view to show updated vote counts
            ListView<Review> listView = ((ListView<Review>) reviewStage.getScene().getRoot().getChildrenUnmodifiable().get(0));
            listView.refresh();
        } else {
            showErrorMessage("Database Error", "Failed to save the vote to the database.");
        }
    }
    
    /**
     * Opens the Direct Message window to start a conversation with a specific reviewer.
     * Navigates to the Direct Message page and initializes a chat session.
     * 
     * @param reviewerUsername The username of the reviewer to message
     */
    private void openDirectMessageWith(String reviewerUsername) {
        if (primaryStage == null) {
            showErrorMessage("Navigation Error", "Cannot navigate to Direct Message page. Primary stage not set.");
            return;
        }
        
        // Create and show the Direct Message page
        DirectMessage dmPage = new DirectMessage();
        dmPage.setCurrentUser(currentUser);
        Scene dmScene = dmPage.createScene(primaryStage);
        
        if (dmScene != null) {
            // Save the current scene to allow returning
            Scene previousScene = primaryStage.getScene();
            
            // Show the Direct Message scene
            primaryStage.setScene(dmScene);
            primaryStage.setTitle("Direct Messages");
            
            // Request to start a new chat with the reviewer
            dmPage.startChatWith(reviewerUsername);
            
            // Close the review window
            if (reviewStage != null && reviewStage.isShowing()) {
                reviewStage.close();
                reviewStage = null;
            }
        } else {
            showErrorMessage("Error", "Could not open Direct Message page.");
        }
    }
    
    /**
     * Prompts the user to enter a new review for an answer.
     * Validates the review text and saves it to the database if valid.
     * 
     * @param reviewListView The list view to update with the new review
     * @param answerID The ID of the answer being reviewed
     */
    private void promptForNewReview(ListView<Review> reviewListView, int answerID) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Review");
        dialog.setHeaderText("Add a new review");
        dialog.setContentText("Please enter your review");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reviewText -> {
            // Validate review
            QuestionValidator.ValidationResult validationResult = ReviewValidator.validateReview(reviewText);
            // If review is invalid
            if (!validationResult.isValid()) {
                showErrorMessage("Invalid Review", validationResult.getMessage());
            }
            else {
                Review newReview = new Review(reviewText, currentUser.getUserName(), answerID);
                int reviewID = databaseHelper.saveReview(newReview);
                if (reviewID != -1) {
                    reviewListView.getItems().add(newReview);
                    allReviews.add(newReview);
                    reviewIDs.put(newReview, reviewID);
                }
                else {
                    showErrorMessage("Database Error", "Something went wrong trying to store this review to the database");
                }
            }
        });
    }
    
    /**
     * Prompts the user to update an existing review.
     * Validates the updated text and saves to the database if valid.
     * 
     * @param reviewListView The list view to refresh after update
     * @param review The review to update
     */
    private void promptForUpdatedReview(ListView<Review> reviewListView, Review review) {
        TextInputDialog dialog = new TextInputDialog(review.getReviewBody());
        dialog.setTitle("Update Review");
        dialog.setHeaderText("Update your review");
        dialog.setContentText("Edit your review:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(updatedText -> {
            if (updatedText.trim().isEmpty()) {
                showErrorMessage("Error", "The updated review cannot be empty.");
            } else {
                Integer reviewID = reviewIDs.get(review);
                if (reviewID != null) {
                    boolean updated = databaseHelper.updateReview(reviewID, updatedText);
                    if (updated) {
                        review.setReviewBody(updatedText);
                        reviewListView.refresh();
                    } else {
                        showErrorMessage("Database Error", "Failed to update the review in the database.");
                    }
                } else {
                    showErrorMessage("Database Error", "Review ID not found.");
                }
            }
        });
    }
    
    /**
     * Displays an error message dialog with the specified title and content.
     * 
     * @param title The title of the error message dialog
     * @param content The content of the error message dialog
     */
    private void showErrorMessage(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}