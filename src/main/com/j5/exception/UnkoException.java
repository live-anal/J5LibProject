package main.com.j5.exception;

/**
 * おうんうんブリブリしちゃうよぉ
 */
public class UnkoException extends Exception{
	public UnkoException(Exception e, String msg) {
		super(msg + "\n" + e);
	}
}
