package databasePart1;

import java.sql.*;
import java.util.*;
import application.User;

/**
 * The DatabaseHelperDM class is responsible for managing the direct messaging
 * functionality in the database, including chat creation, message sending,
 * and message retrieval.
 * 
 * This class provides methods to create and manage chat sessions between users,
 * send and receive messages, and handle various aspects of the messaging system
 * such as read status and search functionality.
 */
public class DatabaseHelperDM {
    
    /**
     * The database connection used for executing SQL statements.
     */
    private static Connection connection;
    
    /**
     * Statement object for executing SQL queries.
     */
    private Statement statement;
    
    /**
     * Constructor that initializes the database helper with an existing connection.
     * 
     * @param connection The active database connection to use for database operations
     */
    public DatabaseHelperDM(Connection connection) {
        this.connection = connection;
        try {
            this.statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the necessary tables for direct messaging if they don't exist.
     * This includes the following tables:
     * <ul>
     *   <li>Chats - Stores information about each chat session</li>
     *   <li>ChatParticipants - Maps users to their respective chats</li>
     *   <li>Messages - Stores the actual message content and metadata</li>
     * </ul>
     * Also creates appropriate indexes for efficient queries.
     * 
     * @throws SQLException If a database access error occurs or the SQL execution fails
     */
    public void createDirectMessageTables() throws SQLException {
        // Create Chats table
        String chatsTable = "CREATE TABLE IF NOT EXISTS Chats ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(chatsTable);
        
        // Create ChatParticipants table
        String chatParticipantsTable = "CREATE TABLE IF NOT EXISTS ChatParticipants ("
                + "chat_id INT NOT NULL, "
                + "user_id INT NOT NULL, "
                + "PRIMARY KEY (chat_id, user_id), "
                + "FOREIGN KEY (chat_id) REFERENCES Chats(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (user_id) REFERENCES cse360users(id) ON DELETE CASCADE)";
        statement.execute(chatParticipantsTable);
        
        // Create Messages table
        String messagesTable = "CREATE TABLE IF NOT EXISTS Messages ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "chat_id INT NOT NULL, "
                + "sender_id INT NOT NULL, "
                + "content VARCHAR(1000) NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "is_read BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (chat_id) REFERENCES Chats(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (sender_id) REFERENCES cse360users(id) ON DELETE CASCADE)";
        statement.execute(messagesTable);
        
        // Create indexes for efficient queries
        statement.execute("CREATE INDEX IF NOT EXISTS idx_messages_chat_timestamp ON Messages(chat_id, timestamp)");
        statement.execute("CREATE INDEX IF NOT EXISTS idx_chat_participants_user ON ChatParticipants(user_id)");
        statement.execute("CREATE INDEX IF NOT EXISTS idx_messages_sender ON Messages(sender_id)");
    }
    
    /**
     * Creates a new chat between two users if one doesn't already exist.
     * If a chat already exists between the specified users, returns the existing chat ID.
     * 
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     * @return The ID of the newly created or existing chat, or -1 if creation failed
     */
    public static int createChat(int user1Id, int user2Id) {
        // First check if a chat already exists between these users
        Integer existingChatId = getChatBetweenUsers(user1Id, user2Id);
        if (existingChatId != null) {
            return existingChatId; // Return existing chat instead of creating a new one
        }
        
        String createChatSQL = "INSERT INTO Chats (created_at, updated_at) VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        String addParticipantsSQL = "INSERT INTO ChatParticipants (chat_id, user_id) VALUES (?, ?)";
        
        try (PreparedStatement createChatStmt = connection.prepareStatement(createChatSQL, Statement.RETURN_GENERATED_KEYS)) {
            // Start transaction
            connection.setAutoCommit(false);
            
            // Create the chat
            createChatStmt.executeUpdate();
            ResultSet generatedKeys = createChatStmt.getGeneratedKeys();
            
            if (generatedKeys.next()) {
                int chatId = generatedKeys.getInt(1);
                
                // Add participants
                try (PreparedStatement addParticipantsStmt = connection.prepareStatement(addParticipantsSQL)) {
                    // Add first user
                    addParticipantsStmt.setInt(1, chatId);
                    addParticipantsStmt.setInt(2, user1Id);
                    addParticipantsStmt.executeUpdate();
                    
                    // Add second user
                    addParticipantsStmt.setInt(1, chatId);
                    addParticipantsStmt.setInt(2, user2Id);
                    addParticipantsStmt.executeUpdate();
                    
                    // Commit the transaction
                    connection.commit();
                    connection.setAutoCommit(true);
                    
                    return chatId;
                }
            }
            
            // If we get here, something went wrong
            connection.rollback();
            connection.setAutoCommit(true);
            return -1;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Checks if a chat exists between two users and returns its ID.
     * 
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     * @return The chat ID if it exists, null otherwise
     */
    public static Integer getChatBetweenUsers(int user1Id, int user2Id) {
        String query = "SELECT c.id FROM Chats c "
                + "JOIN ChatParticipants cp1 ON c.id = cp1.chat_id AND cp1.user_id = ? "
                + "JOIN ChatParticipants cp2 ON c.id = cp2.chat_id AND cp2.user_id = ? "
                + "GROUP BY c.id HAVING COUNT(DISTINCT cp1.user_id) = 1 AND COUNT(DISTINCT cp2.user_id) = 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, user1Id);
            pstmt.setInt(2, user2Id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets all chats associated with a specific user.
     * 
     * @param userId The ID of the user
     * @return A map of chat IDs to the usernames of other participants
     */
    public Map<Integer, String> getUserChats(int userId) {
        Map<Integer, String> userChats = new HashMap<>();
        
        String query = "SELECT c.id, u.userName " +
                       "FROM Chats c " +
                       "JOIN ChatParticipants cp1 ON c.id = cp1.chat_id AND cp1.user_id = ? " +
                       "JOIN ChatParticipants cp2 ON c.id = cp2.chat_id AND cp2.user_id != ? " +
                       "JOIN cse360users u ON cp2.user_id = u.id " +
                       "ORDER BY c.updated_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int chatId = rs.getInt("id");
                String otherUserName = rs.getString("userName");
                userChats.put(chatId, otherUserName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return userChats;
    }
    
    /**
     * Gets the participants of a specific chat.
     * 
     * @param chatId The ID of the chat
     * @param dbHelper The main DatabaseHelper instance to retrieve user roles
     * @return A list of user objects representing the participants
     * @throws NullPointerException If the dbHelper parameter is null
     */
    public List<User> getChatParticipants(int chatId, DatabaseHelper dbHelper) {
        List<User> participants = new ArrayList<>();
        
        String query = "SELECT u.userName, u.password " +
                       "FROM cse360users u " +
                       "JOIN ChatParticipants cp ON u.id = cp.user_id " +
                       "WHERE cp.chat_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, chatId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String userName = rs.getString("userName");
                String password = rs.getString("password");
                
                // Get user roles
                Set<String> roles = dbHelper.getUserRole(userName);
                
                // Create user object
                User user = new User(userName, password, roles);
                participants.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return participants;
    }
    
    /**
     * Sends a message in a chat.
     * Verifies that the sender is a participant in the chat before sending.
     * Updates the chat's updated_at timestamp when a message is sent.
     * 
     * @param chatId The ID of the chat
     * @param senderId The ID of the sending user
     * @param content The content of the message
     * @return The ID of the newly created message, or -1 if sending failed
     */
    public static int sendMessage(int chatId, int senderId, String content) {
        // First verify the sender is a participant in the chat
        String verifyParticipantQuery = "SELECT COUNT(*) FROM ChatParticipants WHERE chat_id = ? AND user_id = ?";
        
        try (PreparedStatement verifyStmt = connection.prepareStatement(verifyParticipantQuery)) {
            verifyStmt.setInt(1, chatId);
            verifyStmt.setInt(2, senderId);
            ResultSet rs = verifyStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                return -1; // Sender is not a participant in the chat
            }
            
            // Update the chat's updated_at timestamp
            String updateChatSQL = "UPDATE Chats SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement updateChatStmt = connection.prepareStatement(updateChatSQL)) {
                updateChatStmt.setInt(1, chatId);
                updateChatStmt.executeUpdate();
            }
            
            // Insert the message
            String insertMessageSQL = "INSERT INTO Messages (chat_id, sender_id, content) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertMessageSQL, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, chatId);
                insertStmt.setInt(2, senderId);
                insertStmt.setString(3, content);
                insertStmt.executeUpdate();
                
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Retrieves all messages in a chat.
     * Messages are returned in chronological order (oldest first).
     * 
     * @param chatId The ID of the chat
     * @return A list of maps containing message details (id, sender, content, timestamp, is_read)
     */
    public static List<Map<String, Object>> getMessagesForChat(int chatId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        String query = "SELECT m.id, m.sender_id, u.userName AS sender_name, m.content, " +
                       "m.timestamp, m.is_read " +
                       "FROM Messages m " +
                       "JOIN cse360users u ON m.sender_id = u.id " +
                       "WHERE m.chat_id = ? " +
                       "ORDER BY m.timestamp ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, chatId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", rs.getInt("id"));
                message.put("sender_id", rs.getInt("sender_id"));
                message.put("sender_name", rs.getString("sender_name"));
                message.put("content", rs.getString("content"));
                message.put("timestamp", rs.getTimestamp("timestamp"));
                message.put("is_read", rs.getBoolean("is_read"));
                
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    /**
     * Gets the count of unread messages in a chat for a specific user.
     * Only counts messages sent by other users, not messages sent by the specified user.
     * 
     * @param chatId The ID of the chat
     * @param userId The ID of the user
     * @return The count of unread messages
     */
    public int getUnreadMessageCount(int chatId, int userId) {
        String query = "SELECT COUNT(*) FROM Messages " +
                       "WHERE chat_id = ? AND sender_id != ? AND is_read = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, chatId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Marks all messages in a chat as read for a specific user.
     * Only marks messages sent by other users, not messages sent by the specified user.
     * 
     * @param chatId The ID of the chat
     * @param userId The ID of the user
     * @return The number of messages marked as read
     */
    public int markMessagesAsRead(int chatId, int userId) {
        String query = "UPDATE Messages SET is_read = TRUE " +
                       "WHERE chat_id = ? AND sender_id != ? AND is_read = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, chatId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Searches for messages containing a specific keyword in a user's chats.
     * Returns messages in reverse chronological order (newest first).
     * 
     * @param userId The ID of the user
     * @param keyword The keyword to search for
     * @return A list of maps containing message details that match the search
     */
    public List<Map<String, Object>> searchMessages(int userId, String keyword) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        String query = "SELECT m.id, m.chat_id, m.sender_id, u.userName AS sender_name, " +
                       "m.content, m.timestamp, m.is_read, u2.userName AS other_user " +
                       "FROM Messages m " +
                       "JOIN cse360users u ON m.sender_id = u.id " +
                       "JOIN ChatParticipants cp1 ON m.chat_id = cp1.chat_id AND cp1.user_id = ? " +
                       "JOIN ChatParticipants cp2 ON m.chat_id = cp2.chat_id AND cp2.user_id != ? " +
                       "JOIN cse360users u2 ON cp2.user_id = u2.id " +
                       "WHERE m.content LIKE ? " +
                       "ORDER BY m.timestamp DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", rs.getInt("id"));
                message.put("chat_id", rs.getInt("chat_id"));
                message.put("sender_id", rs.getInt("sender_id"));
                message.put("sender_name", rs.getString("sender_name"));
                message.put("content", rs.getString("content"));
                message.put("timestamp", rs.getTimestamp("timestamp"));
                message.put("is_read", rs.getBoolean("is_read"));
                message.put("other_user", rs.getString("other_user"));
                
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    /**
     * Gets the most recent chats for a user.
     * Includes information about the last message and unread message count.
     * 
     * @param userId The ID of the user
     * @param limit The maximum number of chats to return
     * @return A map of chat IDs to objects containing chat details
     */
    public Map<Integer, Map<String, Object>> getRecentChats(int userId, int limit) {
        Map<Integer, Map<String, Object>> recentChats = new HashMap<>();
        
        String query = "SELECT c.id, c.updated_at, u.userName AS other_user, " +
                       "(SELECT COUNT(*) FROM Messages m WHERE m.chat_id = c.id AND m.sender_id != ? AND m.is_read = FALSE) AS unread_count, " +
                       "(SELECT m.content FROM Messages m WHERE m.chat_id = c.id ORDER BY m.timestamp DESC LIMIT 1) AS last_message " +
                       "FROM Chats c " +
                       "JOIN ChatParticipants cp1 ON c.id = cp1.chat_id AND cp1.user_id = ? " +
                       "JOIN ChatParticipants cp2 ON c.id = cp2.chat_id AND cp2.user_id != ? " +
                       "JOIN cse360users u ON cp2.user_id = u.id " +
                       "ORDER BY c.updated_at DESC " +
                       "LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            pstmt.setInt(4, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int chatId = rs.getInt("id");
                Map<String, Object> chatDetails = new HashMap<>();
                chatDetails.put("updated_at", rs.getTimestamp("updated_at"));
                chatDetails.put("other_user", rs.getString("other_user"));
                chatDetails.put("unread_count", rs.getInt("unread_count"));
                chatDetails.put("last_message", rs.getString("last_message"));
                
                recentChats.put(chatId, chatDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return recentChats;
    }
    
    /**
     * Deletes a chat and all associated messages.
     * The deletion is cascading, so all related records in the ChatParticipants and Messages tables
     * will also be deleted.
     * 
     * @param chatId The ID of the chat to delete
     * @return True if the chat was successfully deleted, false otherwise
     */
    public boolean deleteChat(int chatId) {
        String query = "DELETE FROM Chats WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, chatId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Gets the ID of a user by their username.
     * 
     * @param userName The username of the user
     * @return The user's ID, or -1 if the user doesn't exist
     */
    public int getUserIdByName(String userName) {
        String query = "SELECT id FROM cse360users WHERE userName = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
}