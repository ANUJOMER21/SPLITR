package com.omer.expensetracker.di

import com.omer.expensetracker.domain.service.WidgetRefresher
import com.omer.expensetracker.widget.GlanceWidgetRefresher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    @Singleton
    abstract fun bindWidgetRefresher(impl: GlanceWidgetRefresher): WidgetRefresher
}
