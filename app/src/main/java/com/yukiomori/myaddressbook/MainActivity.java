package com.yukiomori.myaddressbook;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

    private void loadContactList(){
        //DBから全データを取得
        contactList=dbHelper.getAllContacts();  //取得したリストをメンバに保持
        //ListViewに表示するためのString型のリストを作る
        List<String> displayNames=new ArrayList<>();
        for(Contact contact:contactList){
            //性と名を連結する
            String name=(contact.getLastName()+" "+contact.getFirstName().trim());
            displayNames.add(name);
        }
        //アダプターを作成
        ArrayAdapter<String> adapter=new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayNames
        );
        //ListViewにAdapterをセット
        listView.setAdapter(adapter);
    }
}