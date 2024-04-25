import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(11111);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String clientRequest = new String(receivePacket.getData(), 0, receivePacket.getLength());
                String[] requestParams = clientRequest.split(",");
                String webServerName = requestParams[0];
                if (webServerName.equals("ACK")) {
                    System.out.println(webServerName);
                    continue;
                }
                System.out.println("web server name:"+ webServerName);
                int timerValue = Integer.parseInt(requestParams[1]);

                // Start a separate handler thread for client communication
                Thread handlerThread = new Thread(new ClientHandler(serverSocket, receivePacket.getAddress(), receivePacket.getPort(), webServerName, timerValue));
                handlerThread.start();
            }
        } catch (IOException e) {
            System.out.println("main thread server handler");
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private DatagramSocket serverSocket;
    private InetAddress clientAddress;
    private int clientPort;
    private String webServerName;
    private int timerValue;

    public ClientHandler(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, String webServerName, int timerValue) {
        this.serverSocket = serverSocket;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.webServerName = webServerName;
        this.timerValue = timerValue;
    }

    @Override
    public void run() {
        try {
            // Start a timer set to T seconds
            Thread.sleep(timerValue * 1000);

            // Send GET request to the Web server W
            URL url = new URL("https://" + webServerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read data from Web server W
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }
            reader.close();

            // Send data packets to client C
            String data = responseData.toString();
            int totalPackets = (int) Math.ceil((double) data.length() / 1000);
            for (int i = 0; i < totalPackets; i++) {
                int payloadLength = Math.min(1000, data.length() - i * 1000);
                String payload = data.substring(i * 1000, i * 1000 + payloadLength);
                String packet = i + "," + totalPackets + "," + payloadLength + "," + payload;
                byte[] sendData = packet.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);
            }

            // Receive ACK from client
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String ack = new String(receivePacket.getData(), 0, receivePacket.getLength());

            if (ack.equals("ACK")) {
                System.out.println("DONE");
            } else {
                System.out.println("RESENT");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("main thread client handler");
            e.printStackTrace();
        }
    }
}
