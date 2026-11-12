package com.yukiomori.myaddressbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yukiomori.myaddressbook.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //ViewBindingを取り入れてみる
    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    //保持したほうが扱いやすいのでメンバにしておく
    //検索窓が空のときにポートレートランドスケープが切り替わるとエラーを起こすので、初期化しておく
    //onTextChangedが先に動き出してnull状態のリストを読み込もうとしてヌポイが発生するため
    private List<Contact> contactList=new ArrayList<>();
    //SQL命令用の保持用変数
    private String currentSortOrder = null;
    //検索窓に入力するたびにオブジェクトを作るのが効率が悪いのでAdapterもメンバを作る
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        //ViewBindingのインフレート
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //作成したツールバーをセットアップ
        setSupportActionBar(binding.toolbar);
        //ツールバーがステータスバーと重なっていたので上部の余白を開ける処理
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //ほかの初期化の処理
        dbHelper=new DatabaseHelper(this);
        //タップされたときAddAddressActivityにジャンプ。ということをラムダ式で記述
        binding.btnAdd.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this,AddAddressActivity.class);
            startActivity(intent);
        });
        //検索バーに文字が入力されるとリアルタイムで検索するリスナーを設定
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            @Override
            //文字が変更されている最中・直後に反応する
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                //ポートレートランドスケープ時のエラー対策2
                //contactListがnullなら何もしない
                if (contactList == null) {
                    return;
                }
                //入力されたキーワードを取得(前後の空白を消して小文字化)
                //すべて小文字にすることでのちほど大文字小文字の区別なくヒットさせるようにする
                String query=s.toString().trim().toLowerCase();
                //検索窓が空なら一覧表示に戻る
                if (query.isEmpty()) {
                    loadContactList();
                    return;
                }
                //検索ワードのカタカナ変換
                String queryKatakana=hiraganaToKatakana(query);
                //絞り込み用の新しいリストを作成
                List<Contact> filteredList=new ArrayList<>();

                for (Contact contact : contactList) {
                    //姓と名、みょうじとなまえを結合したフルネームと電話番号をリストから取り出して小文字にする
                    //漢字やひらがなを小文字にしようとしてもスルーされるだけでエラーは起きない
                    String fullName=contact.getLastName().toLowerCase()+contact.getFirstName().toLowerCase();
                    String fullNameKana=contact.getLastNameKana().toLowerCase()+contact.getFirstNameKana().toLowerCase();
                    String phone=contact.getPhone().toLowerCase();

                    //部分一致の判定(queryが含まれているか)
                    //queryが空のとき""(空文字)となり、Javaではどんな文字列でも""は必ず含まれているという仕様になっている
                    //つまりすべての連絡先がヒットするので、結果的に初期状態のようにすべてのリストが表示される
                    //カタカナの名前もヒットするように調整
                    if (fullName.contains(query)||fullName.contains(queryKatakana)
                            ||fullNameKana.contains(query)||fullNameKana.contains(queryKatakana)
                            ||phone.contains(query)){
                        filteredList.add(contact);
                    }
                }
                //検索結果専用のアダプターを作ってリストビューにセット
                ContactAdapter searchAdapter = new ContactAdapter(MainActivity.this, filteredList);
                binding.listView.setAdapter(searchAdapter);
                //検索にヒットしなければ(サーチ後リストが空なら)メッセージ
                updateListVisibility(filteredList,true);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        //一覧の項目がタップされたときの処理
        binding.listView.setOnItemClickListener((parent, view, position, id)->{
            //元のリストのポジションではなく、検索後に作られたリストのポジションのContactを取得
            Contact selectedContact=(Contact)parent.getItemAtPosition(position);
            //ContactDetailActivityへのIntentを作成
            if (selectedContact != null) {
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                //選択された連絡先のUUIDをIntentに持たせる(キー名:"EXTRA_UUID")
                intent.putExtra("EXTRA_UUID", selectedContact.getUuid());
                //画面遷移をする
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContactList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Activityが画面から消える時にDB接続を確実に閉じる
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    //メニューを表示する機能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    // メニューが押された時の処理
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_asc) {
            //昇順
            currentSortOrder = "last_name_kana ASC, first_name_kana ASC";
            loadContactList();
            return true;
        } else if (id == R.id.action_sort_desc) {
            // 降順
            currentSortOrder = "last_name_kana DESC, first_name_kana DESC";
            loadContactList();
            return true;
        } else if (id == R.id.action_sort_default) {
            //デフォルト(登録順)
            currentSortOrder = null;
            loadContactList();
            return true;
        } else if (id == R.id.action_import_csv) {
            //CSVインポートを実行
            startCsvImport();
            return true;
        } else if (id == R.id.action_export_csv) {
            //CSVエクスポートを実行
            startCsvExport();
            return true;
        } else if (id == R.id.action_about) {
            //アプリ情報の表示
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadContactList(){
        //DBから全件取得
        contactList = dbHelper.getAllContacts(currentSortOrder);

        //登録データが0件ならメッセージ
        updateListVisibility(contactList,false);
        //新しいアダプターを作成してセット
        adapter = new ContactAdapter(this, contactList);
        binding.listView.setAdapter(adapter);
    }

    //ひらがなをカタカナに変換するメソッド
    private String hiraganaToKatakana(String str){
        if(str==null){return "";}
        StringBuilder sb=new StringBuilder(str);
        for(int i=0;i<sb.length();i++){
            char c=sb.charAt(i);
            //ひらがなの範囲(ぁ:0x3041_ん:0x3093)
            if(c>='\u3041'&&c<='\u3093'){
                sb.setCharAt(i,(char)(c+0x60)); //カタカナに変換
            }
        }
        return sb.toString();
    }
    //特定条件でメッセージを表示するようにするメソッド
    //検索中なら引数をtrue、そうでないならfalse
    private void updateListVisibility(List<Contact> currentList, boolean isSearching) {
        if (currentList.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.listView.setVisibility(View.GONE);
            //DBに1件も登録されていないときと、検索にヒットしなかったときのメッセージ
            if (contactList.isEmpty()) {
                binding.tvEmpty.setText(R.string.empty_no_contacts);
            } else if (isSearching) {
                binding.tvEmpty.setText(R.string.empty_no_search_results);
            }
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.listView.setVisibility(View.VISIBLE);
        }
    }
    //以下からCSVファイルのエクスポートに関わるもの
    //ファイル保存先が決まった後に実際にデータを書き込む処理を行うランチャー
    //公式推奨のIntent.ACTION_CREATE_DOCUMENTとActivityResultLauncherを使う
    private final androidx.activity.result.ActivityResultLauncher<Intent> exportCsvLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        //URIを渡す
                        writeCsvToUri(uri);
                    }
                }
            });
    //実際の書き込み処理
    private void writeCsvToUri(android.net.Uri uri) {
        //書き込み直前にDBからデータを取得してCSV化
        List<Contact> contacts = dbHelper.getAllContacts(currentSortOrder);
        if (contacts.isEmpty()) {
            Toast.makeText(this, getString(R.string.csv_no_data_to_export), Toast.LENGTH_SHORT).show();
            return;
        }
        String csvData = CsvManager.exportToCsv(contacts);
        //getContentResolver()は他のアプリにアクセスするためのインスタンスを取得する
        //インスタンスがシステム側で取得済みなのでnewは付けなくてよい
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
            writer.write(csvData);
            Toast.makeText(this, getString(R.string.csv_export_success), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.csv_export_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
    //事前チェックを行いインテントを起動
    private void startCsvExport() {
        //DBからリストを取得
        List<Contact> contacts = dbHelper.getAllContacts(currentSortOrder);
        //リストが空なら通知して終了
        if (contacts.isEmpty()) {
            Toast.makeText(this, getString(R.string.csv_no_data_to_export), Toast.LENGTH_SHORT).show();
            return;
        }

        //保存先ファイル名などを指定してIntentを起動
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "address_book_backup.csv");

        exportCsvLauncher.launch(intent);
    }
    //ここまでがCSVのエクスポートに関する処理
    //ここからCSVのインポートに関する処理
    //ファイル選択用ランチャー
    private final androidx.activity.result.ActivityResultLauncher<Intent> importCsvLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        readCsvFromUri(uri);
                    }
                }
            });
    //ファイルを読み込んでDBに保存する処理
    private void readCsvFromUri(android.net.Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            // CsvManagerでContactリストに変換
            List<Contact> importedContacts = CsvManager.importFromCsv(sb.toString());

            if (importedContacts.isEmpty()) {
                Toast.makeText(this, getString(R.string.csv_import_no_data), Toast.LENGTH_SHORT).show();
                return;
            }
            //トランザクション処理
            int successCount = dbHelper.insertContactsBulk(importedContacts);

            Toast.makeText(this, getString(R.string.csv_import_success, successCount), Toast.LENGTH_SHORT).show();
            loadContactList(); // リスト再描画

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.csv_import_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
    //実行命令
    private void startCsvImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //text/csvでは受け付けなかったので変更
        intent.setType("*/*");
        importCsvLauncher.launch(intent);
    }
    //ここまでCSVインポート処理

    //現在の登録数やバージョンを表示するダイアログ
    private void showAboutDialog() {
        int count = (contactList != null) ? contactList.size() : 0;
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_application)
                .setMessage(getString(R.string.about_application_message, count))
                .setPositiveButton("OK", null)
                .show();
    }

}
