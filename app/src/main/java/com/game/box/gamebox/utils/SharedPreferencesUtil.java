
package com.game.box.gamebox.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


import com.game.box.gamebox.App;

import java.util.HashMap;

public final class SharedPreferencesUtil {

	//设备标识
	public static final String DEVICE_ID = "device_id";
	private static final String sharedPreferencesInfo = "klapp.shareInfo";
	private static Context myContext;
	private static SharedPreferences mPreferences;
	private static Editor mEditor;
	private static SharedPreferencesUtil mSharedInstance = new SharedPreferencesUtil();

	private SharedPreferencesUtil() {
	}

	/**
	 * 单例模式获得对象实例
	 * 
	 * @param context
	 * @return
	 */
	public static SharedPreferencesUtil getInstance(Context context) {
		myContext = context.getApplicationContext();
		if (mPreferences == null && myContext != null) {
			mPreferences = myContext.getSharedPreferences(
					sharedPreferencesInfo, Context.MODE_PRIVATE);
			mEditor = mPreferences.edit();
		}
		return mSharedInstance;
	}
	public static SharedPreferencesUtil getInstance() {
		return getInstance(App.getInstance());
	}

	/**
	 * 是否有键
	 * 
	 * @param key
	 * @return
	 */
	public boolean isContainKey(String key) {
		return mPreferences.contains(key);
	}

	private boolean checkEditorNull() {
		boolean result = false;
		if (mEditor == null) {
			result = true;
		}
		return result;
	}

	/**
	 * 删除指定键的值item
	 * 
	 * @param key
	 * @return
	 */
	public boolean clearItem(String key) {
		if (checkEditorNull()) {
			return false;
		}
		mEditor.remove(key);
		return mEditor.commit();
	}

	/**
	 * 获得所有保存对象
	 * 
	 * @return
	 */
	public HashMap<String, ?> getAll() {

		return (HashMap<String, ?>) mPreferences.getAll();
	}

	/**
	 * 给指定键设置String值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setString(String key, String value) {
		if (checkEditorNull()) {
			return false;
		}
		if (mPreferences.contains(key)) {
			mEditor.remove(key);
		}
		mEditor.putString(key, value);
		return mEditor.commit();
	}

	/**
	 * 获得指定键的String类型值
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public String getString(String key) {
		return mPreferences.getString(key, "");
	}

	/**
	 * 获得指定键的String类型值，带有默认值的
	 * 
	 * @param key
	 *            键
	 * @param defValue
	 *            默认值
	 * @return
	 */
	public String getString(String key, String defValue) {
		return mPreferences.getString(key, defValue);
	}

	/**
	 * 给指定键设置int值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setInt(String key, int value) {
		if (checkEditorNull()) {
			return false;
		}
		if (mPreferences.contains(key)) {
			mEditor.remove(key);
		}
		mEditor.putInt(key, value);
		return mEditor.commit();
	}

	/**
	 * 获得int类型数据
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		return mPreferences.getInt(key, 0);
	}

	/**
	 * 获得int类型数据，带有默认值的
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public int getInt(String key, int defValue) {
		return mPreferences.getInt(key, defValue);
	}

	/**
	 * 设置float类型数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setFloat(String key, float value) {
		if (checkEditorNull()) {
			return false;
		}
		if (mPreferences.contains(key)) {
			mEditor.remove(key);
		}
		mEditor.putFloat(key, value);
		return mEditor.commit();
	}

	/**
	 * 获得float类型数据
	 * 
	 * @param key
	 * @return
	 */
	public float getFloat(String key) {
		return mPreferences.getFloat(key, 0);
	}

	/**
	 * 获得float类型数据，带有默认值的
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public float getFloat(String key, float defValue) {
		return mPreferences.getFloat(key, defValue);
	}

	/**
	 * 设置long类型数据，带有默认值的
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setBoolean(String key, boolean value) {
		if (checkEditorNull()) {
			return false;
		}
		if (mPreferences.contains(key)) {
			mEditor.remove(key);
		}
		mEditor.putBoolean(key, value);
		return mEditor.commit();
	}

	/**
	 * 获得boolean类型数据
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public boolean getBoolean(String key, boolean defValue) {
		return mPreferences.getBoolean(key, defValue);
	}

	/**
	 * 设置long类型数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setLong(String key, long value) {
		if (checkEditorNull()) {
			return false;
		}
		if (mPreferences.contains(key)) {
			mEditor.remove(key);
		}
		mEditor.putLong(key, value);
		return mEditor.commit();
	}

	/**
	 * 获得long类型数据
	 * 
	 * @param key
	 * @return
	 */
	public long getLong(String key) {
		return mPreferences.getLong(key, 0);
	}

	/**
	 * 获得long类型数据，带有默认值的
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public long getLong(String key, long defValue) {
		return mPreferences.getLong(key, defValue);
	}

}