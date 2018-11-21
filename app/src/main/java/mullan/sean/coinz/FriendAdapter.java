package mullan.sean.coinz;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyViewHolder> {

    private ArrayList<User> mFriends;

    /*
     *  @brief  { Provides a reference to the views for each data item }
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

    /*
     *  @brief  { Adapter constructor }
     */
    public FriendAdapter(ArrayList<User> friends) {
        this.mFriends = friends;
    }

    /*
     *  @brief  { Create new views (invoked by the layout manager) }
     */
    @Override
    @Nonnull
    public FriendAdapter.MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_list_row, parent, false);
        return new FriendAdapter.MyViewHolder(itemView);
    }

    /*
     *  @brief  { Replace the contents of a view (invoked by the layout manager) }
     */
    @Override
    public void onBindViewHolder(@Nonnull FriendAdapter.MyViewHolder holder, int position) {
        User friend = mFriends.get(position);
        holder.mUsername.setText(friend.getUsername());
        holder.mEmail.setText(friend.getEmail());
    }

    /*
     *  @return  { Size of your friends ArrayList (invoked by the layout manager) }
     */
    @Override
    public int getItemCount() {
        return mFriends.size();
    }
}
