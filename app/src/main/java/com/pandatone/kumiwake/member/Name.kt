package com.pandatone.kumiwake.member

import java.io.Serializable

/**
 * Created by atsushi_2 on 2016/03/02.
 */
class Name(id: Int, name: String, sex: String, age: Int, grade: Int, belong: String, role: String, name_read: String) : Serializable {
    var id: Int = 0
        protected set
    var name: String
        protected set
    var name_read: String
        protected set
    var sex: String
        protected set
    var age: Int = 0
        protected set
    var grade: Int = 0
        protected set
    var belong: String
        protected set
    var role: String
        protected set

    init {
        this.id = id
        this.name = name
        this.sex = sex
        this.age = age
        this.grade = grade
        this.belong = belong
        this.role = role
        this.name_read = name_read
    }

    override fun equals(obj: Any?): Boolean {
        if (obj != null && obj is Name) {
            val target = obj as Name?

            return target!!.id == this.id
        }

        return false
    }

    override fun hashCode(): Int {
        return id
    }

}