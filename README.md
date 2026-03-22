
# 🔐 SecureVote: Blockchain-Based Voting System

A **tamper-proof digital voting system** built using a custom Java-based blockchain integrated with strong cryptographic techniques to ensure **security, transparency, and voter anonymity**.

---

## 📌 Project Overview

SecureVote is a decentralized-style voting system where each vote is securely recorded as a block in a blockchain. It ensures:

* 🛡️ No vote tampering
* 🔍 Full transparency
* 👤 Complete voter anonymity
* 🔐 Strong cryptographic protection

---

## 🏗️ System Architecture

```
Client (HTML/JS)
        ↓
REST APIs (HTTP/JSON)
        ↓
Java Backend (Servlets)
        ↓
Blockchain Ledger (Custom Implementation)
```

---

## 📁 Project Structure

```
Secure Blockchain-Based Voting System/
│
├── AuthServlet.java          # Handles authentication APIs
├── VoteServlet.java          # Handles voting APIs
│
├── Block.java                # Blockchain block structure
├── Blockchain.java           # Blockchain management
├── VoteData.java             # Vote payload model
│
├── CryptoUtils.java          # Cryptographic functions
│
├── VoterService.java         # Voter registration & login
├── VotingService.java        # Voting logic & results
│
├── securevote-v3.html        # Frontend UI
├── pom.xml                   # Maven configuration
└── README.md                 # Project documentation
```

---

## ⚙️ Tech Stack

* **Backend:** Java, Servlets
* **Frontend:** HTML, CSS, JavaScript
* **Build Tool:** Maven
* **Server:** Apache Tomcat
* **Security:**

  * SHA-256 Hashing
  * PBKDF2 Password Hashing
  * RSA Digital Signatures
  * Proof-of-Work

---

## 🔐 Security Features

### 🔑 Password Security

* Uses **PBKDF2WithHmacSHA256**
* 310,000 iterations + salt
* Prevents brute-force attacks

---

### 👤 Voter Anonymity

* Voter IDs hashed using **SHA-256**
* No real identity stored on blockchain

---

### ✍️ Vote Authentication

* Each vote is signed using **RSA-2048**
* Ensures authenticity and prevents tampering

---

### ⛓️ Blockchain Integrity

* Each block linked using SHA-256 hashing
* Any modification breaks the chain

---

### ⛏️ Proof-of-Work

* Blocks are mined using nonce
* Prevents spam and unauthorized changes

---

### 🔐 Session Security

* Secure random tokens
* HttpOnly cookies
* Login attempt restrictions

---

## 🚀 How to Run

### Prerequisites

* Java 11+
* Maven 3+
* Apache Tomcat 9+

---

### 🔧 Steps

```bash
# Clone repository
git clone https://github.com/AbhinavS0201/SecureVote.git

# Navigate into project
cd SecureVote

# Build project
mvn clean package

# Deploy to Tomcat
copy target/*.war %TOMCAT_HOME%/webapps/

# Start server and open
http://localhost:8080
```

---

## 🔑 Default Admin Credentials

```
Voter ID: ADMIN001
Password: Admin@1234
```

---

## 📊 Features

* ✔️ Secure voter registration & login
* ✔️ One-person-one-vote enforcement
* ✔️ Blockchain-based vote storage
* ✔️ Real-time vote counting
* ✔️ Tamper detection
* ✔️ Cryptographically secure voting

---

## 🧠 Key Concepts Used

* Blockchain Data Structure
* Cryptographic Hashing
* Digital Signatures
* Proof-of-Work Consensus
* REST API Architecture

---

## 🚀 Future Enhancements

* Database integration (MySQL / MongoDB)
* Multi-election support
* Admin dashboard
* Deployment on cloud
* Mobile application

