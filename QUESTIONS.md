- if I attach R1 to R2, do I need to add a Link to the link array for both R1 and R2?
- confirm `start` protocol

- what's the difference between a router's rd and the RouterDescription r1 of its links. These can't always be in sync because of the status field... Don't get why we have both.
- in SOSPFPacket, what is public short srcProcessPort;?
	- we don't know what the source process port is, since it is assigned by the system (unless we are talking about the fake ports corresponding to the indices of the Link array of a router)
	- also, what is routerID in this class? And neighborID, isn't that the same as srcIP? Can we go through the fields?
