package com.jinshu.xuzhi.learnpinyin;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract;

import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentAlphabetsTable extends Fragment {
    View rootView;
    GridView alphabetTable;
    ImageView bpmImageView,aoeImageView,goView,settingsView;
    AdapterAlphabetTable mbpmAdapter,maoeAdapter;
    private final String LOG_TAG = this.getClass().getSimpleName();
    String bpm = "b,p,m,f,d,t,n,l,g,k,h,j,q,x,r,z,c,s,y,w,zh,ch,sh";
    String aoe = "a,o,e,i,u,ü,ai,ei,ui,ao,ou,iu,ie,üe,er,an,en,un,in,ün,ang,eng,ing,ong";
    String[] bpmArray,aoeArray;
    String CONSTANTS_RES_PREFIX = "android.resource://com.jinshu.xuzhi.learnpinyin/";
    final MediaPlayer mp  = new MediaPlayer();
    PopupWindow mPopupWindow;
    Button clearExciseRecord;

    public FragmentAlphabetsTable() {
        bpmArray = bpm.split(",");
        aoeArray = aoe.split(",");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_alphabets_table, container, false);
        alphabetTable = (GridView)rootView.findViewById(R.id.alphabet_table);
        bpmImageView = (ImageView)rootView.findViewById(R.id.image_bpm);
        aoeImageView = (ImageView)rootView.findViewById(R.id.image_aoe);
        settingsView = (ImageView)rootView.findViewById(R.id.settings);
        goView = (ImageView)rootView.findViewById(R.id.go);
        mbpmAdapter = new AdapterAlphabetTable(getActivity(),bpmArray);
        maoeAdapter = new AdapterAlphabetTable(getActivity(),aoeArray);
        alphabetTable.setAdapter(mbpmAdapter);


        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_window_menu, null);

        mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        mPopupWindow.getContentView().setFocusableInTouchMode(true);
        mPopupWindow.getContentView().setFocusable(true);
        mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar_bottom);

        clearExciseRecord = (Button)popupView.findViewById(R.id.clear_excise_record);
        clearExciseRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues value = new ContentValues();
                String newStatus = LearnPinyinContract.NO;
                value.put(LearnPinyinContract.Character.COLUMN_DONE, newStatus);
                getActivity().getContentResolver().update(LearnPinyinContract.Character.CONTENT_URI, value, null, null);
            }
        });

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        alphabetTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView currentAlphabet = (TextView) view;
                String alpha = currentAlphabet.getText().toString().replace("ü", "v");

                try {
                    mp.reset();

                    int alphaId = getActivity().getResources().getIdentifier(alpha, "raw", "com.jinshu.xuzhi.learnpinyin");
                    String uriString = CONSTANTS_RES_PREFIX + Integer.toString(alphaId);
                    mp.setDataSource(getActivity(), Uri.parse(uriString));
                    mp.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        bpmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alphabetTable.setAdapter(mbpmAdapter);
                bpmImageView.setImageResource(R.drawable.bpm);
                aoeImageView.setImageResource(R.drawable.aoe_white);
            }
        });
        aoeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alphabetTable.setAdapter(maoeAdapter);
                bpmImageView.setImageResource(R.drawable.bpm_white);
                aoeImageView.setImageResource(R.drawable.aoe);
            }
        });

        goView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra(LearnPinyinContract.Character.COLUMN_DONE, "");/*仅作为一个标记，不需要值*/
                startActivity(intent);
            }
        });

        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.showAsDropDown(goView);

            }
        });
        return rootView;
    }


    @Override
    public void onDestroy() {
        mp.release();
        Log.v(LOG_TAG, "onDestroy");
        super.onDestroy();

    }
}
