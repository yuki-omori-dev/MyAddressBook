package com.yukiomori.myaddressbook;

import static com.yukiomori.myaddressbook.R.*;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

public class AddAddressActivity extends AppCompatActivity {
    //EditTextのデータを格納するための変数。慣習として変数名の頭にetが付くようだ
    private EditText etLastName, etFirstName, etLastNameKana, etFirstNameKana;
    private EditText etZipCode, etAddress, etBuildingRoom, etPhone, etEmail, etMemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(layout.activity_add_address);
        //ツールバーのセットアップ
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //ツールバーがステータスバーと重ならないようにする処理
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        //キーボードが出現した時に入力欄をキーボードよりも上に持ち上げたように見せた時、座標がズレるのを解決するためxmlに
        // android:id="@+id/layout_container"を追記し、scroll.viewではなく中のlayout_containerに余白を設定する
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            //キーボードが出現した時に、選択した項目にキーボードが被さっていたらスクロールする
            //ナビゲーションバーまたはキーボードの大きい方の余白をセット
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            //キーボードが出現したかの判定
            boolean isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            //出現したら250ms待ってから作動する処理
            if (isImeVisible) {
                v.postDelayed(() -> {
                    View focusedView = getCurrentFocus();
                    if (focusedView != null) {
                        //選択されている入力欄の画面上の下端座標を計算
                        //タップされた入力欄focusViewのY座標location[1]に、入力欄自体の高さを足し入力欄の一番下の位置を求める
                        int[] location = new int[2];
                        focusedView.getLocationOnScreen(location);
                        int viewBottomOnScreen = location[1] + focusedView.getHeight();

                        //キーボードの最上部の画面上座標を計算
                        //画面全体の高さrootHeightからキーボードの高さimeInsets.bottomを引き、キーボードの一番上の位置を決める
                        int rootHeight = v.getRootView().getHeight();
                        int keyboardTopOnScreen = rootHeight - imeInsets.bottom;

                        //入力欄がキーボードの下に隠れている場合のみ、被っている分だけスクロール
                        if (viewBottomOnScreen > keyboardTopOnScreen) {
                            int overlap = viewBottomOnScreen - keyboardTopOnScreen + 48; // 余白48px分余裕を持たせる
                            ScrollView scrollView = findViewById(R.id.scroll_view);
                            scrollView.smoothScrollBy(0, overlap);
                        }
                    }
                }, 250);
            }

            return insets;
        });
        //findViewByIdをまとめたメソッド
        initViews();
        //保存ボタンが押された時、データを保存する処理をする
        Button btnSave=findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v->{
            saveDatabase();
        });
        //戻るが押されたときの警告ダイアログ処理
        //は後で書く
    }

    private void initViews(){
        etLastName = findViewById(id.edit_last_name);
        etFirstName = findViewById(id.edit_first_name);
        etLastNameKana = findViewById(id.edit_last_name_kana);
        etFirstNameKana = findViewById(id.edit_first_name_kana);
        etZipCode = findViewById(id.edit_zip_code);
        etAddress = findViewById(id.edit_address);
        etBuildingRoom = findViewById(id.edit_building_room);
        etPhone = findViewById(id.edit_phone);
        etEmail = findViewById(id.edit_email);
        etMemo = findViewById(id.edit_memo);
    }
    // メニューを表示する機能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_detail, menu);
        return true;
    }
    // メニューが押された時の処理
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            //これは使いまわしの仮設定なので、メニューの内容と処理が決まったら書く
            return true;
        } else if (id == R.id.action_delete) {
            //上に同じ
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //EditViewに入力されたデータをDBに保存し、結果をユーザに報告する
    private void saveDatabase(){
        String lastName=etLastName.getText().toString().trim();
        String firstName=etFirstName.getText().toString().trim();
        String lastNameKana=etLastNameKana.getText().toString().trim();
        String firstNameKana=etFirstNameKana.getText().toString().trim();
        String zipCode=etZipCode.getText().toString().trim();
        String address=etAddress.getText().toString().trim();
        String buildingRoom=etBuildingRoom.getText().toString().trim();
        String phone=etPhone.getText().toString().trim();
        String email=etEmail.getText().toString().trim();
        String memo=etMemo.getText().toString().trim();

        //入力チェック(バリデーション)。性か名が入力されていれば登録できることにした
        //将来的にxmlの入力欄をTextInputLayoutで囲い、トーストではなく赤いメッセージを出力するようにしたい
        if(lastName.isEmpty()&&firstName.isEmpty()){
            Toast.makeText(AddAddressActivity.this, "姓か名を入力してください",Toast.LENGTH_SHORT).show();
            return;
        }
        //姓が入力されているのに、姓のふりがなが空ならNG
        if (!lastName.isEmpty() && lastNameKana.isEmpty()) {
            Toast.makeText(this, "みょうじ(ふりがな)を入力してください",Toast.LENGTH_SHORT).show();
            return;
        }

        //名が入力されているのに、名のふりがなが空ならNG
        if (!firstName.isEmpty() && firstNameKana.isEmpty()) {
            Toast.makeText(this, "なまえ(ふりがな)を入力してください",Toast.LENGTH_SHORT).show();
            return;
        }

        //最低限の情報があるので、セッターで入力情報を登録
        Contact contact=new Contact();
        contact.setLastName(lastName);
        contact.setFirstName(firstName);
        contact.setLastNameKana(lastNameKana);
        contact.setFirstNameKana(firstNameKana);
        contact.setZipCode(zipCode);
        contact.setAddress(address);
        contact.setBuildingRoom(buildingRoom);
        contact.setPhone(phone);
        contact.setEmail(email);
        contact.setMemo(memo);
        //DBに保存する
        DatabaseHelper dbHelper=new DatabaseHelper(AddAddressActivity.this);
        long result=dbHelper.insertContact(contact);
        //結果を判定しユーザに返す
        if (result!=-1){
            Toast.makeText(this,"保存しました",Toast.LENGTH_SHORT).show();
            finish();   //MainActivityに戻る
        } else {
            Toast.makeText(this,"保存に失敗しました",Toast.LENGTH_SHORT).show();
        }

    }

}