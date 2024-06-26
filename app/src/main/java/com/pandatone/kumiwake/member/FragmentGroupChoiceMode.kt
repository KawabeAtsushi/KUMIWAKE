package com.pandatone.kumiwake.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.GroupFragmentViewAdapter
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Sort


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentGroupChoiceMode : ListFragment() {
    private var _gpAdapter: GroupAdapter? = null
    private val gpAdapter: GroupAdapter
        get() {
            if (_gpAdapter == null) {
                throw Exception("MemberFragmentViewAdapter not initialized")
            }
            return _gpAdapter!!
        }

    private var _listAdp: GroupFragmentViewAdapter? = null
    private val listAdp: GroupFragmentViewAdapter
        get() {
            if (_listAdp == null) {
                throw Exception("MemberFragmentView ListAdapter not initialized")
            }
            return _listAdp!!
        }
    private var groupList: ArrayList<Group> = ArrayList()

    // 必須*
    // Fragment生成時にシステムが呼び出す
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        StatusHolder.gpNowSort = GroupAdapter.GP_ID
        StatusHolder.gpSortType = "ASC"
        fragmentGroupChoiceMode = this
    }

    private fun init() {
        _gpAdapter = GroupAdapter(requireContext())
        _listAdp = GroupFragmentViewAdapter(requireContext(), groupList)
    }

    override fun onStart() {
        super.onStart()
        loadName()
    }

    // 必須*
    // Fragmentが初めてUIを描画する時にシステムが呼び出す
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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

    //Activity生成後に呼ばれる
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            ChoiceMemberMain.viewPager.setCurrentItem(0, true)
            FragmentMemberChoiceMode.fragmentMemberChoiceMode?.checkByGroup(
                groupList[position].id
            )
        }
    }

    // アクションアイテム選択時
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.item_sort -> {
                Sort.groupSort(requireActivity(), groupList, listAdp, gpAdapter)
            }
        }
        return false
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

    fun groupSortFromMemberFragment() {
        // MemberFragmentから呼ぶ用
        Sort.groupSort(requireActivity(), groupList, listAdp, gpAdapter)
    }

    companion object {
        internal lateinit var fragmentGroupChoiceMode: FragmentGroupChoiceMode
        internal var groupList: ArrayList<Group> = ArrayList()
    }

}






