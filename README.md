# アプリの概要

連絡先を管理できるシンプルなAndroidの住所録アプリです。登録した情報からワンタップで外部アプリを起動できるため、日々の連絡や移動がスムーズに行えます。

<table>
  <tr>
    <th align="center">操作デモ (一覧表示～新規登録)</th>
    <th align="center">操作デモ (検索～詳細表示)</th>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/b05002e0-c599-4825-9223-479310a37b1f" width="220"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/26931832-d4dd-456d-9c62-b5cf3663c25c" width="220"></td>
      </tr>
</table>

## 主な機能

- 氏名、郵便番号、住所、建物名・部屋番号、電話番号、メールアドレス、メモ欄の登録
- 検索機能。並べ替え機能
- ワンタップでの通話アプリ立ち上げ、マップ表示、メール作成
- CSV形式でのエクスポート(住所録保存)およびインポート(住所録取り込み)

## 仕様

- トップ画面の右下ボタンを押すと新規登録画面に移行します
- 姓か名、それに対応するふりがなが入力されていれば登録できるようになっています
- トップ画面の登録住所一覧の各項目をタップすると詳細画面に移行します
- トップ画面のメニューからCSVのエクスポートやインポートを選択できます

## 開発の背景・目的

企業の研修のテーマに用いられることのある住所録の作成を通して、DBの基本のCRUDを学びUI/UXを追求した。

Androidアプリ開発における基本構造(画面遷移、ライフサイクル管理、ローカルDBによる永続化)を一貫して実装する学習をした。

レガシーアプリの改修に携わる可能性を考え、java+XMLおよびSQLiteで構築し、ViewBindingへの改修やRoomへの移行などの訓練を可能とした。

## 使用技術

* **言語**: 【Java】
* **UI**: 【XMLレイアウト】
* **データベース**: 【SQLite】
* **開発環境**: Android Studio / Gradle

## 工夫した点

* **入力バリデーションの実装**:必須入力の未入力や不正な形式での登録を防ぐため、エラーメッセージで通知する仕組みを実装した。特殊文字を管理する仕組みを作成した。
* **検索機能**:入力したワードからリアルタイムで検索する仕組みを実装した。

## データベース設計
erDiagram

    contacts {    
        string uuid PK "主キー (UUID)"        
        string last_name "姓"        
        string first_name "名"        
        string last_name_kana "みょうじ (かな)"        
        string first_name_kana "なまえ (かな)"        
        string zip_code "郵便番号"        
        string address "住所"        
        string building_room "建物名・部屋番号"        
        string phone "電話番号"        
        string email "メールアドレス"        
        string memo "メモ・備考"        
    }


## ビルド・実行手順

1.ターミナル(またはコマンドプロンプト)で本リポジトリをクローンします。
```bash
   git clone https://github.com
   ```

2.**Android Studio** を起動し、起動画面の **「Open」** (またはメニューの`File`>`Open`)から、クローンされたフォルダを選択肢します。

3.プロジェクトを開くと自動的に **Gradleの同期(Gradle Sync)** が始まり、必要なライブラリがDLされます。画面右下のプログレスバーが消えるまで待機します。

4.画面上部のツールバーにあるデバイス選択欄から **Androidエミュレータ(AVD)** または **USBデバッグを有効にしたAndroid実機** を選択します。

5.ツールバーの **Runボタン(緑色の▶️アイコン)** (ショートカット:`Shift+F10`)をクリックすると、アプリのビルドが行われ、アプリが起動します。

## アプリのダウンロード（Android実機向け）
ビルド環境がない場合でも、[Releases ページ](https://github.com/yuki-omori-dev/MyAddressBook/releases) から `.apk` ファイルをダウンロードし、Android 端末にインストールして動作をお試しいただけます。
*(※インストール時に「提供元不明のアプリ」の許可確認が表示される場合があります)*
