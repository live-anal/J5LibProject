package main.com.j5.connect.method;

import java.text.ParseException;

import main.com.j5.connect.Content;
import main.com.j5.connect.J5ch;

/**
 * SuperAnalizer<br>
 * datを解析するための重厚なアナライザ<br>
 * 激しく読み書きしたい場合には余り向かないかも知れない。
 */
public class SuperAnalizer extends ResultSetAnalizer {
	String type = "";
	String format = Content.FORMAT;

	/**
	 * コンストラクタ
	 */
	public SuperAnalizer() {
		super();
	}

	/**
	 * コンストラクタ<br>
	 *
	 * 解析時の日付フォーマットを指定出来る。<br>
	 * デフォルトでは"yyyy/MM/dd(E) HH:mm:ss.SS"だが<br>
	 * 日付の形式が異なる場合は例外が発生する可能性があるので引数にて指定可能。
	 *
	 * @param format 日付フォーマット
	 */
	public SuperAnalizer(String format) {
		this();
		this.format = format;
	}

	/**
	 * 解析メソッド<br>
	 * 与えられたlineを名前、メール、日時、ID、本文、タイトルに分解する。<br>
	 * 内部でif(type.equals(J5ch.DAT))としているため、J5ch#get()以外のレスポンスには反応しないので安心安全。
	 */
	public void analize(String line, String type) {
		this.type = type;

		if(type.equals(J5ch.DAT)) {
			String from = "";
			String mail = "";
			String date = "";
			String time = "";
			String id   = "";
			String msg  = "";

			from = line.substring(0, line.indexOf("<>"));
			line = line.substring(from.length()+2);

			mail = line.substring(0, line.indexOf("<>"));
			line = line.substring(mail.length()+2);

			String buf = line.substring(0, line.indexOf("<>"));

			if(!buf.contains("Over 1000 Thread")) {
				date = line.substring(0, line.indexOf(" "));
				line = line.substring(date.length()+1);

				time = line.substring(0, line.indexOf(" "));
				line = line.substring(time.length()+1);

				id = "";
				if(line.indexOf("<>")==0) {
					line = line.substring(line.indexOf("<>")+2);
				} else {
					id = line.substring(line.indexOf(":")+1, line.indexOf("<>"));
					line = line.substring(id.length()+6);
				}
			}else {
				id = buf;
				line = line.substring(id.length()+2);
			}

			msg = replace(line.substring(1, line.indexOf("<>")-1));
			line = line.substring(msg.length()+4);

			if(subject==null && !line.trim().isEmpty()) {
				subject = line.trim();
			}

			try {
				contents.add(new Content(id, date+" "+time, format, from, mail, msg));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 文字列変換メソッド<br>
	 * ResultSetのbodyではなくcontents内から文字列を整形する<br>
	 * 変換結果は見てのお楽しみ。
	 */
	public String toStringBody() {
		if(type.equals(J5ch.DAT)) {
			String str = subject+"\n";

			for(int i=1; i<=contents.size(); i++) {
				Content c = contents.get(i-1);

				String b = c.getDateString();

				if(!b.isEmpty()) {
					str += i+" 名前:"+c.getFrom()+"["+c.getMail()+"] 投稿日:"+b;
					str += (c.getId().isEmpty())? "\n" : " ID:"+c.getId()+"\n";
				}else {
					str += i+" 名前:"+c.getFrom()+"["+c.getMail()+"] 投稿日:"+c.getId()+"\n";
				}
				str += "\t"+c.getMessage().replaceAll("\n", "\n\t")+"\n\n";
			}

			return str + "\n";
		} else {
			return null;
		}
	}

	/**
	 * 本文フィールドの一部文字を置き換えるメソッド
	 *
	 * @param msg 本文
	 * @return replaceした本文
	 */
	private String replace(String msg) {
		msg = msg.replaceAll("<a .*\">", "");
		msg = msg.replaceAll("</a>", "");
		msg = msg.replaceAll("<br> ", "\n");
		msg = msg.replaceAll("<br>" , "\n");
		msg = msg.replaceAll("&gt;", ">");
		msg = msg.replaceAll("&lt;", "<");
		msg = msg.replaceAll("&amp;", "&");

		return msg;
	}
}
