package main.com.j5.exception;

/**
 * おちんちんプルプルしちゃうよぉ
 */
public class ChinkoException extends Exception{
	public ChinkoException(Exception e, String msg) {
		super(msg + "\n" + e);
	}
}
