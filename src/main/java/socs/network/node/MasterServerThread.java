package socs.network.node;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * A router's server. It accepts connection requests from other Routers' clients
 * using a SocketServer and spins off ServerThreads to service the requests.
 * 
 * @author kstricks
 *
 */
public class MasterServerThread extends Thread {

	private Router router; // the router that spawned this thread
	private int portNumber;
	
	public MasterServerThread(Router router, int portNumber) {
		super("Server");
		this.portNumber = portNumber;
		this.router = router;
	}
	
	@Override
	public void run() {
		boolean listening = true;

		try (
				// create a server socket to perpetually listen to the port
				ServerSocket serverSocket = new ServerSocket(portNumber)) {

			// listen forever
			while (listening) {
				// spin off a new thread with the Socket instance created to
				// connect with the client, and start the thread
				new ServerThread(router, serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
	}

}
