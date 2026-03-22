package com.securevote.blockchain;

import com.securevote.crypto.CryptoUtils;
import java.time.Instant;

/**
 * Represents a single block in the blockchain.
 * Each block is cryptographically linked to the previous block via its hash.
 */
public class Block {

    private final int index;
    private final long timestamp;
    private final VoteData voteData;
    private final String previousHash;
    private final int nonce;
    private final String hash;

    public Block(int index, VoteData voteData, String previousHash) {
        this.index        = index;
        this.timestamp    = Instant.now().toEpochMilli();
        this.voteData     = voteData;
        this.previousHash = previousHash;
        this.nonce        = mineBlock(4);          // Proof-of-Work: 4 leading zeros
        this.hash         = calculateHash();
    }

    // ── Proof-of-Work mining ──────────────────────────────────────────────────
    private int mineBlock(int difficulty) {
        String target = "0".repeat(difficulty);
        int n = 0;
        while (!calculateHashWithNonce(n).substring(0, difficulty).equals(target)) {
            n++;
        }
        return n;
    }

    // ── Hash computation ──────────────────────────────────────────────────────
    public String calculateHash() {
        return calculateHashWithNonce(this.nonce);
    }

    private String calculateHashWithNonce(int n) {
        String raw = index + timestamp + (voteData != null ? voteData.toHashString() : "GENESIS")
                   + previousHash + n;
        return CryptoUtils.sha256(raw);
    }

    // ── Validation ────────────────────────────────────────────────────────────
    public boolean isValid() {
        return hash.equals(calculateHash());
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getIndex()        { return index; }
    public long   getTimestamp()    { return timestamp; }
    public VoteData getVoteData()   { return voteData; }
    public String getPreviousHash() { return previousHash; }
    public int    getNonce()        { return nonce; }
    public String getHash()         { return hash; }

    @Override
    public String toString() {
        return String.format(
            "Block{index=%d, hash='%s...', prevHash='%s...', nonce=%d, vote=%s}",
            index,
            hash.substring(0, 12),
            previousHash.substring(0, 12),
            nonce,
            voteData != null ? voteData.getCandidateId() : "GENESIS"
        );
    }
}
