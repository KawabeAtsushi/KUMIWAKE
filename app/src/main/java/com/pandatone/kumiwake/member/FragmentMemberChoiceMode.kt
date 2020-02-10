package com.pandatone.kumiwake.member

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.AddGroupKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter
import com.pandatone.kumiwake.ui.members.MembersMenuAction
import java.io.IOException
import java.util.*


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMemberChoiceMode : ListFragment() {
    private var memberArray = MemberMain.memberArray
    private var memberList: ArrayList<Member> = ArrayList()
    private lateinit var lv: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbAdapter = MemberListAdapter(memberList,requireContext())
        listAdp = NameListAdapter(requireContext(), memberList)
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
            lv.setItemChecked(position, !listAdp.isPositionChecked(memberList[position].id))
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
        mbAdapter.sortNames(NameListAdapter.nowSort, NameListAdapter.sortType)

        lv.startActionMode(CallbackMB())

        for (member in memberArray) {
            var i = 1
            while (i < listAdp.count) {
                val listItem: Member = memberList[i]
                if (listItem.id == member.id) {
                    lv.setItemChecked(i, !listAdp.isPositionChecked(listItem.id))
                }
                i += 2
            }
        }

        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            lv.setItemChecked(position, !listAdp.isPositionChecked(memberList[position].id))
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
                MembersMenuAction(activity!!,memberList,FragmentGroupChoiceMode.groupList).filtering(builder)
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
            mbAdapter.open()
            var i = 1
            while (i < listAdp.count) {
                val checked = booleanArray.get(i)
                val member: Member = memberList[i]
                if (checked && member.sex != "Index") {
                    memberArray.add(member)
                }
                i += 2
            }
            mbAdapter.close()

            val intent = Intent()
            intent.putExtra(AddGroupKeys.MEMBER_ARRAY.key, memberArray)
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
                    MembersMenuAction(activity!!,memberList,FragmentGroupChoiceMode.groupList).filtering(builder)
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
                listAdp.setNewSelection(memberList[position].id, checked)
            } else {
                listAdp.removeSelection(memberList[position].id)
            }

            mode.title = checkedCount.toString() + getString(R.string.selected)
        }
    }

    @Throws(IOException::class)
    fun selectName(newText: String) {
        if (TextUtils.isEmpty(newText)) {
            mbAdapter.picName(null.toString())
        } else {
            mbAdapter.picName(newText)
        }
        listAdp.notifyDataSetChanged()
    }

    fun checkByGroup(groupId: Int) {
        var i = 1
        while (i < listAdp.count) {
            val member: Member = memberList[i]
            val belongText = member.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val list = ArrayList(Arrays.asList<String>(*belongArray))
            if (list.contains(groupId.toString())) {
                lv.setItemChecked(i, true)
            }
            i += 2
        }
    }

    fun loadName() {
        mbAdapter.open()
        val c = mbAdapter.getDB
        mbAdapter.getCursor(c, memberList, true)
        mbAdapter.close()
        listAdapter = listAdp
        listAdp.notifyDataSetChanged()
    }

    companion object{
        //最初から存在してほしいのでprivateのcompanionにする（じゃないと落ちる。コルーチンとか使えばいけるかも）
        private lateinit var mbAdapter: MemberListAdapter
        private lateinit var listAdp: NameListAdapter
    }

}