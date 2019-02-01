package main.com.j5.exception;

/**
 * おまんまんビラビラしちゃうよぉ
 */
public class MankoException extends Exception{
	public MankoException(Exception e, String msg) {
		super(msg + "\n" + e);
	}
}
