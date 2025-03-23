package databasePart1;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import application.Answer;
import application.Question;
import application.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

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

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "role VARCHAR(20))";
		statement.execute(userTable);
		
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
	    
	    try {
	    	DatabaseMetaData dbm = connection.getMetaData();
	    	ResultSet rs = dbm.getColumns(null,  null, "ANSWERS", "ISCORRECT");
	    	if (!rs.next()) {
	    		statement.execute("ALTER TABLE Answers ADD COLUMN isCorrect BOOLEAN DEFAULT FALSE");
	    	}
	    }
	    catch (SQLException e) {
	    	System.err.println("Error adding isCorrect column: " + e.getMessage());
	    }
	}

	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
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
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Generates a new invitation code and inserts it into the database.
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
	
	// Validates an invitation code to check if it is unused.
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
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	//////////////////////////////
	// QUESTION & ANSWER METHODS//
	//////////////////////////////
	
	// Save question to database
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
	    }catch (SQLException e) {
	        e.printStackTrace();
	        return -1;
	    }
	}
	
	// Save answer to database
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
	
	//update questions using ID in database
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

	
	// Load all questions stored in database for display
	public ObservableList<Question> loadAllQuestions() {
	    ObservableList<Question> questions = FXCollections.observableArrayList();
	    
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
	            questions.add(question);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return questions;
	}
	
	// Load all questions with their IDs stored in the database
	public Map<Question, Integer> loadAllQuestionsWithIDs(){
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
	
	// Loads all answers for a specific question
	private void loadAnswersForQuestion(int questionId, Question question) {
	    String query = "SELECT * FROM Answers WHERE questionId = ? ORDER BY timestamp";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                String text = rs.getString("text");
	                String answeredBy = rs.getString("answeredBy");
	                int upVotes = rs.getInt("upVote");
                    int downVotes = rs.getInt("downVote");
                    boolean isCorrect = rs.getBoolean("isCorrect");
	                
	                Answer answer = new Answer(text, answeredBy);
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
	
	// Deletes a question from the database
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

	// Closes the database connection and statement.
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
	
	// Display users
	public void displayUsernamesAndRoles() throws SQLException {
		String query = "SELECT username, role FROM cse360users";
		try (Statement stmt = connection.createStatement();
		         ResultSet rs = stmt.executeQuery(query)) {

		        // Loop through the result set and print each user's username and role
		        System.out.println("Usernames and Roles:");
		        while (rs.next()) {
		            String userName = rs.getString("userName");
		            String role = rs.getString("role");
		            System.out.println("Username: " + userName + ", Role: " + role);
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
	}
	

}
