package com.securevote.service;

import com.securevote.blockchain.Blockchain;
import com.securevote.blockchain.VoteData;
import com.securevote.crypto.CryptoUtils;
import com.securevote.model.Candidate;
import com.securevote.model.Voter;

import java.time.Instant;
import java.util.*;

/**
 * Orchestrates the vote-casting workflow:
 *  1. Verify session & voter eligibility
 *  2. Create a digitally-signed VoteData payload
 *  3. Submit it to the Blockchain ledger
 *  4. Mark voter as "has voted" to prevent double voting
 */
public class VotingService {

    private static VotingService instance;
    public static synchronized VotingService getInstance() {
        if (instance == null) instance = new VotingService();
        return instance;
    }

    private final Blockchain    blockchain   = Blockchain.getInstance();
    private final VoterService  voterService = VoterService.getInstance();
    private final Map<String, Candidate> candidates = new LinkedHashMap<>();

    private VotingService() {
        // Seed candidates
        candidates.put("C001", new Candidate("C001", "Alice Sharma",  "Progressive Alliance",  "Education & Healthcare reform"));
        candidates.put("C002", new Candidate("C002", "Bob Reddy",     "National Unity Party",  "Economic growth & Infrastructure"));
        candidates.put("C003", new Candidate("C003", "Carol Mehta",   "Green Future Party",    "Climate action & Renewables"));
        candidates.put("C004", new Candidate("C004", "David Khan",    "Digital India Front",   "Technology & Innovation"));
    }

    // ── Cast Vote ─────────────────────────────────────────────────────────────

    /**
     * Records a vote on the blockchain.
     *
     * @param sessionToken active session token
     * @param candidateId  chosen candidate
     * @return confirmation message
     * @throws SecurityException       if the token is invalid
     * @throws IllegalStateException   if the voter already voted
     * @throws IllegalArgumentException if the candidate is unknown
     */
    public String castVote(String sessionToken, String candidateId) {
        // 1. Resolve voter
        Voter voter = voterService.getVoterByToken(sessionToken);
        if (voter == null) {
            throw new SecurityException("Invalid or expired session. Please log in again.");
        }

        // 2. Check eligibility
        if (voter.isHasVoted()) {
            throw new IllegalStateException("You have already cast your vote.");
        }

        // 3. Validate candidate
        if (!candidates.containsKey(candidateId)) {
            throw new IllegalArgumentException("Unknown candidate: " + candidateId);
        }

        // 4. Build signed payload
        long now        = Instant.now().toEpochMilli();
        String payload  = voter.getVoterId() + "|" + candidateId + "|" + now;
        String signature = CryptoUtils.signData(payload);

        VoteData voteData = new VoteData(voter.getVoterId(), candidateId, now, signature);

        // 5. Append to blockchain (atomically)
        blockchain.addVote(voteData);

        // 6. Mark voter
        voter.setHasVoted(true);

        return "Vote cast successfully! Your vote is secured in block #"
             + (blockchain.getTotalVotes()) + ".";
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Collection<Candidate> getCandidates() { return candidates.values(); }

    public Candidate getCandidate(String id) { return candidates.get(id); }

    public Map<String, Long> getResults() { return blockchain.getVoteTally(); }

    /** Enriched results map: candidateId → {name, party, votes} */
    public List<Map<String, Object>> getEnrichedResults() {
        Map<String, Long> tally = blockchain.getVoteTally();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Candidate c : candidates.values()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id",     c.getCandidateId());
            entry.put("name",   c.getName());
            entry.put("party",  c.getParty());
            entry.put("votes",  tally.getOrDefault(c.getCandidateId(), 0L));
            result.add(entry);
        }

        result.sort((a, b) -> Long.compare((Long) b.get("votes"), (Long) a.get("votes")));
        return result;
    }

    public boolean isChainValid() { return blockchain.isChainValid(); }

    public int getTotalVotes() { return blockchain.getTotalVotes(); }
}
