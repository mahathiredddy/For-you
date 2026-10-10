package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BusinessDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.CheckInDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.HackathonDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.RoadmapDao
import com.example.data.local.dao.SkillDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.BusinessPlanEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HackathonEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.RoadmapPhaseEntity
import com.example.data.local.entity.SkillEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        GoalEntity::class,
        RoadmapPhaseEntity::class,
        MilestoneEntity::class,
        TaskEntity::class,
        DailyCheckInEntity::class,
        MemoryEntity::class,
        NoteEntity::class,
        SkillEntity::class,
        HackathonEntity::class,
        BusinessPlanEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
    abstract fun roadmapDao(): RoadmapDao
    abstract fun taskDao(): TaskDao
    abstract fun checkInDao(): CheckInDao
    abstract fun memoryDao(): MemoryDao
    abstract fun noteDao(): NoteDao
    abstract fun skillDao(): SkillDao
    abstract fun hackathonDao(): HackathonDao
    abstract fun businessDao(): BusinessDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "were_here_database"
                )
                    // Removed dropAllTables = true to protect user data from destructive drops
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
