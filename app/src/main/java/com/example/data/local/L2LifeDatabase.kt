package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DiaryDao
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.LearningDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.PersonDao
import com.example.data.local.dao.PreferenceDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.RoutineDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.DiaryEntryEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.LearningSessionEntity
import com.example.data.local.entity.LearningTopicEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.PreferenceEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineStepEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity

import com.example.data.local.dao.FocusDao
import com.example.data.local.dao.WeeklyReviewDao
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.local.entity.WeeklyReviewEntity

@Database(
    entities = [
        TaskEntity::class,
        ProjectEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        NoteEntity::class,
        WellnessLogEntity::class,
        PreferenceEntity::class,
        RoutineEntity::class,
        RoutineStepEntity::class,
        CalendarEventEntity::class,
        DiaryEntryEntity::class,
        GoalEntity::class,
        GoalMilestoneEntity::class,
        LearningTopicEntity::class,
        LearningSessionEntity::class,
        PersonEntity::class,
        FocusSessionEntity::class,
        WeeklyReviewEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class L2LifeDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun wellnessDao(): WellnessDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun routineDao(): RoutineDao
    abstract fun eventDao(): EventDao
    abstract fun diaryDao(): DiaryDao
    abstract fun goalDao(): GoalDao
    abstract fun learningDao(): LearningDao
    abstract fun personDao(): PersonDao
    abstract fun focusDao(): FocusDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao

    companion object {
        @Volatile
        private var INSTANCE: L2LifeDatabase? = null

        fun getInstance(context: Context): L2LifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    L2LifeDatabase::class.java,
                    "l2_life_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
