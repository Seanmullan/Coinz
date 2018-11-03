package mullan.sean.coinz;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;


public class WalletFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "C_WALLET";

    private static HashMap<String,Double> mExchangeRates;

    private RecyclerView    mRecyclerViewCol;
    private RecyclerView    mRecyclerViewRec;
    private CoinAdapter     mCollectedAdapter;
    private CoinAdapter     mReceivedAdapter;
    private ArrayList<Coin> mCollectedCoins;
    private ArrayList<Coin> mReceivedCoins;
    private Friend          mSelectedFriend;
    private String          mSelectedTransfer;
    private double          mGoldAmount;

    /*  Flags to indicate if a transfer is in progress. These are required as the Data class
     *  contains two static variables (one for friend transfer, one for bank) that indicate
     *  how many have currently been transferred for a single transfer, so they would become
     *  inaccurate if, for example, two friend transactions were occurring at the same time
     */
    private boolean mFriendTransferInProgress = false;
    private boolean mBankTransferInProgress = false;

    /*  These variables contain the total number of coins that have either successfully or
     *  unsuccessfully been transferred in one transaction - they are used to identify when
     *  the transfer has finished
     */
    private int mFriendTransferTotal;
    private int mBankTransferTotal;

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

        // Retrieve data
        mExchangeRates  = Data.getRates();
        mCollectedCoins = Data.getCollectedCoins();
        mReceivedCoins  = Data.getReceivedCoins();

        // Initialise buttons
        FloatingActionButton fabSend = view.findViewById(R.id.sendcoins);
        Button btnCollected = view.findViewById(R.id.btn_collected);
        Button btnReceived  = view.findViewById(R.id.btn_received);

        // Initialise recycler views
        mRecyclerViewCol = view.findViewById(R.id.col_recycler_view);
        mRecyclerViewRec = view.findViewById(R.id.rec_recycler_view);
        mRecyclerViewRec.setVisibility(View.INVISIBLE);

        // Set layout manager
        RecyclerView.LayoutManager mLayoutCol = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewCol.setLayoutManager(mLayoutCol);
        RecyclerView.LayoutManager mLayoutRec = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewRec.setLayoutManager(mLayoutRec);

        // Initialise adapter with coins ArrayList
        mCollectedAdapter = new CoinAdapter(mCollectedCoins, inflater.getContext());
        mReceivedAdapter  = new CoinAdapter(mReceivedCoins, inflater.getContext());

        // Add line a line between each object in the recycler view list
        mRecyclerViewCol.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerViewRec.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));

        // Set adapters
        mRecyclerViewCol.setAdapter(mCollectedAdapter);
        mRecyclerViewRec.setAdapter(mReceivedAdapter);

        fabSend.setOnClickListener(this);
        btnCollected.setOnClickListener(this);
        btnReceived.setOnClickListener(this);

        return view;
    }

    public static void updateRates() {
        mExchangeRates = Data.getRates();
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

    /*
     *  @brief  { Provides options for the user to transfer their selected coins to their
     *            bank account or a friend. If a transfer is not currently in process,
      *           then begin the transfer }
     */
    private void openSendDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getLayoutInflater().getContext());
        builder.setTitle("Transfer selected coins");

        // Provide options for user
        CharSequence[] options = {"To bank account", "To a friend"};

        // Clear previously selected transfer
        mSelectedTransfer = "";

        // Set selected transfer
        builder.setSingleChoiceItems(options, -1, (dialog, which) -> {
            switch(which) {
                case 0:
                    mSelectedTransfer = "bank";
                    break;
                case 1:
                    mSelectedTransfer = "friend";
                    break;
                default:
                    Log.d(TAG, "[openSendDialogue] option not recognised: " + which);
            }});

        // Execute transfer if one is not already in progress
        builder.setPositiveButton("OK", (dialog, which) -> {
            switch(mSelectedTransfer) {
                case "bank":
                    if(!mBankTransferInProgress) {
                        transferToBankAccount();
                    } else {
                        displayToast("Please wait until current transfer completes");
                    }
                    break;
                case "friend":
                    if (!mFriendTransferInProgress) {
                        friendTransfer();
                    } else {
                        displayToast("Please wait until current transfer completes");
                    }
                    break;
                case "":
                    displayToast("Please select a transfer option");
                default:
                    Log.d(TAG, "[openSendDialogue] transfer not recognised: " + mSelectedTransfer);
            }});

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void transferToBankAccount() {
        ArrayList<Coin> selectedCoins;
        String collection;
        mGoldAmount = 0.0;
        mBankTransferTotal = 0;

        if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
            selectedCoins = getSelectedCoins(Data.COLLECTED);
            collection    = Data.COLLECTED;
            // Impose 25 coin limit
            if (selectedCoins.size() > 25) {
                displayToast("You cannot transfer more than 25 collected coins per day!");
                return;
            }
        } else {
            selectedCoins = getSelectedCoins(Data.RECEIVED);
            collection    = Data.RECEIVED;
        }

        if (selectedCoins.size() == 0) {
            displayToast("Please select coins to send");
            return;
        }

        // Transfer selected coins to bank account
        mBankTransferInProgress = true;
        Log.d(TAG, "[transferToBankAccount] transfer in progress...");

        for (Coin c : selectedCoins) {
            double value    = c.getValue();
            String currency = c.getCurrency();
            double exchange = value * mExchangeRates.get(currency);
            mGoldAmount += exchange;
            Data.removeCoinFromCollection(c, collection, new OnEventListener<Integer>() {

                @Override
                public void onSuccess(Integer numberProcessed) {
                    // If all coins have been processed
                    if (numberProcessed == selectedCoins.size()) {

                        // Get current date
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                                "yyyy/MM/dd", Locale.ENGLISH);
                        String date = format.format(now);

                        // Add transaction to firebase
                        Transaction transaction = new Transaction(mGoldAmount, date);
                        Data.addTransaction(transaction);

                        // Clear transfer parameters
                        Data.clearBankTransferCount();
                        mBankTransferInProgress = false;

                        // Update the view
                        if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
                            updateCollectedView();
                        } else {
                            updateReceivedView();
                        }
                        Log.d(TAG, "[sendCoinsToFriend] transfer complete");
                        displayToast("Transfer complete");
                    } else {
                        mBankTransferTotal++;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    mBankTransferTotal++;
                    // If all coins have been processed
                    if (mBankTransferTotal == selectedCoins.size()) {

                        // Clear transfer parameters
                        Data.clearBankTransferCount();
                        mBankTransferInProgress = false;
                        Log.d(TAG, "[transferToBankAccount] transfer failed");

                        // Update the view
                        if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
                            updateCollectedView();
                        } else {
                            updateReceivedView();
                        }

                    } else {
                        displayToast("Failed to transfer " + c.getCurrency()
                                + " worth " + c.getValue());
                    }
                    Log.d(TAG, "[sendCoins] failed to transfer coin: " + c.getId());
                }
            });
        }
    }

    /*
     *  @brief  { Opens a dialogue with the user to select the friend they wish to send
     *            their selected coins to. The user is informed if the process was successful
     *            or not and view will be updated if successful }
     */
    private void friendTransfer() {
        // Clear the previously selected friend
        mSelectedFriend = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getLayoutInflater().getContext());
        builder.setTitle("Select friend");

        // Retrieve friends
        List<String> options = new ArrayList<>();
        ArrayList<Friend> friends = Data.getFriends();
        for (Friend f : friends) {
            options.add(f.getUsername());
        }

        // Create array adapter of friends username's
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(
                getLayoutInflater().getContext(),
                android.R.layout.simple_list_item_single_choice,
                options);

        // Set selected friend
        builder.setSingleChoiceItems(arrayAdapter, -1,
                (dialog, which) -> mSelectedFriend = friends.get(which)
        );

        // Invoke method to send the selected coins to the selected friend
        builder.setPositiveButton("OK",
                ((dialog, which) -> {
                    if (mSelectedFriend != null) {
                        sendCoinsToFriend();
                    } else {
                        // No user has been selected
                        displayToast("Please select a friend");
                    }
                }));

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /*
     *  @brief  { Identifies all the selected coins and invokes a method in the Data class to
     *            send each coin the the specified friend. The Data method returns the total
     *            number of coins that have been successfully transferred so far, this number is
     *            used to identify when the transfer has completed. The mFriendsTransferTotal
     *            variable is used to count both successful and unsuccessful transfers - again to
     *            identify when all coins have had an attempted transfer. These parameters are
     *            cleared once the process has completed }
     */
    private void sendCoinsToFriend() {
        ArrayList<Coin> selectedCoins;
        String collection;
        mFriendTransferTotal = 0;

        if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
            selectedCoins = getSelectedCoins(Data.COLLECTED);
            collection    = Data.COLLECTED;
        } else {
            selectedCoins = getSelectedCoins(Data.RECEIVED);
            collection    = Data.RECEIVED;
        }

        if (selectedCoins.size() == 0) {
            displayToast("Please select coins to send");
            return;
        }

        // Send selected coins to friend
        mFriendTransferInProgress = true;
        Log.d(TAG, "[sendCoinsToFriend] transfer in progress...");

        for (Coin c : selectedCoins) {
            Data.sendCoinToFriend(mSelectedFriend, c, collection,
                    new OnEventListener<Integer>() {
                        @Override
                        public void onSuccess(Integer numberTransferred) {
                            // If all coins have successfully been transferred, inform user and
                            // reset transfer parameters
                            Log.d(TAG, "[sendCoins] successfully sent coin: " + c.getId());

                            if (numberTransferred == selectedCoins.size()) {
                                displayToast("Successfully sent coins to "
                                        + mSelectedFriend.getUsername());
                                Data.clearFriendTransferCount();
                                mFriendTransferInProgress = false;

                                // Update the view
                                if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
                                    updateCollectedView();
                                } else {
                                    updateReceivedView();
                                }
                                Log.d(TAG, "[sendCoinsToFriend] transfer complete");

                            // Otherwise, increment the total count so far
                            } else {
                                mFriendTransferTotal++;
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mFriendTransferTotal++;

                            // If all coins have been attempted, then clear the transfer parameters
                            if (mFriendTransferTotal == selectedCoins.size()) {
                                Data.clearFriendTransferCount();
                                mFriendTransferInProgress = false;
                                Log.d(TAG, "[sendCoinsToFriend] transfer failed");

                                // Update the view
                                if (mRecyclerViewCol.getVisibility() == View.VISIBLE) {
                                    updateCollectedView();
                                } else {
                                    updateReceivedView();
                                }

                            } else {
                                displayToast("Failed to send " + c.getCurrency()
                                        + " worth " + c.getValue());
                            }
                            Log.d(TAG, "[sendCoins] failed to send coin: " + c.getId());
                        }
                    });
        }
    }

    /*
     *  @return  { Selected coins from given collection }
     */
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

    /*
     *  @brief  { Display message on device }
     *
     *  @params { Message to be displayed }
     */
    private void displayToast(String message) {
        Toast.makeText(getLayoutInflater().getContext(), message, Toast.LENGTH_SHORT).show();
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