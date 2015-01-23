package net.sf.jrtps.udds.security;

import java.security.cert.X509Certificate;

import net.sf.jrtps.builtin.ParticipantData;

/**
 * AuthenticationData
 * @author mcr70
 */
class AuthenticationData {
	private ParticipantData participantData;
	private byte[] challengeA;	
	private byte[] challengeB;
	private byte[] sharedSecret;
	private X509Certificate certificate;

	/**
	 * Constructs AuthenticationData
	 * @param pd ParticipantData of remote participant
	 */
	AuthenticationData(ParticipantData pd) {
		participantData = pd;
	}

	/** 
	 * Gets the ParticipantData of remote particiapnt
	 * @return ParticipantData
	 */
	ParticipantData getParticipantData() {
		return participantData;
	}
	
	/**
	 * Gets shared_secret
	 * @return shared secret
	 */
	byte[] getSharedSecret() {
		return sharedSecret;
	}
	
	void setSharedSecret(byte[] sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
	}
	
	X509Certificate getCertificate() {
		return certificate;
	}

	/**
	 * Sets the challenge bytes used when creating handshake reply message(Challenge_B)
	 * @param Challenge_B
	 */
	void setReplyChallenge(byte[] challengeBytes) {
		this.challengeB = challengeBytes;
	}

	/**
	 * Gets the challenge bytes used with handshake reply message(Challenge_B)
	 * @return Challenge_B
	 */
	byte[] getReplyChallengeBytes() {
		return challengeB;
	}

	void setRequestChallenge(byte[] challenge) {
		challengeA = challenge;
	}
	
	/**
	 * Gets the challenge bytes used with handshake request message(Challenge_A)
	 * @return Challenge_A
	 */
	byte[] getRequestChallenge() {
		return challengeA;
	}
}
