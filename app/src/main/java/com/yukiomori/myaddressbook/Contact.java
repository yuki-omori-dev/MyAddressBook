package com.yukiomori.myaddressbook;

import java.util.UUID;

//1人分の登録データを保持するためのクラス
public class Contact {
    private String uuid,lastName,firstName,lastNameKana,firstNameKana;
    private String zipCode,address,buildingRoom,phone,email,memo;

    //引数なしのコンストラクタ(後からSetterで値を入れる)。セッターとセットで扱う
    //空文字を入れたフィールドを使うことでnullが発生しなくなる
    public Contact(){
        this.uuid=UUID.randomUUID().toString();
        this.lastName="";
        this.firstName="";
        this.lastNameKana="";
        this.firstNameKana="";
        this.zipCode="";
        this.address="";
        this.buildingRoom="";
        this.phone="";
        this.email="";
        this.memo="";
    }

    // SQLiteなどからデータを読み出す時用の全項目コンストラクタ。ゲッターとセットで扱う
    public Contact(String uuid, String lastName, String firstName, String lastNameKana, String firstNameKana,
                   String zipCode, String address, String buildingRoom, String phone,
                   String email, String memo) {
        this.uuid = uuid;
        this.lastName = lastName;
        this.firstName = firstName;
        this.lastNameKana = lastNameKana;
        this.firstNameKana = firstNameKana;
        this.zipCode = zipCode;
        this.address = address;
        this.buildingRoom = buildingRoom;
        this.phone = phone;
        this.email = email;
        this.memo = memo;
    }

    //セッター
    public void setLastName(String lastName){this.lastName=lastName;}
    public void setFirstName(String firstName){this.firstName=firstName;}
    public void setLastNameKana(String lastNameKana){this.lastNameKana=lastNameKana;}
    public void setFirstNameKana(String firstNameKana){this.firstNameKana=firstNameKana;}
    public void setZipCode(String zipCode){this.zipCode=zipCode;}
    public void setAddress(String address){this.address=address;}
    public void setBuildingRoom(String buildingRoom){this.buildingRoom=buildingRoom;}
    public void setPhone(String phone){this.phone=phone;}
    public void setEmail(String email){this.email=email;}
    public void setMemo(String memo){this.memo=memo;}

    // ゲッター
    public String getUuid() { return uuid; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getLastNameKana() { return lastNameKana; }
    public String getFirstNameKana() { return firstNameKana; }
    public String getZipCode() { return zipCode; }
    public String getAddress() { return address; }
    public String getBuildingRoom() { return buildingRoom; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getMemo() { return memo; }

    //新規作成用(UUIDを生成して値をセットしContactを返す)
    public static Contact createFromInput(
            String lastName, String firstName, String lastNameKana, String firstNameKana,
            String zipCode, String address, String buildingRoom, String phone, String email, String memo){
        Contact contact = new Contact(); //デフォルトコンストラクタでUUIDの生成
        contact.setLastName(lastName);
        contact.setFirstName(firstName);
        contact.setLastNameKana(lastNameKana);
        contact.setFirstNameKana(firstNameKana);
        contact.setZipCode(zipCode);
        contact.setAddress(address);
        contact.setBuildingRoom(buildingRoom);
        contact.setPhone(phone);
        contact.setEmail(email);
        contact.setMemo(memo);
        return contact;
    }
    //更新が行われたとき用(UUIDを指定してContactを返す)
    public static Contact createFromInputWithUuid(
            String uuid,
            String lastName, String firstName, String lastNameKana, String firstNameKana,
            String zipCode, String address, String buildingRoom, String phone, String email, String memo) {
        return new Contact(uuid, lastName, firstName, lastNameKana, firstNameKana,
                zipCode, address, buildingRoom, phone, email, memo);
    }
}
