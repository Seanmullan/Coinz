package mullan.sean.coinz;

import android.app.AlertDialog;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import javax.annotation.Nonnull;


public class WalletFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "C_WALLET";

    private RecyclerView    mRecyclerViewCol;
    private RecyclerView    mRecyclerViewRec;
    private CoinAdapter     mCollectedAdapter;
    private CoinAdapter     mReceivedAdapter;
    private ArrayList<Coin> mCollectedCoins;
    private ArrayList<Coin> mReceivedCoins;

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

        mCollectedCoins = Data.getCollectedCoins();
        mReceivedCoins  = Data.getReceivedCoins();

        FloatingActionButton fabSend = view.findViewById(R.id.sendcoins);
        Button btnCollected = view.findViewById(R.id.btn_collected);
        Button btnReceived  = view.findViewById(R.id.btn_received);

        mRecyclerViewCol = view.findViewById(R.id.col_recycler_view);
        mRecyclerViewRec = view.findViewById(R.id.rec_recycler_view);
        mRecyclerViewRec.setVisibility(View.INVISIBLE);

        // Set layout manager
        RecyclerView.LayoutManager mLayoutCol = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewCol.setLayoutManager(mLayoutCol);
        RecyclerView.LayoutManager mLayoutRec = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewCol.setLayoutManager(mLayoutRec);

        // Initialise adapter with coins ArrayList
        mCollectedAdapter = new CoinAdapter(mCollectedCoins, inflater.getContext());
        mReceivedAdapter  = new CoinAdapter(mReceivedCoins, inflater.getContext());

        // Add line a line between each object in the recycler view list
        mRecyclerViewCol.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerViewRec.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));

        mRecyclerViewCol.setAdapter(mCollectedAdapter);
        mRecyclerViewRec.setAdapter(mReceivedAdapter);

        fabSend.setOnClickListener(this);
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
                updateCollectedView();
                break;
            case R.id.btn_received:
                updateReceivedView();
                break;
            case R.id.sendcoins:
                openSendDialogue();
            default:
                break;
        }
    }

    /*
     *  @brief  { Retrieves updated data, makes received recycler invisible
     *            and makes collected recycler visible, then notifies adapter
     *            of data change }
     */
    private void updateCollectedView() {
        mCollectedCoins = Data.getCollectedCoins();
        mRecyclerViewRec.setVisibility(View.INVISIBLE);
        mRecyclerViewCol.setVisibility(View.VISIBLE);
        mCollectedAdapter.notifyDataSetChanged();
    }

    /*
     *  @brief  { Retrieves updated data, makes friends recycler invisible
     *            and makes recycler recycler visible, then notifies adapter
     *            of data change }
     */
    private void updateReceivedView() {
        mReceivedCoins = Data.getReceivedCoins();
        mRecyclerViewCol.setVisibility(View.INVISIBLE);
        mRecyclerViewRec.setVisibility(View.VISIBLE);
        mReceivedAdapter.notifyDataSetChanged();
    }

    private void openSendDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getLayoutInflater().getContext());
        builder.setTitle("Transfer selected coins");

        // Provide options for user
        CharSequence[] options = {"To bank account", "To a friend"};

        builder.setItems(options, (dialog, which) -> {
            switch(which) {
                case 0:
                    transferToBankAccount();
                    break;
                case 1:
                    transferToFriend();
                    break;
                default:
                    Log.d(TAG, "[openSendDialogue] option not recognised");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void transferToBankAccount() {
        if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
           // TODO: get selected collected coins and transfer to bank
        } else {
            // TODO: get received collected coins and transfer to bank
        }
    }

    private void transferToFriend() {
        ArrayList<Coin> selectedCoins;
        if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
            selectedCoins = getSelectedCoins(Data.COLLECTED);
        } else {
            selectedCoins = getSelectedCoins(Data.RECEIVED);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getLayoutInflater().getContext());
        builder.setTitle("Select friend");
    }

    private ArrayList<Coin> getSelectedCoins(String collection) {
        ArrayList<Coin> selectedCoins = new ArrayList<>();
        if (collection.equals(Data.COLLECTED)) {
            for (Coin c : mCollectedCoins) {
                if (c.isSelected()) {
                    selectedCoins.add(c);
                }
            }
        } else {
            for (Coin c : mReceivedCoins) {
                if (c.isSelected()) {
                    selectedCoins.add(c);
                }
            }
        }
        return selectedCoins;
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