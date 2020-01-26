package com.pandatone.kumiwake.member

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.ui.members.AddMember
import java.io.IOException
import java.util.*


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMemberChoiceMode : ListFragment() {
    var memberArray = MemberMain.memberArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = MemberListAdapter(requireContext())
        nameList = ArrayList()
        listAdp = NameListAdapter(requireContext(), nameList)
        NameListAdapter.nowSort = MemberListAdapter.MB_ID
        NameListAdapter.sortType = "ASC"
        Sort.initial = 0
        loadName()
    }

    override fun onResume() {
        super.onResume()
        loadName()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_member, container, false)
        fab = view.findViewById<View>(R.id.member_fab) as FloatingActionButton
        fab.hide()

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lv = listView
        lv.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        lv.setMultiChoiceModeListener(CallbackMB())
        lv.isFastScrollEnabled = true

        // 行を長押しした時の処理
        lv.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            lv.setItemChecked(position, !listAdp.isPositionChecked(nameList[position].id))
            false
        }

        lv.isTextFilterEnabled = true
    }

    override fun onStart() {
        super.onStart()
        loadName()
        FragmentGroupChoiceMode().loadName()
        NameListAdapter.nowSort = MemberListAdapter.MB_ID
        NameListAdapter.sortType = "ASC"
        dbAdapter.sortNames(NameListAdapter.nowSort, NameListAdapter.sortType)

            lv.startActionMode(CallbackMB())

            if (MemberMain.kumiwake_select) {

                for (member in memberArray) {
                    var i = 1
                    while (i < listAdp.count) {
                        val listItem: Name = nameList[i]
                        if (listItem.id == member.id) {
                            lv.setItemChecked(i, !listAdp.isPositionChecked(listItem.id))
                        }
                        i += 2
                    }
                }
            } else {
                checkByGroup(MemberMain.groupId)
            }

            lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                lv.setItemChecked(position, !listAdp.isPositionChecked(nameList[position].id))
            }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- Menuの処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)

        // アクションアイテム選択時
        when (item.itemId) {
            android.R.id.home -> activity!!.finish()

            R.id.item_all_select -> {
                var i = 1
                while (i < listAdp.count) {
                    lv.setItemChecked(i, true)
                    i += 2
                }
            }

            R.id.item_sort -> {
                Sort.memberSort(builder, requireActivity(), listAdp)
                val dialog = builder.create()
                dialog.show()
            }

            R.id.item_filter -> {
                filtering(builder)
            }
        }

        return false
    }

    private fun filter(layout: View, spinner: Spinner, clear: Boolean) {

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
                for (listItem in FragmentGroupChoiceMode.groupList) {
                    if (belong == listItem.group) {
                        belongId = listItem.id.toString() + ","
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

        for (listItem in FragmentGroupChoiceMode.groupList) {
            list.add(listItem.group)
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
            NameListAdapter.nowSort = MemberListAdapter.MB_ID
            NameListAdapter.sortType = "ASC"
            dbAdapter.sortNames(NameListAdapter.nowSort, NameListAdapter.sortType)
            filter(layout, belongSpinner, false)
            listAdp.notifyDataSetChanged()
            dialog2.dismiss()
        }

        val clearBtn = dialog2.getButton(AlertDialog.BUTTON_NEUTRAL)
        clearBtn.setOnClickListener {
            filter(layout, belongSpinner, true)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    internal inner class CallbackMB : AbsListView.MultiChoiceModeListener {

        private val decisionClicked = View.OnClickListener {

            val booleanArray = lv.checkedItemPositions
            if (!MemberMain.kumiwake_select) {//AddGroupのメンバー選択

                dbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < listAdp.count) {
                    val checked = booleanArray.get(i)
                    val listItem: Name = nameList[i]
                    val listId = listItem.id
                    val newId = MemberMain.groupId
                    if (checked) {
                        val newBelong = StringBuilder()
                        newBelong.append(listItem.belong)
                        newBelong.append("$newId,")
                        dbAdapter.addBelong(listId.toString(), newBelong.toString())
                    } else {
                        deleteBelongInfo(listItem,newId, listId)
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
                while (i < listAdp.count) {
                    val checked = booleanArray.get(i)
                    val listItem: Name = nameList[i]
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
            inflater.inflate(R.menu.member_menu, menu)
            val searchIcon = menu.findItem(R.id.search_view)
            val deleteIcon = menu.findItem(R.id.item_delete)
            MemberMain.decision.setOnClickListener(decisionClicked)
            searchIcon.isVisible = false
            deleteIcon.isVisible = false
            checkedCount = lv.checkedItemCount
            mode.title = checkedCount.toString() + getString(R.string.selected)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
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
                listAdp.setNewSelection(nameList[position].id, checked)
            } else {
                listAdp.removeSelection(nameList[position].id)
            }

            mode.title = checkedCount.toString() + getString(R.string.selected)
        }
    }

    @Throws(IOException::class)
    fun selectName(newText: String) {
        if (TextUtils.isEmpty(newText)) {
            dbAdapter.picName(null.toString())
        } else {
            dbAdapter.picName(newText)
        }
        listAdp.notifyDataSetChanged()
    }

    fun deleteBelongInfo(listItem:Name,groupId: Int, listId: Int) {
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

            for (item in list) {
                newBelong.append("$item,")
            }
            dbAdapter.addBelong(listId.toString(), newBelong.toString())
        }
    }

    fun searchBelong(belongId: String): ArrayList<Name> {
        val memberArrayByBelong = ArrayList<Name>()
        dbAdapter.open()
        var i = 1
        while (i < listAdp.count) {
            val listItem: Name = nameList[i]
            val belongText = listItem.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (Arrays.asList<String>(*belongArray).contains(belongId)) {
                memberArrayByBelong.add(Name(listItem.id, listItem.name, listItem.sex, 0, 0, null.toString(), null.toString(), null.toString()))
            }
            i += 2
        }
        dbAdapter.close()
        return memberArrayByBelong
    }

    fun checkByGroup(groupId: Int) {
        var i = 1
        while (i < listAdp.count) {
            val listItem: Name = nameList[i]
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
        val c = dbAdapter.getDB
        dbAdapter.getCursor(c, nameList,true)
        dbAdapter.close()
        listAdapter = listAdp
        listAdp.notifyDataSetChanged()
    }

    companion object {
        internal lateinit var dbAdapter: MemberListAdapter
        lateinit var listAdp: NameListAdapter
        lateinit var lv: ListView
        var nameList: ArrayList<Name> = ArrayList()
        internal lateinit var fab: FloatingActionButton
        internal var checkedCount = 0
    }

}