package com.yukiomori.myaddressbook;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewTreeLifecycleOwner;

public class ContactDetailActivity extends AppCompatActivity {
    //AddAddressActivityを模して作るのでコピーして転用
    private EditText etLastName, etFirstName, etLastNameKana, etFirstNameKana;
    private EditText etZipCode, etAddress, etBuildingRoom, etPhone, etEmail, etMemo;
    private Button btnBack,btnEdit;
    private boolean isEditMode=false;   //現在が閲覧モードか編集モードかを判定する変数
    private DatabaseHelper dbHelper;    //DBを扱うので準備
    private String currentUuid;         //Intentで受け取るデータの受け皿の宣言

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

        //IntentからUUIDを受け取る処理
        currentUuid=getIntent().getStringExtra("EXTRA_UUID");
        //DBからデータを取得して画面に表示する
        if(currentUuid!=null){
            Contact contact=dbHelper.getContactByUuid(currentUuid);
            if(contact!=null){
                //取得したContactの各項目を画面にセット
                setContactToViews(contact);
            }
        }
        //入力を不可にしてこれを閲覧モードとする
        setFieldsEnabled(false);
        //戻るボタンイベントの設定。編集途中だったときに尋ねるようにするか検討
        btnBack.setOnClickListener(v->finish());
        //編集ボタンを押したら保存モードへ移行し、ボタン名を保存ボタンに
        btnEdit.setOnClickListener(v->{
            if(!isEditMode){
                isEditMode=true;
                setFieldsEnabled(true); //isEditModeを引数にすると警告されるのでtrueとした
                btnEdit.setText("保存する");
                Toast.makeText(this,"編集モードになりました",Toast.LENGTH_SHORT).show();
            }else{
                //データを上書き更新して閲覧モードに戻る処理
                saveUpdatedContact();
            }
        });
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

    // 取得したContactデータを各EditTextにセットする
    private void setContactToViews(Contact contact) {
        etLastName.setText(contact.getLastName());
        etFirstName.setText(contact.getFirstName());
        etLastNameKana.setText(contact.getLastNameKana());
        etFirstNameKana.setText(contact.getFirstNameKana());
        etZipCode.setText(contact.getZipCode());
        etAddress.setText(contact.getAddress());
        etBuildingRoom.setText(contact.getBuildingRoom());
        etPhone.setText(contact.getPhone());
        etEmail.setText(contact.getEmail());
        etMemo.setText(contact.getMemo());
    }
    // 各EditTextの入力の可/不可(enabled)を一括切り替えするメソッド
    private void setFieldsEnabled(boolean enabled) {
        setEditTextEditable(etLastName, enabled);
        setEditTextEditable(etFirstName, enabled);
        setEditTextEditable(etLastNameKana, enabled);
        setEditTextEditable(etFirstNameKana, enabled);
        setEditTextEditable(etZipCode, enabled);
        setEditTextEditable(etAddress, enabled);
        setEditTextEditable(etBuildingRoom, enabled);
        setEditTextEditable(etPhone, enabled);
        setEditTextEditable(etEmail, enabled);
        setEditTextEditable(etMemo, enabled);
    }
    //フォーカスを無効にする仕組み
    private void setEditTextEditable(EditText editText, boolean editable) {
        editText.setFocusable(editable);            //キーボードフォーカスの有効/無効
        editText.setFocusableInTouchMode(editable); //タップ時のフォーカスの有効/無効
        editText.setCursorVisible(editable);        //点滅カーソルの表示/非表示

        //閲覧モード時の文字の色を指定できるように。必要ないなら消してもよい
        editText.setTextColor(Color.BLACK);
    }

    // 変更されたデータをDBに上書き保存する
    private void saveUpdatedContact() {
        String lastName = etLastName.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();

        // 入力チェック(姓か名のどちらかは必須)
        if (lastName.isEmpty() && firstName.isEmpty()) {
            Toast.makeText(this, "姓か名を入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        //既存のUUIDを維持して新しいContactオブジェクトを作成
        Contact updatedContact = new Contact(
                currentUuid,
                lastName,
                firstName,
                etLastNameKana.getText().toString().trim(),
                etFirstNameKana.getText().toString().trim(),
                etZipCode.getText().toString().trim(),
                etAddress.getText().toString().trim(),
                etBuildingRoom.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etMemo.getText().toString().trim()
        );
        //DBを更新(UPDATE)
        int result = dbHelper.updateContact(updatedContact);

        if (result > 0) {
            Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
            //編集モードを終了して閲覧モードに戻す
            isEditMode = false;
            setFieldsEnabled(false);
            btnEdit.setText("編集する");
        } else {
            Toast.makeText(this, "更新に失敗しました", Toast.LENGTH_SHORT).show();
        }
    }

}