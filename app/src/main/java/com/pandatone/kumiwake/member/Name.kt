package com.pandatone.kumiwake.member

import java.io.Serializable

/**
 * Created by atsushi_2 on 2016/03/02.
 */
class Name(id: Int, name: String, sex: String, age: Int, grade: Int, belong: String, role: String, read: String) : Serializable {
    var id: Int = 0
        private set
    var name: String
        private set
    var read: String
        private set
    var sex: String
        private set
    var age: Int = 0
        private set
    private var grade: Int = 0
    var belong: String
        private set
    var role: String

    init {
        this.id = id
        this.name = name
        this.sex = sex
        this.age = age
        this.grade = grade
        this.belong = belong
        this.role = role
        this.read = read
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Name) {
            val target = other as Name?

            return target!!.id == this.id
        }

        return false
    }

    override fun hashCode(): Int {
        return id
    }

}