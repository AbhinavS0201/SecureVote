package com.securevote.servlet;

import com.securevote.blockchain.Blockchain;
import com.securevote.model.Candidate;
import com.securevote.model.Voter;
import com.securevote.service.VoterService;
import com.securevote.service.VotingService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles /api/vote/* requests:
 *   GET  /api/vote/candidates   — list all candidates
 *   POST /api/vote/cast         — cast a vote
 *   GET  /api/vote/results      — public results tally
 *   GET  /api/vote/chain        — admin: full blockchain explorer
 *   GET  /api/vote/status       — current voter's status (has voted?)
 */
@WebServlet("/api/vote/*")
public class VoteServlet extends HttpServlet {

    private final VotingService votingService = VotingService.getInstance();
    private final VoterService  voterService  = VoterService.getInstance();
    private final Blockchain    blockchain    = Blockchain.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();

        try {
            switch (path) {
                case "/candidates": {
                    JSONArray arr = new JSONArray();
                    for (Candidate c : votingService.getCandidates()) {
                        arr.put(new JSONObject()
                            .put("id",        c.getCandidateId())
                            .put("name",      c.getName())
                            .put("party",     c.getParty())
                            .put("manifesto", c.getManifesto()));
                    }
                    out.print(new JSONObject().put("candidates", arr).toString());
                    break;
                }
                case "/results": {
                    List<Map<String, Object>> results = votingService.getEnrichedResults();
                    JSONArray arr = new JSONArray();
                    results.forEach(r -> arr.put(new JSONObject(r)));
                    out.print(new JSONObject()
                        .put("results",     arr)
                        .put("totalVotes",  votingService.getTotalVotes())
                        .put("winner",      blockchain.getWinner())
                        .put("chainValid",  votingService.isChainValid())
                        .toString());
                    break;
                }
                case "/chain": {
                    // Admin only
                    Voter caller = getVoterFromRequest(req);
                    if (caller == null || !"ADMIN".equals(caller.getRole())) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(new JSONObject().put("error", "Admin access required.").toString());
                        return;
                    }
                    List<Map<String, Object>> chain = blockchain.getChainSummary();
                    JSONArray arr = new JSONArray();
                    chain.forEach(b -> arr.put(new JSONObject(b)));
                    out.print(new JSONObject()
                        .put("blocks",     arr)
                        .put("length",     chain.size())
                        .put("isValid",    blockchain.isChainValid())
                        .toString());
                    break;
                }
                case "/status": {
                    Voter voter = getVoterFromRequest(req);
                    if (voter == null) {
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        out.print(new JSONObject().put("error", "Not authenticated.").toString());
                        return;
                    }
                    out.print(new JSONObject()
                        .put("voterId",  voter.getVoterId())
                        .put("name",     voter.getName())
                        .put("hasVoted", voter.isHasVoted())
                        .put("role",     voter.getRole())
                        .toString());
                    break;
                }
                default:
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(new JSONObject().put("error", "Not found").toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if (!"/cast".equals(req.getPathInfo())) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(new JSONObject().put("error", "Not found").toString());
            return;
        }

        try {
            String body        = req.getReader().lines().collect(Collectors.joining());
            JSONObject json    = new JSONObject(body);
            String candidateId = json.getString("candidateId");
            String token       = extractToken(req);

            String result = votingService.castVote(token, candidateId);
            out.print(new JSONObject().put("success", true).put("message", result).toString());

        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(new JSONObject().put("error", e.getMessage()).toString());
        } catch (IllegalStateException | IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject().put("error", e.getMessage()).toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(new JSONObject().put("error", "Internal error: " + e.getMessage()).toString());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Voter getVoterFromRequest(HttpServletRequest req) {
        String token = extractToken(req);
        return (token == null) ? null : voterService.getVoterByToken(token);
    }

    private String extractToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("SESSION_TOKEN".equals(c.getName())) return c.getValue();
            }
        }
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return null;
    }
}
