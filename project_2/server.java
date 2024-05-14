import java.io.*; 
import java.net.*; 
import java.util.ArrayList;
import java.util.*; 

class Server { 
	public static void main(String[] args) 
	{ 
		ServerSocket server = null; 

		try { 

			server = new ServerSocket(11111); 
			server.setReuseAddress(true); 

			// running infinite loop for getting 
			// client request 
			while (true) { 

				// socket object to receive incoming client requests 
				Socket client = server.accept(); 

				// Displaying that new client is connected to server 
				System.out.println("New client connected"
								+ client.getInetAddress() 
										.getHostAddress()); 

				// create a new thread object 
				ClientHandler clientSock = new ClientHandler(client); 

				// This thread will handle the client separately 
				new Thread(clientSock).start(); 
			} 
		} 
		catch (IOException e) { 
			e.printStackTrace(); 
		}
		finally { 
			if (server != null) { 
				try { 
					server.close(); 
				} 
				catch (IOException e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
	} 

	// ClientHandler class 
	private static class ClientHandler implements Runnable { 
		private final Socket clientSocket; 

		// Constructor 
		public ClientHandler(Socket socket) 
		{ 
			this.clientSocket = socket; 
		} 

		public void run()
		{ 
			PrintWriter out = null; 
			BufferedReader in = null; 
			try { 
					
				// get the outputstream and inputstream of client 
				out = new PrintWriter(clientSocket.getOutputStream(), true); 
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 

				String serverName = in.readLine();
				int timerValue = Integer.parseInt(in.readLine());

				System.out.printf("Received webname: %s, timer: %d \n", serverName, timerValue); 

				ArrayList<String> lines = new ArrayList<>();				

				// Send GET request to the Web server W
				URL url = new URL("https://" + serverName);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				// Read data from Web server W and send it line by line
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				// add a condition to the while loop

				String line;
				while ((line = reader.readLine()) != null) {
				    lines.add(line);
				}
				reader.close();

				// Send each line one by one
				for (String storedLine : lines) {
				    out.println(storedLine);
				    Thread.sleep(1); 
				    // System.out.println(storedLine);
				}
				out.println("end");

				long startTime = System.currentTimeMillis(); 

				// Wait for an ack, close connection or retransmit
				boolean done = false;
				while ((line = in.readLine()) != null && (System.currentTimeMillis() - startTime) < timerValue) {
					System.out.println(line);
					if ("ack".equals(line)){
						done = true;
						System.out.println("Done");
						break;
					}
				}

				if (!done){
					System.out.println("Timer Fired!");
					System.out.println("Data RESENT");
				}
				
			} 
			catch (IOException e) { 
				e.printStackTrace(); 
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}  
			finally { 
				try { 
					if (out != null) { 
						out.close(); 
					} 
					if (in != null) { 
						in.close(); 
						clientSocket.close(); 
					} 
				} 
				catch (IOException e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
	} 
}
