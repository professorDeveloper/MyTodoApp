package com.azamovhudstc.playstoretodoapp.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.azamovhudstc.playstoretodoapp.adapters.TaskTypeAdapter
import com.azamovhudstc.playstoretodoapp.database.AppDatabase
import com.azamovhudstc.playstoretodoapp.database.entity.Task
import com.azamovhudstc.playstoretodoapp.utils.CircleDrawable
import com.azamovhudstc.playstoretodoapp.utils.TaskTypes
import com.azamovhudstc.playstoretodoapp.utils.Types
import com.applandeo.materialcalendarview.EventDay
import com.azamovhudstc.playstoretodoapp.AlarmReceiver
import com.azamovhudstc.playstoretodoapp.AlarmReceiver.Companion.NOTIFICATION
import com.azamovhudstc.playstoretodoapp.AlarmReceiver.Companion.NOTIFICATION_CHANNEL_ID
import com.azamovhudstc.playstoretodoapp.AlarmReceiver.Companion.NOTIFICATION_ID
import com.azamovhudstc.playstoretodoapp.AlarmReceiver.Companion.default_notification_channel_id
import com.azamovhudstc.playstoretodoapp.MainActivity
import com.azamovhudstc.playstoretodoapp.R
import com.azamovhudstc.playstoretodoapp.databinding.FragmentAddTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var typeAdapter: TaskTypeAdapter
    private lateinit var database: AppDatabase
    private lateinit var calendar: Calendar
    private var taskType: Types? = null
    private var date: String = ""
    private var time: String = ""
    private var alertID = 0


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(layoutInflater, container, false)

        init()
        loadDates()
        setUpUI()

        return binding.root
    }

    private fun loadDates() {
        lifecycleScope.launch(Dispatchers.Main) {
            alertID = if (database.taskDao().getLastTask() != null)
                (database.taskDao().getLastTask()!!.taskId) + 1
            else 0
            database.taskDao().getTasks().onEach {
                setEvents(it)
            }.launchIn(this)
        }
    }


    private fun init() {
        calendar = Calendar.getInstance()
        taskType = TaskTypes.getTypes(requireContext())[0]
        database = AppDatabase.getDatabase(requireContext())
    }

    private fun setUpUI() {
        setOnClickListener()
        setUpEditText()
        setUpTypeRV()
        setUpParametersUI()

        setUpDateParam()
        setUpTimeParam()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun scheduleNotification(notification: Notification) {
        val notificationIntent = Intent(binding.root.context, AlarmReceiver::class.java)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("noti", binding.timeTv.text.toString())
        notificationIntent.putExtra("noti", binding.timeTv.text.toString())
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            alertID,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
        notification.contentIntent = resultPendingIntent
        notificationIntent.putExtra(NOTIFICATION_ID, 1)
        notificationIntent.putExtra(NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alertID,
            notificationIntent,
            PendingIntent.FLAG_MUTABLE
        )
        val alarmManager =
            (requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager?)!!

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

    }

    private fun getNotification(content: String): Notification {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(binding.root.context, default_notification_channel_id)
        builder.setContentTitle(binding.taskTitleEt.text.toString())
        builder.setContentText(content)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder.setAutoCancel(true)
        builder.setChannelId(NOTIFICATION_CHANNEL_ID)
        return builder.build()
    }

    @SuppressLint("NewApi")
    private fun setOnClickListener() {
        binding.apply {
            cancelTv.setOnClickListener {
                (activity as MainActivity).onBackPressed()
            }

            doneTv.setOnClickListener {
                if (binding.taskTitleEt.text.toString().isNotEmpty()) {
                    val task = Task(
                        name = binding.taskTitleEt.text.toString(),
                        type = taskType!!,
                        date = date,
                        time = time
                    )
                    if (checkDateAndTime()) {
                        scheduleNotification(getNotification(taskType!!.type_name))
                    }
                    insertTask(task)
                    (activity as MainActivity).onBackPressed()
                } else {
                    binding.taskTitleEt.error = "Write down what you want to do!"
                }
            }
        }
    }

    private fun checkDateAndTime(): Boolean {
        if (date.isEmpty() || time.isEmpty()) return false
        else {
            val mCalendar = Calendar.getInstance()
            return mCalendar.timeInMillis < calendar.timeInMillis
        }
    }


    private fun setUpEditText() {
        binding.taskTitleEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(q: CharSequence, s: Int, c: Int, a: Int) {}
            override fun onTextChanged(q: CharSequence, s: Int, b: Int, c: Int) {
                val l = binding.taskTitleEt.lineCount
                if (l > 7) {
                    binding.taskTitleEt.text.delete(
                        binding.taskTitleEt.selectionEnd - 1,
                        binding.taskTitleEt.selectionStart
                    )
                }
            }
        })

    }

    @SuppressLint("SetTextI18n")
    private fun setUpTimeParam() {
        binding.apply {
            timePicker.setIndicatorText("", "")
            timePicker.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            timePicker.setOnTimeSelectedListener { hour, minute -> setTime(hour, minute) }
        }
    }

    private fun setTime(p1: Int, p2: Int) {
        val hour = if (p1.toString().length < 2) "0$p1" else p1
        val minute = if (p2.toString().length < 2) "0$p2" else p2
        time = "${hour}:${minute}"
        binding.timeTv.text = time
        val drawable = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_alarm_for_textview)
        drawable!!.setTint(Color.parseColor("#80000000"))
        binding.timeTv.setCompoundDrawablesWithIntrinsicBounds(
            drawable,
            null,
            null,
            null
        )
        calendar.set(Calendar.HOUR_OF_DAY, p1)
        calendar.set(Calendar.MINUTE, p2)
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDateParam() {
        binding.apply {
            val todayCalendar = Calendar.getInstance()
            val calendarMin = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            calendarMin.set(Calendar.DAY_OF_MONTH, calendarMin.get(Calendar.DAY_OF_MONTH) - 1)
            calendarView.setMinimumDate(calendarMin)
            calendarView.setOnDayClickListener { eventDay ->
                if (eventDay?.isEnabled!!) {
                    if (dateFormat.format(eventDay.calendar?.time!!) == dateFormat.format(
                            todayCalendar.time
                        )
                    ) {
                        binding.dateTv.text = "Today"
                    } else if (eventDay.calendar?.get(Calendar.DATE)!! - todayCalendar.get(
                            Calendar.DATE
                        ) == 1
                    ) {
                        binding.dateTv.text = "Tomorrow"
                    } else binding.dateTv.text = dateFormat.format(eventDay.calendar?.time!!)
                    date = dateFormat.format(eventDay.calendar?.time!!)
                    val drawable = ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_calendar_for_textview
                    )
                    drawable!!.setTint(Color.parseColor("#80000000"))
                    binding.dateTv.setCompoundDrawablesWithIntrinsicBounds(
                        drawable,
                        null,
                        null,
                        null
                    )
                    calendar.set(
                        eventDay.calendar.get(Calendar.YEAR),
                        eventDay.calendar.get(Calendar.MONTH),
                        eventDay.calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }
            }

        }

    }


    private fun setEvents(allTasks: List<Task>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        val list: ArrayList<Task> = allTasks as ArrayList<Task>

        var actualTask: Task? = null
        var colors = ArrayList<Int>()
        val events: ArrayList<EventDay> = ArrayList()

        while (list.isNotEmpty()) {
            val iterator = list.iterator()
            val cal = Calendar.getInstance()

            while (iterator.hasNext()) {
                val task = iterator.next()
                if (actualTask == null) {
                    if (task.date.isNotEmpty()) {
                        actualTask = task
                        colors = arrayListOf()
                        cal.time = dateFormat.parse(task.date)!!
                        colors.add(task.type.card_color)
                    }
                    iterator.remove()
                } else if (actualTask.date == task.date) {
                    colors.add(task.type.card_color)
                    iterator.remove()
                }
            }
            actualTask = null
            val drawable = CircleDrawable(colors)
            events.add(EventDay(cal, drawable))
        }
        binding.calendarView.setEvents(events)

    }

    private fun setUpTypeRV() {
        typeAdapter = TaskTypeAdapter(
            TaskTypes.getTypes(binding.root.context),
            object : TaskTypeAdapter.OnTypeClickListener {
                override fun onTypeClicked(type: Types) {
                    binding.taskTypeRb.text = type.type_name
                    binding.taskTypeRb.compoundDrawables[2].setTint(type.card_color)
                    binding.taskColorCard.setCardBackgroundColor(type.card_color)
                    taskType = type
                }

            }, true
        )
        binding.tasksTypeRv.adapter = typeAdapter
        database.taskDao().getTasks().onEach {
            typeAdapter.setTasks(it)
        }.launchIn(lifecycleScope)

    }


    @SuppressLint("SetTextI18n")
    private fun setUpParametersUI() {
        binding.apply {
            parametersRg.setOnCheckedChangeListener { p0, p1 ->
                when (p1) {
                    R.id.calendar_rb -> {
                        tasksTypeRv.visibility = View.GONE
                        calendarView.visibility = View.VISIBLE
                        timePickerLayout.visibility = View.GONE
                        initDefaultDate()
                    }
                    R.id.time_rb -> {
                        tasksTypeRv.visibility = View.GONE
                        calendarView.visibility = View.GONE
                        timePickerLayout.visibility = View.VISIBLE
                        setTime(binding.timePicker.hour, binding.timePicker.minute)
                        initDefaultDate()
                    }
                    R.id.task_type_rb -> {
                        tasksTypeRv.visibility = View.VISIBLE
                        calendarView.visibility = View.GONE
                        timePickerLayout.visibility = View.GONE
                    }
                }
                hideKeyboard()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initDefaultDate() {
        if (binding.dateTv.text.isEmpty()) {
            binding.dateTv.text = "Today"
            val drawable = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.ic_calendar_for_textview
            )
            drawable!!.setTint(Color.parseColor("#80000000"))
            binding.dateTv.setCompoundDrawablesWithIntrinsicBounds(
                drawable,
                null,
                null,
                null
            )
            date = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ENGLISH
            ).format(binding.calendarView.selectedDates[0].time)
        }
    }

    private fun hideKeyboard() {
        (activity as MainActivity).currentFocus?.let { view ->
            val imm =
                (activity as MainActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun insertTask(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.taskDao().insertTask(task)
        }
    }

}