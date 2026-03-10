package com.game.multicast.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Datagram example: send a UDP packet to a receiver (e.g. localhost:9099).
 * Run with: mvn exec:java -Dexec.mainClass="com.game.multicast.example.DatagramSend" -Dexec.args="[message] [port]"
 * Or: java -cp target/classes com.game.multicast.example.DatagramSend [message] [port]
 */
public class DatagramSend {

    public static void main(String[] args) {
        String message = args.length > 0 ? args[0] : "Hello from DatagramSend";

        // Allocate send buffer from the message
        byte[] buffer = message.getBytes();

        int receiverPort = 9099; // default port
        if (args.length > 1) {
            try {
                receiverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("port must be integer");
                System.exit(2);
            }
        }

        try (DatagramSocket sock = new DatagramSocket()) {
            // Construct the UDP packet using the buffer and fill in the IP headers for localhost
            InetAddress receiverIp = InetAddress.getByName("localhost");
            DatagramPacket pack = new DatagramPacket(buffer, buffer.length, receiverIp, receiverPort);

            // Send packet
            System.out.println("Sending " + message);
            sock.send(pack); // send message and move on
        } catch (Exception e) {
            System.err.println("Send failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
