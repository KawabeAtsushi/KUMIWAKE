package com.pandatone.kumiwake.setting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.PublicMethods.setStatus
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import java.io.File


class Settings : AppCompatActivity(), RewardedVideoAdListener {

    private lateinit var backupAdapter: ArrayAdapter<String>
    private lateinit var otherAdapter: ArrayAdapter<String>
    private lateinit var backupStr: Array<String>
    private lateinit var otherStr: Array<String>

    private lateinit var dimmer: View
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var loadingAnim: LottieAnimationView
    private var rewarded = false

    val dialog: DialogWarehouse
        get() {
            return DialogWarehouse(supportFragmentManager)
        }
    private lateinit var backupList: ListView
    private lateinit var otherList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setTheme(R.style.SettingsTheme)
        setContentView(R.layout.settings)
        setStatus(this, Theme.Setting.primaryColor)
        setTitle(R.string.settings)

        backupList = findViewById(R.id.back_up_list)
        backupList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            checkPermission(this.baseContext, position)
        }
        otherList = findViewById(R.id.other_list)
        otherList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            when (position) {
                0 -> showVersionName()
                1 -> startActivity(Intent(this, PurchaseFreeAdOption::class.java))
                2 -> removeAdsTemp()
                3 -> launchMailer()
                4 -> shareApp()
                5 -> toPrivacyPolicy()
            }
            otherList.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, pos, _ ->
                //行をクリックした時の処理
                when (pos) {
                    1 -> {
                        StatusHolder.cheakStatus = true
                        startActivity(Intent(this, PurchaseFreeAdOption::class.java))
                    }
                }
                false
            }
        }

        setViews()
    }

    private fun setViews() {
        backupStr = arrayOf(getString(R.string.back_up_db), getString(R.string.import_db), getString(R.string.delete_backup))
        otherStr = arrayOf(getString(R.string.app_version), getString(R.string.advertise_delete), getString(R.string.delete_ad_temporary), getString(R.string.contact_us), getString(R.string.share_app), getString(R.string.privacy_policy))
        backupAdapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, backupStr)
        otherAdapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, otherStr)

        backupList.adapter = backupAdapter
        otherList.adapter = otherAdapter
    }

    private fun onBackup() {
        val title = getString(R.string.back_up_db)
        val message = getString(R.string.back_up_attention) + getString(R.string.run_confirmation)
        dialog.fmDialog(title, message, true)
    }

    private fun onImport() {
        val title = getString(R.string.import_db)
        val message = getString(R.string.import_attention) + getString(R.string.run_confirmation)
        dialog.fmDialog(title, message, false)
    }

    private fun onDeleteBackup() {
        val title = getString(R.string.delete_backup)
        val message = getString(R.string.delete_backup_attention)
        dialog.decisionDialog(title, message, function = this::deleteBackup)
    }

    private fun deleteBackup() {

        val dir = File(Environment.getExternalStorageDirectory().path + "/KUMIWAKE_Backup")

        if (!dir.exists()) {
            Toast.makeText(this, getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
        } else {
            dir.deleteRecursively()
            Toast.makeText(this, getString(R.string.deleted_backup_file), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showVersionName() {
        var versionName = ""
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val releaseNoteLink = PublicMethods.getLinkChar(getString(R.string.url_release_note), getString(R.string.release_note))
        dialog.confirmationDialog(getString(R.string.app_version), versionName, releaseNoteLink)
    }

    private fun launchMailer() {
        val intent = Intent()
        intent.action = Intent.ACTION_SENDTO
        intent.data = Uri.parse("mailto:ganbalism@gmail.com")
        intent.putExtra(Intent.EXTRA_SUBJECT, "KUMIWAKE:お問い合わせ")
        //createChooserを使うと選択ダイアログのタイトルを変更する事ができます。
        startActivity(Intent.createChooser(intent, getString(R.string.contact_us)))
    }


    private fun shareApp() {
        val articleTitle = getString(R.string.article_title)
        val articleURL = "https://play.google.com/store/apps/details?id=com.pandatone.kumiwake"
        val sharedText = "$articleTitle\n$articleURL"

        // builderの生成　ShareCompat.IntentBuilder.from(Context context);
        val builder = ShareCompat.IntentBuilder.from(this)

        // アプリ一覧が表示されるDialogのタイトルの設定
        builder.setChooserTitle(R.string.choose_app)

        // シェアするタイトル
        builder.setSubject(articleTitle)

        // シェアするテキスト
        builder.setText(sharedText)

        // シェアするタイプ（他にもいっぱいあるよ）
        builder.setType("text/plain")

        // Shareアプリ一覧のDialogの表示
        builder.startChooser()

    }

    private fun removeAdsTemp() {

        dialog.decisionDialog(getString(R.string.delete_ad_temporary), getString(R.string.delete_ad_temporary_description), getString(R.string.watch_ad), getString(R.string.close)) {
            dimmer = findViewById(R.id.dimmer_layout)
            dimmer.visibility = View.VISIBLE
            loadingAnim = findViewById(R.id.loading_anim)
            loadingAnim.visibility = View.VISIBLE
            loadingAnim.playAnimation()
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
            mRewardedVideoAd.rewardedVideoAdListener = this
            mRewardedVideoAd.loadAd(getString(R.string.adVideoUnit_id),
                    AdRequest.Builder().addTestDevice(getString(R.string.device_id)).build())
            FirebaseAnalyticsEvents.support("CLICKED")
        }
    }

    private fun toPrivacyPolicy() {
        val uri = Uri.parse("https://gist.githubusercontent.com/KawabeAtsushi/39f3ea332b05a6b053b263784a77cd51/raw")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    ////////////////////リワード広告のオーバーライド////////////////////////////////////////////

    // 広告の準備が完了したとき
    override fun onRewardedVideoAdLoaded() {
        mRewardedVideoAd.show()
        loadingAnim.visibility = View.GONE
        loadingAnim.cancelAnimation()
    }

    //報酬対象になったとき
    override fun onRewarded(p0: com.google.android.gms.ads.reward.RewardItem?) {
        rewarded = true
        StatusHolder.adDeleated = true
        MainActivity.mAdView.visibility = View.GONE
    }

    //広告が閉じられたとき
    override fun onRewardedVideoAdClosed() {
        dimmer.visibility = View.GONE

        if (rewarded) {
            Toast.makeText(this, getString(R.string.ads_removed_temporarily), Toast.LENGTH_LONG).show()
            rewarded = false
            FirebaseAnalyticsEvents.support("REWARDED")
        }
    }

    override fun onRewardedVideoAdOpened() {}
    override fun onRewardedVideoStarted() {}
    override fun onRewardedVideoAdLeftApplication() {}
    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {}
    override fun onRewardedVideoCompleted() {}

    /////////////////////////パーミッション/////////////////////////////////////////////////

    private val permission = 1000
    private var position = 0

    // ストレージ許可の確認
    private fun checkPermission(c: Context, pos: Int) {
        position = pos

        if (ContextCompat.checkSelfPermission(c,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 既に許可している
            when (position) {
                0 -> onBackup()
                1 -> onImport()
                2 -> onDeleteBackup()
            }
        } else {
            // 拒否していた場合,許可を求める
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    permission)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == permission) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                when (position) {
                    0 -> onBackup()
                    1 -> onImport()
                    2 -> onDeleteBackup()
                }

            } else {
                // それでも拒否された時の対応
                val toast = Toast.makeText(this.baseContext,
                        getText(R.string.please_permit), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}