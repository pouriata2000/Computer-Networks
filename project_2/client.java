import java.io.*; 
import java.net.*; 
import java.util.*; 

class Client { 
	
	public static void main(String[] args) 
	{ 
		// establish a connection by providing host and port number
		try (Socket socket = new Socket("localhost", 11111)) { 

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			// Ask user to enter Web server W name and timer value T
			System.out.print("Enter Web server name: ");
			String webServerName = reader.readLine();

			System.out.print("Enter timer value (T) in seconds: ");
			int timerValue = Integer.parseInt(reader.readLine()) *1000;

			System.out.println("======================================================");
			System.out.println("This is to test not sending an ack from the client side to test the server response.");
			System.out.print("Do you want to send an Ack at the end? (y for yes, anything else for no): ");
			String answer = reader.readLine();
			boolean ack = false;
			if (answer.equals("y")) {
				ack = true;
			}

			// writing to server 
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
			out.println(webServerName);
			out.println(timerValue);

			long startTime = System.currentTimeMillis(); // Capture current time

			System.out.println("data sent to server!");
			
			// reading from server 
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 

			System.out.println("Server Response:");

			// Timer loop
			boolean done = false;
			String line;
	        while ((line = in.readLine()) != null && (System.currentTimeMillis() - startTime) < timerValue) {
	        	if ("end".equalsIgnoreCase(line)) {
	        		if (ack) {
	        			out.println("ack");
	        		}
		            done = true;
		            System.out.println("OK");
		            break;
	        	}
	        	// System.out.println(line);	 
	        }

	        if (!done){
	        	System.out.println("Timer Fired! FAIL");
	        }
			
		} 
		catch (IOException e) { 
			e.printStackTrace(); 
		} 
	} 
}
