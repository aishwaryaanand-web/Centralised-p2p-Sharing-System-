package org.cs550.cis.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cs550.cis.api.CentralIndexServerAPI;
import org.cs550.cis.registry.RegistryService;

/**
 * An Implementation class for Central index Server API
 * 
 * @see org.cs550.cis.api.CentralIndexServerAPI
 * 
 * @author Aishwarya Anand <A20331867>
 * @mail aanand12@hawk.iit.edu
 */
public class CentralIndexServerAPIimpl implements CentralIndexServerAPI {

	private RegistryService registryservice = null;

	public CentralIndexServerAPIimpl() {
		this.registryservice = new RegistryService();
	}

	/**
	 * 
	 * @see org.cs550.cis.api.CentralIndexServerAPI#search(java.lang.String)
	 */
	@Override
	public Map<String, List<String>> search(String keyword) {
		Map<String, List<String>> lookupMap = new HashMap<String, List<String>>();
		List<String> lookUpResult = new ArrayList<String>();
		boolean hasFile = false;
		List<org.cs550.cis.registry.Registry> registryList = this.showRegistry();
		for (org.cs550.cis.registry.Registry registry : registryList) {
			if (registry.getFileNames().size() > 0) {
				for (String file : registry.getFileNames()) {
					if (file.contains(keyword)) {
						lookUpResult.add(file);
						hasFile = true;
					}
				}
				if (hasFile) {
					lookupMap.put(registry.getRegisteredIp() + ":" + registry.getPortNo(), lookUpResult);
					hasFile = false;
				}
			}
		}
		return lookupMap;
	}

	/**
	 * @see org.cs550.cis.api.CentralIndexServerAPI#showRegistry()
	 */
	@Override
	public <Registry> List<Registry> showRegistry() {
		return (List<Registry>) this.registryservice.getAllRegistry();
	}

	/**
	 * @see org.cs550.cis.api.CentralIndexServerAPI#registerPeer(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public <Registry> void registerPeer(String peerRegName, Registry peer) {
		this.registryservice.addRegistry(peerRegName, (org.cs550.cis.registry.Registry) peer);

	}

	/**
	 * @see org.cs550.cis.api.CentralIndexServerAPI#removePeer(java.lang.String)
	 */
	@Override
	public void removePeer(String key) {
		this.registryservice.deleteRegistry(key);
	}

	/**
	 * @see org.cs550.cis.api.CentralIndexServerAPI#hasPeer(java.lang.String)
	 */
	@Override
	public boolean hasPeer(String key) {
		boolean hasPeer = false;
		if (this.registryservice.getRegistry(key) != null) {
			hasPeer = true;
		}
		return hasPeer;
	}

	/**
	 * @see org.cs550.cis.api.CentralIndexServerAPI#getPeer(java.lang.String)
	 */
	@Override
	public <Registry> Registry getPeer(String key) {
		return (Registry) this.registryservice.getRegistry(key);
	}

}
