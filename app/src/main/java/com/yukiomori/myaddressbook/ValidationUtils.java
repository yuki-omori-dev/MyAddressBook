package com.yukiomori.myaddressbook;

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
            String lastName, String firstName, String lastNameKana, String firstNameKana,
            String zipCode, String phone, String email) {

        if (!hasName(lastName, firstName)) {
            return "姓か名を入力してください";
        }
        if (!hasCorrespondingKana(lastName, lastNameKana)) {
            return "みょうじ（ふりがな）を入力してください";
        }
        if (!hasCorrespondingKana(firstName, firstNameKana)) {
            return "なまえ（ふりがな）を入力してください";
        }
        if (!isHiraganaOnly(lastNameKana)) {
            return "みょうじ（ふりがな）は「ひらがな」で入力してください";
        }
        if (!isHiraganaOnly(firstNameKana)) {
            return "なまえ（ふりがな）は「ひらがな」で入力してください";
        }
        if (!isValidZipCode(zipCode)) {
            return "郵便番号は7桁（例: 1234567）で入力してください";
        }
        if (!isValidPhone(phone)) {
            return "電話番号は10桁または11桁で入力してください";
        }
        if (!isValidEmail(email)) {
            return "メールアドレスの形式が正しくありません";
        }

        return null;
    }
}
