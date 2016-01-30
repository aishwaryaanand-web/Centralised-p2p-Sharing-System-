package org.cs550.cis.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry class that keeps all the information related to peer
 * 
 */
public final class Registry {
	/**
	 * List of file names present in that peer
	 */
	private List<String> fileNames = new ArrayList<String>();
	private String peerName;
	private String registeredIp;
	private String portNo;

	/**
	 * Making default Constructor as private
	 */
	private Registry() {

	}

	/**
	 * Constructor for Registry creation
	 * 
	 * @param peerName
	 *            - Peer Name
	 * @param fileNames
	 *            - File Names present in that Peer
	 * @param registeredIp
	 *            - Registered IP Address of Peer
	 * @param portNo
	 *            - Port No of Peer
	 */
	public Registry(String peerName, List<String> fileNames, String registeredIp, String portNo) {
		super();
		this.fileNames = fileNames;
		this.peerName = peerName;
		this.registeredIp = registeredIp;
		this.portNo = portNo;
	}

	/**
	 * Method that provide the functionality to update the files linked to peer
	 * 
	 * @param fileNames
	 *            - new files that needed to be linked
	 */
	public void UpdateFileNames(final List<String> fileNames) {
		this.fileNames = null;
		this.fileNames = fileNames;
	}

	/**
	 * Getter method to get file name list
	 * 
	 * @return list of files in that peer
	 */
	public List<String> getFileNames() {
		return Collections.unmodifiableList(fileNames);
	}

	/**
	 * Getter method for peer
	 * 
	 * @return Peer Name
	 */
	public String getPeerName() {
		return peerName;
	}

	/**
	 * Getter method for RegisteredIp
	 * 
	 * @return Registerd IP address of Peer
	 */
	public String getRegisteredIp() {
		return registeredIp;
	}

	/**
	 * Getter Method for portNo
	 * 
	 * @return portNo
	 */
	public String getPortNo() {
		return portNo;
	}
}
