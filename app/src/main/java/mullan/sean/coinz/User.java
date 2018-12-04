package mullan.sean.coinz;

/**
 *  A User object stores the users ID, username, email and the amount of gold they have
 */
public class User {

    private String mUserId;
    private String mUsername;
    private String mEmail;
    private double mGold;

    public User(String userId, String username, String email, double gold) {
        this.mUserId   = userId;
        this.mUsername = username;
        this.mEmail    = email;
        this.mGold     = gold;
    }

    public String getUserID() {
        return mUserId;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public double getGold() {
        return mGold;
    }

    /*
     *  @return True if and only if ID's are equal
     */
    @Override
    public boolean equals(Object user) {
        if (user.getClass().equals(User.class)) {
            return this.mUserId.equals(((User) user).mUserId);
        } else {
            return false;
        }
    }
}
