package mullan.sean.coinz;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.annotation.Nonnull;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.MyViewHolder> {

    private static final String TAG = "C_REQUESTADAPTER";

    private ArrayList<Friend> mRequests;
    private ClickListener     acceptListener;
    private ClickListener     declineListener;

    /*
     *  @brief  { Adapter constructor }
     */
    public RequestAdapter(ArrayList<Friend> requests, ClickListener accept, ClickListener decline) {
        this.mRequests = requests;
        this.acceptListener = accept;
        this.declineListener = decline;
    }

    /*
     *  @brief  { Provides a reference to the views for each data item }
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        private TextView username;
        private TextView email;
        private Button   btnAccept;
        private Button   btnDecline;
        private WeakReference<ClickListener> acceptListenerRef;
        private WeakReference<ClickListener> declineListenerRef;

        public MyViewHolder(View view, ClickListener acceptListener, ClickListener declineListener) {
            super(view);

            username = view.findViewById(R.id.friends_name);
            email = view.findViewById(R.id.friends_email);
            btnAccept = view.findViewById(R.id.accept);
            btnDecline = view.findViewById(R.id.decline);
            acceptListenerRef  = new WeakReference<>(acceptListener);
            declineListenerRef = new WeakReference<>(declineListener);

            btnAccept.setOnClickListener(this);
            btnDecline.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == btnAccept.getId()) {
                acceptListenerRef.get().onPositionClicked(getAdapterPosition());
            } else if (v.getId() == btnDecline.getId()) {
                declineListenerRef.get().onPositionClicked(getAdapterPosition());
            } else {
                Log.d(TAG, "[onClick] view ID not recognised: " + v.getId());
            }
        }
    }

    /*
     *  @brief  { Create new views (invoked by the layout manager) }
     */
    @Override
    @Nonnull
    public RequestAdapter.MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_list_row, parent, false);
        return new RequestAdapter.MyViewHolder(itemView, acceptListener, declineListener);
    }

    /*
     *  @brief  { Replace the contents of a view (invoked by the layout manager) }
     */
    @Override
    public void onBindViewHolder(@Nonnull RequestAdapter.MyViewHolder holder, int position) {
        Friend request = mRequests.get(position);
        holder.username.setText(request.getUsername());
        holder.email.setText(request.getEmail());
    }

    /*
     *  @return  { Size of your friend requests ArrayList (invoked by the layout manager) }
     */
    @Override
    public int getItemCount() {
        return mRequests.size();
    }
}