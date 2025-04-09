package application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This class provides functionality to view all reviews created by the current user.
 * It allows users to see, edit, and delete their own reviews through a JavaFX interface.
 * 
 * @author Application Team
 * @version 1.0
 */
public class MyReviewsPage {
    
    /** Database helper for database operations */
    private DatabaseHelper databaseHelper;
    
    /** The currently logged in user */
    private User currentUser;
    
    /** Observable list of the user's reviews to display in ListView */
    private ObservableList<Review> userReviews;
    
    /** Map to store review objects with their corresponding database IDs */
    private Map<Review, Integer> reviewIDs;
    
    /**
     * Constructor for MyReviewsPage that initializes database connection and loads user reviews.
     * 
     * @param databaseHelper Database connection helper for database operations
     * @param currentUser Current logged in user whose reviews will be displayed
     */
    public MyReviewsPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
        this.userReviews = FXCollections.observableArrayList();
        this.reviewIDs = new HashMap<>();
        
        // Load the user's reviews
        loadUserReviews();
    }
    
    /**
     * Loads all reviews created by the current user from the database.
     * Populates the userReviews observable list and the reviewIDs map.
     */
    private void loadUserReviews() {
        List<Review> reviews = DatabaseHelper.getReviewsByAuthor(currentUser.getUserName());
        userReviews.clear();
        userReviews.addAll(reviews);
        
        // Load review IDs
        for (Review review : userReviews) {
            try {
                int reviewId = databaseHelper.findIdOfReview(review);
                reviewIDs.put(review, reviewId);
            } catch (Exception e) {
                System.err.println("Error finding ID for review: " + e.getMessage());
            }
        }
    }
    
    /**
     * Creates and returns the scene for the My Reviews page.
     * Sets up the layout with a header, list view of reviews, and action buttons.
     * 
     * @param primaryStage The primary stage of the application
     * @return Scene containing the My Reviews page layout
     */
    public Scene createScene(Stage primaryStage) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        
        // Create header
        Label headerLabel = new Label("My Reviews");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox headerBox = new HBox(headerLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Create ListView for reviews
        ListView<Review> reviewsListView = new ListView<>(userReviews);
        reviewsListView.setCellFactory(lv -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create container
                    VBox container = new VBox(5);
                    container.setPadding(new Insets(5));
                    
                    // Answer ID label
                    Label answerIdLabel = new Label("Answer ID: " + item.getAnswerID());
                    answerIdLabel.setStyle("-fx-font-weight: bold;");
                    
                    // Review text with wrapping
                    Text reviewText = new Text(item.getReviewBody());
                    reviewText.setWrappingWidth(550);
                    
                    container.getChildren().addAll(answerIdLabel, reviewText);
                    setGraphic(container);
                }
            }
        });
        
        // Create action buttons
        Button editButton = new Button("Edit Review");
        editButton.setOnAction(e -> {
            Review selectedReview = reviewsListView.getSelectionModel().getSelectedItem();
            if (selectedReview != null) {
                promptForUpdatedReview(reviewsListView, selectedReview);
            } else {
                showErrorMessage("Selection Required", "Please select a review to edit.");
            }
        });
        
        Button deleteButton = new Button("Delete Review");
        deleteButton.setOnAction(e -> {
            Review selectedReview = reviewsListView.getSelectionModel().getSelectedItem();
            if (selectedReview != null) {
                promptForDeleteConfirmation(reviewsListView, selectedReview);
            } else {
                showErrorMessage("Selection Required", "Please select a review to delete.");
            }
        });
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            loadUserReviews();
            reviewsListView.refresh();
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new ReviewerHomePage().show(primaryStage, currentUser);
        });
        
        // Layout for buttons
        HBox buttonsBox = new HBox(10, editButton, deleteButton, refreshButton, backButton);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(20, 0, 0, 0));
        
        // No reviews message
        if (userReviews.isEmpty()) {
            Label noReviewsLabel = new Label("You haven't created any reviews yet.");
            noReviewsLabel.setStyle("-fx-font-size: 16px;");
            VBox noReviewsBox = new VBox(noReviewsLabel);
            noReviewsBox.setAlignment(Pos.CENTER);
            mainLayout.setCenter(noReviewsBox);
        } else {
            mainLayout.setCenter(reviewsListView);
        }
        
        mainLayout.setTop(headerBox);
        mainLayout.setBottom(buttonsBox);
        
        return new Scene(mainLayout, 800, 600);
    }
    
    /**
     * Prompts the user to confirm deletion of a review and handles the deletion
     * process if confirmed.
     * 
     * @param reviewListView The ListView to update after deletion
     * @param review The review to delete
     */
    private void promptForDeleteConfirmation(ListView<Review> reviewListView, Review review) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Review");
        alert.setContentText("Are you sure you want to delete this review?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Integer reviewID = reviewIDs.get(review);
            if (reviewID != null) {
                boolean deleted = databaseHelper.deleteReview(reviewID);
                if (deleted) {
                    userReviews.remove(review);
                    reviewIDs.remove(review);
                    reviewListView.refresh();
                } else {
                    showErrorMessage("Database Error", "Failed to delete the review from the database.");
                }
            } else {
                showErrorMessage("Database Error", "Review ID not found.");
            }
        }
    }
    
    /**
     * Prompts the user to update an existing review, validates the input,
     * and updates the review in the database if validation passes.
     * 
     * @param reviewListView The ListView to refresh after update
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
                // Validate the updated review
                QuestionValidator.ValidationResult validationResult = 
                    ReviewValidator.validateReview(updatedText);
                
                if (!validationResult.isValid()) {
                    showErrorMessage("Invalid Review", validationResult.getMessage());
                    return;
                }
                
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
     * @param content The content text of the error message dialog
     */
    private void showErrorMessage(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Closes the database connection.
     * This method is required by the interface but doesn't need to do anything
     * since the DatabaseHelper will be closed by the caller.
     */
    public void closeConnection() {
        // This method is required by the interface but we don't need to do anything
        // since the DatabaseHelper will be closed by the caller
    }
}