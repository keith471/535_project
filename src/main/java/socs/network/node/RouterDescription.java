package socs.network.node;

public class RouterDescription {

	// used for socket communication
	private String processIPAddress; // actual IP address that the router is
										// running on
								// (127.0.0.1)
	private int processPortNumber; // actual port that the "server" is running
									// on
	// used to identify the router in the simulated network space
	private String simulatedIPAddress; // what will be used in the network
										// topology
	// status of the router
	private RouterStatus status;

	public RouterDescription(String processIPAddress, int processPortNumber, String simulatedIPAddress) {
		this.processIPAddress = processIPAddress;
		this.processPortNumber = processPortNumber;
		this.simulatedIPAddress = simulatedIPAddress;
	}

	public String getProcessIPAddress() {
		return processIPAddress;
	}

	public void setProcessIPAddress(String processIPAddress) {
		this.processIPAddress = processIPAddress;
	}

	public int getProcessPortNumber() {
		return processPortNumber;
	}

	public void setProcessPortNumber(int processPortNumber) {
		this.processPortNumber = processPortNumber;
	}

	public String getSimulatedIPAddress() {
		return simulatedIPAddress;
	}

	public void setSimulatedIPAddress(String simulatedIPAddress) {
		this.simulatedIPAddress = simulatedIPAddress;
	}

	public RouterStatus getStatus() {
		return status;
	}

	public void setStatus(RouterStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processIPAddress == null) ? 0 : processIPAddress.hashCode());
		result = prime * result + processPortNumber;
		result = prime * result + ((simulatedIPAddress == null) ? 0 : simulatedIPAddress.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RouterDescription other = (RouterDescription) obj;
		if (processIPAddress == null) {
			if (other.processIPAddress != null) {
				return false;
			}
		} else if (!processIPAddress.equals(other.processIPAddress)) {
			return false;
		}
		if (processPortNumber != other.processPortNumber) {
			return false;
		}
		if (simulatedIPAddress == null) {
			if (other.simulatedIPAddress != null) {
				return false;
			}
		} else if (!simulatedIPAddress.equals(other.simulatedIPAddress)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		return true;
	}

}
