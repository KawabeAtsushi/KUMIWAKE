package com.pandatone.kumiwake.member

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ListFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.GroupNameListAdapter
import java.io.IOException
import java.util.*

/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentGroup : ListFragment() {
    private var parent = MemberMain()
    private lateinit var listItem: GroupListAdapter.Group
    private var checkedCount = 0

    // 必須*
    // Fragment生成時にシステムが呼び出す
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = GroupListAdapter(requireContext())
        nameList = ArrayList()
        listAdp = GroupNameListAdapter(requireContext(), nameList)
        listAdapter = listAdp;
        loadName()
    }

    // 必須*
    // Fragmentが初めてUIを描画する時にシステムが呼び出す
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_group, container, false)
        adviceInFG = view.findViewById<View>(R.id.advice_in_fg) as TextView
        fab = view.findViewById<View>(R.id.group_fab) as FloatingActionButton
        fab.setOnClickListener { parent.moveGroup() }

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }


    // Viewの生成が完了した後に呼ばれる
    // UIパーツの設定などを行う
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ListCount = listView.count
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(Callback())
        listView.isFastScrollEnabled = true
        ListCount = listView.count

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
            val builder2 = android.app.AlertDialog.Builder(activity)
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view2 = inflater.inflate(R.layout.group_info,
                    activity!!.findViewById<View>(R.id.info_layout) as ViewGroup)
            val groupname = nameList[position].group
            if (MemberMain.searchView.isActivated == true)
                MemberMain.searchView.onActionViewCollapsed()
            FragmentMember().loadName()

            val Items = arrayOf(Sort.name_getContext()!!.getString(R.string.information), Sort.name_getContext()!!.getString(R.string.edit), Sort.name_getContext()!!.getString(R.string.delete))
            builder.setTitle(groupname)
            builder.setItems(Items) { dialog, which ->
                when (which) {
                    0 -> {
                        GroupClick.GroupInfoDialog(view2, builder2)
                        GroupClick.SetInfo(position)
                        val dialog2 = builder2.create()
                        dialog2.show()
                        GroupClick.okBt.setOnClickListener { dialog2.dismiss() }
                    }
                    1 -> {
                        val i = Intent(activity, AddGroup::class.java)
                        i.putExtra("POSITION", position)
                        startActivity(i)
                    }
                    2 -> DeleteSingleGroup(position, groupname)
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        if (parent.start_actionmode) {
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                listView.startActionMode(Callback())
                listView.setItemChecked(position, !listAdp.isPositionChecked(position))
            }
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, id ->
            listView.setItemChecked(position, !listAdp.isPositionChecked(position))
            false
        }

        listView.isTextFilterEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // アクションアイテム選択時
        when (item!!.itemId) {
            android.R.id.home -> activity!!.finish()

            R.id.item_delete -> DeleteGroup()

            R.id.item_all_select -> for (i in 0 until ListCount) {
                listView.setItemChecked(i, true)
            }

            R.id.item_sort -> {
                val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
                Sort.groupSort(builder)
                val dialog = builder.create()
                dialog.show()
                ListCount = listView.count
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
    }

    fun DeleteSingleGroup(position: Int, group: String) {
        val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
        builder.setTitle(group)
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { dialog, which ->
            dbAdapter.open()
            listItem = nameList[position]
            val listId = listItem.id
            dbAdapter.selectDelete(listId.toString())
            dbAdapter.close()    // DBを閉じる
            loadName()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, which -> }
        val dialog = builder.create()
        dialog.show()
    }

    fun DeleteGroup() {
        Log.d("DeleteGroup is called.", "hooo")
        // アラートダイアログ表示
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(checkedCount.toString() + " " + getString(R.string.group) + getString(R.string.delete))
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { dialog, which ->
            val list = listView.checkedItemPositions

            dbAdapter.open()     // DBの読み込み(読み書きの方)
            for (i in 0 until ListCount) {
                val checked = list.get(i)
                if (checked == true) {
                    // IDを取得する
                    listItem = nameList[i]
                    val listId = listItem.id
                    dbAdapter.selectDelete(listId.toString())     // DBから取得したIDが入っているデータを削除する
                    FragmentMember().DeleteBelongInfoAll(listId)
                }
            }
            dbAdapter.close()    // DBを閉じる
            listAdp.clearSelection()
            listView.choiceMode = ListView.CHOICE_MODE_NONE
            listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            loadName()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, which -> }
        val dialog = builder.create()
        dialog.show()

        listView.isTextFilterEnabled = true
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////


    private inner class Callback : AbsListView.MultiChoiceModeListener {

        internal val list = listView.checkedItemPositions

        private val decision_clicked = View.OnClickListener {
            dbAdapter.open()     // DBの読み込み(読み書きの方)
            if (!parent.kumiwake_select) {
                for (i in 0 until ListCount) {
                    val checked = list.get(i)
                    if (checked == true) {
                        listItem = nameList[i]
                        val myId = listItem.id

                        val newId = parent.groupId
                        FragmentMember().addGroupByGroup(newId, myId)
                    }
                }
                parent.finish()
            } else {

                for (i in 0 until ListCount) {
                    val checked = list.get(i)
                    if (checked) {
                        listItem = nameList[i]
                        val myId = listItem.id
                        FragmentMember().createKumiwakeListByGroup(myId)
                    }
                }
                parent.moveKumiwake()
            }
            listAdp.clearSelection()
            dbAdapter.close()
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理
            val inflater = activity!!.menuInflater
            inflater.inflate(R.menu.member_main_menu, menu)
            menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu.getItem(3).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            parent.decision.setOnClickListener(decision_clicked)
            val searchIcon = menu.findItem(R.id.search_view)
            val deleteIcon = menu.findItem(R.id.item_delete)
            val itemfilter = menu.findItem(R.id.item_filter)
            itemfilter.isVisible = false
            searchIcon.isVisible = false
            deleteIcon.isVisible = parent.delete_icon_visible
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_delete -> DeleteGroup()

                R.id.item_all_select -> for (i in 0 until ListCount) {
                    listView.setItemChecked(i, true)
                }

                R.id.item_sort -> {
                    val builder = android.support.v7.app.AlertDialog.Builder(activity!!)
                    Sort.groupSort(builder)
                    val dialog = builder.create()
                    dialog.show()
                    ListCount = listView.count
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

    }

    fun loadName() {
        nameList.clear()
        dbAdapter.open()
        val c = dbAdapter.allNames
        dbAdapter.getCursor(c)
        dbAdapter.close()
    }

    companion object {
        internal lateinit var listAdp: GroupNameListAdapter
        internal lateinit var dbAdapter: GroupListAdapter
        internal lateinit var nameList: MutableList<GroupListAdapter.Group>
        internal lateinit var fab: FloatingActionButton
        internal lateinit var adviceInFG: TextView
        internal var ListCount: Int = 0

        @Throws(IOException::class)
        fun selectGroup(newText: String) {
            if (TextUtils.isEmpty(newText)) {
                dbAdapter.picGroup(null.toString(), null.toString())
            } else {
                dbAdapter.picGroup(newText, newText)
            }

        }
    }

}






