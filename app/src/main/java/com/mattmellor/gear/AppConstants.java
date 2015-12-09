package com.mattmellor.gear;

import com.appspot.backendgear_1121.gear.Gear;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

/**
 * Class to store key constants to connect to the backend
 * (So that they only need to be initialized once)
 */
public class AppConstants {

    /**
     * Class instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

    /**
     * Class instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();


    /**
     * Retrieves a Helloworld api service handle to access the API.
     */
    public static Gear getApiServiceHandle() {
        // Use a builder to help formulate the API request.
        Gear.Builder helloWorld = new Gear.Builder(AppConstants.HTTP_TRANSPORT,
                AppConstants.JSON_FACTORY, null);
        helloWorld.setApplicationName("GEAR");
        return helloWorld.build();
    }

}