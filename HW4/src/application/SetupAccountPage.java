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

import databasePart1.*;

import java.util.HashSet;
import java.util.Set;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 * This page is part of the account registration flow and interacts with the database
 * to validate inputs and create new user accounts.
 */
public class SetupAccountPage {
	
    /**
     * Database helper instance used for all database operations.
     */
    private final DatabaseHelper databaseHelper;
    
    /**
     * Constructor for the SetupAccountPage.
     * 
     * @param databaseHelper The database helper instance to handle database operations.
     */
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * Creates and configures the UI elements for the account setup form including:
     * - Input fields for username, password, and invitation code
     * - Submit, back, and quit buttons
     * - Event handlers for form submission and navigation
     * 
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Label headerLabel = new Label("Create a new account");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 10px; -fx-text-fill: DODGERBLUE;");
        
        Button setupButton = new Button("Sign up");
        Button backButton = new Button("Back");
        
        // Button to quit the application
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit(); // Exit the JavaFX application
        });
        
        // Add Enter key press handler to all three input fields
        userNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                setupButton.fire(); // Programmatically click the signup button
            }
        });
        
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                setupButton.fire(); // Programmatically click the signup button
            }
        });
        
        inviteCodeField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                setupButton.fire(); // Programmatically click the signup button
            }
        });
        
        /**
         * Event handler for the back button.
         * Navigates the user back to the login selection page.
         */
        backButton.setOnAction(a -> {
        	// Navigate to the SetLoginSelectioPage
        	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });
        
        /**
         * Event handler for the setup/signup button.
         * Performs the following actions:
         * 1. Retrieves and validates user input
         * 2. Checks if the username is available
         * 3. Validates the invitation code
         * 4. Creates a new user account if all validations pass
         * 5. Navigates to the welcome page upon successful registration
         * 6. Displays appropriate error messages if any validation fails
         */
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            
            try {
            	// Check if the user already exists
            	if(!databaseHelper.doesUserExist(userName)) {
            		
            		// Validate the invitation code
            		if(databaseHelper.validateInvitationCode(code)) {
            			
            			// Create a new user and register them in the database as a student
            			Set<String> roles = new HashSet<>();
            			roles.add("student");
		            	User user = new User(userName, password, roles);
		                databaseHelper.register(user);
		                
		                // Navigate to the Welcome Login Page
		                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            		}
            		else {
            			errorLabel.setText("Please enter a valid invitation code");
            		}
            	}
            	else {
            		errorLabel.setText("This username is taken!!.. Please use another to setup an account");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Create an HBox for the signup and back buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(backButton, setupButton);

        /**
         * Main layout container for the setup account page.
         * Organizes all UI components in a vertical box layout.
         */
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(
            headerLabel, 
            userNameField, 
            passwordField,
            inviteCodeField, 
            buttonBox,
            quitButton, 
            errorLabel
        );

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}