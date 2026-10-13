# Privacy Policy for WE’RE HERE

**Last Updated:** October 10, 2026  
**Product:** WE’RE HERE (Personal Goal Execution Platform)  
**Platform:** Android Native Application  

WE’RE HERE is dedicated to helping individuals turn high-level ambitions into concrete, executable roadmaps while protecting user privacy. This Privacy Policy explains our local-first data architecture, how data is processed, and your rights.

---

### 1. Data Stored Locally on Your Device

WE’RE HERE is designed from the ground up as an **offline-first and local-first** application:
- **Goals and Roadmaps**: Your goal descriptions, target dates, phases, milestones, and daily tasks are stored in a private SQLite database (`were_here_database`) on your device.
- **Notes and Personal Memory**: Research notes, tags, lessons learned, and AI adaptation logs remain stored locally in private application storage.
- **Check-In Logs and Habits**: Daily ratings, hours invested, and blocker notes are kept on your device.

We do not read, transmit, or monetize your local personal notes or goal entries without explicit cloud synchronization.

---

### 2. External Cloud Services & AI Processing

- **Google Gemini API**: When you configure a Gemini API key or use connected AI features (such as roadmap generation, goal feasibility analysis, or AI Coach prompts), the specific context needed for that prompt (e.g. goal title, target deadline, reported skill gaps) is transmitted securely over HTTPS to Google’s Generative Language API. No unrelated private notes or database records are sent.
- **Firebase Authentication & Firestore Sync (Optional)**: If you sign in using Google or Email, your email address, display name, and unique user identifier (`UID`) are processed by Google Firebase to authenticate you. Cloud synchronization, when enabled, syncs your personal data subtree under `/users/{your_uid}/` strictly for multi-device backup.
- **Zero Third-Party Advertising**: WE’RE HERE contains zero advertising SDKs, third-party data brokers, or marketing tracking beacons.

---

### 3. Data Retention and Account Deletion

You maintain complete ownership of your data at all times:
- **Local Data Reset**: You can wipe all local journey records, goals, and chat memory at any time via **Settings > Data Reset & Clear History**.
- **Cloud Account Deletion**: In compliance with Google Play Developer policies, authenticated users can sign out, delete their cloud records, or request full account termination via the in-app Account screen or by contacting the developer.

---

### 4. Information Required Prior to Store Publishing

Before publishing to the Google Play Store or public channels, the application owner must provide:
- **Developer Name / Legal Entity**: Genuine legal entity or developer identity.
- **Contact Support Email**: Valid support email address.
- **Hosted Public Web URL**: The policy markdown hosted on a publicly reachable URL for Google Play Console submission.

---

### 5. Contact Us

For questions regarding this policy or data safety, contact:  
`support@werehere.app` (or the developer contact configured in Google Play Console).
