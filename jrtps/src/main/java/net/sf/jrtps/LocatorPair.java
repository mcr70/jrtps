package net.sf.jrtps;

import net.sf.jrtps.types.Locator;

/**
 * Package access. Used as a struct to carry both unicastLocator and multicastLocator
 * @author mcr70
 */
class LocatorPair {
	/**
	 * Unicast locator, may be null;
	 */
	Locator ucLocator; 
	/**
	 * Multicast locator, may be null;
	 */
	Locator mcLocator;

	LocatorPair() {	
	}
	
	LocatorPair(Locator uc, Locator mc) {
		ucLocator = uc;
		mcLocator = mc;	
	}	
}
