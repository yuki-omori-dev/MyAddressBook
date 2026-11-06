package com.yukiomori.myaddressbook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    //Contactに入っているデータをデータベースに保存するメソッドが必要


}
