package mullan.sean.coinz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import javax.annotation.Nonnull;

/**
 *   Fragment that hosts friends and global leader board. Each row contains the username
 *   and the amount of gold that user has
 */
public class LeaderBoardFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "C_BOARD";

    private ArrayList<User>    mFriends;
    private ArrayList<User>    mGlobal;
    private RecyclerView       mRecyclerViewFriends;
    private RecyclerView       mRecyclerViewGlobal;
    private LeaderBoardAdapter mFriendsAdapter;
    private LeaderBoardAdapter mGlobalAdapter;

    /**
     *  Required empty public constructor
     */
    public LeaderBoardFragment() {}

    /**
     *   Invoke onCreate() of superclass
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *   Inflate the layout, and initialise adapter and recycler view.
     *   Set default display to friends leader board, and add listeners
     *   for friends and global buttons
     */
    @Override
    public View onCreateView(@Nonnull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leader_board, container, false);

        // Populate UI with current data
        mFriends = Data.getFriendLeaderBoard();
        Log.d(TAG, "Friends:" + mFriends);
        mGlobal  = Data.getGlobalLeaderBoard();

        // Retrieve most up to date leader board data in background
        updateLeaderBoardInBackground();

        Button btnFriends = view.findViewById(R.id.btn_friends_lb);
        Button btnGlobal  = view.findViewById(R.id.btn_global_lb);

        mRecyclerViewFriends = view.findViewById(R.id.friends_lb_recycler_view);
        mRecyclerViewGlobal  = view.findViewById(R.id.global_lb_recycler_view);
        mRecyclerViewGlobal.setVisibility(View.INVISIBLE);

        // Set layout manager
        RecyclerView.LayoutManager mLayoutFriends = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewFriends.setLayoutManager(mLayoutFriends);
        RecyclerView.LayoutManager mLayoutGlobal  = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewGlobal.setLayoutManager(mLayoutGlobal);

        // Initialise adapters
        mFriendsAdapter = new LeaderBoardAdapter(mFriends);
        mGlobalAdapter = new LeaderBoardAdapter(mGlobal);

        // Add line a line between each object in the recycler view list
        mRecyclerViewFriends.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerViewGlobal.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));

        // Set adapters
        mRecyclerViewFriends.setAdapter(mFriendsAdapter);
        mRecyclerViewGlobal.setAdapter(mGlobalAdapter);

        btnFriends.setOnClickListener(this);
        btnGlobal.setOnClickListener(this);

        return view;
    }

    /**
     *   Update the recycler view with new data
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "[onClick] updating recycler view");
        switch (v.getId()) {
            case R.id.btn_friends_lb:
                updateFriendsView();
                break;
            case R.id.btn_global_lb:
                updateGlobalView();
                break;
            default:
                break;
        }
    }

    /**
     *   Retrieves updated data, makes global recycler invisible and makes friends
     *   recycler visible, then notifies adapter of data change
     */
    private void updateFriendsView() {
        mFriends = Data.getFriendLeaderBoard();
        Log.d(TAG, "Friends:" + mFriends);
        mFriends.sort((a,b)->Double.compare(b.getGold(), a.getGold()));
        mRecyclerViewGlobal.setVisibility(View.INVISIBLE);
        mRecyclerViewFriends.setVisibility(View.VISIBLE);
        mFriendsAdapter.notifyDataSetChanged();
    }

    /**
     *   Retrieves updated data, makes friends recycler invisible and makes global
     *   recycler visible, then notifies adapter of data change
     */
    private void updateGlobalView() {
        mGlobal = Data.getGlobalLeaderBoard();
        mGlobal.sort((a,b)->Double.compare(b.getGold(), a.getGold()));
        mRecyclerViewFriends.setVisibility(View.INVISIBLE);
        mRecyclerViewGlobal.setVisibility(View.VISIBLE);
        mGlobalAdapter.notifyDataSetChanged();
    }

    /**
     *   In the background, retrieve most up to date data from leader board.
     *   If retrieved data is different from current data, then update the UI
     *   with most up to date data
     */
    private void updateLeaderBoardInBackground() {
        Log.d(TAG, "[updateLeaderBoardInBackground] updating...");
        Data.retrieveLeaderBoard(new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                // Update the currently displayed view (which also updates
                // the data) and the data
                if (mRecyclerViewFriends.getVisibility() == View.VISIBLE) {
                    updateFriendsView();
                    mGlobal = Data.getGlobalLeaderBoard();
                } else {
                    updateGlobalView();
                    mFriends = Data.getFriendLeaderBoard();
                }
                Log.d(TAG, "[updateLeaderBoardInBackground] updated");
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "[updateLeaderBoardInBackground] failed: ", e);
            }
        });
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