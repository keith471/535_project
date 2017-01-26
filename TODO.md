# TODOS

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