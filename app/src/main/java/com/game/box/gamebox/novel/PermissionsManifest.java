package com.game.box.gamebox.novel;

import android.Manifest;

public class PermissionsManifest {
  public static  String[] PERMISSIONS = new String[]{
          Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.READ_SMS
    };
  public static  String[] PERMISSIONS_READ =  new String[]{
          Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.ACCESS_FINE_LOCATION
  };
  public static  String[] PERMISSIONS_NULL =  new String[]{};

  public static  String[] PERMISSIONS_READ_PHONE_STATE =  new String[]{
          Manifest.permission.READ_PHONE_STATE
  };

  public static  String[] PERMISSIONS_WRITE =  new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  public static  String[] PERMISSIONS_ACCESS =  new String[]{
          Manifest.permission.ACCESS_FINE_LOCATION
  };
}
