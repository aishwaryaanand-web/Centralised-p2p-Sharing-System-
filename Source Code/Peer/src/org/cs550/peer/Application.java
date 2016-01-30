package org.cs550.peer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {

	private Properties prop = null;
	private InputStream input = null;
	private static final String CONFIG_DIR = "conf//";
	private static final String CONFIG_FILE = "peer2peer.properties";

	public static void main(String[] args) {
		Application app = new Application();
		app.init();
	}

	private void init() {
		this.prop = new Properties();
		try {
			System.out.println(CONFIG_DIR + CONFIG_FILE);
			this.input = new FileInputStream(CONFIG_DIR + CONFIG_FILE);
			this.prop.load(input);
			Peer2PeerApp.startApp(getCentralIndexServerAddress(), getCentralIndexServerPort(), getLocalServerPort());
		} catch (IOException e) {
			System.out.println("Error While Reading Properties !!! Closing application");
			System.exit(0);
		}
	}

	private String getLocalServerPort() {
		return getStringValue("localserver.port", "9001");
	}

	private String getCentralIndexServerPort() {
		return getStringValue("indexserver.port", "9000");
	}

	private String getCentralIndexServerAddress() {
		return getStringValue("indexserver.address", "localhost");
	}

	private String getStringValue(String key, String defaultValue) {
		String value = null;
		if (this.prop != null) {
			if (this.prop.get(key) != null) {
				value = this.prop.getProperty(key, defaultValue);
			}
		} else {
			System.out.println("Error While Reading Properties !!! Closing application");
			System.exit(0);
		}
		return value;
	}
}
