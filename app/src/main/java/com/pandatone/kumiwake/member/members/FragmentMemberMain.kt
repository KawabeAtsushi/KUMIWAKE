package com.pandatone.kumiwake.member.members

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.ListFragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.pandatone.kumiwake.AddMemberKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.adapter.MemberFragmentViewAdapter
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.function.*
import java.io.IOException


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentMemberMain : ListFragment() {

    private lateinit var mbAdapter: MemberAdapter
    private lateinit var listAdp: MemberFragmentViewAdapter
    private lateinit var lv: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbAdapter = MemberAdapter(requireContext())
        memberList = mbAdapter.getAllMembers()
        listAdp = MemberFragmentViewAdapter(requireContext(), memberList)
        StatusHolder.mbNowSort = MemberAdapter.MB_ID
        StatusHolder.mbSortType = "ASC"
        Sort.initial = 0
        fragmentMemberMain = this
    }

    override fun onStart() {
        super.onStart()
        loadName()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.tab_member, container, false)
        (view.findViewById<View>(R.id.member_fab) as FloatingActionButton).setOnClickListener {
            moveAddMember(
                null
            )
        }

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
            val builder = AlertDialog.Builder(requireActivity())
            val builder2 = AlertDialog.Builder(requireActivity())
            val inflater =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view2 = inflater.inflate(
                R.layout.member_info,
                requireActivity().findViewById<View>(R.id.info_layout) as ViewGroup?
            )
            val memberName = memberList[position].name
            FragmentGroupMain.fragmentGroupMain.loadName()

            val items = arrayOf(
                getText(R.string.information),
                getText(R.string.edit),
                getText(R.string.delete)
            )
            builder.setTitle(memberName)
            builder.setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        MemberClick.memberInfoDialog(view2, builder2)
                        MemberClick.setInfo(requireContext(), memberList[position])
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
            lv.setItemChecked(position, !listAdp.isMemberChecked(memberList[position].id))
            false
        }

        lv.isTextFilterEnabled = true
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- Menuの処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

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

    //単一メンバー削除
    private fun deleteSingleMember(position: Int, name: String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(name)
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { _, _ ->
            val member: Member = memberList[position]
            val listId = member.id
            mbAdapter.selectDelete(listId.toString())
            loadName()
            MemberMethods.updateBelongNo(requireContext())
            FragmentGroupMain.fragmentGroupMain.loadName()
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
        private lateinit var listener: ViewPager.SimpleOnPageChangeListener
        private lateinit var viewPager: ViewPager

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // アクションモード初期化処理
            val inflater = requireActivity().menuInflater
            inflater.inflate(R.menu.member_menu, menu)
            menu.findItem(R.id.search_view).isVisible = false
            menu.findItem(R.id.item_change_age).isVisible = true
            checkedCount = 0
            mode.title = checkedCount.toString() + getString(R.string.selected)
            viewPager = MembersMain.viewPager
            listener = object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    viewPager.currentItem = 0
                }
            }
            viewPager.addOnPageChangeListener(listener)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {

            // アクションアイテム選択時
            when (item.itemId) {
                R.id.item_delete -> {
                    deleteMultiMember(mode)
                }

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
                    Sort.memberSort(
                        requireActivity(),
                        memberList,
                        listAdp,
                        mbAdapter
                    ) { clearSelection(mode) }
                }

                R.id.item_filter -> {
                    Filtering(requireActivity(), mbAdapter, memberList).showFilterDialog(
                        requireActivity(),
                        listAdp
                    )
                }

                R.id.item_change_age -> {
                    changeAge()
                }
            }
            return false
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

        //選択状態が変更されたとき
        override fun onItemCheckedStateChanged(
            mode: ActionMode,
            position: Int, id: Long, checked: Boolean,
        ) {
            // アクションモード時のアイテムの選択状態変更時
            checkedCount = lv.checkedItemCount

            if (checkedCount == 0) {
                listAdp.setNewSelection(memberList[position].id, true)
                clearSelection(mode)
            } else {
                if (checked) {
                    listAdp.setNewSelection(memberList[position].id, checked)
                } else {
                    listAdp.removeSelection(memberList[position].id)
                }
            }

            mode.title = checkedCount.toString() + getString(R.string.selected)
        }

        //全選択解除
        private fun clearSelection(mode: ActionMode) {
            lv.clearChoices()
            listAdp.clearSelection()
            checkedCount = 0
            mode.title = "0" + getString(R.string.selected)
        }

        //複数メンバー削除
        private fun deleteMultiMember(mode: ActionMode) {
            // アラートダイアログ表示
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(
                checkedCount.toString() + " " + getString(R.string.member) + getString(
                    R.string.delete
                )
            )
            builder.setMessage(R.string.Do_delete)
            // OKの時の処理
            builder.setPositiveButton("OK") { _, _ ->
                val checkedMembers = getCheckedMemberList()
                checkedMembers.forEach { member ->
                    val listId = member.id
                    mbAdapter.selectDelete(listId.toString())     // DBから取得したIDが入っているデータを削除する
                }
                listAdp.clearSelection()
                loadName()
                MemberMethods.updateBelongNo(requireContext())
                FragmentGroupMain.fragmentGroupMain.loadName()
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
    fun searchMember(newText: String) {
        if (TextUtils.isEmpty(newText)) {
            mbAdapter.picName(null.toString(), memberList)
        } else {
            mbAdapter.picName(newText, memberList)
        }
        listAdp.notifyDataSetChanged()
    }


    //選択されているメンバーをリストで取得
    fun getCheckedMemberList(): ArrayList<Member> {
        val allMemberList: ArrayList<Member> = mbAdapter.getAllMembers()
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

    //年齢変更ボタンクリック
    fun changeAge() {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(
            R.layout.change_age_dialog,
            activity.findViewById<View>(R.id.change_age_dialog) as? ViewGroup
        )
        builder.setTitle(activity.getText(R.string.change_age))
        builder.setView(layout)
        builder.setPositiveButton("OK", null)
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        // back keyを使用不可に設定
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        val newAgeET = layout.findViewById<View>(R.id.specify_age) as TextInputEditText
        var method = ""
        val conditionGroup = layout.findViewById<View>(R.id.conditionGroup) as RadioGroup
        conditionGroup.setOnCheckedChangeListener { _, checkedId: Int ->
            when (checkedId) {
                R.id.define -> method = ""
                R.id.plus -> method = "+"
                R.id.decline -> method = "-"
            }
        }
        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            if (newAgeET.text!!.isNotEmpty()) {
                val newAge = Integer.parseInt(method + newAgeET.text.toString())
                val conditionButton =
                    layout.findViewById<View>(conditionGroup.checkedRadioButtonId) as RadioButton
                val define = (conditionButton.id == R.id.define)
                MemberMethods.updateAge(
                    requireContext(),
                    getCheckedMemberList(),
                    newAge,
                    define
                )     // 年齢更新
                loadName()
                dialog.dismiss()
                Toast.makeText(context, getText(R.string.updated_age), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, getText(R.string.error_empty_no), Toast.LENGTH_LONG).show()
            }
        }
    }

    //リスト表示更新
    fun loadName() {
        mbAdapter.open()
        val c = mbAdapter.getDB
        mbAdapter.getCursor(c, memberList, true)
        mbAdapter.close()
        listAdapter = listAdp
        mbAdapter.sortNames(StatusHolder.mbNowSort, StatusHolder.mbSortType, memberList)
        listAdp.notifyDataSetChanged()
    }

    companion object {
        internal lateinit var fragmentMemberMain: FragmentMemberMain
        internal var memberList: ArrayList<Member> = ArrayList() //searchMemberの関係
    }
}