package main.com.j5.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Shift-JISをUTF8に変換したりUTF8をShift-JISに変換するクラス。
 */
public abstract class StringEncoder {

	/**
	 * 受け取ったShift-JIS文字列をUTF-8文字列に変換するメソッド
	 *
	 * @param value 受け取ったShift-JIS文字列
	 * @return UTF-8に変換された文字列
	 * @throws UnsupportedEncodingException
	 */
    public static String sjisToUtf8(String value) throws UnsupportedEncodingException {
        byte[] srcStream = value.getBytes("Shift-JIS");
        byte[] destStream = (new String(srcStream, "Shift-JIS")).getBytes("UTF-8");
        value = new String(destStream, "UTF-8");
        value = StringEncoder.convert(value, "Shift-JIS", "UTF-8");
        return value;
    }

    /**
	 * 受け取ったUTF-8文字列をShift-JIS文字列に変換するメソッド
	 *
	 * @param value 受け取ったUTF-8文字列
	 * @return Shift-JISに変換された文字列
	 * @throws UnsupportedEncodingException
	 */
    public static String utf8ToSjis(String value) throws UnsupportedEncodingException {
        byte[] srcStream = value.getBytes("UTF-8");
        value = convert(new String(srcStream, "UTF-8"), "UTF-8", "Shift-JIS");
        byte[] destStream = value.getBytes("Shift-JIS");
        value = new String(destStream, "Shift-JIS");
        return value;
    }

    /**
     * 変換メソッド
     *
     * @param value 変換元文字列
     * @param src	 変換元エンコーディング
     * @param dest	 変換後エンコーディング
     * @return 変換後文字列
     * @throws UnsupportedEncodingException
     */
    private static String convert(String value, String src, String dest) throws UnsupportedEncodingException {
        Map<String, String> conversion = createConversionMap(src, dest);
        char oldChar;
        char newChar;
        String key;
        for (Iterator<String> itr = conversion.keySet().iterator() ; itr.hasNext() ;) {
            key = itr.next();
            oldChar = toChar(key);
            newChar = toChar(conversion.get(key));
            value = value.replace(oldChar, newChar);
        }
        return value;
    }

    /**
     * 変換マップ生成メソッド
     *
     * @param src
     * @param dest
     * @return
     * @throws UnsupportedEncodingException
     */
    private static Map<String, String> createConversionMap(String src, String dest) throws UnsupportedEncodingException {
        Map<String, String> conversion = new HashMap<String, String>();
        if ((src.equals("UTF-8")) && (dest.equals("Shift-JIS"))) {
            conversion.put("U+FF0D", "U+2212");
            conversion.put("U+FF5E", "U+301C");
            conversion.put("U+FFE0", "U+00A2");
            conversion.put("U+FFE1", "U+00A3");
            conversion.put("U+FFE2", "U+00AC");
            conversion.put("U+2015", "U+2014");
            conversion.put("U+2225", "U+2016");
        } else if ((src.equals("Shift-JIS")) && (dest.equals("UTF-8"))) {
            conversion.put("U+2212", "U+FF0D");
            conversion.put("U+301C", "U+FF5E");
            conversion.put("U+00A2", "U+FFE0");
            conversion.put("U+00A3", "U+FFE1");
            conversion.put("U+00AC", "U+FFE2");
            conversion.put("U+2014", "U+2015");
            conversion.put("U+2016", "U+2225");
        } else {
            throw new UnsupportedEncodingException("Unsupported charset = " + src + ",dest=" + dest);
        }
        return conversion;
    }

    private static char toChar(String value) {
        return (char)Integer.parseInt(value.trim().substring("U+".length()), 16);
    }

    /**
     * Shift-JIS文字列をURLエンコーディングするメソッド
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
	public static String toSjis(String str) throws UnsupportedEncodingException{
		return URLEncoder.encode(str,"Shift-JIS");
	}

	/**
	 * UTF-8文字列をURLエンコーディングするメソッド
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String toUTF8(String str) throws UnsupportedEncodingException{
		return URLEncoder.encode(str,"UTF-8");
	}

	/**
	 * 指定文字コードの文字列をURLエンコーディングするメソッド
	 * @param str
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String convert(String str, String charset) throws UnsupportedEncodingException{
		return URLEncoder.encode(str,charset);
	}
}