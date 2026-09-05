package com.omer.expensetracker.domain.service

/** Home-screen widgets render a static snapshot, not a live query — whoever mutates entry
 * data must explicitly ask widgets to redraw. Kept as a domain port so the data layer
 * doesn't depend on Android widget APIs directly. */
interface WidgetRefresher {
    suspend fun refreshAll()
}
