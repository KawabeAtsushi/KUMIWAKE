package com.pandatone.kumiwake.member

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ListFragment
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter
import java.io.IOException
import java.util.*

/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMember : ListFragment() {
    internal var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = MemberListAdapter(activity)
        gpdbAdapter = GroupListAdapter(activity)
        nameList = ArrayList()
        listAdapter = NameListAdapter(activity, nameList)
        groupId = parent!!.groupId.toString()
        NameListAdapter.nowSort = "ID"
        Sort.initial = 2
        listAdapter = listAdapter
        loadName()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.tab_member, container, false)
        fab = view.findViewById<View>(R.id.member_fab) as FloatingActionButton
        fab.setOnClickListener { parent!!.moveMember() }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ListCount = listView.count
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(CallbackMB())
        listView.isFastScrollEnabled = true
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            //行をクリックした時の処理
            val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
            val builder2 = AlertDialog.Builder(activity)
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view2 = inflater.inflate(R.layout.member_info,
                    activity!!.findViewById<View>(R.id.info_layout) as ViewGroup)
            val membername = nameList[position].name
            if (MemberMain.searchView.isActivated == true)
                MemberMain.searchView.onActionViewCollapsed()
            FragmentGroup.loadName()

            val Items = arrayOf(Sort.name_getContext()!!.getString(R.string.information), Sort.name_getContext()!!.getString(R.string.edit), Sort.name_getContext()!!.getString(R.string.delete))
            builder.setTitle(membername)
            builder.setItems(Items) { dialog, which ->
                when (which) {
                    0 -> {
                        MemberClick.MemberInfoDialog(view2, builder2)
                        MemberClick.SetInfo(position)
                        val dialog2 = builder2.create()
                        dialog2.show()
                        MemberClick.okBt.setOnClickListener { dialog2.dismiss() }
                    }
                    1 -> {
                        val i = Intent(activity, AddMember::class.java)
                        i.putExtra("POSITION", position)
                        startActivity(i)
                    }
                    2 -> DeleteSingleMember(position, membername)
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        if (parent!!.start_actionmode == true) {
            listView.startActionMode(CallbackMB())
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                listItem = nameList[i]
                val belongText = listItem.belong
                if (belongText != null) {
                    val belongArray = belongText.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val list = ArrayList(Arrays.asList<String>(*belongArray))
                    if (list.contains(groupId)) {
                        listView.setItemChecked(i, !listAdapter.isPositionChecked(i))
                    }
                }
                dbAdapter.close()
                i += 2
            }
            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                //行をクリックした時の処理
                listView.setItemChecked(position, !listAdapter.isPositionChecked(position))
            }
        }

        if (parent!!.kumiwake_select == true) {
            listView.startActionMode(CallbackMB())
            dbAdapter.open()
            for (j in parent!!.memberArray.indices) {
                var i = 1
                while (i < ListCount) {
                    listItem = nameList[i]
                    if (listItem.id == parent!!.memberArray.get(j).id) {
                        listView.setItemChecked(i, !listAdapter.isPositionChecked(i))
                    }
                    dbAdapter.close()
                    i += 2
                }
            }

            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                //行をクリックした時の処理
                listView.setItemChecked(position, !listAdapter.isPositionChecked(position))
            }
        }

        // 行を長押しした時の処理
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            listView.setItemChecked(position, !listAdapter.isPositionChecked(position))
            false
        }

        listView.isTextFilterEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
        // アクションアイテム選択時
        when (item!!.itemId) {
            android.R.id.home -> activity!!.finish()

            R.id.item_delete -> DeleteMember()

            R.id.item_all_select ->
                var i = 1
            while (i < ListCount) {
                listView.setItemChecked(i, true)
                i += 2
            }

                    R . id . item_sort -> {
                Sort.memberSort(builder)
                val dialog = builder.create()
                dialog.show()
                ListCount = listView.count
            }

            R.id.item_filter -> {
                val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout = inflater.inflate(R.layout.filter_member,
                        activity!!.findViewById<View>(R.id.filter_member) as ViewGroup)
                val belongSpinner = layout.findViewById<View>(R.id.filter_belong_spinner) as Spinner
                val adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_item)
                val list = ArrayList<String>() // 新インスタンスを生成
                list.add(getString(R.string.no_selected))
                for (j in 0 until FragmentGroup.ListCount) {
                    val listItem = FragmentGroup.nameList[j]
                    val groupName = listItem.group
                    list.add(groupName)
                }
                adapter.addAll(list)
                adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                )
                belongSpinner.adapter = adapter
                builder.setTitle(getText(R.string.filtering))
                builder.setView(layout)
                builder.setPositiveButton("OK", null)
                builder.setNegativeButton(R.string.cancel) { dialog, which -> }
                // back keyを使用不可に設定
                builder.setCancelable(false)
                val dialog2 = builder.create()
                dialog2.show()

                val okButton = dialog2.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.setOnClickListener {
                    filter(layout, belongSpinner, dialog2)
                    ListCount = listView.count
                }
            }
        }

        return false
    }

    override fun onAttach(context: Context?) {
        parent = context as MemberMain?
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        loadName()
        dbAdapter.notifyDataSetChanged()
    }

    fun DeleteSingleMember(position: Int, name: String) {
        val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
        builder.setTitle(name)
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { dialog, which ->
            dbAdapter.open()
            listItem = nameList[position]
            val listId = listItem.id
            dbAdapter.selectDelete(listId.toString())
            dbAdapter.close()    // DBを閉じる
            parent!!.reload()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, which -> }
        val dialog = builder.create()
        dialog.show()
    }

    fun DeleteMember() {
        // アラートダイアログ表示
        val builder = android.support.v7.app.AlertDialog.Builder(parent!!.applicationContext)
        builder.setTitle(checkedCount.toString() + " " + parent!!.getString(R.string.member) + parent!!.getString(R.string.delete))
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { dialog, which ->
            dbAdapter.open()     // DBの読み込み(読み書きの方)
            var i = 1
            while (i < ListCount) {
                val booleanArray = listView.checkedItemPositions
                val checked = booleanArray.get(i)
                if (checked == true) {
                    // IDを取得する
                    listItem = nameList[i]
                    val listId = listItem.id
                    dbAdapter.selectDelete(listId.toString())     // DBから取得したIDが入っているデータを削除する
                }
                i += 2
            }
            dbAdapter.close()    // DBを閉じる
            listAdapter.clearSelection()
            parent!!.reload()
        }
    }

    fun filter(layout: View, belongSpinner: Spinner, dialog2: android.support.v7.app.AlertDialog) {
        val error_age: TextView
        val error_grade: TextView
        val maxageV: EditText
        val minageV: EditText
        val maxgradeV: EditText
        val mingradeV: EditText
        val roleSpinner: Spinner
        val maxage: Int
        val minage: Int
        val maxgrade: Int
        val mingrade: Int
        var sex: String
        var groupName: String
        val belong: String
        var belongNo = ""
        var role: String
        maxageV = layout.findViewById<View>(R.id.max_age) as EditText
        minageV = layout.findViewById<View>(R.id.min_age) as EditText
        maxgradeV = layout.findViewById<View>(R.id.max_grade) as EditText
        mingradeV = layout.findViewById<View>(R.id.min_grade) as EditText
        error_age = layout.findViewById<View>(R.id.error_age_range) as TextView
        error_grade = layout.findViewById<View>(R.id.error_grade_range) as TextView
        val sexGroup = layout.findViewById<View>(R.id.sexGroup) as RadioGroup
        val sexButton = layout.findViewById<View>(sexGroup.checkedRadioButtonId) as RadioButton
        roleSpinner = layout.findViewById<View>(R.id.filter_role_spinner) as Spinner
        sex = sexButton.text as String

        if (maxageV.text.toString() != "") {
            maxage = AddMember.getValue(maxageV)
        } else {
            maxage = 1000
        }
        if (minageV.text.toString() != "") {
            minage = AddMember.getValue(minageV)
        } else {
            minage = 0
        }
        if (maxgradeV.text.toString() != "") {
            maxgrade = AddMember.getValue(maxgradeV)
        } else {
            maxgrade = 1000
        }
        if (minageV.text.toString() != "") {
            mingrade = AddMember.getValue(mingradeV)
        } else {
            mingrade = 0
        }

        if (maxage < minage) {
            error_age.visibility = View.VISIBLE
            error_age.setText(R.string.range_error)
        } else {
            error_age.visibility = View.GONE
        }
        if (maxgrade < mingrade) {
            error_grade.visibility = View.VISIBLE
            error_grade.setText(R.string.range_error)
        } else {
            error_grade.visibility = View.GONE
        }
        belong = belongSpinner.selectedItem as String
        if (belong == getString(R.string.no_selected)) {
            belongNo = ""
        } else {
            for (j in 0 until FragmentGroup.ListCount) {
                val listItem = FragmentGroup.nameList[j]
                groupName = listItem.group
                if (belong == groupName) {
                    belongNo = listItem.id.toString() + ","
                }
            }
        }
        role = roleSpinner.selectedItem as String
        if (role == getString(R.string.no_selected)) {
            role = ""
        }
        if (sex == getString(R.string.no_specified)) {
            sex = ""
        }
        try {
            if (maxage >= minage && maxgrade >= mingrade) {
                dbAdapter.filterName(sex, minage, maxage, mingrade, maxgrade, belongNo, role)
                dialog2.dismiss()
            } else {

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    internal inner class CallbackMB : ListView.MultiChoiceModeListener {

        val booleanArray = listView.checkedItemPositions

        private val decision_clicked = View.OnClickListener {
            if (parent!!.kumiwake_select == false) {
                dbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < ListCount) {
                    val checked = booleanArray.get(i)
                    listItem = nameList[i]
                    val listId = listItem.id
                    val newId = parent!!.groupId
                    if (checked == true) {
                        val newBelong = StringBuilder()
                        newBelong.append(listItem.belong)
                        newBelong.append(",$newId")
                        dbAdapter.addBelong(listId.toString(), newBelong.toString())
                    } else {
                        DeleteBelongInfo(newId, listId)
                    }
                    i += 2
                }
                dbAdapter.close()    // DBを閉じる
                loadName()
                parent!!.finish()
            } else {
                recreateKumiwakeList()
                parent!!.moveKumiwake()
            }
            listAdapter.clearSelection()
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理
            val inflater = activity!!.menuInflater
            inflater.inflate(R.menu.member_main_menu, menu)
            val searchIcon = menu.findItem(R.id.search_view)
            val deleteIcon = menu.findItem(R.id.item_delete)
            menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu.getItem(3).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            parent!!.decision.setOnClickListener(decision_clicked)
            searchIcon.isVisible = false
            deleteIcon.isVisible = parent!!.delete_icon_visible
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_delete -> DeleteMember()

                R.id.item_all_select ->
                    var i = 1
                while (i < ListCount) {
                    listView.setItemChecked(i, true)
                    i += 2
                }

                        R . id . item_sort -> {
                    Sort.memberSort(builder)
                    val dialog = builder.create()
                    dialog.show()
                    ListCount = listView.count
                }

                R.id.item_filter -> {
                    val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val layout = inflater.inflate(R.layout.filter_member,
                            activity!!.findViewById<View>(R.id.filter_member) as ViewGroup)
                    val belongSpinner = layout.findViewById<View>(R.id.filter_belong_spinner) as Spinner
                    val adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_item)
                    val list = ArrayList<String>() // 新インスタンスを生成
                    list.add(getString(R.string.no_selected))
                    for (j in 0 until FragmentGroup.ListCount) {
                        val listItem = FragmentGroup.nameList[j]
                        val groupName = listItem.group
                        list.add(groupName)
                    }
                    adapter.addAll(list)
                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item)
                    belongSpinner.adapter = adapter
                    builder.setTitle(getText(R.string.filtering))
                    builder.setView(layout)
                    builder.setPositiveButton("OK", null)
                    builder.setNegativeButton(R.string.cancel) { dialog, which -> }
                    // back keyを使用不可に設定
                    builder.setCancelable(false)
                    val dialog2 = builder.create()
                    dialog2.show()

                    val okButton = dialog2.getButton(AlertDialog.BUTTON_POSITIVE)
                    okButton.setOnClickListener {
                        filter(layout, belongSpinner, dialog2)
                        ListCount = listView.count
                    }
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            // 決定ボタン押下時
            listAdapter.clearSelection()
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード表示事前処理
            return true
        }

        override fun onItemCheckedStateChanged(mode: ActionMode,
                                               position: Int, id: Long, checked: Boolean) {
            // アクションモード時のアイテムの選択状態変更時

            checkedCount = listView.checkedItemCount

            if (checked) {
                listAdapter.setNewSelection(position, checked)
            } else {
                listAdapter.removeSelection(position)
            }

            mode.title = checkedCount.toString() + getString(R.string.selected)
        }

        fun DeleteMember() {
            // アラートダイアログ表示
            val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
            builder.setTitle(checkedCount.toString() + " " + getString(R.string.member) + getString(R.string.delete))
            builder.setMessage(R.string.Do_delete)
            // OKの時の処理
            builder.setPositiveButton("OK") { dialog, which ->
                dbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < ListCount) {
                    val checked = booleanArray.get(i)
                    if (checked == true) {
                        // IDを取得する
                        listItem = nameList[i]
                        val listId = listItem.id
                        dbAdapter.selectDelete(listId.toString())     // DBから取得したIDが入っているデータを削除する
                    }
                    i += 2
                }
                dbAdapter.close()    // DBを閉じる
                listAdapter.clearSelection()
                parent!!.reload()
            }

            builder.setNegativeButton(R.string.cancel) { dialog, which -> }
            val dialog = builder.create()
            dialog.show()

            listView.isTextFilterEnabled = true
        }

        fun recreateKumiwakeList() {
            parent!!.memberArray.clear()
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                val checked = booleanArray.get(i)
                listItem = nameList[i]
                if (checked == true && listItem.sex != "initial") {
                    parent!!.memberArray.add(listItem)
                }
                i += 2
            }
            dbAdapter.close()
        }
    }

    companion object {
        private var parent: MemberMain? = null
        internal var dbAdapter: MemberListAdapter
        var listAdapter: NameListAdapter
        internal var gpdbAdapter: GroupListAdapter
        var nameList: ArrayList<Name> = ArrayList()
        internal var listItem: Name
        internal var fab: FloatingActionButton
        internal var ListCount: Int = 0
        internal var checkedCount = 0


        fun loadName() {
            dbAdapter.open()
            val c = dbAdapter.allNames
            dbAdapter.getCursor(c)
            dbAdapter.close()
        }

        @Throws(IOException::class)
        fun selectName(newText: String) {
            if (TextUtils.isEmpty(newText)) {
                dbAdapter.picName(null)
            } else {
                dbAdapter.picName(newText)
            }
        }

        fun searchBelong(belongId: String): ArrayList<Name> {
            val memberArrayByBelong = ArrayList<Name>()
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                listItem = nameList[i]
                val listName = listItem.name
                val listSex = listItem.sex
                val belongText = listItem.belong
                if (belongText != null) {
                    val belongArray = belongText.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    if (Arrays.asList<String>(*belongArray).contains(belongId)) {
                        memberArrayByBelong.add(Name(0, listName, listSex, 0, 0, null, null, null))
                    }
                }
                i += 2
            }
            dbAdapter.close()
            loadName()
            return memberArrayByBelong
        }

        fun DuplicateBelong() {
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                listItem = nameList[i]
                val listId = listItem.id
                val belongText = listItem.belong
                if (belongText != null) {
                    val belongArray = belongText.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val list = ArrayList(Arrays.asList<String>(*belongArray))
                    val hs = HashSet<String>()
                    hs.addAll(list)
                    list.clear()
                    list.addAll(hs)
                    val newBelong = StringBuilder()

                    for (j in list.indices) {
                        newBelong.append(list[j])
                        if (j != list.size - 1) {
                            newBelong.append(",")
                        }
                    }
                    dbAdapter.addBelong(listId.toString(), newBelong.toString())
                }
                i += 2
            }
            dbAdapter.close()
            loadName()
        }

        fun DeleteBelongInfo(groupId: Int, listId: Int) {
            val belongText = listItem.belong
            if (belongText != null) {
                val belongArray = belongText.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val list = ArrayList(Arrays.asList<String>(*belongArray))
                val hs = HashSet<String>()
                hs.addAll(list)
                list.clear()
                list.addAll(hs)
                if (list.contains(groupId.toString())) {
                    list.remove(groupId.toString())
                    val newBelong = StringBuilder()

                    for (j in list.indices) {
                        newBelong.append(list[j])
                        if (j != list.size - 1) {
                            newBelong.append(",")
                        }
                    }
                    dbAdapter.addBelong(listId.toString(), newBelong.toString())
                }
            }
        }

        fun DeleteBelongInfoAll(groupId: Int) {
            dbAdapter.open()
            for (i in 1 until ListCount) {
                listItem = nameList[i]
                val listId = listItem.id
                DeleteBelongInfo(groupId, listId)
            }
            dbAdapter.close()
            loadName()
        }

        fun addGroupByGroup(newId: Int, myId: Int) {
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                listItem = nameList[i]
                val listId = listItem.id
                val belongText = listItem.belong
                val belongArray = belongText.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val list = ArrayList(Arrays.asList<String>(*belongArray))
                val hs = HashSet<String>()
                hs.addAll(list)
                list.clear()
                list.addAll(hs)
                if (list.contains(myId.toString())) {
                    val newBelong = StringBuilder()
                    for (j in list.indices) {
                        newBelong.append(list[j])
                        if (j != list.size - 1) {
                            newBelong.append(",")
                        }
                    }
                    newBelong.append(",$newId")
                    dbAdapter.addBelong(listId.toString(), newBelong.toString())
                }
                i += 2
            }
            dbAdapter.close()
            loadName()
        }

        fun createKumiwakeListByGroup(groupId: Int) {
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                listItem = nameList[i]
                val belongText = listItem.belong
                if (belongText != null) {
                    val belongArray = belongText.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val list = ArrayList(Arrays.asList<String>(*belongArray))
                    if (list.contains(groupId.toString())) {
                        parent!!.memberArray.add(listItem)
                    }
                }
                i += 2
            }
            dbAdapter.close()
        }
    }

}






