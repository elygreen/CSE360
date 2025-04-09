package application;
import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InstructorHomePage class represents the user interface for the instructor user.
 * This page displays the main dashboard for instructor interactions, including
 * navigation to QandA sections, direct messages, and student reviewer management.
 */
public class InstructorHomePage {
    /** Database helper for executing database operations */
    private DatabaseHelper databaseHelper;
    
    /** The currently logged in user (instructor) */
    private User currentUser;
    
    /**
     * Displays the instructor page in the provided primary stage.
     * Creates the UI layout with various navigation buttons and displays welcome message.
     * 
     * @param primaryStage The primary stage where the scene will be displayed
     * @param databaseHelper Helper class for database operations
     * @param user The currently logged in instructor user
     */
    public void show(Stage primaryStage, DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.currentUser = user;
        
        VBox layout = new VBox(15); // Add spacing between elements
        
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // label to display the welcome message for the instructor
        Label instructorLabel = new Label("Hello, " + user.getUserName() + "!");
        
        instructorLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Button to access QandA Page
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
        
        // Go to previous page
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        backButton.setOnAction(a -> {
            // Navigate to the WelcomeLoginPage
            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        });
        
        /**
         * Button for managing student reviewers - opens the promotion interface
         * when clicked, allowing instructors to grant additional permissions
         */
        Button promoteButton = new Button("Manage Student Reviewers");
        promoteButton.setOnAction(e -> {
            InstructorPromote promotionWindow = new InstructorPromote(databaseHelper, currentUser);
            promotionWindow.show();
        });
        
        // Quit button
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        quitButton.setOnAction(e -> {
            databaseHelper.closeConnection();
            System.exit(0);
        });
        
        layout.getChildren().addAll(instructorLabel, qandaButton, directMessageButton, backButton, promoteButton, quitButton);
        Scene instructorScene = new Scene(layout, 800, 400);
        // Set the scene to primary stage
        primaryStage.setScene(instructorScene);
        primaryStage.setTitle("Instructor Page");
    }
    
    /**
     * Opens the QandA Page in the same window, replacing the current view.
     * Sets up the QandAPage with the current user and handles window close events.
     * 
     * @param primaryStage The primary stage to display the QandA page
     */
    private void openQandAPage(Stage primaryStage) {
        // Create QandAPage instance
        QandAPage qandaPage = new QandAPage();
        qandaPage.setCurrentUser(currentUser);
        
        // Get the scene from QandAPage
        Scene qandaScene = qandaPage.createScene(primaryStage);
        
        // Two options:
        // 1. Open in the same window (replacing the current view)
        primaryStage.setScene(qandaScene);
        primaryStage.setTitle("Questions and Answers");
        
        // Add event handler to close database connection when window is closed
        primaryStage.setOnCloseRequest(e -> {
            qandaPage.closeConnection();
        });
    }
    
    /**
     * Opens the Direct Message Page in the same window, replacing the current view.
     * Sets up the DirectMessage interface with the current user and handles window close events.
     * 
     * @param primaryStage The primary stage to display the direct message page
     */
    private void openDirectMessagePage(Stage primaryStage) {
        // Create DirectMessage instance
        DirectMessage directMessagePage = new DirectMessage();
        directMessagePage.setCurrentUser(currentUser);
        
        // Get the scene from DirectMessage
        Scene directMessageScene = directMessagePage.createScene(primaryStage);
        
        // Open in the same window (replacing the current view)
        primaryStage.setScene(directMessageScene);
        primaryStage.setTitle("Direct Messages");
        
        // Add event handler to close database connection when window is closed
        primaryStage.setOnCloseRequest(e -> {
            directMessagePage.closeConnection();
        });
    }
}