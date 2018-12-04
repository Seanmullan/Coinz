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
 *   Adapter class to store Transaction objects
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private ArrayList<Transaction> mTransactions;

    /**
     *   Provides a reference to the views for each data item (gold amount and date of transaction)
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mDate;
        public TextView mGoldAdded;
        public MyViewHolder(View view) {
            super(view);
            mDate      = view.findViewById(R.id.date);
            mGoldAdded = view.findViewById(R.id.gold_added);
        }
    }

    /**
     *   @param transactions ArrayList of Transaction objects
     */
    public TransactionAdapter(ArrayList<Transaction> transactions) {
        this.mTransactions = transactions;
    }

    /**
     *  Inflate layout of list of transactions (invoked by layout manager)
     */
    @Override
    @Nonnull
    public TransactionAdapter.MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent,
                                                              int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trans_list_row, parent, false);
        return new TransactionAdapter.MyViewHolder(itemView);
    }

    /**
     *   Bind the transaction data to the specified row in transactions list
     *   (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(@Nonnull TransactionAdapter.MyViewHolder holder, int position) {
        Transaction transaction = mTransactions.get(position);
        holder.mDate.setText(transaction.getDate());
        holder.mGoldAdded.setText(String.format(
                Locale.getDefault(), "%.6f",transaction.getGoldAdded()));
    }

    /**
     *  @return  Size of transactions ArrayList (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return mTransactions.size();
    }
}
