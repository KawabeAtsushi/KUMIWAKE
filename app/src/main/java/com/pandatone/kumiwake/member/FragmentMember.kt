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
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter
import kotlinx.android.synthetic.main.filter_member.*
import java.io.IOException
import java.util.*

/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMember : ListFragment() {
    private var groupId: String = "0"
    lateinit var listAdp: NameListAdapter
    var parent = MemberMain()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = MemberListAdapter(requireContext())
        gpdbAdapter = GroupListAdapter(requireContext())
        nameList = ArrayList()
        listAdp = NameListAdapter(requireContext(), nameList)
        groupId = parent.groupId.toString()
        NameListAdapter.nowSort = "ID"
        Sort.initial = 2
        listAdapter = listAdp;
        loadName()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_member, container, false)
        fab = view.findViewById<View>(R.id.member_fab) as FloatingActionButton
        fab.setOnClickListener { parent.moveMember() }

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ListCount = listView.count
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(CallbackMB())
        listView.isFastScrollEnabled = true
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
            val builder2 = AlertDialog.Builder(activity)
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view2 = inflater.inflate(R.layout.member_info,
                    activity!!.findViewById<View>(R.id.info_layout) as ViewGroup)
            val membername = nameList[position].name
            if (MemberMain.searchView.isActivated)
                MemberMain.searchView.onActionViewCollapsed()
            FragmentGroup().loadName()

            val Items = arrayOf(Sort.memberContext()!!.getString(R.string.information), Sort.memberContext()!!.getString(R.string.edit), Sort.memberContext()!!.getString(R.string.delete))
            builder.setTitle(membername)
            builder.setItems(Items) { _, which ->
                when (which) {
                    0 -> {
                        MemberClick.memberInfoDialog(view2, builder2)
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
                    2 -> deleteSingleMember(position, membername)
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        if (parent.start_actionmode) {
            listView.startActionMode(CallbackMB())
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                listItem = nameList[i]
                val belongText = listItem.belong
                val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val list = ArrayList(Arrays.asList<String>(*belongArray))
                if (list.contains(groupId)) {
                    listView.setItemChecked(i, !listAdp.isPositionChecked(i))
                }
                dbAdapter.close()
                i += 2
            }
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                listView.setItemChecked(position, !listAdp.isPositionChecked(position))
            }
        }

        if (parent.kumiwake_select) {
            listView.startActionMode(CallbackMB())
            dbAdapter.open()
            for (j in parent.memberArray.indices) {
                var i = 1
                while (i < ListCount) {
                    listItem = nameList[i]
                    if (listItem.id == parent.memberArray[j].id) {
                        listView.setItemChecked(i, !listAdp.isPositionChecked(i))
                    }
                    dbAdapter.close()
                    i += 2
                }
            }

            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                listView.setItemChecked(position, !listAdp.isPositionChecked(position))
            }
        }

        // 行を長押しした時の処理
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            listView.setItemChecked(position, !listAdp.isPositionChecked(position))
            false
        }

        listView.isTextFilterEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
        // アクションアイテム選択時
        when (item!!.itemId) {
            android.R.id.home -> activity!!.finish()

            R.id.item_delete -> deleteMember()

            R.id.item_all_select -> {
                var i = 1
                while (i < ListCount) {
                    listView.setItemChecked(i, true)
                    i += 2
                }
            }

            R.id.item_sort -> {
                Sort.memberSort(builder)
                val dialog = builder.create()
                dialog.show()
                ListCount = listView.count
            }

            R.id.item_filter -> {
                val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout = inflater.inflate(R.layout.filter_member,
                        activity!!.findViewById<View>(R.id.filter_member) as ViewGroup)
                val filter_belong_spinner = layout.findViewById<View>(R.id.filter_belong_spinner) as Spinner
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
                filter_belong_spinner.adapter = adapter
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
                    filter(layout, filter_belong_spinner, dialog2)
                    ListCount = listView.count
                }
            }
        }

        return false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        loadName()
        dbAdapter.notifyDataSetChanged()
    }

    private fun deleteSingleMember(position: Int, name: String) {
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
            parent.reload()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, which -> }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteMember() {
        // アラートダイアログ表示
        val builder = android.support.v7.app.AlertDialog.Builder(parent.applicationContext)
        builder.setTitle(checkedCount.toString() + " " + parent.getString(R.string.member) + parent.getString(R.string.delete))
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { dialog, which ->
            dbAdapter.open()     // DBの読み込み(読み書きの方)
            var i = 1
            while (i < ListCount) {
                val booleanArray = listView.checkedItemPositions
                val checked = booleanArray.get(i)
                if (checked) {
                    // IDを取得する
                    listItem = nameList[i]
                    val listId = listItem.id
                    dbAdapter.selectDelete(listId.toString())     // DBから取得したIDが入っているデータを削除する
                }
                i += 2
            }
            dbAdapter.close()    // DBを閉じる
            listAdp.clearSelection()
            parent.reload()
        }
    }

    fun filter(layout: View, filter_belong_spinner: Spinner, dialog2: android.support.v7.app.AlertDialog) {
        val maxage: Int
        val minage: Int
        val maxgrade: Int
        val mingrade: Int
        var sex: String
        var groupName: String
        val belong: String = filter_belong_spinner.selectedItem as String
        var belongNo = ""
        var role: String
        val sexButton = layout.findViewById<View>(sexGroup.checkedRadioButtonId) as RadioButton
        sex = sexButton.text as String

        if (max_age.text.toString() != "") {
            maxage = AddMember().getValue(max_age)
        } else {
            maxage = 1000
        }
        if (min_age.text.toString() != "") {
            minage = AddMember().getValue(min_age)
        } else {
            minage = 0
        }
        if (max_grade.text.toString() != "") {
            maxgrade = AddMember().getValue(max_grade)
        } else {
            maxgrade = 1000
        }
        if (min_age.text.toString() != "") {
            mingrade = AddMember().getValue(min_grade)
        } else {
            mingrade = 0
        }

        if (maxage < minage) {
            error_age_range.visibility = View.VISIBLE
            error_age_range.setText(R.string.range_error)
        } else {
            error_age_range.visibility = View.GONE
        }
        if (maxgrade < mingrade) {
            error_grade_range.visibility = View.VISIBLE
            error_grade_range.setText(R.string.range_error)
        } else {
            error_grade_range.visibility = View.GONE
        }
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
        role = filter_role_spinner.selectedItem as String
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

    internal inner class CallbackMB : AbsListView.MultiChoiceModeListener {

        private val booleanArray = listView.checkedItemPositions

        private val decision_clicked = View.OnClickListener {
            if (!parent.kumiwake_select) {
                dbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < ListCount) {
                    val checked = booleanArray.get(i)
                    listItem = nameList[i]
                    val listId = listItem.id
                    val newId = parent.groupId
                    if (checked) {
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
                parent.finish()
            } else {
                recreateKumiwakeList()
                parent.moveKumiwake()
            }
            listAdp.clearSelection()
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
            MemberMain.decision.setOnClickListener(decision_clicked)
            searchIcon.isVisible = false
            deleteIcon.isVisible = parent.delete_icon_visible
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_delete -> deleteMember()

                R.id.item_all_select -> {
                    var i = 1
                    while (i < ListCount) {
                        listView.setItemChecked(i, true)
                        i += 2
                    }
                }

                R.id.item_sort -> {
                    Sort.memberSort(builder)
                    val dialog = builder.create()
                    dialog.show()
                    ListCount = listView.count
                }

                R.id.item_filter -> {
                    val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val layout = inflater.inflate(R.layout.filter_member,
                            activity!!.findViewById<View>(R.id.filter_member) as ViewGroup)
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
                    filter_belong_spinner.adapter = adapter
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
                        filter(layout, filter_belong_spinner, dialog2)
                        ListCount = listView.count
                    }
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            // 決定ボタン押下時
            listAdp.clearSelection()
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
                listAdp.setNewSelection(position, checked)
            } else {
                listAdp.removeSelection(position)
            }

            mode.title = checkedCount.toString() + getString(R.string.selected)
        }

        private fun deleteMember() {
            // アラートダイアログ表示
            val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
            builder.setTitle(checkedCount.toString() + " " + getString(R.string.member) + getString(R.string.delete))
            builder.setMessage(R.string.Do_delete)
            // OKの時の処理
            builder.setPositiveButton("OK") { _, _ ->
                dbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < ListCount) {
                    val checked = booleanArray.get(i)
                    if (checked) {
                        // IDを取得する
                        listItem = nameList[i]
                        val listId = listItem.id
                        dbAdapter.selectDelete(listId.toString())     // DBから取得したIDが入っているデータを削除する
                    }
                    i += 2
                }
                dbAdapter.close()    // DBを閉じる
                listAdp.clearSelection()
                parent.reload()
            }

            builder.setNegativeButton(R.string.cancel) { _, _ -> }
            val dialog = builder.create()
            dialog.show()

            listView.isTextFilterEnabled = true
        }

        private fun recreateKumiwakeList() {
            parent.memberArray.clear()
            dbAdapter.open()
            var i = 1
            while (i < ListCount) {
                val checked = booleanArray.get(i)
                listItem = nameList[i]
                if (checked && listItem.sex != "initial") {
                    parent.memberArray.add(listItem)
                }
                i += 2
            }
            dbAdapter.close()
        }
    }

    @Throws(IOException::class)
    fun selectName(newText: String) {
        if (TextUtils.isEmpty(newText)) {
            dbAdapter.picName(null.toString())
        } else {
            dbAdapter.picName(newText)
        }
    }



    fun addGroupByGroup(newId: Int, myId: Int) {
        dbAdapter.open()
        var i = 1
        while (i < ListCount) {
            listItem = nameList[i]
            val listId = listItem.id
            val belongText = listItem.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val list = ArrayList(Arrays.asList<String>(*belongArray))
            if (list.contains(groupId.toString())) {
                parent.memberArray.add(listItem)
            }
            i += 2
        }
        dbAdapter.close()
    }

    fun duplicateBelong() {
        dbAdapter.open()
        var i = 1
        while (i < ListCount) {
            listItem = nameList[i]
            val listId = listItem.id
            val belongText = listItem.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
            i += 2
        }
        dbAdapter.close()
        loadName()
    }

    fun deleteBelongInfoAll(groupId: Int) {
        dbAdapter.open()
        for (i in 1 until ListCount) {
            listItem = nameList[i]
            val listId = listItem.id
            DeleteBelongInfo(groupId, listId)
        }
        dbAdapter.close()
        loadName()
    }

    fun DeleteBelongInfo(groupId: Int, listId: Int) {
        val belongText = listItem.belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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

    fun searchBelong(belongId: String): ArrayList<Name> {
        val memberArrayByBelong = ArrayList<Name>()
        dbAdapter.open()
        var i = 1
        while (i < ListCount) {
            listItem = nameList[i]
            val listName = listItem.name
            val listSex = listItem.sex
            val belongText = listItem.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (Arrays.asList<String>(*belongArray).contains(belongId)) {
                memberArrayByBelong.add(Name(0, listName, listSex, 0, 0, null.toString(), null.toString(), null.toString()))
            }
            i += 2
        }
        dbAdapter.close()
        loadName()
        return memberArrayByBelong
    }

    fun loadName() {
        dbAdapter.open()
        val c = dbAdapter.allNames
        dbAdapter.getCursor(c)
        dbAdapter.close()
    }

    companion object {
        internal lateinit var dbAdapter: MemberListAdapter
        internal lateinit var gpdbAdapter: GroupListAdapter
        var nameList: ArrayList<Name> = ArrayList()
        internal lateinit var listItem: Name
        internal lateinit var fab: FloatingActionButton
        internal var ListCount: Int = 0
        internal var checkedCount = 0
    }

}






