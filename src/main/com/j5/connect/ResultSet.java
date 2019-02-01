package main.com.j5.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import main.com.j5.connect.method.DefaultAnalizer;
import main.com.j5.connect.method.ResultSetAnalizer;
import main.com.j5.exception.UnkoException;

/**
 *
 */
public class ResultSet extends ArrayList<String>{
	public int code;		 	// HTTPレスポンスコード
	public int bytes;		 	// 受信バイト数
	public String message; 	// HTTPメッセージ
	public String lastmodify;	// 最終取得時間
	public String cookie;		// クッキー

	private ResultSetAnalizer anal;
	private String type;

	/**
	 * デフォルトコンストラクタ
	 * 空のインスタンス生成
	 *
	 * code = bytes = 0
	 * message = lastmodify = cookie = null
	 */
	public ResultSet() {
		super();
		type = null;
		code = 0;
		bytes = 0;
		message = null;
		lastmodify = null;
		cookie = null;
		setAnal(new DefaultAnalizer());
	}

	/**
	 * タイプ設定メソッド<br>
	 * J5chの各get,postメソッド内で何を取得したかを判断する為の設定メソッド<br>
	 * {@link ResultSetAnalizer#analize(String, String)}
	 *
	 * @return type タイプ
	 */
	public String getType() {
		return type;
	}

	/**
	 * タイプ設定メソッド<br>
	 * J5chの各get,postメソッド内で何を取得したかを判断する為の設定メソッド<br>
	 * 今の所ResultSetAnalizer#analize()にて利用されるだけ<br>
	 *
	 * {@link ResultSetAnalizer#analize(String, String)}
	 *
	 * @param type タイプ
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * ResultSetAnalizer設定メソッド<br>
	 * 今の所何も設定しないかsetAnal(new SuperAnalizer())の２択。<br>
	 * SuperAnalizer()を与えるとdat取得時のSystem.out.println(rs);が良い感じになるかも？
	 *
	 * @param anal アナライザインスタンス
	 */
	public void setAnal(ResultSetAnalizer anal) {
		this.anal = anal;
	}

	/**
	 * セッター
	 * HttpURLConnectionインスタンスを受け取りレスポンスを解析
	 * 各フィールドに値を格納
	 *
	 * @param con
 	 * @param charset
	 * @throws UnkoException
	 */
	public void set(HttpURLConnection con, String charset) throws UnkoException {
		bytes = 0;
		cookie = "";

		try {
			code = con.getResponseCode();
			message = con.getResponseMessage();
			lastmodify = con.getHeaderField("Last-Modified");

			Optional<String> length = Optional.ofNullable(con.getHeaderField("Content-Length"));
			length.ifPresent(e -> {
				bytes = Integer.parseInt(e);
			});

			Optional<List<String>> cookies = Optional.ofNullable(con.getHeaderFields().get("Set-Cookie"));
			cookies.ifPresent(e -> {
				for(String s : e) {
					cookie += s;
				}
			});

			BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName(charset)));
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				add(line);
				anal.analize(line, type);
			}

			r.close();

		} catch (IOException e) {
			throw new UnkoException(e, "HttpURLConnectionメソッドから情報を読み取れませんでした。");
		}
	}

	/**
	 * 文字列変換メソッド
	 * 取得した情報をすべて文字列にして返却する。
	 * 簡単な確認等に使える。
	 */
	public String toString() {
		String buf = "";

		buf += toStringField();
		buf += "\n";
		buf += toStringBody();

		return buf;
	}

	/**
	 * 文字列変換メソッド
	 * 取得した内容だけ文字列にして返却する
	 */
	public String toStringBody() {
		String buf = anal.toStringBody();
		return ( buf != null ) ? buf : String.join("\n", this) + "\n";
	}

	/**
	 * 文字列変換メソッド
	 * 取得したフィールド情報のみを文字列にして返却する
	 */
	public String toStringField() {
		String buf = "";
		buf += "code = " + code + "(message = " + message + ")\n";
		buf += "lastmodify = " + lastmodify + "\n";
		buf += "bytes = " + bytes + "\n";
		buf += "cookie = " + cookie + "\n";

		return buf;
	}

	/**
	 * ヘッダ情報コピーメソッド
	 * 空のResultSetインスタンスを生成し各フィールドのみをコピーして返却
	 *
	 * @return ヘッダ情報のみをコピーしたResultSetインスタンス
	 */
	public ResultSet copyHeader() {
		ResultSet buf = new ResultSet();

		buf.bytes = bytes;
		buf.code = code;
		buf.cookie = cookie;
		buf.lastmodify = lastmodify;
		buf.message = message;

		return buf;
	}

	/**
	 * 新スレ差分取得メソッド
	 * getSubject()で取得した前回のResultSetインスタンスを引数に与える事で、
	 * 今回取得した情報から新スレ差分だけを抽出したResultSetインスタンスを返却。
	 *
	 * @param  before	getSubject()の取得結果
	 * @return getNewSubject(before, true)返却値
	 */
	public ResultSet getNewSubject(ResultSet before) {
		return getNewSubject(before, true);
	}

	/**
	 * 新スレ差分取得メソッド
	 * getSubject()で取得した前回のResultSetインスタンスを引数に与える事で、
	 * 今回取得した情報から新スレ差分だけを抽出したResultSetインスタンスを返却。
	 * 第二引数にtrueを与えるとヘッダ情報もコピーされ、falseを与えるとヘッダ情報は破棄される。
	 *
	 * @param  before	getSubject()の取得結果
	 * @param  field	ヘッダ情報のコピー可否
	 * @return 新スレ差分のみを持つResultSetインスタンス
	 */
	public ResultSet getNewSubject(ResultSet before, boolean field) {
		ResultSet buf = (field) ? copyHeader() : new ResultSet();

		for (String e : this) {
			boolean f = true;

			for (String b : before) {
				if (b.indexOf(e.substring(0, 10)) == 0) {
					f = false;
					break;
				}
			}

			if (f) {
				buf.add(e);
			}
		}

		return buf;
	}

	/**
	 * スレ更新分取得メソッド
	 * 前回取得したResultSetインスタンスを引数に与える事で今回取得した情報から
	 * 更新のあったスレだけを抽出するメソッド。
	 * getNewSubject()が新スレ差分に対するメソッドであるのに対し
	 * こちらは既存スレで更新のあったスレだけを抽出する。
	 *
	 * @param before getSubject()の取得結果
	 * @return getUpdateSubject(before, true)返却値
	 */
	public ResultSet getUpdateSubject(ResultSet before) {
		return getUpdateSubject(before, true);
	}

	/**
	 * スレ更新分取得メソッド
	 * 前回取得したResultSetインスタンスを引数に与える事で今回取得した情報から
	 * 更新のあったスレだけを抽出するメソッド。
	 * getNewSubject()が新スレ差分に対するメソッドであるのに対し
	 * こちらは既存スレで更新のあったスレだけを抽出する。
	 * 第二引数にtrueを与えるとヘッダ情報もコピーされ、falseを与えるとヘッダ情報は破棄される。
	 *
	 * @param before getSubject()の取得結果
	 * @param  field	ヘッダ情報のコピー可否
	 * @return 更新されたスレをまとめたResultSet
	 */
	public ResultSet getUpdateSubject(ResultSet before, boolean field) {
		ResultSet buf = (field) ? copyHeader() : new ResultSet();

		for (String a : this) {
			for (String b : before) {
				if (b.indexOf(a.substring(0, 10)) == 0 && !a.equals(b)) {
					buf.add(a);
					break;
				}
			}
		}

		return buf;
	}

	/**
	 * contentsフィールド取得メソッド<br>
	 * {@link ResultSetAnalizer#getContent()}
	 *
	 * @return contents
	 */
	public List<Content> getContent(){
		return anal.getContent();
	}

	/**
	 * contentフィールドからcontentインスタンスを直接取得するメソッド
	 */
	public Content getContent(int i) {
		return getContent().get(i);
	}

	/**
	 * subjectフィールド取得メソッド<br>
	 * {@link ResultSetAnalizer#getSubject()}
	 *
	 * @return
	 */
	public String getSubject() {
		return anal.getSubject();
	}
}
