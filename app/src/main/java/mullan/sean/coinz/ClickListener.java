package mullan.sean.coinz;

/**
 *   Interface to be used with RecyclerView Adapters to detect the position of an
 *   item that has been clicked in the list
 */
public interface ClickListener {

    /**
     *  Passes position of item that has been clicked in argument
     */
    void onPositionClicked(int position);
}
