package com.yukiomori.myaddressbook;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CsvManager {
    //正規表現文字列だけでコンパイル
    private static final Pattern CSV_SPLIT_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

    // ContactのリストからCSV文字列を生成する
    public static String exportToCsv(List<Contact> contacts) {
        StringBuilder sb = new StringBuilder();
        //ヘッダー
        sb.append("UUID,姓,名,みょうじ,なまえ,郵便番号,住所,建物名・部屋番号,電話番号,メールアドレス,メモ\n");

        for (Contact c : contacts) {
            sb.append(escapeCsv(c.getUuid())).append(",");
            sb.append(escapeCsv(c.getLastName())).append(",");
            sb.append(escapeCsv(c.getFirstName())).append(",");
            sb.append(escapeCsv(c.getLastNameKana())).append(",");
            sb.append(escapeCsv(c.getFirstNameKana())).append(",");
            sb.append(escapeCsv(c.getZipCode())).append(",");
            sb.append(escapeCsv(c.getAddress())).append(",");
            sb.append(escapeCsv(c.getBuildingRoom())).append(",");
            sb.append(escapeCsv(c.getPhone())).append(",");
            sb.append(escapeCsv(c.getEmail())).append(",");
            sb.append(escapeCsv(c.getMemo())).append("\n");
        }
        return sb.toString();
    }
    // カンマや改行が含まれていた場合にCSVが壊れないようにエスケープする関数
    private static String escapeCsv(String value) {
        if (value == null) return "";
        //カンマ、ダブルクォーテーション、改行、が含まれる場合はエスケープし全体を囲む
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    //CSV文字列からContactのリストに変換するインポート機能
    public static List<Contact> importFromCsv(String csvContent) {
        List<Contact> contacts = new ArrayList<>();
        if (csvContent == null || csvContent.isEmpty()) {
            return contacts;
        }

        List<String> csvList = splitCsvRecords(csvContent);
        int startIndex=0;
        //1行目がヘッダーかチェック
        if(!csvList.isEmpty()){
            //先頭のレコードを取り出す
            String firstLine=csvList.get(0).trim();
            //大文字・小文字を区別せずUUIDまたは姓が含まれていればヘッダー行と判断する
            if(firstLine.toUpperCase().contains("UUID")||firstLine.contains("姓")){
                startIndex=1;   //1行目をスキップ
            }
        }

        for (int i = startIndex; i < csvList.size(); i++) {
            String line = csvList.get(i).trim();
            if (line.isEmpty()) continue;

            //メモなどにカンマが含まれている可能性があるのでCSVパーサーを通すのが理想
            String[] tokens = CSV_SPLIT_PATTERN.split(line, -1);

            if (tokens.length >= 11) {
                String uuid = unescapeCsv(tokens[0]);
                //ダブルチェックにより途中にヘッダー文字列が紛れていたらスキップしてデータを保護
                if (uuid.equalsIgnoreCase("UUID")) {continue;}
                String lastName = unescapeCsv(tokens[1]);
                String firstName = unescapeCsv(tokens[2]);
                String lastNameKana = unescapeCsv(tokens[3]);
                String firstNameKana = unescapeCsv(tokens[4]);
                String zipCode = unescapeCsv(tokens[5]);
                String address = unescapeCsv(tokens[6]);
                String buildingRoom = unescapeCsv(tokens[7]);
                String phone = unescapeCsv(tokens[8]);
                String email = unescapeCsv(tokens[9]);
                String memo = unescapeCsv(tokens[10]);

                Contact contact = new Contact(uuid, lastName, firstName, lastNameKana, firstNameKana,
                        zipCode, address, buildingRoom, phone, email, memo);
                contacts.add(contact);
            }
        }
        return contacts;
    }
    //簡易CSVパーサー
    private static String unescapeCsv(String value) {
        if (value == null) return "";
        value = value.trim();
        //ダブルクォーテーションで囲まれている場合は外す
        //文字数が単一の"だったときのためvalue.length() >= 2 && を追記
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }
        return value;
    }

    //改行が含まれていても対応するパーサー
    //ダブルクォーテーションの中にいる間は改行を行分割しない、というロジック
    private static List<String> splitCsvRecords(String csvContent) {
        List<String> records = new ArrayList<>();
        StringBuilder currentRecord = new StringBuilder();
        boolean inQuotes = false; // ダブルクォートの中にいるかのフラグ

        for (int i = 0; i < csvContent.length(); i++) {
            char c = csvContent.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes; // クォートの開閉を反転
                currentRecord.append(c);
            } else if (c == '\n' && !inQuotes) {
                // クォートの外側にある改行のみ「レコードの終わり」とみなす
                records.add(currentRecord.toString());
                currentRecord.setLength(0); // リセット
            } else if (c == '\r' && !inQuotes) {
                // Windowsの改行コード(\r\n)対策: \r は無視
            } else {
                currentRecord.append(c);
            }
        }
        if (currentRecord.length() > 0) {
            records.add(currentRecord.toString());
        }
        return records;
    }
}
