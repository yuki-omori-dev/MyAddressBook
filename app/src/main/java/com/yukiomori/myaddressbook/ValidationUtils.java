package com.yukiomori.myaddressbook;

import android.content.Context;
import android.widget.EditText;

public class ValidationUtils {
    //ここから個別の項目についてのバリデーション
    //姓か名のどちらかが入力されているか
    public static boolean hasName(String lastName, String firstName) {
        return (lastName != null && !lastName.trim().isEmpty())
                || (firstName != null && !firstName.trim().isEmpty());
    }

    //名前が入力されている場合、対応するふりがなも入力されているか
    public static boolean hasCorrespondingKana(String name, String kana) {
        if (name != null && !name.trim().isEmpty()) {
            return kana != null && !kana.trim().isEmpty();
        }
        return true;
    }

    //ひらがなチェック（空文字は許容、入力がある場合は「ひらがな」と「長音符」のみ）
    public static boolean isHiraganaOnly(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        return text.matches("^[ぁ-んー]+$");
    }

    //郵便番号桁数チェック（ハイフンを除いて数字7桁か）
    public static boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.isEmpty()) {
            return true;
        }
        String digitsOnly = zipCode.replaceAll("-", "");
        return digitsOnly.length() == 7;
    }

    //電話番号桁数チェック（ハイフンを除いて数字10桁または11桁か）
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true;
        }
        String digitsOnly = phone.replaceAll("-", "");
        return digitsOnly.length() == 10 || digitsOnly.length() == 11;
    }

    //メールアドレス形式チェック
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    //上記までが個別の項目についてのバリデーション

    //入力値をまとめて検証し、エラーメッセージを返す。問題がなければnullを返す
    //チェックしたい項目が増えたり減ったりしたら調整する
    public static String validateContactInput(
            Context context,
            String lastName, String firstName, String lastNameKana, String firstNameKana,
            String zipCode, String phone, String email) {

        if (!hasName(lastName, firstName)) {
            return context.getString(R.string.val_err_name_required);
        }
        if (!hasCorrespondingKana(lastName, lastNameKana)) {
            return context.getString(R.string.val_err_last_name_kana_required);
        }
        if (!hasCorrespondingKana(firstName, firstNameKana)) {
            return context.getString(R.string.val_err_first_name_kana_required);
        }
        if (!isHiraganaOnly(lastNameKana)) {
            return context.getString(R.string.val_err_last_name_kana_hiragana);
        }
        if (!isHiraganaOnly(firstNameKana)) {
            return context.getString(R.string.val_err_first_name_kana_hiragana);
        }
        if (!isValidZipCode(zipCode)) {
            return context.getString(R.string.val_err_zip_code);
        }
        if (!isValidPhone(phone)) {
            return context.getString(R.string.val_err_phone);
        }
        if (!isValidEmail(email)) {
            return context.getString(R.string.val_err_email);
        }

        return null;
    }

    //可変長引数でEditTextをまとめて受け取り、入力されている項目があればtrueを返す
    public static boolean hasUnsavedChanges(EditText... editTexts){
        if (editTexts == null) return false;

        for (EditText editText : editTexts) {
            if (!getEditTextString(editText).isEmpty()) {
                return true;
            }
        }
        return false; //すべて空ならfalse
    }

    //EditTextをトリムして値を得るヘルパーメソッド
    public static String getEditTextString(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
