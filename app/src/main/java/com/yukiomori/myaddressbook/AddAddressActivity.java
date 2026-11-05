package com.yukiomori.myaddressbook;

import static com.yukiomori.myaddressbook.R.*;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

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
        //R.id.mainを変更
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroll_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //findViewByIdをまとめたメソッド
        initViews();
        //保存ボタンが押された時、データを保存する処理をする
        Button btnSave=findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v->{
            saveAddressData();
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

    private saveAddressData{
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

        //入力チェック(バリデーション)
        //20261105まで。続きを書くこと
    }
}