package com.yukiomori.myaddressbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yukiomori.myaddressbook.databinding.ListItemContactBinding;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {
    public ContactAdapter(@NonNull Context context, @NonNull List<Contact> contacts){
        super(context,0,contacts);
    }

    @NonNull
    @Override
    //最初にリストを表示するタイミングやスクロールしたとき、画面に戻ってきたときに呼び出されるメソッド
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ListItemContactBinding binding;
        //1行分のレイアウトがまだ作られていない場合のみ生成する
        //convertViewは画面外に消えた行のViewを使いまわして中の文字だけを書き換えてメモリの無駄遣いを防いでいる。えらい
        if (convertView == null) {
            //初回はインフレート
            binding = ListItemContactBinding.inflate(LayoutInflater.from(getContext()), parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            //再利用
            binding = (ListItemContactBinding) convertView.getTag();
        }
        //その行のContactデータを取得
        Contact contact = getItem(position);
        // データをセット
        if (contact != null) {
            String name = contact.getLastName() + " " + contact.getFirstName();
            binding.tvName.setText(name);
            binding.tvPhone.setText(contact.getPhone());
            binding.tvAddress.setText(contact.getAddress());
        }

        return convertView;
    }

}
