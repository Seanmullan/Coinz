package mullan.sean.coinz;

public class Friend {

    private String mUserId;
    private String mUsername;
    private String mEmail;

    public Friend(String userId, String username, String email) {
        this.mUserId   = userId;
        this.mUsername = username;
        this.mEmail    = email;
    }

    public String getUserID() { return mUserId; }

    public String getUsername() { return mUsername; }

    public String getEmail() { return mEmail; }
}
