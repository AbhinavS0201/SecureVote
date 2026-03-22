# 🔐 Secure Blockchain-Based Voting System

## Project Overview
A tamper-proof digital voting system built on a custom Java blockchain with cryptographic security.

---

## 🏗️ Project Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      BROWSER (CLIENT)                        │
│  HTML + CSS + JS  ──►  REST API calls over HTTPS            │
└───────────────────────────┬─────────────────────────────────┘
                            │  JSON / HTTP
┌───────────────────────────▼─────────────────────────────────┐
│                 JAVA BACKEND (Tomcat 9)                       │
│                                                               │
│  ┌─────────────┐   ┌─────────────┐   ┌────────────────────┐ │
│  │ AuthServlet │   │ VoteServlet │   │   CryptoUtils      │ │
│  │/api/auth/*  │   │/api/vote/*  │   │ SHA-256 / PBKDF2   │ │
│  └──────┬──────┘   └──────┬──────┘   │ RSA-2048 / HMAC    │ │
│         │                 │           └────────────────────┘ │
│  ┌──────▼──────┐   ┌──────▼──────┐                          │
│  │VoterService │   │VotingService│                          │
│  │ Registration│   │  Vote Logic │                          │
│  │ Auth/Session│   │  Results   │                          │
│  └─────────────┘   └──────┬──────┘                          │
└──────────────────────────┬┴─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                   BLOCKCHAIN LEDGER                          │
│                                                               │
│  Block[0] ──► Block[1] ──► Block[2] ──► Block[N]            │
│  (Genesis)    (Vote 1)     (Vote 2)     (Vote N)             │
│                                                               │
│  Each Block Contains:                                         │
│  • Index       • Timestamp    • VoteData                     │
│  • PrevHash    • Hash         • Nonce (PoW)                  │
│                                                               │
│  VoteData Contains:                                           │
│  • voterIdHash (SHA-256)      • candidateId                  │
│  • castAt (epoch ms)          • digitalSignature (RSA)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 System Modules

| Module | Class | Responsibility |
|---|---|---|
| **Blockchain** | `Block.java` | Immutable block with hash linkage + PoW mining |
| **Blockchain** | `Blockchain.java` | Singleton chain manager, validation, tally |
| **Blockchain** | `VoteData.java` | Encrypted vote payload inside each block |
| **Crypto** | `CryptoUtils.java` | SHA-256, PBKDF2, RSA-2048, session tokens |
| **Model** | `Voter.java` | Voter entity with hashed password |
| **Model** | `Candidate.java` | Election candidate entity |
| **Service** | `VoterService.java` | Registration, login, sessions, lockout |
| **Service** | `VotingService.java` | Vote orchestration, results, chain query |
| **Servlet** | `AuthServlet.java` | REST `/api/auth/*` — register / login / logout |
| **Servlet** | `VoteServlet.java` | REST `/api/vote/*` — cast / results / explorer |
| **Frontend** | `index.html` | SPA voting interface |

---

## 🔐 How Cryptography Secures the System

### 1. Password Storage — PBKDF2WithHmacSHA256
```
Password "MyPass@1" ──► PBKDF2(310,000 iterations, random 16-byte salt)
                    ──► "base64salt$base64hash"  ← stored in DB
```
- **310,000 iterations** make brute-force attacks ~billion× slower
- **Random salt** prevents rainbow table attacks
- **Constant-time comparison** (`MessageDigest.isEqual`) prevents timing attacks

### 2. Voter Anonymity — SHA-256 Hashing
```
VoterID "VTR-001" ──► SHA-256 ──► "3a7f9b2c..."  ← stored in blockchain
```
- Voter identity is irreversibly anonymised before hitting the ledger
- Double-vote detection uses the hash, not the real ID

### 3. Vote Authenticity — RSA-2048 Digital Signature
```
payload = voterIdHash + "|" + candidateId + "|" + timestamp
signature = RSA_PRIVATE_KEY.sign(SHA-256(payload))
```
- Only the server (holding the private key) can produce valid signatures
- Any tampered vote fails signature verification → block is invalid

### 4. Block Integrity — SHA-256 Chain Linking
```
Block[N].hash = SHA-256(index + timestamp + voteData + Block[N-1].hash + nonce)
```
- Changing any vote changes that block's hash
- This breaks the link to Block[N+1], Block[N+2], … → tampering detected immediately

### 5. Proof-of-Work — Anti-Spam Mining
```
Mine until: SHA-256(blockData + nonce).startsWith("0000")
```
- Requires ~65,000 hash attempts per block
- Re-mining the entire chain after tampering is computationally infeasible

### 6. Session Security — 256-bit Random Tokens
```
token = SecureRandom.nextBytes(32) → Base64URL encoded
```
- Sent as `HttpOnly` cookie (inaccessible to JavaScript → XSS protection)
- Expires after 1 hour
- Account locked after 5 failed login attempts

---

## 🚀 How to Run

### Prerequisites
- Java 11+
- Maven 3.6+
- Tomcat 9 (or use embedded plugin)

### Build & Deploy
```bash
# Clone / navigate to project
cd blockchain-voting

# Build WAR
mvn clean package

# Deploy to Tomcat
cp target/blockchain-voting.war $TOMCAT_HOME/webapps/

# OR run with embedded Tomcat
mvn tomcat7:run

# Open browser
open http://localhost:8080
```

### Default Admin Credentials
```
Voter ID : ADMIN001
Password : Admin@1234
```

---

## 🔒 Security Checklist

- [x] Passwords hashed with PBKDF2 (310k iterations)
- [x] Voter IDs anonymised via SHA-256
- [x] Each vote signed with RSA-2048
- [x] Blockchain chain-linked via SHA-256
- [x] Proof-of-Work prevents cheap tampering
- [x] Double-vote prevention (in-memory + blockchain hash set)
- [x] Session tokens: 256-bit random, HttpOnly cookie
- [x] Login lockout after 5 failed attempts
- [x] Constant-time password comparison
- [x] No plaintext secrets stored anywhere

---

## 📁 File Structure

```
blockchain-voting/
├── pom.xml
├── README.md
├── webapp/
│   └── index.html                          ← Frontend SPA
└── src/main/java/com/securevote/
    ├── blockchain/
    │   ├── Block.java                      ← Block entity + mining
    │   ├── Blockchain.java                 ← Ledger singleton
    │   └── VoteData.java                   ← Vote payload
    ├── crypto/
    │   └── CryptoUtils.java                ← All crypto ops
    ├── model/
    │   ├── Voter.java
    │   └── Candidate.java
    ├── service/
    │   ├── VoterService.java               ← Auth & registration
    │   └── VotingService.java              ← Vote orchestration
    └── servlet/
        ├── AuthServlet.java                ← /api/auth/*
        └── VoteServlet.java                ← /api/vote/*
```
