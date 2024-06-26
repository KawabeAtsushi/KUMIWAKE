package com.pandatone.kumiwake.member.members

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.ListFragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.AddGroupKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.GroupFragmentViewAdapter
import com.pandatone.kumiwake.member.AddGroup
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.GroupClick
import com.pandatone.kumiwake.member.function.GroupMethods
import com.pandatone.kumiwake.member.function.Sort
import java.io.IOException


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentGroupMain : ListFragment() {
    private var checkedCount = 0

    private lateinit var listAdp: GroupFragmentViewAdapter
    private lateinit var gpAdapter: GroupAdapter

    // 必須*
    // Fragment生成時にシステムが呼び出す
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpAdapter = GroupAdapter(requireContext())
        listAdp = GroupFragmentViewAdapter(requireContext(), groupList)
        fragmentGroupMain = this
        StatusHolder.gpNowSort = GroupAdapter.GP_ID
        StatusHolder.gpSortType = "ASC"
    }

    // 必須*
    // Fragmentが初めてUIを描画する時にシステムが呼び出す
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.tab_group, container, false)
        (view.findViewById<View>(R.id.group_fab) as FloatingActionButton).setOnClickListener { moveAddGroup() }

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    override fun onStart() {
        super.onStart()
        listAdp = GroupFragmentViewAdapter(
            requireContext(),
            groupList
        ) //notifyDataSet..ではメンバー追加した際にグループ数が反映されなかったので
        loadName()
    }

    // Viewの生成が完了した後に呼ばれる
    // UIパーツの設定などを行う
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView.isFastScrollEnabled = true

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            val builder = AlertDialog.Builder(requireActivity())
            val groupName = groupList[position].name
            if (MembersMain.searchView.isActivated)
                MembersMain.searchView.onActionViewCollapsed()
            FragmentMemberMain.fragmentMemberMain.loadName()

            val items = arrayOf(
                getString(R.string.information),
                getString(R.string.edit),
                getString(R.string.delete)
            )
            builder.setTitle(groupName)
            builder.setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        GroupClick(requireActivity()).infoDialog(
                            groupList[position],
                            GroupMethods.searchBelong(
                                requireContext(),
                                groupList[position].id.toString()
                            )
                        )
                    }

                    1 -> {
                        val i = Intent(activity, AddGroup::class.java)
                        i.putExtra(AddGroupKeys.EDIT_ID.key, groupList[position].id)
                        startActivity(i)
                    }

                    2 -> deleteSingleGroup(position, groupName)
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(Callback())
        listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                listView.setItemChecked(
                    position,
                    !listAdp.isPositionChecked(groupList[position].id)
                )
                false
            }

        listView.isTextFilterEnabled = true
    }

    // アクションアイテム選択時
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> requireActivity().finish()

            R.id.item_all_select -> {
                for (i in 0 until listAdp.count) {
                    listView.setItemChecked(i, true)
                }
            }

            R.id.item_sort -> {
                Sort.groupSort(requireActivity(), groupList, listAdp, gpAdapter)
            }
        }

        return false
    }

    //グループ登録画面に遷移
    private fun moveAddGroup() {
        val intent = Intent(activity, AddGroup::class.java)
        startActivity(intent)
    }

    //１つグループ削除
    private fun deleteSingleGroup(position: Int, group: String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(group)
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { _, _ ->
            val groupId = groupList[position].id
            GroupMethods.deleteBelongInfoAll(requireContext(), groupId)
            gpAdapter.selectDelete(groupId.toString())
            FragmentMemberMain.fragmentMemberMain.loadName()
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

        private lateinit var listener: ViewPager.SimpleOnPageChangeListener
        private lateinit var viewPager: ViewPager

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理
            val inflater = requireActivity().menuInflater
            inflater.inflate(R.menu.member_menu, menu)
            menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.getItem(3).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.findItem(R.id.search_view).isVisible = false
            menu.findItem(R.id.item_delete).isVisible = true
            menu.findItem(R.id.item_filter).isVisible = false
            menu.findItem(R.id.item_all_select).isVisible = true
            viewPager = MembersMain.viewPager
            listener = object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    viewPager.currentItem = 1
                }
            }
            viewPager.addOnPageChangeListener(listener)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_delete -> {
                    deleteMultiGroup(mode)
                }

                R.id.item_all_select -> {
                    if (checkedCount >= listAdp.count / 2) {
                        clearSelection(mode)
                    } else {
                        for (i in 0 until listAdp.count) {
                            listView.setItemChecked(i, true)
                        }
                    }
                }

                R.id.item_sort -> {
                    listView.clearChoices()
                    listAdp.clearSelection()
                    mode.title = "0" + getString(R.string.selected)
                    Sort.groupSort(requireActivity(), groupList, listAdp, gpAdapter)
                }
            }
            return false
        }

        //全選択解除
        private fun clearSelection(mode: ActionMode) {
            listView.clearChoices()
            listAdp.clearSelection()
            checkedCount = 0
            mode.title = "0" + getString(R.string.selected)
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            // アクションモード終了時
            listAdp.clearSelection()
            viewPager.removeOnPageChangeListener(listener)
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード表示事前処理
            return true
        }

        override fun onItemCheckedStateChanged(
            mode: ActionMode,
            position: Int, id: Long, checked: Boolean,
        ) {
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

    //複数グループ削除
    fun deleteMultiGroup(mode: ActionMode) {

        // アラートダイアログ表示
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(checkedCount.toString() + " " + getString(R.string.group) + getString(R.string.delete))
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { _, _ ->
            val list = listView.checkedItemPositions
            for (i in 0 until listAdp.count) {
                val checked = list.get(i)
                if (checked) {
                    // IDを取得する
                    val groupId = groupList[i].id
                    GroupMethods.deleteBelongInfoAll(requireContext(), groupId)
                    gpAdapter.selectDelete(groupId.toString())     // DBから取得したIDが入っているデータを削除する
                }
            }
            listAdp.clearSelection()
            FragmentMemberMain.fragmentMemberMain.loadName()
            loadName()
            mode.finish()
        }

        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        val dialog = builder.create()
        dialog.show()

        listView.isTextFilterEnabled = true
    }

    //検索した時のグループ絞り込み
    @Throws(IOException::class)
    fun searchGroup(newText: String) {
        if (TextUtils.isEmpty(newText)) {
            gpAdapter.picGroup(null.toString(), groupList)
        } else {
            gpAdapter.picGroup(newText, groupList)
        }
        listAdp.notifyDataSetChanged()
    }

    //リスト表示更新
    fun loadName() {
        gpAdapter.open()
        val c = gpAdapter.getDB
        gpAdapter.getCursor(c, groupList)
        gpAdapter.close()
        listAdapter = listAdp
        gpAdapter.sortGroups(StatusHolder.gpNowSort, StatusHolder.gpSortType, groupList)
        listAdp.notifyDataSetChanged()
    }

    companion object {
        internal lateinit var fragmentGroupMain: FragmentGroupMain
        internal var groupList: ArrayList<Group> = ArrayList()
    }

}






