package com.pandatone.kumiwake.ui.members

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandatone.kumiwake.AddMemberKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.MemberClick
import com.pandatone.kumiwake.member.Member
import com.pandatone.kumiwake.member.Sort
import java.io.IOException
import java.util.*


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMemberMain : ListFragment() {

    private lateinit var lv: ListView
    private var memberList: ArrayList<Member> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbAdapter = MemberListAdapter(memberList,requireContext())
        memberList = mbAdapter.getAllMembers()
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

    override fun onStart() {
        super.onStart()
        loadName()
        FragmentGroupMain().loadName()
        NameListAdapter.nowSort = MemberListAdapter.MB_ID
        NameListAdapter.sortType = "ASC"
        mbAdapter.sortNames(NameListAdapter.nowSort, NameListAdapter.sortType)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_member, container, false)
        (view.findViewById<View>(R.id.member_fab) as FloatingActionButton).setOnClickListener { moveAddMember(null) }

        // Fragmentとlayoutを紐付ける
        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    //メンバー登録画面に遷移
    private fun moveAddMember(member: Member?) {
        val intent = Intent(activity, AddMember::class.java)
        intent.putExtra(AddMemberKeys.MEMBER.key, member)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lv = listView
        lv.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        lv.setMultiChoiceModeListener(CallbackMB())
        lv.isFastScrollEnabled = true
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
            val builder2 = AlertDialog.Builder(activity)
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view2 = inflater.inflate(R.layout.member_info, activity!!.findViewById<View>(R.id.info_layout) as ViewGroup?)
            val memberName = memberList[position].name
            FragmentGroupMain().loadName()

            val items = arrayOf(getText(R.string.information), getText(R.string.edit), getText(R.string.delete))
            builder.setTitle(memberName)
            builder.setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        MemberClick.memberInfoDialog(view2, builder2)
                        MemberClick.setInfo(context!!, memberList[position], mbAdapter)
                        val dialog2 = builder2.create()
                        dialog2.show()
                        MemberClick.okBt.setOnClickListener { dialog2.dismiss() }
                    }
                    1 -> moveAddMember(memberList[position])
                    2 -> deleteSingleMember(position, memberName)
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        // 行を長押しした時の処理
        lv.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            lv.setItemChecked(position, !listAdp.isPositionChecked(memberList[position].id))
            false
        }

        lv.isTextFilterEnabled = true
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
                MembersMenuAction(activity!!,memberList,FragmentGroupMain.groupList).filtering(builder)
            }
        }

        return false
    }

    //単一メンバー削除
    private fun deleteSingleMember(position: Int, name: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        builder.setTitle(name)
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { _, _ ->
            mbAdapter.open()
            val member: Member = memberList[position]
            val listId = member.id
            mbAdapter.selectDelete(listId.toString())
            mbAdapter.close()    // DBを閉じる
            loadName()
            updateBelongNo()
            FragmentGroupMain().loadName()
        }

        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    internal inner class CallbackMB : AbsListView.MultiChoiceModeListener {

        private var checkedCount = 0

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理

            val inflater = activity!!.menuInflater
            inflater.inflate(R.menu.member_menu, menu)
            val searchIcon = menu.findItem(R.id.search_view)
            searchIcon.isVisible = false
            checkedCount = 0
            mode.title = checkedCount.toString() + getString(R.string.selected)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)

            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_delete -> {
                    deleteMultiMember(mode)
                }

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
                    MembersMenuAction(activity!!,memberList,FragmentGroupMain.groupList).filtering(builder)
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

        //複数メンバー削除
        private fun deleteMultiMember(mode: ActionMode) {
            // アラートダイアログ表示
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
            builder.setTitle(checkedCount.toString() + " " + getString(R.string.member) + getString(R.string.delete))
            builder.setMessage(R.string.Do_delete)
            // OKの時の処理
            builder.setPositiveButton("OK") { _, _ ->
                val booleanArray = lv.checkedItemPositions
                mbAdapter.open()     // DBの読み込み(読み書きの方)
                var i = 1
                while (i < listAdp.count) {
                    val checked = booleanArray.get(i)
                    if (checked) {
                        // IDを取得する
                        val member: Member = memberList[i]
                        val listId = member.id
                        mbAdapter.selectDelete(listId.toString())     // DBから取得したIDが入っているデータを削除する
                    }
                    i += 2
                }
                mbAdapter.close()    // DBを閉じる
                listAdp.clearSelection()
                loadName()
                updateBelongNo()
                FragmentGroupMain().loadName()
                mode.finish()
            }

            builder.setNegativeButton(R.string.cancel) { _, _ -> }
            val dialog = builder.create()
            dialog.show()

            lv.isTextFilterEnabled = true
        }
    }

    //メンバーの検索表示処理
    @Throws(IOException::class)
    fun selectName(newText: String) {
        if (TextUtils.isEmpty(newText)) {
            mbAdapter.picName(null.toString())
        } else {
            mbAdapter.picName(newText)
        }
        listAdp.notifyDataSetChanged()
    }

    //全てのメンバーからグループ(groupIdのグループ)を削除（グループ削除の際にコール）
    fun deleteBelongInfoAll(groupId: Int) {
        mbAdapter.open()
        memberList.forEach{member ->
            deleteBelongInfo(member, groupId, member.id)
        }
        mbAdapter.close()
    }

    //メンバー(member)の所属グループ(groupIdのグループ)を削除
    private fun deleteBelongInfo(member: Member, groupId: Int, listId: Int) {
        val belongText = member.belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val list = ArrayList(Arrays.asList<String>(*belongArray))
        val hs = HashSet<String>()
        hs.addAll(list)
        list.clear()
        list.addAll(hs)
        if (list.contains(groupId.toString())) {
            list.remove(groupId.toString())
            val newBelong = StringBuilder()
            for (item in list) {
                newBelong.append("$item,")
            }
            mbAdapter.addBelong(listId.toString(), newBelong.toString())
        }
    }

    //引数belongIdのグループに所属するメンバーリストを返す
    fun searchBelong(belongId: String): ArrayList<Member> {
        val memberArrayByBelong = ArrayList<Member>()
        mbAdapter.open()
        memberList.forEach{member ->
            val belongText = member.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (Arrays.asList<String>(*belongArray).contains(belongId)) {
                memberArrayByBelong.add(Member(member.id, member.name, member.sex, 0, 0, null.toString(), null.toString(), null.toString()))
            }
        }
        mbAdapter.close()
        return memberArrayByBelong
    }

    //Groupの所属人数データ更新
    fun updateBelongNo() {
        for (group in FragmentGroupMain.groupList) {
            val groupId = group.id.toString()
            var belongNo = 0
            memberList.forEach{member ->
                val belongArray = member.belong.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val list = ArrayList(Arrays.asList<String>(*belongArray))
                if (list.contains(groupId)) {
                    belongNo++
                }
            }
            FragmentGroupMain.gpAdapter.updateBelongNo(groupId, belongNo)
        }
    }

    //リスト表示更新
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