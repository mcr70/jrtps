package net.sf.jrtps.udds.security;

import java.security.cert.X509Certificate;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.types.Guid;

/**
 * 
 * @author mcr70
 */
class AuthenticationData {
	private ParticipantData participantData;
	private byte[] sharedSecret;
	private byte[] challenge;
	private X509Certificate certificate;
	private Guid sourceGuid;
	
	AuthenticationData(ParticipantData pd) {
		this.participantData = pd;
	}
	
	public AuthenticationData(Guid sourceGuid) {
		this.sourceGuid = sourceGuid;
	}

	ParticipantData getParticipantData() {
		return participantData;
	}
	
	byte[] getSharedSecret() {
		return sharedSecret;
	}
	
	void setSharedSecret(byte[] sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	byte[] getChallenge() {
		return challenge;
	}

	void setChallenge(byte[] challenge) {
		this.challenge = challenge;
	}

	void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
	}
	
	public X509Certificate getCertificate() {
		return certificate;
	}
}
