package com.yukiomori.myaddressbook;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import com.yukiomori.myaddressbook.databinding.ActivityContactDetailBinding;

public class ContactDetailActivity extends AppCompatActivity {
    private ActivityContactDetailBinding binding;
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
        //ViewBindingをインフレート
        binding = ActivityContactDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //作成したツールバーをセットアップ
        setSupportActionBar(binding.toolbar);
        //ツールバーがステータスバーと重なっていたので上部の余白を開ける処理
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        //キーボードが出現した時に、選択した項目にキーボードが被さっていたらスクロールする
        //ナビゲーションバーまたはキーボードの大きい方の余白をセット
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
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
                            //機種ごとのdensity(倍率)を求め、48pxを基準としたマージンとする
                            float density=getResources().getDisplayMetrics().density;
                            int marginPx=(int)(48*density);

                            //端末ごとに余白分を計算し余裕を持たせる
                            int overlap = viewBottomOnScreen - keyboardTopOnScreen + marginPx;
                            binding.scrollView.smoothScrollBy(0, overlap);
                        }
                    }
                }, 250);
            }
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
        binding.btnBack.setOnClickListener(v->getOnBackPressedDispatcher().onBackPressed());
        //編集ボタンを押したら保存モードへ移行し、ボタン名を保存ボタンに
        binding.btnEdit.setOnClickListener(v->{
            toggleEditMode();
        });
        //電話の部分をタップしたら電話を起動
        binding.editPhone.setOnClickListener(v -> {
            //閲覧モードのときのみ実行
            if (!isEditMode) {
                String phone = binding.editPhone.getText().toString().trim();
                if (!phone.isEmpty()) {
                    //ダイヤル画面を開いて番号を入力状態にするところまでのアクション
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
            }
        });
        //メールの部分をタップしたらメールを起動
        binding.editEmail.setOnClickListener(v -> {
            //閲覧モードのときのみ実行
            if (!isEditMode) {
                String email = binding.editEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    startActivity(intent);
                }
            }
        });
        //住所をタップしたらマップを起動
        binding.editAddress.setOnClickListener(v -> {
            //閲覧モードのときのみ実行
            if (!isEditMode) {
                String address = binding.editAddress.getText().toString().trim()+binding.editBuildingRoom.getText().toString().trim();
                if (!address.isEmpty()) {
                    // 住所文字列をエンコードしてURIを作成
                    Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                    Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Activityが画面から消える時にDB接続を確実に閉じる
        if (dbHelper != null) {
            dbHelper.close();
        }
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
        binding.editLastName.setText(contact.getLastName());
        binding.editFirstName.setText(contact.getFirstName());
        binding.editLastNameKana.setText(contact.getLastNameKana());
        binding.editFirstNameKana.setText(contact.getFirstNameKana());
        binding.editZipCode.setText(contact.getZipCode());
        binding.editAddress.setText(contact.getAddress());
        binding.editBuildingRoom.setText(contact.getBuildingRoom());
        binding.editPhone.setText(contact.getPhone());
        binding.editEmail.setText(contact.getEmail());
        binding.editMemo.setText(contact.getMemo());
    }
    // 各EditTextの入力の可/不可(enabled)を一括切り替えするメソッド
    private void setFieldsEnabled(boolean enabled) {
        setEditTextEditable(binding.editLastName, enabled);
        setEditTextEditable(binding.editFirstName, enabled);
        setEditTextEditable(binding.editLastNameKana, enabled);
        setEditTextEditable(binding.editFirstNameKana, enabled);
        setEditTextEditable(binding.editZipCode, enabled);
        setEditTextEditable(binding.editAddress, enabled);
        setEditTextEditable(binding.editBuildingRoom, enabled);
        setEditTextEditable(binding.editPhone, enabled);
        setEditTextEditable(binding.editEmail, enabled);
        setEditTextEditable(binding.editMemo, enabled);
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
            if (editText == binding.editPhone || editText == binding.editAddress || editText == binding.editEmail) {
                editText.setTextColor(Color.BLUE);
            } else {
                editText.setTextColor(Color.BLACK);
            }
        }
    }

    //更新を保存してMainに戻るモードと更新を保存して閲覧モードに戻るパターンの2つを条件分岐でまとめたもの
    private boolean saveContact(boolean finishAfterSave) {
        String lastName = getEditTextString(binding.editLastName);
        String firstName = getEditTextString(binding.editFirstName);
        String lastNameKana = getEditTextString(binding.editLastNameKana);
        String firstNameKana = getEditTextString(binding.editFirstNameKana);
        String zipCode = getEditTextString(binding.editZipCode);
        String address = getEditTextString(binding.editAddress);
        String buildingRoom = getEditTextString(binding.editBuildingRoom);
        String phone = getEditTextString(binding.editPhone);
        String email = getEditTextString(binding.editEmail);
        String memo = getEditTextString(binding.editMemo);

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
        return !getEditTextString(binding.editLastName).equals(initialLastName)
                || !getEditTextString(binding.editFirstName).equals(initialFirstName)
                || !getEditTextString(binding.editLastNameKana).equals(initialLastNameKana)
                || !getEditTextString(binding.editFirstNameKana).equals(initialFirstNameKana)
                || !getEditTextString(binding.editZipCode).equals(initialZipCode)
                || !getEditTextString(binding.editAddress).equals(initialAddress)
                || !getEditTextString(binding.editBuildingRoom).equals(initialBuildingRoom)
                || !getEditTextString(binding.editPhone).equals(initialPhone)
                || !getEditTextString(binding.editEmail).equals(initialEmail)
                || !getEditTextString(binding.editMemo).equals(initialMemo);
    }

    //EditTextの現在の値を初期値(initial〜)として一括更新するメソッド
    private void updateInitialValues() {
        initialLastName = getEditTextString(binding.editLastName);
        initialFirstName = getEditTextString(binding.editFirstName);
        initialLastNameKana = getEditTextString(binding.editLastNameKana);
        initialFirstNameKana = getEditTextString(binding.editFirstNameKana);
        initialZipCode = getEditTextString(binding.editZipCode);
        initialAddress = getEditTextString(binding.editAddress);
        initialBuildingRoom = getEditTextString(binding.editBuildingRoom);
        initialPhone = getEditTextString(binding.editPhone);
        initialEmail = getEditTextString(binding.editEmail);
        initialMemo = getEditTextString(binding.editMemo);
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
        String name = (getEditTextString(binding.editLastName) + " " + getEditTextString(binding.editFirstName)).trim();
        String phone = getEditTextString(binding.editPhone);
        String address = (getEditTextString(binding.editAddress) + " " + getEditTextString(binding.editBuildingRoom)).trim();
        String email = getEditTextString(binding.editEmail);

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
            binding.btnEdit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); //明るい緑
            binding.btnBack.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935"))); //赤
            binding.btnEdit.setText(R.string.btn_save);
        } else {
            //閲覧モード時:元の紫・グレーに戻す
            binding.btnEdit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6200EE"))); //紫
            binding.btnBack.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#757575"))); //グレー
            binding.btnEdit.setText(R.string.btn_edit);
        }
    }
}
