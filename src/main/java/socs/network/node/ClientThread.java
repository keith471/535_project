package socs.network.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import socs.network.exceptions.UnexpectedMessageException;
import socs.network.message.MessageType;
import socs.network.message.SOSPFPacket;

public class ClientThread extends Thread {

	private Router router;
	private Protocol protocol;
	private RouterDescription source;
	private RouterDescription dest;

	// optional fields
	private int linkPort;

	public ClientThread(Router router, Protocol protocol, RouterDescription source, RouterDescription dest) {
		this.router = router;
		this.protocol = protocol;
		this.source = source;
		this.dest = dest;
	}

	@Override
	public void run() {

		switch (this.protocol) {
		case HANDSHAKE:
			handshake();
			break;
		case ADDLINK:
			sendAddLink();
			break;
		default:
			// error
		}

	}

	/**
	 * Contains all the logic for performing a handshake from the client's
	 * perspective
	 */
	public void handshake() {
		try (Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {

			SOSPFPacket inPacket, outPacket;

			outPacket = new SOSPFPacket(MessageType.HELLO, source.getProcessIPAddress(), source.getProcessPortNumber(),
					source.getSimulatedIPAddress());

			// send hello to server
			os.writeObject(outPacket);

			// wait for server to send us hello back
			inPacket = (SOSPFPacket) is.readObject();

			if (inPacket.getMessageType() != MessageType.HELLO) {
				// this should never happen
				throw new UnexpectedMessageException();
			}

			System.out.println("received hello from " + inPacket.getSrcIP() + ";\n");

			// set remote router to TWO_WAY
			this.dest.setStatus(RouterStatus.TWO_WAY);
			System.out.println("set " + inPacket.getSrcIP() + " state to INIT;\n");

			// send HELLO back to server
			os.writeObject(outPacket);

			// assume it worked!!!
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Could't read packet as an SOSPFPacket. Should never get this error...");
		}
	}

	public void sendAddLink() {
		try (
				Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {

			SOSPFPacket inPacket, outPacket;
			
			outPacket = new SOSPFPacket(MessageType.ADDLINK, source.getProcessIPAddress(),
					source.getProcessPortNumber(), source.getSimulatedIPAddress());

			// send ADDLINK request to destination router
			os.writeObject(outPacket);

			// await a response (SUCCESS or ERROR)
			inPacket = (SOSPFPacket) is.readObject();

			if (inPacket.getMessageType() == MessageType.ERROR) {
				// something went wrong
				System.err.println("Error. Server said: " + inPacket.getErrorMsg());
				// tell this router that it needs to remove the link it
				// optimistically added
				this.router.removeLinkAtPort(this.linkPort);
			} else {
				System.out.println("Router at " + dest.getSimulatedIPAddress() + " successfully added link");
			}

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Could't read packet as an SOSPFPacket. Should never get this error...");
		}
	}

	public void sendHello() {
		try (
				// create a socket connection to the server (pass the server
				// hostname and port)
				Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {
			// get the output stream of the socket so we can write to the
			// server
			// PrintWriter out = new PrintWriter(socket.getOutputStream(),
			// true);
			// get the input stream of the socket so we can read to the
			// server
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(socket.getInputStream()));) {

			SOSPFPacket inPacket, outPacket;

			// router 1 send HELLO to remote router
			// remote router (R2) receives HELLO: adds a Link to R1, sets the
			// status in its Link's
			// RouterDescription of R1 (??) to INIT, then sends HELLO back to R1
			// R1 receives HELLO from R2 and sets the status in its Link's
			// RouterDescription of R1 (??) as TWO_WAY, and sends HELLO back to
			// R2
			// R2 receives HELLO from R1 and sets the status in its Link's
			// RouterDescription of R1 (??) as TWO_WAY

			// create a hello packet
			// outPacket = "HELLO";
			// send packet on socket
			// out.println(outPacket);
			// wait to hear back from remote router
			// inPacket = in.readLine();
			// TODO: make sure they replied with HELLO
			// System.out.println("received " + inPacket + " from some router
			// server");

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		}
	}

	public void sendLsaUpdate() {
		try (
				// create a socket connection to the server (pass the server
				// hostname and port)
				Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {
			// get the output stream of the socket so we can write to the
			// server
			// PrintWriter out = new PrintWriter(socket.getOutputStream(),
			// true);
			// get the input stream of the socket so we can read to the
			// server
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(socket.getInputStream()));) {

			// handle the LSAUpdate -- follow the protocol

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		}
	}

	public RouterDescription getSource() {
		return source;
	}

	public void setSource(RouterDescription source) {
		this.source = source;
	}

	public RouterDescription getDest() {
		return dest;
	}

	public void setDest(RouterDescription dest) {
		this.dest = dest;
	}

	public int getLinkPort() {
		return linkPort;
	}

	public void setLinkPort(int linkPort) {
		this.linkPort = linkPort;
	}

}
