package com.example.team4.di

import android.content.Context
import androidx.room.Room
import com.example.team4.data.local.AppDatabase
import com.example.team4.data.local.FundDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fund_tracker_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFundDao(database: AppDatabase): FundDao = database.fundDao()
}
