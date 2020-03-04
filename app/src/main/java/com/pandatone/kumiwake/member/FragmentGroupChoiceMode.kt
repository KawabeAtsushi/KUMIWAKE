package com.pandatone.kumiwake.member

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.GroupFragmentViewAdapter
import com.pandatone.kumiwake.member.Function.Group
import com.pandatone.kumiwake.member.Function.MemberMain


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentGroupChoiceMode : ListFragment() {
    private var checkedCount = 0
    private var groupList: ArrayList<Group> = ArrayList()

    // 必須*
    // Fragment生成時にシステムが呼び出す
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpAdapter = GroupAdapter(requireContext())
        groupList = ArrayList()
        listAdp = GroupFragmentViewAdapter(requireContext(), groupList)
    }

    override fun onStart() {
        super.onStart()
        loadName()
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

    //Activity生成後に呼ばれる
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //行をクリックした時の処理
                MemberMain.viewPager.setCurrentItem(0, true)
                FragmentMemberChoiceMode().checkByGroup(groupList[position].id)
            }
    }

    //リスト表示更新
    fun loadName() {
        gpAdapter.open()
        val c = gpAdapter.getDB
        gpAdapter.getCursor(c, groupList)
        gpAdapter.close()
        listAdapter = listAdp
        listAdp.notifyDataSetChanged()
    }

    companion object {
        //最初から存在してほしいのでprivateのcompanionにする（じゃないと落ちる。コルーチンとか使えばいけるかも）
        private lateinit var gpAdapter: GroupAdapter
        private lateinit var listAdp: GroupFragmentViewAdapter
    }

}






