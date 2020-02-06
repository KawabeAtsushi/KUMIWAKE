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
import java.io.IOException
import java.util.*


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMemberChoiceMode : ListFragment() {
    private var memberArray = MemberMain.memberArray
    private lateinit var dbAdapter: MemberListAdapter
    private lateinit var listAdp: NameListAdapter
    private lateinit var lv: ListView
    private var nameList: ArrayList<Name> = ArrayList()

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
        val fab = view.findViewById<View>(R.id.member_fab) as FloatingActionButton
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

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    internal inner class CallbackMB : AbsListView.MultiChoiceModeListener {

        private var checkedCount = 0

        private val decisionClicked = View.OnClickListener {

            val booleanArray = lv.checkedItemPositions

            //メンバー決定
            memberArray.clear()
            dbAdapter.open()
            var i = 1
            while (i < listAdp.count) {
                val checked = booleanArray.get(i)
                val listItem: Name = nameList[i]
                if (checked && listItem.sex != "Index") {
                    memberArray.add(listItem)
                }
                i += 2
            }
            dbAdapter.close()

            val intent = Intent()
            intent.putExtra(NormalMode.MEMBER_ARRAY, memberArray)
            requireActivity().setResult(Activity.RESULT_OK, intent)
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
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)

            // アクションアイテム選択時
            when (item.itemId) {

                R.id.item_all_select -> {
                    var i = 1
                    while (i < listAdp.count) {
                        lv.setItemChecked(i, true)
                        i += 2
                    }
                }

                R.id.item_sort -> {
                    lv.clearChoices()
                    listAdp.clearSelection()
                    mode.title = "0" + getString(R.string.selected)
                    Sort.memberSort(builder, requireActivity(), listAdp)
                    val dialog = builder.create()
                    dialog.show()
                }

                R.id.item_filter -> {
                    lv.clearChoices()
                    listAdp.clearSelection()
                    mode.title = "0" + getString(R.string.selected)
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
        dbAdapter.getCursor(c, Companion.nameList, true)
        dbAdapter.close()
        listAdapter = listAdp
        listAdp.notifyDataSetChanged()
    }

    companion object {
        var nameList: ArrayList<Name> = ArrayList()
    }

}