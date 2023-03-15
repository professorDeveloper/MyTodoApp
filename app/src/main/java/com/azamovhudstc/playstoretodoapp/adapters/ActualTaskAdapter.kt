package com.azamovhudstc.playstoretodoapp.adapters

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.azamovhudstc.playstoretodoapp.AlarmReceiver
import com.azamovhudstc.playstoretodoapp.database.AppDatabase
import com.azamovhudstc.playstoretodoapp.database.dao.TaskDao
import com.azamovhudstc.playstoretodoapp.database.entity.Task
import com.azamovhudstc.playstoretodoapp.R
import com.azamovhudstc.playstoretodoapp.databinding.ItemActualTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class ActualTaskAdapter(
    private val notification_time: String,
    private val isByType: Boolean
) :
    RecyclerView.Adapter<ActualTaskAdapter.ATVH>() {

    private var list = arrayListOf<Task>()
    private var notiAnimated = false
    private lateinit var context:Context
    private lateinit var taskDao: TaskDao

    inner class ATVH(private val itemBinding: ItemActualTaskBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        init {
            context = itemBinding.root.context
            taskDao = AppDatabase.getDatabase(context).taskDao()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun onBind(task: Task, position: Int) {

            itemBinding.titleTv.text = task.name
            itemBinding.timeTv.text = task.time

            if (isByType) {
                setColors(task)
            } else {
                itemBinding.colorCard.setCardBackgroundColor(task.type.card_color)
                if (task.time == notification_time && !notiAnimated) {
                    val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
                    anim.duration = 1000
                    anim.repeatCount = 5
                    anim.repeatMode = Animation.REVERSE
                    itemBinding.root.startAnimation(anim)
                    notiAnimated = true
                }
            }
            setDrawables(task)

            itemBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked && !task.isCompleted) {
                    cancelAlarm(task)
                    task.isCompleted = true
                    taskDao.updateTask(task)
                    notifyDataSetChanged()
                }
            }
            checkTask(task)
            itemBinding.root.setOnLongClickListener {
                PopupMenu(context, it, Gravity.TOP).apply {
                    inflate(R.menu.task_edit_menu)
                    setOnMenuItemClickListener { item: MenuItem? ->
                        when (item!!.itemId) {
                            R.id.delete -> {
                                cancelAlarm(task)
                                list.remove(task)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, list.size - 1)
                                taskDao.deleteTask(task)
                            }
                        }
                        true
                    }
                    show()
                }

                true
            }
        }

        @SuppressLint("UnspecifiedImmutableFlag")
        private fun cancelAlarm(task: Task) {

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.taskId,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_MUTABLE
            )
            val alarmManager =
                (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)!!
            alarmManager.cancel(pendingIntent)
        }

        private fun checkTask(task: Task) {
            if (task.isCompleted) {
                val spannableTitle = SpannableString(itemBinding.titleTv.text)
                spannableTitle.setSpan(
                    StrikethroughSpan(),
                    0,
                    spannableTitle.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                itemBinding.titleTv.text = spannableTitle
                itemBinding.checkbox.isChecked = true
                itemBinding.checkbox.isEnabled = false
                if (isByType) {
                    itemBinding.titleTv.setTextColor(task.type.complated_color)
                    itemBinding.timeTv.setTextColor(task.type.complated_color)
                    itemBinding.dateTv.setTextColor(task.type.complated_color)
                }
            } else {
                itemBinding.checkbox.isChecked = false
                itemBinding.checkbox.isEnabled = true
            }
        }

        private fun setDrawables(task: Task) {
            if (task.time.isNotEmpty()) {
                val drawableTime =
                    ContextCompat.getDrawable(context, R.drawable.ic_alarm_for_textview)!!
                drawableTime.colorFilter = if (isByType)
                    PorterDuffColorFilter(task.type.count_color, PorterDuff.Mode.SRC_IN)
                else PorterDuffColorFilter(Color.parseColor("#80000000"), PorterDuff.Mode.SRC_IN)
                itemBinding.timeTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawableTime,
                    null,
                    null,
                    null
                )
            }
            if (task.date.isNotEmpty() && isByType) {
                val drawable =
                    ContextCompat.getDrawable(context, R.drawable.ic_calendar_for_textview)!!
                drawable.colorFilter =
                    PorterDuffColorFilter(task.type.count_color, PorterDuff.Mode.SRC_IN)
                itemBinding.dateTv.setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
                val date = checkDate(task.date)
                itemBinding.dateTv.text = date
            }
        }

        private fun setColors(task: Task) {
            itemBinding.apply {
                if (isByType) {
                    titleTv.setTextColor(task.type.name_color)
                    dateTv.setTextColor(task.type.complated_color)
                    timeTv.setTextColor(task.type.complated_color)
                    dividerView.setBackgroundColor(task.type.complated_color)
                } else {
                    titleTv.setTextColor(task.type.name_color)
                    dateTv.setTextColor(task.type.complated_color)
                    timeTv.setTextColor(task.type.complated_color)
                    dividerView.setBackgroundColor(task.type.complated_color)
                }
            }
        }
    }

    private fun checkDate(date: String): String {

        val checkedDate:String
        val todayCalendar = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        calendar.time = dateFormat.parse(date)

        if (date == dateFormat.format(todayCalendar.time)) {
            checkedDate = "Today"
        } else if (calendar.get(Calendar.DATE) - todayCalendar.get(Calendar.DATE) == 1) {
            checkedDate = "Tomorrow"
        } else checkedDate = date

        return checkedDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ATVH {
        return ATVH(
            ItemActualTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ATVH, position: Int) {
        holder.onBind(list[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTasks(list: List<Task>) {
        this.list = list as ArrayList<Task>
        notifyDataSetChanged()
    }

    fun disableAnimation(b: Boolean) {
        notiAnimated = b
    }

    override fun getItemCount(): Int = list.size

}