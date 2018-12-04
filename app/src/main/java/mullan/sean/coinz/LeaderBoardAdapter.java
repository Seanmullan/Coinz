package mullan.sean.coinz;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 *   Adapter class to store User objects for leader board
 */
public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.MyViewHolder> {

    private ArrayList<User> mUsers;

    /**
     *   Provides a reference to the views for each data item (username and amount of gold)
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mUsername;
        public TextView mGold;
        public MyViewHolder(View view) {
            super(view);
            mUsername = view.findViewById(R.id.name);
            mGold     = view.findViewById(R.id.gold);
        }
    }

    /**
     * @param users ArrayList of User objects
     */
    public LeaderBoardAdapter(ArrayList<User> users) {
        this.mUsers = users;
    }

    /**
     *  Inflate layout of list of users (invoked by layout manager)
     */
    @Override
    @Nonnull
    public LeaderBoardAdapter.MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_list_row, parent, false);
        return new LeaderBoardAdapter.MyViewHolder(itemView);
    }

    /**
     *   Bind the user data to the specified row in the users list (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(@Nonnull LeaderBoardAdapter.MyViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.mUsername.setText(user.getUsername());
        holder.mGold.setText(String.format(Locale.getDefault(), "%.6f", user.getGold()));
    }

    /**
     *  @return Size of users ArrayList (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
