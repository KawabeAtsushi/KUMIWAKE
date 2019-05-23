package com.pandatone.kumiwake.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.customize.CustomDialog;

import butterknife.ButterKnife;

/**
 * Created by atsushi_2 on 2016/02/19.
 */
public class SettingHelp extends AppCompatActivity {
    static Context context;
    CustomDialog customDialog;
    ListView howToUseList, backupList, otherList;
    ArrayAdapter<String> howToUse_adapter, backup_adapter, other_adapter;
    String[] how_to_use_str, backup_str, other_str;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_4);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.setting_help);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        context = getApplication();
        setViews();
        howToUseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //行をクリックした時の処理
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                switch (position) {
                    case 0:
                        String message = getString(R.string.how_to_kumiwake) + "■" + getString(R.string.normal_mode) + "■\n"
                                + getString(R.string.description_of_normal_mode) + "\n\n■" + getString(R.string.quick_mode) + "■\n" + getString(R.string.description_of_quick_mode);
                        ConfirmationDialog(how_to_use_str[0], message);
                        break;
                    case 1:
                        ConfirmationDialog(how_to_use_str[1], getText(R.string.how_to_member));
                        break;
                    case 2:
                        ConfirmationDialog(how_to_use_str[2], getText(R.string.how_to_sekigime));
                        break;
                }
            }
        });
        backupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //行をクリックした時の処理
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                switch (position) {
                    case 0:
                        onBackup();
                        break;
                    case 1:
                        onImport();
                        break;
                    case 2:
                        onDeleteBackup();
                        break;
                }
            }
        });
        otherList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //行をクリックした時の処理
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                switch (position) {
                    case 0:
                        showVersionName(getContext());
                        break;
                    case 1:
                        ConfirmationDialog(getString(R.string.advertise_delete), getString(R.string.wait_for_implementation));
                        break;
                    case 2:
                        launchMailer();
                        break;
                    case 3:
                        shareApp();
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // いつものUPナビゲーションの処理
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setViews() {
        how_to_use_str = new String[]{getString(R.string.about_kumiwake), getString(R.string.about_member), getString(R.string.about_sekigime)};
        backup_str = new String[]{getString(R.string.back_up_db), getString(R.string.import_db), getString(R.string.delete_backup)};
        other_str = new String[]{getString(R.string.app_version), getString(R.string.advertise_delete), getString(R.string.contact_us), getString(R.string.share_app)};
        howToUseList = (ListView) findViewById(R.id.how_to_use_list);
        backupList = (ListView) findViewById(R.id.back_up_list);
        otherList = (ListView) findViewById(R.id.other_list);
        howToUse_adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, how_to_use_str);
        backup_adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, backup_str);
        other_adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, other_str);

        howToUseList.setAdapter(howToUse_adapter);
        backupList.setAdapter(backup_adapter);
        otherList.setAdapter(other_adapter);
    }

    public void ConfirmationDialog(String title, CharSequence message) {
        customDialog = new CustomDialog();
        customDialog.setTitle(title);
        customDialog.setMessage(message);
        customDialog.show(getFragmentManager(), "Btn");
    }

    public void DecisionDialog(String title, CharSequence message, int code) {
        customDialog = new CustomDialog();
        customDialog.setTitle(title);
        customDialog.setMessage(message);
        customDialog.setOnPositiveClickListener(code);
        customDialog.show(getFragmentManager(), "Btn");
    }

    public void onBackup() {
        String title = getString(R.string.back_up_db);
        String message = getString(R.string.back_up_attention) + getString(R.string.run_confirmation);
        DecisionDialog(title, message, 1);
    }

    public void onImport() {
        String title = getString(R.string.import_db);
        String message = getString(R.string.import_attention) + getString(R.string.run_confirmation);
        DecisionDialog(title, message, 2);
    }

    public void onDeleteBackup() {
        String title = getString(R.string.delete_backup);
        String message = getString(R.string.delete_backup_attention);
        DecisionDialog(title, message,3);
    }

    public void showVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ConfirmationDialog(getString(R.string.app_version), versionName);
    }

    public void launchMailer() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:ganbalism@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "KUMIWAKE:お問い合わせ");
//createChooserを使うと選択ダイアログのタイトルを変更する事ができます。
        startActivity(Intent.createChooser(intent, getString(R.string.contact_us)));
    }


    public void shareApp() {
        String articleTitle = getString(R.string.article_title);
        String articleURL = "https://play.google.com/store/apps/details?id=com.pandatone.pandatone_ganbalism_atsushi_2.kumiwake";
        String sharedText = articleTitle + "\n" + articleURL + "\n" + "_(÷3」∠)_";

// builderの生成　ShareCompat.IntentBuilder.from(Context context);
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this);

// アプリ一覧が表示されるDialogのタイトルの設定
        builder.setChooserTitle(R.string.choose_app);

// シェアするタイトル
        builder.setSubject(articleTitle);

// シェアするテキスト
        builder.setText(sharedText);

// シェアするタイプ（他にもいっぱいあるよ）
        builder.setType("text/plain");

// Shareアプリ一覧のDialogの表示
        builder.startChooser();

    }

    protected static Context getContext() {
        return context;
    }
}
