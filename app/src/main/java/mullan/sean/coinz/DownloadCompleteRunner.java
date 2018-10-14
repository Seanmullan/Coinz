package mullan.sean.coinz;

public class DownloadCompleteRunner {

    static String result;
    static boolean complete = false;

    public static void downloadComplete(String result) {
        DownloadCompleteRunner.result = result;
        complete = true;
    }

    public static boolean isComplete() {
        return complete;
    }
}
