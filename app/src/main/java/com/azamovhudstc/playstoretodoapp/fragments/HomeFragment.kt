package com.azamovhudstc.playstoretodoapp.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import com.azamovhudstc.playstoretodoapp.adapters.ActualTaskAdapter
import com.azamovhudstc.playstoretodoapp.adapters.TaskTypeAdapter
import com.azamovhudstc.playstoretodoapp.utils.TaskTypes
import com.azamovhudstc.playstoretodoapp.utils.Types
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*
import android.view.WindowManager
import android.view.LayoutInflater
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.azamovhudstc.playstoretodoapp.BuildConfig
import com.azamovhudstc.playstoretodoapp.R
import com.azamovhudstc.playstoretodoapp.database.AppDatabase
import com.azamovhudstc.playstoretodoapp.database.entity.Task
import com.azamovhudstc.playstoretodoapp.databinding.FragmentHomeBinding
import com.azamovhudstc.playstoretodoapp.databinding.ItemBottomsheetBinding
import kotlinx.android.synthetic.main.about_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var actualAdapter: ActualTaskAdapter
    private lateinit var taskAdapter: TaskTypeAdapter
    private lateinit var database: AppDatabase
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var actualTime = "Today"
    private lateinit var sPref: SharedPreferences
    private var animated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sPref = requireActivity().getSharedPreferences("shared", MODE_PRIVATE)


    }


    @SuppressLint("NotifyDataSetChanged", "InlinedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        database = AppDatabase.getDatabase(binding.root.context)
        binding.drawerContainer.setRadius(Gravity.START, 35f)
        binding.drawerContainer.setViewScale(Gravity.START, 0.9f)
        binding.drawerContainer.setViewElevation(Gravity.START, 20f)

        onClickListeners()

        initAdapters()
        actualTime = sPref.getString("actual", "Today")!!
        setActualTasks(actualTime)

        return binding.root
    }

    private fun setActualTasks(string: String) {
        val amount = when (string) {
            "Yesterday" -> -1
            "Today" -> 0
            "Tomorrow" -> 1
            else -> 0
        }
        binding.todayTv.text = string
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, amount)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val actualDay = dateFormat.format(calendar.time)
        var list: List<Task>
        database.taskDao().getTasksByDate(actualDay).onEach {
            list = it
            actualAdapter.disableAnimation(animated)
            actualAdapter.setTasks(list)
            if (list.isEmpty()) {
                binding.nothingTv.visibility = View.VISIBLE
            } else binding.nothingTv.visibility = View.GONE
        }.launchIn(lifecycleScope)

    }


    private fun initAdapters() {
        val string = arguments?.getString("noti", "")
        actualAdapter =
            ActualTaskAdapter(string.toString(), isByType = false)

        taskAdapter = TaskTypeAdapter(
            TaskTypes.getTypes(binding.root.context),
            object : TaskTypeAdapter.OnTypeClickListener {
                @SuppressLint("SetTextI18n")
                override fun onTypeClicked(type: Types) {
                    openBottomSheet(type)
                }
            },
            false
        )

        binding.actualTasksRv.adapter = actualAdapter
        binding.tasksListRv.adapter = taskAdapter
        database.taskDao().getTasks().onEach {
            taskAdapter.setTasks(it)
        }.launchIn(lifecycleScope)

    }

    private fun onClickListeners() {
        binding.apply {
            navigationView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.share -> {
                        val sharingIntent = Intent(Intent.ACTION_SEND)
                        sharingIntent.type = "text/plain"

                        val shareBody = "Dictionary App Sharing With"
                        val shareSubject =
                            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareSubject)
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                        startActivity(Intent.createChooser(sharingIntent, "Share using"))
                    }
                    R.id.rate -> {
                        val url =
                            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    }
                    R.id.info -> {
                        var  alert= Dialog(requireContext())
                        val inflater = LayoutInflater.from(requireContext())
                        var view = inflater.inflate(R.layout.about_dialog,null)
                        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        alert.setContentView(view)
                        view.closeAboutDialog.setOnClickListener {
                            alert.dismiss()
                        }
                        alert.show()
                    }
                }
                drawerContainer.closeDrawer(GravityCompat.START)
                true
            }

            fam.setOnClickListener() {
                parentFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    AddTaskFragment()
                ).addToBackStack("add").commit()
//                    showPopup(fam)
            }
            imageMenu.setOnClickListener {
                drawerContainer.openDrawer(GravityCompat.START)
            }

            // Menu Click
            moreIv.setOnClickListener {
                val popupMenu = PopupMenu(binding.root.context, moreIv)
                popupMenu.inflate(R.menu.more_menu)
                popupMenu.setOnMenuItemClickListener {
                    actualTime = it.title.toString()
                    sPref.edit().putString("actual", actualTime).apply()
                    setActualTasks(actualTime)
                    true
                }
                popupMenu.show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openBottomSheet(type: Types) {
        bottomSheetDialog = BottomSheetDialog(binding.root.context, R.style.SheetDialog)
        val view = ItemBottomsheetBinding.inflate(layoutInflater)
        view.titleTv.text = type.type_name



        view.linearLayout.background.setTint(type.card_color)
        view.titleTv.setTextColor(type.name_color)
        view.titleTv.compoundDrawablesRelative.forEach {
            it?.setTint(type.name_color)
        }
        view.taskCountTv.setTextColor(type.count_color)
        val adapter = ActualTaskAdapter("", isByType = true)

        lifecycleScope.launch(Dispatchers.IO) {
            val list = database.taskDao().getTasksByType(type.type_name)
            adapter.setTasks(list)
            view.taskCountTv.text = list.size.toString() + if (list.size > 1) " tasks" else " task"
        }

        view.rv.adapter = adapter

        bottomSheetDialog?.setContentView(view.root)
        bottomSheetDialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val parentLayout =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { v ->
                val behaviour = BottomSheetBehavior.from(v)
                setupFullHeight(v)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        bottomSheetDialog?.show()

    }


    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onPause() {
        super.onPause()
        animated = true
    }


    @SuppressLint("DiscouragedPrivateApi")
    private fun showPopup(view: View) {
        val popupMenu = PopupMenu(binding.root.context, view, Gravity.END)



        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            bottomSheetDialog?.dismiss()
            when (item!!.itemId) {
                R.id.task -> {
                    parentFragmentManager.beginTransaction().replace(
                        R.id.fragment_container,
                        AddTaskFragment()
                    ).addToBackStack("add").commit()
                }
//                R.id.list -> {
//                    Toast.makeText(requireContext(), "coming soon", Toast.LENGTH_SHORT).show()
//                }
            }
            true
        }
        popupMenu.inflate(R.menu.popup_menu)
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popupMenu)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error showing menu icons, ${e.message}")
        } finally {
            popupMenu.show()
        }
    }

}
