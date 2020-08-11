package com.pandatone.kumiwake.others.drawing

import java.io.Serializable

class Ticket(id: Int, name: String, color: Int) : Serializable {
    var id: Int
        private set
    var name: String
    var color: Int

    init {
        this.id = id
        this.name = name
        this.color = color
    }

}