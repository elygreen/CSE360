package application;
import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FirstPage class represents the initial screen for the first user.
 * It prompts the user to set up administrator access and navigate to the setup screen.
 * This class is responsible for creating and displaying the first page UI elements.
 */
public class FirstPage {
    
    /**
     * Reference to the DatabaseHelper for database operations.
     * This helper provides methods for interacting with the application database.
     */
    private final DatabaseHelper databaseHelper;
    
    /**
     * Constructs a new FirstPage with the specified database helper.
     * 
     * @param databaseHelper The database helper object to handle database operations.
     */
    public FirstPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the first page in the provided primary stage.
     * 
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        // Create a vertical layout with 5 pixels spacing between elements
        VBox layout = new VBox(5);
        
        // Set layout alignment and padding
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        /**
         * Create and style the welcome label for the first user
         */
        Label userLabel = new Label("Hello..You are the first person here. \nPlease select continue to setup administrator access");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        /**
         * Create the continue button that navigates to the admin setup page
         */
        Button continueButton = new Button("Continue");
        
        // Define the action for the continue button
        continueButton.setOnAction(a -> {
            // Create and show the admin setup page when button is clicked
            new AdminSetupPage(databaseHelper).show(primaryStage);
        });
        
        // Add all UI elements to the layout
        layout.getChildren().addAll(userLabel, continueButton);
        
        // Create a new scene with the layout and dimensions
        Scene firstPageScene = new Scene(layout, 800, 400);
        
        // Configure and show the primary stage
        primaryStage.setScene(firstPageScene);
        primaryStage.setTitle("First Page");
        primaryStage.show();
    }
}