package application;
import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * StaffHomePage class represents the user interface for staff users. 
 * This page displays a dashboard for staff interactions, including navigation to QandA sections,
 * direct messages, and user management functionality for banning users.
 */
public class StaffHomePage {
    /** Database helper for executing database operations */
    private DatabaseHelper databaseHelper;
    
    /** The currently logged in user (staff) */
    private User currentUser;
    
    /**
     * Displays the staff page in the provided primary stage.
     * Creates the UI layout with various navigation buttons and displays welcome message.
     * Also includes user management functionality for banning users.
     * 
     * @param primaryStage The primary stage where the scene will be displayed
     * @param databaseHelper Helper class for database operations
     * @param user The currently logged in staff user
     */
    public void show(Stage primaryStage, DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.currentUser = user;
        
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        //welcome message
        Label staffLabel = new Label("Hello, Staff " + user.getUserName() + "!");
        staffLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        //QandA page button
        Button qandaButton = new Button("View Questions and Answers");
        qandaButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        qandaButton.setOnAction(e -> {
            openQandAPage(primaryStage);
        });
        
        //DM button
        Button directMessageButton = new Button("Direct Messages");
        directMessageButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        directMessageButton.setOnAction(e -> {
            openDirectMessagePage(primaryStage);
        });
        
        //Ban page button
        Label userManagementLabel = new Label("User Management");
        userManagementLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ComboBox<String> userSelector = new ComboBox<>();
        userSelector.setPromptText("Select a user");
        userSelector.setPrefWidth(250);
        
        databaseHelper.ensureBanColumnExists();
        
        // Get all users with ban status
        Map<String, Boolean> userBanStatus = databaseHelper.getAllUsersWithBanStatus();
        List<String> userList = new ArrayList<>(userBanStatus.keySet());
        userSelector.setItems(FXCollections.observableArrayList(userList));
        
        //display current ban status
        Label banStatusLabel = new Label("User status: ");
        banStatusLabel.setStyle("-fx-font-size: 12px;");
        
        // banning buttons
        Button banButton = new Button("Ban User");
        banButton.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white;");
        banButton.setDisable(true);
        
        Button unbanButton = new Button("Unban User");
        unbanButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        unbanButton.setDisable(true);
        
        // Create HBox for the ban/unban buttons
        HBox banButtonsBox = new HBox(10);
        banButtonsBox.setAlignment(Pos.CENTER);
        banButtonsBox.getChildren().addAll(banButton, unbanButton);
        
        // Update status label and enable/disable buttons when user is selected
        userSelector.setOnAction(e -> {
            String selectedUser = userSelector.getValue();
            if (selectedUser != null) {
                boolean isBanned = userBanStatus.getOrDefault(selectedUser, false);
                banStatusLabel.setText("User status: " + (isBanned ? "BANNED" : "Active"));

                if (selectedUser.equals(currentUser.getUserName())) {
                    banButton.setDisable(true);
                    unbanButton.setDisable(true);
                    banStatusLabel.setText("User status: Active (Cannot modify your own account)");
                } else {
                    banButton.setDisable(isBanned);
                    unbanButton.setDisable(!isBanned);
                }
            }
        });
        
        // Ban func
        banButton.setOnAction(e -> {
            String selectedUser = userSelector.getValue();
            if (selectedUser != null) {
                //confirmation dialog
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Ban User");
                confirmAlert.setHeaderText("You are about to ban user: " + selectedUser);
                confirmAlert.setContentText("This will prevent the user from logging in. Are you sure?");
                
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        boolean success = databaseHelper.banUser(selectedUser);
                        if (success) {
                            userBanStatus.put(selectedUser, true);
                            banStatusLabel.setText("User status: BANNED");
                            banButton.setDisable(true);
                            unbanButton.setDisable(false);
                            showInfoMessage("Success", "User " + selectedUser + " has been banned.");
                        }
                        else {
                            showErrorMessage("Error", "Failed to ban user. Please try again.");
                        }
                    }
                });
            }
        });
        
        // Unban func
        unbanButton.setOnAction(e -> {
            String selectedUser = userSelector.getValue();
            if (selectedUser != null) {
                //confirmation dialog
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Unban User");
                confirmAlert.setHeaderText("You are about to unban user: " + selectedUser);
                confirmAlert.setContentText("This will allow the user to log in again. Are you sure?"); 
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        boolean success = databaseHelper.unbanUser(selectedUser);
                        if (success) {
                            userBanStatus.put(selectedUser, false);
                            banStatusLabel.setText("User status: Active");
                            banButton.setDisable(false);
                            unbanButton.setDisable(true);
                            showInfoMessage("Success", "User " + selectedUser + " has been unbanned.");
                        }
                        else {
                            showErrorMessage("Error", "Failed to unban user. Please try again.");
                        }
                    }
                });
            }
        });
        
        // Refresh button
        Button refreshButton = new Button("Refresh User List");
        refreshButton.setOnAction(e -> {
            databaseHelper.ensureBanColumnExists();
            Map<String, Boolean> updatedUserBanStatus = databaseHelper.getAllUsersWithBanStatus();
            List<String> updatedUserList = new ArrayList<>(updatedUserBanStatus.keySet());
            userSelector.setItems(FXCollections.observableArrayList(updatedUserList));
            userSelector.getSelectionModel().clearSelection();
            banStatusLabel.setText("User status: ");
            banButton.setDisable(true);
            unbanButton.setDisable(true);
        });
        
        VBox userManagementBox = new VBox(10);
        userManagementBox.setAlignment(Pos.CENTER);
        userManagementBox.setPadding(new Insets(10));
        userManagementBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 10;");
        userManagementBox.getChildren().addAll(
            userManagementLabel,
            userSelector,
            banStatusLabel,
            banButtonsBox,
            refreshButton
        );
        
     // Sensitive Content Management Button
        Button sensitiveContentButton = new Button("Manage Sensitive Content");
        sensitiveContentButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-color: #FF9800; -fx-text-fill: white;");
        sensitiveContentButton.setOnAction(e -> {
            StaffSensitiveContentManager sensitiveManager = new StaffSensitiveContentManager(databaseHelper, currentUser, primaryStage);
            sensitiveManager.show();
        });
        
        // Navigation buttons
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        backButton.setOnAction(a -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        });
      
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        quitButton.setOnAction(e -> {
            databaseHelper.closeConnection();
            System.exit(0);
        });
        

        HBox navButtonBox = new HBox(10);
        navButtonBox.setAlignment(Pos.CENTER);
        navButtonBox.getChildren().addAll(backButton, quitButton);
        
        // Add all components to the main layout
        layout.getChildren().addAll(
            staffLabel, 
            qandaButton, 
            directMessageButton, 
            userManagementBox,
            sensitiveContentButton,
            navButtonBox
        );
        
        Scene staffScene = new Scene(layout, 800, 600); // Increased height for the new components
        
        // Set the scene to primary stage
        primaryStage.setScene(staffScene);
        primaryStage.setTitle("Staff Page");
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
        
        // Open in the same window (replacing the current view)
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
    
    /**
     * Displays an information message dialog with the specified title and content.
     * 
     * @param title The title of the information dialog
     * @param content The content of the information dialog
     */
    private void showInfoMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Displays an error message dialog with the specified title and content.
     * 
     * @param title The title of the error dialog
     * @param content The content of the error dialog
     */
    private void showErrorMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}