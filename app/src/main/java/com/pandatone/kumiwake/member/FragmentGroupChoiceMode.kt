package com.pandatone.kumiwake.member

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.GroupNameListAdapter
import java.io.IOException



/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentGroupChoiceMode : ListFragment() {
    private var checkedCount = 0
    private lateinit var dbAdapter: GroupListAdapter

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
        val adviceInFG = view.findViewById<View>(R.id.advice_in_fg) as TextView
        val fab = view.findViewById<View>(R.id.group_fab) as FloatingActionButton
        adviceInFG.visibility = View.VISIBLE
        fab.hide()

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    // Viewの生成が完了した後に呼ばれる
    // UIパーツの設定などを行う
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView.isFastScrollEnabled = true
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
        internal var groupList: ArrayList<GroupListAdapter.Group> = ArrayList()
    }

}






