package com.azamovhudstc.playstoretodoapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.azamovhudstc.playstoretodoapp.database.entity.Task
import com.azamovhudstc.playstoretodoapp.utils.Types
import com.azamovhudstc.playstoretodoapp.databinding.ItemTasksCheckerBinding
import java.util.ArrayList


class TaskTypeAdapter(
    private val list: List<Types>,
    private val listener: OnTypeClickListener,
    private val forAdd: Boolean
) :
    RecyclerView.Adapter<TaskTypeAdapter.TTVH>() {

    private var selectedPos = -1
    private var imageView: ImageView? = null
    private var allTasks: ArrayList<Task>? = null

    inner class TTVH(private val itemBinding: ItemTasksCheckerBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(type: Types, position: Int) {
            itemBinding.titleTv.text = type.type_name
            itemBinding.card.setCardBackgroundColor(type.card_color)
            itemBinding.titleTv.setTextColor(type.name_color)
            itemBinding.taskCountTv.setTextColor(type.count_color)

            var counter = 0
            allTasks?.forEach {
                if (it.type.type_name==type.type_name)
                    counter++
            }
            val taskCount = counter.toString() + if (counter > 1) " tasks" else " task"
            itemBinding.taskCountTv.text = taskCount

            if (imageView == null && selectedPos == -1 && forAdd) {
                selectedPos = 0
                imageView = itemBinding.checkIv
                itemBinding.checkIv.visibility = View.VISIBLE
            }

            itemBinding.card.setOnClickListener {
                if (selectedPos != position && forAdd) {
                    imageView!!.visibility = View.GONE
                    selectedPos = position
                    imageView = itemBinding.checkIv
                    itemBinding.checkIv.visibility = View.VISIBLE
                }
                listener.onTypeClicked(type)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TTVH {
        return TTVH(
            ItemTasksCheckerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun setTasks(list: List<Task>){
        allTasks = arrayListOf()
        allTasks!!.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TTVH, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int = 5

    interface OnTypeClickListener {
        fun onTypeClicked(type: Types)
    }

}