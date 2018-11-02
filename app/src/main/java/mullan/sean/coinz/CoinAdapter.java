package mullan.sean.coinz;

import android.annotation.SuppressLint;
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

import javax.annotation.Nonnull;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.MyViewHolder> {

    private static final String TAG = "C_COIN_ADAPTER";

    private ArrayList<Coin> mCoins;
    private Context mContext;

    /*
     *  @brief  { Provides a reference to the views for each data item }
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView  mCurrency, mValue;
        public ImageView mCoinImage;
        public MyViewHolder(View view) {
            super(view);
            mCurrency = view.findViewById(R.id.currency);
            mValue    = view.findViewById(R.id.value);
            mCoinImage = view.findViewById(R.id.coin_image);
        }
    }

    /*
     *  @brief  { Adapter constructor }
     */
    public CoinAdapter(ArrayList<Coin> coins, Context context) {
        this.mCoins = coins;
        this.mContext = context;
    }

    /*
     *  @brief  { Create new views (invoked by the layout manager) }
     */
    @Override
    @Nonnull
    public CoinAdapter.MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coin_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    /*
     *  @brief  { Replace the contents of a view (invoked by the layout manager) }
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Coin coin = mCoins.get(position);
        holder.mCurrency.setText(coin.getCurrency());
        holder.mValue.setText(String.format("%.6f", coin.getValue()));
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

    /*
     *  @return  { Size of your coin ArrayList (invoked by the layout manager) }
     */
    @Override
    public int getItemCount() {
        return mCoins.size();
    }
}