package org.cs550.peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cs550.commands.Commands;
import org.cs550.commands.DownloadPeerFileCommand;
import org.cs550.commands.FileLookUpCommand;
import org.cs550.commands.LookUpResponseCommand;
import org.cs550.commands.MessageCommand;
import org.cs550.commands.PeerFileCommand;
import org.cs550.commands.RegisterPeerCommand;
import org.cs550.peer.exception.Peer2PeerException;
import org.cs550.peer.server.PeerServer;
import org.cs550.peer.server.service.FileUpdateService;
import org.cs550.peer.util.PeerUtils;

public class Peer2PeerApp {

	private static Peer2PeerApp instance = null;
	private String indexServerAddress;
	private String indexServerPort;
	private String peerServerPort;
	private String appDir;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private Peer2PeerApp() {

	}

	private Peer2PeerApp(String serverAddress, String serverPort, String peerServerPort) {
		// Making this private to create static factory method
		try {
			init(serverAddress, serverPort, peerServerPort);
		} catch (Peer2PeerException e) {
			System.out.println(e.getMessage());
		}
	}

	public static Peer2PeerApp startApp(String serverAddress, String serverPort, String peerServerPort) {
		if (instance == null) {
			instance = new Peer2PeerApp(serverAddress, serverPort, peerServerPort);
		}
		return instance;
	}

	private void init(String serverAddress, String serverPort, String peerServerPort) throws Peer2PeerException {
		this.indexServerAddress = serverAddress;
		this.indexServerPort = serverPort;
		this.peerServerPort = peerServerPort;
		Socket serverSocket = connectToServerSocket(indexServerAddress, indexServerPort);
		if (serverSocket != null && serverSocket.isConnected()) {
			System.out.println("Peer Application Started.\nYou are connected to central index server " + indexServerAddress + " port no :" + indexServerPort
					+ "\nYour local server is running on Port no :" + peerServerPort);
			startPeerServer(Integer.parseInt(peerServerPort));
			ServerResponseMessage(serverSocket);
			sendServerCommand(serverSocket, createPeerRegisterCommand());
			ServerResponseMessage(serverSocket);
			executor.scheduleAtFixedRate(new FileUpdateService(this.appDir, PeerUtils.getFileListFromDir(appDir), serverSocket), 1, 2, TimeUnit.MINUTES);
			while (serverSocket.isConnected()) {
				int menuOption = printMenu();
				if (menuOption == 1) {
					sendServerCommand(serverSocket, new FileLookUpCommand(getSerachKeyword()));
					ServerResponseMessage(serverSocket);
				} else if (menuOption == 2) {
					handlefileDonwload();
				} else if (menuOption == 3) {
					System.out.println("Closing Application ");
					System.exit(0);
				} else {
					System.out.println("Invalid Menu Option Please Try Again!");
				}
			}

		} else {
			System.out.println("Unable to Start Server");
		}
	}

	private Socket connectToServerSocket(String serverAddress, String port) throws Peer2PeerException {
		Socket socket = null;
		try {
			socket = new Socket(serverAddress, Integer.parseInt(port));
		} catch (NumberFormatException e) {
			throw new Peer2PeerException("Please Check the port No", e.getCause());
		} catch (UnknownHostException e) {
			throw new Peer2PeerException("Host not found", e.getCause());
		} catch (IOException e) {
			throw new Peer2PeerException("Socket closed ", e.getCause());
		}
		return socket;
	}

	private void startPeerServer(int port) {
		Thread peerServer = new Thread(new PeerServer(port));
		peerServer.start();
	}

	private RegisterPeerCommand createPeerRegisterCommand() {
		return new RegisterPeerCommand(setpPeerName(), Integer.parseInt(peerServerPort), getDirInfo());
	}

	private void sendServerCommand(Socket socket, Commands cmd) throws Peer2PeerException {
		OutputStream os;
		try {
			os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(cmd);
		} catch (IOException e) {
			throw new Peer2PeerException("Error while sending request to server", e.getCause());
		}
	}

	@SuppressWarnings("resource")
	private List<String> getDirInfo() {
		String peerDir = "";
		Scanner scanIn;
		boolean isNotDirValid = true;
		while (isNotDirValid) {
			System.out.println("Please Enter your Directory Locaiton");
			scanIn = new Scanner(System.in);
			peerDir = scanIn.nextLine();
			if (peerDir == "\\") {
				System.out.println("Invalid Directory. Please try again");
			} else {
				File folderexistes = new File(peerDir);
				if (folderexistes.exists() && folderexistes.isDirectory()) {
					isNotDirValid = false;
				} else {
					System.out.println("Invalid Directory. Please try again");
				}
			}
		}
		this.setAppDir(peerDir);
		return PeerUtils.getFileListFromDir(peerDir);
	}

	private String setpPeerName() {
		System.out.println("Please Enter Peer Name:");
		Scanner peerScanIn = new Scanner(System.in);
		return peerScanIn.nextLine();
	}

	private int printMenu() {
		int menu = 0;
		System.out.println("Please Select Menu:");
		System.out.println("1::: Search File IN CI Server");
		System.out.println("2::: Donwload File From Peer");
		System.out.println("3::: Quit");
		try {
			Scanner menuIn = new Scanner(System.in);
			menu = menuIn.nextInt();
		} catch (InputMismatchException e) {

		}
		return menu;
	}

	private String getSerachKeyword() {
		System.out.println("Please Enter File Name to search:");
		Scanner fileScanIn = new Scanner(System.in);
		return fileScanIn.nextLine();
	}

	private void handlefileDonwload() throws Peer2PeerException {
		String peerIpAdd, downloadfile;
		peerIpAdd = getPeerIpAddress();
		int peerHost = getPeerHostAddress();
		downloadfile = getFileDownloadPath();
		Socket peerServerSocket = connectToServerSocket(peerIpAdd, String.valueOf(peerHost));
		sendServerCommand(peerServerSocket, new DownloadPeerFileCommand(downloadfile));
		donwloadFileResp(peerServerSocket);
	}

	private String getPeerIpAddress() {
		System.out.println("Pleaase Enter the Peer Host Ip Address");
		Scanner hostScanner = new Scanner(System.in);
		return hostScanner.nextLine();
	}

	private int getPeerHostAddress() {
		System.out.println("Pleaase Enter the Port of peer");
		Scanner portScanner = new Scanner(System.in);
		return portScanner.nextInt();
	}

	private String getFileDownloadPath() {
		System.out.println("Please Enter File Path :");
		Scanner fpathScanner = new Scanner(System.in);
		return fpathScanner.nextLine();
	}

	private void ServerResponseMessage(Socket clientSocket) throws Peer2PeerException {
		InputStream is;
		ObjectInputStream ois;
		try {
			is = clientSocket.getInputStream();
			ois = new ObjectInputStream(is);
			Object serverResp = (Object) ois.readObject();
			if (serverResp != null) {
				if (serverResp instanceof MessageCommand) {
					System.out.println(":: Server Response ::");
					System.out.println(((MessageCommand) serverResp).getMessage());
				} else if (serverResp instanceof LookUpResponseCommand) {
					if (((LookUpResponseCommand) serverResp).getlookUpReult().size() > 0) {

						StringBuilder sb = new StringBuilder();
						for (Map.Entry<String, List<String>> entry : ((LookUpResponseCommand) serverResp).getlookUpReult().entrySet()) {
							sb.append(entry.getKey()).append("  :  ");
							for (String fileName : entry.getValue()) {
								sb.append(fileName).append(", ");
							}
						}
						System.out.println(sb.toString());
					} else {
						System.out.println("No Result Found");
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new Peer2PeerException("Error while sending request to server", e.getCause());
		}
	}

	private void donwloadFileResp(Socket peerSocket) throws Peer2PeerException {
		try {
			InputStream peeris = peerSocket.getInputStream();
			ObjectInputStream peerois = new ObjectInputStream(peeris);
			Object serverResp = (Object) peerois.readObject();
			while (serverResp == null) {
				peeris = peerSocket.getInputStream();
				peerois = new ObjectInputStream(peeris);
				serverResp = (Object) peerois.readObject();
			}
			if (serverResp instanceof PeerFileCommand) {
				String fileName = ((PeerFileCommand) serverResp).getFilename();
				File dstFile = new File(getAppDir() + "/" + fileName);
				FileOutputStream fileOutputStream = new FileOutputStream(dstFile);
				fileOutputStream.write(((PeerFileCommand) serverResp).getFileData());
				fileOutputStream.flush();
				fileOutputStream.close();
				System.out.println("your donwload file : " + fileName + " is successfully saved at location " + appDir);
			} else {
				System.out.println("No Result Found");
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new Peer2PeerException("Error while sending request to server", e.getCause());
		}
	}

	public String getAppDir() {
		return appDir;
	}

	public void setAppDir(String appDir) {
		this.appDir = appDir;
	}
}
