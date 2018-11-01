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

public class FriendsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "C_FRIENDS";

    private RecyclerView mRecyclerViewFriends;
    private RecyclerView mRecyclerViewRequests;
    private FriendAdapter mFriendsAdapter;
    private ArrayList<Friend> mFriends;

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

        mFriends = Data.getFriends();

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

        // Add line a line between each object in the recycler view list
        mRecyclerViewFriends.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerViewRequests.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));

        // Set adapters
        mRecyclerViewFriends.setAdapter(mFriendsAdapter);

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
                mFriends = Data.getFriends();
                mRecyclerViewRequests.setVisibility(View.INVISIBLE);
                mRecyclerViewFriends.setVisibility(View.VISIBLE);
                mFriendsAdapter = new FriendAdapter(mFriends);
                mRecyclerViewFriends.setAdapter(mFriendsAdapter);
                break;
            case R.id.btn_requests:
                mRecyclerViewFriends.setVisibility(View.INVISIBLE);
                mRecyclerViewRequests.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}