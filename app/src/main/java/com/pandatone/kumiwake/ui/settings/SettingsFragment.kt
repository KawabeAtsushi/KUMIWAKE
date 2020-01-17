package com.pandatone.kumiwake.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ui.DialogWarehouse
import android.app.Activity.RESULT_OK
import com.pandatone.kumiwake.MyApplication
import java.io.UnsupportedEncodingException
import java.net.URLDecoder


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: MembersViewModel
    private lateinit var howToUse_adapter: ArrayAdapter<String>
    private lateinit var backup_adapter: ArrayAdapter<String>
    private lateinit var other_adapter: ArrayAdapter<String>
    private lateinit var how_to_use_str: Array<String>
    private lateinit var backup_str: Array<String>
    private lateinit var other_str: Array<String>
    private val dialog: DialogWarehouse
        get() {
            return DialogWarehouse(requireFragmentManager())
        }
    private lateinit var howToUseList: ListView
    private lateinit var backupList: ListView
    private lateinit var otherList: ListView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
                ViewModelProviders.of(this).get(MembersViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        howToUseList = root.findViewById(R.id.how_to_use_list)
        howToUseList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            when (position) {
                0 -> {
                    val message = (getString(R.string.how_to_kumiwake) + "■" + getString(R.string.normal_mode) + "■\n"
                            + getString(R.string.description_of_normal_kumiwake) + "\n\n■" + getString(R.string.quick_mode) + "■\n" + getString(R.string.description_of_quick_kumiwake))
                    dialog.confirmationDialog(how_to_use_str[0], message)
                }
                1 -> dialog.confirmationDialog(how_to_use_str[1], getText(R.string.how_to_member))
                2 -> dialog.confirmationDialog(how_to_use_str[2], getText(R.string.how_to_sekigime))
            }
        }
        backupList = root.findViewById(R.id.back_up_list)
        backupList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            if (position != 3) {
                checkPermission(activity!!.baseContext, position)
            } else {
                onRefreshData()
            }
        }
        otherList = root.findViewById(R.id.other_list)
        otherList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            when (position) {
                0 -> showVersionName(activity!!.baseContext)
                1 -> dialog.confirmationDialog(getString(R.string.advertise_delete), getString(R.string.wait_for_implementation))
                2 -> launchMailer()
                3 -> shareApp()
                4 -> toPrivacyPolicy()
            }
        }

        setViews()

        return root
    }

    private fun setViews() {
        val context = activity!!.baseContext

        how_to_use_str = arrayOf(getString(R.string.about_kumiwake), getString(R.string.about_member), getString(R.string.about_sekigime))
        backup_str = arrayOf(getString(R.string.back_up_db), getString(R.string.import_db), getString(R.string.delete_backup), getString(R.string.refresh_data))
        other_str = arrayOf(getString(R.string.app_version), getString(R.string.advertise_delete), getString(R.string.contact_us), getString(R.string.share_app), getString(R.string.privacy_policy))
        howToUse_adapter = ArrayAdapter(context, android.R.layout.simple_expandable_list_item_1, how_to_use_str)
        backup_adapter = ArrayAdapter(context, android.R.layout.simple_expandable_list_item_1, backup_str)
        other_adapter = ArrayAdapter(context, android.R.layout.simple_expandable_list_item_1, other_str)

        howToUseList.adapter = howToUse_adapter
        backupList.adapter = backup_adapter
        otherList.adapter = other_adapter
    }

    private fun onBackup() {
        val title = getString(R.string.back_up_db)
        val message = getString(R.string.back_up_attention) + getString(R.string.run_confirmation)
        dialog.decisionDialog(title, message, 1)
    }

    private fun onImport() {
        val title = getString(R.string.import_db)
        val message = getString(R.string.import_attention) + getString(R.string.run_confirmation)
        dialog.decisionDialog(title, message, 2)
    }

    private fun onDeleteBackup() {
        val title = getString(R.string.delete_backup)
        val message = getString(R.string.delete_backup_attention)
        dialog.decisionDialog(title, message, 3)
    }

    private fun onRefreshData() {
        val title = getString(R.string.refresh_data)
        val message = getString(R.string.refresh_attention) + getString(R.string.run_confirmation)
        dialog.decisionDialog(title, message, 4)
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

        dialog.confirmationDialog(getString(R.string.app_version), versionName)
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
        val builder = ShareCompat.IntentBuilder.from(activity!!)

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

    private fun toPrivacyPolicy() {
//        val uri = Uri.parse("https://gist.githubusercontent.com/KawabeAtsushi/39f3ea332b05a6b053b263784a77cd51/raw/7666e22b85561c34a95863f9482ed900482d2c8d/privacy%2520policy")
//        val intent = Intent(Intent.ACTION_VIEW, uri)
//        startActivity(intent)
        onClick()
    }

    // 識別用のコード
    private val CHOSE_FILE_CODE = 12345
    public var decodedfilePath:String = ""

    private fun onClick() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "file/*"
        startActivityForResult(Intent.createChooser(intent, "FileManager"), CHOSE_FILE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == CHOSE_FILE_CODE && resultCode == RESULT_OK) {
                val filePath = data!!.dataString!!.replace("file://", "")
                decodedfilePath = URLDecoder.decode(filePath, "utf-8")
                Toast.makeText(activity, decodedfilePath, Toast.LENGTH_SHORT).show()
            }
        } catch (e: UnsupportedEncodingException) {
            // いい感じに例外処理
        }

    }

    /////////////////////////パーミッション/////////////////////////////////////////////////

    private val PERMISSION = 1000
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
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                when (position) {
                    0 -> onBackup()
                    1 -> onImport()
                    2 -> onDeleteBackup()
                }

            } else {
                // それでも拒否された時の対応
                val toast = Toast.makeText(activity!!.baseContext,
                        getText(R.string.please_permit), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}