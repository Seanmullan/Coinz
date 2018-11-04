package mullan.sean.coinz;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileTask extends AsyncTask<String, Void, String> {

    private OnEventListener<String> mCallBack;
    public Exception mException;


    /*
     *  @brief  { Constructor that initialises the callback listener }
     */
    public DownloadFileTask(OnEventListener<String> callback) {
        mCallBack = callback;
    }

    /*
     *  @brief  { Invokes file loader to retrieve data from URL and returns
     *            the data }
     */
    @Override
    protected String doInBackground(String... urls) {
        try {
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e) {
            return "Unable to load content. Check your network connection";
        }
    }

    /*
     *  @brief  { Invokes the data download for the URL and the parser for the
     *            data, then returns data once parsed }
     */
    private String loadFileFromNetwork(String urlString) throws IOException {
        return readStream(downloadUrl(new URL(urlString)));
    }

    /*
     *  @brief  { Sends HTTP GET request to fetch data from URL }
     */
    private InputStream downloadUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000); // milliseconds
        conn.setConnectTimeout(15000); // milliseconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    /*
     *  @brief  { Parses data from input stream and returns the data String }
     */
    @NonNull
    private String readStream(InputStream stream) throws IOException {
        StringBuilder str = new StringBuilder();
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream))) {
            while ((line = bufferedReader.readLine()) != null) {
                str.append(line);
            }
        }
        return str.toString();
    }

    /*
     *  @brief  { Called by superclass once execution is completed, then invoke callback
     *            function with result }
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mCallBack != null) {
            if (mException == null) {
                mCallBack.onSuccess(result);
            } else {
                mCallBack.onFailure(mException);
            }
        }
    }
}
