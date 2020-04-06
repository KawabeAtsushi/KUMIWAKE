package com.pandatone.kumiwake.history

import java.io.Serializable

class History(id: Int, time: String, name: String, result: String, mode: Int, keep: Int, parent: Int) : Serializable {
    var id: Int //ID
        private set
    var time: String //作成日時
        private set
    var name: String //履歴名（デフォルトは日時にする）
        private set
    var result: String //（組分け結果）
        private set
    var mode: Int //kumiwake(0) or sekigime(1)
        private set
    var keep: Int //条件を保存する(Default -1,保存の場合は 1)
        private set
    var parent: Int //もとにした条件のID(Default -1)
        private set

    init {
        this.id = id
        this.time = time
        this.name = name
        this.result = result
        this.mode = mode
        this.keep = keep
        this.parent = parent
    }

}