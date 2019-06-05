package com.pandatone.kumiwake.member;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.MemberListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/02/24.
 */
public class AddMember extends AppCompatActivity {


    private static AppCompatEditText nameEditText;
    private static AppCompatEditText readEditText;
    private static TextInputLayout textInputLayout;
    private static RadioGroup sexGroup;
    private static RadioButton sexButton;
    private static EditText ageEditText;
    private static EditText gradeEditText;
    private static Button belongSpinner;
    private static Button roleSpinner;
    private static MemberListAdapter dbAdapter;
    private String[] beforeBelong, afterBelong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_member);
        ButterKnife.bind(this);
        dbAdapter = new MemberListAdapter(this);
        beforeBelong = null;
        afterBelong = null;
        findViews();
        ageEditText.setText("");
        gradeEditText.setText("");
        Intent i = getIntent();
        int position = i.getIntExtra("POSITION", -1);
        if (position != -1) {
            setItem(position);
        }
    }

    public void findViews() {
        nameEditText = (AppCompatEditText) findViewById(R.id.input_name);
        readEditText = (AppCompatEditText) findViewById(R.id.input_name_read);
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.toString().matches("^[a-zA-Z0-9ぁ-ん]+$")) {
                    return source;
                }
                return "";
            }
        };
        InputFilter[] filters = {inputFilter};
        readEditText.setFilters(filters);
        textInputLayout = (TextInputLayout) findViewById(R.id.member_form_input_layout);
        sexGroup = (RadioGroup) findViewById(R.id.sexGroup);
        ageEditText = (EditText) findViewById(R.id.input_age);
        gradeEditText = (EditText) findViewById(R.id.input_grade);
        belongSpinner = (Button) findViewById(R.id.select_group_spinner);
        roleSpinner = (Button) findViewById(R.id.select_role_spinner);
    }

    public void setItem(int position) {
        Name listItem = FragmentMember.nameList.get(position);
        int listId = listItem.getId();
        String name = listItem.getName();
        String name_read = listItem.getName_read();
        String sex = listItem.getSex();
        String age = String.valueOf(listItem.getAge());
        String grade = String.valueOf(listItem.getGrade());
        String belong = MemberClick.ViewBelong(position);
        String role = listItem.getRole();

        nameEditText.setText(name);
        readEditText.setText(name_read);
        if (sex != null && sex.equals("女")) {
            sexGroup.check(R.id.womanBtn);
        } else {
            sexGroup.check(R.id.manBtn);
        }
        ageEditText.setText(age);
        gradeEditText.setText(grade);
        belongSpinner.setText(belong);
        roleSpinner.setText(role);

        update(listId);

    }


    @OnClick(R.id.select_group_spinner)
    void onSelectGroupSpinnerClicked(final View view) {
        // 選択中の候補を取得
        final String buttonText = belongSpinner.getText().toString();
        String[] textArray = buttonText.split(",");
        // 候補リスト
        List<String> list = new ArrayList<String>();
        for (int j = 0; j < FragmentGroup.ListCount; j++) {
            GroupListAdapter.Group listItem = FragmentGroup.nameList.get(j);
            String groupName = listItem.getGroup();

            list.add(groupName);
        }
        final String[] belongArray = list.toArray(new String[0]);
        // 選択リスト
        boolean[] checkArray = new boolean[belongArray.length];
        for (int i = 0; i < belongArray.length; i++) {
            checkArray[i] = false;
            for (String data : textArray) {
                if (belongArray[i].equals(data)) {
                    checkArray[i] = true;
                    break;
                }
            }
        }
        // ダイアログを生成
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // 選択イベント
        dialog.setMultiChoiceItems(belongArray, checkArray, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dia, int value, boolean isChecked) {
                String text = belongSpinner.getText().toString();
                // 選択された場合
                if (isChecked) {
                    // ボタンの表示に追加
                    belongSpinner.setText(text + ("".equals(text) ? "" : ",") + belongArray[value]);
                } else {
                    // ボタンの表示から削除
                    if (text.indexOf(belongArray[value] + ",") >= 0) {
                        belongSpinner.setText(text.replace(belongArray[value] + ",", ""));
                    } else if (text.indexOf("," + belongArray[value]) >= 0) {
                        belongSpinner.setText(text.replace("," + belongArray[value], ""));
                    } else {
                        belongSpinner.setText(text.replace(belongArray[value], ""));
                    }
                }
            }
        });
        dialog.setPositiveButton(getText(R.string.decide), null);
        dialog.setNeutralButton(getText(R.string.clear), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int value) {
                belongSpinner.setText("");
                // 再表示
                onSelectGroupSpinnerClicked(view);
            }
        });
        dialog.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int value) {
                // 選択前の状態に戻す
                belongSpinner.setText(buttonText);
            }
        });
        dialog.show();

        beforeBelong = textArray;;
    }

    @OnClick(R.id.select_role_spinner)
    void onSelectRoleSpinnerClicked(final View view) {
        // 選択中の候補を取得
        final String buttonText = roleSpinner.getText().toString();
        String[] textArray = buttonText.split(",");

        // 候補リスト
        String[] strs = getResources().getStringArray(R.array.role);
        List<String> list = new ArrayList<String>(Arrays.asList(strs)); // 新インスタンスを生成
        list.remove(0);
        final String[] likeArray = list.toArray(new String[list.size()]);

        // 選択リスト
        boolean[] checkArray = new boolean[likeArray.length];
        for (int i = 0; i < likeArray.length; i++) {
            checkArray[i] = false;
            for (String data : textArray) {
                if (likeArray[i].equals(data)) {
                    checkArray[i] = true;
                    break;
                }
            }
        }
        // ダイアログを生成
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // 選択イベント
        dialog.setMultiChoiceItems(likeArray, checkArray, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dia, int value, boolean isChecked) {
                String text = roleSpinner.getText().toString();
                // 選択された場合
                if (isChecked) {
                    // ボタンの表示に追加
                    roleSpinner.setText(text + ("".equals(text) ? "" : ",") + likeArray[value]);
                } else {
                    // ボタンの表示から削除
                    if (text.indexOf(likeArray[value] + ",") >= 0) {
                        roleSpinner.setText(text.replace(likeArray[value] + ",", ""));
                    } else if (text.indexOf("," + likeArray[value]) >= 0) {
                        roleSpinner.setText(text.replace("," + likeArray[value], ""));
                    } else {
                        roleSpinner.setText(text.replace(likeArray[value], ""));
                    }
                }
            }
        });
        dialog.setPositiveButton(getText(R.string.decide), null);
        dialog.setNeutralButton(getText(R.string.clear), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int value) {
                roleSpinner.setText("");
                // 再表示
                onSelectRoleSpinnerClicked(view);
            }
        });
        dialog.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int value) {
                // 選択前の状態に戻す
                roleSpinner.setText(buttonText);
            }
        });
        dialog.show();
    }

    @OnClick(R.id.member_registration_button)
    void onRegistrationMemberClicked() {
        String name = nameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(getText(R.string.error_empty_name));
        } else {
            saveItem();
            afterBelong = belongSpinner.getText().toString().split(",");
            changeBelongNo();
            Toast.makeText(this, getText(R.string.member) + " \"" + name + "\" " + getText(R.string.registered), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    protected void updateItem(int listId) {
        sexButton = (RadioButton) findViewById(sexGroup.getCheckedRadioButtonId());
        String name = nameEditText.getText().toString();
        String name_read = readEditText.getText().toString();
        String sex = (String) sexButton.getText();
        int age = getValue(ageEditText);
        int grade = getValue(gradeEditText);
        String belong = belongConvertToNo();
        String role = roleSpinner.getText().toString();

        dbAdapter.updateMember(listId, name, sex, age, grade, belong, role, name_read);
        FragmentMember.loadName();
    }

    public void update(final int listId) {
        final Button updateBt = (Button) findViewById(R.id.member_registration_button);
        updateBt.setText(R.string.update);
        updateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getText(R.string.error_empty_name));
                } else {
                    updateItem(listId);
                    afterBelong = belongSpinner.getText().toString().split(",");
                    changeBelongNo();
                    Toast.makeText(getApplicationContext(), getText(R.string.member) + " \"" + name + "\" " + getText(R.string.updated), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    protected void saveItem() {
        sexButton = (RadioButton) findViewById(sexGroup.getCheckedRadioButtonId());

        String name = nameEditText.getText().toString();
        String name_read = readEditText.getText().toString();
        String sex = (String) sexButton.getText();
        int age = getValue(ageEditText);
        int grade = getValue(gradeEditText);
        String belong = belongConvertToNo();
        String role = roleSpinner.getText().toString();

        dbAdapter.open();
        dbAdapter.saveName(name, sex, age, grade, belong, role, name_read);
        dbAdapter.close();
    }

    public static int getValue(EditText totext) {
        String text = totext.getText().toString();
        int a = 0;
        if (text.length() > 0) {
            a = Integer.parseInt(text);
        }
        return a;

    }

    public static String belongConvertToNo() {
        dbAdapter.open();
        String belongText = belongSpinner.getText().toString();
        String[] belongTextArray = belongText.split(",");
        StringBuilder BelongNo = new StringBuilder();

        for (int i = 0; i < belongTextArray.length; i++) {
            String belongGroup = belongTextArray[i];
            for (int j = 0; j < FragmentGroup.ListCount; j++) {
                GroupListAdapter.Group listItem = FragmentGroup.nameList.get(j);
                String groupName = listItem.getGroup();
                if (belongGroup.equals(groupName)) {
                    String listName = String.valueOf(listItem.getId());
                    BelongNo.append(listName + ",");
                }
            }
        }
        dbAdapter.close();

        return BelongNo.toString();
    }

    public void changeBelongNo() {
        int i, j,k,id,belongNo,beforeBelongNo,afterBelongNo;
        String name,name_read="ￚ no data ￚ";
        Boolean change=true;
        GroupListAdapter GPdbAdapter=new GroupListAdapter(this);

        if(beforeBelong!=null){
            beforeBelongNo= beforeBelong.length;
        }else{
            beforeBelongNo=0;
        }

        if(afterBelong!=null){
            afterBelongNo= afterBelong.length;
        }else{
            afterBelongNo=0;
        }

        for (i = 0; i < beforeBelongNo; i++) {
            for (j = 0; j < afterBelongNo; j++) {

                if(beforeBelong[i].equals(afterBelong[j])){
                    change=false;
                }
            }
            if(change){
                GPdbAdapter.open();
                for (k = 0; k < FragmentGroup.ListCount; k++) {
                    GroupListAdapter.Group listItem = FragmentGroup.nameList.get(k);
                    String groupName = listItem.getGroup();
                    if (beforeBelong[i].equals(groupName)) {
                        id = listItem.getId();
                        name=listItem.getGroup();
                        belongNo=listItem.getBelongNo()-1;
                        GPdbAdapter.updateGroup(id,name,name_read,belongNo);
                    }
                }
                GPdbAdapter.close();
            }
        }

        change=true;

        for (i = 0; i < afterBelongNo; i++) {
            for (j = 0; j < beforeBelongNo; j++) {
                if(afterBelong[i].equals(beforeBelong[j])){
                    change=false;
                }
            }
            if(change){
                GPdbAdapter.open();
                for (k = 0; k < FragmentGroup.ListCount; k++) {
                    GroupListAdapter.Group listItem = FragmentGroup.nameList.get(k);
                    String groupName = listItem.getGroup();
                    if (afterBelong[i].equals(groupName)) {
                        id = listItem.getId();
                        name=listItem.getGroup();
                        belongNo=listItem.getBelongNo()+1;
                        GPdbAdapter.updateGroup(id,name,name_read,belongNo);
                    }
                }
                GPdbAdapter.close();
            }
        }
    }

}







