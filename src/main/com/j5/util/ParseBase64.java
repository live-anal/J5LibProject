package main.com.j5.util;

import java.io.File;
import java.util.Base64;

/**
 * ファイルや文字列をBase64形式に変換したりするクラス
 * 各メソッドのcharsetは未指定の場合デフォルトでUTF-8指定となる
 */
public abstract class ParseBase64 {
	public static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * 引数に与えた文字列をBase64形式にエンコードする
	 * @param str 指定文字列
	 * @return Base64形式にエンコードした文字列
	 * @throws Exception
	 */
	public static String toBase64(String str)throws Exception{
		return toBase64(str, DEFAULT_CHARSET);
	}

	/**
	 * 引数に与えた文字列をBase64形式にエンコードする
	 * @param str 指定文字列
	 * @param charset 指定文字列の文字コード
	 * 例えばShift-JISで受け取ったStringインスタンスを引数に指定した場合は第二引数に"Shift-JIS"とする。
	 *
	 * @return Base64形式にエンコードした文字列
	 * @throws Exception
	 */
	public static String toBase64(String str, String charset)throws Exception{
		return Base64.getEncoder().encodeToString(ParseByteArray.fromString(str, charset));
	}

	/**
	 * 引数に与えたFileインスタンスをBase64形式にエンコードする
	 * @param file 指定ファイル
	 * @return Base64形式にエンコードした文字列
	 * @throws Exception
	 */
	public static String toBase64(File file)throws Exception{
		return Base64.getEncoder().encodeToString(ParseByteArray.fromFile(file));
	}

	/**
	 * 引数に与えたBase64形式をbyte[]型にデコードする
	 * @param str Base64文字列
	 * @return デコードしたbyte[]値
	 * @throws Exception
	 */
	public static byte[] fromBase64(String str)throws Exception{
		return fromBase64(str, DEFAULT_CHARSET);
	}

	/**
	 * 引数に与えたBase64形式をbyte[]型にデコードする
	 * @param str Base64文字列
	 * @param charset Base64文字列の文字コード
 	 * 例えば第一引数がShift-JISで受け取った文字列であれば第二引数に"Shift-JIS"とする。
 	 *
	 * @return デコードしたbyte[]値
	 * @throws Exception
	 */
	public static byte[] fromBase64(String str, String charset)throws Exception{
		return Base64.getDecoder().decode(ParseByteArray.fromString(str, charset));
	}
}