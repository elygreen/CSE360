package databasePart1;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import application.Answer;
import application.Question;
import application.Review;
import application.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 * It provides functionality for managing questions, answers, reviews, user roles, and voting systems.
 */
public class DatabaseHelper {

    /** JDBC driver name for H2 database */
    static final String JDBC_DRIVER = "org.h2.Driver";
    
    /** Database URL for the application's H2 database */
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

    /** Database username */
    static final String USER = "sa";
    
    /** Database password */
    static final String PASS = "";

    /** The database connection object */
    private Connection connection = null;
    
    /** The statement object for executing SQL statements */
    private Statement statement = null;

    /**
     * Establishes a connection to the database.
     * Loads the JDBC driver, creates necessary database tables if they don't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            // You can use this command to clear the database and restart from fresh.
            //statement.execute("DROP ALL OBJECTS");

            createTables();  // Create the necessary tables if they don't exist
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Creates all the necessary database tables if they don't exist.
     * This includes tables for users, roles, questions, answers, votes, reviews, etc.
     *
     * @throws SQLException If a database access error occurs
     */
    private void createTables() throws SQLException {
    	String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
    	        + "id INT AUTO_INCREMENT PRIMARY KEY, "
    	        + "userName VARCHAR(255) UNIQUE, "
    	        + "password VARCHAR(255), "
    	        + "is_banned BOOLEAN DEFAULT FALSE)";
    	statement.execute(userTable);
        
        // Separate table from userTable to store multiple roles
        String userRoles = "CREATE TABLE IF NOT EXISTS UserRoles ("
                + "roleId INT AUTO_INCREMENT PRIMARY KEY, "
                + "userId INT NOT NULL, "
                + "role VARCHAR(50) NOT NULL, "
                + "FOREIGN KEY (userId) REFERENCES cse360users(id) ON DELETE CASCADE)";
        statement.execute(userRoles);
        
        // Create the invitation codes table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);
    
        // Create questions table
        String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "body VARCHAR(1000) NOT NULL, "
                + "askedBy VARCHAR(255) NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(questionsTable);
        
        // Create answers table
        String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionId INT NOT NULL, "
                + "text VARCHAR(1000) NOT NULL, "
                + "answeredBy VARCHAR(255) NOT NULL, "
                + "upVote INT DEFAULT 0, "
                + "downVote INT DEFAULT 0, "
                + "isCorrect BOOLEAN DEFAULT FALSE, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (questionId) REFERENCES Questions(id) ON DELETE CASCADE)";
        statement.execute(answersTable);
        
        // Create answer upvote & downvote table
        String votesTable = "CREATE TABLE IF NOT EXISTS Votes ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "answerId INT NOT NULL, "
                + "userName VARCHAR(255) NOT NULL, "
                + "voteType VARCHAR(10) NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (answerId) REFERENCES Answers(id) ON DELETE CASCADE, "
                + "UNIQUE (answerId, userName))";
        statement.execute(votesTable);
        
        // Create question votes table
        String questionVotesTable = "CREATE TABLE IF NOT EXISTS QuestionVotes ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "questionId INT NOT NULL, "
            + "userName VARCHAR(255) NOT NULL, "
            + "voteType VARCHAR(10) NOT NULL, "
            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (questionId) REFERENCES Questions(id) ON DELETE CASCADE, "
            + "UNIQUE (questionId, userName))";
        statement.execute(questionVotesTable);
        
        // Create reviews table
        String reviewsTable = "CREATE TABLE IF NOT EXISTS Reviews ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "answerId INT NOT NULL, "
            + "reviewer VARCHAR(255) NOT NULL, "
            + "text VARCHAR(1000) NOT NULL)";
        statement.execute(reviewsTable);
        
        // Create tables for Direct Messages
        createDirectMessageTables();
        
        // Create trusted reviewers table
        createTrustedReviewersTable();
        
        // Create review votes table
        String reviewVotesTable = "CREATE TABLE IF NOT EXISTS ReviewVotes ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "reviewId INT NOT NULL, "
            + "userName VARCHAR(255) NOT NULL, "
            + "voteType VARCHAR(10) NOT NULL, "
            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (reviewId) REFERENCES Reviews(id) ON DELETE CASCADE, "
            + "UNIQUE (reviewId, userName))";
        statement.execute(reviewVotesTable);
        
        // Add upvote and downvote columns to Reviews table if they don't exist
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet rs = dbm.getColumns(null, null, "REVIEWS", "UPVOTE");
            if (!rs.next()) {
                statement.execute("ALTER TABLE Reviews ADD COLUMN upVote INT DEFAULT 0");
                statement.execute("ALTER TABLE Reviews ADD COLUMN downVote INT DEFAULT 0");
            }
        } catch (SQLException e) {
            System.err.println("Error adding vote columns to Reviews table: " + e.getMessage());
        }

        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet rs = dbm.getColumns(null, null, "ANSWERS", "ISCORRECT");
            if (!rs.next()) {
                statement.execute("ALTER TABLE Answers ADD COLUMN isCorrect BOOLEAN DEFAULT FALSE");
            }
        } catch (SQLException e) {
            System.err.println("Error adding isCorrect column: " + e.getMessage());
        }
    }

    /**
     * Checks if the database is empty (contains no users).
     *
     * @return True if the database is empty, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /**
     * Registers a new user in the database with their username, password, and roles.
     *
     * @param user The User object containing user information to register
     * @throws SQLException If a database access error occurs
     */
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password) VALUES (?, ?)";
        String insertRole = "INSERT INTO UserRoles (userId, role) VALUES (?,?)";
        try (    
            PreparedStatement pstmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement rstmt = connection.prepareStatement(insertRole);
        ) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.executeUpdate();
            
            // Retrieve the generated userId
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                // Insert roles into user_roles table
                for (String role : user.getRole()) {
                    rstmt.setInt(1, userId);
                    rstmt.setString(2, role);
                    rstmt.addBatch();
                }
                rstmt.executeBatch(); // Execute all role inserts in one batch
            } else {
                throw new SQLException("Failed to retrieve userId after inserting user.");
            }
        }
    }
    
    /**
     * Deletes a user from the database by their username.
     *
     * @param userName The username of the user to delete
     */
    public void deleteUser(String userName) {
        // SQL query for deleting a user
        String deleteQuery = "DELETE FROM cse360users WHERE userName = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            // Set the username parameter in the query
            preparedStatement.setString(1, userName);
            
            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            
            // Provide feedback
            if (rowsAffected > 0) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
        }
    }    
    
    /**
     * Validates a user's login credentials against the database, and also checks if the user is banned.
     *
     * @param user The User object containing login credentials
     * @return True if the login is valid and the user is not banned, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean login(User user) throws SQLException {
        // First ensure the ban column exists
        ensureBanColumnExists();
        
        String query = "SELECT is_banned FROM cse360users WHERE userName = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Check if the user is banned
                    boolean isBanned = rs.getBoolean("is_banned");
                    return !isBanned; // Return true only if user is not banned
                }
                return false; // User credentials not found
            }
        }
    }
    
    /**
     * Checks if a user with the given username already exists in the database.
     *
     * @param userName The username to check
     * @return True if the user exists, false otherwise
     */
    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // If the count is greater than 0, the user exists
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // If an error occurs, assume user doesn't exist
    }
    
    /**
     * Retrieves all roles assigned to a user from the database.
     *
     * @param userName The username whose roles to retrieve
     * @return A Set of String containing the user's roles
     */
    public Set<String> getUserRole(String userName) {
        // Create set of roles
        Set<String> roles = new HashSet<>();
        String query = "SELECT role FROM userRoles WHERE userId = (SELECT id FROM cse360users WHERE userName = ?)";
        
        // Get username
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            
            // Add roles to user from database
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("role"));
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }
    
    /**
     * Generates a new random invitation code and stores it in the database.
     *
     * @return The generated invitation code
     */
    public String generateInvitationCode() {
        String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
        String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return code;
    }
    
    /**
     * Validates an invitation code against the database to check if it exists and is unused.
     *
     * @param code The invitation code to validate
     * @return True if the code is valid and unused, false otherwise
     */
    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Mark the code as used
                markInvitationCodeAsUsed(code);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Marks an invitation code as used in the database.
     *
     * @param code The invitation code to mark as used
     */
    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    ///////////////////////////////
    // QUESTION & ANSWER METHODS //
    ///////////////////////////////
    
    /**
     * Saves a question to the database and its associated answers if any.
     *
     * @param question The Question object to save
     * @return The ID of the saved question, or -1 if the operation failed
     */
    public int saveQuestion(Question question) {
        String insertQuestion = "INSERT INTO Questions (body, askedBy) VALUES (?, ?)";
        try (PreparedStatement preparedstatement = connection.prepareStatement(insertQuestion, Statement.RETURN_GENERATED_KEYS)) {
            preparedstatement.setString(1, question.getBody());
            preparedstatement.setString(2, question.getAskedBy());
            int affectedRows = preparedstatement.executeUpdate();
            
            // Failure
            if (affectedRows == 0) {
                return -1;
            }
            
            // get the generated question ID
            try (ResultSet generatedKeys = preparedstatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int questionId = generatedKeys.getInt(1);
                    // Save all answers for this question
                    for (Answer answer : question.getAnswers()) {
                        saveAnswer(questionId, answer);
                    }
                    return questionId;
                }
                // Failed to get ID
                else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Saves an answer to the database for a specific question.
     *
     * @param questionId The ID of the question this answer belongs to
     * @param answer The Answer object to save
     * @return True if the answer was saved successfully, false otherwise
     */
    public boolean saveAnswer(int questionId, Answer answer) {
        String insertAnswer = "INSERT INTO Answers (questionId, text, answeredBy, upVote, downVote, isCorrect) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedstatement = connection.prepareStatement(insertAnswer)) {
            preparedstatement.setInt(1, questionId);
            preparedstatement.setString(2, answer.getText());
            preparedstatement.setString(3, answer.getAnsweredBy());
            preparedstatement.setInt(4, answer.getUpvotes());
            preparedstatement.setInt(5, answer.getDownvotes());
            preparedstatement.setBoolean(6, answer.isCorrect());
            int affectedRows = preparedstatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    ///////////////////////////////
    // UPVOTE & DOWNVOTE METHODS //
    ///////////////////////////////
    
    /**
     * Updates the upvote and downvote counts for a specific answer.
     *
     * @param answerId The ID of the answer to update
     * @param upVotes The new upvote count
     * @param downVotes The new downvote count
     * @return True if the update was successful, false otherwise
     */
    public boolean updateAnswerVotes(int answerId, int upVotes, int downVotes) {
        String query = "UPDATE Answers SET upVote = ?, downVote = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, upVotes);
            pstmt.setInt(2, downVotes);
            pstmt.setInt(3, answerId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Records a vote on an answer by a user. Handles new votes, changing vote types,
     * and removing votes when a user clicks the same vote button again.
     *
     * @param answerId The ID of the answer being voted on
     * @param userName The username of the voter
     * @param voteType The type of vote ("upvote" or "downvote")
     * @return True if the operation was successful, false otherwise
     */
    public boolean recordVote(int answerId, String userName, String voteType) {
        // check if user has already voted on answer
        String existingVoteType = getUserVoteType(answerId, userName);
        
        // User has not voted
        if (existingVoteType == null) {
            String insertVote = "INSERT INTO Votes (answerId, userName, voteType) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertVote)) {
                pstmt.setInt(1, answerId);
                pstmt.setString(2, userName);
                pstmt.setString(3, voteType);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        // User has voted and is changing their vote (up to down || down to up)
        else if (!existingVoteType.equals(voteType)) {
            String updateVote = "UPDATE Votes SET voteType = ? WHERE answerId = ? AND userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateVote)) {
                pstmt.setString(1, voteType);
                pstmt.setInt(2, answerId);
                pstmt.setString(3, userName);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        // User is clicking on vote they've already clicked on (aka removing their vote)
        else {
            String deleteVote = "DELETE FROM Votes WHERE answerId = ? AND userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteVote)) {
                pstmt.setInt(1, answerId);
                pstmt.setString(2, userName);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    /**
     * Gets the number of upvotes for a specific answer.
     *
     * @param answerId The ID of the answer
     * @return The number of upvotes for the answer
     */
    public int getAnswerUpvotes(int answerId) {
        String query = "SELECT upVote FROM Answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("upVote");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Gets the number of downvotes for a specific answer.
     *
     * @param answerId The ID of the answer
     * @return The number of downvotes for the answer
     */
    public int getAnswerDownvotes(int answerId) {
        String query = "SELECT downVote FROM Answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("downVote");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    
    /**
     * Gets the type of vote a user made on an answer.
     *
     * @param answerId The ID of the answer
     * @param userName The username of the voter
     * @return The vote type ("upvote" or "downvote") or null if the user hasn't voted
     */
    public String getUserVoteType(int answerId, String userName) {
        String query = "SELECT voteType FROM Votes WHERE answerId = ? AND userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            pstmt.setString(2, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("voteType");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        // Return null if user has not voted on the answer
        return null;
    }
    
    /**
     * Recalculates the vote counts for an answer by counting votes in the Votes table
     * and updates the Answers table with the current counts.
     *
     * @param answerId The ID of the answer to recalculate votes for
     * @return True if the recalculation was successful, false otherwise
     */
    public boolean recalculateAnswerVotes(int answerId) {
        
        // Count upvotes
        String upvoteQuery = "SELECT COUNT(*) as count FROM Votes WHERE answerId = ? AND voteType = 'upvote'";
        // Count downvotes
        String downvoteQuery = "SELECT COUNT(*) as count FROM Votes WHERE answerId = ? AND voteType = 'downvote'";
        
        try {
            int upvotes = 0;
            int downvotes = 0;
            
            // Get upvote count
            try (PreparedStatement pstmt = connection.prepareStatement(upvoteQuery)) {
                pstmt.setInt(1, answerId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    upvotes = rs.getInt("count");
                }
            }
            
            // Get downvote count
            try (PreparedStatement pstmt = connection.prepareStatement(downvoteQuery)) {
                pstmt.setInt(1, answerId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    downvotes = rs.getInt("count");
                }
            }
            
            // Update answer w/ up to date vote counts
            return updateAnswerVotes(answerId, upvotes, downvotes);
            
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Records a vote on a question by a user. Handles new votes, changing vote types,
     * and removing votes when a user clicks the same vote button again.
     *
     * @param questionId The ID of the question being voted on
     * @param userName The username of the voter
     * @param voteType The type of vote ("upvote" or "downvote")
     * @return True if the operation was successful, false otherwise
     */
    public boolean recordQuestionVote(int questionId, String userName, String voteType) {
        // Check user already voted on question
        String existingVoteType = getUserQuestionVoteType(questionId, userName);
        
        // User has not voted
        if (existingVoteType == null) {
            String insertVote = "INSERT INTO QuestionVotes (questionId, userName, voteType) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertVote)) {
                pstmt.setInt(1, questionId);
                pstmt.setString(2, userName);
                pstmt.setString(3, voteType);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        // User has voted and is changing their vote
        else if (!existingVoteType.equals(voteType)) {
            String updateVote = "UPDATE QuestionVotes SET voteType = ? WHERE questionId = ? AND userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateVote)) {
                pstmt.setString(1, voteType);
                pstmt.setInt(2, questionId);
                pstmt.setString(3, userName);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        // User is removing their vote
        else {
            String deleteVote = "DELETE FROM QuestionVotes WHERE questionId = ? AND userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteVote)) {
                pstmt.setInt(1, questionId);
                pstmt.setString(2, userName);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Gets the type of vote a user made on a question.
     *
     * @param questionId The ID of the question
     * @param userName The username of the voter
     * @return The vote type ("upvote" or "downvote") or null if the user hasn't voted
     */
    public String getUserQuestionVoteType(int questionId, String userName) {
        String query = "SELECT voteType FROM QuestionVotes WHERE questionId = ? AND userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("voteType");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Gets the number of upvotes for a specific question.
     *
     * @param questionId The ID of the question
     * @return The number of upvotes for the question
     */
    public int getQuestionUpvotes(int questionId) {
        String query = "SELECT COUNT(*) as count FROM QuestionVotes WHERE questionId = ? AND voteType = 'upvote'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets the number of downvotes for a specific question.
     *
     * @param questionId The ID of the question
     * @return The number of downvotes for the question
     */
    public int getQuestionDownvotes(int questionId) {
        String query = "SELECT COUNT(*) as count FROM QuestionVotes WHERE questionId = ? AND voteType = 'downvote'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Updates a question's text in the database.
     *
     * @param questionID The ID of the question to update
     * @param updatedText The new text for the question
     * @return True if the update was successful, false otherwise
     */
    public boolean updateQuestion(int questionID, String updatedText) {
        String query = "UPDATE questions SET body = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, updatedText);
            stmt.setInt(2, questionID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Saves a review to the database.
     *
     * @param review The Review object to save
     * @return The ID of the saved review, or -1 if the operation failed
     */
    public int saveReview(Review review) {
        String insertReview = "INSERT INTO Reviews (answerId, reviewer, text) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertReview, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters for the prepared statement
            preparedStatement.setInt(1, review.getAnswerID());
            preparedStatement.setString(2, review.getReviewedBy());
            preparedStatement.setString(3, review.getReviewBody());

            int affectedRows = preparedStatement.executeUpdate();

            // Check if the insert was successful
            if (affectedRows == 0) {
                return -1;
            }

            // Retrieve the generated ID
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1; // No ID was generated
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Handle SQL exception
    }
    
    /**
     * Loads all reviews stored in the database.
     *
     * @return An ObservableList of Review objects
     */
    public ObservableList<Review> loadAllReviews() {
        ObservableList<Review> reviews = FXCollections.observableArrayList();
        
        String query = "SELECT * FROM Reviews";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int reviewID = rs.getInt("id");
                int answerID = rs.getInt("answerId");
                String reviewer = rs.getString("reviwer");
                String body = rs.getString("text");
                Review review = new Review(body, reviewer, answerID);
                
                reviews.add(review);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }
    
    /**
     * Loads all reviews with their corresponding answer IDs.
     *
     * @return A Map of Review objects to their answer IDs
     */
    public Map<Review, Integer> loadAllReviewsWithIDs() {
        Map<Review, Integer> reviewsWithIDs = new HashMap<>();
        String query = "SELECT * FROM Reviews";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {
        
            while (rs.next()) {
                int reviewID = rs.getInt("id");
                int answerID = rs.getInt("answerId");
                String body = rs.getString("text");
                String reviewer = rs.getString("reviewer");
                Review review = new Review(body, reviewer, answerID);
                
                reviewsWithIDs.put(review, answerID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviewsWithIDs;
    }
    
    /**
     * Gets all reviews written by a specific author.
     *
     * @param author The username of the author
     * @return A List of Review objects
     */
    public static List<Review> getReviewsByAuthor(String author) {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM reviews WHERE reviewer = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, author);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Review review = new Review(
                        resultSet.getString("text"),
                        resultSet.getString("reviewer"),
                        resultSet.getInt("answerId")
                    );
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reviews;
    }
    
    
    /**
     * Loads all questions with their IDs from the database.
     *
     * @return A Map of Question objects to their IDs
     */
    public Map<Question, Integer> loadAllQuestionsWithIDs() {
        Map<Question, Integer> questionsWithIDs = new HashMap<>();
        String query = "SELECT * FROM Questions ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("id");
                    String body = rs.getString("body");
                    String askedBy = rs.getString("askedBy");
                    Question question = new Question(body, askedBy);
                    // load answers for this question
                    loadAnswersForQuestion(questionId, question);
                    questionsWithIDs.put(question, questionId);
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questionsWithIDs;
    }
    
    /**
     * Loads all answers for a specific question and adds them to the Question object.
     *
     * @param questionId The ID of the question whose answers to load
     * @param question The Question object to add the answers to
     */
    private void loadAnswersForQuestion(int questionId, Question question) {
        String query = "SELECT * FROM Answers WHERE questionId = ? ORDER BY timestamp";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int answerId = rs.getInt("id");
                    String text = rs.getString("text");
                    String answeredBy = rs.getString("answeredBy");
                    int upVotes = rs.getInt("upVote");
                    int downVotes = rs.getInt("downVote");
                    boolean isCorrect = rs.getBoolean("isCorrect");
                    
                    Answer answer = new Answer(text, answeredBy);
                    // store database ID
                    answer.setId(answerId);
                    
                    // set upvote / downvotes directly so exact values are loaded from database and are up to date
                    for (int i = 0; i < upVotes; i++) {
                        answer.upvote();
                    }
                    for (int i = 0; i < downVotes; i++) {
                        answer.downvote();
                    }
                    if (isCorrect) {
                        answer.markAsCorrect();
                    }
                    question.addAnswer(answer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
        
    /**
     * Finds the database ID of a question based on its content and author.
     *
     * @param question The Question object to find the ID for
     * @return The ID of the question, or -1 if not found
     * @throws RuntimeException If there is an error retrieving the ID
     */
    public int findIdOfQuestion(Question question) {
        // SQL query to find the ID of the question
        String findQuestion = "SELECT id FROM Questions WHERE body = ? AND askedBy = ?";
        int questionId = -1; // Default value if the question is not found
        
        try (PreparedStatement pstmt = connection.prepareStatement(findQuestion)) {
            pstmt.setString(1, question.getBody());
            pstmt.setString(2, question.getAskedBy());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    questionId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve question ID from database", e); // Rethrow exception
        }
        
        return questionId; 
    }
    
    /**
     * Finds the database ID of a review based on its content, author, and answer ID.
     *
     * @param review The Review object to find the ID for
     * @return The ID of the review
     * @throws RuntimeException If there is an error retrieving the ID
     */
    public int findIdOfReview(Review review) {
        
        // SQL query to find the ID of the review
        String query = "SELECT id FROM Reviews WHERE answerId = ? AND reviewer = ? AND text = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            // Set parameters in the query
            preparedStatement.setInt(1, review.getAnswerID());
            preparedStatement.setString(2, review.getReviewedBy());
            preparedStatement.setString(3, review.getReviewBody());
            
            // Execute the query and fetch the result
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id"); // Return the ID of the review
            } else {
                throw new SQLException("No matching review found in the database.");
            }
        } catch (SQLException e) {
             e.printStackTrace();
             throw new RuntimeException("Failed to retrieve question ID from database", e);
        }
    }
    
    /**
     * Deletes a question from the database.
     *
     * @param question The Question object to delete
     * @return True if the question was deleted successfully, false otherwise
     */
    public boolean deleteQuestion(Question question) {
        // find the question ID
        String findQuestion = "SELECT id FROM Questions WHERE body = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(findQuestion)) {
            pstmt.setString(1, question.getBody());
            pstmt.setString(2, question.getAskedBy());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int questionId = rs.getInt("id");                
                    // delete the question matching id
                    String deleteQuestion = "DELETE FROM Questions WHERE id = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuestion)) {
                        deleteStmt.setInt(1, questionId);
                        int affectedRows = deleteStmt.executeUpdate();
                        return affectedRows > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Closes the database connection and statement.
     * Should be called when the application is shutting down.
     */
    public void closeConnection() {
        try{ 
            if(statement!=null) statement.close(); 
        } catch(SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if(connection!=null) connection.close(); 
        } catch(SQLException se){ 
            se.printStackTrace(); 
        } 
    }
    
    /**
     * Displays all usernames and their associated roles to the console.
     * Used for debugging purposes.
     *
     * @throws SQLException If a database access error occurs
     */
    public void displayUsernamesAndRoles() throws SQLException {
        // Query to get usernames, passwords, and their associated roles
        String query = "SELECT u.userName, u.password, GROUP_CONCAT(r.role SEPARATOR ', ') AS roles "
                     + "FROM cse360users u "
                     + "LEFT JOIN userRoles r ON u.id = r.userId "
                     + "GROUP BY u.id, u.userName, u.password";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Usernames, Passwords, and Roles:");

            // Loop through the result set and print each user's information
            while (rs.next()) {
                String userName = rs.getString("userName");
                String password = rs.getString("password");
                String roles = rs.getString("roles");
                System.out.println("Username: " + userName + ", Password: " + password + ", Roles: " + (roles != null ? roles : "No roles assigned"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Resets the database by dropping all objects.
     * This will remove all tables, data, and other database objects.
     *
     * @return boolean indicating whether the reset was successful
     */
    public boolean resetDatabase() {
        try {
            // The DROP ALL OBJECTS command will remove all tables, views, sequences, etc.
            System.out.println("Resetting database - dropping all objects...");
            statement.execute("DROP ALL OBJECTS");
            
            // After dropping everything, recreate the necessary tables
            createTables();
            
            System.out.println("Database has been reset successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error resetting database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets all users with their roles from the database.
     *
     * @return An ObservableList of User objects with their roles
     */
    public ObservableList<User> getAllUsersWithRoles() {
        ObservableList<User> users = FXCollections.observableArrayList();
        
        String query = "SELECT u.id, u.userName, u.password FROM cse360users u";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int userId = rs.getInt("id");
                String userName = rs.getString("userName");
                String password = rs.getString("password");
                
                // Create a set to hold roles
                Set<String> roles = new HashSet<>();
                
                // Get roles for this user
                String roleQuery = "SELECT role FROM UserRoles WHERE userId = ?";
                try (PreparedStatement rolePstmt = connection.prepareStatement(roleQuery)) {
                    rolePstmt.setInt(1, userId);
                    ResultSet roleRs = rolePstmt.executeQuery();
                    
                    while (roleRs.next()) {
                        roles.add(roleRs.getString("role"));
                    }
                }
                
                // Create user object with the set of roles
                User user = new User(userName, password, roles);
                // Be extra sure the username is set
                if (!roles.isEmpty()) {
                    System.out.println("Adding user: " + userName + " with roles: " + String.join(", ", roles));
                } else {
                    System.out.println("Adding user: " + userName + " with no roles");
                }
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Returns the current database connection.
     * 
     * @return The current Connection object
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Adds a role to a user in the database.
     * 
     * @param userName The username of the user to update
     * @param role The role to add
     * @return True if the role was added successfully, false otherwise
     */
    public boolean addRoleToUser(String userName, String role) {
        try {
            // First check if the user already has this role
            Set<String> userRoles = getUserRole(userName);
            if (userRoles.contains(role)) {
                return false; // User already has this role
            }
            
            // Get the userId from the database
            String getUserIdQuery = "SELECT id FROM cse360users WHERE userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getUserIdQuery)) {
                pstmt.setString(1, userName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    
                    // Insert the new role
                    String insertRoleQuery = "INSERT INTO UserRoles (userId, role) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertRoleQuery)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setString(2, role);
                        int rowsAffected = insertStmt.executeUpdate();
                        
                        return rowsAffected > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return false;
    }

    /**
     * Removes a role from a user in the database.
     * 
     * @param userName The username of the user to update
     * @param role The role to remove
     * @return True if the role was removed successfully, false otherwise
     */
    public boolean removeRoleFromUser(String userName, String role) {
        try {
            // Get the userId from the database
            String getUserIdQuery = "SELECT id FROM cse360users WHERE userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getUserIdQuery)) {
                pstmt.setString(1, userName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    
                    // Delete the role
                    String deleteRoleQuery = "DELETE FROM UserRoles WHERE userId = ? AND role = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteRoleQuery)) {
                        deleteStmt.setInt(1, userId);
                        deleteStmt.setString(2, role);
                        int rowsAffected = deleteStmt.executeUpdate();
                        
                        return rowsAffected > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Deletes a review from the database based on its ID.
     * 
     * @param reviewID The ID of the review to delete
     * @return True if the review was successfully deleted, false otherwise
     */
    public boolean deleteReview(Integer reviewID) {
        String deleteReview = "DELETE FROM Reviews WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteReview)) {
            pstmt.setInt(1, reviewID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the text of a review in the database.
     * 
     * @param reviewID The ID of the review to update
     * @param updatedText The new text for the review
     * @return True if the review was successfully updated, false otherwise
     */
    public boolean updateReview(Integer reviewID, String updatedText) {
        String updateReview = "UPDATE Reviews SET text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateReview)) {
            pstmt.setString(1, updatedText);
            pstmt.setInt(2, reviewID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates the necessary tables for direct messaging if they don't exist.
     * This includes tables for chats, chat participants, and messages.
     *
     * @throws SQLException If a database access error occurs
     */
    private void createDirectMessageTables() throws SQLException {
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
     * Creates the trusted reviewers table if it doesn't exist.
     * This table stores users who are trusted to review content.
     */
    private void createTrustedReviewersTable() {
        String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS TrustedReviewers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userId INT NOT NULL, "
                + "trustedReviewerUserName VARCHAR(255) NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (userId) REFERENCES cse360users(id) ON DELETE CASCADE, "
                + "UNIQUE (userId, trustedReviewerUserName))";
        
        try {
            statement.execute(trustedReviewersTable);
            // Create an index for more efficient lookups
            statement.execute("CREATE INDEX IF NOT EXISTS idx_trusted_reviewers_user ON TrustedReviewers(userId)");
        } catch (SQLException e) {
            System.err.println("Error creating TrustedReviewers table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the question ID associated with an answer.
     * 
     * @param answerId The ID of the answer
     * @return The ID of the question this answer belongs to, or -1 if not found
     */
    public int getQuestionIdForAnswer(int answerId) {
        String query = "SELECT questionId FROM Answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("questionId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Records a vote on a review. Handles adding new votes, changing vote types,
     * and removing votes when a user clicks the same vote button again.
     * 
     * @param reviewId The ID of the review being voted on
     * @param userName The username of the voter
     * @param voteType The type of vote ("upvote" or "downvote")
     * @return True if the operation was successful, false otherwise
     */
    public boolean recordReviewVote(int reviewId, String userName, String voteType) {
        // Check if user has already voted on this review
        String existingVoteType = getUserReviewVoteType(reviewId, userName);
        
        // User has not voted yet
        if (existingVoteType == null) {
            String insertVote = "INSERT INTO ReviewVotes (reviewId, userName, voteType) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertVote)) {
                pstmt.setInt(1, reviewId);
                pstmt.setString(2, userName);
                pstmt.setString(3, voteType);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        // User is changing vote type (upvote to downvote or vice versa)
        else if (!existingVoteType.equals(voteType)) {
            String updateVote = "UPDATE ReviewVotes SET voteType = ? WHERE reviewId = ? AND userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateVote)) {
                pstmt.setString(1, voteType);
                pstmt.setInt(2, reviewId);
                pstmt.setString(3, userName);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        // User is clicking on the same vote type again (removing their vote)
        else {
            String deleteVote = "DELETE FROM ReviewVotes WHERE reviewId = ? AND userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteVote)) {
                pstmt.setInt(1, reviewId);
                pstmt.setString(2, userName);
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    /**
     * Gets the type of vote a user has made on a review.
     * 
     * @param reviewId The ID of the review
     * @param userName The username of the voter
     * @return The vote type ("upvote" or "downvote") or null if the user hasn't voted
     */
    public String getUserReviewVoteType(int reviewId, String userName) {
        String query = "SELECT voteType FROM ReviewVotes WHERE reviewId = ? AND userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            pstmt.setString(2, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("voteType");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Gets the number of upvotes for a review.
     * 
     * @param reviewId The ID of the review
     * @return The number of upvotes
     */
    public int getReviewUpvotes(int reviewId) {
        String query = "SELECT COUNT(*) as count FROM ReviewVotes WHERE reviewId = ? AND voteType = 'upvote'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Gets the number of downvotes for a review.
     * 
     * @param reviewId The ID of the review
     * @return The number of downvotes
     */
    public int getReviewDownvotes(int reviewId) {
        String query = "SELECT COUNT(*) as count FROM ReviewVotes WHERE reviewId = ? AND voteType = 'downvote'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Updates the upvote and downvote counts stored in the Reviews table.
     * 
     * @param reviewId The ID of the review
     * @param upVotes The number of upvotes
     * @param downVotes The number of downvotes
     * @return True if the update was successful, false otherwise
     */
    public boolean updateReviewVotes(int reviewId, int upVotes, int downVotes) {
        String query = "UPDATE Reviews SET upVote = ?, downVote = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, upVotes);
            pstmt.setInt(2, downVotes);
            pstmt.setInt(3, reviewId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Recalculates the vote counts for a review and updates the database.
     * 
     * @param reviewId The ID of the review
     * @return True if the recalculation was successful, false otherwise
     */
    public boolean recalculateReviewVotes(int reviewId) {
        // Count upvotes
        String upvoteQuery = "SELECT COUNT(*) as count FROM ReviewVotes WHERE reviewId = ? AND voteType = 'upvote'";
        // Count downvotes
        String downvoteQuery = "SELECT COUNT(*) as count FROM ReviewVotes WHERE reviewId = ? AND voteType = 'downvote'";
        
        try {
            int upvotes = 0;
            int downvotes = 0;
            
            // Get upvote count
            try (PreparedStatement pstmt = connection.prepareStatement(upvoteQuery)) {
                pstmt.setInt(1, reviewId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    upvotes = rs.getInt("count");
                }
            }
            
            // Get downvote count
            try (PreparedStatement pstmt = connection.prepareStatement(downvoteQuery)) {
                pstmt.setInt(1, reviewId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    downvotes = rs.getInt("count");
                }
            }
            
            // Update review with current vote counts
            return updateReviewVotes(reviewId, upvotes, downvotes);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    ///////////////////////
    /// STAFF FUNCTIONS ///
    
    /**
     * Ensures the ban column exists in the cse360users table.
     * If the column doesn't exist, it will be added.
     * 
     * @return true if the column exists or was successfully added, false otherwise
     */
    public boolean ensureBanColumnExists() {
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet rs = dbm.getColumns(null, null, "CSE360USERS", "IS_BANNED");
            
            // If the column doesn't exist, add it
            if (!rs.next()) {
                String alterTable = "ALTER TABLE cse360users ADD COLUMN is_banned BOOLEAN DEFAULT FALSE";
                statement.execute(alterTable);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error ensuring ban column exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Bans a user by setting their is_banned flag to true.
     * 
     * @param userName The username of the user to ban
     * @return true if the user was successfully banned, false otherwise
     */
    public boolean banUser(String userName) {
        ensureBanColumnExists();
        
        // Now ban the user
        String query = "UPDATE cse360users SET is_banned = TRUE WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error banning user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unbans a user by setting their is_banned flag to false.
     * 
     * @param userName The username of the user to unban
     * @return true if the user was successfully unbanned, false otherwise
     */
    public boolean unbanUser(String userName) {
        ensureBanColumnExists();
        
        //unban the user
        String query = "UPDATE cse360users SET is_banned = FALSE WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error unbanning user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all users with their ban status.
     * 
     * @return A map where the key is the username and the value is the ban status (true if banned, false otherwise)
     */
    public Map<String, Boolean> getAllUsersWithBanStatus() {
        ensureBanColumnExists();
        
        Map<String, Boolean> userBanStatus = new HashMap<>();
        
        String query = "SELECT userName, is_banned FROM cse360users";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String userName = rs.getString("userName");
                boolean isBanned = rs.getBoolean("is_banned");
                userBanStatus.put(userName, isBanned);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ban status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return userBanStatus;
    }

    /**
     * Checks if a user is banned.
     * 
     * @param userName The username to check
     * @return true if the user is banned, false otherwise
     */
    public boolean isUserBanned(String userName) {
        //Make sure the ban column exists
        ensureBanColumnExists();
        String query = "SELECT is_banned FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("is_banned");
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user is banned: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Ensures that the database has the required columns for tracking sensitive content.
     * This method adds 'is_sensitive' columns to both questions and answers tables if they don't exist.
     */
    public void ensureSensitiveColumnsExist() {
        try {
            // Check if sensitive column exists in questions table
            boolean questionSensitiveExists = false;
            boolean answerSensitiveExists = false;
            
            //  question table
            java.sql.ResultSet rs = connection.getMetaData().getColumns(null, null, "QUESTIONS", "IS_SENSITIVE");
            if (rs.next()) {
                questionSensitiveExists = true;
            }
            rs.close();
            
            // answer table
            rs = connection.getMetaData().getColumns(null, null, "ANSWERS", "IS_SENSITIVE");
            if (rs.next()) {
                answerSensitiveExists = true;
            }
            rs.close();
            
            // Add columns if none
            if (!questionSensitiveExists) {
                try {
                    PreparedStatement stmt = connection.prepareStatement(
                            "ALTER TABLE questions ADD COLUMN is_sensitive INTEGER DEFAULT 0");
                    stmt.executeUpdate();
                    stmt.close();
                    System.out.println("Added is_sensitive column to questions table");
                }
                catch (SQLException e) {
                    if (e.getMessage().contains("Duplicate column")) {
                        System.out.println("Column is_sensitive already exists in questions table");
                    }
                    else {
                        throw e;
                    }
                }
            }
            
            if (!answerSensitiveExists) {
                try {
                    PreparedStatement stmt = connection.prepareStatement(
                            "ALTER TABLE answers ADD COLUMN is_sensitive INTEGER DEFAULT 0");
                    stmt.executeUpdate();
                    stmt.close();
                    System.out.println("Added is_sensitive column to answers table");
                }
                catch (SQLException e) {
                    if (e.getMessage().contains("Duplicate column")) {
                        System.out.println("Column is_sensitive already exists in answers table");
                    }
                    else {
                        throw e;
                    }
                }
            }
        }
        catch (SQLException e) {
            System.err.println("Error ensuring sensitive columns exist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a question is marked as sensitive.
     * 
     * @param questionId The ID of the question to check
     * @return true if the question is marked as sensitive, false otherwise
     */
    public boolean isQuestionSensitive(int questionId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT is_sensitive FROM questions WHERE id = ?");
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            
            boolean isSensitive = false;
            if (rs.next()) {
                isSensitive = rs.getInt("is_sensitive") == 1;
            }
            
            rs.close();
            stmt.close();
            return isSensitive;
        } catch (SQLException e) {
            System.err.println("Error checking if question is sensitive: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if an answer is marked as sensitive.
     * 
     * @param answerId The ID of the answer to check
     * @return true if the answer is marked as sensitive, false otherwise
     */
    public boolean isAnswerSensitive(int answerId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT is_sensitive FROM answers WHERE id = ?");
            stmt.setInt(1, answerId);
            ResultSet rs = stmt.executeQuery();
            
            boolean isSensitive = false;
            if (rs.next()) {
                isSensitive = rs.getInt("is_sensitive") == 1;
            }
            
            rs.close();
            stmt.close();
            return isSensitive;
        } catch (SQLException e) {
            System.err.println("Error checking if answer is sensitive: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sets the sensitivity flag for a question.
     * 
     * @param questionId The ID of the question to update
     * @param isSensitive Whether the question should be marked as sensitive
     * @return true if the operation was successful, false otherwise
     */
    public boolean setQuestionSensitivity(int questionId, boolean isSensitive) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE questions SET is_sensitive = ? WHERE id = ?");
            stmt.setInt(1, isSensitive ? 1 : 0);
            stmt.setInt(2, questionId);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error setting question sensitivity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sets the sensitivity flag for an answer.
     * 
     * @param answerId The ID of the answer to update
     * @param isSensitive Whether the answer should be marked as sensitive
     * @return true if the operation was successful, false otherwise
     */
    public boolean setAnswerSensitivity(int answerId, boolean isSensitive) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE answers SET is_sensitive = ? WHERE id = ?");
            stmt.setInt(1, isSensitive ? 1 : 0);
            stmt.setInt(2, answerId);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error setting answer sensitivity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads answers for a question from the database, including sensitivity flags.
     * Overrides the existing method to include the sensitive flag in the Answer objects.
     * 
     * @param questionId The ID of the question to load answers for
     * @return A list of answers for the specified question
     */
    public List<Answer> loadAnswersForQuestion(int questionId) {
        List<Answer> answers = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT a.id, a.text, a.answered_by, " +
                    "a.upvotes, a.downvotes, a.is_correct, a.is_sensitive " +
                    "FROM answers a WHERE a.question_id = ?");
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String answeredBy = rs.getString("answered_by");
                int upvotes = rs.getInt("upvotes");
                int downvotes = rs.getInt("downvotes");
                boolean isCorrect = rs.getBoolean("is_correct");
                boolean isSensitive = rs.getInt("is_sensitive") == 1;
                
                Answer answer = new Answer(text, answeredBy);
                answer.setId(id);
                answer.setUpvote(upvotes);
                answer.setDownvote(downvotes);
                if (isCorrect) {
                    answer.markAsCorrect();
                }
                if (isSensitive) {
                    answer.markAsSensitive();
                }
                
                answers.add(answer);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading answers for question: " + e.getMessage());
            e.printStackTrace();
        }
        
        return answers;
    }

    /**
     * Loads all questions from the database with their sensitivity flags.
     * Overrides the existing method to include the sensitive flag in the Question objects.
     * 
     * @return An ObservableList of all questions in the database
     */
    public ObservableList<Question> loadAllQuestions() {
        ObservableList<Question> allQuestions = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, body, asked_by, upvotes, downvotes, is_sensitive FROM questions");
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String body = rs.getString("body");
                String askedBy = rs.getString("asked_by");
                int upvotes = rs.getInt("upvotes");
                int downvotes = rs.getInt("downvotes");
                boolean isSensitive = rs.getInt("is_sensitive") == 1;
                
                Question question = new Question(body, askedBy);
                question.setUpvotes(upvotes);
                question.setDownvotes(downvotes);
                if (isSensitive) {
                    question.markAsSensitive();
                }
                
                // Load answers for this question
                List<Answer> answers = loadAnswersForQuestion(id);
                for (Answer answer : answers) {
                    question.addAnswer(answer);
                }
                
                allQuestions.add(question);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading questions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return allQuestions;
    }
    
    
}