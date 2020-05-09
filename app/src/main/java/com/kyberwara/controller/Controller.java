package com.kyberwara.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import com.erz.joysticklibrary.JoyStick;

public class Controller extends AppCompatActivity {
    private View[] buttons;
    private JoyStick[] joySticks;
    private ConnectionManager connectionManager;
    private Timer timer = new Timer();
    private ByteArrayOutputStream packetDataStream = new ByteArrayOutputStream(18);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        String port = intent.getStringExtra("port");

        buttons = new View[] {
                findViewById(R.id.leftTrigger),
                findViewById(R.id.rightTrigger),
                findViewById(R.id.leftBumper),
                findViewById(R.id.rightBumper),

                findViewById(R.id.back),
                findViewById(R.id.start),

                findViewById(R.id.up),
                findViewById(R.id.left),
                findViewById(R.id.right),
                findViewById(R.id.down),

                findViewById(R.id.y),
                findViewById(R.id.b),
                findViewById(R.id.x),
                findViewById(R.id.a),
        };

        joySticks = new JoyStick[] {
                findViewById(R.id.joyStickLeft),
                findViewById(R.id.joyStickRight)
        };

        try {
            connectionManager = new ConnectionManager(ip, port);

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    getControllerState();
                }
            }, 0, 60);
        } catch (SocketException e) {
            Log.e("Controller", "Failed to create socket", e);
            Toast.makeText(getApplicationContext(), "Failed to create socket", Toast.LENGTH_SHORT).show();
            finish();
        } catch (UnknownHostException e) {
            Log.e("Controller", "Failed to resolve " + ip, e);
            Toast.makeText(getApplicationContext(), "Failed to resolve IP address", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getControllerState() {
        for (View button : buttons) {
            packetDataStream.write(
                button.isPressed() ? (byte) 1 : (byte) 0
            );
        }

        for (JoyStick joyStick : joySticks) {
            double angle = joyStick.getAngle();

            double power = joyStick.getPower() / 100;
            if (power > 1) power = 1;

            packetDataStream.write((byte) (Math.cos(angle) * -127 * power));
            packetDataStream.write((byte) (Math.sin(angle) * 127 * power));
        }

        connectionManager.sendPacket(packetDataStream.toByteArray());
        packetDataStream.reset();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setMessage("Exit?")
            .setNegativeButton("No", (dialog, id) -> dialog.cancel())
            .setPositiveButton("Yes", (dialog, id) -> {
                try {
                    packetDataStream.close();
                } catch (IOException e) {
                    Log.e("Controller", "Failed to close stream", e);
                } finally {
                    timer.cancel();
                    connectionManager.disconnect();
                    finish();
                }
            })
            .show();
    }
}
