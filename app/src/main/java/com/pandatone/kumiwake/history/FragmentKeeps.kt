package com.pandatone.kumiwake.history

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.ListFragment
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.adapter.MemberFragmentViewAdapter
import com.pandatone.kumiwake.member.function.Filtering
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.member.function.Sort
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentKeeps : ListFragment() {

    private val dialog: DialogWarehouse
        get() {
            return DialogWarehouse(requireFragmentManager())
        }

    // 必須*
    // Fragment生成時にシステムが呼び出す
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hsAdapter = HistoryAdapter(requireContext())
        listAdp = HistoryFragmentViewAdapter(requireContext(), historyList)
        HistoryMethods.sortType = "ASC"
    }

    override fun onStart() {
        super.onStart()
        loadName()
        toolbarTitle = context?.getString(R.string.favorite) + " " + historyList.count().toString() + "♥s"
    }

    // 必須*
    // Fragmentが初めてUIを描画する時にシステムが呼び出す
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_favorite, container, false)
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
        //行をクリックした時の処理
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            HistoryInfo(activity!!).infoDialog(historyList[position])
        }
        //行をロングクリックした時の処理
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            HistoryAdapter(requireContext()).updateHistoryState(historyList[position],"",true)
            loadName()
            FragmentHistory().loadName()
            setToolbarTitle(requireContext())
            false
        }
    }

    // アクションアイテム選択時
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.item_sort -> {
                HistoryMethods.historySort(activity!!, historyList, listAdp)
                toFavoList()
            }
            R.id.menu_help -> dialog.confirmationDialog(getString(R.string.history), getString(R.string.how_to_history))
        }
        return false
    }

    //リスト表示更新
    fun loadName() {
        hsAdapter.open()
        val c = hsAdapter.getDB
        hsAdapter.getCursor(c, historyList)
        toFavoList()
        hsAdapter.close()
        listAdapter = listAdp
        listAdp.notifyDataSetChanged()
    }

    private fun toFavoList(){
        historyList.removeAll(){it.keep == -1}
    }

    fun setToolbarTitle(c:Context){
        toolbarTitle = c.getString(R.string.favorite) + " " + historyList.count().toString() + "♥s"
        HistoryMain.toolbar.title = toolbarTitle
    }

    companion object {
        //最初から存在してほしいのでprivateのcompanionにする（じゃないと落ちる。コルーチンとか使えばいけるかも）
        private lateinit var hsAdapter: HistoryAdapter
        internal lateinit var listAdp: HistoryFragmentViewAdapter
        internal var historyList: ArrayList<History> = ArrayList()
        internal var toolbarTitle = ""
    }
}