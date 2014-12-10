package net.sf.jrtps.udds.security;

import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.builtin.ParticipantData;

/**
 * 
 * @author mcr70
 */
class AuthenticationData {
	private ParticipantData participantData;
	private byte[] sharedSecret;
	private byte[] challenge;
	private X509Certificate certificate;
	private byte[] challengeB;
	private byte[] challengeA;
	private final CountDownLatch timeOutLatch;
	

	public AuthenticationData(X509Certificate certificate) {
		this.certificate = certificate;
		timeOutLatch = new CountDownLatch(1);
	}

	public AuthenticationData(ParticipantData pd) {
		participantData = pd;
		timeOutLatch = new CountDownLatch(1);
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

	/**
	 * Sets the challenge bytes used when creating handshake reply message(Challenge_B)
	 * @param Challenge_B
	 */
	public void setReplyChallenge(byte[] challengeBytes) {
		this.challengeB = challengeBytes;
	}

	/**
	 * Gets the challenge bytes used with handshake reply message(Challenge_B)
	 * @return Challenge_B
	 */
	public byte[] getReplyChallengeBytes() {
		return challengeB;
	}

	public void setRequestChallenge(byte[] challenge) {
		challengeA = challenge;
	}
	
	public byte[] getChallengeRequestChallenge() {
		return challengeA;
	}

//	public String toString() {
//		return certificate.getSubjectDN().toString();
//	}

	public CountDownLatch getTimeoutLatch() {
		return timeOutLatch;
	}
}
