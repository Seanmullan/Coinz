package mullan.sean.coinz;

public class Friend {

    private String mUserId;
    private String mUsername;

    public Friend(String userId, String username) {
        this.mUserId   = userId;
        this.mUsername = username;
    }

    public String getUserID() { return mUserId; }

    public String getUsername() { return mUsername; }
}
