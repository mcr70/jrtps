package net.sf.jrtps;

import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.Locator;

public class Proxy {
	private final DiscoveredData discoveredData;
	
	private Locator ucLocator;
	private Locator mcLocator;

	protected Proxy(DiscoveredData dd) {
		discoveredData = dd;
	}
	
	/**
	 * Gets the unicast locator of this Proxy.
	 * @return unicast locator
	 */
	public Locator getUnicastLocator() {
		return ucLocator;
	}

	/**
	 * Sets the unicast locator for this Proxy.
	 * @param locator
	 */
	public void setUnicastLocator(Locator locator) {
		this.ucLocator = locator;
	}

	/**
	 * Gets the multicast locator of this Proxy.
	 * @return multicast locator
	 */
	public Locator getMulticastLocator() {
		return mcLocator;
	}

	/**
	 * Sets the multicast locator for this Proxy
	 * @param locator
	 */
	public void setMulticastLocator(Locator locator) {
		this.mcLocator = locator;
	}

	public DiscoveredData getDiscoveredData() {
		return discoveredData;
	}
	
	public Guid getGuid() {
		return discoveredData.getKey();
	}

	public String toString() {
		return getGuid().toString();
	}
}
