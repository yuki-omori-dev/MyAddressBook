package com.yukiomori.myaddressbook;

import android.content.Intent;
import android.content.res.ColorStateList;
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
                    new AlertDialog.Builder(ContactDetailActivity.this)
                            .setTitle(R.string.dialog_title_confirm)
                            .setMessage(R.string.dialog_msg_unsaved_edit)
                            .setPositiveButton(R.string.dialog_btn_save_back, (dialog, which) -> {
                                // データを上書き保存したあと、画面を閉じて前画面(MainActivity)に戻る
                                if (saveUpdatedContactAndFinish()) {
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.dialog_btn_discard_back, (dialog, which) -> {
                                // 保存せずにそのまま戻る
                                setEnabled(false);
                                getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNeutralButton(R.string.dialog_btn_cancel, (dialog, which) -> {
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
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_confirm)
                    .setMessage(R.string.dialog_msg_delete_confirm)
                    .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                        int result = dbHelper.deleteContact(currentUuid);
                        if (result > 0) {
                            Toast.makeText(this, getString(R.string.toast_delete_success), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, getString(R.string.toast_delete_failed), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .show();
            return true;
        } else if (id == R.id.action_share) {
            shareContact();
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
            setFieldsEnabled(true);
            changeButtonStyles(true);
            Toast.makeText(this, getString(R.string.toast_mode_edit), Toast.LENGTH_SHORT).show();
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
        String lastName = getEditTextString(etLastName);
        String firstName = getEditTextString(etFirstName);
        String lastNameKana = getEditTextString(etLastNameKana);
        String firstNameKana = getEditTextString(etFirstNameKana);
        String zipCode = getEditTextString(etZipCode);
        String address = getEditTextString(etAddress);
        String buildingRoom = getEditTextString(etBuildingRoom);
        String phone = getEditTextString(etPhone);
        String email = getEditTextString(etEmail);
        String memo = getEditTextString(etMemo);

        //入力チェック(バリデーション)
        String errorMessage = ValidationUtils.validateContactInput(
                this, lastName, firstName, lastNameKana, firstNameKana,
                zipCode, phone, email
        );

        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return false;
        }

        //Contactオブジェクトの作成
        Contact updatedContact = Contact.createFromInputWithUuid(
                currentUuid,
                lastName, firstName, lastNameKana, firstNameKana,
                zipCode, address, buildingRoom, phone, email, memo
        );

        //DB更新
        int result = dbHelper.updateContact(updatedContact);
        if (result > 0) {
            //保存に成功したのでinitial変数を初期値として更新
            updateInitialValues();
            if (finishAfterSave) {
                // ダイアログからの「保存して戻る」用
                Toast.makeText(this, getString(R.string.toast_update_save_success), Toast.LENGTH_SHORT).show();
                return true;
            } else {
                // 通常の「保存する」ボタン用（閲覧モードに戻る）
                Toast.makeText(this, getString(R.string.toast_update_success), Toast.LENGTH_SHORT).show();
                isEditMode = false;
                setFieldsEnabled(false);
                changeButtonStyles(false);
                return true;
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_update_failed), Toast.LENGTH_SHORT).show();
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
        // 編集開始時の値から1つでも変化していれば true を返す
        return !getEditTextString(etLastName).equals(initialLastName)
                || !getEditTextString(etFirstName).equals(initialFirstName)
                || !getEditTextString(etLastNameKana).equals(initialLastNameKana)
                || !getEditTextString(etFirstNameKana).equals(initialFirstNameKana)
                || !getEditTextString(etZipCode).equals(initialZipCode)
                || !getEditTextString(etAddress).equals(initialAddress)
                || !getEditTextString(etBuildingRoom).equals(initialBuildingRoom)
                || !getEditTextString(etPhone).equals(initialPhone)
                || !getEditTextString(etEmail).equals(initialEmail)
                || !getEditTextString(etMemo).equals(initialMemo);
    }

    //EditTextの現在の値を初期値(initial〜)として一括更新するメソッド
    private void updateInitialValues() {
        initialLastName = getEditTextString(etLastName);
        initialFirstName = getEditTextString(etFirstName);
        initialLastNameKana = getEditTextString(etLastNameKana);
        initialFirstNameKana = getEditTextString(etFirstNameKana);
        initialZipCode = getEditTextString(etZipCode);
        initialAddress = getEditTextString(etAddress);
        initialBuildingRoom = getEditTextString(etBuildingRoom);
        initialPhone = getEditTextString(etPhone);
        initialEmail = getEditTextString(etEmail);
        initialMemo = getEditTextString(etMemo);
    }
    //EditTextをトリムして値を得るヘルパーメソッド
    private String getEditTextString(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    //連絡先を他アプリに共有するメソッド
    private void shareContact() {
        String name = (getEditTextString(etLastName) + " " + getEditTextString(etFirstName)).trim();
        String phone = getEditTextString(etPhone);
        String address = (getEditTextString(etAddress) + " " + getEditTextString(etBuildingRoom)).trim();
        String email = getEditTextString(etEmail);

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.share_header));
        if (!name.isEmpty()) sb.append(getString(R.string.share_name, name));
        if (!phone.isEmpty()) sb.append(getString(R.string.share_phone, phone));
        if (!address.isEmpty()) sb.append(getString(R.string.share_address, address));
        if (!email.isEmpty()) sb.append(getString(R.string.share_email, email));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());

        // 共有選択ダイアログを表示
        startActivity(Intent.createChooser(shareIntent, getString(R.string.csv_share_title)));
    }

    //編集モード、閲覧モードに応じてボタンの色とテキストを切り替えるメソッド
    private void changeButtonStyles(boolean isEdit) {
        if (isEdit) {
            //編集モード時:保存ボタンを明るい緑、戻るボタンを赤に変更
            btnEdit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); //明るい緑
            btnBack.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935"))); //赤
            btnEdit.setText(R.string.btn_save);
        } else {
            //閲覧モード時:元の紫・グレーに戻す
            btnEdit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6200EE"))); //紫
            btnBack.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#757575"))); //グレー
            btnEdit.setText(R.string.btn_edit);
        }
    }
}
