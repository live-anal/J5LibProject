package main.com.j5.util;

/**
 * FileIOでファイルに追記があった場合に呼び出されるリスナ
 */
public interface FileWriteListener {
	/**
	 * ファイルに追記された際に呼び出されるメソッド
	 * @param str ファイルに追記された文字列
	 */
	public void writed(String str);
}
