package com.pandatone.kumiwake.member.function

import java.io.Serializable

class Group(id: Int, name: String, read: String, belong_no: Int) : Serializable {
    var id: Int
        private set
    var name: String
        private set
    private var read: String
    var belongNo: Int
        private set

    init {
        this.id = id
        this.name = name
        this.read = read
        this.belongNo = belong_no
    }

}