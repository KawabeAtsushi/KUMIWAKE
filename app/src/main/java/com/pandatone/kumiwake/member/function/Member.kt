package com.pandatone.kumiwake.member.function

import java.io.Serializable

/**
 * Created by atsushi_2 on 2016/03/02.
 */
class Member(
    id: Int,
    name: String,
    sex: String,
    age: Int,
    belong: String,
    read: String,
    leader: Int
) : Serializable {
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
    var belong: String
        private set
    var leader: Int

    init {
        this.id = id
        this.name = name
        this.read = read
        this.sex = sex
        this.age = age
        this.belong = belong
        this.leader = leader
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Member) {
            val target = other as Member?

            return target!!.id == this.id
        }

        return false
    }

    override fun hashCode(): Int {
        return id
    }

}