package com.securevote.blockchain;

import com.securevote.crypto.CryptoUtils;

/**
 * Immutable data object embedded inside each blockchain block.
 * Contains: voter identity (hashed), candidate choice, and a digital signature.
 */
public class VoteData {

    private final String voterIdHash;      // SHA-256 of voter's ID — never store plain ID
    private final String candidateId;
    private final long   castAt;
    private final String digitalSignature; // Signed by the server's private key

    public VoteData(String voterId, String candidateId, long castAt, String digitalSignature) {
        this.voterIdHash      = CryptoUtils.sha256(voterId);  // Hash before storing
        this.candidateId      = candidateId;
        this.castAt           = castAt;
        this.digitalSignature = digitalSignature;
    }

    /**
     * Produces a deterministic string for hashing into the block.
     */
    public String toHashString() {
        return voterIdHash + "|" + candidateId + "|" + castAt + "|" + digitalSignature;
    }

    public String getVoterIdHash()      { return voterIdHash; }
    public String getCandidateId()      { return candidateId; }
    public long   getCastAt()           { return castAt; }
    public String getDigitalSignature() { return digitalSignature; }
}
