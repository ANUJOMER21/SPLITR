package com.omer.expensetracker.data.local

import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.split.FriendDao
import com.omer.expensetracker.data.local.entity.CategoryEntity
import com.omer.expensetracker.data.local.entity.split.FriendEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AppDatabase.SeedCallback] only fires on a genuinely fresh install. A dev-time schema bump
 * (`fallbackToDestructiveMigration`) drops and recreates every table but does **not** reliably
 * re-invoke that callback, which silently leaves categories and the implicit "You" friend
 * missing for anyone who upgrades without clearing app data — confirmed by comparing a migrated
 * install against a `pm clear` fresh one. This re-checks and repairs both on every app launch,
 * so the app self-heals regardless of which path created the database.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val categoryDao: CategoryDao,
    private val friendDao: FriendDao
) {
    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        if (categoryDao.count() == 0) {
            DEFAULT_CATEGORY_SEEDS.forEach { seed ->
                categoryDao.insert(
                    CategoryEntity(
                        id = seed.id, name = seed.name, iconKey = seed.iconKey, colorArgb = seed.colorArgb,
                        isDefault = true, isActive = true, createdAt = now, updatedAt = now
                    )
                )
            }
        }
        if (friendDao.getYou() == null) {
            friendDao.insert(
                FriendEntity(
                    id = "you", name = "You", email = "", contactInfo = null, avatarColorArgb = 0xFF3B82F6L,
                    isYou = true, linkedUserId = null, createdAt = now, updatedAt = now
                )
            )
        }
    }
}
