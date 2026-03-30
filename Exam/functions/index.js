/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {onSchedule} = require("firebase-functions/v2/scheduler");
const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");

// Initialize Firebase Admin
initializeApp();

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance.
setGlobalOptions({ maxInstances: 10 });

/**
 * Scheduled function that runs every hour to check if the global session has expired.
 * If more than sessionLength (default 24h) has passed, it resets the session and all game objects.
 */
exports.resetSession = onSchedule("every 1 hours", async (event) => {
  const db = getFirestore();

  try {
    const sessionRef = db.collection("globalSession").doc("current");
    const sessionDoc = await sessionRef.get();

    // Initialize if it doesn't exist
    if (!sessionDoc.exists) {
      logger.info("No global session found, creating initial session...");
      const newSessionId = Date.now().toString();
      await sessionRef.set({
        sessionId: newSessionId,
        sessionStart: new Date(),
        sessionLength: 24,
      });
      return;
    }

    const data = sessionDoc.data();
    const now = Date.now();
    const sessionStart = data.sessionStart.toMillis();
    const hoursPassed = (now - sessionStart) / (1000 * 60 * 60);
    const sessionLength = data.sessionLength || 24;

    if (hoursPassed >= sessionLength) {
      logger.info(`Session expired (${hoursPassed.toFixed(2)} hours passed). Resetting...`);
      const newSessionId = Date.now().toString();

      // 1. Uppdatera session
      await sessionRef.set({
        sessionId: newSessionId,
        sessionStart: new Date(),
        sessionLength: 24,
      });

      // 2. Reset objekt i objects kollektionen
      const snapshot = await db.collection("objects").get();
      const chunks = [];
      const size =500;

    // Split into chunks of 500 (Firestore batch limit)
    for (let i = 0; i < snapshot.docs.length; i += size) {
        chunks.push(snapshot.docs.slice(i, i + size));
    }

    for (const chunk of chunks) {
        const batch = db.batch();
        chunk.forEach(doc => {
            batch.update(doc.ref, {
                active: true,
                foundBy: "",
                sessionId: newSessionId // This matches your new ViewModel logic
        });
    });
    await batch.commit();
        logger.info(`Successfully reset session and ${snapshot.size} objects.`);
      }
    } else {
      logger.info(`Session still active. ${hoursPassed.toFixed(2)}/${sessionLength} hours passed.`);
    }
  } catch (error) {
    logger.error("Error in resetSession function:", error);
  }
});

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
