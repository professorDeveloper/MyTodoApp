package com.azamovhudstc.playstoretodoapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import com.azamovhudstc.playstoretodoapp.R

data class Types(
    val type_name:String,
    val name_color:Int,
    val card_color:Int,
    val count_color:Int,
    val complated_color:Int,
    val count:Int = 0,
)

object TaskTypes {
    @SuppressLint("NewApi")
    fun getTypes(context: Context): List<Types> {

        return arrayListOf(
            Types("Inbox",context.getColor(R.color.black), context.getColor(R.color.light_gray), context.getColor(R.color.gray), Color.parseColor("#80000000")),
            Types("Work",context.getColor(R.color.white), context.getColor(R.color.light_green), context.getColor(R.color.light_gray), Color.parseColor("#80FFFFFF")),
            Types("Shopping",context.getColor(R.color.white), context.getColor(R.color.light_red), context.getColor(R.color.light_gray), Color.parseColor("#80FFFFFF")),
            Types("Family", context.getColor(R.color.black),context.getColor(R.color.yellow), context.getColor(R.color.gray), Color.parseColor("#80000000")),
            Types("Personal",context.getColor(R.color.white), context.getColor(R.color.purple), context.getColor(R.color.light_gray), Color.parseColor("#80FFFFFF"))
        )
    }
}