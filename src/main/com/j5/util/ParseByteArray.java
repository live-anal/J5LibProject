package main.com.j5.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * 色んなオブジェクトをByte配列に変換したりするクラス。
 * 各メソッドのcharsetは未指定の場合デフォルトでUTF-8指定となる
 */
public abstract class ParseByteArray {
	public static final String DEFAULT_CHARSET = "UTF-8";

	public static byte[] fromChar(char value){
		int arraySize = Character.SIZE / Byte.SIZE;
		ByteBuffer buffer = ByteBuffer.allocate(arraySize);
		return buffer.putChar(value).array();
	}

	public static byte[] fromShort(short value){
		int arraySize = Short.SIZE / Byte.SIZE;
		ByteBuffer buffer = ByteBuffer.allocate(arraySize);
		return buffer.putShort(value).array();
	}

	public static byte[] fromInt(int value){
		int arraySize = Integer.SIZE / Byte.SIZE;
		ByteBuffer buffer = ByteBuffer.allocate(arraySize);
		return buffer.putInt(value).array();
	}

	public static byte[] fromLong(long value){
		int arraySize = Long.SIZE / Byte.SIZE;
		ByteBuffer buffer = ByteBuffer.allocate(arraySize);
		return buffer.putLong(value).array();
	}

	public static byte[] fromFloat(float value){
		int arraySize = Float.SIZE / Byte.SIZE;
		ByteBuffer buffer = ByteBuffer.allocate(arraySize);
		return buffer.putFloat(value).array();
	}

	public static byte[] fromDouble(double value){
		int arraySize = Double.SIZE / Byte.SIZE;
		ByteBuffer buffer = ByteBuffer.allocate(arraySize);
		return buffer.putDouble(value).array();
	}

	public static byte[] fromString(String value)throws Exception{
		return fromString(value, DEFAULT_CHARSET);
	}

	public static byte[] fromString(String value, String charset)throws Exception{
		return value.getBytes(charset);
	}

	public static byte[] fromBoolean(boolean flag){
		int value = 0;

		if(flag == true) value = 1;

		return fromInt(value);
	}

	public static char toChar(byte[] buf){
		return ByteBuffer.wrap(buf).getChar();
	}

	public static short toShort(byte[] buf){
		return ByteBuffer.wrap(buf).getShort();
	}

	public static int toInt(byte[] buf){
		return ByteBuffer.wrap(buf).getInt();
	}

	public static long toLong(byte[] buf){
		return ByteBuffer.wrap(buf).getLong();
	}

	public static float toFloat(byte[] buf){
		return ByteBuffer.wrap(buf).getFloat();
	}

	public static double toDouble(byte[] buf){
		return ByteBuffer.wrap(buf).getDouble();
	}

	public static String toString(byte[] buf)throws Exception{
		return toString(buf, DEFAULT_CHARSET);
	}

	public static String toString(byte[] buf, String charset)throws Exception{
		return new String(buf, charset);
	}

	public static boolean toBoolean(byte[] buf){
		boolean flag = false;

		if(toInt(buf) == 1)flag = true;

		return flag;
	}

	public static File toFile(byte[] buf, String filename) throws Exception{
		return toFile(buf, "./", filename);
	}

	public static File toFile(byte[] buf, String filepath, String filename) throws Exception{
		return toFile(buf, new File(filepath + filename));
	}

	public static File toFile(byte[] buf, File file) throws Exception{
		FileOutputStream output = new FileOutputStream(file);
		output.write(buf);

		output.flush();
		output.close();

		return file;
	}

	public static byte[] fromFile(String filename) throws Exception {
		return fromFile("./",filename);
	}

	public static byte[] fromFile(String filepath, String filename) throws Exception {
		return fromFile( new File(filepath+filename) );
	}

	public static byte[] fromFile(File file) throws Exception{
		byte[] b = new byte[1];

	    FileInputStream input = new FileInputStream(file);
	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    while (input.read(b) > 0) {
	        output.write(b);
	    }

	    output.flush();
	    input.close();
	    output.close();

	    return output.toByteArray();
	}
}