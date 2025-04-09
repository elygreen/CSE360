package application;

import databasePart1.DatabaseHelperDM;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * ChatDetailWindow class displays the detailed view of a chat conversation,
 * allowing users to view message history and send new messages.
 * <p>
 * This class provides a graphical user interface for a messaging application where
 * users can view their conversation history with another user and send new messages.
 * The window includes a header with user information, a scrollable message area,
 * and an input area for composing and sending messages.
 * </p>
 */
public class ChatDetailWindow {
    
    /**
     * The stage for displaying the chat detail dialog.
     */
    private Stage dialogStage;
    
    /**
     * The unique identifier for the current chat conversation.
     */
    private int chatId;
    
    /**
     * The unique identifier for the current user.
     */
    private int currentUserId;
    
    /**
     * The display name of the current user.
     */
    private String currentUserName;
    
    /**
     * The display name of the other participant in the conversation.
     */
    private String otherUserName;
    
    /**
     * Database helper for direct messaging operations.
     */
    private DatabaseHelperDM dmHelper;
    
    /**
     * Container for displaying all message bubbles in the conversation.
     */
    private VBox messagesContainer;
    
    /**
     * Text field for entering new messages.
     */
    private TextField messageInput;
    
    /**
     * Formatter for displaying message timestamps in a readable format.
     */
    private SimpleDateFormat timeFormat;
    
    /**
     * Constructor for the ChatDetailWindow.
     * <p>
     * Initializes the window with the chat information and marks all unread messages
     * as read when the window is opened.
     * </p>
     * 
     * @param chatId The unique identifier of the chat conversation to display
     * @param currentUserId The unique identifier of the current user
     * @param currentUserName The display name of the current user
     * @param otherUserName The display name of the other participant
     * @param dmHelper The database helper for direct messaging operations
     */
    public ChatDetailWindow(int chatId, int currentUserId, String currentUserName, 
                           String otherUserName, DatabaseHelperDM dmHelper) {
        this.chatId = chatId;
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;
        this.otherUserName = otherUserName;
        this.dmHelper = dmHelper;
        this.timeFormat = new SimpleDateFormat("h:mm a");
        
        // Mark messages as read as soon as the window opens
        dmHelper.markMessagesAsRead(chatId, currentUserId);
    }
    
    /**
     * Shows the chat detail window.
     * <p>
     * Creates and displays the dialog window with all UI components including
     * the header, message area, and input area. Loads existing messages and
     * automatically scrolls to the most recent message.
     * </p>
     */
    public void show() {
        // Create the stage
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Chat with " + otherUserName);
        dialogStage.setMinWidth(600);
        dialogStage.setMinHeight(500);
        
        // Create the main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        
        // Create the header
        HBox header = createHeader();
        mainLayout.setTop(header);
        
        // Create scrollable message area
        ScrollPane scrollPane = createMessageArea();
        mainLayout.setCenter(scrollPane);
        
        // Create the input area
        HBox inputArea = createInputArea();
        mainLayout.setBottom(inputArea);
        
        // Load messages
        loadMessages();
        
        // Create the scene and show the stage
        Scene scene = new Scene(mainLayout, 600, 500);
        dialogStage.setScene(scene);
        dialogStage.show();
        
        
        
        // Auto-scroll to bottom when messages load
        Platform.runLater(() -> {
            scrollPane.setVvalue(1.0);
        });
    }
    
    /**
     * Creates the header area with user information.
     * <p>
     * The header displays the name of the other participant in the conversation.
     * </p>
     * 
     * @return An HBox containing the header components
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        Label titleLabel = new Label("Conversation with " + otherUserName);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        header.getChildren().add(titleLabel);
        return header;
    }
    
    /**
     * Creates the scrollable message area.
     * <p>
     * This area displays all messages in the conversation and allows scrolling
     * through message history.
     * </p>
     * 
     * @return A ScrollPane containing the message container
     */
    private ScrollPane createMessageArea() {
        messagesContainer = new VBox(10);
        messagesContainer.setPadding(new Insets(10, 0, 10, 0));
        
        ScrollPane scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(5));
        
        return scrollPane;
    }
    
    /**
     * Creates the input area for sending messages.
     * <p>
     * This area contains a text field for typing messages and a send button.
     * Messages can be sent by clicking the send button or pressing Enter.
     * </p>
     * 
     * @return An HBox containing the input field and send button
     */
    private HBox createInputArea() {
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setPadding(new Insets(10, 0, 0, 0));
        inputArea.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefHeight(40);
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        
        // Allow sending message with Enter key
        messageInput.setOnAction(e -> sendMessage());
        
        Button sendButton = new Button("Send");
        sendButton.setPrefHeight(40);
        sendButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        sendButton.setOnAction(e -> sendMessage());
        
        inputArea.getChildren().addAll(messageInput, sendButton);
        return inputArea;
    }
    
    /**
     * Loads and displays messages for the current chat.
     * <p>
     * Retrieves messages from the database and creates UI elements to display them.
     * Messages sent by the current user are displayed on the right side with a green
     * background, while messages from the other participant are displayed on the left
     * with a white background. Each message includes the content, time sent, and read status.
     * </p>
     */
    private void loadMessages() {
        messagesContainer.getChildren().clear();
        
        List<Map<String, Object>> messages = dmHelper.getMessagesForChat(chatId);
        
        for (Map<String, Object> messageData : messages) {
            int senderId = (int) messageData.get("sender_id");
            String senderName = (String) messageData.get("sender_name");
            String content = (String) messageData.get("content");
            Timestamp timestamp = (Timestamp) messageData.get("timestamp");
            boolean isRead = (boolean) messageData.get("is_read");
            
            // Create a message bubble
            HBox messageRow = new HBox(10);
            
            // Align based on sender (current user's messages on right, others on left)
            boolean isCurrentUser = (senderId == currentUserId);
            if (isCurrentUser) {
                messageRow.setAlignment(Pos.CENTER_RIGHT);
            } else {
                messageRow.setAlignment(Pos.CENTER_LEFT);
            }
            
            // Create the message content
            VBox messageBubble = new VBox(2);
            messageBubble.setPadding(new Insets(10));
            messageBubble.setMaxWidth(400);
            
            // Set different styles for sent vs received messages
            if (isCurrentUser) {
                messageBubble.setStyle("-fx-background-color: #DCF8C6; -fx-background-radius: 10;" +
                                      "-fx-border-radius: 10; -fx-border-color: #c5e1a5;");
            } else {
                messageBubble.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
                                      "-fx-border-radius: 10; -fx-border-color: #E0E0E0;");
            }
            
            // Message text
            Label messageText = new Label(content);
            messageText.setWrapText(true);
            
            // Time and status info
            HBox infoBox = new HBox(5);
            infoBox.setAlignment(isCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            
            Label timeLabel = new Label(timeFormat.format(timestamp));
            timeLabel.setFont(Font.font("System", 10));
            timeLabel.setTextFill(Color.GRAY);
            
            infoBox.getChildren().add(timeLabel);
            
            // For sent messages, add read status
            if (isCurrentUser) {
                Label statusLabel = new Label(isRead ? "Read" : "Delivered");
                statusLabel.setFont(Font.font("System", 10));
                statusLabel.setTextFill(isRead ? Color.GRAY : Color.DARKGRAY);
                infoBox.getChildren().add(statusLabel);
            }
            
            messageBubble.getChildren().addAll(messageText, infoBox);
            messageRow.getChildren().add(messageBubble);
            
            messagesContainer.getChildren().add(messageRow);
        }
        
        // Add a spacer at the end to ensure messages don't get hidden behind the input area
        Region spacer = new Region();
        spacer.setPrefHeight(10);
        messagesContainer.getChildren().add(spacer);
    }
    
    /**
     * Sends a new message.
     * <p>
     * Retrieves the message text from the input field, sends it to the database,
     * and updates the UI. If the message is sent successfully, the input field is
     * cleared, messages are reloaded, and the view scrolls to the new message.
     * If the message fails to send, an error dialog is displayed.
     * </p>
     */
    private void sendMessage() {
        String message = messageInput.getText().trim();
        
        if (!message.isEmpty()) {
            // Send the message
            int result = dmHelper.sendMessage(chatId, currentUserId, message);
            
            if (result > 0) {
                // Clear the input field
                messageInput.clear();
                
                // Reload the messages
                loadMessages();
                
                // Scroll to the bottom
                Platform.runLater(() -> {
                    ScrollPane scrollPane = (ScrollPane) messagesContainer.getParent();
                    scrollPane.setVvalue(1.0);
                });
            } else {
                showErrorMessage("Error", "Failed to send message. Please try again.");
            }
        }
    }
    
    /**
     * Displays an error message dialog.
     * <p>
     * Creates and shows a modal dialog with an error message.
     * </p>
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
}