package com.dam.invernadero;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ServiciosWeb {
    private static ServiciosWeb instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private ServiciosWeb(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized ServiciosWeb getInstance(Context context) {
        if (instance == null) {
            instance = new ServiciosWeb(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // timeout en ms (10 segundos)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        getRequestQueue().add(req);
    }

}