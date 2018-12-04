package mullan.sean.coinz;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 *   Adapter class to store Coin objects
 */
public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.MyViewHolder> {

    private static final String TAG = "C_COIN_ADAPTER";

    private ArrayList<Coin> mCoins;
    private Context mContext;

    /**
     *   Provides a reference to the views for each data item (currency, value and coin image).
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView  mCurrency, mValue;
        ImageView mCoinImage;
        MyViewHolder(View view) {
            super(view);
            mCurrency = view.findViewById(R.id.currency);
            mValue    = view.findViewById(R.id.value);
            mCoinImage = view.findViewById(R.id.coin_image);
        }
    }

    /**
     * @param coins ArrayList of coins
     * @param context Application context
     */
    CoinAdapter(ArrayList<Coin> coins, Context context) {
        this.mCoins = coins;
        this.mContext = context;
    }

    /**
     *  Inflate layout of list of coins (invoked by layout manager)
     */
    @Override
    @Nonnull
    public CoinAdapter.MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coin_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    /**
     *   Bind the coin data to the specified row in the coin list (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Coin coin = mCoins.get(position);
        holder.mCurrency.setText(coin.getCurrency());
        holder.mValue.setText(String.format(Locale.getDefault(), "%.6f", coin.getValue()));
        holder.itemView.setBackgroundColor(coin.isSelected() ? Color.GRAY : Color.WHITE);
        holder.itemView.setOnClickListener(v -> {
            coin.setSelected(!coin.isSelected());
            holder.itemView.setBackgroundColor(coin.isSelected() ? Color.LTGRAY : Color.WHITE);
        });
        Drawable icon;
        switch (coin.getCurrency()) {
            case Data.DOLR:
                icon = ContextCompat.getDrawable(mContext, R.drawable.dolr);
                holder.mCoinImage.setImageDrawable(icon);
                break;
            case Data.PENY:
                icon = ContextCompat.getDrawable(mContext, R.drawable.peny);
                holder.mCoinImage.setImageDrawable(icon);
                break;
            case Data.QUID:
                icon = ContextCompat.getDrawable(mContext, R.drawable.quid);
                holder.mCoinImage.setImageDrawable(icon);
                break;
            case Data.SHIL:
                icon = ContextCompat.getDrawable(mContext, R.drawable.shil);
                holder.mCoinImage.setImageDrawable(icon);
                break;
            default:
                Log.d(TAG, "[onBindViewHolder] invalid currency");
        }
    }

    /**
     *  @return  Size of your coin ArrayList (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return mCoins.size();
    }
}