package com.game.multicast.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Datagram example: create a socket that listens on a port (default 9099) and receive UDP packets.
 * Run with: mvn exec:java -Dexec.mainClass="com.game.multicast.example.DatagramReceive" -Dexec.args="[port]"
 * Or: java -cp target/classes com.game.multicast.example.DatagramReceive [port]
 */
public class DatagramReceive {

    public static void main(String[] args) {
        int portNo = 9099; // default port
        if (args.length > 0) {
            try {
                portNo = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("port must be integer");
                System.exit(2);
            }
        }

        // Receive buffer
        byte[] buffer = new byte[1024];
        // Packet object for receiving; on receipt it will be populated
        DatagramPacket pack = new DatagramPacket(buffer, buffer.length);

        try (DatagramSocket sock = new DatagramSocket(portNo)) {
            System.out.println("Listening on port " + portNo + " (UDP). Send a packet to see it here.");
            // Sits and waits indefinitely for a packet
            sock.receive(pack);
            // On receipt of packet, receive object is populated; output information
            System.out.println("Received " + new String(pack.getData(), pack.getOffset(), pack.getLength()));
        } catch (Exception e) {
            System.err.println("Receive failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
