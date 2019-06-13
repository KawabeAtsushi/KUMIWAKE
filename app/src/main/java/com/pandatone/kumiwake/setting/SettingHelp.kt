package com.pandatone.kumiwake.setting

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import butterknife.ButterKnife
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.customize.CustomDialog

/**
 * Created by atsushi_2 on 2016/02/19.
 */
class SettingHelp : AppCompatActivity() {
    internal var customDialog: CustomDialog
    internal var howToUseList: ListView
    internal var backupList: ListView
    internal var otherList: ListView
    internal var howToUse_adapter: ArrayAdapter<String>
    internal var backup_adapter: ArrayAdapter<String>
    internal var other_adapter: ArrayAdapter<String>
    internal var how_to_use_str: Array<String>
    internal var backup_str: Array<String>
    internal var other_str: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_help)
        val toolbar = findViewById<View>(R.id.tool_bar_4) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setTitle(R.string.setting_help)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        ButterKnife.bind(this)
        context = application
        setViews()
        howToUseList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            //行をクリックした時の処理
            when (position) {
                0 -> {
                    val message = (getString(R.string.how_to_kumiwake) + "■" + getString(R.string.normal_mode) + "■\n"
                            + getString(R.string.description_of_normal_mode) + "\n\n■" + getString(R.string.quick_mode) + "■\n" + getString(R.string.description_of_quick_mode))
                    ConfirmationDialog(how_to_use_str[0], message)
                }
                1 -> ConfirmationDialog(how_to_use_str[1], getText(R.string.how_to_member))
                2 -> ConfirmationDialog(how_to_use_str[2], getText(R.string.how_to_sekigime))
            }
        }
        backupList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            //行をクリックした時の処理
            when (position) {
                0 -> onBackup()
                1 -> onImport()
                2 -> onDeleteBackup()
            }
        }
        otherList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            //行をクリックした時の処理
            when (position) {
                0 -> showVersionName(context)
                1 -> ConfirmationDialog(getString(R.string.advertise_delete), getString(R.string.wait_for_implementation))
                2 -> launchMailer()
                3 -> shareApp()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // いつものUPナビゲーションの処理
        when (id) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setViews() {
        how_to_use_str = arrayOf(getString(R.string.about_kumiwake), getString(R.string.about_member), getString(R.string.about_sekigime))
        backup_str = arrayOf(getString(R.string.back_up_db), getString(R.string.import_db), getString(R.string.delete_backup))
        other_str = arrayOf(getString(R.string.app_version), getString(R.string.advertise_delete), getString(R.string.contact_us), getString(R.string.share_app))
        howToUseList = findViewById<View>(R.id.how_to_use_list) as ListView
        backupList = findViewById<View>(R.id.back_up_list) as ListView
        otherList = findViewById<View>(R.id.other_list) as ListView
        howToUse_adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, how_to_use_str)
        backup_adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, backup_str)
        other_adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, other_str)

        howToUseList.adapter = howToUse_adapter
        backupList.adapter = backup_adapter
        otherList.adapter = other_adapter
    }

    fun ConfirmationDialog(title: String, message: CharSequence) {
        customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        customDialog.show(fragmentManager, "Btn")
    }

    fun DecisionDialog(title: String, message: CharSequence, code: Int) {
        customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        customDialog.setOnPositiveClickListener(code)
        customDialog.show(fragmentManager, "Btn")
    }

    fun onBackup() {
        val title = getString(R.string.back_up_db)
        val message = getString(R.string.back_up_attention) + getString(R.string.run_confirmation)
        DecisionDialog(title, message, 1)
    }

    fun onImport() {
        val title = getString(R.string.import_db)
        val message = getString(R.string.import_attention) + getString(R.string.run_confirmation)
        DecisionDialog(title, message, 2)
    }

    fun onDeleteBackup() {
        val title = getString(R.string.delete_backup)
        val message = getString(R.string.delete_backup_attention)
        DecisionDialog(title, message, 3)
    }

    fun showVersionName(context: Context) {
        val pm = context.packageManager
        var versionName = ""
        try {
            val packageInfo = pm.getPackageInfo(context.packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        ConfirmationDialog(getString(R.string.app_version), versionName)
    }

    fun launchMailer() {
        val intent = Intent()
        intent.action = Intent.ACTION_SENDTO
        intent.data = Uri.parse("mailto:ganbalism@gmail.com")
        intent.putExtra(Intent.EXTRA_SUBJECT, "KUMIWAKE:お問い合わせ")
        //createChooserを使うと選択ダイアログのタイトルを変更する事ができます。
        startActivity(Intent.createChooser(intent, getString(R.string.contact_us)))
    }


    fun shareApp() {
        val articleTitle = getString(R.string.article_title)
        val articleURL = "https://play.google.com/store/apps/details?id=com.pandatone.pandatone_ganbalism_atsushi_2.kumiwake"
        val sharedText = "$articleTitle\n$articleURL\n_(÷3」∠)_"

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

    companion object {
        var context: Context
            internal set
    }
}
