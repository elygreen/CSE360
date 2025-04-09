package application;
import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.HashSet;
import java.util.Set;

/**
 * AdminHomePage class represents the user interface for the admin user.
 * This page displays a welcome message for the admin and provides admin functions
 * including generating invitation codes, viewing users, and resetting the database.
 * The UI is organized into sections for different administrative functions.
 */
public class AdminHomePage {
    
    /**
     * Displays the admin page in the provided primary stage.
     * This method creates and configures the entire admin interface with sections for
     * invitation code generation, user management, role management, and database reset.
     * 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param user The current user who has admin privileges.
     */
    public void show(Stage primaryStage, DatabaseHelper databaseHelper, User user) {
        VBox layout = new VBox(15); // Added spacing between elements
        
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // label to display the welcome message for the admin with username
        Label adminLabel = new Label("Hello, " + user.getUserName());
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create a section for invitation code generation
        VBox inviteSection = new VBox(10);
        inviteSection.setAlignment(Pos.CENTER);
        
        // Button to generate the invitation code
        Button generateInviteButton = new Button("Generate Invitation Code");
        
        // Label to display the generated invitation code
        Label inviteCodeLabel = new Label("");
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
        
        // Set action for generate invite button
        generateInviteButton.setOnAction(a -> {
            // Generate the invitation code using the databaseHelper and set it to the label
            String invitationCode = databaseHelper.generateInvitationCode();
            inviteCodeLabel.setText(invitationCode);
        });
        
        // Add components to invite section
        inviteSection.getChildren().addAll(generateInviteButton, inviteCodeLabel);
        
        // Create a section for user management
        VBox userSection = new VBox(10);
        userSection.setAlignment(Pos.CENTER);
        
        Label userSectionLabel = new Label("User Management");
        userSectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Create TableView for users
        TableView<User> userTable = new TableView<>();
        userTable.setPrefHeight(200);
        userTable.setPrefWidth(600);
        
        // Define columns
        TableColumn<User, String> userNameCol = new TableColumn<>("Username");
        // Use explicit cell value factory instead of PropertyValueFactory
        userNameCol.setCellValueFactory(cellData -> {
            User userData = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(userData.getUserName());
        });
        userNameCol.setPrefWidth(200);
        
        TableColumn<User, String> rolesCol = new TableColumn<>("Roles");
        rolesCol.setCellValueFactory(data -> {
            User userData = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.join(", ", userData.getRole())
            );
        });
        rolesCol.setPrefWidth(400);
        
        // Add columns to table
        userTable.getColumns().addAll(userNameCol, rolesCol);
        
        // Load user data
        ObservableList<User> users = databaseHelper.getAllUsersWithRoles();
        userTable.setItems(users);
        
        // Add refresh button for user table
        Button refreshUserTableButton = new Button("Refresh User List");
        refreshUserTableButton.setOnAction(e -> {
            ObservableList<User> refreshedUsers = databaseHelper.getAllUsersWithRoles();
            userTable.setItems(refreshedUsers);
        });
        
        // Add components to user section
        userSection.getChildren().addAll(userSectionLabel, userTable, refreshUserTableButton);
        
        // Create a section for role management
        VBox roleManagementSection = new VBox(10);
        roleManagementSection.setAlignment(Pos.CENTER);
        roleManagementSection.setPadding(new javafx.geometry.Insets(10));
        roleManagementSection.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label roleManagementLabel = new Label("Role Management");
        roleManagementLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // ComboBox for selecting a user
        ComboBox<String> userSelector = new ComboBox<>();
        userSelector.setPromptText("Select a user");
        userSelector.setPrefWidth(200);

        // ComboBox for selecting a role to add/remove
        ComboBox<String> roleSelector = new ComboBox<>();
        roleSelector.getItems().addAll("admin", "instructor", "student", "reviewer", "staff");
        roleSelector.setPromptText("Select a role");
        roleSelector.setPrefWidth(200);

        // Label to show user's current roles
        Label currentRolesLabel = new Label("Current roles: ");
        currentRolesLabel.setStyle("-fx-font-size: 12px;");

        // Buttons for adding and removing roles
        Button addRoleButton = new Button("Add Role");
        addRoleButton.setDisable(true); // Initially disabled until both user and role are selected

        Button removeRoleButton = new Button("Remove Role");
        removeRoleButton.setDisable(true); // Initially disabled until both user and role are selected

        // HBox for buttons
        HBox roleButtonsBox = new HBox(10);
        roleButtonsBox.setAlignment(Pos.CENTER);
        roleButtonsBox.getChildren().addAll(addRoleButton, removeRoleButton);

        // Populate user selector with usernames from the table
        ObservableList<User> allUsers = databaseHelper.getAllUsersWithRoles();
        for (User u : allUsers) {
            userSelector.getItems().add(u.getUserName());
        }

        // Event handler for user selection
        userSelector.setOnAction(e -> {
            String selectedUser = userSelector.getValue();
            if (selectedUser != null) {
                // Find the user object
                User selectedUserObj = null;
                for (User u : allUsers) {
                    if (u.getUserName().equals(selectedUser)) {
                        selectedUserObj = u;
                        break;
                    }
                }
                
                if (selectedUserObj != null) {
                    // Update current roles label
                    currentRolesLabel.setText("Current roles: " + String.join(", ", selectedUserObj.getRole()));
                    
                    // Enable buttons if role is also selected
                    if (roleSelector.getValue() != null) {
                        addRoleButton.setDisable(false);
                        removeRoleButton.setDisable(false);
                    }
                }
            }
        });

        // Event handler for role selection
        roleSelector.setOnAction(e -> {
            if (userSelector.getValue() != null && roleSelector.getValue() != null) {
                addRoleButton.setDisable(false);
                removeRoleButton.setDisable(false);
            }
        });

        /**
         * Handles the add role button click event.
         * Validates that a reviewer role can only be added to users who already have the student role.
         * Shows appropriate alerts for success or failure.
         */
        addRoleButton.setOnAction(e -> {
            String selectedUser = userSelector.getValue();
            String selectedRole = roleSelector.getValue();
            
            if (selectedUser != null && selectedRole != null) {
                // Get the selected user object
                User selectedUserObj = null;
                for (User u : allUsers) {
                    if (u.getUserName().equals(selectedUser)) {
                        selectedUserObj = u;
                        break;
                    }
                }
                
                // Special handling for reviewer role - can only add to users who already have student role
                if (selectedRole.equals("reviewer") && selectedUserObj != null) {
                    if (!selectedUserObj.hasRole("student")) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Cannot add reviewer role to users who are not students. Please add the student role first.");
                        errorAlert.showAndWait();
                        return;
                    }
                }
                
                // Call database helper to add role
                boolean success = databaseHelper.addRoleToUser(selectedUser, selectedRole);
                
                if (success) {
                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Role Added");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Role '" + selectedRole + "' has been added to user '" + selectedUser + "'.");
                    successAlert.showAndWait();
                    
                    // Refresh user list and update current roles label
                    refreshUserData(databaseHelper, userTable, userSelector, selectedUser, currentRolesLabel);
                } else {
                    // Show error message
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to add role. The user may already have this role.");
                    errorAlert.showAndWait();
                }
            }
        });

        /**
         * Handles the remove role button click event.
         * Performs validation to ensure:
         * - Users always have at least one role
         * - Student role is not removed if user has reviewer role
         * - At least one admin remains in the system
         * Shows appropriate alerts for success or failure.
         */
        removeRoleButton.setOnAction(e -> {
            String selectedUser = userSelector.getValue();
            String selectedRole = roleSelector.getValue();
            
            if (selectedUser != null && selectedRole != null) {
                // First verify this won't leave the user without the student role
                User selectedUserObj = null;
                for (User u : allUsers) {
                    if (u.getUserName().equals(selectedUser)) {
                        selectedUserObj = u;
                        break;
                    }
                }
                
                if (selectedUserObj != null) {
                    Set<String> userRoles = selectedUserObj.getRole();
                    
                    // Check if removing the selected role would leave the user with no roles
                    if (userRoles.size() == 1 && userRoles.contains(selectedRole)) {
                        // Show error - can't remove the only role
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Cannot remove role. User must have at least one role.");
                        errorAlert.showAndWait();
                        return;
                    }
                    
                    // If removing "student" role, make sure user has at least one other role
                    if (selectedRole.equals("student") && !userRoles.stream().anyMatch(r -> !r.equals("student"))) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Cannot remove the 'student' role when it's the only role.");
                        errorAlert.showAndWait();
                        return;
                    }
                    
                    // If removing "student" role but the user has "reviewer" role, don't allow it
                    if (selectedRole.equals("student") && userRoles.contains("reviewer")) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Cannot remove the 'student' role from a user who has the 'reviewer' role. Please remove the 'reviewer' role first.");
                        errorAlert.showAndWait();
                        return;
                    }
                    
                    // Check if removing an admin role would leave the system without any admins
                    if (selectedRole.equals("admin") && userRoles.contains("admin")) {
                        // Count how many users have the admin role in total
                        int adminCount = 0;
                        for (User u : databaseHelper.getAllUsersWithRoles()) {
                            if (u.getRole().contains("admin")) {
                                adminCount++;
                            }
                        }
                        
                        // If there's only one admin and we're trying to remove it, prevent the action
                        if (adminCount <= 1) {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Cannot remove the admin role. The system must have at least one administrator.");
                            errorAlert.showAndWait();
                            return;
                        }
                    }
                    
                    // Call database helper to remove role
                    boolean success = databaseHelper.removeRoleFromUser(selectedUser, selectedRole);
                    
                    if (success) {
                        // Show success message
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Role Removed");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Role '" + selectedRole + "' has been removed from user '" + selectedUser + "'.");
                        successAlert.showAndWait();
                        
                        // Refresh user list and update current roles label
                        refreshUserData(databaseHelper, userTable, userSelector, selectedUser, currentRolesLabel);
                    } else {
                        // Show error message
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Failed to remove role. The user may not have this role.");
                        errorAlert.showAndWait();
                    }
                }
            }
        });

        // Add components to role management section
        roleManagementSection.getChildren().addAll(
            roleManagementLabel, 
            userSelector, 
            currentRolesLabel, 
            roleSelector, 
            roleButtonsBox
        );

        // Add role management section to user section
        userSection.getChildren().add(roleManagementSection);
        
        /**
         * Creates a Reset Database button with warning styling.
         * When clicked, shows a confirmation dialog and performs the reset if confirmed.
         * After successful reset, the application is closed.
         */
        Button resetDatabaseButton = new Button("Reset Database");
        resetDatabaseButton.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white;");
        resetDatabaseButton.setOnAction(e -> {
            // Create confirmation dialog
            Alert confirmDialog = new Alert(Alert.AlertType.WARNING);
            confirmDialog.setTitle("Reset Database");
            confirmDialog.setHeaderText("WARNING: You are about to reset the entire database");
            confirmDialog.setContentText("This action will permanently delete ALL data including users, questions, and answers. " +
                                        "This action cannot be undone. The application will close and you will need to restart it. " +
                                        "Are you sure you want to proceed?");
            
            // Add custom buttons
            ButtonType yesButton = new ButtonType("Yes, Reset Database", ButtonBar.ButtonData.YES);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmDialog.getButtonTypes().setAll(cancelButton, yesButton);
            
            // Show dialog and wait for response
            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    // Perform database reset
                    boolean success = databaseHelper.resetDatabase();
                    
                    if (success) {
                        // Show success message
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Database Reset");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Database has been successfully reset. The application will now close. Please restart the application.");
                        
                        // Show alert and then exit when it's closed
                        successAlert.showAndWait().ifPresent(r -> {
                            // Close database connection
                            databaseHelper.closeConnection();
                            // Exit the JavaFX application
                            Platform.exit();
                        });
                    } else {
                        // Show error message
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("An error occurred while resetting the database. Please try again.");
                        errorAlert.showAndWait();
                    }
                }
            });
        });
        
        /**
         * Back button to return to the previous page.
         * When clicked, navigates to the WelcomeLoginPage.
         */
        Button backButton = new Button("Back");  
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        backButton.setOnAction(a -> {
            // Navigate to the WelcomeLoginPage
            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        });
        
        /**
         * Quit button to exit the application.
         * When clicked, closes the database connection and exits the application.
         */
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        quitButton.setOnAction(e -> {
            databaseHelper.closeConnection();
            System.exit(0);
        });
        
        // Create a horizontal box for the admin action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(resetDatabaseButton);
        
        // Create a horizontal box for navigation buttons
        HBox navButtonBox = new HBox(10);
        navButtonBox.setAlignment(Pos.CENTER);
        navButtonBox.getChildren().addAll(backButton, quitButton);
        
        // Add all components to the main layout
        layout.getChildren().addAll(adminLabel, inviteSection, userSection, buttonBox, navButtonBox);
        
        Scene adminScene = new Scene(layout, 800, 600); // Increased height to accommodate the table
        // Set the scene to primary stage
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Dashboard - Logged in as: " + user.getUserName());
    }
    
    /**
     * Helper method to refresh the user data after role changes.
     * Updates both the user table view and the current roles label for the selected user.
     * 
     * @param databaseHelper The database helper instance for retrieving updated user data
     * @param userTable The table view to refresh with updated user data
     * @param userSelector The user dropdown to refresh
     * @param selectedUserName The currently selected username to focus after refresh
     * @param currentRolesLabel The label showing current roles to update with the latest roles
     */
    private void refreshUserData(DatabaseHelper databaseHelper, TableView<User> userTable, 
                                ComboBox<String> userSelector, String selectedUserName, 
                                Label currentRolesLabel) {
        // Refresh user table
        ObservableList<User> refreshedUsers = databaseHelper.getAllUsersWithRoles();
        userTable.setItems(refreshedUsers);
        
        // Update current roles label for the selected user
        for (User u : refreshedUsers) {
            if (u.getUserName().equals(selectedUserName)) {
                currentRolesLabel.setText("Current roles: " + String.join(", ", u.getRole()));
                break;
            }
        }
    }
}