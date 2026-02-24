# KeyLock

**Professional Offline Cryptographic & HSM Simulation Platform**

<div align="center">

![Version](https://img.shields.io/badge/version-1.0-green)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue)
![Compose](https://img.shields.io/badge/compose-2024.09-purple)
![License](https://img.shields.io/badge/license-Proprietary-red)

</div>

## 🔐 Overview

KeyLock is a comprehensive cryptographic calculator and Hardware Security Module (HSM) command simulator designed for:

- **Payments Engineers**
- **Cryptography Specialists**  
- **Security Professionals**
- **HSM Operators**
- **Payment Card Industry (PCI) Teams**

### Key Features

✅ **100% Offline Operation** - No cloud, no telemetry, no analytics  
✅ **Comprehensive Crypto Tools** - AES, DES, RSA, ECC, hashing, MAC  
✅ **HSM Simulation** - Thales, Atalla, SafeNet command sets  
✅ **Payments Focused** - CVV, PIN, MAC, DUKPT, EMV operations  
✅ **Secure by Design** - Encrypted storage, memory zeroization, auto-lock  
✅ **Professional UI** - Dark green theme, expert-focused workflow  

## 🎯 Core Modules

### 1. Crypto Calculator

Complete cryptographic operations suite with 7 main categories:

- **Main** - AES/DES encryption, modes (ECB, CBC, CFB, OFB), KCV
- **Generic** - Hashes, encoding, BCD, Base64, UUID
- **Cipher** - AES, DES, RSA, ECC, ECDSA, FPE
- **Keys** - Key generation, HSM keys, key blocks (Thales, TR-31)
- **Payments** - CVV, MAC, PIN, DUKPT, card validation
- **EMV** - Cryptograms, SDA, DDA, secure messaging, ATR/APDU parsing
- **Development** - Padding, trace parsing, bit operations

### 2. HSM Commander

Offline HSM command simulation engine supporting:

- **Thales** - 100+ commands (NO, A0, A6, CA, BK, etc.)
- **Atalla** - Core PIN, key, and MAC commands
- **SafeNet** - Encryption and key management commands

Features:
- Command console with history
- Structured request/response parsing
- Encrypted audit logging
- Load testing capability

### 3. Security Layer

- Master password authentication
- Auto-lock on inactivity
- AES-256-GCM encrypted storage
- Secure memory zeroization
- Future: Biometric authentication

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM + Clean Architecture |
| **Cryptography** | Java Cryptography Architecture (JCA) |
| **Platform** | Android (iOS/Desktop planned) |
| **Build** | Gradle with Kotlin DSL |

## 📱 Screenshots

> Coming soon: Screenshots of Crypto Calculator, HSM Commander, and Console output

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or later
- JDK 21
- Android SDK 26+ (minimum)
- Android SDK 36 (target)

### Build Instructions

1. Clone the repository:
```bash
git clone https://github.com/yourusername/keylock.git
cd keylock
```

2. Open in Android Studio

3. Sync Gradle dependencies

4. Build and run:
```bash
./gradlew assembleDebug
```

Or use Android Studio's Run button.

### Configuration

No configuration needed - the app is 100% offline and requires no API keys or external services.

## 📖 Usage

### Quick Start: AES Encryption

1. Launch KeyLock
2. Enter master password (minimum 8 characters)
3. Navigate to **Crypto Calculator**
4. Select algorithm: **AES-256**, mode: **CBC**
5. Input:
   - Key (64 hex characters)
   - Data (hex or ASCII)
   - IV (32 hex characters)
6. Click **ENCRYPT**
7. View result in console

### HSM Command Example

1. Navigate to **HSM Commander**
2. Select **Thales** vendor
3. Enter command: `NO00` (diagnostics)
4. Click **EXECUTE**
5. View response: `NP00SIM-KEYLOCK-001`

## 📚 Documentation

See [KEYLOCK_PRO_DOCUMENTATION.md](KEYLOCK_PRO_DOCUMENTATION.md) for:

- Complete feature documentation
- Architecture details
- API reference
- Security considerations
- Implementation roadmap

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  ┌────────────┬──────────┬────────────┐ │
│  │   Crypto   │   HSM    │  Security  │ │
│  │ Calculator │Commander │   Vault    │ │
│  └────────────┴──────────┴────────────┘ │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────┴───────────────────────┐
│         Domain Layer (Business Logic)   │
│  ┌────────────┬──────────┬────────────┐ │
│  │   Crypto   │   HSM    │  Security  │ │
│  │   Engine   │  Engine  │   Manager  │ │
│  └────────────┴──────────┴────────────┘ │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────┴───────────────────────┐
│          Data Layer (Storage)           │
│        Encrypted SharedPreferences      │
└─────────────────────────────────────────┘
```

## 🔒 Security

### Implemented

✅ Master password authentication  
✅ Encrypted local storage (AES-256-GCM)  
✅ Memory zeroization for sensitive data  
✅ No network connectivity  
✅ Auto-lock on inactivity  

### Planned

⏳ Biometric authentication (fingerprint, face)  
⏳ Hardware device binding  
⏳ Tamper detection  
⏳ Screen capture prevention  

## � CI/CD Pipeline

**Two Automated Workflows:**

1. **Build KeyLock** - Builds APK artifacts
   - Triggers: Push to dev/qa/main
   - Creates: APK files for installation
   - Artifacts: `keylock-{branch}-{timestamp}-apk`
   
2. **Code Quality & Analysis** - Quality checks only (no APK)
   - Triggers: Push to dev/qa/main
   - Creates: Lint reports, code analysis
   - Artifacts: `code-quality-reports`

**APK Downloads:**

| Branch | Artifact Name | Build Type | Retention |
|--------|--------------|------------|----------|
| **dev** | `keylock-dev-{timestamp}-apk` | Debug APK | 30 days |
| **qa** | `keylock-qa-{timestamp}-apk` | Release APK | 30 days |
| **main** | `keylock-main-{timestamp}-release` | Signed APK + AAB | 90 days |

**Timestamp format:** `ddmmyyyyhhmmss` (e.g., `24022026143000`)

**Getting the APK:**
1. Go to GitHub → **Actions** tab
2. Click **Build KeyLock** workflow (not Code Quality)
3. Scroll to **Artifacts** section
4. Download the timestamped APK

**Workflow:**
1. Develop on `dev` branch (automatic debug builds)
2. Merge to `qa` for QA team testing (automatic release builds)
3. Merge to `main` for production (automatic signing & Play Store artifacts)

**Version Number:** Each build gets a unique version based on build time (ddmmyyyyhhmmss)

## �🗺️ Roadmap

### Version 1.1 (Q2 2026)
- [ ] Complete Payment menu tools (CVV, DUKPT, PIN)
- [ ] Complete EMV menu tools (cryptograms, SDA/DDA)
- [ ] Full RSA implementation
- [ ] Settings screen

### Version 1.2 (Q3 2026)
- [ ] Compose Multiplatform (Desktop support)
- [ ] Biometric authentication
- [ ] Import/export encrypted backups

### Version 2.0 (Q4 2026)
- [ ] Real HSM integration
- [ ] Smartcard reader support
- [ ] Advanced scripting

## 📄 License

Proprietary - All Rights Reserved

This is professional software for cryptography and payments engineers.

## 🤝 Contributing

This is a proprietary project. For collaboration inquiries, please contact the maintainer.

## ⚠️ Disclaimer

This software is for **professional use only**. It provides cryptographic operations for:

- Development and testing
- HSM simulation and training
- Cryptographic validation
- Payment system integration testing

**Not intended for:**
- Production payment processing
- Real financial transactions
- Replacement for certified HSM hardware

Always use certified, audited cryptographic solutions for production systems.

## 📧 Support

For professional support and inquiries:
- Email: support@keylock.pro (coming soon)
- CI/CD Setup: See [CI_CD.md](CI_CD.md)

---

**KeyLock** - Professional Cryptographic Tools for Payments Engineers

*100% Offline • Zero Telemetry • Secure Local Storage*
