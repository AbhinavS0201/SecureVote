# 🔐 SecureVote — Blockchain-Powered Digital Voting Platform

SecureVote is a secure electronic voting platform that leverages blockchain principles and modern cryptographic techniques to ensure vote integrity, transparency, and voter privacy. The system records votes on an immutable blockchain-inspired ledger, preventing unauthorized modifications while maintaining a verifiable audit trail.

---

## 🌐 Live Demo

**Application:** https://secure-vote-peach.vercel.app/

**Repository:** https://github.com/AbhinavS0201/SecureVote

---

## 🚀 Overview

Traditional digital voting systems often face challenges related to trust, vote manipulation, and transparency. SecureVote addresses these concerns by implementing a blockchain-based architecture that provides:

* Immutable vote records
* End-to-end vote verification
* Tamper detection mechanisms
* Cryptographically secured authentication
* Transparent vote auditing
* Privacy-preserving voter identification

The project demonstrates the practical application of blockchain concepts, cryptographic security, and distributed ledger principles within an electronic voting environment.

---

## ✨ Core Features

### 🗳 Secure Voting System

* Digital voter registration and authentication
* One-voter-one-vote enforcement
* Real-time vote submission and validation
* Election result computation

### ⛓ Blockchain Ledger

* Custom blockchain implementation
* SHA-256 block hashing
* Hash-linked block structure
* Immutable vote storage

### 🔐 Advanced Security

* PBKDF2 password hashing
* RSA digital signatures
* Secure session management
* Cryptographic vote verification

### 📊 Transparency & Integrity

* Tamper detection
* Vote auditability
* Blockchain validation
* Secure election records

---

## 🏗 System Architecture

```text
Frontend (HTML/CSS/JavaScript)
            │
            ▼
      Java Servlets
            │
            ▼
      Voting Services
            │
            ▼
     Blockchain Ledger
            │
            ▼
 Cryptographic Validation
```

---

## 🛠 Technology Stack

### Backend

* Java 11
* Java Servlets
* Maven
* Apache Tomcat

### Frontend

* HTML5
* CSS3
* JavaScript

### Security

* SHA-256 Hashing
* PBKDF2 Password Encryption
* RSA-2048 Digital Signatures
* Secure Session Tokens

### Blockchain Components

* Block Generation
* Hash Chaining
* Proof-of-Work Mechanism
* Ledger Validation

---

## 📂 Project Structure

```text
SecureVote
│
├── AuthServlet.java
├── VoteServlet.java
├── Block.java
├── Blockchain.java
├── CryptoUtils.java
├── VoteData.java
├── VoterService.java
├── VotingService.java
├── index.html
├── pom.xml
└── README.md
```

---

## 🔒 Security Implementation

### Password Protection

* PBKDF2WithHmacSHA256
* Salted password storage
* High iteration count protection

### Digital Signatures

* RSA-2048 key pairs
* Vote authenticity verification
* Signature-based integrity checks

### Blockchain Security

* SHA-256 block hashing
* Linked block validation
* Tamper-resistant vote records

### Session Security

* Secure token generation
* Authentication controls
* Login attempt protection

---

## 🚀 Local Setup

### Prerequisites

* Java 11+
* Maven 3+
* Apache Tomcat 9+

### Installation

```bash
git clone https://github.com/AbhinavS0201/SecureVote.git

cd SecureVote

mvn clean package
```

Deploy the generated WAR file to Apache Tomcat and start the server.

---

## 📈 Future Enhancements

* Multi-election management
* Administrative dashboard
* Cloud deployment support
* Database integration
* Mobile application
* Advanced analytics
* Voter verification workflows

---

## 👨‍💻 Author

**Abhinav Rama**

GitHub: https://github.com/AbhinavS0201

---

## 📄 License

This project is licensed under the MIT License.
