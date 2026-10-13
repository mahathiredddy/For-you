/**
 * WE'RE HERE: Production Secure Gemini Proxy Cloud Function
 *
 * Enforces server-side authentication, rate limiting, and zero client secret exposure.
 */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { GoogleGenAI } = require("@google/genai");

admin.initializeApp();

// Simple in-memory rate limiter per UID (e.g. max 30 requests/hour)
const userRequestCounts = new Map();

exports.generateAiContent = functions.https.onRequest(async (req, res) => {
  // CORS configuration
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type, Authorization");

  if (req.method === "OPTIONS") {
    return res.status(204).send("");
  }

  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method Not Allowed. Use POST." });
  }

  // 1. Verify Authorization Bearer Token
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(401).json({ error: "Unauthorized: Missing Bearer Token" });
  }

  const idToken = authHeader.split("Bearer ")[1];
  let uid = null;

  try {
    const decoded = await admin.auth().verifyIdToken(idToken);
    uid = decoded.uid;
  } catch (err) {
    return res.status(403).json({ error: "Forbidden: Invalid or expired Firebase ID token" });
  }

  // 2. Enforce Per-User Rate Limits
  const now = Date.now();
  const userRate = userRequestCounts.get(uid) || { count: 0, resetAt: now + 3600000 };
  if (now > userRate.resetAt) {
    userRate.count = 0;
    userRate.resetAt = now + 3600000;
  }
  if (userRate.count >= 40) {
    return res.status(429).json({ error: "Rate limit exceeded. Maximum 40 AI requests per hour." });
  }
  userRate.count++;
  userRequestCounts.set(uid, userRate);

  // 3. Extract Prompt & System Instruction
  const { prompt, systemInstruction, forceJson } = req.body;
  if (!prompt || typeof prompt !== "string") {
    return res.status(400).json({ error: "Bad Request: prompt is required" });
  }

  // 4. Resolve Server-Side Gemini API Key from Environment
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    return res.status(503).json({ error: "Server Configuration Error: GEMINI_API_KEY is not configured on Cloud Functions." });
  }

  try {
    const ai = new GoogleGenAI({ apiKey });
    const response = await ai.models.generateContent({
      model: "gemini-2.5-flash",
      contents: prompt,
      config: {
        systemInstruction: systemInstruction || undefined,
        responseMimeType: forceJson ? "application/json" : "text/plain"
      }
    });

    const resultText = response.text || "";
    return res.status(200).json({
      text: resultText,
      uid: uid,
      timestamp: Date.now()
    });
  } catch (apiError) {
    console.error("Gemini API invocation failure:", apiError.message);
    return res.status(502).json({ error: "Upstream AI service error. Operating fallback." });
  }
});
