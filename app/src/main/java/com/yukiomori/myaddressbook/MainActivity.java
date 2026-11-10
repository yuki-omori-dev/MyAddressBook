package com.yukiomori.myaddressbook;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private DatabaseHelper dbHelper;
    //保持したほうが扱いやすいのでメンバにしておく
    //検索窓が空のときにポートレートランドスケープが切り替わるとエラーを起こすので、初期化しておく
    //onTextChangedが先に動き出してnull状態のリストを読み込もうとしてヌポイが発生するため
    private List<Contact> contactList=new ArrayList<>();
    private String currentSortOrder = null; //SQL命令用の保持用変数
    //検索窓に入力するたびにオブジェクトを作るのが効率が悪いのでAdapterもメンバを作る
    private ContactAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        //作成したツールバーをセットアップ
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //ツールバーがステータスバーと重なっていたので上部の余白を開ける処理
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //FABのID取得。のちのちViewBindingへの置き換えを試したい
        FloatingActionButton btnAdd=findViewById(R.id.btn_Add);
        //ほかの初期化の処理
        listView=findViewById(R.id.list_view);
        dbHelper=new DatabaseHelper(this);
        //タップされたときAddAddressActivityにジャンプ。ということをラムダ式で記述
        btnAdd.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this,AddAddressActivity.class);
            startActivity(intent);
        });
        //検索バーに文字が入力されるとリアルタイムで検索するリスナーを設定
        EditText etSearch=findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            @Override
            //文字が変更されている最中・直後に反応する
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                //ポートレートランドスケープ時のエラー対策2
                //contactListがnullなら何もしない
                if (contactList == null) {
                    return;
                }
                //入力されたキーワードを取得(前後の空白を消して小文字化)
                //すべて小文字にすることでのちほど大文字小文字の区別なくヒットさせるようにする
                String query=s.toString().trim().toLowerCase();
                //検索ワードのカタカナ変換
                String queryKatakana=hiraganaToKatakana(query);
                //絞り込み用の新しいリストを作成
                List<Contact> filteredList=new ArrayList<>();

                for (Contact contact : contactList) {
                    //姓と名、みょうじとなまえを結合したフルネームと電話番号をリストから取り出して小文字にする
                    //漢字やひらがなを小文字にしようとしてもスルーされるだけでエラーは起きない
                    String fullName=contact.getLastName()+contact.getFirstName().toLowerCase();
                    String fullNameKana=contact.getLastNameKana()+contact.getFirstNameKana().toLowerCase();
                    String phone=contact.getPhone().toLowerCase();

                    //部分一致の判定(queryが含まれているか)
                    //queryが空のとき""(空文字)となり、Javaではどんな文字列でも""は必ず含まれているという仕様になっている
                    //つまりすべての連絡先がヒットするので、結果的に初期状態のようにすべてのリストが表示される
                    //カタカナの名前もヒットするように調整
                    if (fullName.contains(query)||fullName.contains(queryKatakana)
                            ||fullNameKana.contains(query)||fullNameKana.contains(queryKatakana)
                            ||phone.contains(query)){
                        filteredList.add(contact);
                    }
                }

                //絞り込んだリストで既存のアダプターの中を更新し、再描画を通知
                if (adapter != null) {
                    adapter.clear();
                    adapter.addAll(filteredList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });


        //一覧の項目がタップされたときの処理
        listView.setOnItemClickListener((parent, view, position, id)->{
            //タップされた行(position)のContactオブジェクトを取得
            Contact selectedContact=contactList.get(position);
            //ContactDetailActivityへのIntentを作成
            Intent intent=new Intent(MainActivity.this,ContactDetailActivity.class);
            //選択された連絡先のUUIDをIntentに持たせる(キー名:"EXTRA_UUID")
            intent.putExtra("EXTRA_UUID",selectedContact.getUuid());
            //画面遷移をする
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContactList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //メニューを表示する機能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    // メニューが押された時の処理
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_asc) {
            //昇順
            currentSortOrder = "last_name_kana ASC, first_name_kana ASC";
            loadContactList();
            return true;
        } else if (id == R.id.action_sort_desc) {
            // 降順
            currentSortOrder = "last_name_kana DESC, first_name_kana DESC";
            loadContactList();
            return true;
        } else if (id == R.id.action_sort_default) {
            //デフォルト(登録順)
            currentSortOrder = null;
            loadContactList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadContactList(){
        contactList = dbHelper.getAllContacts(currentSortOrder);

        //初回のみ生成し、2回目以降は中身を更新して通知
        if (adapter == null) {
            adapter = new ContactAdapter(this, contactList);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(contactList);
            adapter.notifyDataSetChanged(); //再描画を通知
        }
    }

    //ひらがなをカタカナに変換するメソッド
    private String hiraganaToKatakana(String str){
        if(str==null){return "";}
        StringBuilder sb=new StringBuilder(str);
        for(int i=0;i<sb.length();i++){
            char c=sb.charAt(i);
            //ひらがなの範囲(ぁ:0x3041_ん:0x3093)
            if(c>='\u3041'&&c<='\u3093'){
                sb.setCharAt(i,(char)(c+0x60)); //カタカナに変換
            }
        }
        return sb.toString();
    }
}