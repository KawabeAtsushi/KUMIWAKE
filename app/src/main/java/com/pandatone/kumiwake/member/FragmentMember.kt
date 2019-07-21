package com.pandatone.kumiwake.member

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import java.io.IOException
import java.util.*


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMember : ListFragment() {
    val parent = MemberMain()
    var memberArray = parent.memberArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = MemberListAdapter(requireContext())
        nameList = ArrayList()
        listAdp = NameListAdapter(requireContext(), nameList)
        NameListAdapter.nowSort = "ID"
        Sort.initial = 0
        listAdapter = listAdp
        loadName()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_member, container, false)
        fab = view.findViewById<View>(R.id.member_fab) as FloatingActionButton
        fab.setOnClickListener { moveMember() }

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    private fun moveMember() {
        val intent = Intent(activity, AddMember::class.java)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lv = listView
        lv.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        lv.setMultiChoiceModeListener(CallbackMB())
        lv.isFastScrollEnabled = true
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
            val builder2 = AlertDialog.Builder(activity)
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view2 = inflater.inflate(R.layout.member_info, activity!!.findViewById<View>(R.id.info_layout) as ViewGroup?)
            val memberName = nameList[position].name
            if (MemberMain.searchView.isActivated)
                MemberMain.searchView.onActionViewCollapsed()
            FragmentGroup().loadName()
            listCount = lv.count

            val items = arrayOf(MyApplication.context!!.getText(R.string.information), MyApplication.context!!.getText(R.string.edit), MyApplication.context!!.getText(R.string.delete))
            builder.setTitle(memberName)
            builder.setItems(items) { _, which ->
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
                        i.putExtra(AddMember.POSITION, position)
                        startActivity(i)
                    }
                    2 -> deleteSingleMember(position, memberName)
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        // 行を長押しした時の処理
        lv.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            lv.setItemChecked(position, !listAdp.isPositionChecked(position))
            false
        }

        lv.isTextFilterEnabled = true
    }

    //Activity生成後に呼ばれる
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (MemberMain.startAction) {
            lv.startActionMode(CallbackMB())

            if (MemberMain.kumiwake_select) {

                for (j in memberArray.indices) {
                    var i = 1
                    while (i < listCount) {
                        listItem = nameList[i]
                        if (listItem.id == memberArray[j].id) {
                            lv.setItemChecked(i, !listAdp.isPositionChecked(i))
                        }
                        i += 2
                    }
                }
            } else {
                checkByGroup(MemberMain.groupId)
            }

            lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                lv.setItemChecked(position, !listAdp.isPositionChecked(position))
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- Menuの処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        listCount = lv.count

        // アクションアイテム選択時
        when (item!!.itemId) {
            android.R.id.home -> activity!!.finish()

            R.id.item_all_select -> {
                var i = 1
                while (i < listCount) {
                    lv.setItemChecked(i, true)
                    i += 2
                }
            }

            R.id.item_sort -> {
                Sort.memberSort(builder, requireActivity())
                val dialog = builder.create()
                dialog.show()
            }

            R.id.item_filter -> {
                filtering(builder)
            }
        }

        return false
    }

    override fun onStart() {
        super.onStart()
        loadName()
    }

    private fun deleteSingleMember(position: Int, name: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        builder.setTitle(name)
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { _, _ ->
            dbAdapter.open()
            listItem = nameList[position]
            val listId = listItem.id
            dbAdapter.selectDelete(listId.toString())
            dbAdapter.close()    // DBを閉じる
            loadName()
            listCount = lv.count
        }

        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

    private fun filter(layout: View, spinner: Spinner, clear: Boolean) {

        var groupName: String
        val belong: String = spinner.selectedItem as String
        var belongId = ""
        val sexGroup = layout.findViewById<View>(R.id.sexGroup) as RadioGroup
        val sexButton = layout.findViewById<View>(sexGroup.checkedRadioButtonId) as RadioButton
        val error_age_range = layout.findViewById<View>(R.id.error_age_range) as TextView
        var sex = sexButton.text as String
        val max_age = layout.findViewById<View>(R.id.max_age) as TextInputEditText
        val min_age = layout.findViewById<View>(R.id.min_age) as TextInputEditText

        if (clear) {
            spinner.setSelection(0)
            sexGroup.check(R.id.noSelect)
            max_age.setText("")
            min_age.setText("")
        }

        val maxAge: Int = if (max_age.text.toString() != "") {
            AddMember().getValue(max_age)
        } else {
            1000
        }
        val minAge: Int = if (min_age.text.toString() != "") {
            AddMember().getValue(min_age)
        } else {
            0
        }


        if (maxAge < minAge) {
            error_age_range.visibility = View.VISIBLE
            error_age_range.setText(R.string.range_error)
        } else {
            error_age_range.visibility = View.GONE

            if (sex == getString(R.string.all)) {
                sex = ""
            }

            if (belong == getString(R.string.no_selected)) {
                belongId = ""
            } else {
                for (j in 0 until FragmentGroup.ListCount) {
                    val listItem = FragmentGroup.groupList[j]
                    groupName = listItem.group
                    if (belong == groupName) {
                        belongId = listItem.id.toString()
                    }
                }
            }

            dbAdapter.filterName(sex, minAge, maxAge, belongId)
        }

    }

    fun filtering(builder: androidx.appcompat.app.AlertDialog.Builder) {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.filter_member, activity!!.findViewById<View>(R.id.filter_member) as? ViewGroup)
        val belongSpinner = layout.findViewById<View>(R.id.filter_belong_spinner) as Spinner
        val adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_item)
        val list = ArrayList<String>() // 新インスタンスを生成
        list.add(getString(R.string.no_selected))

        for (j in 0 until FragmentGroup.ListCount) {
            val listItem = FragmentGroup.groupList[j]
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
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        builder.setNeutralButton(R.string.clear, null)
        // back keyを使用不可に設定
        builder.setCancelable(false)
        val dialog2 = builder.create()
        dialog2.show()

        val okButton = dialog2.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            filter(layout, belongSpinner, false)
            dialog2.dismiss()
        }

        val cancelBtn = dialog2.getButton(AlertDialog.BUTTON_NEUTRAL)
        cancelBtn.setOnClickListener {
            filter(layout, belongSpinner, true)
            dbAdapter.sortNames(MemberListAdapter.MB_ID, "ASC")
            NameListAdapter.nowSort = "ID"
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    internal inner class CallbackMB : AbsListView.MultiChoiceModeListener {

        private val booleanArray = lv.checkedItemPositions

        private val decisionClicked = View.OnClickListener {
            listCount = lv.count

            if (!MemberMain.kumiwake_select) {//AddGroupのメンバー選択

                dbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < listCount) {
                    val checked = booleanArray.get(i)
                    listItem = nameList[i]
                    val listId = listItem.id
                    val newId = MemberMain.groupId
                    if (checked) {
                        val newBelong = StringBuilder()
                        newBelong.append(listItem.belong)
                        newBelong.append(",$newId")
                        dbAdapter.addBelong(listId.toString(), newBelong.toString())
                    } else {
                        deleteBelongInfo(newId, listId)
                    }
                    i += 2
                }
                dbAdapter.close()    // DBを閉じる
                loadName()

            } else {
                //normalModeのメンバー選択
                memberArray.clear()
                dbAdapter.open()
                var i = 1
                while (i < lv.count) {
                    val checked = booleanArray.get(i)
                    listItem = nameList[i]
                    if (checked && listItem.sex != "initial") {
                        memberArray.add(listItem)
                    }
                    i += 2
                }
                dbAdapter.close()

                val intent = Intent(activity, NormalMode::class.java)
                intent.putExtra(NormalMode.MEMBER_ARRAY, memberArray)
                requireActivity().setResult(Activity.RESULT_OK, intent)
            }

            requireActivity().finish()
            listAdp.clearSelection()
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理

            val inflater = activity!!.menuInflater
            inflater.inflate(R.menu.member_main_menu, menu)
            val searchIcon = menu.findItem(R.id.search_view)
            val deleteIcon = menu.findItem(R.id.item_delete)
            MemberMain.decision.setOnClickListener(decisionClicked)
            searchIcon.isVisible = false
            deleteIcon.isVisible = MemberMain.delete_icon_visible
            listCount = lv.count
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
            listCount = lv.count

            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_delete -> deleteMember()

                R.id.item_all_select -> {
                    var i = 1
                    while (i < listCount) {
                        lv.setItemChecked(i, true)
                        i += 2
                    }
                }

                R.id.item_sort -> {
                    Sort.memberSort(builder, requireActivity())
                    val dialog = builder.create()
                    dialog.show()
                }

                R.id.item_filter -> {
                    filtering(builder)
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

            checkedCount = lv.checkedItemCount

            if (checked) {
                listAdp.setNewSelection(position, checked)
            } else {
                listAdp.removeSelection(position)
            }

            mode.title = checkedCount.toString() + getString(R.string.selected)
        }

        private fun deleteMember() {
            // アラートダイアログ表示
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
            builder.setTitle(checkedCount.toString() + " " + getString(R.string.member) + getString(R.string.delete))
            builder.setMessage(R.string.Do_delete)
            // OKの時の処理
            builder.setPositiveButton("OK") { _, _ ->
                dbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < listCount) {
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
                loadName()
                listCount = lv.count
            }

            builder.setNegativeButton(R.string.cancel) { _, _ -> }
            val dialog = builder.create()
            dialog.show()

            lv.isTextFilterEnabled = true
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

    fun duplicateBelong() {
        dbAdapter.open()
        var i = 1
        while (i < listCount) {
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
    }

    fun deleteBelongInfoAll(groupId: Int) {
        dbAdapter.open()
        for (i in 1 until listCount) {
            listItem = nameList[i]
            val listId = listItem.id
            deleteBelongInfo(groupId, listId)
        }
        dbAdapter.close()
    }

    fun deleteBelongInfo(groupId: Int, listId: Int) {
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
        while (i < listCount) {
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
        return memberArrayByBelong
    }

    fun checkByGroup(groupId: Int) {
        var i = 1
        while (i < listCount) {
            listItem = nameList[i]
            val belongText = listItem.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val list = ArrayList(Arrays.asList<String>(*belongArray))
            if (list.contains(groupId.toString())) {
                lv.setItemChecked(i, true)
            }
            i += 2
        }
    }

    fun loadName() {
        dbAdapter.open()
        val c = dbAdapter.allNames
        dbAdapter.getCursor(c)
        dbAdapter.close()
        dbAdapter.notifyDataSetChanged()
    }

    companion object {
        internal lateinit var dbAdapter: MemberListAdapter
        lateinit var listAdp: NameListAdapter
        private lateinit var lv: ListView
        var nameList: ArrayList<Name> = ArrayList()
        internal lateinit var listItem: Name
        internal lateinit var fab: FloatingActionButton
        internal var checkedCount = 0
        private var listCount = 0
    }

}






