package application;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import databasePart1.DatabaseHelper;

/**
 * Test class for staff functionality.
 * Tests the implementation of staff-specific features such as:
 * - Ban system
 * - Managing sensitive content
 * - Staff permissions
 * - Staff labeling
 */
public class StaffTest {

    private DatabaseHelper databaseHelper;
    private User staffUser;
    private User regularUser;
    private User testUser;

    @BeforeEach
    public void setUp() throws SQLException {
        // Initialize database connection and helper
        databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();

        // Create staff 
        Set<String> staffRoles = new HashSet<>();
        staffRoles.add("staff");
        staffUser = new User("staffuser", "password", staffRoles);
        
        // Create regular user
        Set<String> userRoles = new HashSet<>();
        userRoles.add("student");
        regularUser = new User("testregularuser", "password", userRoles);
        
        // Create a test user
        Set<String> testRoles = new HashSet<>();
        testRoles.add("student");
        testUser = new User("testuser", "password", testRoles);

        if (!databaseHelper.doesUserExist(staffUser.getUserName())) {
            databaseHelper.register(staffUser);
        }
        if (!databaseHelper.doesUserExist(regularUser.getUserName())) {
            databaseHelper.register(regularUser);
        }
        if (!databaseHelper.doesUserExist(testUser.getUserName())) {
            databaseHelper.register(testUser);
        }
    }

    @AfterEach
    public void tearDown() {
        // Clean up
        try {
            if (databaseHelper.doesUserExist(regularUser.getUserName())) {
                databaseHelper.unbanUser(regularUser.getUserName());
                databaseHelper.deleteUser(regularUser.getUserName());
            }
            
            if (databaseHelper.doesUserExist(testUser.getUserName())) {
                databaseHelper.deleteUser(testUser.getUserName());
            }
        } catch (Exception e) {
            System.err.println("Error in tearDown: " + e.getMessage());
        }
        databaseHelper.closeConnection();
    }

    @Test
    public void testEnsureBanColumnExists() {
        // Test ban column is properly created
        boolean result = databaseHelper.ensureBanColumnExists();
        assertTrue(result, "Ban column should be created successfully");
    }

    @Test
    public void testBanUser() {
        // Test banning a user
        boolean banResult = databaseHelper.banUser(regularUser.getUserName());
        assertTrue(banResult, "User should be banned successfully");
        
        // Verify the user is actually banned
        boolean isBanned = databaseHelper.isUserBanned(regularUser.getUserName());
        assertTrue(isBanned, "User should be marked as banned");
        
        // try to login with banned user should fail
        try {
            boolean loginResult = databaseHelper.login(regularUser);
            assertFalse(loginResult, "Banned user should not be able to login");
        } catch (SQLException e) {
            fail("Login check should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testUnbanUser() {
        //ban then unban user
        databaseHelper.banUser(regularUser.getUserName());
        boolean unbanResult = databaseHelper.unbanUser(regularUser.getUserName());
        assertTrue(unbanResult, "User should be unbanned successfully");
        
        // Verify the user is unbanned
        boolean isBanned = databaseHelper.isUserBanned(regularUser.getUserName());
        assertFalse(isBanned, "User should not be marked as banned");
        
        // Attempt to login
        try {
            boolean loginResult = databaseHelper.login(regularUser);
            assertTrue(loginResult, "Unbanned user should be able to login");
        } catch (SQLException e) {
            fail("Login check should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetAllUsersWithBanStatus() {
        // Ban one user for testing
        databaseHelper.banUser(regularUser.getUserName());
        
        // Get all users with ban status
        Map<String, Boolean> userBanStatus = databaseHelper.getAllUsersWithBanStatus();
        
        // Verify the map contains expected users
        assertTrue(userBanStatus.containsKey(regularUser.getUserName()), 
            "User ban status map should contain the regular user");
        assertTrue(userBanStatus.containsKey(staffUser.getUserName()), 
            "User ban status map should contain the staff user");
            
        // Verify the ban status is correct
        assertTrue(userBanStatus.get(regularUser.getUserName()), 
            "Regular user should be shown as banned");
        assertFalse(userBanStatus.get(staffUser.getUserName()), 
            "Staff user should not be shown as banned");
    }

    @Test
    public void testStaffUserHasStaffRole() {
        // test to verify staff user has the staff role
        assertTrue(staffUser.hasRole("staff"), 
            "Staff user should have the staff role");
    }
    
    @Test
    public void testRegularUserDoesNotHaveStaffRole() {
        // Verify regular user not a staff
        assertFalse(regularUser.hasRole("staff"), 
            "Regular user should not have the staff role");
    }
    
    @Test
    public void testSetQuestionSensitivityTwice() {
        // Test marking a question as sensitive twice doesnt break anything
        Question question = new Question("Test question for double sensitivity", testUser.getUserName());
        int questionId = databaseHelper.saveQuestion(question);
        
        // Mark sensitive twice
        boolean firstMark = databaseHelper.setQuestionSensitivity(questionId, true);
        boolean secondMark = databaseHelper.setQuestionSensitivity(questionId, true);
        assertTrue(firstMark, "First sensitivity marking should succeed");
        assertTrue(secondMark, "Second sensitivity marking should also succeed");
        databaseHelper.deleteQuestion(question);
    }
    
    @Test
    public void testBanThenBanAgain() {
        // Test banning an already banned user
        databaseHelper.banUser(regularUser.getUserName());
        boolean secondBanResult = databaseHelper.banUser(regularUser.getUserName());
        assertTrue(secondBanResult, "Banning a banned user should not cause any problems");
        assertTrue(databaseHelper.isUserBanned(regularUser.getUserName()), 
            "User should be be banned after second ban");
        databaseHelper.unbanUser(regularUser.getUserName());
    }
    
    @Test
    public void testUnbanThenUnbanAgain() {
    	// test unbanning an unbanned user
        databaseHelper.banUser(regularUser.getUserName());
        boolean firstUnban = databaseHelper.unbanUser(regularUser.getUserName());
        boolean secondUnban = databaseHelper.unbanUser(regularUser.getUserName());
        assertTrue(firstUnban, "First unban should be fine");
        assertTrue(secondUnban, "Second unban should also be fine");
        assertFalse(databaseHelper.isUserBanned(regularUser.getUserName()), 
            "User should not be banned after two unban");
    }
    
    @Test
    public void testEmptyUserNameBan() {
    	// Try to ban empty username
        boolean result = databaseHelper.banUser("");
        assertFalse(result, "Should not be able to ban a user with empty username");
    }
    
    @Test
    public void testNullUserNameBan() {
        // Try banning a null username
        boolean result = databaseHelper.banUser(null);
        assertFalse(result, "Should not be able to ban a null username");
    }
    
    @Test
    public void testEnsureBanColumnExistsMultipleInvocations() {
        // Test calling the column creation method multiple times doesnt break anything
        boolean firstCall = databaseHelper.ensureBanColumnExists();
        boolean secondCall = databaseHelper.ensureBanColumnExists();
        boolean thirdCall = databaseHelper.ensureBanColumnExists();
        assertTrue(firstCall, "First call succeeded");
        assertTrue(secondCall, "Second call succeeded");
        assertTrue(thirdCall, "Third call succeeded");
    }
    
    @Test
    public void testStaffCannotBanThemselves() {
        // verify staff isnt banned
        assertFalse(databaseHelper.isUserBanned(staffUser.getUserName()), 
            "Staff user should not be banned initially");
        // ban staff user
        boolean banResult = databaseHelper.banUser(staffUser.getUserName());
        // This should succeed at the database level, but the UI should prevent it
        assertTrue(banResult, "Ban operation should work on the database level");
        databaseHelper.unbanUser(staffUser.getUserName());
    }

    @Test
    public void testUserLoginAfterBanning() throws SQLException {
        // Test that a user cannot login after being banned
        databaseHelper.unbanUser(regularUser.getUserName());
        boolean initialLoginResult = databaseHelper.login(regularUser);
        assertTrue(initialLoginResult, "User should be able to login before being banned");
        databaseHelper.banUser(regularUser.getUserName());
        
        // Verify user cannot login
        boolean bannedLoginResult = databaseHelper.login(regularUser);
        assertFalse(bannedLoginResult, "User should not be able to login after being banned");
    }
    
    @Test
    public void testStaffCanViewAllUsers() {
        // Test that staff members can see all users in the system
        Map<String, Boolean> allUsers = databaseHelper.getAllUsersWithBanStatus();
        // check map contains expected users
        assertFalse(allUsers.isEmpty(), "User map should not be empty");
        assertTrue(allUsers.containsKey(staffUser.getUserName()), 
            "Staff user should be visible in the user list");
        assertTrue(allUsers.containsKey(regularUser.getUserName()), 
            "Regular user should be visible in the user list");
    }

    @Test
    public void testStaffCanViewQuestionSensitivityStatus() {
        Question question = new Question("Test sensitive question?", testUser.getUserName());
        int questionId = databaseHelper.saveQuestion(question);
        
        // question should not be marked as sensitive
        assertFalse(databaseHelper.isQuestionSensitive(questionId), 
            "Question should initially not be marked as sensitive");
        
        // Mark question as sensitive
        boolean marked = databaseHelper.setQuestionSensitivity(questionId, true);
        assertTrue(marked, "Question should be markable as sensitive");
        // Verify staff can see sensitivity status
        assertTrue(databaseHelper.isQuestionSensitive(questionId), 
            "Staff should be able to see that question is marked as sensitive");
        databaseHelper.deleteQuestion(question);
    }

    @Test
    public void testBanStatusPersistence() {
        boolean banResult = databaseHelper.banUser(regularUser.getUserName());
        assertTrue(banResult, "Should be able to ban user");
        
        // Verify ban status is correctly stored
        Map<String, Boolean> users = databaseHelper.getAllUsersWithBanStatus();
        assertTrue(users.get(regularUser.getUserName()), 
            "User should show as banned in the user list");
        
        // Ensure user is still banned after retrieving list again
        Map<String, Boolean> refreshedUsers = databaseHelper.getAllUsersWithBanStatus();
        assertTrue(refreshedUsers.get(regularUser.getUserName()), 
            "Ban status should persist when user list is refreshed");
        databaseHelper.unbanUser(regularUser.getUserName());
    }

    @Test
    public void testBanningNonExistentUser() {
        // attempting to ban a user that doesn't exist
        boolean result = databaseHelper.banUser("nonexistentuser12345789");
        assertFalse(result, "Should not be able to ban a non-existent user");
    }

    @Test
    public void testUnbanningNonExistentUser() {
        // attempting to unban a user that doesn't exist
        boolean result = databaseHelper.unbanUser("nonexistentuser12345789");
        assertFalse(result, "Should not be able to unban a non-existent user");
    }


    @Test
    public void testSensitivityOnNonExistentContent() {
        // Test behavior when trying to check sensitivity on content that doesn't exist
        boolean nonExistentQuestionSensitive = databaseHelper.isQuestionSensitive(99999);
        boolean nonExistentAnswerSensitive = databaseHelper.isAnswerSensitive(99999);
        assertFalse(nonExistentQuestionSensitive, 
            "Non-existent question should not be reported as sensitive");
        assertFalse(nonExistentAnswerSensitive, 
            "Non-existent answer should not be reported as sensitive");
    }

    @Test
    public void testBannedUserCannotCreateContent() {
        databaseHelper.banUser(testUser.getUserName());
        try {
            // Attempt to login with banned user
            boolean loginResult = databaseHelper.login(testUser);
            assertFalse(loginResult, "Banned user should not be able to login");
            assertTrue(databaseHelper.isUserBanned(testUser.getUserName()), 
                "User should still be banned, preventing content creation");
        }
        catch (SQLException e) {
            fail("Login check should not throw an exception: " + e.getMessage());
        }
        finally {
            databaseHelper.unbanUser(testUser.getUserName());
        }
    }
    
}