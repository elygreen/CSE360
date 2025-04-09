package application;

/**
 * <p> Title: Directed Graph-translated Password Assessor. </p>
 * 
 * <p> Description: A demonstration of the mechanical translation of Directed Graph 
 * diagram into an executable Java program using the Password Evaluator Directed Graph. 
 * The code detailed design is based on a while loop with a cascade of if statements</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 0.00		2018-02-22	Initial baseline 
 * 
 */
public class PasswordEvaluator {

    /**********************************************************************************************
     * 
     * Result attributes to be used for GUI applications where a detailed error message and a 
     * pointer to the character of the error will enhance the user experience.
     * 
     */

    /**
     * The error message text that describes any password validation failures
     */
    public static String passwordErrorMessage = "";		

    /**
     * The input being processed
     */
    public static String passwordInput = "";			

    /**
     * The index where the error was located in the password
     */
    public static int passwordIndexofError = -1;		

    /**
     * Flag indicating whether an uppercase letter was found in the password
     */
    public static boolean foundUpperCase = false;

    /**
     * Flag indicating whether a lowercase letter was found in the password
     */
    public static boolean foundLowerCase = false;

    /**
     * Flag indicating whether a numeric digit was found in the password
     */
    public static boolean foundNumericDigit = false;

    /**
     * Flag indicating whether a special character was found in the password
     */
    public static boolean foundSpecialChar = false;

    /**
     * Flag indicating whether the password is at least 8 characters long
     */
    public static boolean foundLongEnough = false;

    /**
     * The input line being evaluated
     */
    private static String inputLine = "";				

    /**
     * The current character being examined in the password evaluation process
     */
    private static char currentChar;					

    /**
     * The index of the current character being evaluated
     */
    private static int currentCharNdx;					

    /**
     * Flag that specifies if the FSM (Finite State Machine) is running
     */
    private static boolean running;						

    /**
     * This private method displays the input line and then on a line under it displays an up arrow
     * at the point where an error is detected. This method is designed to be used to 
     * display the error message on the console terminal.
     * 
     * The method prints:
     * 1. The entire input line
     * 2. A question mark at the position of the current character
     * 3. Information about the password size, current character index, and the current character
     */
    private static void displayInputState() {
        // Display the entire input line
        System.out.println(inputLine);
        System.out.println(inputLine.substring(0,currentCharNdx) + "?");
        System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + 
                currentCharNdx + "  |  The currentChar: \"" + currentChar + "\"");
    }

    /**
     * This method evaluates a password string according to specific criteria:
     * - Must contain at least one uppercase letter
     * - Must contain at least one lowercase letter
     * - Must contain at least one numeric digit
     * - Must contain at least one special character
     * - Must be at least 8 characters long
     * 
     * The method implements a mechanical transformation of a Directed Graph diagram into code,
     * examining each character in the input string and updating flags for each requirement.
     * 
     * @param input The password string to evaluate
     * @return An empty string if the password meets all criteria, or an error message listing
     *         the specific criteria that were not met
     */
    public static String evaluatePassword(String input) {
        // The following are the local variable used to perform the Directed Graph simulation
        passwordErrorMessage = "";
        passwordIndexofError = 0;			// Initialize the IndexofError
        inputLine = input;					// Save the reference to the input line as a global
        currentCharNdx = 0;					// The index of the current character
        
        if(input.length() <= 0) return "*** Error *** The password is empty!";
        
        // The input is not empty, so we can access the first character
        currentChar = input.charAt(0);		// The current character from the above indexed position

        // The Directed Graph simulation continues until the end of the input is reached or at some 
        // state the current character does not match any valid transition to a next state

        passwordInput = input;				// Save a copy of the input
        foundUpperCase = false;				// Reset the Boolean flag
        foundLowerCase = false;				// Reset the Boolean flag
        foundNumericDigit = false;			// Reset the Boolean flag
        foundSpecialChar = false;			// Reset the Boolean flag
        foundNumericDigit = false;			// Reset the Boolean flag - Note: This is duplicated in the original code
        foundLongEnough = false;			// Reset the Boolean flag
        running = true;						// Start the loop

        // The Directed Graph simulation continues until the end of the input is reached or at some 
        // state the current character does not match any valid transition
        while (running) {
            displayInputState();
            // The cascading if statement sequentially tries the current character against all of the
            // valid transitions
            if (currentChar >= 'A' && currentChar <= 'Z') {
                System.out.println("Upper case letter found");
                foundUpperCase = true;
            } else if (currentChar >= 'a' && currentChar <= 'z') {
                System.out.println("Lower case letter found");
                foundLowerCase = true;
            } else if (currentChar >= '0' && currentChar <= '9') {
                System.out.println("Digit found");
                foundNumericDigit = true;
            } else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) {
                System.out.println("Special character found");
                foundSpecialChar = true;
            } else {
                passwordIndexofError = currentCharNdx;
                return "*** Error *** An invalid character has been found!";
            }
            if (currentCharNdx >= 7) {
                System.out.println("At least 8 characters found");
                foundLongEnough = true;
            }
            
            // Go to the next character if there is one
            currentCharNdx++;
            if (currentCharNdx >= inputLine.length())
                running = false;
            else
                currentChar = input.charAt(currentCharNdx);
            
            System.out.println();
        }
        
        String errMessage = "";
        if (!foundUpperCase)
            errMessage += "Upper case; ";
        
        if (!foundLowerCase)
            errMessage += "Lower case; ";
        
        if (!foundNumericDigit)
            errMessage += "Numeric digits; ";
            
        if (!foundSpecialChar)
            errMessage += "Special character; ";
            
        if (!foundLongEnough)
            errMessage += "Long Enough; ";
        
        if (errMessage == "")
            return "";
        
        passwordIndexofError = currentCharNdx;
        return errMessage + "conditions were not satisfied";
    }
}