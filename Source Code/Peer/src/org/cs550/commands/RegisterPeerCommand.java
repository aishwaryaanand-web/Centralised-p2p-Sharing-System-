package org.cs550.commands;

import java.util.List;

public class RegisterPeerCommand extends Commands {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8161585864718564981L;
	private String peerName;
	private int peerServerPort;
	private List<String> fileList;

	public RegisterPeerCommand(String string,int peerServerPort, List<String> fileList) {
		this.peerName = string;
		this.fileList = fileList;
		this.peerServerPort=peerServerPort;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public String peerName() {
		return this.peerName;
	}
	
	public int getPeerServerPort() {
		return this.peerServerPort;
	}
}
