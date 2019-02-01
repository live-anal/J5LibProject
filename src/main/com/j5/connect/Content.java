package main.com.j5.connect;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 解析したDAT情報を保持するクラス
 */
public class Content {
	/**
	 * 5chDATの日付フォーマットデフォルト定数<br>
	 * yyyy/MM/dd(E) HH:mm:ss.SS
	 */
	public static final String FORMAT="yyyy/MM/dd(E) HH:mm:ss.SS";

	/**
	 * 5chDATの日付フォーマット<br>
	 * 板によってはデフォルトフォーマットと異なる日付の可能性もある（と思う）<br>
	 * その場合はsetFormat()メソッドで手動設定すること（コンマ三桁程度なら問題なし）。
	 */
	public String format;

	/**
	 * SimpleDateFormatインスタンス
	 */
	private DateFormat df;

	/**
	 * レス日時フィールド
	 */
	private Date date;

	/**
	 * レス名前欄フィールド
	 */
	private String from;

	/**
	 * レスメール欄フィールド
	 */
	private String mail;

	/**
	 * レス本文フィールド
	 */
	private String msg;

	/**
	 * レスIDフィールド
	 */
	private String id;

	/**
	 * 空のContentインスタンスを生成するコンストラクタ
	 * @throws ParseException 日付フォーマット時に発生しうる例外
	 */
	public Content() throws ParseException {
		this(null, null, null, null, null);
	}

	/**
	 * * Contentインスタンス生成コンストラクタ
	 *
	 * @param id		ID
	 * @param date		日付
	 * @param from		名前
	 * @param mail		メール
	 * @param msg		本文
	 * @throws ParseException 日付フォーマット時に発生しうる例外
	 */
	public Content(String id, String date, String from, String mail, String msg) throws ParseException {
		this(id, date, FORMAT, from, mail, msg);
	}

	/**
	 * Contentインスタンス生成コンストラクタ<br>
	 * 日付フォーマットが指定出来るのがミソ。
	 *
	 * @param id		ID
	 * @param date		日付
	 * @param format	日付フォーマット
	 * @param from		名前
	 * @param mail		メール
	 * @param msg		本文
	 * @throws ParseException 日付フォーマット時に発生しうる例外
	 */
	public Content(String id, String date, String format, String from, String mail, String msg) throws ParseException {
		setFormat(format);
		setId(id);
		setDate(date);
		setFrom(from);
		setMail(mail);
		setMessage(msg);
	}

	/**
	 * 日付フォーマット設定メソッド
	 * @param format 日付フォーマット
	 */
	public void setFormat(String format) {
		this.format = format;
		df = new SimpleDateFormat(this.format);
	}

	/**
	 * 日付フォーマット取得メソッド
	 * @return String 日付フォーマット文字列
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * ID取得メソッド
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * ID設定メソッド
	 * @param id ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 日付取得メソッド
	 * @return Date型の日付
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * 日付取得メソッド
	 * @return 指定した日付フォーマットに合わせた日付文字列
	 */
	public String getDateString() {
		if(date!=null) {
			Calendar cal = Calendar.getInstance(new Locale("ja", "JP", "JP"));
			cal.setTime(date);
			return df.format(cal.getTime());
		}else {
			return "";
		}
	}

	/**
	 * 日付設定メソッド
	 *
	 * @param str 日付文字列
	 * @throws ParseException	String型からDate型に変換できなかった場合の例外（フォーマット違いが主）
	 */
	public void setDate(String str) throws ParseException {
		if(!str.trim().isEmpty()) {
			date = df.parse(str);
		} else {
			date = null;
		}
	}

	/**
	 * 名前欄取得メソッド
	 * @return 名前欄
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * 名前設定メソッド
	 * @param from 名前
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * メール欄取得メソッド
	 * @return メール欄
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * メール欄設定メソッド
	 * @param mail メール欄
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * 本文取得メソッド
	 * @return 本文
	 */
	public String getMessage() {
		return msg;
	}

	/**
	 * 本文設定メソッド
	 * @param msg 本文
	 */
	public void setMessage(String msg) {
		this.msg = msg;
	}
}