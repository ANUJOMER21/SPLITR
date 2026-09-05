package com.omer.expensetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.omer.expensetracker.data.local.DatabaseSeeder
import com.omer.expensetracker.data.repository.sync.FirestoreSyncCoordinator
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.SettingsRepository
import com.omer.expensetracker.domain.repository.sync.AuthRepository
import com.omer.expensetracker.domain.repository.sync.CloudSyncRepository
import com.omer.expensetracker.work.SyncScheduler
import com.omer.expensetracker.work.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class ExpenseTrackerApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workScheduler: WorkScheduler
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var cloudSyncRepository: CloudSyncRepository
    @Inject lateinit var syncCoordinator: FirestoreSyncCoordinator
    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var entryRepository: EntryRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        workScheduler.scheduleDaily()
        workScheduler.runCatchUpNow()

        syncScheduler.schedulePeriodic()
        appScope.launch {
            databaseSeeder.ensureSeeded()
            // Onboarding is a genuine first-run-after-install thing only: if the pref was never
            // written but there's already data (an upgrade over an existing install), mark it
            // done so we don't show the intro to an existing user.
            if (settingsRepository.onboardingCompleteRaw.first() == null &&
                entryRepository.observeFilteredEntries(EntryFilter()).first().isNotEmpty()
            ) {
                settingsRepository.setOnboardingComplete(true)
            }
            authRepository.observeCurrentUser().collectLatest { user ->
                if (user != null) {
                    syncCoordinator.start(user.uid)
                    cloudSyncRepository.claimLocalData()
                    syncScheduler.triggerPushSoon()
                } else {
                    syncCoordinator.stop()
                }
            }
        }
    }
}
