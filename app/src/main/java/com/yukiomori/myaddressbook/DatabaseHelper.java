package com.yukiomori.myaddressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    //データベースのファイルの名前。スマホ内部にこの名前で保存される
    private static final String DATABASE_NAME="address_book.db";
    //データベースのバージョン。設定しないといけないとのこと
    private static final int DATABASE_VERSION=1;
    //テーブル名
    private static final String TABLE_NAME="contacts";
    //カラム名。列の名前
    private static final String COLUMN_UUID="uuid";
    private static final String COLUMN_LAST_NAME="last_name";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME_KANA = "last_name_kana";
    private static final String COLUMN_FIRST_NAME_KANA = "first_name_kana";
    private static final String COLUMN_ZIP_CODE = "zip_code";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_BUILDING_ROOM = "building_room";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_MEMO = "memo";
    //コンストラクタ
    public DatabaseHelper(Context context) {
        //親クラスにアプリ情報、データベース名、バージョンを渡す
        //引数factoryがnullであるとき、デフォルトのCursor生成処理を使う指示になるとのこと
        //99%お約束のようだ。Cursorはデータを1行ずつ読み出すための矢印のような役割
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //テーブルを新規作成するためのSQL。onCreateの外側で定数のように定義してもよい
        //文字列連結するための""で囲うとき、スペースを入れ忘れないように。エラーになる
        String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_UUID + " TEXT PRIMARY KEY, "
                + COLUMN_LAST_NAME + " TEXT, "
                + COLUMN_FIRST_NAME + " TEXT, "
                + COLUMN_LAST_NAME_KANA + " TEXT, "
                + COLUMN_FIRST_NAME_KANA + " TEXT, "
                + COLUMN_ZIP_CODE + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_BUILDING_ROOM + " TEXT, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_MEMO + " TEXT" + ")";
        //テーブルを作成する
        db.execSQL(CREATE_TABLE_SQL);
    }

    //データベースのバージョンが上がった時に自動で動く処理
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        //既存の古いテーブルを削除する
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        //onCreateを呼び出して作り直す
        onCreate(db);
    }

    //新しい連絡先をデータベースに保存するメソッド。contactに保存したデータをデータベースへ移すイメージ
    public long insertContact(Contact contact){
        //ContentValuesクラスという、データを挿入する時に列名(キー)と値(バリュー)をペアで格納するクラスを使う
        //複雑なSQLを組み立てることなく安全にデータを登録できる
        ContentValues contentValues=new ContentValues();
        //put()メソッドでキーとバリューを紐づけている
        contentValues.put(COLUMN_UUID, contact.getUuid());
        contentValues.put(COLUMN_LAST_NAME, contact.getLastName());
        contentValues.put(COLUMN_FIRST_NAME, contact.getFirstName());
        contentValues.put(COLUMN_LAST_NAME_KANA, contact.getLastNameKana());
        contentValues.put(COLUMN_FIRST_NAME_KANA, contact.getFirstNameKana());
        contentValues.put(COLUMN_ZIP_CODE, contact.getZipCode());
        contentValues.put(COLUMN_ADDRESS, contact.getAddress());
        contentValues.put(COLUMN_BUILDING_ROOM, contact.getBuildingRoom());
        contentValues.put(COLUMN_PHONE, contact.getPhone());
        contentValues.put(COLUMN_EMAIL, contact.getEmail());
        contentValues.put(COLUMN_MEMO, contact.getMemo());
        //書き込みが可能なデータベースを取得
        SQLiteDatabase db=this.getWritableDatabase();
        //データベースに挿入
        long result=db.insert(TABLE_NAME,null,contentValues);
        //データベースを閉じる
        db.close();
        return result;
    }

    //データベースから全データを取り出してListにする
    public List<Contact> getAllContacts(){
        List<Contact> contactList=new ArrayList<>();
        //読み出し専用のデータベースを取得
        SQLiteDatabase db = this.getReadableDatabase();
        //SELECT * FROM contactsのSQL文
        //CursorはSQLiteにおいては表のどの行をいま見ているかを指し示す役割
        //取得時は最初の行の一つ手前を指している状態なので注意
        Cursor cursor=db.rawQuery("SELECT * FROM "+ TABLE_NAME,null);
        // 結果を1行ずつ取り出す
        if (cursor.moveToFirst()) {
            do {
                // 各カラムの値を取得（文字列がnullなら空文字にする)
                //cursor.getColumnIndexOrThrow(カラム名)により、そのカラムが左から何番目にあるか(列番号)を調べる
                String uuid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UUID));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));
                String lastNameKana = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME_KANA));
                String firstNameKana = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME_KANA));
                String zipCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ZIP_CODE));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
                String buildingRoom = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUILDING_ROOM));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                String memo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO));

                // 全項目コンストラクタを使って Contact オブジェクトを作成
                Contact contact = new Contact(uuid, lastName, firstName, lastNameKana, firstNameKana,
                        zipCode, address, buildingRoom, phone, email, memo);

                // リストに追加
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // カーソルとDBを閉じる
        cursor.close();
        db.close();

        return contactList;
    }

    //指定したUUIDのContactを返す
    public Contact getContactByUuid(String uuid){
        Contact contact=null;
        SQLiteDatabase db=this.getReadableDatabase();
        //プレースホルダーを使って検索
        //ユーザーの入力操作などがなくとも、全てプレースホルダーを使うことをルールにするとエラーに強くなる
        Cursor cursor=db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_UUID+" = ?",new String[]{uuid});

        if (cursor.moveToFirst()) {
            //探した1件からデータを拾って
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));
            String lastNameKana = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME_KANA));
            String firstNameKana = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME_KANA));
            String zipCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ZIP_CODE));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
            String buildingRoom = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUILDING_ROOM));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String memo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO));
            //全項目コンストラクタを使ってContactオブジェクトを作成
            contact = new Contact(uuid, lastName, firstName, lastNameKana, firstNameKana,
                    zipCode, address, buildingRoom, phone, email, memo);
        }
        //かならずカーソルやDBは使ったら閉じる
        cursor.close();
        db.close();
        return contact;
    }

    //編集後の情報をDBに上書き保存する
    public int updateContact(Contact contact){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LAST_NAME, contact.getLastName());
        contentValues.put(COLUMN_FIRST_NAME, contact.getFirstName());
        contentValues.put(COLUMN_LAST_NAME_KANA, contact.getLastNameKana());
        contentValues.put(COLUMN_FIRST_NAME_KANA, contact.getFirstNameKana());
        contentValues.put(COLUMN_ZIP_CODE, contact.getZipCode());
        contentValues.put(COLUMN_ADDRESS, contact.getAddress());
        contentValues.put(COLUMN_BUILDING_ROOM, contact.getBuildingRoom());
        contentValues.put(COLUMN_PHONE, contact.getPhone());
        contentValues.put(COLUMN_EMAIL, contact.getEmail());
        contentValues.put(COLUMN_MEMO, contact.getMemo());

        // WHERE uuid = ? の条件に一致するレコードをupdateで更新する
        int rowsAffected = db.update(
                TABLE_NAME,
                contentValues,
                COLUMN_UUID + " = ?",
                new String[]{contact.getUuid()}
        );

        db.close();
        // 更新された行数(正常なら1)を返す
        return rowsAffected;

    }
}
