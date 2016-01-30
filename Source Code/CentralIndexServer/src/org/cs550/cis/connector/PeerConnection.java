package org.cs550.cis.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.cs550.cis.api.CentralIndexServerAPI;
import org.cs550.cis.exception.CentralIndexServerException;
import org.cs550.cis.registry.Registry;
import org.cs550.commands.FileLookUpCommand;
import org.cs550.commands.LookUpResponseCommand;
import org.cs550.commands.MessageCommand;
import org.cs550.commands.RegisterPeerCommand;
import org.cs550.commands.UpdatePeerFilesCommand;

public class PeerConnection {
	private Socket connectedSocket;
	private OutputStream os;
	private InputStream is;
	private CentralIndexServerAPI centralIndexServerAPI;
	private String ipAddress;
	private int conenctionPort;
	private int peerServerPort;
	private volatile boolean isConnected;

	public PeerConnection(Socket connSocket, CentralIndexServerAPI centralIndexServerAPI) {
		try {
			init(connSocket, centralIndexServerAPI);
			start();
		} catch (CentralIndexServerException e) {
			System.out.println(e.getMessage());
		}
	}

	private void init(Socket connSocket, CentralIndexServerAPI centralIndexServerAPI) throws CentralIndexServerException {
		this.connectedSocket = connSocket;
		this.isConnected = this.connectedSocket.isConnected();
		this.ipAddress = this.connectedSocket.getInetAddress().getHostAddress();
		this.conenctionPort = this.connectedSocket.getPort();
		this.centralIndexServerAPI = centralIndexServerAPI;
		try {
			this.os = this.connectedSocket.getOutputStream();
			this.is = this.connectedSocket.getInputStream();
		} catch (IOException e) {
			this.isConnected = false;
			CISConnectionManager.getConnectionManager().removeConnection(this.conenctionPort);
			throw new CentralIndexServerException("There seems like problem happened cloosing connection");
		}

	}

	private void start() throws CentralIndexServerException {
		if (isConnected()) {
			writeMessageCommand();
			while (isConnected()) {
				//Long startTime=System.nanoTime();
				Object obj = readObjectFromSocket();
				if (obj != null && isConnected()) {
					if (obj instanceof RegisterPeerCommand) {
						registerPeerConnection(obj);
					} else if (obj instanceof FileLookUpCommand) {
						peerFileLookUp(obj);
					} else if (obj instanceof UpdatePeerFilesCommand) {
						//System.out.println("Inside Update Command");
						updatePeerFiles((UpdatePeerFilesCommand) obj);
					}
				}
			//	System.out.println("Total responseTime "+ peerServerPort+" "+(System.nanoTime()-startTime));
			}
		}
	}

	private void updatePeerFiles(UpdatePeerFilesCommand updatePeerFilesCommand) throws CentralIndexServerException {
		if (this.centralIndexServerAPI.hasPeer(generatePeerName(getConnAddress(), getPeerServerPort()))) {
			Registry peerReg = this.centralIndexServerAPI.getPeer(generatePeerName(getConnAddress(), getPeerServerPort()));
			peerReg.UpdateFileNames(updatePeerFilesCommand.getFileList());
			for (String iterable_element : updatePeerFilesCommand.getFileList()) {
				System.out.println(iterable_element);
			}
		}
	}

	private Object readObjectFromSocket() throws CentralIndexServerException {
		try {
			ObjectInputStream ois = new ObjectInputStream(this.is);
			return (Object) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new CentralIndexServerException("Error While reading message Comand does not exist in Server");
		} catch (IOException e) {
			this.isConnected = false;
			CISConnectionManager.getConnectionManager().removeConnection(conenctionPort);
			if (this.centralIndexServerAPI.hasPeer(generatePeerName(getConnAddress(), getPeerServerPort()))) {
				this.centralIndexServerAPI.removePeer(generatePeerName(getConnAddress(), getPeerServerPort()));
			}
			throw new CentralIndexServerException("Error while reading message from peer closing connection");
		}
	}

	private void writeMessageCommand() throws CentralIndexServerException {

		try {
			ObjectOutputStream oos = new ObjectOutputStream(this.os);
			oos.writeObject(MessageCommand.SERVER_RESP_CONN_ESTABLISHED);
			oos.flush();
		} catch (IOException e) {
			this.isConnected = false;
			CISConnectionManager.getConnectionManager().removeConnection(conenctionPort);
			throw new CentralIndexServerException("Error While writing message");
		}

	}

	private void writeMessageCommand(MessageCommand messageCommand) throws CentralIndexServerException {

		try {
			ObjectOutputStream oos = new ObjectOutputStream(this.os);
			oos.writeObject(messageCommand);
			oos.flush();
		} catch (IOException e) {
			this.isConnected = false;
			CISConnectionManager.getConnectionManager().removeConnection(conenctionPort);
			throw new CentralIndexServerException("Error While writing message");
		}

	}

	private void peerFileLookUp(Object obj) throws CentralIndexServerException {
		Map<String, List<String>> serverLookUpResp = centralIndexServerAPI.search(((FileLookUpCommand) obj).getSearchkeywords());
		try {
			ObjectOutputStream oos = new ObjectOutputStream(this.os);
			oos.writeObject(new LookUpResponseCommand(serverLookUpResp));
		} catch (IOException e) {
			this.centralIndexServerAPI.removePeer(generatePeerName(getConnAddress(), getPeerServerPort()));
			CISConnectionManager.getConnectionManager().removeConnection(conenctionPort);
			this.isConnected = false;
			throw new CentralIndexServerException("Error While File Look up Removing Peer Connection Closed");
		}
	}

	private void registerPeerConnection(Object obj) throws CentralIndexServerException {
		centralIndexServerAPI.registerPeer(generatePeerName(getConnAddress(), getPeerServerPort()), createNewRegistry((RegisterPeerCommand) obj));
		System.out.println("Registry Created ");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(this.os);
			oos.writeObject(MessageCommand.SERVER_RESP_REGISTRY_SUCCESS_MSG);
			oos.flush();
		} catch (IOException e) {
			CISConnectionManager.getConnectionManager().removeConnection(conenctionPort);
			throw new CentralIndexServerException("Error while registring peer closing Connection");
		}
	}

	private boolean isConnected() {
		this.isConnected = this.connectedSocket.isConnected();
		return this.isConnected;
	}

	private int getPeerServerPort() {
		return this.connectedSocket.getPort();
	}

	private String getConnAddress() {
		return this.ipAddress;
	}

	private Registry createNewRegistry(RegisterPeerCommand registerPeerCommand) {
		this.peerServerPort = registerPeerCommand.getPeerServerPort();
		return new Registry(registerPeerCommand.peerName(), registerPeerCommand.getFileList(), getConnAddress(), String.valueOf(registerPeerCommand
				.getPeerServerPort()));
	}

	private String generatePeerName(String ipAddress, int port) {
		System.out.println("IpAddress" + ipAddress + " Port " + port);
		return ipAddress + ":" + port;
	}

	public boolean isAlive() {
		return isConnected();
	}
}
