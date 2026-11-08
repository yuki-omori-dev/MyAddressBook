package com.yukiomori.myaddressbook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContactDetailActivity extends AppCompatActivity {
    //AddAddressActivityを模して作るのでコピーして転用
    private EditText etLastName, etFirstName, etLastNameKana, etFirstNameKana;
    private EditText etZipCode, etAddress, etBuildingRoom, etPhone, etEmail, etMemo;
    private Button btnBack,btnEdit;
    //現在が閲覧モードか編集モードかを判定する変数
    private boolean isEditMode=false;
    //DBを扱うので準備
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_contact_detail);
        //AddAddressActivityの転用
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });
        initViews();
        dbHelper = new DatabaseHelper(this);

        // IntentからUUIDを受け取る処理（あとで忘れずに書くこと）

        // ボタンイベントの設定（あとで忘れずに書くこと）
    }

    private void initViews() {
        etLastName=findViewById(R.id.edit_last_name);
        etFirstName=findViewById(R.id.edit_first_name);
        etLastNameKana=findViewById(R.id.edit_last_name_kana);
        etFirstNameKana=findViewById(R.id.edit_first_name_kana);
        etZipCode=findViewById(R.id.edit_zip_code);
        etAddress=findViewById(R.id.edit_address);
        etBuildingRoom=findViewById(R.id.edit_building_room);
        etPhone=findViewById(R.id.edit_phone);
        etEmail=findViewById(R.id.edit_email);
        etMemo=findViewById(R.id.edit_memo);

        btnBack=findViewById(R.id.btn_back);
        btnEdit=findViewById(R.id.btn_edit);
    }
}