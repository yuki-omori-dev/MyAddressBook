package com.yukiomori.myaddressbook;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    //編集モードに入ったときに開始時点のテキストを保持するメンバ
    private String initialLastName = "", initialFirstName = "", initialLastNameKana = "", initialFirstNameKana = "";
    private String initialZipCode = "", initialAddress = "", initialBuildingRoom = "", initialPhone = "", initialEmail = "", initialMemo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_contact_detail);
        //作成したツールバーをセットアップ
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //ツールバーがステータスバーと重なっていたので上部の余白を開ける処理
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        //AddAddressActivityの転用
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });
        //編集モード時に入力途中で戻るが押された時の処理
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 「編集モード中」かつ「未保存の入力がある」場合のみダイアログを表示
                if (isEditMode && hasTextChanged()) {
                    new androidx.appcompat.app.AlertDialog.Builder(ContactDetailActivity.this) // ※クラス名に注意
                            .setTitle("確認")
                            .setMessage("編集中の内容があります。どうしますか？")
                            .setPositiveButton("保存して戻る", (dialog, which) -> {
                                // データを上書き保存したあと、画面を閉じて前画面(MainActivity)に戻る
                                if (saveUpdatedContactAndFinish()) {
                                    finish();
                                }
                            })
                            .setNegativeButton("破棄して戻る", (dialog, which) -> {
                                // 保存せずにそのまま戻る
                                setEnabled(false);
                                getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNeutralButton("キャンセル", (dialog, which) -> {
                                // 何もしない（編集画面に留まる）
                            })
                            .show();
                } else {
                    // 閲覧モードのときや、変更がないときはそのまま戻る
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
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
        //戻るボタンイベントの設定。編集途中だったときバック処理に飛ぶ
        btnBack.setOnClickListener(v->getOnBackPressedDispatcher().onBackPressed());
        //編集ボタンを押したら保存モードへ移行し、ボタン名を保存ボタンに
        btnEdit.setOnClickListener(v->{
            toggleEditMode();
        });
        //電話の部分をタップしたら電話を起動
        etPhone.setOnClickListener(v -> {
            //閲覧モードのときのみ実行
            if (!isEditMode) {
                String phone = etPhone.getText().toString().trim();
                if (!phone.isEmpty()) {
                    //ダイヤル画面を開いて番号を入力状態にするところまでのアクション
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
            }
        });
        //メールの部分をタップしたらメールを起動
        etEmail.setOnClickListener(v -> {
            //閲覧モードのときのみ実行
            if (!isEditMode) {
                String email = etEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    startActivity(intent);
                }
            }
        });
        //住所をタップしたらマップを起動
        etAddress.setOnClickListener(v -> {
            //閲覧モードのときのみ実行
            if (!isEditMode) {
                String address = etAddress.getText().toString().trim()+etBuildingRoom.getText().toString().trim();
                if (!address.isEmpty()) {
                    // 住所文字列をエンコードしてURIを作成
                    Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                    Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
                    startActivity(intent);
                }
            }
        });
    }
    //menu形式で作成したXMLを読み込んで使う
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_detail, menu);
        return true;
    }
    //メニューが押された時の処理
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            toggleEditMode();
            return true;
        } else if (id == R.id.action_delete) {
            //誤操作防止の確認ダイアログを作る
            new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("この連絡先を削除してもよろしいですか？")
                    .setPositiveButton("削除",(dialog,which)->{
                        int result = dbHelper.deleteContact(currentUuid);
                        if (result > 0) {
                            Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "削除に失敗しました", Toast.LENGTH_SHORT).show();
                        }
                    })
                            .setNegativeButton("キャンセル",null)
                                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    //編集モードに移行する処理をメソッド化した
    private void toggleEditMode(){
        if(!isEditMode){
            isEditMode=true;
            setFieldsEnabled(true); //isEditModeを引数にすると警告されるのでtrueとした
            btnEdit.setText("保存する");
            Toast.makeText(this,"編集モードになりました",Toast.LENGTH_SHORT).show();
            //編集モードに入った時点でのEditTextの値を保持
            updateInitialValues();
        }else{
            //データを上書き更新して閲覧モードに戻る処理
            saveUpdatedContact();
        }
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

        //閲覧モード時の文字の色を指定できるように
        if (editable) {
            editText.setTextColor(Color.BLACK); //編集モード時:黒文字
        } else {
            //閲覧モード時:電話番号や住所はタップ可能だと分かるように青色にする
            if (editText == etPhone || editText == etAddress || editText == etEmail) {
                editText.setTextColor(Color.BLUE);
            } else {
                editText.setTextColor(Color.BLACK);
            }
        }
    }

    //更新を保存してMainに戻るモードと更新を保存して閲覧モードに戻るパターンの2つを条件分岐でまとめたもの
    private boolean saveContact(boolean finishAfterSave) {
        String lastName=etLastName.getText().toString().trim();
        String firstName=etFirstName.getText().toString().trim();
        String lastNameKana=etLastNameKana.getText().toString().trim();
        String firstNameKana=etFirstNameKana.getText().toString().trim();
        String zipCode=etZipCode.getText().toString().trim();
        String phone=etPhone.getText().toString().trim();
        String email=etEmail.getText().toString().trim();

        // 1. 入力チェック（バリデーション）
        //姓か名は入力させる
        if (!ValidationUtils.hasName(lastName,firstName)) {
            Toast.makeText(this, "姓か名を入力してください", Toast.LENGTH_SHORT).show();
            return false;
        }
        //姓が入力されみょうじが空のときNG
        if (!ValidationUtils.hasCorrespondingKana(lastName,lastNameKana)) {
            Toast.makeText(this, "みょうじ（ふりがな）を入力してください", Toast.LENGTH_SHORT).show();
            return false;
        }
        //名が入力されなまえが空のときNG
        if (!ValidationUtils.hasCorrespondingKana(firstName,firstNameKana)) {
            Toast.makeText(this, "なまえ（ふりがな）を入力してください", Toast.LENGTH_SHORT).show();
            return false;
        }
        //ふりがながひらがなで入力されているかチェック
        if (!ValidationUtils.isHiraganaOnly(lastNameKana)) {
            Toast.makeText(this, "みょうじ（ふりがな）は「ひらがな」で入力してください", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!ValidationUtils.isHiraganaOnly(firstNameKana)) {
            Toast.makeText(this, "なまえ（ふりがな）は「ひらがな」で入力してください", Toast.LENGTH_SHORT).show();
            return false;
        }
        //郵便番号の桁数チェック
        if (!ValidationUtils.isValidZipCode(zipCode)) {
            Toast.makeText(this, "郵便番号は7桁（例: 1234567）で入力してください", Toast.LENGTH_SHORT).show();
            return false;
        }
        //電話番号の桁数チェック
        if (!ValidationUtils.isValidPhone(phone)) {
            Toast.makeText(this, "電話番号は10桁または11桁で入力してください", Toast.LENGTH_SHORT).show();
            return false;
        }
        //メールアドレスの形式チェック
        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, "メールアドレスの形式が正しくありません", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Contactオブジェクトの作成
        Contact updatedContact = new Contact(
                currentUuid,
                lastName,
                firstName,
                lastNameKana,
                firstNameKana,
                etZipCode.getText().toString().trim(),
                etAddress.getText().toString().trim(),
                etBuildingRoom.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etMemo.getText().toString().trim()
        );

        // 3. DB更新
        int result = dbHelper.updateContact(updatedContact);
        if (result > 0) {
            //保存に成功したのでinitial変数を初期値として更新
            updateInitialValues();
            if (finishAfterSave) {
                // ダイアログからの「保存して戻る」用
                Toast.makeText(this, "更新して保存しました", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                // 通常の「保存する」ボタン用（閲覧モードに戻る）
                Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
                isEditMode = false;
                setFieldsEnabled(false);
                btnEdit.setText("編集する");
                return true;
            }
        } else {
            Toast.makeText(this, "更新に失敗しました", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //変更されたデータをDBに上書き保存し、閲覧モードに戻る
    private void saveUpdatedContact() {
        saveContact(false);
    }
    //保存してActivityを移動するメソッド
    //戻り値booleanにすることで入力チェックを確実に通過して、処理が成功したことを確認してからfinish()する
    private boolean saveUpdatedContactAndFinish() {
        return saveContact(true);
    }

    //編集モード時に内容に変化があったかを確認する
    private boolean hasTextChanged() {
        String currentLastName = etLastName.getText().toString();
        String currentFirstName = etFirstName.getText().toString();
        String currentLastNameKana = etLastNameKana.getText().toString();
        String currentFirstNameKana = etFirstNameKana.getText().toString();
        String currentZipCode = etZipCode.getText().toString();
        String currentAddress = etAddress.getText().toString();
        String currentBuildingRoom = etBuildingRoom.getText().toString();
        String currentPhone = etPhone.getText().toString();
        String currentEmail = etEmail.getText().toString();
        String currentMemo = etMemo.getText().toString();

        // 編集開始時の値から1つでも変化していれば true を返す
        return !currentLastName.equals(initialLastName)
                || !currentFirstName.equals(initialFirstName)
                || !currentLastNameKana.equals(initialLastNameKana)
                || !currentFirstNameKana.equals(initialFirstNameKana)
                || !currentZipCode.equals(initialZipCode)
                || !currentAddress.equals(initialAddress)
                || !currentBuildingRoom.equals(initialBuildingRoom)
                || !currentPhone.equals(initialPhone)
                || !currentEmail.equals(initialEmail)
                || !currentMemo.equals(initialMemo);
    }

    //EditTextの現在の値を初期値(initial〜)として一括更新するメソッド
    private void updateInitialValues() {
        initialLastName = etLastName.getText().toString();
        initialFirstName = etFirstName.getText().toString();
        initialLastNameKana = etLastNameKana.getText().toString();
        initialFirstNameKana = etFirstNameKana.getText().toString();
        initialZipCode = etZipCode.getText().toString();
        initialAddress = etAddress.getText().toString();
        initialBuildingRoom = etBuildingRoom.getText().toString();
        initialPhone = etPhone.getText().toString();
        initialEmail = etEmail.getText().toString();
        initialMemo = etMemo.getText().toString();
    }
}