package com.pandatone.kumiwake.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.ListFragment
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.member.function.Sort
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class FragmentHistory : ListFragment() {

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
    }

    override fun onStart() {
        super.onStart()
        activity?.actionBar?.title = activity?.actionBar?.title + historyList
        loadName()
    }

    // 必須*
    // Fragmentが初めてUIを描画する時にシステムが呼び出す
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_history, container, false)

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
            HistoryInfo(activity!!).infoDialog(historyList[position])
        }
    }

    // アクションアイテム選択時
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_help -> dialog.confirmationDialog(getString(R.string.history), getString(R.string.how_to_history))
        }
        return false
    }

    //リスト表示更新
    fun loadName() {
        hsAdapter.open()
        val c = hsAdapter.getDB
        hsAdapter.getCursor(c, historyList)
        hsAdapter.close()
        listAdapter = listAdp
        listAdp.notifyDataSetChanged()
    }

    companion object {
        //最初から存在してほしいのでprivateのcompanionにする（じゃないと落ちる。コルーチンとか使えばいけるかも）
        private lateinit var hsAdapter: HistoryAdapter
        internal lateinit var listAdp: HistoryFragmentViewAdapter
        internal var historyList: ArrayList<History> = ArrayList()
    }

}






