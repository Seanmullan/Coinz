package mullan.sean.coinz;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import javax.annotation.Nonnull;

/**
 *   Adapter class to store Friend objects
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyViewHolder> {

    private ArrayList<User> mFriends;

    /**
     *   Provides a reference to the views for each data item (username and email)
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mUsername;
        public TextView mEmail;
        public MyViewHolder(View view) {
            super(view);
            mUsername = view.findViewById(R.id.friends_name);
            mEmail    = view.findViewById(R.id.friends_email);
        }
    }

    /**
     * @param friends ArrayList of User objects that represent the users friends
     */
    public FriendAdapter(ArrayList<User> friends) {
        this.mFriends = friends;
    }

    /**
     *  Inflate layout of list of coins (invoked by layout manager)
     */
    @Override
    @Nonnull
    public FriendAdapter.MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_list_row, parent, false);
        return new FriendAdapter.MyViewHolder(itemView);
    }

    /**
     *   Bind the user data to the specified row in the friends list (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(@Nonnull FriendAdapter.MyViewHolder holder, int position) {
        User friend = mFriends.get(position);
        holder.mUsername.setText(friend.getUsername());
        holder.mEmail.setText(friend.getEmail());
    }

    /**
     *  @return  Size of the friends ArrayList (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return mFriends.size();
    }
}
