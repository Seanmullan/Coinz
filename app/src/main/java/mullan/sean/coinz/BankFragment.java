package mullan.sean.coinz;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class BankFragment extends Fragment {

    private HashMap<String,Double> mExchangeRates;
    private double                 mGoldAmount;
    private ArrayList<Transaction> mTransactions;
    private RecyclerView mRecyclerViewTrans;
    private TransactionAdapter mTransAdapter;

    public BankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bank, container, false);

        mExchangeRates = Data.getRates();
        mGoldAmount    = Data.getGoldAmount();
        mTransactions  = Data.getTransactions();

        TextView goldAmount = view.findViewById(R.id.gold_amount);
        goldAmount.setText(String.format(Locale.getDefault(), "%.3f", mGoldAmount));

        TextView dolrRate = view.findViewById(R.id.dolr_rate);
        dolrRate.setText(String.format(Locale.getDefault(), "1  :  %.6f", mExchangeRates.get("DOLR")));
        TextView penyRate = view.findViewById(R.id.peny_rate);
        penyRate.setText(String.format(Locale.getDefault(), "1  :  %.6f", mExchangeRates.get("PENY")));
        TextView quidRate = view.findViewById(R.id.quid_rate);
        quidRate.setText(String.format(Locale.getDefault(), "1  :  %.6f", mExchangeRates.get("QUID")));
        TextView shilRate = view.findViewById(R.id.shil_rate);
        shilRate.setText(String.format(Locale.getDefault(), "1  :  %.6f", mExchangeRates.get("SHIL")));

        mRecyclerViewTrans = view.findViewById(R.id.trans_recycler_view);

        // Set layout manager
        RecyclerView.LayoutManager mLayoutTrans = new LinearLayoutManager(inflater.getContext());
        mRecyclerViewTrans.setLayoutManager(mLayoutTrans);

        // Initialise adapter with transactions list
        mTransAdapter = new TransactionAdapter(mTransactions);

        // Add line a line between each object in the recycler view list
        mRecyclerViewTrans.addItemDecoration(
                new DividerItemDecoration(inflater.getContext(), LinearLayoutManager.VERTICAL));

        // Set adapters
        mRecyclerViewTrans.setAdapter(mTransAdapter);
        return view;

    }
}
