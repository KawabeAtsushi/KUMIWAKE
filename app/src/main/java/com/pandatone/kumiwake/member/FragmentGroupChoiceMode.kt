package com.pandatone.kumiwake.member

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.GroupNameListAdapter
import com.pandatone.kumiwake.ui.members.AddGroup
import com.pandatone.kumiwake.ui.members.GroupClick
import java.io.IOException



/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentGroupChoiceMode : ListFragment() {
    private lateinit var listItem: GroupListAdapter.Group
    private var checkedCount = 0

    // 必須*
    // Fragment生成時にシステムが呼び出す
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = GroupListAdapter(requireContext())
        groupList = ArrayList()
        listAdp = GroupNameListAdapter(requireContext(), groupList)
    }

    // 必須*
    // Fragmentが初めてUIを描画する時にシステムが呼び出す
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_group, container, false)
        adviceInFG = view.findViewById<View>(R.id.advice_in_fg) as TextView
        fab = view.findViewById<View>(R.id.group_fab) as FloatingActionButton
        fab.setOnClickListener { moveAddGroup() }

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    private fun moveAddGroup() {
        val intent = Intent(activity, AddGroup::class.java)
        startActivity(intent)
    }

    // Viewの生成が完了した後に呼ばれる
    // UIパーツの設定などを行う
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView.isFastScrollEnabled = true

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            val builder = AlertDialog.Builder(activity!!)
            val builder2 = android.app.AlertDialog.Builder(activity)
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view2 = inflater.inflate(R.layout.group_info, activity!!.findViewById<View>(R.id.info_layout) as ViewGroup?)
            val groupName = groupList[position].group
            if (MemberMain.searchView.isActivated)
                MemberMain.searchView.onActionViewCollapsed()
            FragmentMemberChoiceMode().loadName()

            val items = arrayOf(getString(R.string.information), getString(R.string.edit), getString(R.string.delete))
            builder.setTitle(groupName)
            builder.setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        GroupClick.groupInfoDialog(view2, builder2)
                        GroupClick.setInfo(context!!, groupList[position],FragmentMemberChoiceMode().searchBelong(groupList[position].id.toString()))
                        val dialog2 = builder2.create()
                        dialog2.show()
                        GroupClick.okBt.setOnClickListener { dialog2.dismiss() }
                    }
                    1 -> {
                        val i = Intent(activity, AddGroup::class.java)
                        i.putExtra(AddGroup.GROUP_ID, groupList[position].id)
                        startActivity(i)
                    }
                    2 -> deleteSingleGroup(position, groupName)
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        listView.isTextFilterEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // アクションアイテム選択時
        when (item.itemId) {
            android.R.id.home -> activity!!.finish()

            R.id.item_all_select -> for (i in 0 until listAdp.count) {
                listView.setItemChecked(i, true)
            }

            R.id.item_sort -> {
                val builder = AlertDialog.Builder(activity!!)
                Sort.groupSort(builder, activity!!)
                val dialog = builder.create()
                dialog.show()
            }
        }

        return false
    }

    //Activity生成後に呼ばれる
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                MemberMain.viewPager.setCurrentItem(0, true)
                FragmentMemberChoiceMode().checkByGroup(groupList[position].id)
            }
    }

    override fun onStart() {
        super.onStart()
        loadName()
        FragmentMemberChoiceMode().loadName()
    }

    private fun deleteSingleGroup(position: Int, group: String) {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(group)
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { _, _ ->
            dbAdapter.open()
            listItem = groupList[position]
            val listId = listItem.id
            dbAdapter.selectDelete(listId.toString())
            dbAdapter.close()    // DBを閉じる
            FragmentMemberChoiceMode().loadName()
            loadName()
        }

        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////


    private inner class Callback : AbsListView.MultiChoiceModeListener {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理
            val inflater = activity!!.menuInflater
            inflater.inflate(R.menu.member_menu, menu)
            menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.getItem(3).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            val searchIcon = menu.findItem(R.id.search_view)
            val deleteIcon = menu.findItem(R.id.item_delete)
            val itemFilter = menu.findItem(R.id.item_filter)
            val allSelect = menu.findItem(R.id.item_all_select)
            itemFilter.isVisible = false
            searchIcon.isVisible = false
            deleteIcon.isVisible = false
            allSelect.isVisible = false

            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            // アクションアイテム選択時
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
            listView.checkedItemPosition

            if (checked) {
                listAdp.setNewSelection(groupList[position].id, checked)
            } else {
                listAdp.removeSelection(groupList[position].id)
            }
            mode.title = checkedCount.toString() + getString(R.string.selected)
        }

    }

    @Throws(IOException::class)
    fun selectGroup(newText: String) {
        if (TextUtils.isEmpty(newText)) {
            dbAdapter.picGroup(null.toString(), null.toString())
        } else {
            dbAdapter.picGroup(newText, newText)
        }

    }

    fun loadName() {
        dbAdapter.open()
        val c = dbAdapter.getDB
        dbAdapter.getCursor(c, groupList)
        dbAdapter.close()
        listAdapter = listAdp
        listAdp.notifyDataSetChanged()
    }

    companion object {
        internal lateinit var listAdp: GroupNameListAdapter
        internal lateinit var dbAdapter: GroupListAdapter
        internal var groupList: ArrayList<GroupListAdapter.Group> = ArrayList()
        internal lateinit var fab: FloatingActionButton
        internal lateinit var adviceInFG: TextView
    }

}






