/**
 * Stands in for Firebase Cloud Functions, which require the paid Blaze plan for Firestore
 * triggers. This is a plain long-lived Node process: it opens Firestore onSnapshot listeners
 * (an ordinary Admin SDK read, free on Spark) and calls FCM send (also free) directly, instead
 * of Google deploying/running the trigger for you.
 *
 * Deploy: Render free web service, env var FIREBASE_SERVICE_ACCOUNT_B64 = base64 of the
 * service account JSON (Firebase console -> Project settings -> Service accounts -> Generate
 * new private key). Render free tier sleeps after 15min with no HTTP traffic and killing this
 * process along with it -- ping GET / every 10min with a free UptimeRobot monitor to keep
 * pushes near-real-time.
 */

const express = require("express");
const { initializeApp, cert } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

const serviceAccountJson = Buffer.from(process.env.FIREBASE_SERVICE_ACCOUNT_B64 ?? "", "base64").toString("utf8");
if (!serviceAccountJson) {
  throw new Error("FIREBASE_SERVICE_ACCOUNT_B64 env var is required");
}

initializeApp({ credential: cert(JSON.parse(serviceAccountJson)) });
const db = getFirestore();
const messaging = getMessaging();

async function tokensFor(uid) {
  const snapshot = await db.collection("users").doc(uid).collection("fcmTokens").get();
  return snapshot.docs.map((d) => d.id);
}

async function sendToUids(uids, title, body, type) {
  const tokenLists = await Promise.all(uids.map(tokensFor));
  const tokens = tokenLists.flat();
  if (tokens.length === 0) return;

  const response = await messaging.sendEachForMulticast({
    tokens,
    notification: { title, body },
    data: { type },
  });

  response.responses.forEach((r, i) => {
    if (!r.success) console.warn(`FCM send failed for token ${tokens[i]}`, r.error);
  });
}

function formatMinor(amountMinor) {
  return `₹${(amountMinor / 100).toFixed(2)}`;
}

function onAdded(query, handler) {
  query.onSnapshot((snapshot) => {
    snapshot.docChanges().forEach((change) => {
      if (change.type !== "added") return;
      handler(change.doc).catch((err) => console.error(`listener handler failed for ${change.doc.ref.path}`, err));
    });
  }, (err) => console.error("snapshot listener error", err));
}

onAdded(db.collection("sharedExpenses"), async (doc) => {
  const data = doc.data();
  const memberUids = data.memberUids ?? [];
  const authorUid = data.lastEditedByUid;
  const recipients = memberUids.filter((uid) => uid !== authorUid);
  if (recipients.length === 0) return;

  await sendToUids(
    recipients,
    "New shared expense",
    `${data.description} — ${formatMinor(data.amountMinor ?? 0)}`,
    "shared_expense"
  );
});

onAdded(db.collection("settlements"), async (doc) => {
  const data = doc.data();
  const memberUids = data.memberUids ?? [];
  const authorUid = data.lastEditedByUid;
  const recipients = memberUids.filter((uid) => uid !== authorUid);
  if (recipients.length === 0) return;

  await sendToUids(recipients, "Settlement recorded", formatMinor(data.amountMinor ?? 0), "settlement");
});

onAdded(db.collection("settlementReminders"), async (doc) => {
  const { fromUid, toUid, amountMinor } = doc.data();

  const fromProfile = await db.collection("userDirectory").doc(fromUid).get();
  const fromName = fromProfile.data()?.displayName ?? "A friend";

  await sendToUids([toUid], "Settlement reminder", `${fromName} reminds you to settle up ${formatMinor(amountMinor)}`, "settlement_reminder");

  // One-shot notification — nothing else reads this doc, so clean it up.
  await doc.ref.delete();
});

const app = express();
app.get("/", (_req, res) => res.send("expense-tracker notification server running"));
app.listen(process.env.PORT ?? 3000, () => console.log("listening"));
