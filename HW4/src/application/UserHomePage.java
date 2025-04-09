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
 * UserHomePage provides a graphical interface for student users after login.
 * This page displays a welcome message for the student user and provides 
 * navigation to various features like QandA functionality and direct messaging.
 * It serves as the main hub for student interactions with the application.
 */
public class UserHomePage {
    
    /**
     * Database helper instance for handling all database operations.
     */
    private DatabaseHelper databaseHelper;
    
    /**
     * Reference to the currently logged-in user.
     */
    private User currentUser;
    
    /**
     * Constructs a new UserHomePage instance.
     * Initializes the database connection upon creation.
     * If connection fails, an error message is displayed.
     */
    public UserHomePage() {
        databaseHelper = new DatabaseHelper();
        try {
            databaseHelper.connectToDatabase();
        } catch (Exception e) {
            showErrorMessage("Database Error", "Failed to connect to database");
        }
    }
    
    /**
     * Displays the student user page in the provided primary stage.
     * Creates and configures the UI components including welcome label,
     * navigation buttons for QandA, direct messages, back navigation, and application exit.
     * 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user The currently logged in user.
     */
    public void show(Stage primaryStage, User user) {
        this.currentUser = user;
        
        VBox layout = new VBox(15); // Add spacing between elements
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Label to display Hello user
        Label userLabel = new Label("Hello, " + user.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
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
        
        // Quit button
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        quitButton.setOnAction(e -> {
            databaseHelper.closeConnection();
            System.exit(0);
        });
        
        layout.getChildren().addAll(userLabel, qandaButton, directMessageButton, backButton, quitButton);
        Scene userScene = new Scene(layout, 800, 400);
        
        // Set the scene to primary stage
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Student Page");
        primaryStage.setOnCloseRequest(e -> {
            databaseHelper.closeConnection();
        });
    }
    
    /**
     * Opens the QandA Page for viewing and interacting with questions and answers.
     * Creates a new QandAPage instance, sets the current user, and transitions
     * the application to display the QandA interface.
     * 
     * @param primaryStage The primary stage where the QandA scene will be displayed.
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
     * Opens the Direct Message Page for communicating with other users.
     * Creates a new DirectMessage instance, sets the current user, and transitions
     * the application to display the direct messaging interface.
     * 
     * @param primaryStage The primary stage where the direct message scene will be displayed.
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