package application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 * <p>
 * This class handles role-based UI generation, showing only the navigation options that are
 * relevant to the authenticated user's assigned roles.
 * </p>
 */
public class WelcomeLoginPage {
    
    /**
     * The database helper instance used for database operations and connection management.
     */
    private final DatabaseHelper databaseHelper;
    
    /**
     * Constructs a new WelcomeLoginPage with the specified database helper.
     *
     * @param databaseHelper The database helper instance to use for database operations.
     */
    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the welcome page UI tailored to the authenticated user's roles.
     * <p>
     * This method creates a JavaFX scene with navigation buttons based on the user's roles.
     * Each button navigates to a different section of the application. The method also
     * provides options to logout or quit the application entirely.
     * </p>
     *
     * @param primaryStage The primary stage of the JavaFX application.
     * @param user The authenticated user object containing role information.
     */
    public void show(Stage primaryStage, User user) {
        
        VBox layout = new VBox(5);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label welcomeLabel = new Label("Welcome!!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Only add the admin button if the user has admin role
        if(user.hasRole("admin")) {
            Button adminButton = new Button("Go to Admin Page");
            adminButton.setOnAction(a -> {
                new AdminHomePage().show(primaryStage, databaseHelper, user);
            });
            layout.getChildren().add(adminButton);
        }
        
        // Only add the user button if the user has user role
        if(user.hasRole("user")) {
            Button userButton = new Button("Go to User Page");
            userButton.setOnAction(a -> {
                new UserHomePage().show(primaryStage, user);
            });
            layout.getChildren().add(userButton);
        }
        
        // Only add the student button if the user has student role
        if(user.hasRole("student")) {
            Button studentButton = new Button("Go to Student Page");
            studentButton.setOnAction(a -> {
                new UserHomePage().show(primaryStage, user);
            });
            layout.getChildren().add(studentButton);
        }
        
        // Only add the reviewer button if the user has reviewer role
        if(user.hasRole("reviewer")) {
            Button reviewerButton = new Button("Go to Reviewer Page");
            reviewerButton.setOnAction(a -> {
                new ReviewerHomePage().show(primaryStage, user);
            });
            layout.getChildren().add(reviewerButton);
        }
        
        // Only add the instructor button if the user has instructor role
        if(user.hasRole("instructor")) {
            Button instructorButton = new Button("Go to Instructor Page");
            instructorButton.setOnAction(a -> {
                new InstructorHomePage().show(primaryStage, databaseHelper, user);
            });
            layout.getChildren().add(instructorButton);
        }
        
        if(user.hasRole("staff")) {
            Button staffButton = new Button("Go to Staff Page");
            staffButton.setOnAction(a -> {
                new StaffHomePage().show(primaryStage, databaseHelper, user);
            });
            layout.getChildren().add(staffButton);
        }
        
        // Button to logout and return to login page
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            // Create and show the login page
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });
        
        // Button to quit the application
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit(); // Exit the JavaFX application
        });
        
        // Add all components to the layout
        layout.getChildren().addAll(welcomeLabel, logoutButton, quitButton);
        Scene welcomeScene = new Scene(layout, 800, 400);
        // Set the scene to primary stage
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}