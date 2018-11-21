package mullan.sean.coinz;

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
     *  @brief  { Two User objects are equal if they have the same ID (since
     *            ID's are unique, this means it is the same user) }
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
