package net.sf.jrtps.udds.security;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.omg.dds.security.xml.governance.DomainAccessRules;
import org.omg.dds.security.xml.governance.DomainAccessRulesNode;
import org.omg.dds.security.xml.governance.DomainRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XmlGovernance is a helper class to read values from Xml file.
 * 
 * @author mcr70
 */
public class XmlGovernance {
	private static final Logger logger = LoggerFactory.getLogger(XmlGovernance.class);
	private DomainAccessRulesNode dar;

	public XmlGovernance(InputStream is) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(org.omg.dds.security.xml.governance.ObjectFactory.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<DomainAccessRulesNode> unmarshal = (JAXBElement<DomainAccessRulesNode>) jaxbUnmarshaller.unmarshal(is);
        dar = unmarshal.getValue();
	}
	
	/**
	 * Gets whether or not unauthenticated join is allowed.
	 * Default value is false.
	 * 
	 * @param domainId domainID to check
	 * @return true, if domain allows unauthenticated join.
	 */
	public boolean allowUnauthenticatedJoin(int domainId) {
		DomainRule rule = getDomainRule(domainId);
		boolean value = false;
		if (rule != null) {
			value = Boolean.parseBoolean(rule.getAllowUnauthenticatedJoin().value());
		}
		
		logger.debug("allowUnauthenticatedJoin({})", value);
		return value;
	}

	/**
	 * Check, whether or not join access control is enforced.
	 * Defaults to false.
	 * 
	 * @param domainId domainId to check
	 * @return true, if access control is enforced (isAccessProtected = true)
	 */
	public boolean enableJoinAccessControl(int domainId) {
		DomainRule rule = getDomainRule(domainId);
		boolean value = false;
		if (rule != null) {
			value = Boolean.parseBoolean(rule.getEnableJoinAccessControl().value());
		}
		
		logger.debug("enableJoinAccessControl({})", value);
		return value;
	}
	
	
	private DomainRule getDomainRule(int domainId) {
		DomainAccessRules domainAccessRules = dar.getDomainAccessRules();
		List<DomainRule> domainRules = domainAccessRules.getDomainRule();
		String domainIdString = "" + domainId;
		
		for (DomainRule rule : domainRules) {
			if (domainIdString.equals(rule.getDomainId())) {
				return rule;
			}
		}
		
		return null;
	}
}
