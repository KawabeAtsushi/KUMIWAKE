package com.pandatone.kumiwake.kumiwake.function

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.pandatone.kumiwake.R

/**
 * Created by atsushi_2 on 2024/04/30.
 */
object CustomResultDisplayStyle {
    fun createDisplayStyleDialog(
        activity: AppCompatActivity,
        sortType: KumiwakeComparator.SortType,
        showSexIcons: Boolean,
        showNumberIcons: Boolean,
        selectedTab: Int,
        onChangeValues: (showSexIcons: Boolean, showNumberIcons: Boolean, sortType: KumiwakeComparator.SortType, byGroup: Boolean) -> Unit,
    ) {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.result_display_style_dialog_layout,
            activity.findViewById<View>(R.id.info_layout) as ViewGroup?
        )

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText(R.string.by_group))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.by_member))
        tabLayout.selectTab(tabLayout.getTabAt(selectedTab))

        // アイコン表示
        val checkSexIcon = view.findViewById<CheckBox>(R.id.show_sex_icon_check)
        val checkNumberIcon = view.findViewById<CheckBox>(R.id.show_number_icon_check)
        checkSexIcon.isChecked = showSexIcons
        checkNumberIcon.isChecked = showNumberIcons

        // 並べ替え
        val sortByDefaultRadioButton = view.findViewById<RadioButton>(R.id.default_by_leader)
        val sortBySexRadioButton = view.findViewById<RadioButton>(R.id.sex_by_sex)
        val sortByNameRadioButton = view.findViewById<RadioButton>(R.id.name_asc)
        val sortByAgeRadioButton = view.findViewById<RadioButton>(R.id.age_asc)
        when (sortType) {
            KumiwakeComparator.SortType.SEX -> sortBySexRadioButton.isChecked = true
            KumiwakeComparator.SortType.NAME -> sortByNameRadioButton.isChecked = true
            KumiwakeComparator.SortType.AGE -> sortByAgeRadioButton.isChecked = true
            KumiwakeComparator.SortType.DEFAULT -> sortByDefaultRadioButton.isChecked = true
        }

        builder
            .setView(view)
            .setPositiveButton(R.string.change) { _, _ ->
                val showSexIconsVal = checkSexIcon.isChecked
                val showNumberIconsVal = checkNumberIcon.isChecked
                val sortTypeVal = if (sortBySexRadioButton.isChecked) {
                    KumiwakeComparator.SortType.SEX
                } else if (sortByNameRadioButton.isChecked) {
                    KumiwakeComparator.SortType.NAME
                } else if (sortByAgeRadioButton.isChecked) {
                    KumiwakeComparator.SortType.AGE
                } else {
                    KumiwakeComparator.SortType.DEFAULT
                }
                val byGroupVal = tabLayout.getTabAt(0)?.isSelected ?: true
                onChangeValues(
                    showSexIconsVal,
                    showNumberIconsVal,
                    sortTypeVal,
                    byGroupVal,

                    )
                val scrollView = activity.findViewById<View>(R.id.kumiwake_scroll) as ScrollView
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
}