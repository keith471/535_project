# TODOS

Hey man so adding new nodes to established networks works fine as long as we have a node send out LSAUPDATEs in response to an ADDLINK message. So if we have A and B already established and connected and later add C and attach it to B then everything works fine as long as B sends out LSAUPDATEs in response to C sending it an ADDLINK.

The downside here is that `attach` has the side effect of causing other nodes to send out LSAUPDATEs. So node A will receive an LSAUPDATE from node B that shows that B now can reach C even before we call start on C. But it does work as expected.

Another solution would be to allow node B to send an LSAUPDATE back to node C, even when node C is the one to send the LSAUPDATE to B
We can allow this if the receiving node has LSA entries in its LSdatabase that the sending node does not have. We can check for this scenario by having B, upon receiving an LSA update from C, iterate through the LSAs in the LSA update and check to see if B has LSA entries in its database that were not contained in the update.

Also, on another unrelated note, but I want to say it anyway before I forget, we should probably add a check before sending out any message from a port. The check would be that the link at the port is 2-way. That would indicate that both nodes of the link have run `start`, and thus are "up and running". Otherwise, I think we could ignore them. I doubt this is necessary at all, but is probably more realistic

We need to have multiple "clients"

## Now
- get routers working with sockets
	- rework "client" so that we have a client corresponding to every link
		- perhaps client should be a property of link
			- the client is what the router uses to communicate with the router (Server) at the other end of the link
	- each router need to:
		- be constantly listening to stdin for input from user (one thread)
		- be constantly listening on a port for requests to connect (another thread)
			- when such a request is received, it spins of a new thread per request to handle the request
		
	
- implement attach, start, neighbors


## Later
- have to define communication protocol - nope, already done in SOSPFPacket.java, but could wrap it
- need map from simulated ip address to actual ip address and port number
- link state database synchronization:
	- happens when the link state of a router changes
	- the router where the link state changes broadcasts LSAUPDATE which contains the latest information of the link state to all neighbors
	- the routers which receive the message will in turn broadcast to their own neighbors except the one which sends the LSAUPDATE
	- LSAUPDATE contains one or more LSA objects which summarize the latest link state info to the router
	- router only performs update if the LSAUPDATE's sequence number is larger than the max sequence number of an LSAUPDATE it has received previously (seems we need to keep track of this)