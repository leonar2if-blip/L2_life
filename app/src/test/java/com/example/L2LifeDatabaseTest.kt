package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.L2LifeDatabase
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.PreferenceDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.RoutineDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entity.PreferenceEntity
import com.example.data.repository.CalendarRepositoryImpl
import com.example.data.repository.HabitRepositoryImpl
import com.example.data.repository.NoteRepositoryImpl
import com.example.data.repository.ProjectRepositoryImpl
import com.example.data.repository.RoutineRepositoryImpl
import com.example.data.repository.TaskRepositoryImpl
import com.example.data.repository.WellnessRepositoryImpl
import com.example.domain.model.Priority
import com.example.domain.model.TaskStatus
import com.example.domain.model.TimeOfDaySlot
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class L2LifeDatabaseTest {

    private lateinit var db: L2LifeDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var projectDao: ProjectDao
    private lateinit var habitDao: HabitDao
    private lateinit var routineDao: RoutineDao
    private lateinit var eventDao: EventDao
    private lateinit var noteDao: NoteDao
    private lateinit var wellnessDao: WellnessDao
    private lateinit var preferenceDao: PreferenceDao
    private lateinit var diaryDao: com.example.data.local.dao.DiaryDao
    private lateinit var goalDao: com.example.data.local.dao.GoalDao
    private lateinit var learningDao: com.example.data.local.dao.LearningDao
    private lateinit var personDao: com.example.data.local.dao.PersonDao
    private lateinit var focusDao: com.example.data.local.dao.FocusDao
    private lateinit var weeklyReviewDao: com.example.data.local.dao.WeeklyReviewDao

    private lateinit var taskRepository: TaskRepositoryImpl
    private lateinit var projectRepository: ProjectRepositoryImpl
    private lateinit var habitRepository: HabitRepositoryImpl
    private lateinit var routineRepository: RoutineRepositoryImpl
    private lateinit var calendarRepository: CalendarRepositoryImpl
    private lateinit var noteRepository: NoteRepositoryImpl
    private lateinit var wellnessRepository: WellnessRepositoryImpl
    private lateinit var diaryRepository: com.example.data.repository.DiaryRepositoryImpl
    private lateinit var goalRepository: com.example.data.repository.GoalRepositoryImpl
    private lateinit var learningRepository: com.example.data.repository.LearningRepositoryImpl
    private lateinit var personRepository: com.example.data.repository.PersonRepositoryImpl
    private lateinit var focusRepository: com.example.data.repository.FocusRepositoryImpl
    private lateinit var statsRepository: com.example.data.repository.StatsRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, L2LifeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = db.taskDao()
        projectDao = db.projectDao()
        habitDao = db.habitDao()
        routineDao = db.routineDao()
        eventDao = db.eventDao()
        noteDao = db.noteDao()
        wellnessDao = db.wellnessDao()
        preferenceDao = db.preferenceDao()
        diaryDao = db.diaryDao()
        goalDao = db.goalDao()
        learningDao = db.learningDao()
        personDao = db.personDao()
        focusDao = db.focusDao()
        weeklyReviewDao = db.weeklyReviewDao()

        taskRepository = TaskRepositoryImpl(taskDao)
        projectRepository = ProjectRepositoryImpl(projectDao, taskDao)
        habitRepository = HabitRepositoryImpl(habitDao)
        routineRepository = RoutineRepositoryImpl(routineDao)
        calendarRepository = CalendarRepositoryImpl(eventDao, taskDao)
        noteRepository = NoteRepositoryImpl(noteDao)
        wellnessRepository = WellnessRepositoryImpl(wellnessDao)
        diaryRepository = com.example.data.repository.DiaryRepositoryImpl(
            diaryDao = diaryDao,
            taskDao = taskDao,
            habitDao = habitDao,
            wellnessDao = wellnessDao
        )
        goalRepository = com.example.data.repository.GoalRepositoryImpl(goalDao)
        learningRepository = com.example.data.repository.LearningRepositoryImpl(learningDao)
        personRepository = com.example.data.repository.PersonRepositoryImpl(personDao)
        focusRepository = com.example.data.repository.FocusRepositoryImpl(
            focusDao = focusDao,
            taskDao = taskDao,
            learningDao = learningDao
        )
        statsRepository = com.example.data.repository.StatsRepositoryImpl(
            taskDao = taskDao,
            habitDao = habitDao,
            wellnessDao = wellnessDao,
            learningDao = learningDao,
            goalDao = goalDao,
            projectDao = projectDao,
            focusDao = focusDao,
            weeklyReviewDao = weeklyReviewDao
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testTaskLifecyclePersistence() = runTest {
        val task = taskRepository.createTask(
            title = "Configurar base de datos L2 Life",
            description = "Verificar Room y persistencia local",
            priority = Priority.URGENT,
            category = "Trabajo"
        )
        assertNotNull(task.id)

        val pendingList = taskRepository.observePendingTasks().first()
        assertEquals(1, pendingList.size)
        assertEquals("Configurar base de datos L2 Life", pendingList[0].title)
        assertEquals(Priority.URGENT, pendingList[0].priority)

        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)
        val updated = taskRepository.getTaskById(task.id)
        assertNotNull(updated)
        assertTrue(updated!!.isCompleted)

        taskRepository.deleteTask(task.id)
        val deleted = taskRepository.getTaskById(task.id)
        assertNull(deleted)
    }

    @Test
    fun testProjectCreationAndProgressCalculation() = runTest {
        val project = projectRepository.createProject(
            name = "Lanzamiento L2 Life",
            description = "Hitos y tareas del producto",
            colorHex = "#2563EB",
            category = "Trabajo"
        )
        assertNotNull(project.id)

        // Add 2 tasks to project
        val task1 = taskRepository.createTask(
            title = "Diseñar UI",
            priority = Priority.HIGH,
            projectId = project.id,
            category = "Proyecto"
        )
        val task2 = taskRepository.createTask(
            title = "Compilar y probar",
            priority = Priority.URGENT,
            projectId = project.id,
            category = "Proyecto"
        )

        // Verify initial progress is 0%
        val detailInitial = projectRepository.observeProjectDetail(project.id).first()
        assertNotNull(detailInitial)
        assertEquals(2, detailInitial!!.totalTasks)
        assertEquals(0, detailInitial.completedTasks)
        assertEquals(0f, detailInitial.progress, 0.01f)

        // Complete task 1 -> progress becomes 50% (0.5f)
        taskRepository.updateTaskStatus(task1.id, TaskStatus.COMPLETED)
        val detailAfter1 = projectRepository.observeProjectDetail(project.id).first()
        assertNotNull(detailAfter1)
        assertEquals(1, detailAfter1!!.completedTasks)
        assertEquals(0.5f, detailAfter1.progress, 0.01f)

        // Complete task 2 -> progress becomes 100% (1.0f)
        taskRepository.updateTaskStatus(task2.id, TaskStatus.COMPLETED)
        val detailAfter2 = projectRepository.observeProjectDetail(project.id).first()
        assertNotNull(detailAfter2)
        assertEquals(2, detailAfter2!!.completedTasks)
        assertEquals(1.0f, detailAfter2.progress, 0.01f)
    }

    @Test
    fun testRoutineLifecycleAndStepSkipping() = runTest {
        val routine = routineRepository.createRoutine(
            title = "Rutina Mañanera",
            description = "Enfoque matutino",
            timeOfDaySlot = TimeOfDaySlot.MORNING,
            steps = listOf(
                "Hidratarse (500ml)" to 2,
                "Meditación" to 10,
                "Revisar prioridades" to 5
            )
        )
        assertNotNull(routine.id)

        val routineWithSteps = routineRepository.observeRoutine(routine.id).first()
        assertNotNull(routineWithSteps)
        assertEquals(3, routineWithSteps!!.steps.size)
        assertEquals(17, routineWithSteps.totalDurationMinutes)

        // Complete step 1
        val step1 = routineWithSteps.steps[0]
        routineRepository.updateStepStatus(step1.id, isCompleted = true, isSkipped = false)

        // Skip step 2
        val step2 = routineWithSteps.steps[1]
        routineRepository.updateStepStatus(step2.id, isCompleted = false, isSkipped = true)

        val updated = routineRepository.observeRoutine(routine.id).first()
        assertTrue(updated!!.steps[0].isCompleted)
        assertTrue(updated.steps[1].isSkipped)

        // Reset routine
        routineRepository.resetRoutine(routine.id)
        val reset = routineRepository.observeRoutine(routine.id).first()
        assertFalse(reset!!.steps[0].isCompleted)
        assertFalse(reset.steps[1].isSkipped)
    }

    @Test
    fun testCalendarEventAndScheduleMerging() = runTest {
        val now = System.currentTimeMillis()
        val startOfDay = now - 3600000L
        val endOfDay = now + 3600000L

        // Create an event
        calendarRepository.createEvent(
            title = "Reunión de Sincronización",
            description = "Alineación semanal",
            startTime = now,
            endTime = now + 1800000L,
            category = "Trabajo"
        )

        // Create a scheduled task with scheduledDate
        taskRepository.createTask(
            title = "Bloque de código profundo",
            priority = Priority.HIGH,
            scheduledDate = now + 900000L,
            category = "Trabajo"
        )

        val schedule = calendarRepository.observeScheduleForRange(startOfDay, endOfDay).first()
        assertEquals(2, schedule.size)
        assertTrue(schedule.any { it.title == "Reunión de Sincronización" && !it.isTaskDeadline })
        assertTrue(schedule.any { it.title == "Bloque de código profundo" })
    }

    @Test
    fun testHabitAndLogPersistence() = runTest {
        val habit = habitRepository.createHabit(
            title = "Beber agua",
            targetValue = 8.0,
            unit = "vasos",
            isPositive = true
        )

        val activeHabits = habitRepository.observeActiveHabits().first()
        assertEquals(1, activeHabits.size)
        assertEquals("Beber agua", activeHabits[0].title)

        habitRepository.toggleHabitToday(habit.id, true)
        val logsToday = habitRepository.observeTodayLogs().first()
        assertEquals(1, logsToday.size)
        assertTrue(logsToday[0].completed)

        habitRepository.toggleHabitToday(habit.id, false)
        val logsAfterUndo = habitRepository.observeTodayLogs().first()
        assertEquals(0, logsAfterUndo.size)
    }

    @Test
    fun testNoteCreationAndSearch() = runTest {
        noteRepository.createNote(
            title = "Arquitectura Limpia",
            content = "L2 Life utiliza separación de responsabilidades y modelos desacoplados.",
            category = "Ideas"
        )

        val searchResults = noteRepository.searchNotes("Arquitectura").first()
        assertEquals(1, searchResults.size)
        assertEquals("Arquitectura Limpia", searchResults[0].title)
    }

    @Test
    fun testWaterIntakeTracking() = runTest {
        wellnessRepository.logWater(250)
        wellnessRepository.logWater(500)

        val totalWater = wellnessRepository.observeTodayWaterMl().first()
        assertEquals(750, totalWater)

        wellnessRepository.undoLatestWater()
        val afterUndo = wellnessRepository.observeTodayWaterMl().first()
        assertEquals(250, afterUndo)
    }

    @Test
    fun testUserPreferences() = runTest {
        preferenceDao.setPreference(PreferenceEntity("theme_mode", "DARK"))
        val pref = preferenceDao.getPreference("theme_mode")
        assertNotNull(pref)
        assertEquals("DARK", pref!!.value)
    }

    @Test
    fun testDiaryDayDraftGenerationAndSave() = runTest {
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()

        // Create and complete a task
        val task = taskRepository.createTask(
            title = "Escribir pruebas de Etapa 3",
            priority = Priority.URGENT,
            category = "Desarrollo"
        )
        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)

        // Log water intake
        wellnessRepository.logWater(500)

        // Generate draft based on actual Room data
        val draft = diaryRepository.generateDayDraft(todayEpochDay)
        assertTrue(draft.contains("Escribir pruebas de Etapa 3"))
        assertTrue(draft.contains("500 ml"))

        // Save diary entry
        val entry = diaryRepository.saveDiaryEntry(
            dateEpochDay = todayEpochDay,
            title = "Reflexión del día",
            content = "Día productivo completando objetivos y pruebas.",
            derivedSummary = draft,
            mood = "Productivo"
        )
        assertNotNull(entry.id)

        val retrieved = diaryRepository.observeDiaryForDate(todayEpochDay).first()
        assertNotNull(retrieved)
        assertEquals("Reflexión del día", retrieved!!.title)
        assertEquals("Productivo", retrieved.mood)
        assertTrue(retrieved.derivedSummary.contains("500 ml"))
    }

    @Test
    fun testGoalMilestonesAndProgressCalculation() = runTest {
        val goal = goalRepository.createGoal(
            title = "Aprender Arquitectura Android",
            description = "Módulo completo de Clean Architecture y Room",
            timeHorizon = com.example.domain.model.GoalHorizon.SHORT_TERM,
            category = "Educación"
        )
        assertNotNull(goal.id)

        // Add 2 milestones
        goalRepository.addMilestone(goal.id, "Entender entidades y DAOs")
        goalRepository.addMilestone(goal.id, "Implementar repositorios reactivos")

        val initialDetails = goalRepository.observeGoalDetail(goal.id).first()
        assertNotNull(initialDetails)
        assertEquals(2, initialDetails!!.milestones.size)
        assertEquals(0f, initialDetails.progress, 0.01f)

        // Achieve 1 milestone -> 50%
        val milestone1 = initialDetails.milestones[0]
        goalRepository.toggleMilestone(milestone1.id, isAchieved = true)

        val afterAchieved1 = goalRepository.observeGoalDetail(goal.id).first()
        assertNotNull(afterAchieved1)
        assertEquals(0.5f, afterAchieved1!!.progress, 0.01f)
        assertTrue(afterAchieved1.milestones.first { it.id == milestone1.id }.isAchieved)

        // Achieve 2nd milestone -> 100%
        val milestone2 = initialDetails.milestones[1]
        goalRepository.toggleMilestone(milestone2.id, isAchieved = true)

        val afterAchieved2 = goalRepository.observeGoalDetail(goal.id).first()
        assertNotNull(afterAchieved2)
        assertEquals(1.0f, afterAchieved2!!.progress, 0.01f)
    }

    @Test
    fun testLearningTopicsAndStudyHoursAccumulation() = runTest {
        val topic = learningRepository.createTopic(
            title = "Jetpack Compose Avanzado",
            description = "Layouts y animaciones complejas",
            category = "Tecnología"
        )
        assertNotNull(topic.id)

        // Log sessions of 30m and 45m
        learningRepository.logSession(topic.id, 30, "Estudio de SubcomposeLayout")
        learningRepository.logSession(topic.id, 45, "Práctica de animación de transición")

        val topics = learningRepository.observeTopics().first()
        val updatedTopic = topics.first { it.id == topic.id }
        assertEquals(75, updatedTopic.totalStudyMinutes)
        assertEquals(1.25f, updatedTopic.totalHours, 0.01f)

        val sessions = learningRepository.observeSessionsForTopic(topic.id).first()
        assertEquals(2, sessions.size)
    }

    @Test
    fun testPersonContactManagement() = runTest {
        val person = personRepository.createPerson(
            fullName = "Elena Gomez",
            alias = "Mentora",
            category = com.example.domain.model.PersonCategory.WORK,
            importantDatesNotes = "Cumpleaños 5 de Junio",
            notes = "Reunión quincenal de seguimiento",
            nextAction = "Revisar arquitectura de la aplicación"
        )
        assertNotNull(person.id)

        val people = personRepository.observePeople().first()
        assertEquals(1, people.size)
        assertEquals("Elena Gomez", people[0].fullName)
        assertEquals("Revisar arquitectura de la aplicación", people[0].nextAction)
        assertEquals(com.example.domain.model.PersonCategory.WORK, people[0].category)

        // Update person
        personRepository.updatePerson(person.copy(nextAction = "Presentar avances de Etapa 3"))
        val updatedPeople = personRepository.observePeople().first()
        assertEquals("Presentar avances de Etapa 3", updatedPeople[0].nextAction)
    }

    @Test
    fun testFocusSessionRecordingAndTaskAutoComplete() = runTest {
        // Create a pending task
        val task = taskRepository.createTask(
            title = "Escribir tests de integración Room",
            description = "Validar operaciones locales",
            priority = Priority.HIGH,
            category = "Trabajo"
        )
        assertNotNull(task.id)
        assertEquals(TaskStatus.PENDING, task.status)

        // Record a 25-minute focus session linked to the task with auto-complete
        val session = focusRepository.recordFocusSession(
            mode = com.example.domain.model.FocusMode.WORK,
            durationMinutes = 25,
            title = task.title,
            linkedTaskId = task.id,
            linkedTopicId = null,
            notes = "Bloque de código enfocado completado con éxito",
            markTaskCompleted = true
        )

        assertNotNull(session.id)
        assertEquals(25, session.durationMinutes)
        assertEquals(com.example.domain.model.FocusMode.WORK, session.mode)

        // Task should now be marked as COMPLETED
        val pendingTasks = taskRepository.observePendingTasks().first()
        assertFalse(pendingTasks.any { it.id == task.id })

        val allSessions = focusRepository.observeAllSessions().first()
        assertEquals(1, allSessions.size)
        assertEquals("Bloque de código enfocado completado con éxito", allSessions[0].notes)

        val todayMinutes = focusRepository.observeTodayFocusMinutes().first()
        assertEquals(25, todayMinutes)
    }

    @Test
    fun testFocusSessionAutoLogsToLearningTopic() = runTest {
        val topic = learningRepository.createTopic(
            title = "Estructuras de Datos y Algoritmos",
            description = "Árboles y grafos",
            category = "Ciencia"
        )

        // Complete 45-minute focus session linked to this topic
        focusRepository.recordFocusSession(
            mode = com.example.domain.model.FocusMode.WORK,
            durationMinutes = 45,
            title = "Estudio de árboles binarios",
            linkedTaskId = null,
            linkedTopicId = topic.id,
            notes = "Repaso de AVL y árboles rojo-negro",
            markTaskCompleted = false
        )

        // The learning topic should have 45 study minutes automatically logged
        val topics = learningRepository.observeTopics().first()
        val updatedTopic = topics.first { it.id == topic.id }
        assertEquals(45, updatedTopic.totalStudyMinutes)

        val topicSessions = learningRepository.observeSessionsForTopic(topic.id).first()
        assertEquals(1, topicSessions.size)
    }

    @Test
    fun testWeeklyStatsAggregationAndReviewGeneration() = runTest {
        val todayEpochDay = System.currentTimeMillis() / 86400000L
        val weekStartEpochDay = todayEpochDay - 3
        val weekEndEpochDay = todayEpochDay + 3

        // Create and complete task
        val task = taskRepository.createTask("Tarea Semanal", priority = Priority.MEDIUM)
        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)

        // Habit and log
        val habit = habitRepository.createHabit("Lectura Diaria")
        habitRepository.toggleHabitToday(habit.id, true)

        // Water log
        wellnessRepository.logWater(750)

        // Focus session
        focusRepository.recordFocusSession(
            mode = com.example.domain.model.FocusMode.WORK,
            durationMinutes = 30,
            title = "Enfoque Semanal",
            linkedTaskId = null,
            linkedTopicId = null,
            notes = "Sesión de métrica",
            markTaskCompleted = false
        )

        // Observe weekly stats
        val stats = statsRepository.observeWeeklyStats(weekStartEpochDay, weekEndEpochDay).first()
        assertEquals(1, stats.totalTasksCompleted)
        assertEquals(1, stats.totalHabitsLogged)
        assertEquals(30, stats.totalFocusMinutes)
        assertEquals(7, stats.dailyPoints.size)

        // Generate review draft
        val draft = statsRepository.generateReviewDraft(weekStartEpochDay, weekEndEpochDay)
        assertTrue(draft.contains("Revisión Semanal"))
        assertTrue(draft.contains("Tareas completadas: 1"))
        assertTrue(draft.contains("Hábitos cumplidos: 1"))

        // Save weekly review
        val review = com.example.domain.model.WeeklyReviewItem(
            id = "test-review-1",
            weekStartEpochDay = weekStartEpochDay,
            weekEndEpochDay = weekEndEpochDay,
            accomplishments = "Semana muy productiva y constante",
            challenges = "Mejorar la puntualidad en descansos",
            nextWeekFocus = "Completar la Etapa 5",
            notes = "Buenas sensaciones",
            energyRating = 4,
            productivityRating = 5
        )
        statsRepository.saveReview(review)

        val saved = statsRepository.observeReviewForWeek(weekStartEpochDay).first()
        assertNotNull(saved)
        assertEquals(4, saved!!.energyRating)
        assertEquals(5, saved.productivityRating)
        assertEquals("Semana muy productiva y constante", saved.accomplishments)

        val allReviews = statsRepository.observeAllReviews().first()
        assertEquals(1, allReviews.size)
    }
}
