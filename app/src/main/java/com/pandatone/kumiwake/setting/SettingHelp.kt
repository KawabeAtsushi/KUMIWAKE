package com.pandatone.kumiwake.setting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.customize.CustomDialog
import kotlinx.android.synthetic.main.setting_help.*



/**
 * Created by atsushi_2 on 2016/02/19.
 */
class SettingHelp : AppCompatActivity() {
    private lateinit var customDialog: CustomDialog
    private lateinit var howToUse_adapter: ArrayAdapter<String>
    private lateinit var backup_adapter: ArrayAdapter<String>
    private lateinit var other_adapter: ArrayAdapter<String>
    private lateinit var how_to_use_str: Array<String>
    private lateinit var backup_str: Array<String>
    private lateinit var other_str: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_help)
        val toolbar = findViewById<View>(R.id.tool_bar_4) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setTitle(R.string.setting_help)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        ButterKnife.bind(this)
        setViews()
        how_to_use_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            when (position) {
                0 -> {
                    val message = (getString(R.string.how_to_kumiwake) + "■" + getString(R.string.normal_mode) + "■\n"
                            + getString(R.string.description_of_normal_mode) + "\n\n■" + getString(R.string.quick_mode) + "■\n" + getString(R.string.description_of_quick_mode))
                    confirmationDialog(how_to_use_str[0], message)
                }
                1 -> confirmationDialog(how_to_use_str[1], getText(R.string.how_to_member))
                2 -> confirmationDialog(how_to_use_str[2], getText(R.string.how_to_sekigime))
            }
        }
        back_up_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            checkPermission(this, position)
        }
        other_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            when (position) {
                0 -> showVersionName(applicationContext)
                1 -> confirmationDialog(getString(R.string.advertise_delete), getString(R.string.wait_for_implementation))
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

    private fun setViews() {
        how_to_use_str = arrayOf(getString(R.string.about_kumiwake), getString(R.string.about_member), getString(R.string.about_sekigime))
        backup_str = arrayOf(getString(R.string.back_up_db), getString(R.string.import_db), getString(R.string.delete_backup))
        other_str = arrayOf(getString(R.string.app_version), getString(R.string.advertise_delete), getString(R.string.contact_us), getString(R.string.share_app))
        howToUse_adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, how_to_use_str)
        backup_adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, backup_str)
        other_adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, other_str)

        how_to_use_list.adapter = howToUse_adapter
        back_up_list.adapter = backup_adapter
        other_list.adapter = other_adapter
    }

    private fun confirmationDialog(title: String, message: CharSequence) {
        customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    private fun decisionDialog(title: String, message: CharSequence, code: Int) {
        customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        customDialog.setOnPositiveClickListener(code)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    private fun onBackup() {
        val title = getString(R.string.back_up_db)
        val message = getString(R.string.back_up_attention) + getString(R.string.run_confirmation)
        decisionDialog(title, message, 1)
    }

    private fun onImport() {
        val title = getString(R.string.import_db)
        val message = getString(R.string.import_attention) + getString(R.string.run_confirmation)
        decisionDialog(title, message, 2)
    }

    private fun onDeleteBackup() {
        val title = getString(R.string.delete_backup)
        val message = getString(R.string.delete_backup_attention)
        decisionDialog(title, message, 3)
    }

    private fun showVersionName(context: Context) {
        val pm = context.packageManager
        var versionName = ""
        try {
            val packageInfo = pm.getPackageInfo(context.packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        confirmationDialog(getString(R.string.app_version), versionName)
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

    /////////////////////////パーミッション/////////////////////////////////////////////////

    private val REQUEST_PERMISSION = 1000
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
            // 拒否していた場合
            requestPermission()
        }
    }

    // 許可を求める
    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION)

        } else {
            val toast = Toast.makeText(this,
                    getText(R.string.please_permit), Toast.LENGTH_SHORT)
            toast.show()

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                when (position) {
                    0 -> onBackup()
                    1 -> onImport()
                    2 -> onDeleteBackup()
                }

            } else {
                // それでも拒否された時の対応
                val toast = Toast.makeText(this,
                        getText(R.string.please_permit), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

}
