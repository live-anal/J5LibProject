package main.com.j5.connect.method;

/**
 * 空のResultSetAnalizer<br>
 * 特に何もせず解析の負荷を軽減する目的の物<br>
 * ResultSetインスタンスのデフォルトアナライザ
 */
public class DefaultAnalizer extends ResultSetAnalizer {

	/**
	 * ResultSet#set()メソッドにてレスポンスを一行ずつ処理する際のメソッド<br>
	 * DefaultAnalizerにおいては中身が無いただの飾りメソッド
	 */
	public void analize(String line, String type) {}

	/**
	 * ResultSet#toString()メソッドにてレスポンス内容を返す際のメソッド<br>
	 * DefaultAnalizerにおいてはnullを返却する
	 */
	public String toStringBody() {
		return null;
	}
}
