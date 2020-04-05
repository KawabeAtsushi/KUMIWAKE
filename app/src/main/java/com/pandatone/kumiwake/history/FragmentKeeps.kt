package com.pandatone.kumiwake.history

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.ListFragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.AddGroupKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.adapter.MemberFragmentViewAdapter
import com.pandatone.kumiwake.member.function.Filtering
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.member.function.Sort


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentKeeps : ListFragment() {
    private var memberArray = HistoryMain.memberArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbAdapter = MemberAdapter(requireContext())
        memberList = mbAdapter.getAllMembers()
        listAdp = MemberFragmentViewAdapter(requireContext(), memberList)
        StatusHolder.mbNowSort = MemberAdapter.MB_ID
        StatusHolder.mbSortType = "ASC"
        Sort.initial = 0
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
        lv.isTextFilterEnabled = true

    }

    override fun onStart() {
        super.onStart()
        loadName()
        FragmentHistory().loadName()
        val toolbar = activity!!.findViewById<View>(R.id.toolbar2) as Toolbar
        toolbar.startActionMode(CallbackMB())
        //初期の選択済みのメンバーをチェックする
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

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
                Sort.memberSort(requireActivity(), memberList, listAdp)
            }

            R.id.item_filter -> {
                Filtering(requireActivity(), memberList).showFilterDialog(requireActivity(), listAdp)
            }
        }

        return false
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    internal inner class CallbackMB : AbsListView.MultiChoiceModeListener {

        private var checkedCount = 0
        private var page: Int = 0 //current Tab

        private val decisionClicked = View.OnClickListener {

            val booleanArray = lv.checkedItemPositions

            //メンバー決定
            memberArray.clear()
            var i = 1
            while (i < listAdp.count) {
                val checked = booleanArray.get(i)
                val member: Member = memberList[i]
                if (checked && member.sex != StatusHolder.index) {
                    memberArray.add(member)
                }
                i += 2
            }
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
            HistoryMain.decision.setOnClickListener(decisionClicked)
            searchIcon.isVisible = false
            deleteIcon.isVisible = false
            checkedCount = lv.checkedItemCount
            mode.title = checkedCount.toString() + getString(R.string.selected)
            lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                lv.setItemChecked(position, !listAdp.isPositionChecked(memberList[position].id))
            }

            HistoryMain.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageScrollStateChanged(state: Int) {
                    page = HistoryMain.viewPager.currentItem
                    val itemFilter = menu.findItem(R.id.item_filter)
                    itemFilter.isVisible = (page == 0)
                    val allSelect = menu.findItem(R.id.item_all_select)
                    allSelect.isVisible = (page == 0)
                }
            })

            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {

            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_all_select -> {
                    if (checkedCount >= listAdp.count / 2) {
                        clearSelection(mode)
                    } else {
                        var i = 1
                        while (i < listAdp.count) {
                            lv.setItemChecked(i, true)
                            i += 2
                        }
                    }
                }

                R.id.item_sort -> {
                    clearSelection(mode)
                    if (page == 0) {
                        Sort.memberSort(requireActivity(), memberList, listAdp)
                    } else {
                        Sort.groupSort(requireActivity(), FragmentHistory.groupList, FragmentHistory.listAdp)
                        FragmentHistory.listAdp.notifyDataSetChanged()
                    }
                }

                R.id.item_filter -> {
                    clearSelection(mode)
                    Filtering(activity!!, memberList).showFilterDialog(requireActivity(), listAdp)
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード表示事前処理
            return true
        }

        override fun onItemCheckedStateChanged(mode: ActionMode,
                                               position: Int, id: Long, checked: Boolean) {
            // アクションモード時のアイテムの選択状態変更時(変更後)
            checkedCount = lv.checkedItemCount

            if (checked) {
                listAdp.setNewSelection(memberList[position].id, checked)
            } else {
                listAdp.removeSelection(memberList[position].id)
            }
            mode.title = checkedCount.toString() + getString(R.string.selected)
        }

        private fun clearSelection(mode: ActionMode) {
            lv.clearChoices()
            listAdp.clearSelection()
            checkedCount = 0
            mode.title = "0" + getString(R.string.selected)
        }
    }

    fun checkByGroup(groupId: Int) {
        var i = 1
        while (i < listAdp.count) {
            val member: Member = memberList[i]
            val belongText = member.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val list = ArrayList(listOf(*belongArray))
            if (list.contains(groupId.toString())) {
                lv.setItemChecked(i, true)
            }
            i += 2
        }
    }

    private fun loadName() {
        mbAdapter.open()
        val c = mbAdapter.getDB
        mbAdapter.getCursor(c, memberList, true)
        mbAdapter.close()
        listAdapter = listAdp
        mbAdapter.sortNames(StatusHolder.mbNowSort, StatusHolder.mbSortType, memberList)
        listAdp.notifyDataSetChanged()
    }

    companion object {
        //最初から存在してほしいのでprivateのcompanionにする（じゃないと落ちる。コルーチンとか使えばいけるかも）
        private lateinit var mbAdapter: MemberAdapter
        private lateinit var listAdp: MemberFragmentViewAdapter
        lateinit var lv: ListView
        private var memberList: ArrayList<Member> = ArrayList()
    }

}