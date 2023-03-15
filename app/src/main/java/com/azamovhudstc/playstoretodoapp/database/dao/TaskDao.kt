package com.azamovhudstc.playstoretodoapp.database.dao

import androidx.room.*
import androidx.room.Query
import com.azamovhudstc.playstoretodoapp.database.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM task_table")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task_table where type_name = :type")
    suspend fun getTasksByType(type: String): List<Task>

    @Query("SELECT * FROM task_table where date = :date")
    fun getTasksByDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM task_table where date = :date || time = :time")
    fun getTasksByDateAndTime(date: String, time: String): Flow<List<Task>>

    @Query("select count(type_name) from task_table where type_name = :type")
    fun getTaskCountByType(type: String): Flow<Int>

    @Update
    fun updateTask(task: Task)

    @Delete
    fun deleteTask(task: Task)

    @Query("SELECT * FROM task_table ORDER BY taskId DESC LIMIT 1")
    suspend fun getLastTask(): Task?

}