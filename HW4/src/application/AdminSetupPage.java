package application;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The AdminSetupPage class handles the setup process for creating an administrator account.
 * This class provides a graphical user interface for the initial system setup where
 * the first user can create an administrator account with appropriate credentials.
 * 
 * The page contains form fields for username and password entry, and handles
 * the registration of the admin user in the database.
 */
public class AdminSetupPage {
    
    /**
     * A helper object that provides database operations for user management.
     */
    private final DatabaseHelper databaseHelper;
    
    /**
     * Constructs a new AdminSetupPage with the specified database helper.
     * 
     * @param databaseHelper The database helper instance for handling user registration
     */
    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the administrator setup form in the provided stage.
     * 
     * This method creates and configures the UI components for the admin setup page,
     * including username and password fields, and a setup button. It also handles
     * the user registration process when the setup button is clicked.
     * 
     * @param primaryStage The JavaFX stage where the admin setup interface will be displayed
     */
    public void show(Stage primaryStage) {
        // Add a header label with blue styling (same as in SetupAccountPage)
        Label headerLabel = new Label("Administrator Account Setup");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 10px; -fx-text-fill: DODGERBLUE;");
        
        // Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        Button setupButton = new Button("Setup");
        
        // Add Enter key press handler to username field
        userNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                setupButton.fire(); // Programmatically click the setup button
            }
        });
        
        // Add Enter key press handler to password field
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                setupButton.fire(); // Programmatically click the setup button
            }
        });
        
        /**
         * Sets up an action handler for the setup button.
         * When clicked, it retrieves the username and password,
         * creates a new User object with admin role, and registers it in the database.
         * On success, navigates to the Welcome Login Page.
         */
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            try {
                Set<String> roles = new HashSet<>();
                roles.add("admin");
                User user = new User(userName, password, roles);
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Include the headerLabel in the layout
        VBox layout = new VBox(10, headerLabel, userNameField, passwordField, setupButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}