package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public static String userNameRecognizerErrorMessage = "";	// The error message text
	public static String userNameRecognizerInput = "";			// The input being processed
	public static int userNameRecognizerIndexofError = -1;		// The index of error location
	private static int state = 0;						// The current state value
	private static int nextState = 0;					// The next state value
	private static boolean finalState = false;			// Is this state a final state?
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running
	private static int userNameSize = 0;			// A numeric value may not exceed 16 characters
	
    /**
     * Validates the username based on Task 5 EFSM
     * @param username The username to validate.
     * @return true if the username is valid, false otherwise.
     */
	
	// Private method to move to the next character within the limits of the input line
	private static void moveToNextCharacter() {
		currentCharNdx++;
		if (currentCharNdx < inputLine.length())
			currentChar = inputLine.charAt(currentCharNdx);
		else {
			currentChar = ' ';
			running = false;
		}
	}

    public static String checkForValidUserName(String input) {
		// Check to ensure that there is input to process
		if(input.length() <= 0) {
			return "\n The username input is empty!";
		}
		
		// The local variables used to perform the Finite State Machine simulation
		state = 0;							// This is the FSM state number
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		currentChar = input.charAt(0);		// The current character from above indexed position

		// The Finite State Machines continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state

		userNameRecognizerInput = input;	// Save a copy of the input
		running = true;						// Start the loop
		nextState = -1;						// There is no next state
		
		// This is the place where semantic actions for a transition to the initial state occur
		
		userNameSize = 0;					// Initialize the UserName size

		// The Finite State Machines continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state
		while (running) {
			// The switch statement takes the execution to the code for the current state, where
			// that code sees whether or not the current character is valid to transition to a
			// next state
			switch (state) {
			case 0: 
				// State 0 has 1 valid transition that is addressed by an if statement.
				
				// The current character is checked against A-Z, a-z. If any are matched
				// the FSM goes to state 1
				
				// UPDATED to make the first character alphabetical only
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' )) {	// Check for a-z
					nextState = 1;
					
					// Count the character 
					userNameSize++;
					
					// This only occurs once, so there is no need to check for the size getting
					// too large.
				}
				// If it is none of those characters, the FSM halts
				else 
					running = false;
				
				// The execution of this state is finished
				break;
			
			case 1: 
				// State 1 has two valid transitions, 
				//	1: a A-Z, a-z, 0-9 that transitions back to state 1
				//  2: a period that transitions to state 2 

				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||	// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {	// Check for 0-9
					nextState = 1;
					
					// Count the character
					userNameSize++;
				}
				// UPDATED to add - and _ as possibilities alongside . between alphanumeric characters
				// . -> State 2
				else if ((currentChar == '.') || 	// Check for .
						(currentChar == '-') ||		// Check for -
						(currentChar == '_')){		// Check for _
					nextState = 2;
					
					// Count the .
					userNameSize++;
				}				
				// If it is none of those characters, the FSM halts
				else
					running = false;
				
				// The execution of this state is finished
				// If the size is larger than 16, the loop must stop
				if (userNameSize > 16)
					running = false;
				break;			
				
			case 2: 
				// State 2 deals with a character after a period in the name.
				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||	// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {	// Check for 0-9
					nextState = 1;
					
					// Count the odd digit
					userNameSize++;
					
				}
				// If it is none of those characters, the FSM halts
				else 
					running = false;

				// The execution of this state is finished
				// If the size is larger than 16, the loop must stop
				if (userNameSize > 16)
					running = false;
				break;			
			}
			
			if (running) {
				// When the processing of a state has finished, the FSM proceeds to the next
				// character in the input and if there is one, it fetches that character and
				// updates the currentChar.  If there is no next character the currentChar is
				// set to a blank.
				moveToNextCharacter();

				// Move to the next state
				state = nextState;
				
				// Is the new state a final state?  If so, signal this fact.
				if (state == 1) finalState = true;

				// Ensure that one of the cases sets this to a valid value
				nextState = -1;
			}
			// Should the FSM get here, the loop starts again
	
		}
		// When the FSM halts, we must determine if the situation is an error or not.  That depends
		// of the current state of the FSM and whether or not the whole string has been consumed.
		// This switch directs the execution to separate code for each of the FSM states and that
		// makes it possible for this code to display a very specific error message to improve the
		// user experience.
		userNameRecognizerIndexofError = currentCharNdx;	// Set index of a possible error;
		userNameRecognizerErrorMessage = "\n*** ERROR *** ";
		
		// The following code is a slight variation to support just console output.
		switch (state) {
		case 0:
			// State 0 is not a final state, so we can return a very specific error message
			// UPDATED: changed to only accept alphabetic starting char
			return "UserName must start with A-Z, or a-z\n";

		case 1:
			// State 1 is a final state.  Check to see if the UserName length is valid.  If so we
			// we must ensure the whole string has been consumed.

			if (userNameSize < 4) {
				// UserName is too small
				return "A UserName must have at least 4 characters.\n";
			}
			else if (userNameSize > 16) {
				// UserName is too long
				return "A UserName must have no more than 16 character.\n";
			}
			else if (currentCharNdx < input.length()) {
				// There are characters remaining in the input, so the input is not valid
				return "A UserName character may only contain the characters A-Z, a-z, 0-9.\n";
			}
			else {
					// UserName is valid
					userNameRecognizerIndexofError = -1;
					return "";
			}

		case 2:
			// UPDATED error message to reflect . _ -
			// State 2 is not a final state, so we can return a very specific error message
			return "A UserName character after a period hyphen or underscore must be A-Z, a-z, 0-9.\n";
			
		default:
			// This is for the case where we have a state that is outside of the valid range.
			// This should not happen
			return "";
		}
	}

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    
	public static String pw_passwordErrorMessage = "";		// The error message text
	public static String pw_passwordInput = "";			// The input being processed
	public static int pw_passwordIndexofError = -1;		// The index where the error was located
	public static boolean pw_foundUpperCase = false;
	public static boolean pw_foundLowerCase = false;
	public static boolean pw_foundNumericDigit = false;
	public static boolean pw_foundSpecialChar = false;
	public static boolean pw_foundLongEnough = false;
	private static String pw_inputLine = "";				// The input line
	private static char pw_currentChar;					// The current character in the line
	private static int pw_currentCharNdx;					// The index of the current character
	private static boolean pw_running;						// The flag that specifies if the FSM is 
														// running
    
    public static String checkForValidPassword(String input) {
    	// The following are the local variable used to perform the Directed Graph simulation
    	pw_passwordErrorMessage = "";
    	pw_passwordIndexofError = 0;			// Initialize the IndexofError
    	pw_inputLine = input;					// Save the reference to the input line as a global
    	pw_currentCharNdx = 0;					// The index of the current character
    			
    	if(input.length() <= 0) return "*** Error *** The password is empty!";
    			
		// The input is not empty, so we can access the first character
    	pw_currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state

    	pw_passwordInput = input;				// Save a copy of the input
    	pw_foundUpperCase = false;				// Reset the Boolean flag
    	pw_foundLowerCase = false;				// Reset the Boolean flag
    	pw_foundNumericDigit = false;			// Reset the Boolean flag
    	pw_foundSpecialChar = false;			// Reset the Boolean flag
    	pw_foundNumericDigit = false;			// Reset the Boolean flag
    	pw_foundLongEnough = false;			// Reset the Boolean flag
    	pw_running = true;						// Start the loop

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition
		while (pw_running) {
			// The cascading if statement sequentially tries the current character against all of the
			// valid transitions
			if (pw_currentChar >= 'A' && pw_currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				pw_foundUpperCase = true;
			} else if (pw_currentChar >= 'a' && pw_currentChar <= 'z') {
				System.out.println("Lower case letter found");
				pw_foundLowerCase = true;
			} else if (pw_currentChar >= '0' && pw_currentChar <= '9') {
				System.out.println("Digit found");
				pw_foundNumericDigit = true;
			// UPDATED special characters to match the definition on the Basic Password Evaluator EFSM
			} else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(pw_currentChar) >= 0) {
				System.out.println("Special character found");
				pw_foundSpecialChar = true;
			} else {
				pw_passwordIndexofError = pw_currentCharNdx;
				return "*** Error *** An invalid character has been found!";
			}
			if (pw_currentCharNdx >= 7) {
				System.out.println("At least 8 characters found");
				pw_foundLongEnough = true;
			}
			
			// Go to the next character if there is one
			pw_currentCharNdx++;
			if (pw_currentCharNdx >= pw_inputLine.length())
				pw_running = false;
			else
				pw_currentChar = input.charAt(pw_currentCharNdx);
			
			System.out.println();
		}
		
		String errMessage = "";
		if (!pw_foundUpperCase)
			errMessage += "Upper case; ";
		
		if (!pw_foundLowerCase)
			errMessage += "Lower case; ";
		
		if (!pw_foundNumericDigit)
			errMessage += "Numeric digits; ";
			
		if (!pw_foundSpecialChar)
			errMessage += "Special character; ";
			
		if (!pw_foundLongEnough)
			errMessage += "Long Enough; ";
		
		if (errMessage == "")
			return "";
		
		pw_passwordIndexofError = pw_currentCharNdx;
		return "Password conditions: " + errMessage + "were not satisfied";
    }
    
    
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            
            /*if (checkForValidUserName(userName)!="") {
                errorLabel.setText(checkForValidUserName(userName));
                return; // Stop further processing
            }*/
            
            String username_validation_message = checkForValidUserName(userName);
            String pw_validation_message = checkForValidPassword(password);
            
            
            try {
            	if (username_validation_message == "") {
            	// Check if the user already exists
	            	if(!databaseHelper.doesUserExist(userName)) {
	            		if (pw_validation_message == "") {
		            		// Validate the invitation code
		            		if(databaseHelper.validateInvitationCode(code)) {
		            			
		            			// Create a new user and register them in the database
				            	User user=new User(userName, password, "user");
				                databaseHelper.register(user);
				                
				             // Navigate to the Welcome Login Page
				                new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
		            		}
		            		else {
		            			errorLabel.setText("Please enter a valid invitation code");
		            		}
	            		}
	            		else {
	            			errorLabel.setText(pw_validation_message);
	            		}
	            	}
	            	else {
	            		errorLabel.setText("This useruserName is taken!!.. Please use another to setup an account");
	            	}
            	}
            	else {
            		errorLabel.setText(username_validation_message);
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField,inviteCodeField, setupButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
