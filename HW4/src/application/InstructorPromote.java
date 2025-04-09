package application;

import databasePart1.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * InstructorPromote class represents a pop-up window that allows instructors
 * to promote or demote students by adding or removing the reviewer role.
 * This class handles the UI components and database operations for managing
 * student reviewer roles within the application.
 */
public class InstructorPromote {
    /** Database helper instance for performing database operations */
    private DatabaseHelper databaseHelper;
    
    /** The currently logged-in instructor user */
    private User currentUser;
    
    /** The stage for the popup window */
    private Stage popupStage;
    
    /** Dropdown for selecting students */
    private ComboBox<String> studentSelector;
    
    /** Label showing the current reviewer status of the selected student */
    private Label statusLabel;

    /**
     * Constructor for InstructorPromote.
     * 
     * @param databaseHelper Helper class for database operations
     * @param currentUser The currently logged in instructor user
     */
    public InstructorPromote(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    /**
     * Shows the promotion/demotion popup window.
     * Creates and displays a modal dialog with UI components for selecting
     * students and managing their reviewer roles.
     */
    public void show() {
        // Create a new stage for the popup
        popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
        popupStage.setTitle("Manage Student Reviewers");
        popupStage.setMinWidth(400);
        popupStage.setMinHeight(300);

        // Create UI components
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        // Title label
        Label titleLabel = new Label("Promote/Demote Students");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Information label
        Label infoLabel = new Label("Select a student to add or remove reviewer role");
        infoLabel.setStyle("-fx-font-size: 14px;");

        // Student selector
        studentSelector = new ComboBox<>();
        studentSelector.setPromptText("Select a student");
        studentSelector.setPrefWidth(250);
        
        // Load students
        loadStudents();

        // Current status label
        statusLabel = new Label("Current status: Not a reviewer");
        statusLabel.setStyle("-fx-font-size: 14px;");

        // Update status label when a student is selected
        studentSelector.setOnAction(e -> updateStatusLabel());

        // Buttons for promotion and demotion
        Button promoteButton = new Button("Promote to Reviewer");
        promoteButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        promoteButton.setOnAction(e -> promoteStudent());

        Button demoteButton = new Button("Remove Reviewer Role");
        demoteButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        demoteButton.setOnAction(e -> demoteStudent());

        // HBox for buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(promoteButton, demoteButton);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        closeButton.setOnAction(e -> popupStage.close());

        // Add all components to the layout
        layout.getChildren().addAll(
            titleLabel, 
            infoLabel, 
            studentSelector, 
            statusLabel, 
            buttonBox, 
            closeButton
        );

        // Create and set the scene
        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        
        // Show the popup
        popupStage.showAndWait();
    }

    /**
     * Loads all students into the student selector combobox.
     * Only includes users who have the "student" role but not "instructor" or "admin" roles.
     * This method filters the complete user list to display only eligible students.
     */
    private void loadStudents() {
        // Get all users with roles
        ObservableList<User> allUsers = databaseHelper.getAllUsersWithRoles();
        
        // Filter to only include students (who are not also instructors or admins)
        List<String> studentUsernames = new ArrayList<>();
        for (User user : allUsers) {
            if (user.hasRole("student") && 
                !user.hasRole("instructor") && 
                !user.hasRole("admin")) {
                studentUsernames.add(user.getUserName());
            }
        }
        
        // Update the ComboBox items
        studentSelector.setItems(FXCollections.observableArrayList(studentUsernames));
    }

    /**
     * Updates the status label based on the selected student.
     * Retrieves the selected student's information and checks if they have
     * the reviewer role, then updates the status label accordingly.
     */
    private void updateStatusLabel() {
        String selectedUsername = studentSelector.getValue();
        if (selectedUsername == null || selectedUsername.isEmpty()) {
            statusLabel.setText("Current status: No student selected");
            return;
        }

        // Get the user object
        ObservableList<User> allUsers = databaseHelper.getAllUsersWithRoles();
        for (User user : allUsers) {
            if (user.getUserName().equals(selectedUsername)) {
                boolean isReviewer = user.hasRole("reviewer");
                statusLabel.setText("Current status: " + 
                                   (isReviewer ? "Is a reviewer" : "Not a reviewer"));
                return;
            }
        }
    }

    /**
     * Promotes the selected student by adding the reviewer role.
     * Validates the selection, checks if the student already has the reviewer role,
     * and performs the database operation to add the role if needed.
     * Shows appropriate alerts for success, failure, or validation issues.
     */
    private void promoteStudent() {
        String selectedUsername = studentSelector.getValue();
        if (selectedUsername == null || selectedUsername.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Student Selected", 
                      "Please select a student to promote.");
            return;
        }

        // Check if already a reviewer
        ObservableList<User> allUsers = databaseHelper.getAllUsersWithRoles();
        for (User user : allUsers) {
            if (user.getUserName().equals(selectedUsername)) {
                if (user.hasRole("reviewer")) {
                    showAlert(Alert.AlertType.INFORMATION, "Already a Reviewer", 
                              "This student is already a reviewer.");
                    return;
                }
                break;
            }
        }

        // Add reviewer role
        boolean success = databaseHelper.addRoleToUser(selectedUsername, "reviewer");
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Student Promoted", 
                      "Successfully promoted " + selectedUsername + " to reviewer.");
            updateStatusLabel();
        } else {
            showAlert(Alert.AlertType.ERROR, "Promotion Failed", 
                      "Failed to promote student. Please try again.");
        }
    }

    /**
     * Demotes the selected student by removing the reviewer role.
     * Validates the selection, checks if the student currently has the reviewer role,
     * and performs the database operation to remove the role if needed.
     * Shows appropriate alerts for success, failure, or validation issues.
     */
    private void demoteStudent() {
        String selectedUsername = studentSelector.getValue();
        if (selectedUsername == null || selectedUsername.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Student Selected", 
                      "Please select a student to demote.");
            return;
        }

        // Check if actually a reviewer
        ObservableList<User> allUsers = databaseHelper.getAllUsersWithRoles();
        for (User user : allUsers) {
            if (user.getUserName().equals(selectedUsername)) {
                if (!user.hasRole("reviewer")) {
                    showAlert(Alert.AlertType.INFORMATION, "Not a Reviewer", 
                              "This student is not currently a reviewer.");
                    return;
                }
                break;
            }
        }

        // Remove reviewer role
        boolean success = databaseHelper.removeRoleFromUser(selectedUsername, "reviewer");
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Student Demoted", 
                      "Successfully removed reviewer role from " + selectedUsername + ".");
            updateStatusLabel();
        } else {
            showAlert(Alert.AlertType.ERROR, "Demotion Failed", 
                      "Failed to demote student. Please try again.");
        }
    }

    /**
     * Shows an alert dialog with the specified type, title, and message.
     * Creates and displays a JavaFX Alert dialog to inform the user about
     * operations results or required actions.
     * 
     * @param alertType The type of alert to show (e.g., information, warning, error)
     * @param title The title of the alert
     * @param message The message to display in the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}