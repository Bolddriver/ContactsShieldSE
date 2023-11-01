package com.bolddriver.contactshooker.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ContactsInfoContent implements BaseColumns {
    public static final String AUTHORITIES = "com.bolddriver.contactshooker.provider.ContactsInfoProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/contacts");

    //表的各个字段的名称
    public static final String C_NAME = "display_name";
    public static final String C_NUMBER = "data1";
}