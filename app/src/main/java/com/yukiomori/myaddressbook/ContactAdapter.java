package com.yukiomori.myaddressbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {
    public ContactAdapter(@NonNull Context context, @NonNull List<Contact> contacts){
        super(context,0,contacts);
    }

    @NonNull
    @Override
    //最初にリストを表示するタイミングやスクロールしたとき、画面に戻ってきたときに呼び出されるメソッド
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //1行分のレイアウトがまだ作られていない場合のみ生成する
        //convertViewは画面外に消えた行のViewを使いまわして中の文字だけを書き換えてメモリの無駄遣いを防いでいる。えらい
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_contact, parent, false);
        }
        //その行のContactデータを取得
        Contact contact = getItem(position);
        //レイアウト内のTextViewを取得
        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvPhone = convertView.findViewById(R.id.tv_phone);
        TextView tvAddress=convertView.findViewById(R.id.tv_address);
        // データをセット
        if (contact != null) {
            String name = contact.getLastName() + " " + contact.getFirstName();
            tvName.setText(name);
            tvPhone.setText(contact.getPhone());
            tvAddress.setText(contact.getAddress());
        }

        return convertView;
    }

}
