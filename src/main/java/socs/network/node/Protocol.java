package socs.network.node;

/**
 * Simply used to identify to client threads what it is that they are to do
 * 
 * @author kstricks
 *
 */
public enum Protocol {
	HANDSHAKE, ADDLINK, LSAUPDATE, LSAUPDATEFORWARD
}
