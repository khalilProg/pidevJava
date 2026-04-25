import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ListModels {
    public static void main(String[] args) throws Exception {
        String apiKey = "AIzaSyArwb7tEiw29Xnrr3cw3kxgYBG-cu9SsFk";
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Status: " + response.code());
            System.out.println(response.body().string());
        }
    }
}
