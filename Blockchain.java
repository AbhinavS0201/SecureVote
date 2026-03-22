package com.securevote.blockchain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The immutable Blockchain Ledger.
 *
 * Design principles:
 *  - Singleton — one chain per JVM lifetime
 *  - Thread-safe additions via synchronized methods
 *  - Full-chain validation on every read
 *  - Double-vote prevention via an in-memory voter registry
 */
public class Blockchain {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static Blockchain instance;
    public static synchronized Blockchain getInstance() {
        if (instance == null) instance = new Blockchain();
        return instance;
    }

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<Block>         chain        = new ArrayList<>();
    private final Set<String>         usedVoters   = ConcurrentHashMap.newKeySet();
    private final Map<String, Long>   voteTally    = new ConcurrentHashMap<>();

    private Blockchain() {
        chain.add(createGenesisBlock());
    }

    // ── Genesis Block ─────────────────────────────────────────────────────────
    private Block createGenesisBlock() {
        return new Block(0, null, "0000000000000000000000000000000000000000000000000000000000000000");
    }

    // ── Core Operations ───────────────────────────────────────────────────────

    /**
     * Adds a new vote to the blockchain.
     * @throws IllegalStateException if the voter has already voted.
     */
    public synchronized boolean addVote(VoteData voteData) {
        if (usedVoters.contains(voteData.getVoterIdHash())) {
            throw new IllegalStateException("Voter has already cast a vote.");
        }

        Block latest = getLatestBlock();
        Block newBlock = new Block(chain.size(), voteData, latest.getHash());

        chain.add(newBlock);
        usedVoters.add(voteData.getVoterIdHash());
        voteTally.merge(voteData.getCandidateId(), 1L, Long::sum);
        return true;
    }

    /**
     * Validates every block's hash integrity and chain linkage.
     */
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current  = chain.get(i);
            Block previous = chain.get(i - 1);

            if (!current.isValid()) return false;
            if (!current.getPreviousHash().equals(previous.getHash())) return false;
        }
        return true;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Block getLatestBlock() { return chain.get(chain.size() - 1); }

    public List<Block> getChain() { return Collections.unmodifiableList(chain); }

    public Map<String, Long> getVoteTally() { return Collections.unmodifiableMap(voteTally); }

    public boolean hasVoterVoted(String voterIdHash) { return usedVoters.contains(voterIdHash); }

    public int getTotalVotes() { return chain.size() - 1; } // exclude genesis

    /** Returns the winning candidate (or "No votes yet"). */
    public String getWinner() {
        return voteTally.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("No votes yet");
    }

    /** Summarized chain report for admin dashboard. */
    public List<Map<String, Object>> getChainSummary() {
        return chain.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("index",        b.getIndex());
            m.put("hash",         b.getHash().substring(0, 16) + "...");
            m.put("previousHash", b.getPreviousHash().substring(0, 16) + "...");
            m.put("timestamp",    b.getTimestamp());
            m.put("nonce",        b.getNonce());
            if (b.getVoteData() != null) {
                m.put("candidate",    b.getVoteData().getCandidateId());
                m.put("voterHash",    b.getVoteData().getVoterIdHash().substring(0, 12) + "...");
            } else {
                m.put("candidate",    "GENESIS");
                m.put("voterHash",    "—");
            }
            return m;
        }).collect(Collectors.toList());
    }
}
