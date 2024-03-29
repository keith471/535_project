package socs.network.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import socs.network.exceptions.DuplicateLinkException;
import socs.network.exceptions.NoAvailablePortsException;
import socs.network.exceptions.SelfLinkException;
import socs.network.exceptions.UnexpectedMessageException;
import socs.network.message.LinkDescription;
import socs.network.message.MessageType;
import socs.network.message.SOSPFPacket;

public class ServerThread extends Thread {

	private Router router;
	private Socket socket = null;

	// instantiated with a Socket connection to a client
	public ServerThread(Router router, Socket socket) {
		super("ServerThread");
		this.socket = socket;
		this.router = router;
	}

	/**
	 * Switch based on the message type of the packet received, and then follow
	 * the according protocol
	 */
	@Override
	public void run() {

		try (
				// get the output of the socket to talk to the client
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {

			SOSPFPacket inputPacket;
			// in this protocol, the client speaks first, so we just begin listening
			inputPacket = (SOSPFPacket) is.readObject();
			// message received, process it
			switch (inputPacket.getMessageType()) {
			case HELLO:
				handshake(inputPacket, is, os);
				break;
			case LSAUPDATE:
				processLsaUpdate(inputPacket, os);
				break;
			case LSAUPDATESENDBACK:
				processLsaUpdateSendBack(inputPacket, os);
				break;
			case ADDLINK:
				handleAddLink(inputPacket, os);
				break;
			case REMOVELINK:
				handleRemoveLink(inputPacket, os);
				break;
			default:
				System.err.println("ERROR: server received an unexpected SOSPFPacket. This should never happen.");
				System.exit(1);
			}

			// IMPORTANT: as we did not create the socket connection to the
			// client within a try with resources statement, we are responsible
			// for closing the socket ourselves upon finishing communication
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// PROTOCOLS
	/////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * The server received request to connect followed by a HELLO. This means it
	 * has initiated a handshake with us.
	 * 
	 * @param inPacket
	 */
	private void handshake(SOSPFPacket inPacket, ObjectInputStream is, ObjectOutputStream os)
			throws IOException, ClassNotFoundException {
		System.out.println("received hello from " + inPacket.getSrcIP() + ";\n");
		// tell the router to update its link to the client that sent HELLO to
		// reflect that that client has been initialized
		this.router.updateLinkStatusFromSourceIp(inPacket.getSrcIP(), RouterStatus.INIT);
		System.out.println("set " + inPacket.getSrcIP() + " state to INIT;\n");
		
		// send HELLO back to the client
		os.writeObject(new SOSPFPacket(MessageType.HELLO, this.router.getRd().getProcessIPAddress(),
				this.router.getRd().getProcessPortNumber(), this.router.getRd().getSimulatedIPAddress()));
		
		// wait for a second HELLO
		inPacket = (SOSPFPacket) is.readObject();

		if (inPacket.getMessageType() != MessageType.HELLO) {
			// this should never happen
			throw new UnexpectedMessageException();
		}

		System.out.println("received hello from " + inPacket.getSrcIP() + ";\n");

		// tell the router to update the link status again, this time to TWO_WAY
		this.router.updateLinkStatusFromSourceIp(inPacket.getSrcIP(), RouterStatus.TWO_WAY);
		System.out.println("set " + inPacket.getSrcIP() + " state to TWO_WAY;\n");
	}

	/**
	 * The server received a request to connect followed by an ADDLINK. This
	 * means the remote client has requested we add a link to it.
	 * 
	 * @param packet
	 * @param os
	 * @throws IOException
	 */
	private void handleAddLink(SOSPFPacket packet, ObjectOutputStream os) throws IOException {
		try {
			// need to get an available port and add the link all in one go
			RouterDescription rd2 = new RouterDescription(packet.getSrcProcessIP(), packet.getSrcProcessPort(),
					packet.getSrcIP());
			Link l = new Link(router.getRd(), rd2, packet.getWeight());
			int port = this.router.addLink(l);
			// create new LinkDescription and add it to the LinkStateDatabase
			LinkDescription ld = new LinkDescription(rd2.getSimulatedIPAddress(), port, packet.getWeight());
			this.router.addLinkDescriptionToLinkStateDatabase(ld);
			// even though we've changed our LSD, given the nature of
			// Router.forwardLsaUpdate(Link, SOSPFPacket), we know that we don't
			// have to send out LSAUPDATEs ourself since we can and should just
			// wait for the LSAUPDATE from the router that requested we add this
			// link. that's why it suffices to just add the link description to
			// the LSD as above. we need not do anything else.
			
			// send back success message
			SOSPFPacket responsePacket = new SOSPFPacket();
			os.writeObject(responsePacket);
		} catch (NoAvailablePortsException ex) {
			// send ERROR response packet
			SOSPFPacket errPacket = new SOSPFPacket("No ports available!!!");
			os.writeObject(errPacket);
		} catch (DuplicateLinkException ex) {
			// send ERROR response packet
			SOSPFPacket errPacket = new SOSPFPacket("You already have a link to this server!!!");
			os.writeObject(errPacket);
		} catch (SelfLinkException ex) {
			// send ERROR response packet
			SOSPFPacket errPacket = new SOSPFPacket(
					"The client tried forcing the server to add a link to itself... This should never happen!!!");
			os.writeObject(errPacket);
		}
	}

	/**
	 * A remote router connected to us has requested that we remove out link to
	 * it. We need to remove the link from our ports array and also from our
	 * link-state database. This means we also need to trigger an LSAUPDATE.
	 * 
	 * @param packet
	 * @param os
	 * @throws IOException
	 */
	private void handleRemoveLink(SOSPFPacket packet, ObjectOutputStream os) throws IOException {
		// remove the link
		this.router.reactToRemoveLinkRequest(packet.getSrcIP());

		// send back success message
		SOSPFPacket responsePacket = new SOSPFPacket();
		os.writeObject(responsePacket);
	}

	/**
	 * The server received a request to connect followed by an LSAUPDATE. This
	 * means the remote client is propagating an LSAUPDATE to us, so we should
	 * update our Router's LinkStateDatabase
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void processLsaUpdate(SOSPFPacket packet, ObjectOutputStream os) throws IOException {
		// call the method in the router for updating LinkStateDatabase
		this.router.performLsaUpdate(packet);
		
		// send back a success message
		SOSPFPacket responsePacket = new SOSPFPacket();
		os.writeObject(responsePacket);
	}

	private void processLsaUpdateSendBack(SOSPFPacket packet, ObjectOutputStream os) throws IOException {
		this.router.performLsaUpdateSendBack(packet);

		SOSPFPacket responsePacket = new SOSPFPacket();
		os.writeObject(responsePacket);
	}
}
