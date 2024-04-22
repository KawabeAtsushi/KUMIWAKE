package com.pandatone.kumiwake.setting

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.PublicMethods.setStatus
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.Theme
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import java.io.File


class Settings : AppCompatActivity() {

    private lateinit var backupAdapter: ArrayAdapter<String>
    private lateinit var otherAdapter: ArrayAdapter<String>
    private lateinit var backupStr: Array<String>
    private lateinit var otherStr: Array<String>

    private lateinit var dimmer: View
    private var rewardedAd: RewardedAd? = null
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
            when (position) {
                0 -> onBackup()
                1 -> onImport()
            }
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
            otherList.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { _, _, pos, _ ->
                    //行をクリックした時の処理
                    when (pos) {
                        1 -> {
                            StatusHolder.checkStatus = true
                            startActivity(Intent(this, PurchaseFreeAdOption::class.java))
                        }
                    }
                    false
                }
        }

        setViews()
    }

    private fun setViews() {
        backupStr = arrayOf(
            getString(R.string.back_up_db),
            getString(R.string.import_db),
        )
        otherStr = arrayOf(
            getString(R.string.app_version),
            getString(R.string.advertise_delete),
            getString(R.string.delete_ad_temporary),
            getString(R.string.contact_us),
            getString(R.string.share_app),
            getString(R.string.privacy_policy)
        )
        backupAdapter =
            ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, backupStr)
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


    private fun showVersionName() {
        var versionName = ""
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val releaseNoteLink = PublicMethods.getLinkChar(
            getString(R.string.url_release_note),
            getString(R.string.release_note)
        )
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

        dialog.decisionDialog(
            getString(R.string.delete_ad_temporary),
            getString(R.string.delete_ad_temporary_description),
            getString(R.string.watch_ad),
            getString(R.string.close)
        ) {
            dimmer = findViewById(R.id.dimmer_layout)
            dimmer.visibility = View.VISIBLE
            loadingAnim = findViewById(R.id.loading_anim)
            loadingAnim.visibility = View.VISIBLE
            loadingAnim.playAnimation()
            var adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                this,
                "ca-app-pub-3940256099942544/5224354917",
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        rewardedAd = null
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        onRewardedVideoAdLoaded(ad)
                    }

                })
            FirebaseAnalyticsEvents.support("CLICKED")
        }
    }

    private fun toPrivacyPolicy() {
        val uri =
            Uri.parse("https://gist.githubusercontent.com/KawabeAtsushi/39f3ea332b05a6b053b263784a77cd51/raw")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    ////////////////////リワード広告のオーバーライド////////////////////////////////////////////

    // 広告の準備が完了したとき
    private fun onRewardedVideoAdLoaded(ad: RewardedAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                rewardedAd = null
                onRewardedVideoAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
                loadingAnim.visibility = View.GONE
                loadingAnim.cancelAnimation()
            }
        }
        ad.show(this@Settings, OnUserEarnedRewardListener { rewardItem ->
            onRewarded(rewardItem)
        })
    }

    //報酬対象になったとき
    private fun onRewarded(p0: RewardItem?) {
        rewarded = true
        StatusHolder.adDeleted = true
        MainActivity.mAdView.visibility = View.GONE
    }

    //広告が閉じられたとき
    fun onRewardedVideoAdClosed() {
        dimmer.visibility = View.GONE

        if (rewarded) {
            Toast.makeText(this, getString(R.string.ads_removed_temporarily), Toast.LENGTH_LONG)
                .show()
            rewarded = false
            FirebaseAnalyticsEvents.support("REWARDED")
        }
    }

}