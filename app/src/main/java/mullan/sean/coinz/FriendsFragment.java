package mullan.sean.coinz;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public class FriendsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "C_FRIENDS";

    private RecyclerView mRecyclerViewFriends;
    private RecyclerView mRecyclerViewRequests;
    private FriendAdapter mFriendsAdapter;
    private RequestAdapter mRequestAdapter;
    private ArrayList<Friend> mFriends;
    private ArrayList<Friend> mRequests;

    /*
     * @brief { Required empty public constructor }
     */
    public FriendsFragment() {
    }

    /*
     *  @brief  { Invoke onCreate of superclass }
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     *  @brief  { Inflate the layout, and initialise adapter and recycler view.
     *            Set default display to collected coins, and add listeners
     *            for collected and received buttons }
     */
    @Override
    public View onCreateView(@Nonnull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        // Populate UI with current data
        mFriends  = Data.getFriends();
        mRequests = Data.getRequests();

        // Retrieve most up to date friend and requests data in background
        updateFriendsInBackground();
        updateRequestsInBackground();

        FloatingActionButton fabAddFriend = view.findViewById(R.id.addfriend);
        Button btnFriends = view.findViewById(R.id.btn_friends);
        Button btnRequests = view.findViewById(R.id.btn_requests);

        mRecyclerViewFriends = view.findViewById(R.id.friends_recycler_view);
        mRecyclerViewRequests = view.findViewById(R.id.requests_recycler_view);
        mRecyclerViewRequests.setVisibility(View.INVISIBLE);

        // Set layout manager
        RecyclerView.LayoutManager mLayoutFriends = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewFriends.setLayoutManager(mLayoutFriends);
        RecyclerView.LayoutManager mLayoutRequests = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewRequests.setLayoutManager(mLayoutRequests);

        // Initialise adapter with coins ArrayList
        mFriendsAdapter = new FriendAdapter(mFriends);

        mRequestAdapter = new RequestAdapter(mRequests,
                position -> {
                    Friend friend = mRequests.get(position);
                    Data.acceptFriendRequest(friend);
                    String msg = "Friend request accepted: " + friend.getUsername();
                    Toast.makeText(inflater.getContext(), msg, Toast.LENGTH_SHORT).show();
                    updateRequestsView();
                }, position -> {
                    Friend friend = mRequests.get(position);
                    Data.declineFriendRequest(friend);
                    String msg = "Friend request declined: " + friend.getUsername();
                    Toast.makeText(inflater.getContext(), msg, Toast.LENGTH_SHORT).show();
                    updateRequestsView();
                });

        // Add line a line between each object in the recycler view list
        mRecyclerViewFriends.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerViewRequests.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));

        // Set adapters
        mRecyclerViewFriends.setAdapter(mFriendsAdapter);
        mRecyclerViewRequests.setAdapter(mRequestAdapter);

        fabAddFriend.setOnClickListener(this);
        btnFriends.setOnClickListener(this);
        btnRequests.setOnClickListener(this);

        return view;
    }

    /*
     *  @brief  { Update the recycler view with new data }
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "[onClick] updating recycler view");
        switch (v.getId()) {
            case R.id.btn_friends:
                updateFriendsView();
                break;
            case R.id.btn_requests:
                updateRequestsView();
                break;
            case R.id.addfriend:
                openAddFriendDialogue();
            default:
                break;
        }
    }

    /*
     *  @brief  { Retrieves updated data, makes requests recycler invisible
     *            and makes friends recycler visible, then notifies adapter
     *            of data change }
     */
    private void updateFriendsView() {
        mFriends = Data.getFriends();
        mRecyclerViewRequests.setVisibility(View.INVISIBLE);
        mRecyclerViewFriends.setVisibility(View.VISIBLE);
        mFriendsAdapter.notifyDataSetChanged();
    }

    /*
     *  @brief  { Retrieves updated data, makes friends recycler invisible
     *            and makes recycler recycler visible, then notifies adapter
     *            of data change }
     */
    private void updateRequestsView() {
        mRequests = Data.getRequests();
        mRecyclerViewFriends.setVisibility(View.INVISIBLE);
        mRecyclerViewRequests.setVisibility(View.VISIBLE);
        mRequestAdapter.notifyDataSetChanged();
    }

    /*
     *  @brief  { In the background, retrieve most up to date data from friends collection.
     *            If retrieved data is different from current data, then update the UI
     *            with most up to date data }
     */
    private void updateFriendsInBackground() {
        Log.d(TAG, "[updateFriendsInBackground] updating...");
        Data.retrieveAllFriends(new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                // If friends view is currently visible, update the view (which also updates
                // the data), otherwise just update the data
                if (mRecyclerViewFriends.getVisibility() == View.VISIBLE) {
                    updateFriendsView();
                } else {
                    mFriends = Data.getFriends();
                }
                Log.d(TAG, "[updateFriendsInBackground] updated");
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "[updateFriendsInBackground] couldn't retrieve friends: ", e);
            }
        });
    }

    /*
     *  @brief  { In the background, retrieve most up to date data from friend requests.
     *            If retrieved data is different from current data, then update the UI
     *            with most up to date data }
     */
    private void updateRequestsInBackground() {
        Log.d(TAG, "[updateRequestsInBackground] updating...");
        Data.retrieveAllRequests(new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                // If requests view is currently visible, update the view (which also updates
                // the data), otherwise just update the data
                if (mRecyclerViewRequests.getVisibility() == View.VISIBLE) {
                    updateRequestsView();
                } else {
                    mRequests = Data.getRequests();
                }
                Log.d(TAG, "[updateRequestsInBackground] updated");
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "[updateRequestsInBackground] couldn't retrieve requests: ", e);
            }
        });
    }

    /*
     *  @brief  { Opens a dialogue with the user and prompts them to enter the email
     *            address of the friend they wish to add. The sendFriendRequest method
     *            is then called in the data class with the entered email }
     */
    @SuppressWarnings("unchecked")
    private void openAddFriendDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getLayoutInflater().getContext());
        builder.setTitle("Enter their email address");

        // Set up the input
        final EditText input = new EditText(getLayoutInflater().getContext());

        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String email = input.getText().toString();

            // Send friend request
            Data.sendFriendRequest(email, new OnEventListener() {
                @Override
                public void onSuccess(Object object) {
                    Toast.makeText(getLayoutInflater().getContext(),
                            R.string.msg_friend_request,
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Nothing found");
                    Toast.makeText(getLayoutInflater().getContext(),
                            R.string.msg_add_friend_failed,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Display builder
        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}