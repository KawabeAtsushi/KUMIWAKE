package com.pandatone.kumiwake

import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.pandatone.kumiwake.ui.kumiwake.KumiwakeFragment
import com.pandatone.kumiwake.ui.members.MembersFragment
import com.pandatone.kumiwake.ui.sekigime.SekigimeFragment
import com.pandatone.kumiwake.ui.settings.SettingsFragment


class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val navView: AHBottomNavigation = findViewById(R.id.nav_view)

        navView.addItem(AHBottomNavigationItem(R.string.kumiwake, R.drawable.ic_kumiwake_24px, Theme.Red.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.sekigime, R.drawable.ic_sekigime_24px, Theme.Green.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.member, R.drawable.ic_members_24dp, Theme.Blue.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.setting_help, R.drawable.ic_settings_24dp, Theme.Yellow.primaryColor))

        navView.isColored = true

        setUpToolbar()
        navView.setOnTabSelectedListener(mOnNavigationItemSelectedListener)

        // To open the first tab as default
        openFragment(KumiwakeFragment())
    }


    private val mOnNavigationItemSelectedListener = AHBottomNavigation.OnTabSelectedListener { position, _ ->
        val menuItem: MenuItem = AHBottomNavigationAdapter(this, R.menu.bottom_nav_menu).getMenuItem(position)
        supportActionBar!!.title = menuItem.title
         when (menuItem.itemId) {

                R.id.navigation_kumiwake -> {
                    openFragment(KumiwakeFragment())
                    supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Red.primaryColor))
                    supportActionBar!!.title = Html.fromHtml("<font color='#FFFFFF'>"+ getString(R.string.kumiwake) + "</font>")
                    true
                }

                R.id.navigation_sekigime -> {
                    openFragment(SekigimeFragment())
                    supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Green.primaryColor))
                    supportActionBar!!.title = Html.fromHtml("<font color='#616161'>"+ getString(R.string.sekigime) + "</font>")
                    true
                }

                R.id.navigation_members -> {
                    openFragment(MembersFragment())
                    supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Blue.primaryColor))
                    supportActionBar!!.title = Html.fromHtml("<font color='#FFFFFF'>"+ getString(R.string.member) + "</font>")
                    true
                }

                R.id.navigation_settings -> {
                    openFragment(SettingsFragment())
                    supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Yellow.primaryColor))
                    supportActionBar!!.title = Html.fromHtml("<font color='#616161'>"+ getString(R.string.setting_help) + "</font>")
                    true
                }
             else -> false
         }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_layout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setUpToolbar() {

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = Html.fromHtml("<font color='#FFFFFF'>"+ getString(R.string.kumiwake) + "</font>")
    }


    companion object {
        var sekigime = false
    }
}
