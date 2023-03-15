package com.azamovhudstc.playstoretodoapp.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Category:Serializable {
    @PrimaryKey(autoGenerate = true)
    var category_id: Int? = null
    var category_name: String? = null
    var category_color: Int? = null

    constructor(category_name: String?, category_color: Int?) {
        this.category_name = category_name
        this.category_color = category_color
    }


    constructor()
    constructor(category_id: Int?, category_name: String?, category_color: Int?) {
        this.category_id = category_id
        this.category_name = category_name
        this.category_color = category_color
    }

}