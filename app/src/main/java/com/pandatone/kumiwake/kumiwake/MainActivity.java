
package com.pandatone.kumiwake.kumiwake;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.MemberMain;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.pandatone.kumiwake.sekigime.SekigimeDescription;
import com.pandatone.kumiwake.setting.SettingHelp;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener {
    public List<Category> mCategories;
    private Category kumiwake,member,sekigime,setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        setContentView(R.layout.activity_main);
        setViews();

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2315101868638564/8665451539");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build();
        mAdView.loadAd(adRequest);

    }

    public void onClick(View v){
        Category category;
        Resources res = getResources();
        String mPackageName = getPackageName();
        ImageView icon;
        TextView title;
        switch (v.getId()) {
            case R.id.category_item1:
                category=mCategories.get(0);
                break;
            case R.id.category_item2:
                category=mCategories.get(1);
                break;
            case R.id.category_item3:
                category=mCategories.get(2);
                break;
            default:
                category=mCategories.get(3);
                break;
        }
        icon = (ImageView) v.findViewById(res.getIdentifier(
                "category_icon" + category.getId(), "id", mPackageName));
        title = (TextView) v.findViewById(res.getIdentifier(
                "category_title" + category.getId(), "id", mPackageName));
        startActivityWithTransition(category.getId(),icon,title);
        }

    public void setViews(){
        FrameLayout v;
        ImageView icon;
        TextView title;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        int halfScreenWidth = (int)(screenWidth *0.5);
        int miniScreenHeight = (int)(screenHeight * 0.34);

        Resources res = getResources();
        String mPackageName = getPackageName();
        kumiwake = new Category(getString(R.string.kumiwake),"1", Theme.red);
        member = new Category(getString(R.string.member),"2", Theme.blue);
        sekigime = new Category(getString(R.string.sekigime),"3",Theme.green);
        setting = new Category(getString(R.string.setting_help),"4",Theme.yellow);
        mCategories= Arrays.asList(kumiwake,member,sekigime,setting);

        for(int i=0;i<4;i++) {
            Category category = mCategories.get(i);
                    v=(FrameLayout)findViewById(res.getIdentifier(
                            "category_item" + category.getId(), "id", mPackageName));
                    icon = (ImageView) v.findViewById(res.getIdentifier(
                            "category_icon" + category.getId(), "id", mPackageName));
                    title = (TextView) v.findViewById(res.getIdentifier(
                            "category_title" + category.getId(), "id", mPackageName));

            v.getLayoutParams().width=halfScreenWidth;
            v.getLayoutParams().height=miniScreenHeight;
            v.setBackgroundColor(getResources().getColor(category.getTheme().getWindowBackgroundColor()));
            icon.setImageResource(res.getIdentifier(
                    "icon_" + category.getId(), "drawable", mPackageName));
            title.setText(category.getName());
            title.setTextColor(getResources().getColor(category.getTheme().getTextPrimaryColor()));
            title.setBackgroundColor(getResources().getColor(category.getTheme().getPrimaryColor()));
            v.setOnClickListener(this);
        }
    }

    public void startActivityWithTransition(String id,ImageView iv,TextView tv) {
        final Intent intent;

        switch (id) {
            case "1":
                intent = new Intent(this, KumiwakeSelectMode.class);
                KumiwakeSelectMode.sekigime = false;
                break;
            case "2":
                intent = new Intent(this, MemberMain.class);
                break;
            case "3":
                intent = new Intent(this, SekigimeDescription.class);
                break;
            default:
                intent = new Intent(this, SettingHelp.class);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this,tv,"explodeTv").toBundle());
        }
        else{
            startActivity(intent);
        }
    }

    public static Context getContext() {
        return MemberMain.getContext();
    }

}
