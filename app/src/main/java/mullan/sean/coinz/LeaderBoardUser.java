package mullan.sean.coinz;

import android.util.Log;

public class LeaderBoardUser {

    private String mUsername;
    private double mGold;

    public LeaderBoardUser(String username, double gold) {
        this.mUsername = username;
        this.mGold     = gold;
    }

    public String getUsername() {
        return mUsername;
    }

    public double getGold() {
        return mGold;
    }

    @Override
    public boolean equals(Object user) {
        if (user.getClass().equals(LeaderBoardUser.class)) {
            return this.mUsername.equals(((LeaderBoardUser) user).mUsername);
        } else {
            return false;
        }
    }
}
