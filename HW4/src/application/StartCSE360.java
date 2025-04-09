package application;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.DatabaseHelper;

/**
 * Main application class that serves as the entry point for the CSE360 application.
 * This class handles the initial startup sequence, database connection,
 * and directs the application flow to the appropriate first screen based on
 * database state.
 * 
 * @author CSE360 Team
 * @version 1.0
 */
public class StartCSE360 extends Application {
    /**
     * Database helper instance used for all database operations throughout the application.
     * This is a singleton instance shared across the application.
     */
    private static final DatabaseHelper databaseHelper = new DatabaseHelper();
    
    /**
     * The main entry point for the application.
     * Launches the JavaFX application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * JavaFX application start method that initializes the application.
     * Connects to the database and determines the first screen to display based on
     * whether the database is empty or contains data.
     * 
     * @param primaryStage The primary stage for this application
     * @throws SQLException If a database access error occurs during connection or query execution
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            if (databaseHelper.isDatabaseEmpty()) {
                // If database is empty, show the first-time setup page
                new FirstPage(databaseHelper).show(primaryStage);
            } else {
                // If database contains data, show the login selection page
            	
                if (databaseHelper.doesUserExist("student"))
                	databaseHelper.deleteUser("student");
                if (databaseHelper.doesUserExist("reviewer"))
                	databaseHelper.deleteUser("reviewer");
                if (databaseHelper.doesUserExist("instructor"))
                	databaseHelper.deleteUser("instructor");
                
                //////////////////////////////////////
                // COMMENT THIS OUT IF DONE TESTING //
                //////////////////////////////////////
                
                databaseHelper.displayUsernamesAndRoles();
                
                
                
                new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}