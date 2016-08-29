package com.jinshu.xuzhi.learnpinyin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    View rootView;
    GridView alphabetsGridView;
    TextView character,pinyin,goToAlphabet;
    ImageView delete;
    int index = 0;
    //String tone = "āáǎàēéěèōóǒò";
    String [][]tone = {{"¯a","ā"},{"ˊa","á"},{"ˇa","ǎ"},{"ˋa","à"},
                        {"¯o","ō"},{"ˊo","ó"},{"ˇo","ǒ"},{"ˋo","ò"},
                        {"¯e","ē"},{"ˊe","é"},{"ˇe","ě"},{"ˋe","è"},
                        {"¯i","ī"},{"ˊi","í"},{"ˇi","ǐ"},{"ˋi","ì"},
                        {"¯u","ū"},{"ˊu","ú"},{"ˇu","ǔ"},{"ˋu","ù"},
                        {"¯ü","ǖ"},{"ˊü","ǘ"},{"ˇü","ǚ"},{"ˋü","ǜ"},
    };
    AdapterAlphabets mAdapter;
    private String alphabets = "abcdefghijklmnopqrstuüwxyz¯ˊˇˋ";
    private String[] alphabetsArray;
    static Typeface tf1;
    private final String LOG_TAG = this.getClass().getSimpleName();
    final MediaPlayer mp  = new MediaPlayer();
    String CONSTANTS_RES_PREFIX = "android.resource://com.jinshu.xuzhi.learnpinyin/";
    static final int PINYIN_LEARNING_LOADER = 0;
    class CharacterInfo{
        int id;
        String character;
        String sounds;
        String pinyin;
        String done;
        public  CharacterInfo(String name){
            id = 0;
            character = name;
            sounds = "";
            pinyin = sounds;
            done = LearnPinyinContract.NO;
        }
        public  CharacterInfo(){
            id = 0;
            character = "";
            sounds = "";
            pinyin = "";
            done = LearnPinyinContract.NO;
        }
    }
    CharacterInfo[] mCharacters;

    public MainActivityFragment() {

        alphabetsArray = alphabets.split("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        alphabetsGridView =  (GridView)rootView.findViewById(R.id.alphabet);
        character = (TextView)rootView.findViewById(R.id.character);
        pinyin =  (TextView)rootView.findViewById(R.id.pinyin);
        delete = (ImageView)rootView.findViewById(R.id.delete);
        goToAlphabet = (TextView)rootView.findViewById(R.id.go_to_alphabetTable);
        mAdapter = new AdapterAlphabets(getContext(),alphabetsArray);
        alphabetsGridView.setAdapter(mAdapter);

        tf1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/simkai.ttf");//设置卡片字体为楷体
        character.setTypeface(tf1);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        //character.setText(mCharacters[index].character);

       alphabetsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String content = alphabetsArray[position + 1];
                String currentPinyin = pinyin.getText().toString();
                //Log.e(LOG_TAG, "position = " + position);

                /*ü 遇到 j,q,x,y要去掉两点*/
              if (content.equals("ü") &&
                   (currentPinyin.contains("j")||currentPinyin.contains("q")||currentPinyin.contains("x")||currentPinyin.contains("y")))
                    content = "u";

                /*标音标，音标在最后标，标完后不能再输入其他拼音，只能删除*/

                if (position < 26)/*a-z*/ {
                    pinyin.setText(currentPinyin + content);
                } else/*四声音标*/ {
                    /*转换为标准四声音标显示*/
                    currentPinyin = convertToPinyinWithTone(currentPinyin, content);
                    pinyin.setText(currentPinyin);
                }
                /*输入至少一个字符后，显示删除符*/
                if (pinyin.getText().toString().length() == 1) {
                    delete.setVisibility(View.VISIBLE);
                }
                if (currentPinyin.equals(mCharacters[index].pinyin)) {
                    //character.setTextColor(getActivity().getResources().getColor(R.color.green));
                    //show success card
                    /*show character card */
                    Bundle bundle = new Bundle();
                    bundle.putString("character", mCharacters[index].character);
                    bundle.putString("pinyin", mCharacters[index].pinyin);
                    CardFragment fragment = new CardFragment();
                    fragment.setArguments(bundle);
                    fragment.show(getFragmentManager(), "CardFragment");
                    //play sound
                    try {
                        mp.reset();
                        AssetFileDescriptor afd = getActivity().getAssets().openFd("characters/"+ mCharacters[index].sounds +".mp3");
                        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();
                        mp.prepareAsync();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    //go to next pinyin card
                    index++;
                    if (index < mCharacters.length) {
                        //change to next pinyin
                        pinyin.setText("");
                        character.setText(mCharacters[index].character);
                        delete.setVisibility(View.GONE);
                    }
                    else
                    {
                        pinyin.setText("");
                        delete.setVisibility(View.GONE);
                    }


                }

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alphabetsGridView.setClickable(true);
                String currentPinyin = pinyin.getText().toString();
                pinyin.setText(currentPinyin.substring(0, currentPinyin.length() - 1));
                Log.e(LOG_TAG, pinyin.getText().toString());
                if (pinyin.getText().toString().length() == 0) {
                    delete.setVisibility(View.GONE);
                }
                if (!pinyin.getText().toString().equals("tiān")) {
                    character.setTextColor(getActivity().getResources().getColor(R.color.white));
                }
            }
        });

        goToAlphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityAlphabetsTable.class);
                startActivity(intent);
            }
        });
        return rootView;

    }

    String convertToPinyinWithTone(String dbSound,String dbTone)
    {
        String pinyin = "";
        String toneAlphabet = findToneAlphabet(dbSound);

        switch (dbTone){
            case "1":
                dbTone = "¯";
                break;
            case "2":
                dbTone = "ˊ";
                break;
            case "3":
                dbTone = "ˇ";
                break;
            case "4":
                dbTone = "ˋ";
                break;
            case "0":/*0 轻声 没有音标*/
                dbTone = "";
            default:/*0 轻声 没有音标*/
                break;
        }
        //Log.e(LOG_TAG, "dbTone = " + dbTone);
        if (dbTone.equals("")) return dbSound;

        String flag = dbTone + toneAlphabet;
        for (int i = 0; i < tone.length; i++) {
            if (flag.equals(tone[i][0])) {
                pinyin = dbSound.replaceFirst(tone[i][0].substring(1), tone[i][1]);
                break;
            }
        }
       // Log.e(LOG_TAG, "pinyin with tone = " + pinyin);
        return pinyin;
    }
    /*标音规则
    1． 如果一个音节只有一个元音，声调符号应标在元音上。
    2． 如果有两个元音，声调符号就按a,o,e,i,u,ü的顺序标记。
    3． i,u两个元音并列时，声调标在后面的元音上。如：tuī（推），qiú（球）
    4． 调号恰巧标在i的上面，那么i上的小点要省去。*/
    String findToneAlphabet(String currentPinyin)
    {
        String toneAlphabet="";
        String allFinals = "";
        final String format = "[aoeiuü]";//所有韵母字母
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(currentPinyin);
        while (matcher.find())/*查找并获取所有韵母*/
        {
            //Log.e(LOG_TAG,"matcher.group = " + matcher.group());
            allFinals = allFinals + matcher.group();
        }
        //Log.e(LOG_TAG, "allFinals = " + allFinals + ",length = " + allFinals.length());
        if(allFinals.length() == 1)
        {
            toneAlphabet = allFinals;
        }
        else if (allFinals.length() == 2)
        {
            if (allFinals.contains("ui") ||allFinals.contains("iu"))
            {
                toneAlphabet = allFinals.substring(1,2);
            }
            else {
                if (allFinals.contains("a")) toneAlphabet = "a";
                else if (allFinals.contains("o")) toneAlphabet = "o";
                else if (allFinals.contains("e")) toneAlphabet = "e";
                else if (allFinals.contains("i")) toneAlphabet = "i";
                else if (allFinals.contains("u")) toneAlphabet = "u";
                else if (allFinals.contains("ü")) toneAlphabet = "ü";
            }
        }
        else
        {
            if (allFinals.contains("a")) toneAlphabet = "a";
            else if (allFinals.contains("o")) toneAlphabet = "o";
            else if (allFinals.contains("e")) toneAlphabet = "e";
            else if (allFinals.contains("i")) toneAlphabet = "i";
            else if (allFinals.contains("u")) toneAlphabet = "u";
            else if (allFinals.contains("ü")) toneAlphabet = "ü";
        }
        //Log.e(LOG_TAG, "toneAlphabet = " + toneAlphabet);
        return toneAlphabet;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        getLoaderManager().restartLoader(PINYIN_LEARNING_LOADER, null, this);
        Log.e(LOG_TAG, "onResume");
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri uri = null;
        String sortOrder = LearnPinyinContract.Character.COLUMN_ID + " ASC";


        uri = LearnPinyinContract.Character.buildCharacterUriByIdList("1,2,3,4,5,6,7,8,9,10");
        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if ((cursor == null)||(cursor.getCount() == 0)) {
            Log.e(LOG_TAG, " return cursorLoader.getId() = " + cursorLoader.getId());
            return;
        }
        //int id = cursorLoader.getId();
        //String[] nameListString = nameList.split("");
        int cursorCount = cursor.getCount();
        //Log.v(LOG_TAG,"cursor.getCount() = " + cursor.getCount());
        cursor.moveToFirst();

        /*complete learning cards info*/
        mCharacters = new CharacterInfo[cursorCount];//first item is ""

        for (int i = 0;i < mCharacters.length;i++)
        {
            mCharacters[i] = new CharacterInfo();
            generateCharactersInfo(mCharacters[i],cursor);
            cursor.moveToNext();
        }

        character.setText(mCharacters[index].character);


        //UpdateDisplay(index);
        Log.e(LOG_TAG, "onLoadFinished");

    }
        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader)
        {    }

    void generateCharactersInfo(CharacterInfo info,Cursor cursor)
    {
        info.character = cursor.getString(cursor.getColumnIndex(LearnPinyinContract.Character.COLUMN_NAME));
        info.id = cursor.getInt(cursor.getColumnIndex(LearnPinyinContract.Character.COLUMN_ID));
        info.done = cursor.getString(cursor.getColumnIndex(LearnPinyinContract.Character.COLUMN_DONE));
        info.sounds =  cursor.getString(cursor.getColumnIndex(LearnPinyinContract.Character.COLUMN_PRONUNCIATION));
        info.pinyin = convertToPinyinWithTone(info.sounds.substring(0, info.sounds.length() - 1), info.sounds.substring(info.sounds.length() - 1, info.sounds.length()));
    }

    public static class CardFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog, null);
            TextView character = (TextView)view.findViewById(R.id.character);
            TextView pinyin = (TextView)view.findViewById(R.id.pinyin);
            character.setText(getArguments().getString("character"));
            pinyin.setText(getArguments().getString("pinyin"));
            character.setTypeface(tf1);
            builder.setView(view);
            //TextView view = new TextView(getActivity());
            //view.setText("b");
            //view.setBackgroundResource(R.drawable.card_border);
            //builder.setView(view);
            return builder.create();
        }
    }
    @Override
    public void onDestroy() {
        mp.release();
        Log.v(LOG_TAG, "onDestroy");
        super.onDestroy();

    }
}
