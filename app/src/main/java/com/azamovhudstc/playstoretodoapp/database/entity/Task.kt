package com.azamovhudstc.playstoretodoapp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azamovhudstc.playstoretodoapp.utils.Types

@Entity(tableName = "task_table")
class Task(
    @ColumnInfo(name = "name")
    val name:String,
    @Embedded
    val type: Types,
    val date:String,
    val time:String,
    var isCompleted:Boolean = false,
){
    @PrimaryKey(autoGenerate = true)
    var taskId:Int = 0
}