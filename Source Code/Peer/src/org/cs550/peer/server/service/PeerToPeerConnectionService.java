package org.cs550.peer.server.service;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.cs550.commands.DownloadPeerFileCommand;
import org.cs550.commands.FileLookUpCommand;
import org.cs550.commands.MessageCommand;
import org.cs550.commands.PeerFileCommand;
import org.cs550.commands.RegisterPeerCommand;

public class PeerToPeerConnectionService {
	private Socket connectedSocket;
	private OutputStream os;
	private ObjectOutputStream oos;
	private InputStream is;
	private volatile boolean isConnected;

	public PeerToPeerConnectionService(Socket connSocket) {
		this.connectedSocket = connSocket;
		System.out.println("p2p");
		downloadFile();

	}

	private void downloadFile() {
		if (this.connectedSocket.isConnected()) {
			try {
				while (this.connectedSocket.isConnected()) {
					this.is = this.connectedSocket.getInputStream();
					ObjectInputStream ois = new ObjectInputStream(is);
					Object obj = (Object) ois.readObject();
					if (obj != null && this.connectedSocket.isConnected()) {
						if (obj instanceof DownloadPeerFileCommand) {
							String filePath = ((DownloadPeerFileCommand) obj).getFileName();
							if (new File(filePath).exists()) {
								this.os = this.connectedSocket.getOutputStream();
								this.oos = new ObjectOutputStream(os);
								this.oos.writeObject(createPeerFile(filePath));
							} else {
								this.os = this.connectedSocket.getOutputStream();
								this.oos = new ObjectOutputStream(os);
								this.oos.writeObject(MessageCommand.P2P_FILE_NOT_FOUND_MSG);
							}

						} else {
							this.os = this.connectedSocket.getOutputStream();
							this.oos = new ObjectOutputStream(os);
							this.oos.writeObject(MessageCommand.UNKNOWN_COMMAND_MSG);
						}
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	public PeerFileCommand createPeerFile(String path) {
		PeerFileCommand peerFileCommand = new PeerFileCommand();
		peerFileCommand.setFilename(path.substring(path.lastIndexOf("/") + 1, path.length()));
		DataInputStream diStream;
		try {
			diStream = new DataInputStream(new FileInputStream(path));

			File file = new File(path);
			long len = (int) file.length();
			byte[] fileBytes = new byte[(int) len];
			int read = 0;
			int numRead = 0;
			while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
				read = read + numRead;
			}
			peerFileCommand.setFileData(fileBytes);
			peerFileCommand.setFileSize(len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return peerFileCommand;
	}

	private String generatePeerName(String ipAddress, int port) {
		System.out.println("IpAddress" + ipAddress + " Port " + port);
		return ipAddress + ":" + port;
	}

	public boolean isAlive() {
		return this.connectedSocket.isConnected();
	}
}
