# Link State Routing

- Link state routing is a protocol used in packet switching networks.
- It is performed by every __switching__ node in the network (nodes that are prepared to forward packets)
	- such nodes are called routers in the internet
- every node constructs a map of the connectivity to the network, in the form of a graph
- each node then independently calculates the next best logical path from it to every possible destination in the network
- the collection of best paths then forms the node's routing table
- the key to get link state routing to work well is to synchronize the understanding of the network structure across all nodes