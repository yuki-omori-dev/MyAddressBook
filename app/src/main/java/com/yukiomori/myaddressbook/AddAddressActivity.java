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
        //戻るボタンイベントの設定。入力途中だったときバック処理に飛ぶ
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        //保存ボタンが押された時、データを保存する処理をする
        Button btnSave=findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v->{
            saveDatabase();
        });
        //戻るが押されたときの警告ダイアログ処理
        //onBackPressed()が非推奨なので、onCreate内でOnBackPressedDispatcherを使ってコールバックを登録
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (ValidationUtils.hasUnsavedChanges(
                        etLastName, etFirstName, etLastNameKana, etFirstNameKana,
                        etZipCode, etAddress, etBuildingRoom, etPhone, etEmail, etMemo)) {
                    // 未保存の入力がある場合のみダイアログを表示
                    new androidx.appcompat.app.AlertDialog.Builder(AddAddressActivity.this)
                            .setTitle("確認")
                            .setMessage("入力中の内容があります。どうしますか？")
                            .setPositiveButton("保存して戻る", (dialog, which) -> {
                                saveDatabase(); // 保存処理
                            })
                            .setNegativeButton("破棄して戻る", (dialog, which) -> {
                                // 一時的にこのコールバックを無効化して、再度戻る操作をシステムに流す
                                setEnabled(false);
                                getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNeutralButton("キャンセル", (dialog, which) -> {
                                // 何もしない（入力画面に留まる）
                            })
                            .show();
                } else {
                    // 未保存の入力がなければそのまま戻る
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
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
        String lastName = ValidationUtils.getEditTextString(etLastName);
        String firstName = ValidationUtils.getEditTextString(etFirstName);
        String lastNameKana = ValidationUtils.getEditTextString(etLastNameKana);
        String firstNameKana = ValidationUtils.getEditTextString(etFirstNameKana);
        String zipCode = ValidationUtils.getEditTextString(etZipCode);
        String address = ValidationUtils.getEditTextString(etAddress);
        String buildingRoom = ValidationUtils.getEditTextString(etBuildingRoom);
        String phone = ValidationUtils.getEditTextString(etPhone);
        String email = ValidationUtils.getEditTextString(etEmail);
        String memo = ValidationUtils.getEditTextString(etMemo);

        //入力チェック(バリデーション)。性か名が入力されていれば登録できることにした
        //チェック項目を増やした
        //将来的にxmlの入力欄をTextInputLayoutで囲い、トーストではなく赤いメッセージを出力するようにしたい
        String errorMessage=ValidationUtils.validateContactInput(
                lastName,firstName,lastNameKana,firstNameKana,zipCode,phone,email
        );

        if (errorMessage!=null) {
            Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
            return;
        }

        //こちらで想定している最低限の情報が入力されているので、セッターで入力情報を登録
        Contact contact=Contact.createFromInput(
                lastName,firstName,lastNameKana,firstNameKana,
                zipCode,address,buildingRoom,phone,email,memo
        );
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