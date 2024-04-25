import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Ask user to enter Web server W name
            System.out.print("Enter Web server name: ");
            String webServerName = reader.readLine();

            // Ask user to enter timer value T in seconds
            System.out.print("Enter timer value (T) in seconds: ");
            int timerValue = Integer.parseInt(reader.readLine());

            // Create UDP socket
            DatagramSocket clientSocket = new DatagramSocket();

            // Start a timer set to T seconds
            Thread.sleep(timerValue * 1000);

            // Send packet to server with W and T fields
            String request = webServerName + "," + timerValue;
            byte[] sendData = request.getBytes();
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 11111;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            clientSocket.send(sendPacket);

            // Receive data packets from server
            StringBuilder responseData = new StringBuilder();
            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                String packet = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Process packet
                String[] fields = packet.split(",");
                int packetNumber = Integer.parseInt(fields[0]);
                int totalPackets = Integer.parseInt(fields[1]);
                int payloadLength = Integer.parseInt(fields[2]);
                String payload = fields[3];
                responseData.append(payload);

                // Send ACK to server if all packets are received
                if (packetNumber == totalPackets - 1) {
                    String ack = "ACK";
                    byte[] ackData = ack.getBytes();
                    DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, serverAddress, serverPort);
                    clientSocket.send(ackPacket);
                    break;
                }
            }

            // Print the contents in the messages
            System.out.println(responseData.toString());
            System.out.println("OK");

            // Close socket
            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
