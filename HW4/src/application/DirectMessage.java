package application;

import databasePart1.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.Connection;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DirectMessage class represents the interface for viewing and interacting
 * with direct messages from other users.
 * <p>
 * This class provides functionality to view existing chats, start new conversations,
 * and manage direct message interactions between users in the application.
 */
public class DirectMessage {
    /** The currently logged-in user */
    private User currentUser;
    
    /** Helper for general database operations */
    private DatabaseHelper databaseHelper;
    
    /** Helper specifically for direct message related database operations */
    private DatabaseHelperDM dmHelper;
    
    /** The primary stage of the application */
    private Stage primaryStage;
    
    /** Observable list of chat items displayed in the UI */
    private ObservableList<ChatListItem> chatList;
    
    /** ListView component that displays the chat items */
    private ListView<ChatListItem> chatListView;
    
    /** Database ID of the current user */
    private int currentUserId;
    
    /**
     * Sets the current user for this direct message interface.
     * 
     * @param user The currently logged in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Creates and returns the scene for the Direct Message page.
     * <p>
     * This method initializes the database connections, sets up the UI components,
     * and configures event handlers for the direct message interface.
     * 
     * @param stage The primary stage of the application
     * @return The created scene, or null if an error occurs
     */
    public Scene createScene(Stage stage) {
        this.primaryStage = stage;
        this.databaseHelper = new DatabaseHelper();
        
        try {
            databaseHelper.connectToDatabase();
            // Create the direct message helper using the main database connection
            this.dmHelper = new DatabaseHelperDM(databaseHelper.getConnection());
            
            // Get current user ID
            currentUserId = dmHelper.getUserIdByName(currentUser.getUserName());
            if (currentUserId == -1) {
                showErrorMessage("User Error", "Could not find current user in database.");
                return null;
            }
        } catch (Exception e) {
            showErrorMessage("Database Error", "Could not connect to database: " + e.getMessage());
            return null;
        }
        
        // Main layout container
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        
        // Header
        Label headerLabel = new Label("Direct Messages");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        VBox headerBox = new VBox(10, headerLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        mainLayout.setTop(headerBox);
        
        // Chat list
        chatList = FXCollections.observableArrayList();
        chatListView = new ListView<>(chatList);
        chatListView.setPrefHeight(400);
        
        // Set custom cell factory for chat list items
        chatListView.setCellFactory(new Callback<ListView<ChatListItem>, ListCell<ChatListItem>>() {
            @Override
            public ListCell<ChatListItem> call(ListView<ChatListItem> param) {
                return new ChatListCell();
            }
        });
        
        // Add selection listener to chat list
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openChatDetail(newVal);
            }
        });
        
        // Load chats from database
        loadChats();
        
        // Center the chat list
        mainLayout.setCenter(chatListView);
        
        // Button container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(15, 0, 0, 0));
        
        // New Chat button
        Button newChatButton = new Button("New Chat");
        newChatButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        newChatButton.setOnAction(e -> showNewChatDialog());
        
        // Refresh button
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        refreshButton.setOnAction(e -> loadChats());
        
        // Back button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        backButton.setOnAction(e -> navigateToUserHomePage());
        
        // Quit button
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        quitButton.setOnAction(e -> {
            closeConnection();
            System.exit(0);
        });
        
        // Add buttons to container
        buttonContainer.getChildren().addAll(newChatButton, refreshButton, backButton, quitButton);
        mainLayout.setBottom(buttonContainer);
        
        // Create and return scene
        Scene scene = new Scene(mainLayout, 800, 600);
        return scene;
    }
    
    /**
     * Starts a new chat with a specific user or opens an existing chat.
     * <p>
     * This method first checks if a chat with the specified user already exists.
     * If it does, that chat is opened. Otherwise, a new chat is created.
     * 
     * @param username The username to start a chat with
     */
    public void startChatWith(String username) {
        // Ensure we're on the JavaFX application thread
        Platform.runLater(() -> {
            // Try to find an existing chat with this user
            boolean existingChatFound = false;
            
            // First refresh the chat list to ensure we have the latest data
            loadChats();
            
            // Look for existing chat with this user
            for (ChatListItem item : chatList) {
                if (item.getOtherUser().equals(username)) {
                    // Select this chat
                    chatListView.getSelectionModel().select(item);
                    existingChatFound = true;
                    break;
                }
            }
            
            // If no existing chat found, create a new one
            if (!existingChatFound) {
                createNewChat(username);
            }
        });
    }
    
    /**
     * Loads recent chats from the database into the chat list.
     * <p>
     * This method clears the current chat list, retrieves recent chats from the database,
     * and sorts them by unread status and timestamp (newest first).
     */
    private void loadChats() {
        chatList.clear();
        
        // Get recent chats for the current user (limit to 50 chats)
        Map<Integer, Map<String, Object>> recentChats = dmHelper.getRecentChats(currentUserId, 50);
        
        // Convert to list items and add to observable list
        for (Map.Entry<Integer, Map<String, Object>> entry : recentChats.entrySet()) {
            int chatId = entry.getKey();
            Map<String, Object> chatDetails = entry.getValue();
            
            String otherUser = (String) chatDetails.get("other_user");
            String lastMessage = (String) chatDetails.get("last_message");
            Timestamp updatedAt = (Timestamp) chatDetails.get("updated_at");
            int unreadCount = (Integer) chatDetails.get("unread_count");
            
            // Check if the user is a staff member
            boolean isStaff = isUserStaff(otherUser);
            
            ChatListItem item = new ChatListItem(chatId, otherUser, lastMessage, updatedAt, unreadCount > 0, isStaff);
            chatList.add(item);
        }
        
        // Sort by updated_at timestamp (newest first) and unread status
        chatList.sort((a, b) -> {
            // Unread messages always come first
            if (a.isUnread() && !b.isUnread()) {
                return -1;
            } else if (!a.isUnread() && b.isUnread()) {
                return 1;
            }
            // Then sort by timestamp
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
    }
    
    /**
     * Checks if a user has the staff role.
     * 
     * @param username The username to check
     * @return true if the user has the staff role, false otherwise
     */
    private boolean isUserStaff(String username) {
        Set<String> roles = databaseHelper.getUserRole(username);
        return roles.contains("staff");
    }
    
    /**
     * Opens a chat detail view for the selected chat.
     * <p>
     * This method creates and shows a new chat detail window for the selected chat,
     * and marks the chat as read in the list.
     * 
     * @param chatItem The selected chat item to open
     */
    private void openChatDetail(ChatListItem chatItem) {
        // Create and show the chat detail window
        ChatDetailWindow chatDetailWindow = new ChatDetailWindow(
            chatItem.getChatId(),
            currentUserId,
            currentUser.getUserName(),
            chatItem.getOtherUser(),
            dmHelper
        );
        
        // Show the window
        chatDetailWindow.show();
        
        // Mark as not unread in the list view
        chatItem.setUnread(false);
        
        // Refresh the list to update read status
        loadChats();
    }
    
    /**
     * Shows a dialog to create a new chat with another user.
     * <p>
     * This method displays a dialog that allows the user to select another user
     * from a dropdown or enter a username manually to start a new chat.
     */
    private void showNewChatDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Chat");
        dialog.setHeaderText("Start a new conversation");
        
        // Set button types
        ButtonType startButtonType = new ButtonType("Start Chat", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);
        
        // Create the layout grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        // Add a label for the dropdown
        Label dropdownLabel = new Label("Select a user:");
        grid.add(dropdownLabel, 0, 0);
        
        // Create the dropdown (ComboBox) of users
        ComboBox<String> userDropdown = new ComboBox<>();
        userDropdown.setPromptText("Select a user");
        userDropdown.setPrefWidth(300);
        grid.add(userDropdown, 1, 0);
        
        // Load all users into the dropdown
        loadUsersIntoDropdown(userDropdown);
        
        // Add a separator
        Separator separator = new Separator();
        separator.setPrefWidth(400);
        grid.add(separator, 0, 1, 2, 1);
        
        // Add a label for the text field
        Label textFieldLabel = new Label("Or enter a username:");
        grid.add(textFieldLabel, 0, 2);
        
        // Add the text field for manual entry
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username manually");
        grid.add(usernameField, 1, 2);
        
        // Make the dropdown selection update the text field
        userDropdown.setOnAction(e -> {
            String selectedUser = userDropdown.getValue();
            if (selectedUser != null) {
                usernameField.setText(selectedUser);
            }
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the dropdown by default
        Platform.runLater(() -> userDropdown.requestFocus());
        
        // Convert the result to a username when the start button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == startButtonType) {
                // Prioritize the text field value
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    return username;
                }
                // Fall back to dropdown if text field is empty
                return userDropdown.getValue();
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result != null && !result.isEmpty()) {
                createNewChat(result);
            }
        });
    }
    
    /**
     * Loads all users from the database into the dropdown.
     * <p>
     * This method retrieves all users from the database, excludes the current user,
     * and populates the dropdown with the sorted list of usernames.
     * 
     * @param userDropdown The ComboBox to populate with usernames
     */
    private void loadUsersIntoDropdown(ComboBox<String> userDropdown) {
        // Get all users from the database
        ObservableList<User> allUsers = databaseHelper.getAllUsersWithRoles();
        
        // Convert to list of usernames, excluding the current user
        ObservableList<String> usernames = FXCollections.observableArrayList();
        
        for (User user : allUsers) {
            String username = user.getUserName();
            // Don't add the current user to the list
            if (!username.equals(currentUser.getUserName())) {
                usernames.add(username);
            }
        }
        
        // Sort alphabetically
        FXCollections.sort(usernames);
        
        // Set the items in the dropdown
        userDropdown.setItems(usernames);
    }
    
    /**
     * Creates a new chat with another user.
     * <p>
     * This method checks if the specified user exists, creates a new chat or retrieves
     * an existing chat with that user, and selects it in the chat list.
     * 
     * @param otherUsername The username of the other user to chat with
     */
    private void createNewChat(String otherUsername) {
        // Check if the user exists
        int otherUserId = dmHelper.getUserIdByName(otherUsername);
        if (otherUserId == -1) {
            showErrorMessage("User Not Found", "Could not find user: " + otherUsername);
            return;
        }
        
        // Check if trying to chat with self
        if (otherUserId == currentUserId) {
            showErrorMessage("Invalid Action", "You cannot chat with yourself.");
            return;
        }
        
        // Create a new chat or get existing chat
        int chatId = dmHelper.createChat(currentUserId, otherUserId);
        if (chatId == -1) {
            showErrorMessage("Chat Error", "Could not create chat with user: " + otherUsername);
            return;
        }
        
        // Refresh the chat list
        loadChats();
        
        // Select the new chat in the list
        for (ChatListItem item : chatList) {
            if (item.getChatId() == chatId) {
                chatListView.getSelectionModel().select(item);
                break;
            }
        }
    }
    
    /**
     * Closes the database connection.
     * <p>
     * This method should be called when the application is closing
     * to properly release database resources.
     */
    public void closeConnection() {
        if (databaseHelper != null) {
            databaseHelper.closeConnection();
        }
    }
    
    /**
     * Navigates back to the appropriate home page based on user's role.
     * <p>
     * This method determines the user's role and navigates to the
     * corresponding home page.
     */
    private void navigateToUserHomePage() {
        if (currentUser == null) {
            showErrorMessage("Error", "Current user is not set.");
            return;
        }

        if (currentUser.hasRole("instructor")) {
            // Route to InstructorHomePage
            InstructorHomePage instructorPage = new InstructorHomePage();
            instructorPage.show(primaryStage, databaseHelper, currentUser);
        } else if (currentUser.hasRole("student")) {
            // Route to UserHomePage
            UserHomePage userPage = new UserHomePage();
            userPage.show(primaryStage, currentUser);
        } else if (currentUser.hasRole("reviewer")) {
        	// Route to UserHomePage
        	ReviewerHomePage reviewerPage = new ReviewerHomePage();
        	reviewerPage.show(primaryStage, currentUser);
        } else {
            // Default fallback - could show error or a generic page
            showErrorMessage("Error", "Unknown user role, can't navigate to appropriate page.");
        }
    }
    
    /**
     * Displays an error message dialog.
     * <p>
     * This method creates and shows an error alert dialog with the specified
     * title and message.
     * 
     * @param title The title of the error dialog
     * @param message The error message to display
     */
    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Inner class to represent a chat list item.
     * <p>
     * This class encapsulates information about a chat including the chat ID,
     * other user's username, last message, timestamp, unread status, and staff status.
     */
    private static class ChatListItem {
        /** The database ID of the chat */
        private final int chatId;
        
        /** The username of the other user in the chat */
        private final String otherUser;
        
        /** The text of the last message in the chat */
        private final String lastMessage;
        
        /** The timestamp of the last message */
        private final Timestamp timestamp;
        
        /** Whether the chat has unread messages */
        private boolean unread;
        
        /** Whether the other user is a staff member */
        private final boolean isStaff;
        
        /**
         * Constructs a new ChatListItem.
         * 
         * @param chatId The database ID of the chat
         * @param otherUser The username of the other user in the chat
         * @param lastMessage The text of the last message in the chat
         * @param timestamp The timestamp of the last message
         * @param unread Whether the chat has unread messages
         * @param isStaff Whether the other user is a staff member
         */
        public ChatListItem(int chatId, String otherUser, String lastMessage, Timestamp timestamp, boolean unread, boolean isStaff) {
            this.chatId = chatId;
            this.otherUser = otherUser;
            this.lastMessage = lastMessage;
            this.timestamp = timestamp;
            this.unread = unread;
            this.isStaff = isStaff;
        }
        
        /**
         * Gets the chat ID.
         * 
         * @return The database ID of the chat
         */
        public int getChatId() {
            return chatId;
        }
        
        /**
         * Gets the other user's username.
         * 
         * @return The username of the other user in the chat
         */
        public String getOtherUser() {
            return otherUser;
        }
        
        /**
         * Gets the last message text.
         * 
         * @return The text of the last message in the chat
         */
        public String getLastMessage() {
            return lastMessage;
        }
        
        /**
         * Gets the timestamp of the last message.
         * 
         * @return The timestamp of the last message
         */
        public Timestamp getTimestamp() {
            return timestamp;
        }
        
        /**
         * Checks if the chat has unread messages.
         * 
         * @return true if the chat has unread messages, false otherwise
         */
        public boolean isUnread() {
            return unread;
        }
        
        /**
         * Sets the unread status of the chat.
         * 
         * @param unread The new unread status
         */
        public void setUnread(boolean unread) {
            this.unread = unread;
        }
        
        /**
         * Checks if the other user is a staff member.
         * 
         * @return true if the other user is a staff member, false otherwise
         */
        public boolean isStaff() {
            return isStaff;
        }
    }
    
    /**
     * Custom cell renderer for chat list items.
     * <p>
     * This class defines how each chat item is displayed in the list view,
     * including formatting for unread messages and staff indicators.
     */
    private static class ChatListCell extends ListCell<ChatListItem> {
        /** Container for all elements in the cell */
        private final VBox container;
        
        /** Label for displaying the username */
        private final Label usernameLabel;
        
        /** Label for displaying the message preview */
        private final Label messageLabel;
        
        /** Label for displaying the timestamp */
        private final Label timestampLabel;
        
        /** Date formatter for the timestamp */
        private final SimpleDateFormat dateFormat;
        
        /**
         * Constructs a new ChatListCell.
         * <p>
         * Initializes and configures the layout and components for the cell.
         */
        public ChatListCell() {
            container = new VBox(3);
            container.setPadding(new Insets(10));
            
            usernameLabel = new Label();
            usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            messageLabel = new Label();
            messageLabel.setStyle("-fx-font-size: 12px;");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(350);
            
            timestampLabel = new Label();
            timestampLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999999;");
            
            container.getChildren().addAll(usernameLabel, messageLabel, timestampLabel);
            
            dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a");
        }
        
        /**
         * Updates the cell with data from the chat item.
         * <p>
         * This method is called by JavaFX to update the cell when it becomes visible
         * or when the data changes.
         * 
         * @param item The chat item to display
         * @param empty Whether the cell is empty
         */
        @Override
        protected void updateItem(ChatListItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                if (item.isStaff()) {
                    // Add a star for staff members
                    usernameLabel.setText("★ " + item.getOtherUser());
                    usernameLabel.setTextFill(Color.GREEN);
                } else {
                    usernameLabel.setText(item.getOtherUser());
                    usernameLabel.setTextFill(Color.BLACK);
                }
                
                String messagePreview = item.getLastMessage();
                if (messagePreview != null && messagePreview.length() > 50) {
                    messagePreview = messagePreview.substring(0, 47) + "...";
                }
                messageLabel.setText(messagePreview != null ? messagePreview : "(No messages)");
                
                timestampLabel.setText(dateFormat.format(item.getTimestamp()));
                
                // Set background color for unread messages
                if (item.isUnread()) {
                    setStyle("-fx-background-color: #e6f7ff;");
                    usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0066cc;");
                } else {
                    setStyle("");
                    
                    // Preserve the star and staff coloring when not unread
                    if (item.isStaff()) {
                        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: green;");
                    } else {
                        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    }
                }
                
                setGraphic(container);
            }
        }
    }
}