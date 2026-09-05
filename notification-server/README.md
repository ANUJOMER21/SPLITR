# Notification server (free-tier replacement for Cloud Functions)

Firebase Cloud Functions need the paid Blaze plan for Firestore triggers. This is the free
substitute: a plain Node process holding Firestore `onSnapshot` listeners (ordinary Admin SDK
reads — free on Spark) that calls FCM send directly (also free) instead of a deployed trigger.

## Deploy (Render free web service)

1. Firebase console → Project settings → Service accounts → **Generate new private key**.
   Downloads a JSON file — never commit it.
2. `base64 -i service-account.json | pbcopy` (macOS) to copy it as one line.
3. On [render.com](https://render.com): New → Web Service → point at this repo, root
   directory `notification-server` (or New → Blueprint using the included `render.yaml`).
4. Set env var `FIREBASE_SERVICE_ACCOUNT_B64` to the base64 string from step 2.
5. Deploy. Logs should show `listening`.

## Keep it awake

Render's free tier sleeps a service after 15 minutes with no HTTP traffic, which kills this
process and its listeners along with it. Add a free [UptimeRobot](https://uptimerobot.com)
(or similar) monitor hitting `https://<your-service>.onrender.com/` every 5–10 minutes so
pushes stay near-real-time. Without it, notifications only resume after the next visit wakes
the service back up.
