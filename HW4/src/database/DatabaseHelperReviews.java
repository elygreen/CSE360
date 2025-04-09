package databasePart1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the database operations related to trusted reviewers functionality.
 * It allows users to save, retrieve, and manage their list of trusted reviewers.
 * <p>
 * The class provides methods to add and remove trusted reviewers, check if a reviewer
 * is trusted, retrieve lists of trusted reviewers, and get statistics about reviewer trust.
 * </p>
 * <p>
 * It uses a connection from a parent DatabaseHelper class and manages a TrustedReviewers
 * table in the database.
 * </p>
 */
public class DatabaseHelperReviews {
    
    /**
     * The database connection used for all operations.
     * This connection is obtained from the parent DatabaseHelper instance.
     */
    private static Connection connection;
    
    /**
     * Constructor that initializes the connection from an existing DatabaseHelper.
     * It also ensures the TrustedReviewers table exists in the database.
     * 
     * @param dbHelper The main DatabaseHelper instance that provides the database connection
     */
    public DatabaseHelperReviews(DatabaseHelper dbHelper) {
        this.connection = dbHelper.getConnection();
        createTrustedReviewersTable();
    }
    
    /**
     * Creates the TrustedReviewers table if it doesn't exist.
     * <p>
     * The table has the following structure:
     * <ul>
     *   <li>id: Auto-incrementing primary key</li>
     *   <li>userId: Foreign key reference to cse360users(id)</li>
     *   <li>trustedReviewerUserName: Username of the trusted reviewer</li>
     *   <li>timestamp: Automatic timestamp of when the trust relationship was created</li>
     * </ul>
     * </p>
     * <p>
     * The method also creates an index on the userId column for more efficient lookups.
     * </p>
     */
    private void createTrustedReviewersTable() {
        String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS TrustedReviewers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userId INT NOT NULL, "
                + "trustedReviewerUserName VARCHAR(255) NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (userId) REFERENCES cse360users(id) ON DELETE CASCADE, "
                + "UNIQUE (userId, trustedReviewerUserName))";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(trustedReviewersTable);
            // Create an index for more efficient lookups
            statement.execute("CREATE INDEX IF NOT EXISTS idx_trusted_reviewers_user ON TrustedReviewers(userId)");
        } catch (SQLException e) {
            System.err.println("Error creating TrustedReviewers table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a reviewer to a user's trusted list.
     * <p>
     * The method performs several validations before adding:
     * <ul>
     *   <li>Checks if the user exists</li>
     *   <li>Checks if the reviewer exists</li>
     *   <li>Checks if the reviewer is already trusted by the user</li>
     * </ul>
     * </p>
     * 
     * @param userName The username of the user adding a trusted reviewer
     * @param trustedReviewerUserName The username of the reviewer to trust
     * @return True if the addition was successful, false otherwise
     */
    public static boolean addTrustedReviewer(String userName, String trustedReviewerUserName) {
        // First, get the userId for the given username
        int userId = getUserId(userName);
        if (userId == -1) {
            return false; // User not found
        }
        
        // Check if the trusted reviewer exists
        int reviewerId = getUserId(trustedReviewerUserName);
        if (reviewerId == -1) {
            return false; // Reviewer not found
        }
        
        // Check if the trusted reviewer is already in the user's list
        if (isTrustedReviewer(userName, trustedReviewerUserName)) {
            return false; // Already trusted
        }
        
        // Add the trusted reviewer
        String insertTrustedReviewer = "INSERT INTO TrustedReviewers (userId, trustedReviewerUserName) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertTrustedReviewer)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, trustedReviewerUserName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding trusted reviewer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Removes a reviewer from a user's trusted list.
     * <p>
     * The method first validates the user exists before attempting to remove the trusted reviewer.
     * </p>
     * 
     * @param userName The username of the user removing a trusted reviewer
     * @param trustedReviewerUserName The username of the reviewer to remove from trusted list
     * @return True if the removal was successful, false otherwise
     */
    public boolean removeTrustedReviewer(String userName, String trustedReviewerUserName) {
        // First, get the userId for the given username
        int userId = getUserId(userName);
        if (userId == -1) {
            return false; // User not found
        }
        
        // Remove the trusted reviewer
        String deleteTrustedReviewer = "DELETE FROM TrustedReviewers WHERE userId = ? AND trustedReviewerUserName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteTrustedReviewer)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, trustedReviewerUserName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing trusted reviewer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Checks if a reviewer is in a user's trusted list.
     * <p>
     * The method first validates the user exists before checking the trust relationship.
     * </p>
     * 
     * @param userName The username of the user
     * @param trustedReviewerUserName The username of the reviewer to check
     * @return True if the reviewer is trusted by the user, false otherwise
     */
    public static boolean isTrustedReviewer(String userName, String trustedReviewerUserName) {
        // First, get the userId for the given username
        int userId = getUserId(userName);
        if (userId == -1) {
            return false; // User not found
        }
        
        // Check if the reviewer is trusted
        String checkTrustedReviewer = "SELECT * FROM TrustedReviewers WHERE userId = ? AND trustedReviewerUserName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkTrustedReviewer)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, trustedReviewerUserName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If there's a result, the reviewer is trusted
            }
        } catch (SQLException e) {
            System.err.println("Error checking trusted reviewer status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets the list of all trusted reviewers for a user.
     * <p>
     * The method first validates the user exists. If the user doesn't exist,
     * an empty list is returned.
     * </p>
     * <p>
     * The results are ordered by the timestamp of when the trust relationship was established.
     * </p>
     * 
     * @param userName The username of the user
     * @return A list of usernames of trusted reviewers, or an empty list if user not found or has no trusted reviewers
     */
    public static List<String> getTrustedReviewers(String userName) {
        List<String> trustedReviewers = new ArrayList<>();
        
        // First, get the userId for the given username
        int userId = getUserId(userName);
        if (userId == -1) {
            return trustedReviewers; // Return empty list if user not found
        }
        
        // Get all trusted reviewers
        String getTrustedReviewers = "SELECT trustedReviewerUserName FROM TrustedReviewers WHERE userId = ? ORDER BY timestamp";
        try (PreparedStatement pstmt = connection.prepareStatement(getTrustedReviewers)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trustedReviewers.add(rs.getString("trustedReviewerUserName"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting trusted reviewers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trustedReviewers;
    }
    
    /**
     * Gets a list of users who trust a specific reviewer.
     * <p>
     * This method performs a join between the cse360users table and the TrustedReviewers table
     * to find all users who have added the specified reviewer to their trusted list.
     * </p>
     * <p>
     * The results are ordered alphabetically by username.
     * </p>
     * 
     * @param reviewerUserName The username of the reviewer
     * @return A list of usernames of users who trust this reviewer, or an empty list if none found
     */
    public List<String> getUsersWhoTrustReviewer(String reviewerUserName) {
        List<String> usersWhoTrust = new ArrayList<>();
        
        // Get all users who trust this reviewer
        String getUsers = "SELECT u.userName FROM cse360users u "
                + "JOIN TrustedReviewers tr ON u.id = tr.userId "
                + "WHERE tr.trustedReviewerUserName = ? "
                + "ORDER BY u.userName";
        
        try (PreparedStatement pstmt = connection.prepareStatement(getUsers)) {
            pstmt.setString(1, reviewerUserName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    usersWhoTrust.add(rs.getString("userName"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting users who trust reviewer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return usersWhoTrust;
    }
    
    /**
     * Gets the count of users who trust a specific reviewer.
     * <p>
     * This method counts the number of entries in the TrustedReviewers table
     * where the trustedReviewerUserName matches the specified username.
     * </p>
     * 
     * @param reviewerUserName The username of the reviewer
     * @return The number of users who trust this reviewer
     */
    public int getTrustCount(String reviewerUserName) {
        // Get count of users who trust this reviewer
        String getCount = "SELECT COUNT(*) as count FROM TrustedReviewers WHERE trustedReviewerUserName = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(getCount)) {
            pstmt.setString(1, reviewerUserName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting trust count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Helper method to get a user's ID from their username.
     * <p>
     * This method queries the cse360users table to find the ID associated with the given username.
     * </p>
     * 
     * @param userName The username to look up
     * @return The user's ID or -1 if not found
     * @throws SQLException If a database access error occurs
     */
    private static int getUserId(String userName) {
        String getUserId = "SELECT id FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getUserId)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1; // User not found
    }
    
    /**
     * Clears all trusted reviewers for a specific user.
     * <p>
     * This method removes all entries from the TrustedReviewers table
     * where the userId matches the ID of the specified user.
     * </p>
     * <p>
     * The method first validates the user exists before attempting to clear their trusted reviewers.
     * </p>
     * 
     * @param userName The username of the user
     * @return True if the operation was successful, false otherwise
     */
    public boolean clearAllTrustedReviewers(String userName) {
        // First, get the userId for the given username
        int userId = getUserId(userName);
        if (userId == -1) {
            return false; // User not found
        }
        
        // Delete all trusted reviewers for this user
        String deleteTrustedReviewers = "DELETE FROM TrustedReviewers WHERE userId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteTrustedReviewers)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected >= 0; // Success even if no rows were deleted
        } catch (SQLException e) {
            System.err.println("Error clearing trusted reviewers: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets all reviewers sorted by trust count (most trusted first).
     * <p>
     * This method queries the TrustedReviewers table to count how many users trust each reviewer,
     * then returns a list of reviewers sorted in descending order by trust count.
     * </p>
     * <p>
     * If two reviewers have the same trust count, they are sorted alphabetically by username.
     * </p>
     * 
     * @return A list of reviewer usernames sorted by how many users trust them, or an empty list if none found
     */
    public List<String> getReviewersByTrustCount() {
        List<String> reviewers = new ArrayList<>();
        
        // Query to get reviewers sorted by trust count
        String getReviewers = "SELECT trustedReviewerUserName, COUNT(*) as trustCount "
                + "FROM TrustedReviewers "
                + "GROUP BY trustedReviewerUserName "
                + "ORDER BY trustCount DESC, trustedReviewerUserName ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(getReviewers)) {
            
            while (rs.next()) {
                reviewers.add(rs.getString("trustedReviewerUserName"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviewers by trust count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviewers;
    }
}