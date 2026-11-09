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
    private List<Contact> contactList;  //保持したほうが扱いやすいのでメンバにしておく

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
                //入力されたキーワードを取得(前後の空白を消して小文字化)
                //すべて小文字にすることでのちほど大文字小文字の区別なくヒットさせるようにする
                String query=s.toString().trim().toLowerCase();

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
                    if (fullName.contains(query)||fullNameKana.contains(query)||phone.contains(query)){
                        filteredList.add(contact);
                    }
                }

                //絞り込み後のリストをアダプターにセットして表示を更新
                ContactAdapter adapter=new ContactAdapter(MainActivity.this, filteredList);
                listView.setAdapter(adapter);
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

        // 昇順に並べ替え
        if (id == R.id.action_sort_asc) {
            if (contactList != null) {
                contactList.sort((c1, c2) -> {
                    int result = c1.getLastNameKana().compareTo(c2.getLastNameKana());
                    if (result == 0) {
                        result = c1.getFirstNameKana().compareTo(c2.getFirstNameKana());
                    }
                    return result;
                });
                //画面(ListView)のアダプターを再セットして表示を更新する
                listView.setAdapter(new ContactAdapter(this, contactList));
            }
            return true;

            // 降順に並べ替え
        } else if (id == R.id.action_sort_desc) {
            if (contactList != null) {
                contactList.sort((c1, c2) -> {
                    int result = c2.getLastNameKana().compareTo(c1.getLastNameKana());
                    if (result == 0) {
                        result = c2.getFirstNameKana().compareTo(c1.getFirstNameKana());
                    }
                    return result;
                });
                //画面(ListView)のアダプターを再セットして表示を更新する
                listView.setAdapter(new ContactAdapter(this, contactList));
            }
            return true;

            //登録順(デフォルト)に戻す
        } else if (id == R.id.action_sort_default) {
            loadContactList(); // 再読み込みしてデフォルト表示に戻す
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadContactList(){
        //DBから全データを取得
        contactList=dbHelper.getAllContacts();  //取得したリストをメンバに保持
        //カスタムアダプターをインスタンス化
        ContactAdapter adapter=new ContactAdapter(this,contactList);
        //ListViewにAdapterをセット
        listView.setAdapter(adapter);
    }
}