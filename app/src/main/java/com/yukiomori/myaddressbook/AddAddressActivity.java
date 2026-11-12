package com.yukiomori.myaddressbook;

import static com.yukiomori.myaddressbook.R.*;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.yukiomori.myaddressbook.databinding.ActivityAddAddressBinding;

public class AddAddressActivity extends AppCompatActivity {
    //ViewBindingを利用
    private ActivityAddAddressBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        //インフレート
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //ツールバーのセットアップ
        setSupportActionBar(binding.toolbar);
        //ツールバーがステータスバーと重ならないようにする処理
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        //キーボードが出現した時に入力欄をキーボードよりも上に持ち上げたように見せた時、座標がズレるのを解決するためxmlに
        // android:id="@+id/layout_container"を追記し、scroll.viewではなく中のlayout_containerに余白を設定する
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutContainer, (v, insets) -> {
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
        //文字が入力されるとボタンの色が変わる
        setupInputWatchers();
        //戻るボタンイベントの設定。入力途中だったときバック処理に飛ぶ
        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        //保存ボタンが押された時、データを保存する処理をする
        binding.btnSave.setOnClickListener(v-> saveDatabase());
        //戻るが押されたときの警告ダイアログ処理
        //onBackPressed()が非推奨なので、onCreate内でOnBackPressedDispatcherを使ってコールバックを登録
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (ValidationUtils.hasUnsavedChanges(
                        binding.editLastName, binding.editFirstName, binding.editLastNameKana, binding.editFirstNameKana,
                        binding.editZipCode, binding.editAddress, binding.editBuildingRoom, binding.editPhone, binding.editEmail, binding.editMemo)) {
                    // 未保存の入力がある場合のみダイアログを表示
                    new androidx.appcompat.app.AlertDialog.Builder(AddAddressActivity.this)
                            .setTitle(R.string.dialog_title_confirm)
                            .setMessage(R.string.dialog_msg_unsaved_add)
                            .setPositiveButton(R.string.dialog_btn_save_back, (dialog, which) -> {
                                saveDatabase(); // 保存処理
                            })
                            .setNegativeButton(R.string.dialog_btn_discard_back, (dialog, which) -> {
                                // 一時的にこのコールバックを無効化して、再度戻る操作をシステムに流す
                                setEnabled(false);
                                getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNeutralButton(R.string.dialog_btn_cancel, (dialog, which) -> {
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

    // メニューを表示する機能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_address, menu);
        return true;
    }
    // メニューが押された時の処理
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveDatabase();
            return true;
        } else if (id == R.id.action_clear) {
            clearAllFields();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //EditViewに入力されたデータをDBに保存し、結果をユーザに報告する
    private void saveDatabase() {
        String lastName = ValidationUtils.getEditTextString(binding.editLastName);
        String firstName = ValidationUtils.getEditTextString(binding.editFirstName);
        String lastNameKana = ValidationUtils.getEditTextString(binding.editLastNameKana);
        String firstNameKana = ValidationUtils.getEditTextString(binding.editFirstNameKana);
        String zipCode = ValidationUtils.getEditTextString(binding.editZipCode);
        String address = ValidationUtils.getEditTextString(binding.editAddress);
        String buildingRoom = ValidationUtils.getEditTextString(binding.editBuildingRoom);
        String phone = ValidationUtils.getEditTextString(binding.editPhone);
        String email = ValidationUtils.getEditTextString(binding.editEmail);
        String memo = ValidationUtils.getEditTextString(binding.editMemo);

        //入力チェック(バリデーション)
        String errorMessage = ValidationUtils.validateContactInput(
                this, lastName, firstName, lastNameKana, firstNameKana, zipCode, phone, email
        );

        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        //こちらで想定している最低限の情報が入力されているので、セッターで入力情報を登録
        Contact contact = Contact.createFromInput(
                lastName, firstName, lastNameKana, firstNameKana,
                zipCode, address, buildingRoom, phone, email, memo
        );
        //DBに保存する
        try (DatabaseHelper dbHelper = new DatabaseHelper(AddAddressActivity.this)) {
            long result = dbHelper.insertContact(contact);
            //結果を判定しユーザに返す
            if (result != -1) {
                Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                finish();   //MainActivityに戻る
            } else {
                Toast.makeText(this, getString(R.string.toast_save_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 入力中のデータを一括消去(リセット)するメソッド
    private void clearAllFields() {
        if (!ValidationUtils.hasUnsavedChanges(
                binding.editLastName, binding.editFirstName, binding.editLastNameKana, binding.editFirstNameKana,
                binding.editZipCode, binding.editAddress, binding.editBuildingRoom, binding.editPhone, binding.editEmail, binding.editMemo
        )) {
            Toast.makeText(this, getString(R.string.toast_no_unsaved_content), Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_confirm)
                .setMessage(R.string.dialog_msg_clear_confirm)
                .setPositiveButton(R.string.btn_clear, (dialog, which) -> {
                    binding.editLastName.setText("");
                    binding.editFirstName.setText("");
                    binding.editLastNameKana.setText("");
                    binding.editFirstNameKana.setText("");
                    binding.editZipCode.setText("");
                    binding.editAddress.setText("");
                    binding.editBuildingRoom.setText("");
                    binding.editPhone.setText("");
                    binding.editEmail.setText("");
                    binding.editMemo.setText("");
                    Toast.makeText(this, getString(R.string.toast_fields_cleared), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.dialog_btn_cancel, null)
                .show();
    }

    //入力の有無に応じて保存ボタンの色を変更するメソッド
    private void updateSaveButtonState() {
        boolean hasInput = ValidationUtils.hasUnsavedChanges(
                binding.editLastName, binding.editFirstName, binding.editLastNameKana, binding.editFirstNameKana,
                binding.editZipCode, binding.editAddress, binding.editBuildingRoom, binding.editPhone, binding.editEmail, binding.editMemo
        );

        if (hasInput) {
            // 何らかの入力がある場合:明るい緑色
            binding.btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            // 全て未入力の場合:元の紫色
            binding.btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6200EE")));
        }
    }

    //全EditTextに文字変化リスナーを一括セットするメソッド
    private void setupInputWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //文字が入力・変更されるたびにボタン色を更新
                updateSaveButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.editLastName.addTextChangedListener(watcher);
        binding.editFirstName.addTextChangedListener(watcher);
        binding.editLastNameKana.addTextChangedListener(watcher);
        binding.editFirstNameKana.addTextChangedListener(watcher);
        binding.editZipCode.addTextChangedListener(watcher);
        binding.editAddress.addTextChangedListener(watcher);
        binding.editBuildingRoom.addTextChangedListener(watcher);
        binding.editPhone.addTextChangedListener(watcher);
        binding.editEmail.addTextChangedListener(watcher);
        binding.editMemo.addTextChangedListener(watcher);
    }

}
