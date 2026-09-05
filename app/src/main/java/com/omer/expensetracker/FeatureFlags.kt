package com.omer.expensetracker

/**
 * Compile-time gates for features that are fully built but not yet ready to ship.
 *
 * Split With Friends (shared expenses, groups, balances, settlements) is Phase 3 of the
 * roadmap: the data layer, domain logic, and screens all exist and work, but the feature
 * stays hidden — no nav entry point anywhere in the app — until this flag flips to `true`.
 * Flip it locally to develop/QA the feature; leave it `false` for every real build until
 * backend sync design lands and it's ready for users.
 */
object FeatureFlags {
    const val SPLIT_WITH_FRIENDS_ENABLED = true

    /**
     * Phase 5 — Firebase backend (auth + Firestore, cross-device balances).
     * Backed by the real `expense-a2ed0` Firebase project (`google-services.json` +
     * `google-services` Gradle plugin applied). Auth (email/password + Google Sign-In) is live;
     * the cross-device sync outbox still waits on Phase 5's backend half.
     */
    const val CLOUD_SYNC_ENABLED = true

    /**
     * Dev scaffolding — shows a "Load sample split data" row under More ▸ Split with friends
     * that seeds friends, groups and shared expenses for QA of the settle-up flows. Keep `false`
     * for real builds.
     */
    const val SPLIT_SAMPLE_DATA_SEED = false
}
