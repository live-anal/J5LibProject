package main.com.j5.connect.method;

import java.util.ArrayList;
import java.util.List;

import main.com.j5.connect.Content;

/**
 * ResultSetAnalizer<br>
 * ResultSetインスタンスの拡張用として用意した抽象クラス<br>
 * 主にこのクラスを継承した各アナライザをResultSet#setAnal()に与える事でレスポンス解析処理に変化を生む
 */
public abstract class ResultSetAnalizer {
	/**
	 * Contentインスタンスリスト<br>
	 * 各行を解析した情報はここに保持する
	 */
	protected List<Content> contents;

	/**
	 * スレタイ<br>
	 * dat解析において取得したスレタイを保持する
	 */
	protected 	String subject;

	/**
	 * コンストラクタ<br>
	 * contents = ArrayList&lt;&gt;();<br>
	 * subject  = null;
	 */
	public ResultSetAnalizer() {
		contents = new ArrayList<>();
		subject = null;
	}

	/**
	 * contentsゲッター<br>
	 *
	 * @return List&lt;Content&gt; content
	 */
	public List<Content> getContent(){
		return contents;
	}

	/**
	 * subjectゲッター<br>
	 *
	 * @return String subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * 解析抽象メソッド<br>
	 * 取得した各行は一行ずつこのメソッドに渡されるので、好きなように処理を記述すること<br>
	 * typeで渡される文字列は以下の通り<br>
	 * (1) static J5ch#TXT_SUBJECT = "subject.txt";<br>
	 * (2) static J5ch#TXT_SETTING = "SETTING.TXT";<br>
	 * (3) static J5ch#TXT_1001    = "1000.txt";<br>
	 * (4) static J5ch#TXT_HEAD    = "head.txt";<br>
	 * (5) static J5ch#HTML        = "HTML";<br>
	 * (6) static J5ch#DAT		   = "DAT";<br>
	 * (7) static J5ch#POST		   = "POST";<br>
	 * <br>
	 * @param line	処理行
	 * @param type	レスポンス取得タイプ
	 */
	public abstract void analize(String line, String type);

	/**
	 * ResultSet#toString()が呼ばれた際の処理<br>
	 * contentを整形するも良し、タイトルだけ返すも良し<br>
	 * 何も返す気が無い場合はnullをreturnすること<br>
	 *
	 * @return 文字列
	 */
	public abstract String toStringBody();

}
