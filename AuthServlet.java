package com.securevote.servlet;

import com.securevote.service.VoterService;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * Handles /api/auth/* requests:
 *   POST /api/auth/register  — voter registration
 *   POST /api/auth/login     — authentication → session token
 *   POST /api/auth/logout    — invalidate session
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final VoterService voterService = VoterService.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        // Basic security headers
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");

        String body = req.getReader().lines().collect(Collectors.joining());
        JSONObject json = new JSONObject(body);
        String pathInfo = req.getPathInfo();

        PrintWriter out = resp.getWriter();

        try {
            switch (pathInfo) {
                case "/register": {
                    String voterId   = json.getString("voterId");
                    String name      = json.getString("name");
                    String email     = json.getString("email");
                    String password  = json.getString("password");

                    voterService.registerVoter(voterId, name, email, password);

                    out.print(new JSONObject()
                        .put("success", true)
                        .put("message", "Registration successful! You can now log in.")
                        .toString());
                    break;
                }
                case "/login": {
                    String voterId  = json.getString("voterId");
                    String password = json.getString("password");

                    String token = voterService.login(voterId, password);

                    // Set secure, HttpOnly cookie
                    Cookie cookie = new Cookie("SESSION_TOKEN", token);
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(3600);  // 1 hour
                    // cookie.setSecure(true); // Enable in HTTPS production
                    cookie.setPath("/");
                    resp.addCookie(cookie);

                    out.print(new JSONObject()
                        .put("success", true)
                        .put("message", "Login successful!")
                        .put("token",   token)
                        .toString());
                    break;
                }
                case "/logout": {
                    String token = extractToken(req);
                    if (token != null) voterService.logout(token);

                    // Clear cookie
                    Cookie cookie = new Cookie("SESSION_TOKEN", "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    resp.addCookie(cookie);

                    out.print(new JSONObject()
                        .put("success", true)
                        .put("message", "Logged out.")
                        .toString());
                    break;
                }
                default:
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(new JSONObject().put("error", "Unknown endpoint").toString());
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject().put("error", e.getMessage()).toString());
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(new JSONObject().put("error", e.getMessage()).toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(new JSONObject().put("error", "Internal error: " + e.getMessage()).toString());
        }
    }

    private String extractToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("SESSION_TOKEN".equals(c.getName())) return c.getValue();
            }
        }
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
