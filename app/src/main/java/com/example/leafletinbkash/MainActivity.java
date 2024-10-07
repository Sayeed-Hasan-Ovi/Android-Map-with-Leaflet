package com.example.leafletinbkash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.leafletinbkash.databinding.ActivityMainBinding;
import com.example.leafletinbkash.model.Feature;
import com.example.leafletinbkash.model.MapErrorResponse;
import com.example.leafletinbkash.CustomWebChromeClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private ActivityMainBinding binding;

    private FusedLocationProviderClient fusedLocationClient;

    private boolean dataInResponse;

    Data data;

    MapErrorResponse error;

    String jsonString;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*WebView webView = binding.webView;
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);*/

        webView = binding.webView;
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient()); // Set the custom WebChromeClient
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

//        binding.loadButton.setOnClickListener(v ->
                binding.webView.loadUrl("file:///android_asset/sample.html");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        jsonString = getString(R.string.jsonResponseDoc1Res3);
        try {
            // Parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            if (jsonNode.has("error")) {
                error = objectMapper.treeToValue(jsonNode, MapErrorResponse.class);
                System.out.println(error.getError());
                dataInResponse = false;

                // Display appropriate error message using Toast
                String errorMessage = error.getError();
                /*if (errorMessage.equals("Missing required params")) {
                    showToast(getString(R.string.error_missing_required_params));
                } else if (errorMessage.equals("clientId is not matched")) {
                    showToast(getString(R.string.error_clientid_is_not_matched));
                } else if (errorMessage.equals("Could not fetch the data")) {
                    showToast(getString(R.string.error_could_not_fetch_the_data));
                } else {
                    showToast("Unknown error occurred");
                }*/
                showToast(errorMessage);
            } else if (jsonNode.has("data")) {
                data = objectMapper.treeToValue(jsonNode, Data.class);
                dataInResponse = true;
                // Access the data
                System.out.println("Type: " + data.getData().getType());
                System.out.println("Features: " + data.getData().getFeatures());

                // Example: Accessing specific feature details
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Toast.makeText(MainActivity.this, "Current Location: " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();

                            // Pass the location and JSON data to the WebView
                            if (dataInResponse) {
                                for (int i = 0; i < data.getData().getFeatures().size(); i++) {
                                    Feature feature = data.getData().getFeatures().get(i);
                                    System.out.println("Feature " + (i + 1) + ":");
                                    System.out.println("  Type: " + feature.getType());
                                    System.out.println("  Geometry Type: " + feature.getGeometry().getType());
                                    System.out.println("  Coordinates: " + java.util.Arrays.toString(feature.getGeometry().getCoordinates()));
                                    System.out.println("  Properties:");
                                    System.out.println("    ID: " + feature.getProperties().getId());
                                    System.out.println("    Layer: " + feature.getProperties().getLayer());
                                    System.out.println("    Name: " + feature.getProperties().getName());
                                    System.out.println("    Wallet Number: " + feature.getProperties().getWallet_number());
                                    System.out.println("    Address: " + feature.getProperties().getAddress());
                                    System.out.println("    URL: " + feature.getProperties().getUrl());
                                    System.out.println(" Lat : " + feature.getGeometry().getCoordinates()[1]);
                                    System.out.println(" Lon : " + feature.getGeometry().getCoordinates()[0]);

                                    Gson gson = new Gson();
                                    String featureJson = gson.toJson(feature);
                                    String jsCommand = "javascript:setLocationAndData(" + featureJson + ")";
                                    System.out.println("Executing JS Command: " + jsCommand);
                                    webView.evaluateJavascript(jsCommand, null);                               }
                            }
                        }
                    });
        }
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}

