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


public class WalletFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "C_WALLET";

    private RecyclerView    mRecyclerView;
    private CoinAdapter     mAdapter;
    private ArrayList<Coin> mCoins;

    /*
     * @brief { Required empty public constructor }
     */
    public WalletFragment(){}

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

        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        mCoins = Data.getCollectedCoins();

        Button btnCollected = view.findViewById(R.id.btn_collected);
        Button btnReceived  = view.findViewById(R.id.btn_received);

        mRecyclerView = view.findViewById(R.id.my_recycler_view);

        // Set layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(inflater.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Initialise adapter with coins ArrayList
        mAdapter = new CoinAdapter(mCoins, inflater.getContext());

        // Add line a line between each object in the recycler view list
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        btnCollected.setOnClickListener(this);
        btnReceived.setOnClickListener(this);

        return view;
    }

    /*
     *  @brief  { Update the recycler view with new data }
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "[onClick] updating recycler view");
        switch(v.getId()) {
            case R.id.btn_collected:
                mCoins = Data.getCollectedCoins();
                break;
            case R.id.btn_received:
                mCoins = Data.getReceivedCoins();
                break;
            default:
                break;
        }
        mAdapter = new CoinAdapter(mCoins, getLayoutInflater().getContext());
        mRecyclerView.setAdapter(mAdapter);
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