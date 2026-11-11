package com.yukiomori.myaddressbook;

import java.util.ArrayList;
import java.util.List;

public class CsvManager {

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
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    // CSV文字列からContactのリストに変換するインポート機能
    public static List<Contact> importFromCsv(String csvContent) {
        List<Contact> contacts = new ArrayList<>();
        if (csvContent == null || csvContent.isEmpty()) {
            return contacts;
        }

        String[] lines = csvContent.split("\n");
        //0行目はヘッダーなのでi=1からスタート
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            //メモなどにカンマが含まれている可能性があるのでCSVパーサーを通すのが理想
            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (tokens.length >= 11) {
                String uuid = unescapeCsv(tokens[0]);
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
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }
        return value;
    }
}
