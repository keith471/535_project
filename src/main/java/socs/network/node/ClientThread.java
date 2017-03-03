package socs.network.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import socs.network.exceptions.UnexpectedMessageException;
import socs.network.message.LSA;
import socs.network.message.MessageType;
import socs.network.message.SOSPFPacket;

public class ClientThread extends Thread {

	private Router router;
	private Protocol protocol;
	private RouterDescription source;
	private RouterDescription dest;

	// optional fields
	private int linkPort;
	private int weight;
	private SOSPFPacket packet;

	public ClientThread(Router router, Protocol protocol, RouterDescription source, RouterDescription dest) {
		this.router = router;
		this.protocol = protocol;
		this.source = source;
		this.dest = dest;
	}

	// overloaded constructor for LSAUPDATEFORWARD
	public ClientThread(SOSPFPacket packet, RouterDescription dest) {
		this.protocol = Protocol.LSAUPDATEFORWARD;
		this.packet = packet;
		this.dest = dest;
	}

	/**
	 * Switch based on the protocol this client was initialized with
	 */
	@Override
	public void run() {

		switch (this.protocol) {
		case HANDSHAKE:
			handshake();
			break;
		case ADDLINK:
			sendAddLink();
			break;
		case LSAUPDATE:
			sendLsaUpdate();
			break;
		case LSAUPDATEFORWARD:
			forwardLsaUpdate();
			break;
		default:
			System.err.println("ERROR: client instantiated with an unexpected protocol. This should never happen.");
			System.exit(1);
		}

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// PROTOCOLS
	/////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Performs a handshake with a remote server
	 */
	public void handshake() {
		try (
				// create a socket connection to the server (pass the server
				// hostname and port)
				// get the output stream of the socket so we can write to the
				// server
				// get the input stream of the socket so we can read from the
				// server
				Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
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
			System.out.println("set " + inPacket.getSrcIP() + " state to TWO_WAY;\n");

			// send HELLO back to server
			os.writeObject(outPacket);

			// assume it worked!!!
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Could't read packet as an SOSPFPacket. Should never get this error...");
			System.exit(1);
		}
	}

	/**
	 * Protocol for requesting that a remote server add a link to us
	 */
	public void sendAddLink() {
		try (Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {

			SOSPFPacket inPacket, outPacket;

			outPacket = new SOSPFPacket(MessageType.ADDLINK, source.getProcessIPAddress(),
					source.getProcessPortNumber(), source.getSimulatedIPAddress(), weight);

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
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Could't read packet as an SOSPFPacket. Should never get this error...");
			System.exit(1);
		}
	}

	/**
	 * Protocol for communicating an LSAUPDATE to a remote server
	 */
	public void sendLsaUpdate() {
		try (Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {

			SOSPFPacket inPacket, outPacket;

			// create the LSA Vector to be passed in the packet using the
			// HashMap in LinkStateDatabase of the router
			Vector<LSA> lsaArray = new Vector<LSA>();
			for (LSA lsa : this.router.getLsd().get_Store().values()) {
				lsaArray.addElement(lsa);
			}

			// create the packet and send LSAUPDATE request to destination
			// router
			outPacket = new SOSPFPacket(MessageType.LSAUPDATE, source.getProcessIPAddress(),
					source.getProcessPortNumber(), source.getSimulatedIPAddress(), lsaArray);
			os.writeObject(outPacket);

			// await a response (SUCCESS or ERROR)
			inPacket = (SOSPFPacket) is.readObject();

			if (inPacket.getMessageType() == MessageType.SUCCESS) {
				System.out.println("Successfully sent LSAUPDATE to router " + dest.getSimulatedIPAddress());
			} else {
				System.err.println("Error sending LSAUPDATE to router " + dest.getSimulatedIPAddress());
			}
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Could't read packet as an SOSPFPacket. Should never get this error...");
			System.exit(1);
		}
	}

	public void forwardLsaUpdate() {
		try (Socket socket = new Socket(dest.getProcessIPAddress(), dest.getProcessPortNumber());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {

			SOSPFPacket inPacket;

			// pass on the packet
			os.writeObject(this.packet);

			// await a response (SUCCESS or ERROR)
			inPacket = (SOSPFPacket) is.readObject();

			if (inPacket.getMessageType() == MessageType.SUCCESS) {
				System.out.println("Successfully forwarded LSAUPDATE to router " + dest.getSimulatedIPAddress());
			} else {
				System.err.println("Error forwarding LSAUPDATE to router " + dest.getSimulatedIPAddress());
			}
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + dest.getProcessIPAddress());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Could't read packet as an SOSPFPacket. Should never get this error...");
			System.exit(1);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// GETTERS AND SETTERS
	/////////////////////////////////////////////////////////////////////////////////////////////////

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

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
