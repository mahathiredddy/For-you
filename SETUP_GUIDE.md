# WE’RE HERE: Production Setup & External Configuration Guide

**Application ID:** `com.aistudio.werehere.kxmpzq`  
**Target Environment:** Android SDK 36 (Java 11, Kotlin 2.0.21, Compose M3)  

This guide provides the exact, step-by-step external setup required to transition WE’RE HERE from local offline mode to full cloud synchronization and secure server-side AI execution.

---

## 1. Firebase Project Creation & Android Registration

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project (e.g. `were-here-prod`).
2. Add an Android Application:
   - **Package name / Application ID:** `com.aistudio.werehere.kxmpzq` (must match exactly)
   - **App nickname:** `WE'RE HERE`
3. Generate and register your SHA-1 and SHA-256 certificate fingerprints:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
   Add both SHA-1 and SHA-256 fingerprints to Project Settings > General > Your Apps in the Firebase Console.
4. Download `google-services.json` from the Firebase Console.
5. Place the file at `/app/google-services.json`.

---

## 2. Authentication Configuration

1. In the Firebase Console, navigate to **Build > Authentication > Sign-in method**.
2. **Email/Password**: Enable Email/Password provider.
3. **Google Sign-In**:
   - Enable Google provider.
   - Note the **Web client ID** automatically generated under Web SDK configuration.
   - In the Android app, pass this Web client ID to `AuthViewModel.signInWithGoogle(context, serverClientId)`.

---

## 3. Firestore Provisioning & Rules Deployment

1. Navigate to **Build > Firestore Database** and click **Create database**.
2. Choose a production region (e.g. `asia-south1` or `us-central1`) and select Production mode.
3. Deploy the zero-trust security rules included in the repository:
   ```bash
   firebase deploy --only firestore:rules
   ```
   *The rules enforce that `/users/{userId}/**` can strictly be read or written by `request.auth.uid == userId`.*

---

## 4. Deploying the Secure Gemini AI Backend Proxy

To protect your Gemini API keys from client extraction:
1. Navigate to the `/backend` directory:
   ```bash
   cd backend
   npm install
   ```
2. Configure the secret in Google Cloud Secret Manager / Cloud Functions environment:
   ```bash
   firebase functions:config:set gemini.key="YOUR_GEMINI_API_KEY"
   # or with Secret Manager:
   firebase functions:secrets:set GEMINI_API_KEY
   ```
3. Deploy the function:
   ```bash
   firebase deploy --only functions
   ```
4. Copy the deployed function URL (e.g. `https://<region>-<project>.cloudfunctions.net/generateAiContent`) and enter it in **Settings > Configurable Backend Endpoint**.

---

## 5. Local Firebase Emulator Suite (Testing)

To test rules and sync locally without cloud charges:
```bash
firebase emulators:start --only firestore,auth
```
Connect Android debug builds by configuring the emulator host in `FirestoreSyncManager`.

---

## 6. Manual vs. Automated Matrix

| Configuration Component | Handled Locally in Code | Manual Owner Step in Console |
|---|---|---|
| Offline-First Room DB | Yes (Version 1, Zero Destructive Fallback) | None |
| Dynamic Unit Economics & Scores | Yes (Defensive Math, Edge Cases Covered) | None |
| Auth UI & Credential Manager Flow | Yes (AuthViewModel + Compose Screens) | Enable Providers in Firebase Console |
| `google-services.json` | Scaffolding warns cleanly if missing | Download and place in `/app` |
| Web Client ID for Google Auth | Hooked into `AuthScreen.kt` | Copy from Firebase Web Auth config |
| Gemini Key Storage | Secure Header + Redacted Errors | Configure Secret Manager on Cloud Functions |
| Play Store Listing & Data Safety | Disclosures drafted in `PRIVACY_POLICY.md` | Submit in Google Play Console |
