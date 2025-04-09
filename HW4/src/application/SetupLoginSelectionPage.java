package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.application.Platform;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import databasePart1.*;

/**
 * The SetupLoginSelectionPage class serves as the entry point to the application's authentication system.
 * It provides a login interface with an option to navigate to the account setup page for new users.
 * This class handles user authentication and validates credentials against the database.
 */
public class SetupLoginSelectionPage {
    
    /**
     * Database helper instance used for database operations like user validation and role retrieval.
     */
    private final DatabaseHelper databaseHelper;

    /**
     * Constructs a new SetupLoginSelectionPage with the specified database helper.
     * 
     * @param databaseHelper The database helper instance used for database operations
     */
    public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the login selection page in the provided stage.
     * The page contains input fields for username and password, along with buttons
     * to login, create a new account, or quit the application.
     * 
     * @param primaryStage The primary stage to display the login page
     */
    public void show(Stage primaryStage) {    
    	// Input fields for the user's userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter username");
        userNameField.setMaxWidth(250);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        // Header for the login page
        Label headerLabel = new Label("User Login");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: DODGERBLUE;");
        
        // Login button to submit credentials
        Button loginButton = new Button("Login");
        
        // Button to navigate to the account setup page
        Button setupButton = new Button("Create New Account");
        
        // Button to quit the application
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit(); // Exit the JavaFX application
        });
        
        // Add Enter key press handler to both the username and password fields after loginButton is created
        userNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire(); // Programmatically click the login button
            }
        });
        
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire(); // Programmatically click the login button
            }
        });
        
        // Setup the action for the setup button to navigate to the SetupAccountPage
        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper).show(primaryStage);
        });
        
        // Setup the action for the login button to validate credentials and login
        loginButton.setOnAction(a -> {
            // Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            
            try {
                User user = new User(userName, password, new HashSet<>());
                WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
                
                // Retrieve the user's role from the database using userName
                Set<String> roles = databaseHelper.getUserRole(userName);
                
                // Check if user has roles (exists in the system)
                if(!roles.isEmpty()) {
                    // Add roles of the user
                    user.getRole().addAll(roles);
                    
                    // Check if the user is banned
                    if(databaseHelper.isUserBanned(userName)) {
                        // Display an error if the user is banned
                        errorLabel.setText("Your account has been banned. Please contact an administrator.");
                        return;
                    }
                    
                    // Attempt to log in with the provided credentials
                    if(databaseHelper.login(user)) {
                        welcomeLoginPage.show(primaryStage, user);
                    }
                    else {
                        // Display an error if the login fails
                        errorLabel.setText("Error logging in");
                    }
                }
                else {
                    // Display an error if the account does not exist
                    errorLabel.setText("User account doesn't exist");
                }
                
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            } 
        });
        
        // Create an HBox to place the login and setup buttons side by side
        HBox buttonBox = new HBox(10); // 10 pixels spacing between buttons
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(setupButton, loginButton); // Setup button on left, login on right
        
        // Create and configure the layout
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(
            headerLabel, 
            userNameField, 
            passwordField,
            buttonBox,     // Add the HBox containing the two buttons
            quitButton,    // Quit button below the other buttons
            errorLabel
        );

        // Configure and display the scene
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}