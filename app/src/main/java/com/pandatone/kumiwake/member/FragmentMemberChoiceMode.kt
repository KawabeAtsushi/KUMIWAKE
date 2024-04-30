package com.pandatone.kumiwake.member

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
class FragmentMemberChoiceMode : ListFragment() {
    private var _lv: ListView? = null
    private val lv: ListView
        get() {
            if (_lv == null) {
                throw Exception("listview not initialized")
            }
            return _lv!!
        }

    private var _mbAdapter: MemberAdapter? = null
    private val mbAdapter: MemberAdapter
        get() {
            if (_mbAdapter == null) {
                throw Exception("mbAdapter not initialized")
            }
            return _mbAdapter!!
        }

    private var _listAdp: MemberFragmentViewAdapter? = null
    private val listAdp: MemberFragmentViewAdapter
        get() {
            if (_listAdp == null) {
                throw Exception("MemberFragmentViewAdapter not initialized")
            }
            return _listAdp!!
        }
    private var memberArray = ChoiceMemberMain.memberArray

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.tab_member, container, false)
        val fab = view.findViewById<View>(R.id.member_fab) as FloatingActionButton
        fab.hide()
        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initListView()
        fragmentMemberChoiceMode = this
    }

    private fun init() {
        _mbAdapter = MemberAdapter(requireContext())
        memberList = _mbAdapter!!.getAllMembers()
        _listAdp = MemberFragmentViewAdapter(requireContext(), memberList)
        StatusHolder.mbNowSort = MemberAdapter.MB_ID
        StatusHolder.mbSortType = "ASC"
        Sort.initial = 0
        loadName()
    }

    private fun initListView() {
        _lv = listView
        _lv!!.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        _lv!!.setMultiChoiceModeListener(CallbackMB())
        _lv!!.isFastScrollEnabled = true
        _lv!!.isTextFilterEnabled = true
    }

    override fun onStart() {
        super.onStart()
        loadName()
        val toolbar = requireActivity().findViewById<View>(R.id.toolbar2) as Toolbar
        toolbar.startActionMode(CallbackMB())
        //初期の選択済みのメンバーをチェックする
        for (member in memberArray) {
            var i = 1
            while (i < listAdp.count) {
                val listItem: Member = memberList[i]
                if (listItem.id == member.id) {
                    lv.setItemChecked(i, !listAdp.isMemberChecked(listItem.id))
                }
                i += 2
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // アクションアイテム選択時
        when (item.itemId) {
            android.R.id.home -> requireActivity().finish()

            R.id.item_all_select -> {
                var i = 1
                while (i < listAdp.count) {
                    lv.setItemChecked(i, true)
                    i += 2
                }
            }

            R.id.item_sort -> {
                Sort.memberSort(requireActivity(), memberList, listAdp, mbAdapter)
            }

            R.id.item_filter -> {
                Filtering(requireActivity(), mbAdapter, memberList).showFilterDialog(
                    requireActivity(),
                    listAdp
                )
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
            val intent = Intent()
            intent.putExtra(AddGroupKeys.MEMBER_ARRAY.key, getCheckedMemberList())
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
            listAdp.clearSelection()
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理
            val inflater = requireActivity().menuInflater
            inflater.inflate(R.menu.member_menu, menu)
            val searchIcon = menu.findItem(R.id.search_view)
            val deleteIcon = menu.findItem(R.id.item_delete)
            ChoiceMemberMain.decision.setOnClickListener(decisionClicked)
            searchIcon.isVisible = false
            deleteIcon.isVisible = false
            checkedCount = lv.checkedItemCount
            mode.title = checkedCount.toString() + getString(R.string.selected)
            lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                val memberId = listAdp.getItem(position).id
                lv.setItemChecked(position, !listAdp.isMemberChecked(memberId))
            }

            ChoiceMemberMain.viewPager.addOnPageChangeListener(object :
                ViewPager.SimpleOnPageChangeListener() {
                override fun onPageScrollStateChanged(state: Int) {
                    page = ChoiceMemberMain.viewPager.currentItem
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
                    if (page == 0) {
                        Sort.memberSort(
                            requireActivity(),
                            memberList,
                            listAdp,
                            mbAdapter
                        ) { clearSelection(mode) }
                    } else {
                        FragmentGroupChoiceMode.fragmentGroupChoiceMode.groupSortFromMemberFragment()
                    }
                }

                R.id.item_filter -> {
                    Filtering(requireActivity(), mbAdapter, memberList).showFilterDialog(
                        requireActivity(),
                        listAdp
                    )
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

        override fun onItemCheckedStateChanged(
            mode: ActionMode,
            position: Int,
            id: Long,
            checked: Boolean,
        ) {
            // アクションモード時のアイテムの選択状態変更時(変更後)
            checkedCount = lv.checkedItemCount
            val member = memberList[position]

            if (checked) {
                listAdp.setNewSelection(member.id, true)
            } else {
                listAdp.removeSelection(member.id)
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

    //選択されているメンバーをリストで取得
    fun getCheckedMemberList(): ArrayList<Member> {
        val allMemberList: ArrayList<Member> = _mbAdapter!!.getAllMembers()
        val checkedMemberList = ArrayList<Member>()
        val booleanArray = lv.checkedItemPositions

        for (i in 0 until mbAdapter.count) {
            val memberIndex = i * 2 + 1
            val checked = booleanArray.get(memberIndex)
            val member: Member = allMemberList[i]
            if (checked && member.sex != StatusHolder.index) {
                checkedMemberList.add(member)
            }
        }

        return checkedMemberList
    }

    fun checkByGroup(groupId: Int) {
        var i = 1
        while (i < listAdp.count) {
            val member: Member = memberList[i]
            val belongText = member.belong
            val belongArray =
                belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
        var fragmentMemberChoiceMode: FragmentMemberChoiceMode? = null
        private var memberList: ArrayList<Member> = ArrayList()
    }

}