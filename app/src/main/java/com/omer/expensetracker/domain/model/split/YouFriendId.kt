package com.omer.expensetracker.domain.model.split

/** The stable id of the implicit "You" friend record, seeded once when the database is first
 * created (see [com.omer.expensetracker.data.local.AppDatabase.SeedCallback]). Every balance
 * row is this app-owner's net position against one other [Friend] — "You" never has a balance
 * against itself, so it's always excluded when a shared expense's or settlement's deltas are
 * applied. */
const val YOU_FRIEND_ID = "you"
