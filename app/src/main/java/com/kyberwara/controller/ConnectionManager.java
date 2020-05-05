package com.kyberwara.controller;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ConnectionManager {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    ConnectionManager(String ip, String port) throws SocketException, UnknownHostException {
        this.port = Integer.parseInt(port);
        socket = new DatagramSocket();
        address = InetAddress.getByName(ip);
    }

    Future sendPacket(byte[] packetData) {
        return executorService.submit(() -> {
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, address, port);

            try {
                socket.send(packet);
            } catch (IOException e) {
                Log.e("PacketSender", "Failed to send packet", e);
            }
        });
    }

    void disconnect() {
        try {
            sendPacket(new byte[] {(byte) -1}).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("PacketSender", "Failed to retrieve result of Future", e);
        } finally {
            executorService.shutdown();
            socket.close();
        }
    }
}
