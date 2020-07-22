package com.pandatone.kumiwake.setting

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.PublicMethods.setStatus
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.Theme
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


class Help : AppCompatActivity() {

    private lateinit var howToUseAdapter: ArrayAdapter<String>
    private lateinit var howToUseStr: Array<String>
    val dialog: DialogWarehouse
        get() {
            return DialogWarehouse(supportFragmentManager)
        }
    private lateinit var howToUseList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.SettingsTheme)
        setContentView(R.layout.help)
        setStatus(this, Theme.Setting.primaryColor)
        setTitle(R.string.help)

        howToUseList = findViewById(R.id.how_to_use_list)
        howToUseList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            val homepageLink = PublicMethods.getLinkChar(getString(R.string.url_homepage), getString(R.string.more_details))
            when (position) {
                0 -> dialog.confirmationDialog(howToUseStr[0], getString(R.string.how_to_kumiwake), homepageLink)
                1 -> dialog.confirmationDialog(howToUseStr[1], getText(R.string.how_to_sekigime), homepageLink)
                2 -> dialog.confirmationDialog(howToUseStr[2], getText(R.string.description_of_normal), homepageLink)
                3 -> dialog.confirmationDialog(howToUseStr[3], getText(R.string.description_of_quick), homepageLink)
                4 -> dialog.confirmationDialog(howToUseStr[4], getText(R.string.how_to_member), homepageLink)
                5 -> PublicMethods.toWebSite(this, supportFragmentManager)
            }
        }

        setViews()
    }

    private fun setViews() {

        howToUseStr = arrayOf(getString(R.string.about_kumiwake), getString(R.string.about_sekigime), getString(R.string.normal_mode), getString(R.string.quick_mode), getString(R.string.about_member), getString(R.string.detail_help))
        howToUseAdapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, howToUseStr)

        howToUseList.adapter = howToUseAdapter
    }

}