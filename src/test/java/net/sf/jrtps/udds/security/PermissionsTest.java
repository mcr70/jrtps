package net.sf.jrtps.udds.security;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sf.jrtps.udds.security.governance.DomainAccessRules;
import net.sf.jrtps.udds.security.governance.DomainAccessRulesNode;
import net.sf.jrtps.udds.security.governance.DomainRule;
import net.sf.jrtps.udds.security.governance.TopicAccessRules;
import net.sf.jrtps.udds.security.governance.TopicRule;
import net.sf.jrtps.udds.security.permissions.Criteria;
import net.sf.jrtps.udds.security.permissions.Grant;
import net.sf.jrtps.udds.security.permissions.Permissions;
import net.sf.jrtps.udds.security.permissions.PermissionsNode;
import net.sf.jrtps.udds.security.permissions.Rule;

import org.junit.Test;

public class PermissionsTest {
    @Test
    public void testPermissions() throws JAXBException {
        System.out.println("testPermissions()");
        JAXBContext jaxbContext = JAXBContext.newInstance(net.sf.jrtps.udds.security.permissions.ObjectFactory.class);

        InputStream is = PermissionsTest.class.getResourceAsStream("/dds_security_permissions_example.xml");
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<PermissionsNode> unmarshal = (JAXBElement<PermissionsNode>) jaxbUnmarshaller.unmarshal(is);
        PermissionsNode pn = unmarshal.getValue(); 

        Permissions permissions = pn.getPermissions();
        List<Grant> grants = permissions.getGrant();
        for (Grant grant : grants) {
            System.out.println("grant " + grant.getName() + ", " + grant.getSubjectName());
            List<JAXBElement<Rule>> allowRuleOrDenyRule = grant.getAllowRuleOrDenyRule();
            for (JAXBElement<Rule> rule : allowRuleOrDenyRule) {
                System.out.println("  - " + rule.getName());
                Rule aRule = rule.getValue();
                List<Criteria> publish = aRule.getPublish();
                for (Criteria crit : publish) {
                    List<JAXBElement<?>> topicOrPartitionOrDataTags = crit.getTopicOrPartitionOrDataTags();
                    for (JAXBElement<?> tpd : topicOrPartitionOrDataTags) {
                        Object value = tpd.getValue();
                        System.out.println("    " + tpd.getName() + ", " + value);
                    }
                }
                
            }
        }
    }


    @Test
    public void testGovernance() throws JAXBException {
        System.out.println("testGovernance()");
        JAXBContext jaxbContext = JAXBContext.newInstance(net.sf.jrtps.udds.security.governance.ObjectFactory.class);

        InputStream is = PermissionsTest.class.getResourceAsStream("/dds_security_governance_example.xml");
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<DomainAccessRulesNode> unmarshal = (JAXBElement<DomainAccessRulesNode>) jaxbUnmarshaller.unmarshal(is);
        DomainAccessRulesNode dar = unmarshal.getValue();
        
        DomainAccessRules domainAccessRules = dar.getDomainAccessRules();
        List<DomainRule> domainRule = domainAccessRules.getDomainRule();
        
        for (DomainRule dr : domainRule) {
            System.out.println("Domain Rule, allowUnauthJoin " + dr.getAllowUnauthenticatedJoin() + ", enable join access control " +
                    dr.getEnableJoinAccessControl() + ", discovery protection " + dr.getDiscoveryProtectionKind());
            System.out.println("   Liveliness protection kind " + dr.getLivelinessProtectionKind() + 
                    ", RTPS protection kind" + dr.getRtpsProtectionKind());
            
            TopicAccessRules topicAccessRules = dr.getTopicAccessRules();
            List<TopicRule> topicRule = topicAccessRules.getTopicRule();
            for (TopicRule tr : topicRule) {
                System.out.println(" - " + tr.getTopicExpression() + ": data protection " + tr.getDataProtectionKind() + 
                        ", discovery protection " + tr.getEnableDiscoveryProtection() + ", metadata " + tr.getMetadataProtectionKind());
            }
        }        
    }
}
