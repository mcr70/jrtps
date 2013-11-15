package net.sf.jrtps;

import java.util.HashMap;

import net.sf.jrtps.message.parameter.QosPolicy;

public class QualityOfService {
	private HashMap<Class<? extends QosPolicy>, QosPolicy> policies = new HashMap<>();
	
	public void addPolicy(QosPolicy policy) {
		policies.put(policy.getClass(), policy);
	}
	
	public QosPolicy getPolicy(Class<? extends QosPolicy> policyClass) {
		QosPolicy policy = policies.get(policyClass);
		if (policy == null) {
			policy = getDefaultPolicy(policyClass);
		}
		
		return policy;
	}

	private QosPolicy getDefaultPolicy(Class<? extends QosPolicy> policyClass) {
		// TODO Auto-generated method stub
		return null;
	}
}
