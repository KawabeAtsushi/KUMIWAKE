package com.pandatone.kumiwake

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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

        //setUpToolbar()
        navView.setOnTabSelectedListener(mOnNavigationItemSelectedListener)

        // To open the first tab as default
        openFragment(KumiwakeFragment())
    }


    private val mOnNavigationItemSelectedListener = AHBottomNavigation.OnTabSelectedListener { position, wasSelected ->
        val menuItem: MenuItem = AHBottomNavigationAdapter(this, R.menu.bottom_nav_menu).getMenuItem(position)
         when (menuItem.itemId) {

                R.id.navigation_kumiwake -> {
                    openFragment(KumiwakeFragment())
                    true
                }

                R.id.navigation_sekigime -> {
                    openFragment(SekigimeFragment())
                    true
                }

                R.id.navigation_members -> {
                    openFragment(MembersFragment())
                    true
                }

                R.id.navigation_settings -> {
                    openFragment(SettingsFragment())
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

    fun setUpToolbar() {

        // Hide action bar
        val actionBar = supportActionBar
        actionBar!!.hide()
    }


    companion object {
        var sekigime = false
    }
}
