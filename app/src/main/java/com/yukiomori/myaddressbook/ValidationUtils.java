package com.yukiomori.myaddressbook;

public class ValidationUtils {

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
}
