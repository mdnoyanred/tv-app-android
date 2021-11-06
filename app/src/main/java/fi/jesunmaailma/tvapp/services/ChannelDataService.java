package fi.jesunmaailma.tvapp.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class ChannelDataService {
    Context context;

    public ChannelDataService(Context context) {
        this.context = context;
    }

    public interface OnDataResponse {
        void onResponse(JSONArray response);
        void onError(String error);
    }

    public void getChannelData(String url, OnDataResponse onDataResponse) {

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                onDataResponse.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onDataResponse.onError(error.getMessage());
            }
        });

        queue.add(arrayRequest);
    }

}
