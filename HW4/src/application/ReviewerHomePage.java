package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This class represents the home page for a reviewer user in the application.
 * It displays a welcome message for the reviewer and provides navigation buttons 
 * to access reviewer-specific functionality such as viewing questions and answers,
 * accessing direct messages, and viewing the reviewer's own reviews.
 * 
 * The class manages database connections and user state throughout the reviewer's session.
 */
public class ReviewerHomePage {
    
    /**
     * Database helper object used to manage database connections and operations.
     */
    private DatabaseHelper databaseHelper;
    
    /**
     * Reference to the currently logged in user.
     */
    private User currentUser;
    
    /**
     * Constructs a new ReviewerHomePage instance.
     * Initializes the database helper and attempts to establish a database connection.
     * If the connection fails, an error message is displayed to the user.
     */
    public ReviewerHomePage() {
        databaseHelper = new DatabaseHelper();
        try {
            databaseHelper.connectToDatabase();
        } catch (Exception e) {
            showErrorMessage("Database Error", "Failed to connect to database");
        }
    }
    
    /**
     * Displays the reviewer user page in the provided primary stage.
     * Sets up the UI elements including welcome message and navigation buttons.
     * 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user The currently logged in user.
     */
    public void show(Stage primaryStage, User user) {
        this.currentUser = user;
        
        VBox layout = new VBox(15); // Add spacing between elements
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Label to display Hello user
        Label userLabel = new Label("Hello, Reviewer " + user.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Button to access Q&A Page
        Button qandaButton = new Button("View Questions and Answers");
        qandaButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        qandaButton.setOnAction(e -> {
            openQandAPage(primaryStage);
        });
        
        // Button to access Direct Messages
        Button directMessageButton = new Button("Direct Messages");
        directMessageButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        directMessageButton.setOnAction(e -> {
            openDirectMessagePage(primaryStage);
        });
        
        // Button to view my reviews
        Button myReviewsButton = new Button("My Reviews");
        myReviewsButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        myReviewsButton.setOnAction(e -> {
            openMyReviewsPage(primaryStage);
        });
        
        // Go to previous page
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        backButton.setOnAction(a -> {
            // Navigate to the WelcomeLoginPage
            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        });
        
        // Quit button
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        quitButton.setOnAction(e -> {
            databaseHelper.closeConnection();
            System.exit(0);
        });
        
        layout.getChildren().addAll(userLabel, qandaButton, directMessageButton, myReviewsButton, backButton, quitButton);
        Scene reviewerScene = new Scene(layout, 800, 400);
        
        // Set the scene to primary stage
        primaryStage.setScene(reviewerScene);
        primaryStage.setTitle("Reviewer Page");
        primaryStage.setOnCloseRequest(e -> {
            databaseHelper.closeConnection();
        });
    }
    
    /**
     * Opens the Questions and Answers Page for the reviewer.
     * Creates a new QandAPage instance, sets the current user, and displays the scene.
     * Also sets up event handlers for closing the window properly.
     * 
     * @param primaryStage The primary stage where the Q&A scene will be displayed.
     */
    private void openQandAPage(Stage primaryStage) {
        // Create QandAPage instance
        QandAPage qandaPage = new QandAPage();
        qandaPage.setCurrentUser(currentUser);
        
        // Get the scene from QandAPage
        Scene qandaScene = qandaPage.createScene(primaryStage);
        
        // Display in the current window
        primaryStage.setScene(qandaScene);
        primaryStage.setTitle("Questions and Answers");
        
        // Add event handler to close database connection when window is closed
        primaryStage.setOnCloseRequest(e -> {
            qandaPage.closeConnection();
            databaseHelper.closeConnection();
        });
    }
    
    /**
     * Opens the Direct Message Page for the reviewer.
     * Creates a new DirectMessage instance, sets the current user, and displays the scene.
     * Also sets up event handlers for closing the window properly.
     * 
     * @param primaryStage The primary stage where the Direct Message scene will be displayed.
     */
    private void openDirectMessagePage(Stage primaryStage) {
        // Create DirectMessage instance
        DirectMessage directMessagePage = new DirectMessage();
        directMessagePage.setCurrentUser(currentUser);
        
        // Get the scene from DirectMessage
        Scene directMessageScene = directMessagePage.createScene(primaryStage);
        
        // Display in the current window
        primaryStage.setScene(directMessageScene);
        primaryStage.setTitle("Direct Messages");
        
        // Add event handler to close database connection when window is closed
        primaryStage.setOnCloseRequest(e -> {
            directMessagePage.closeConnection();
            databaseHelper.closeConnection();
        });
    }
    
    /**
     * Opens the My Reviews Page for the reviewer.
     * Creates a new MyReviewsPage instance and displays the scene.
     * Also sets up event handlers for closing the window properly.
     * 
     * @param primaryStage The primary stage where the My Reviews scene will be displayed.
     */
    private void openMyReviewsPage(Stage primaryStage) {
        // Create MyReviews instance
        MyReviewsPage myReviewsPage = new MyReviewsPage(databaseHelper, currentUser);
        
        // Get the scene from MyReviewsPage
        Scene myReviewsScene = myReviewsPage.createScene(primaryStage);
        
        // Display in the current window
        primaryStage.setScene(myReviewsScene);
        primaryStage.setTitle("My Reviews");
        
        // Add event handler to close database connection when window is closed
        primaryStage.setOnCloseRequest(e -> {
            myReviewsPage.closeConnection();
            databaseHelper.closeConnection();
        });
    }
    
    /**
     * Displays an error message to the user using a JavaFX Alert dialog.
     * 
     * @param title The title of the error message dialog.
     * @param content The content/body text of the error message dialog.
     */
    private void showErrorMessage(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}