/*
 * This is the main activity.
 *
 * It checks if the device is connected to Wi-Fi,
 * validates the IP address and port input field
 * values and passes them to the controller activity.
 */

package com.kyberwara.controller;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        EditText ipInput = findViewById(R.id.ip);
        EditText portInput = findViewById(R.id.port);

        findViewById(R.id.connect).setOnClickListener(view -> {
            String ip = ipInput.getText().toString();
            String port = portInput.getText().toString();

            if (!ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                Toast.makeText(getApplicationContext(), "Invalid IP address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!port.matches("^[0-9]{1,5}$")) {
                Toast.makeText(getApplicationContext(), "Invalid port", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isWifiConnected()) {
                Toast.makeText(getApplicationContext(), "Not connected to Wi-Fi", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getApplicationContext(), Controller.class)
                .putExtra("ip", ip)
                .putExtra("port", port);

            startActivity(intent);
        });
    }

    private boolean isWifiConnected() {
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                }
            }
        }

        return false;
    }
}
