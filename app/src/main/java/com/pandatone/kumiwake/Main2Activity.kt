package com.pandatone.kumiwake

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_kumiwake, R.id.navigation_sekigime, R.id.navigation_members, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        // BottomNavigationViewを選択したときのリスナー
//        navView.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
//            override fun onNavigationItemSelected(item: MenuItem): Boolean {
//
//                // 各選択したときの処理
//                when (item.itemId) {
//                    R.id.navigation_kumiwake -> {
//                        switchFragment(KumiwakeFragment())
//                        return true
//                    }
//                    R.id.navigation_sekigime -> {
//                        switchFragment(SekigimeFragment())
//                        return true
//                    }
//                    R.id.navigation_members -> {
//                        switchFragment(MembersFragment())
//                        return true
//                    }
//                    R.id.navigation_settings -> {
//                        switchFragment(SettingsFragment())
//                        return true
//                    }
//                }
//                return true
//            }
//        })
    }

//    private fun switchFragment(fragment:Fragment) {
//        val transaction = supportFragmentManager.beginTransaction()
//        val currentFragment: Fragment? = supportFragmentManager.primaryNavigationFragment
//        val tag = fragment.javaClass.simpleName
//        if (currentFragment != null) {
//            transaction.hide(currentFragment)
//        }
//        var fragmentTemp: Fragment? = supportFragmentManager.findFragmentByTag(tag)
//        if (fragmentTemp == null) {
//            fragmentTemp = fragment
//            transaction.add(R.id.nav_host_fragment, fragmentTemp, tag)
//        } else {
//            Log.d("tag",tag)
//            transaction.show(fragmentTemp)
//        }
//        transaction.setPrimaryNavigationFragment(fragmentTemp)
//        transaction.setReorderingAllowed(true)
//        transaction.commitNowAllowingStateLoss()
//    }

    companion object {
        var sekigime = false
    }
}
