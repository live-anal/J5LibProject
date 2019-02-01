package main.com.j5.connect;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import main.com.j5.connect.method.DefaultAnalizer;
import main.com.j5.connect.method.ResultSetAnalizer;
import main.com.j5.exception.ChinkoException;
import main.com.j5.exception.MankoException;
import main.com.j5.exception.UnkoException;
import main.com.j5.util.OriginMap;
import main.com.j5.util.StringEncoder;

public class J5ch {
	/*
	 * ファイル名に関する共通定数
	 */
	/**
	 * スレ一覧取得用定数兼Type
	 */
	public final static String TXT_SUBJECT = "subject.txt";

	/**
	 * 板設定取得用定数兼Type
	 */
	public final static String TXT_SETTING = "SETTING.TXT";

	/**
	 * 完走レス取得用定数兼Type
	 */
	public final static String TXT_1001 = "1000.txt";

	/**
	 * LocalRule取得用定数兼Type
	 */
	public final static String TXT_HEAD = "head.txt";

	/**
	 * API未認証時のHTMLデータを取得した際にResultSetに与えるType
	 */
	public final static String HTML = "HTML";

	/**
	 * POST時レスポンスのResultSetに与えるType
	 */
	public final static String POST = "POST";

	/**
	 * API認証時のDATデータを取得した際にResultSetに与えるType
	 */
	public final static String DAT = "DAT";

	/*
	 * デフォルトエンコード
	 */
	/**
	 * リクエストのデフォルトエンコード形式
	 */
	public final static String REQUEST_CHARSET = "Shift-JIS";

	/**
	 * レスポンスのデフォルトエンコード形式
	 */
	public final static String RESPONSE_CHARSET = "Shift-JIS";

	/*
	 * 認証用定数
	 */
	/**
	 * API認証時のURL
	 */
	private final static String URI_AUTH = "https://api.5ch.net/v1";

	/**
	 * Ronin認証時のURL
	 */
	private final static String URI_RONIN = "https://2chv.tora3.net/futen.cgi";

	/**
	 * Beホスト
	 */
	private final static String BE_HOST = "be.5ch.net";

	/**
	 * Beログイン用URL
	 */
	@SuppressWarnings("unused")
	private final static String URI_BE = "http://" + BE_HOST;

	/**
	 * 認証時の計算アルゴリズム
	 */
	private final static String ALGO = "HmacSHA256";

	/**
	 * 認証時に使うCT値
	 */
	private final static String CT = "1234567890";

	/*
	 * クッキー処理の分岐用
	 */
	/**
	 * クッキーを保持し再postをする
	 */
	public final static int COOKIE_AND_REPOST = 2;

	/**
	 * クッキーを保持し再postはしない
	 */
	public final static int COOKIE_IN = 1;

	/**
	 * クッキーも保持しないし再postもしない
	 */
	public final static int NOTHING = 0;

	/*
	 * その他定数
	 */
	/**
	 * 新規スレッド作成用submit
	 */
	private final static String Form1 = "新規スレッド作成";

	/**
	 * 書込用submit
	 */
	private final static String Form2 = "書き込む";

	/**
	 * oekaki用ヘッダ情報
	 */
	private final static String IMAGE_DATA = "data:image/png;base64";

	/**
	 * 板一覧取得用URL
	 */
	private final static String URI_BBSLIST = "http://menu.5ch.net/bbsmenu.html";

	/**
	 * 板一覧を解析する開始行
	 */
	private final static int BBSLIST_START = 20;

	/*
	 * 5chに関する変数
	 */
	/**
	 * hoboマップ<br>
	 * datを取得する際にはhobo値の計算をkey毎に行う必要があるため、<br>
	 * 一度計算したhobo値はkeyと紐づけてhoboマップに保持する事で計算の手間を省く。<br>
	 * すなわちhobo.get(key)でデータが取得できると言うことは既に一度取得した事のあるスレであり、<br>
	 * hoboマップは未読・既読の管理にも使える。
	 */
	private Map<String, String> hobo;

	/**
	 * ホスト名
	 */
	private String host;

	/**
	 * 板名
	 */
	private String bbs;

	/**
	 * クッキー文字列（nullあり）
	 */
	private String cookie;

	/**
	 * 仲介プロキシ
	 */
	private Proxy proxy;

	/**
	 * DAT取得用UserAgent<br>
	 * 生DAT取得時にはAPPKeyに対応したUAでの取得が求められるらしいのでPost用UAと分離した。
	 */
	private String getua;

	/**
	 * Post用UserAgent<br>
	 * DAT取得用UserAgentと違ってどんな文字列でも良い。
	 */
	private String postua;

	/*
	 * 5chAPIに関する変数
	 */
	/**
	 * App鍵
	 */
	private String appkey;

	/**
	 * HM鍵
	 */
	private String hmkey;

	/**
	 * 認証用UserAgent
	 */
	private String auth_ua;

	/**
	 * 認証用X_2ch_UA
	 */
	private String auth_xua;

	/**
	 * API認証後のSID
	 */
	private String api_sid;

	/**
	 * API認証時点の時間<br>
	 * APIのSIDは発行後24時間で失効するため長時間プログラムを動かすなら<br>
	 * auth_dateを確認して24時間経過したら再認証する必要がありそう。
	 */
	private Date auth_date;

	/*
	 * 浪人に関する変数
	 */
	/**
	 * 浪人ID
	 */
	private String ID;

	/**
	 * 浪人PW
	 */
	private String PW;

	/**
	 * 浪人用UserAgent<br>
	 * API認証用UAと同じで良いとも聞くしDAT取得用UAと同じで良いとも聞くし良く分からない
	 */
	private String ronin_ua;

	/**
	 * 浪人SID<br>
	 * APIのSIDと同じく発行後24時間で失効
	 */
	private String ronin_sid;

	/**
	 * 浪人SID発行時の時間<br>
	 * APIのSIDと同じく発行後24時間で失効するのでコレを確認して24時間経過したなら再度SIDを発行する必要がある。
	 */
	private Date ronin_date;

	/*
	 * Beに関する変数
	 */
	/**
	 * Be登録メールアドレス
	 */
	private String mail;

	/**
	 * Be登録パスワード
	 */
	private String password;

	/**
	 * Be認証クッキー(MDMD DMDM)
	 */
	private String be_cookie;

	/*
	 * その他変数
	 */
	/**
	 * API機能のONOFF<br>
	 * API認証後はtrueになるのでもしDATじゃなくてHTMLを取得したいとかならfalseに設定してやると良い。
	 */
	private boolean api_auth;

	/**
	 * 浪人機能のONOFF<br>
	 * 浪人認証後はtrueになるのでもし浪人経由での読書をやめたいならfalseに設定してやると良い。
	 */
	private boolean ronin_auth;

	/**
	 * Be機能のONOFF<br>
	 * Be認証後はtrueになるのでもしBe機能をOffにしたいならfalseに設定してやると良い。
	 */
	private boolean be_auth;

	/**
	 * クッキー自動処理機能
	 */
	private int auto_cookie;

	/**
	 * 読込タイムアウト値（ミリ秒）
	 */
	private int readTimeout;

	/**
	 * 接続タイムアウト値（ミリ秒）
	 */
	private int connectTimeout;

	/**
	 * リクエストエンコーディング
	 */
	private String request_charset;

	/**
	 * レスポンスエンコーディング
	 */
	private String response_charset;

	/**
	 * レスポンス取得時の拡張解析インスタンス
	 */
	private ResultSetAnalizer anal;

	/**
	 * コンストラクタ
	 */
	public J5ch() {
		clearHobo();
		clearCookie();
		clearProxy();
		setTimeout(10000, 5000);
		setCharset(REQUEST_CHARSET, RESPONSE_CHARSET);
		setUseAPI(false);
		setUseRonin(false);
		setAnal(new DefaultAnalizer());
		setAutoCookie(NOTHING);
	}

	/**
	 * コンストラクタ
	 *
	 * @param url 板URL（https://agree.5ch.net/liveanarchy/など）
	 */
	public J5ch(String url) {
		this();
		set5ch(url);
	}

	/**
	 * コンストラクタ
	 * @param host ホスト名（agreeなど）
	 * @param bbs	板名（liveanarchyなど）
	 */
	public J5ch(String host, String bbs) {
		this();
		set5ch(host, bbs);
	}

	/**
	 * post時cookie未取得の場合自動で付与して再postするかの設定
	 * デフォルトtrue
	 * @param f 自動付与ONOFF
	 */
	public void setAutoCookie(int define) {
		auto_cookie = define;
	}

	/**
	 * 拡張解析インスタンス設定メソッド<br>
	 * デフォルトではsetAnal(new DefaultAnalizer())を呼んだ事と同義<br>
	 * デフォルトは今までと変わらないResultSetインスタンスを生成する。<br>
	 * setAnal(new SuperAnalizer())を設定するとDATの各レスを細かい粒度で解析し保持する。<br>
	 * ただその分処理に負担が掛かるので激しいレスポンスを行いたいならそのままで良いかと。
	 *
	 * @param anal ResultSetAnalizerインスタンス
	 */
	public void setAnal(ResultSetAnalizer anal) {
		this.anal = anal;
	}

	/**
	 * 拡張解析インスタンス取得メソッド
	 * @return ResultSetAnalizerインスタンス
	 */
	public ResultSetAnalizer getAnal() {
		return anal;
	}

	/**
	 * keyに対応するhobo値取得メソッド
	 * @param key スレ番号
	 * @return hobo値
	 */
	public String getHobo(String key) {
		return hobo.get(key);
	}

	/**
	 * hoboマップを全て取得するメソッド
	 * @return hoboマップ
	 */
	public Map<String, String> getHobo() {
		return hobo;
	}

	/**
	 * 外部からhoboマップを直接取り込むメソッド
	 * @param hobo hoboマップ
	 */
	public void setHobo(Map<String, String> hobo) {
		this.hobo = hobo;
	}

	/**
	 * hobo値とkeyを直接紐づけ保持するメソッド
	 * @param key	key値
	 * @param hobo	hobo値
	 */
	public void setHobo(String key, String hobo) {
		this.hobo.put(key, hobo);
	}

	/**
	 * hoboマップを全てクリアするメソッド<br>
	 * スレが全て未読状態になるため、一部のhobo値を消したいだけであればremoveHobo()を使ってください。<br>
	 * {@link J5ch#removeHobo(String)}
	 */
	public void clearHobo() {
		hobo = new HashMap<String, String>();
	}

	/**
	 * keyに対応するhobo値が取得済みか確認するメソッド<br>
	 * ようは既読済みか否かの判断に使える。
	 * @param key スレ番
	 * @return 取得済みか否か
	 */
	public boolean isHobo(String key) {
		if (hobo.containsKey(key) && hobo.get(key) != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * keyに対応するhobo値を削除するメソッド
	 * @param key
	 */
	public void removeHobo(String key) {
		hobo.remove(key);
	}

	/**
	 * 設定したhostを取得するメソッド
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * 設定したhostを整形して取得するメソッド
	 * @return "{host}.5ch.net"
	 */
	public String getHostDomain() {
		return new String(host + ".5ch.net");
	}

	/**
	 * 設定したhostを整形して取得するメソッド
	 * @return "https://{host}.5ch.net/"
	 */
	public String getHostPath() {
		return getHostPath("https");
	}

	/**
	 * 設定したhostを整形して取得するメソッド
	 * @param protocol プロトコル
	 * @return "{protocol}://{host}.5ch.net/"
	 */
	public String getHostPath(String protocol) {
		return new String(protocol + "://" + getHostDomain() + "/");
	}

	/**
	 * hostを設定するメソッド
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * 設定したbbsを取得するメソッド
	 * @return bbs
	 */
	public String getBBS() {
		return bbs;
	}

	/**
	 * 取得したbbsを整形して取得するメソッド
	 * @return "https://{host}.5ch.net/{bbs}"
	 */
	public String getBBSPath() {
		return getBBSPath("https");
	}

	/**
	 * 設定したbbsを取得するメソッド
	 * @param protocol プロトコル
	 * @return "{protocol}://{host}.5ch.net/{bbs}"
	 */
	public String getBBSPath(String protocol) {
		return new String(getHostPath(protocol) + bbs + "/");
	}

	/**
	 * bbs設定メソッド
	 * @param bbs
	 */
	public void setBBS(String bbs) {
		this.bbs = bbs;
	}

	/**
	 * bbs.cgiのフルパスを取得するメソッド
	 * @param protocol プロトコル
	 * @return "{protocol}://{host}.5ch.net/test/bbs.cgi"
	 */
	public String getPath(String protocol) {
		return new String(getHostPath(protocol) + "test/bbs.cgi/");
	}

	/**
	* read.cgiのフルパスを取得するメソッド
	* @param protocol プロトコル
	* @return "{protocol}://{host}.5ch.net/test/bbs.cgi"
	*/
	public String getReadPath(String protocol) {
		return new String(getHostPath(protocol) + "test/read.cgi/");
	}

	/**
	 * スレのフルパスを取得するメソッド
	 * @param protocol プロトコル
	 * @param key スレ番
	 * @return "{protocol}://{host}.5ch.net/test/bbs.cgi/{bbs}/{key}/"
	 */
	public String getPath(String protocol, String key) {
		return new String(getPath(protocol) + bbs + "/" + key + "/");
	}

	/**
	 * スレのフルパスを取得するメソッド
	 * @param protocol プロトコル
	 * @param key スレ番
	 * @return "{protocol}://{host}.5ch.net/test/read.cgi/{bbs}/{key}/"
	 */
	public String getReadPath(String protocol, String key) {
		return new String(getReadPath(protocol) + bbs + "/" + key + "/");
	}

	/**
	 * host,bbs同時設定メソッド
	 * @param host
	 * @param bbs
	 */
	public void set5ch(String host, String bbs) {
		setHost(host);
		setBBS(bbs);
	}

	/**
	 * URLからhost,bbsを設定するメソッド
	 * @param url "https://agree.5ch.net/liveanarchy"など
	 */
	public void set5ch(String url) {
		if (url.contains("://"))
			url = url.substring(url.indexOf("://") + 3, url.length());

		String host = url.substring(0, url.indexOf("."));
		String bbs = url.substring(url.indexOf(".net/") + 5, url.length());
		bbs = bbs.substring(0, bbs.indexOf("/"));

		set5ch(host, bbs);
	}

	/**
	 * クッキー取得メソッド
	 * @return
	 */
	public String getCookie() {
		return cookie;
	}

	/**
	 * クッキー設定メソッド
	 * @param cookie
	 */
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	/**
	 * クッキー初期化メソッド
	 */
	public void clearCookie() {
		this.cookie = null;
	}

	/**
	 * プロキシ設定取得メソッド
	 * @return Proxyインスタンス
	 */
	public Proxy getProxy() {
		return proxy;
	}

	/**
	 * プロキシ設定メソッド
	 * @param proxy Proxyインスタンス
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * プロキシ設定メソッド
	 * @param type 「Proxy.Type.{HTTP/HTTPS}」など、プロキシのタイプ
	 * @param addr プロキシアドレス
	 * @param port プロキシポート
	 */
	public void setProxy(Proxy.Type type, String addr, int port) {
		setProxy(new Proxy(type, new InetSocketAddress(addr, port)));
	}

	/**
	 * プロキシ設定メソッド
	 * @param addr プロキシアドレス
	 * @param port プロキシポート
	 */
	public void setProxy(String addr, int port) {
		setProxy(Proxy.Type.HTTP, addr, port);
	}

	/**
	 * プロキシ設定メソッド
	 * @param type 「Proxy.Type.{HTTP/HTTPS}」など、プロキシのタイプ
	 * @param addr_and_port "xxx.xxx.xxx.xxx:yyyy"とか"xxx.xxx.xxx.xxx    yyyy"とかアドレスとポートがセットになった文字列
	 * @param regex アドレスとポートを何の文字列を基準にで分けるかの正規表現（":"とか"[ ]+"とかでOK）
	 */
	public void setProxy(Proxy.Type type, String addr_and_port, String regex) {
		String[] proxy = addr_and_port.split(regex, 2);

		setProxy(type, proxy[0], Integer.parseInt(proxy[1]));
	}

	/**
	 * プロキシ設定メソッド
	 * @param addr_and_port "xxx.xxx.xxx.xxx:yyyy"とか"xxx.xxx.xxx.xxx    yyyy"とかアドレスとポートがセットになった文字列
	 * @param regex アドレスとポートを何の文字列を基準にで分けるかの正規表現（":"とか"[ ]+"とかでOK）
	 */
	public void setProxy(String addr_and_port, String regex) {
		setProxy(Proxy.Type.HTTP, addr_and_port, regex);
	}

	/**
	 * プロキシ初期化メソッド
	 * setProxy(Proxy.NO_PROXY);
	 */
	public void clearProxy() {
		setProxy(Proxy.NO_PROXY);
	}

	/**
	 * DAT取得用UAのゲッター
	 * @return DAT取得UA
	 */
	public String getGetUA() {
		return getua;
	}

	/**
	 * DAT取得用UAのセッター
	 * @param getua DAT取得UA
	 */
	public void setGetUA(String getua) {
		this.getua = getua;
	}

	/**
	 * 書込用UAのゲッター
	 * @return 書込用UA
	 */
	public String getPostUA() {
		return postua;
	}

	/**
	 * 書込用UAのセッター
	 * @param postua 書込用UA
	 */
	public void setPostUA(String postua) {
		this.postua = postua;
	}

	/**
	 * 設定したAPP鍵の取得
	 * @return APP鍵
	 */
	public String getAppKey() {
		return appkey;
	}

	/**
	 * 設定したHM鍵の取得
	 * @return HM鍵
	 */
	public String getHMKey() {
		return hmkey;
	}

	/**
	 * 設定した認証用UAの取得
	 * @return 認証用UA
	 */
	public String getAuthUA() {
		return auth_ua;
	}

	/**
	 * 設定した認証用XUAの取得
	 * @return X_2ch_UA
	 */
	public String getAuthXUA() {
		return auth_xua;
	}

	/**
	 * 認証に必要な情報を設定するメソッド
	 * @param appkey	APP鍵
	 * @param hmkey	HM鍵
	 * @param ua		認証用Useragent
	 * @param xua		認証用X_2ch_UA
	 * @param getua	Get用UAを認証用UAと同じにするか
	 * @param roninua	浪人用UAを認証用UAと同じにするか
	 */
	public void set5chAPI(String appkey, String hmkey, String ua, String xua, boolean getua, boolean roninua) {
		if (getua)
			setGetUA(ua);
		if (roninua)
			setRoninUA(xua);

		this.appkey = appkey;
		this.hmkey = hmkey;
		this.auth_ua = ua;
		this.auth_xua = xua;
	}

	/**
	 * 認証に必要な情報を設定するメソッド<br>
	 * 浪人用UAも自動で認証用UAと同じに設定されるので注意<br>
	 * もし浪人用UAは別の物を使いたい場合はこのメソッド「呼び出し後」に設定する事！
	 *
	 * @param appkey	APP鍵
	 * @param hmkey	HM鍵
	 * @param ua		認証用Useragent
	 * @param xua		認証用X_2ch_UA
	 * @param getua	Get用UAを認証用UAと同じにするか
	 */
	public void set5chAPI(String appkey, String hmkey, String ua, String xua, boolean getua) {
		set5chAPI(appkey, hmkey, ua, xua, true, true);
	}

	/**
	 * 認証に必要な情報を設定するメソッド<br>
	 * 浪人用UAもDAT取得用UAも自動で認証用UAと同じに設定されるので注意<br>
	 * もし浪人用UAとDAT取得用UAは別の物を使いたい場合、このメソッド「呼び出し後」に設定する事！
	 * @param appkey	APP鍵
	 * @param hmkey	HM鍵
	 * @param ua		認証用Useragent
	 * @param xua		認証用X_2ch_UA
	 */
	public void set5chAPI(String appkey, String hmkey, String ua, String xua) {
		set5chAPI(appkey, hmkey, ua, xua, true);
	}

	/**
	 * API認証後のSID取得メソッド
	 * @return SID
	 */
	public String getAPISID() {
		return api_sid;
	}

	/**
	 * 外部からAPISIDを設定するメソッド
	 * @param sid SID
	 */
	public void setAPISID(String sid) {
		this.api_sid = sid;
	}

	/**
	 * 認証した時間を取得するメソッド
	 * @return Date型の時間
	 */
	public Date getAuthDate() {
		return auth_date;
	}

	/**
	 * 認証した時間を設定するメソッド
	 * @param date Date型の時間
	 */
	public void setAuthDate(Date date) {
		auth_date = date;
	}

	/**
	 * 認証した時間文字列をDate型に変換して設定するメソッド
	 * @param date 認証時間
	 * @throws ParseException StringをDateに変換する際に発生しうる例外（フォーマットに沿わない文字列が与えられた場合など）
	 */
	public void setAuthDate(String date) throws ParseException {
		DateFormat df = new SimpleDateFormat();
		auth_date = df.parse(date);
	}

	/**
	 * 浪人ID取得メソッド
	 * @return 浪人ID
	 */
	public String getRoninID() {
		return ID;
	}

	/**
	 * 浪人PW取得メソッド
	 * @return 浪人PW
	 */
	public String getRoninPW() {
		return PW;
	}

	/**
	 * 浪人ID,PW設定メソッド
	 * @param ID 浪人ID
	 * @param PW 浪人PW
	 */
	public void setRonin(String ID, String PW) {
		this.ID = ID;
		this.PW = PW;
	}

	/**
	 * 浪人用Useragent取得メソッド
	 * @return 浪人用Useragent
	 */
	public String getRoninUA() {
		return ronin_ua;
	}

	/**
	 * 浪人用Useragent設定メソッド
	 * @param ua 浪人用Useragent
	 */
	public void setRoninUA(String ua) {
		this.ronin_ua = ua;
	}

	/**
	 * 浪人SID取得メソッド
	 * @return 浪人SID
	 */
	public String getRoninSID() {
		return ronin_sid;
	}

	/**
	 * 浪人SID設定メソッド
	 * @param sid 浪人SID
	 */
	public void setRoninSID(String sid) {
		this.ronin_sid = sid;
	}

	/**
	 * 浪人認証時間取得メソッド
	 * @return Date型時間
	 */
	public Date getRoninDate() {
		return ronin_date;
	}

	/**
	 * 浪人認証時間設定メソッド
	 * @param date
	 */
	public void setRoninDate(Date date) {
		ronin_date = date;
	}

	/**
	 * 浪人認証時間設定メソッド<br>
	 * {@link J5ch#setAuthDate(String)}
	 *
	 * @param date
	 * @throws ParseException
	 */
	public void setRoninDate(String date) throws ParseException {
		DateFormat df = new SimpleDateFormat();
		ronin_date = df.parse(date);
	}

	/**
	 * Beメール取得メソッド
	 * @return Beメール
	 */
	public String getBeMail() {
		return mail;
	}

	/**
	 * Beパスワード取得メソッド
	 * @return Beパスワード
	 */
	public String getBePassword() {
		return password;
	}

	/**
	 * Be認証情報設定メソッド
	 * @param mail Beメール
	 * @param pw	Beパスワード
	 */
	public void setBe(String mail, String pw) {
		this.mail = mail;
		this.password = pw;
	}

	/**
	 * Be認証後クッキー（DMDM, MDMD）設定メソッド
	 * @param cookie Be付与クッキー
	 */
	public void setBeCookie(String cookie) {
		be_cookie = cookie;
	}

	/**
	 * Be認証後クッキー（DMDM, MDMD）取得メソッド
	 * @return Be付与クッキー
	 */
	public String getBeCookie() {
		return be_cookie;
	}

	/**
	 * API機能確認メソッド
	 * @return true=API経由での読込、 false=HTML直接取得
	 */
	public boolean isAPI() {
		return api_auth;
	}

	/**
	 * API機能設定メソッド<br>
	 * ※ APISID取得済みか確認して認証完了していることを担保してください。
	 * @param f true=API経由での読込、 false=HTML直接取得
	 */
	public void setUseAPI(boolean f) {
		this.api_auth = f;
	}

	/**
	 * 浪人機能確認メソッド
	 * @return true=浪人経由での読み書き、 false=浪人機能無効化
	 */
	public boolean isRonin() {
		return ronin_auth;
	}

	/**
	 * 浪人機能設定メソッド<br>
	 * このメソッドは主に書込時の浪人機能を制御するものになります。<br>
	 * 浪人で過去ログを取得するか否かを切り替える場合は、setUseRonin()の後に再度API認証を行う必要があります。<br>
	 * API認証の際に浪人ID,PWも渡され、APISIDに埋め込む形で適用されてしまうためです。<br>
	 * @param f
	 */
	public void setUseRonin(boolean f) {
		this.ronin_auth = f;
	}

	/**
	 * Be機能確認メソッド（Be機能はまだ未実装です）
	 * @return
	 */
	public boolean isBe() {
		return be_auth;
	}

	/**
	 * Be機能設定メソッド（Be機能はまだ未実装です）
	 * @param f
	 */
	public void setUseBe(boolean f) {
		be_auth = f;
	}

	/**
	 * 読込タイムアウト値取得メソッド
	 * @return
	 */
	public int getReadTimeout() {
		return readTimeout;
	}

	/**
	 * 接続タイムアウト値取得メソッド
	 * @return
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * 読込・接続タイムアウト値設定メソッド
	 * @param read		読込タイムアウト値（デフォルト10000ms）
	 * @param connect	接続タイムアウト値（デフォルト5000ms）
	 */
	public void setTimeout(int read, int connect) {
		readTimeout = read;
		connectTimeout = connect;
	}

	/**
	 * エンコーディング形式設定メソッド<br>
	 * リクエスト、レスポンスともに設定されるため注意してください。<br>
	 * 基本的にはリクエスト=UTF-8、レスポンス=Shift-JISでやり取りしてください。
	 * @param charset エンコーディング形式
	 */
	public void setCharset(String charset) {
		setCharset(charset, charset);
	}

	/**
	 * エンコーディング形式設定メソッド<br>
	 * 基本的にはリクエスト=UTF-8、レスポンス=Shift-JISでやり取りしてください。
	 * @param request  リクエストエンコーディング（デフォルトUTF-8）
	 * @param response レスポンスエンコーディング（デフォルトShift-JIS）
	 */
	public void setCharset(String request, String response) {
		setRequestCharset(request);
		setResponseCharset(response);
	}

	/**
	 * リクエストエンコーディング設定メソッド
	 * @param charset リクエストエンコーディング（デフォルトUTF-8）
	 */
	public void setRequestCharset(String charset) {
		this.request_charset = charset;
	}

	/**
	 * レスポンスエンコーディング設定メソッド
	 * @param charset レスポンスエンコーディング（デフォルトShift-JIS）
	 */
	public void setResponseCharset(String charset) {
		this.response_charset = charset;
	}

	/**
	 * リクエストエンコーディング取得メソッド
	 * @return charset文字列
	 */
	public String getRequestCharset() {
		return request_charset;
	}

	/**
	 * レスポンスエンコーディング取得メソッド
	 * @return charset文字列
	 */
	public String getResponseCharset() {
		return response_charset;
	}

	/**
	 * API認証メソッド<br>
	 * 先に{@link J5ch#set5chAPI(String, String, String, String)}を呼ぶか、{@link J5ch#auth5chAPI(String, String, String, String)}を呼んで認証情報を設定してください。<br>
	 * また浪人での過去ログ取得機能をONにする場合は認証する前に{@link J5ch#authRonin(String, String)}で浪人認証してください。
	 *
	 * @return 認証結果（code=200であればtrue）
	 * @throws ChinkoException 認証時に発生しうる例外
	 */
	public boolean auth5chAPI() throws ChinkoException {
		String HB;

		if ((HB = hmac(hmkey, appkey + CT, ALGO)) != null) {
			if (api_auth(appkey, CT, HB) == 200) {
				setUseAPI(true);
			}
		}

		return api_auth;
	}

	/**
	 * API認証情報設定兼メソッド<br>
	 * DAT取得用UA、浪人UAはuaに統一されます。
	 *
	 * @param appkey	App鍵
	 * @param hmkey	HM鍵
	 * @param ua		認証用Useragent
	 * @param xua		認証用X_2ch_UA
	 * @return			認証結果（code=200であればtrue）
	 * @throws			ChinkoException 認証時に発生しうる例外
	 */
	public boolean auth5chAPI(String appkey, String hmkey, String ua, String xua) throws ChinkoException {
		set5chAPI(appkey, hmkey, ua, xua, true, true);
		return auth5chAPI();
	}

	/**
	 * 認証時に必要なHMAC計算メソッド
	 * @param hmkey	HM鍵
	 * @param msg		APP+CT値
	 * @param algo		HmacSHA256
	 * @return hmac値
	 */
	private String hmac(String hmkey, String msg, String algo) {
		StringBuilder sb = null;

		try {
			Mac mac = Mac.getInstance(algo);
			mac.init(new SecretKeySpec(hmkey.getBytes(), algo));
			byte[] mac_bytes = mac.doFinal(msg.getBytes());
			sb = new StringBuilder(2 * mac_bytes.length);

			for (byte b : mac_bytes) {
				sb.append(String.format("%02x", b & 0xff));
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
		}

		return (sb != null) ? sb.toString() : null;
	}

	/**
	 * API認証を行うメソッド
	 *
	 * @param appkey
	 * @param CT
	 * @param HB
	 * @return
	 * @throws ChinkoException
	 */
	private int api_auth(String appkey, String CT, String HB) throws ChinkoException {
		int code = 0;
		String value = (ronin_auth) ? "ID=" + ID + "&PW=" + PW + "&KY=" + appkey + "&CT=" + CT + "&HB=" + HB
				: "ID=&PW=&KY=" + appkey + "&CT=" + CT + "&HB=" + HB;
		String sid = "";

		HttpsURLConnection con;

		try {
			URL url = new URL(URI_AUTH + "/auth/");
			con = (HttpsURLConnection) url.openConnection(proxy);

			con.setDoInput(true);
			con.setDoOutput(true);

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Length", getBytes(value) + "");
			con.setRequestProperty("User-Agent", auth_ua);
			con.setRequestProperty("X-2ch-UA", auth_xua);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectTimeout);

		} catch (IOException e) {
			throw new ChinkoException(e, "認証URLの文字列が不正です。解析出来ませんでした。" + URI_AUTH + "/auth/");
		}

		try {
			con.connect();

			BufferedWriter w = getBufferedWriter(con);
			w.write(value);
			w.flush();

			setAuthDate(new Date());

			BufferedReader r = getBufferedReader(con);
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				sid += line;
			}

			code = con.getResponseCode();
			api_sid = (!sid.equals("")) ? sid.substring(sid.indexOf(":") + 1, sid.length()) : null;

			w.close();
			r.close();
			con.disconnect();

		} catch (IOException e) {
			e.printStackTrace();
			throw new ChinkoException(e, "API鯖が見つかりません。ネットワーク未接続か鯖の移動等の障害が考えられます。" + URI_AUTH + "/auth/");
		}

		return code;
	}

	/**
	 * 浪人認証を行うメソッド<br>
	 * API認証より前に認証する事を推奨。
	 *
	 * @return code=200であればtrue（SIDがpppppppp....の場合も200であればtrueなので注意）
	 * @throws ChinkoException 例外
	 */
	public boolean authRonin() throws ChinkoException {
		if (ronin_auth(ID, PW) == 200) {
			setUseRonin(true);
		}

		return ronin_auth;
	}

	/**
	 * 認証兼設定を行うメソッド<br>
	 * API認証より前に認証する事を推奨。
	 *
	 * @param ID	浪人ID
	 * @param PW	浪人PW
	 * @return code=200であればtrue（SIDがpppppppp....の場合も200であればtrueなので注意）
	 * @throws ChinkoException 例外
	 */
	public boolean authRonin(String ID, String PW) throws ChinkoException {
		setRonin(ID, PW);
		return authRonin();
	}

	/**
	 * 浪人認証メソッド
	 * @param ID
	 * @param PW
	 * @return
	 * @throws ChinkoException
	 */
	private int ronin_auth(String ID, String PW) throws ChinkoException {
		int code = 0;
		String value = "ID=" + ID + "&PW=" + PW;
		String sid = "";

		HttpsURLConnection con;
		try {
			URL url = new URL(URI_RONIN);

			con = (HttpsURLConnection) url.openConnection(proxy);
			con.setDoInput(true);
			con.setDoOutput(true);

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Length", getBytes(value) + "");
			con.setRequestProperty("User-Agent", ronin_ua);
			con.setRequestProperty("X-2ch-UA", auth_xua);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectTimeout);
		} catch (IOException e) {
			throw new ChinkoException(e, "浪人URLの文字列が不正です。解析出来ませんでした。" + URI_RONIN);
		}

		try {
			con.connect();

			BufferedWriter w = getBufferedWriter(con);
			w.write(value);
			w.flush();

			setRoninDate(new Date());

			BufferedReader r = getBufferedReader(con);
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				sid += line;
			}

			sid = sid.substring(sid.indexOf("=") + 1, sid.length());

			ronin_ua = sid.substring(0, sid.indexOf(":"));
			ronin_ua += "(" + auth_xua + ")";

			code = con.getResponseCode();

			ronin_sid = sid;

			w.close();
			r.close();
			con.disconnect();

		} catch (IOException e) {
			e.printStackTrace();
			throw new ChinkoException(e, "浪人鯖が見つかりません。ネットワーク未接続か鯖の移動等の障害が考えられます。" + URI_AUTH + "/auth/");
		}

		return code;
	}

	/**
	 * Be認証メソッド（未実装）
	 *
	 * @return	code=200であればtrue（未実装なので必ず404/falseが返ります）
	 * @throws ChinkoException 例外
	 */
	public boolean authBe() throws ChinkoException {
		return authBe(mail, password);
	}

	/**
	 * Be認証メソッド（未実装）
	 *
	 * @param mail
	 * @param password
	 * @return
	 * @throws ChinkoException
	 */

	public boolean authBe(String mail, String password) throws ChinkoException {
		setBe(mail, password);

		if (be_auth() == 200) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Be認証メソッド（未実装）
	 *
	 * @return
	 * @throws ChinkoException
	 */

	private int be_auth() throws ChinkoException {
		// TODO BEログイン処理

		return 404;
	}

	/**
	 * スレ一覧取得メソッド
	 * @return スレ一覧ResultSetインスタンス
	 * @throws UnkoException 取得できなかった場合の例外
	 */
	public ResultSet getSubject() throws UnkoException {
		return getResultSet(getTXT(TXT_SUBJECT), TXT_SUBJECT);
	}

	/**
	 * スレ一覧取得メソッド<br>
	 * 引数にmodifyを設定すると、スレ一覧が更新されていれば中身が返る。<br>
	 * 更新されていなければcode=302で中身は空のResultSetインスタンスが返る。
	 *
	 * @param modify	前回取得したmodify情報（getSubject(rs.lastmodify);）
	 * @return スレ一覧ResultSetインスタンス
	 * @throws UnkoException 取得できなかった場合の例外
	 */
	public ResultSet getSubject(String modify) throws UnkoException {
		return getResultSet(getTXT(TXT_SUBJECT, modify), TXT_SUBJECT);
	}

	/**
	 * 板設定を取得するメソッド
	 * @return
	 * @throws UnkoException
	 */
	public ResultSet getSetting() throws UnkoException {
		return getResultSet(getTXT(TXT_SETTING), TXT_SETTING);
	}

	/**
	 * 完走時に書き込まれるAAの取得メソッド
	 * @return
	 * @throws UnkoException
	 */
	public ResultSet get1001() throws UnkoException {
		return getResultSet(getTXT(TXT_1001), TXT_SETTING);
	}

	/**
	 * ローカルルール取得メソッド
	 * @return
	 * @throws UnkoException
	 */
	public ResultSet getHead() throws UnkoException {
		return getResultSet(getTXT(TXT_HEAD), TXT_SETTING);
	}

	/**
	 * http://{host}.5ch.net/{bbs}/{filename}にアクセスして取得するメソッド
	 *
	 * @param filename	ファイル名
	 * @return HttpURLConnectionインスタンス
	 * @throws UnkoException
	 */
	public HttpURLConnection getTXT(String filename) throws UnkoException {
		return getTXT(filename, null);
	}

	/**
	 * http://{host}.5ch.net/{bbs}/{filename}にアクセスして取得するメソッド
	 * @param filename ファイル名
	 * @param modify	前回取得modify情報
	 * @return
	 * @throws UnkoException
	 */
	public HttpURLConnection getTXT(String filename, String modify) throws UnkoException {
		HttpURLConnection con;

		try {
			URL url = new URL(getBBSPath("http") + filename);
			con = (HttpURLConnection) url.openConnection(proxy);

			con.setDoInput(true);
			con.setDoOutput(false);

			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", getua);
			con.setRequestProperty("Connection", "close");
			if (modify != null)
				con.setRequestProperty("If-Modified-Since", modify);
			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectTimeout);

			con.connect();

		} catch (IOException e) {
			throw new UnkoException(e, filename + "を取得できませんでした。" + getBBSPath("http") + filename);
		}

		return con;
	}

	/**
	 * 板一覧取得メソッド<br>
	 * カテゴリ名<br>
	 * 　　├板名 : URL<br>
	 * 　　…<br>
	 * と言った形でMap&lt;category, Map&lt;name, url&gt;&gt;インスタンスが返される。<br>
	 * 全文表示したいなら下記のように書くと良いかも知れない。<br>
	 * <p>
	 * Map&lt;String, Map&lt;String, String&gt;&gt; list = j5.getBBSList();<br>
	 * for(String category : list.keySet()){<br>
	 * 　　System.out.println(category);<br>
	 * <br>
	 * 　　Map&lt;String, String&gt; bbs = list.get(category);<br>
	 * 　　for(String name : bbs.keySet()){<br>
	 * 　　　　String url = bbs.get(name);<br>
	 * 　　　　<br>
	 * 　　　　System.out.println("\t" + name + " = " + url);<br>
	 * 　　}<br>
	 * }<br>
	 * </p>
	 * @return 板一覧
	 * @throws UnkoException
	 */
	public Map<String, Map<String, String>> getBBSList() throws UnkoException {
		Map<String, Map<String, String>> bbslist = new LinkedHashMap<>();
		HttpURLConnection con;

		try {
			URL url = new URL(URI_BBSLIST);
			con = (HttpURLConnection) url.openConnection();

			con.setDoInput(true);
			con.setDoOutput(false);

			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", getua);
			con.setRequestProperty("Connection", "close");
			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectTimeout);

			con.connect();
		} catch (IOException e) {
			throw new UnkoException(e, URI_BBSLIST + "が見つかりません。");
		}

		try {
			BufferedReader r = getBufferedReader(con);

			// 10行送り
			for (int i = 0; i < BBSLIST_START; i++) {
				r.readLine();
			}

			// データを読み取りres変数に格納
			String category = "";
			for (String line = r.readLine(); line != null; line = r.readLine()) {

				// カテゴリ名取得
				if (line.indexOf("<br><br><B>") == 0) {
					category = line.substring(line.indexOf("<B>") + 3, line.lastIndexOf("</B>")).trim();
					bbslist.put(category, new LinkedHashMap<>());

					// 各板URL取得
				} else if (line.indexOf("<A HREF") >= 0 && !line.contains("mailto")) {
					line = line.substring(line.indexOf("=") + 1, line.length());
					String link = line.substring(0, line.indexOf(">")).trim();
					String name = line.substring(line.indexOf(">") + 1, line.lastIndexOf("</A>")).trim();
					bbslist.get(category).put(name, link);
				}
			}
		} catch (IOException e) {
			throw new UnkoException(e, "解析中にエラーが発生しました。HTMLのフォーマットが変わったかも知れません。");
		}

		return bbslist;
	}

	/**
	 * 指定スレ取得メソッド
	 * @param key スレ番
	 * @return API認証済みであればDAT、未認証であればHTMLが返る
	 * @throws UnkoException
	 */
	public ResultSet get(String key) throws UnkoException {
		return get(key, null, -1);
	}

	/**
	 * 指定スレ取得メソッド
	 * @param key 		スレ番
	 * @param modify	前回取得時間  （DAT取得時のみ適用可）
	 * @param bytes	取得済みサイズ（DAT取得時のみ適用可）
	 * @return
	 * @throws UnkoException
	 */
	public ResultSet get(String key, String modify, int bytes) throws UnkoException {
		if (!api_auth) {
			return getResultSet(getHTML(key), HTML);
		} else {
			return getResultSet(getDat(key, modify, bytes), DAT);
		}
	}

	/**
	 * HTML形式でスレを取得するメソッド
	 * @param key スレ番
	 * @return HttpURLConnectionインスタンス
	 * @throws UnkoException
	 */
	public HttpURLConnection getHTML(String key) throws UnkoException {
		HttpURLConnection con;

		try {
			URL url = new URL(getReadPath("http", key));
			con = (HttpURLConnection) url.openConnection(proxy);
			con.setDoInput(true);
			con.setDoOutput(false);

			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", getua);
			con.setRequestProperty("Connection", "close");
			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectTimeout);
			con.connect();
		} catch (IOException e) {
			throw new UnkoException(e, "スレッドにアクセスできません。" + getReadPath("https", key));
		}

		return con;
	}

	/**
	 * DAT形式でスレを取得するメソッド
	 * @param key		スレ番
	 * @param modify	前回取得日時（null可）
	 * @param bytes	取得済みサイズ
	 * @return	HttpURLConnectionインスタンス
	 * @throws UnkoException
	 */
	public HttpsURLConnection getDat(String key, String modify, int bytes) throws UnkoException {
		String hobo = hobo(key);
		HttpsURLConnection con;

		try {
			URL url = new URL(URI_AUTH + "/" + getHost() + "/" + getBBS() + "/" + key);
			con = (HttpsURLConnection) url.openConnection(proxy);

			con.setDoInput(true);
			con.setDoOutput(true);

			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", getua);
			con.setRequestProperty("Connection", "close");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			if (modify != null && bytes > -1) {
				con.setRequestProperty("If-Modified-Since", modify);
				con.setRequestProperty("Range", "bytes=" + bytes + "-");
			}

			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectTimeout);

			con.connect();

			BufferedWriter w = getBufferedWriter(con);
			w.write("sid=" + api_sid + "&hobo=" + hobo + "&appkey=" + appkey);
			w.flush();

			w.close();

		} catch (IOException e) {
			throw new UnkoException(e, "DATを取得できませんでした。" + getPath("https", key));
		}

		return con;
	}

	/**
	 * 書込メソッド<br>
	 * レスを書き込む。
	 *
	 * @param key	スレ番
	 * @param from	名前欄
	 * @param mail	メ欄
	 * @param msg	本文
	 * @return 書込結果
	 * @throws UnkoException	うんブリ
	 * @throws MankoException	まんビラ
	 */
	public ResultSet post(String key, String from, String mail, String msg)
			throws UnkoException, MankoException {
		return post(false, key, from, mail, msg);
	}

	/**
	 * 書込メソッド<br>
	 * お絵かき付きでレスを書き込む。
	 *
	 * @param key	スレ番
	 * @param from	名前欄
	 * @param mail	メ欄
	 * @param msg	本文
	 * @param oekaki	お絵かきFileインスタンス
	 * @return 書込結果
	 * @throws UnkoException	うんブリ
	 * @throws MankoException	まんビラ
	 */
	public ResultSet post(String key, String from, String mail, String msg, File oekaki)
			throws UnkoException, MankoException {
		return post(false, key, from, mail, msg, oekaki);
	}

	/**
	 * 書込メソッド<br>
	 * 第一引数trueでスレ立て、falseでレス投稿に切り替わる。<br>
	 * 第二引数は第一引数に応じてkeyかスレタイを入れること。<br>
	 *
	 * @param f スレ立て=true、レス=false
	 * @param key_or_subject	trueの場合立てるスレタイ、falseの場合書き込むスレ番
	 * @param from	名前欄
	 * @param mail	メ欄
	 * @param msg	本文
	 * @return 書込結果
	 * @throws UnkoException	うんブリ
	 * @throws MankoException	まんビラ
	 */
	public ResultSet post(boolean f, String key_or_subject, String from, String mail, String msg) throws UnkoException, MankoException {
		return post(f, key_or_subject, from, mail, msg, null);
	}

	/**
	 * 書込メソッド<br>
	 * 第一引数trueでスレ立て、falseでレス投稿に切り替わる。<br>
	 * 第二引数は第一引数に応じてkeyかスレタイを入れること。<br>
	 * oekakiはスレ立て時は無効化される。
	 *
	 * @param f スレ立て=true、レス=false
	 * @param key_or_subject	trueの場合立てるスレタイ、falseの場合書き込むスレ番
	 * @param from	名前欄
	 * @param mail	メ欄
	 * @param msg	本文
	 * @param oekaki お絵かきFileインスタンス
	 * @return 書込結果
	 * @throws UnkoException	うんブリ
	 * @throws MankoException	まんビラ
	 */
	public ResultSet post(boolean f, String key_or_subject, String from, String mail, String msg, File oekaki) throws UnkoException, MankoException {
		from   = convertNyoro(from, "Shift-JIS");
		mail   = convertNyoro(mail, "Shift-JIS");
		msg    = convertNyoro(msg, "Shift-JIS");

		if (f)
			return getResultSet(postThread(key_or_subject, from, mail, msg), POST);

		else if (!f && oekaki == null)
			return getResultSet(postResponse(key_or_subject, from, mail, msg), POST);

		else
			return getResultSet(postResponse(key_or_subject, from, mail, msg, oekaki), POST);
	}

	/**
	 * 書込メソッド<br>
	 * 第一引数は下記のデータを設定する。<br>
	 * {@link J5ch#createThreadPostdata(String, String, String, String)}<br>
	 * {@link J5ch#createResponsePostdata(String, String, String, String)}<br>
	 * {@link J5ch#createResponsePostdata(String, String, String, String, File)}<br>
	 * <br>
	 * 第二引数は取得したpostdataを下記メソッドでbyte長計算した結果を設定する。<br>
	 * {@link J5ch#getBytes(String)}
	 *
	 * @param postdata	ポストデータ
	 * @param bytes	バイト長
	 * @return	HttpURLConnectionインスタンス
	 * @throws UnkoException
	 */
	public HttpsURLConnection post(String postdata, int bytes) throws UnkoException {
		HttpsURLConnection con = null;

		try {
			URL url = new URL(getPath("https"));
			con = (HttpsURLConnection) url.openConnection(proxy);

			con.setDoInput(true);
			con.setDoOutput(true);

			con.setRequestMethod("POST");
			con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			con.setRequestProperty("Content-length", bytes + "");
			con.setRequestProperty("Referer", getBBSPath("https"));
			con.setRequestProperty("Connection", "close");

			if (ronin_auth)
				con.setRequestProperty("User-Agent", ronin_ua);
			else
				con.setRequestProperty("User-Agent", postua);
			if (cookie != null)
				con.setRequestProperty("Cookie", cookie);

			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectTimeout);

			con.connect();

			BufferedWriter w = getBufferedWriter(con);
			w.write(postdata);
			w.flush();

			if (cookie == null) {
				if (auto_cookie == COOKIE_AND_REPOST) {
					cookie = getResultSet(con, POST).cookie;
					return post(postdata, bytes);

				} else if (auto_cookie == COOKIE_IN) {
					cookie = getResultSet(con, POST).cookie;
				}
			}
		} catch (IOException e) {
			throw new UnkoException(e, getPath("https") + "に接続できませんでした。");
		}

		return con;
	}

	/**
	 * スレ建て用Postメソッド
	 * @param subject	スレタイ
	 * @param from		名前欄
	 * @param mail		メ欄
	 * @param msg		本文
	 * @return
	 * @throws MankoException
	 * @throws UnkoException
	 */
	public HttpsURLConnection postThread(String subject, String from, String mail, String msg)	throws MankoException, UnkoException {
		subject = convertNyoro(subject, "Shift-JIS");

		String postdata = createThreadPostdata(subject, from, mail, msg);
		int bytes = getBytes(postdata, request_charset);

		return post(postdata, bytes);
	}

	/**
	 * レス書込用Postメソッド
	 * @param key		スレ番
	 * @param from		名前欄
	 * @param mail		メ欄
	 * @param msg		本文
	 * @return
	 * @throws MankoException
	 * @throws UnkoException
	 */
	public HttpsURLConnection postResponse(String key, String from, String mail, String msg)
			throws MankoException, UnkoException {
		String postdata = createResponsePostdata(key, from, mail, msg);
		int bytes = getBytes(postdata, request_charset);

		return post(postdata, bytes);
	}

	/**
	 * お絵かき付きレス書込用Postメソッド
	 * @param key		スレ番
	 * @param from		名前欄
	 * @param mail		メ欄
	 * @param msg		本文
	 * @param oekaki	お絵かきFileインスタンス
	 * @return
	 * @throws MankoException
	 * @throws UnkoException
	 */
	public HttpsURLConnection postResponse(String key, String from, String mail, String msg, File oekaki)
			throws MankoException, UnkoException {
		String postdata = createResponsePostdata(key, from, mail, msg, oekaki);
		int bytes = getBytes(postdata, request_charset);

		return post(postdata, bytes);
	}

	/**
	 * スレ建て用ポストデータ構築メソッド
	 * @param subject
	 * @param from
	 * @param mail
	 * @param msg
	 * @return
	 * @throws MankoException
	 */
	public String createThreadPostdata(String subject, String from, String mail, String msg) throws MankoException {
		String postdata = "";

		try {
			postdata += "bbs=" + bbs;
			postdata += "&subject=" + StringEncoder.convert(subject, request_charset);
			postdata += "&time=1";
			postdata += "&submit=" + StringEncoder.convert(Form1, request_charset);
			postdata += "&FROM=" + StringEncoder.convert(from, request_charset);
			postdata += "&mail=" + StringEncoder.convert(mail, request_charset);
			postdata += "&MESSAGE=" + StringEncoder.convert(msg, request_charset);
			if (ronin_auth)
				postdata += "&sid=" + ronin_sid;
			postdata += "&suka=pontan";

		} catch (IOException e) {
			throw new MankoException(e, "StringEncoder.convert()でエラーが発生しました。");
		}

		return postdata;
	}

	/**
	 * 書込用ポストデータ構築メソッド
	 * @param key
	 * @param from
	 * @param mail
	 * @param msg
	 * @return
	 * @throws MankoException
	 */
	public String createResponsePostdata(String key, String from, String mail, String msg) throws MankoException {
		String postdata = "";

		try {
			postdata += "bbs=" + bbs;
			postdata += "&key=" + key;
			postdata += "&time=" + 1;
			postdata += "&submit=" + StringEncoder.convert(Form2, request_charset);
			postdata += "&FROM=" + StringEncoder.convert(from, request_charset);
			postdata += "&mail=" + StringEncoder.convert(mail, request_charset);
			postdata += "&MESSAGE=" + StringEncoder.convert(msg, request_charset);
			if (ronin_auth)
				postdata += "&sid=" + ronin_sid;
			postdata += "&suka=pontan";
		} catch (IOException e) {
			throw new MankoException(e, "StringEncoder.convert()でエラーが発生しました。");
		}

		return postdata;
	}

	/**
	 * お絵かき付き書込用ポストデータ構築メソッド
	 * @param key
	 * @param from
	 * @param mail
	 * @param msg
	 * @param oekaki
	 * @return
	 * @throws MankoException
	 */
	public String createResponsePostdata(String key, String from, String mail, String msg, File oekaki)
			throws MankoException {
		String postdata = "";

		try {
			postdata += "FROM=" + StringEncoder.convert(from, request_charset);
			postdata += "&mail=" + StringEncoder.convert(mail, request_charset);
			postdata += "&MESSAGE=" + StringEncoder.convert(msg, request_charset);
			postdata += "&bbs=" + bbs;
			postdata += "&time=1";
			postdata += "&key=" + key;
			if (ronin_auth)
				postdata += "&sid=" + ronin_sid;
			postdata += "&oekaki=" + StringEncoder.convert(IMAGE_DATA + "," + getOekaki(oekaki), request_charset);
		} catch (IOException e) {
			throw new MankoException(e, "StringEncoder.convert()でエラーが発生しました。");
		}

		return postdata;
	}

	/**
	 * hobo値計算メソッド
	 * @param key
	 * @return
	 */
	private String hobo(String key) {
		String value = "";

		if ((value = hobo.get(key)) == null) {
			String msg = "/v1/" + host + "/" + bbs + "/" + key + api_sid + appkey;
			value = hmac(hmkey, msg, ALGO);

			hobo.put(key, value);
		}
		return value;
	}

	/**
	 * お絵かきFileインスタンスをBase64文字列に変換するメソッド
	 * @param file	お絵かきFileインスタンス
	 * @return	Base64文字列
	 */
	private String getOekaki(File file) {
		byte[] bImage = null;

		try {
			BufferedImage image = ImageIO.read(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(baos);

			image.flush();

			String name = file.getName();
			ImageIO.write(image, name.substring(name.lastIndexOf(".") + 1, name.length()), bos);

			bos.flush();
			bos.close();

			bImage = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Base64.getEncoder().encodeToString(bImage);
	}

	/**
	 *
	 * @param con
	 * @param type
	 * @return
	 * @throws UnkoException
	 */
	public ResultSet getResultSet(HttpURLConnection con, String type) throws UnkoException {
		ResultSet result;

		try {
			result = new ResultSet();
			result.setAnal(anal.getClass().newInstance());
			result.setType(type);
			result.set(con, response_charset);
		} catch (Exception e) {
			throw new UnkoException(e, "ResultSetを準備出来ませんでした。レスポンスが無かったかも知れません。");
		}

		return result;
	}

	/**
	 * スレ勢い取得メソッド<br>
	 * 引数に与えられたスレ番keyの勢いを計算します。
	 *
	 * @param key スレ番10桁
	 * @return 勢いfloat値
	 * @throws UnkoException 計算不能(dat落ち等)
	 */
	public float getSpeed(String key) throws UnkoException {
		return getSpeed(key, getSubject());
	}

	/**
	 * スレ勢い取得メソッド<br>
	 * 引数に与えられたスレ番keyの勢いを、第二引数に与えられたスレ一覧から計算します。
	 * @param key スレ番10桁
	 * @return 勢いfloat値
	 */
	public float getSpeed(String key, List<String> subject) {
		return getSpeeds(subject, -1).get(key);
	}

	/**
	 * 全スレの勢いを計算します。<br>
	 * 内部でgetSubject()を呼び出しています。
	 *
	 * @throws UnkoException	getSubject()失敗時の例外
	 * @return 各勢いを格納したList
	 */
	public List<Float> getSpeeds() throws UnkoException {
		ResultSet subject = getSubject();
		return getSpeeds(subject);
	}

	/**
	 * 与えられたスレ一覧の勢いを計算します。<br>
	 * 過去のスレ一覧からも勢い計算出来るはず。<br>
	 * @param subject getSubject()取得結果
	 * @return 各勢いを格納したList
	 */
	public List<Float> getSpeeds(List<String> subject) {
		OriginMap<String, Float> map = new OriginMap<>((LinkedHashMap<String, Float>) getSpeeds(subject, -1));
		List<Float> list = new ArrayList<>();
		for (int i = 0; i < map.size(); i++) {
			list.add(map.getValue(i));
		}
		return list;
	}

	/**
	 * 板の最大スレ数を考慮して全スレの勢いを計算します。<br>
	 * スレ立て直後の86400を基準値とし、スレ数が最大スレ数を超えた数だけ基準値が86400*{1/(2*n)}と変化します。<br>
	 * @param subject getSubject()取得結果
	 * @param thread_max 板で設定されている最大スレ数
	 * @return スレ番をkeyに各勢いを格納したmap
	 */
	public Map<String, Float> getSpeeds(List<String> subject, int thread_max) {
		long time = System.currentTimeMillis() / 1000 - 2;

		Map<String, Float> buf = new LinkedHashMap<>();
		int base = 1 + ((thread_max > 0) ? subject.size() / thread_max : 0);
		int def = 86400 / base;
		for (String s : subject) {
			long key = Long.parseLong(s.substring(0, 10));
			int res = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf(")")));
			float ans = (float) res / (time - key) * def;
			buf.put(key + "", (ans < 0) ? 0 : ans);
		}
		return buf;
	}

	/**
	 * URLConnectionからBufferedReaderインスタンスをOpenするメソッド
	 * エンコーディング形式はrespnse_charset
	 *
	 * @param con			URLConnectionインスタンス
	 * @return				BufferedReaderインスタンス
	 * @throws IOException	例外
	 */
	public BufferedReader getBufferedReader(URLConnection con) throws IOException {
		return getBufferedReader(con, response_charset);
	}

	/**
	 * URLConnectionからBufferedWriterインスタンスをOpenするメソッド
	 * エンコーディング形式はrequest_charset
	 *
	 * @param con			URLConnectionインスタンス
	 * @return				BufferedWriterインスタンス
	 * @throws IOException	例外
	 */
	public BufferedWriter getBufferedWriter(URLConnection con) throws IOException {
		return getBufferedWriter(con, request_charset);
	}

	/**
	 * dataのByteサイズを取得するメソッド<br>
	 * エンコーディング形式はrequest_charset
	 *
	 * @param data ポストデータ
	 * @return		Byteサイズ
	 */
	public int getBytes(String data) {
		return getBytes(data, request_charset);
	}

	/**
	 * URLConnectionからBufferedReaderインスタンスをOpenするメソッド
	 *
	 * @param con			URLConnectionインスタンス
	 * @param charset		エンコーディング形式
	 * @return				BufferedReaderインスタンス
	 * @throws IOException	例外
	 */
	public static BufferedReader getBufferedReader(URLConnection con, String charset) throws IOException {
		return new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName(charset)));
	}

	/**
	 * URLConnectionからBufferedWriterインスタンスをOpenするメソッド
	 *
	 * @param con			URLConnectionインスタンス
	 * @param charset		エンコーディング形式
	 * @return				BufferedWriterインスタンス
	 * @throws IOException	例外
	 */
	public static BufferedWriter getBufferedWriter(URLConnection con, String charset) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), Charset.forName(charset)));
	}

	/**
	 * dataのByteサイズを取得する静的メソッド
	 * @param data		ポストデータ
	 * @param charset	エンコーディング形式
	 * @return			サイズ
	 */
	public static int getBytes(String data, String charset) {
		return data.getBytes(Charset.forName(charset)).length;
	}

	/**
	 * 今までどおりのsetUserAgent()<br>
	 * 内部ではsetPostUA()を呼んでいる。
	 *
	 * @param ua 書込用ユーザエージェント
	 */
	public void setUserAgent(String ua) {
		setPostUA(ua);
	}


	/**
	 * ニョロ文字変換メソッド<br>
	 * charsetでShift-JISを指定した場合、msgをShift-JIS文字列としてUTF-8用に一部の文字を変換します。<br>
	 * 逆にcharsetでUTF-8を指定した場合、一部の文字をShift-JIS用に変換します。<br>
	 * 例えばtoNyoro("ぬほほ～", "UTF-8");とした場合、「～」は「〜」に置き換えられて返却されます。<br>
	 *
	 * @param msg		変換元メッセージ
	 * @param charset	変換元の文字コード
	 * @throws UnkoException
	 */
	public static String convertNyoro(String msg, String charset) throws UnkoException {
		try {
			Map<String, String> conversion = createConversionMap(charset);

			char oldChar;
			char newChar;
			String key;

			for (Iterator<String> itr = conversion.keySet().iterator(); itr.hasNext();) {
				key = itr.next();
				oldChar = toChar(key);
				newChar = toChar(conversion.get(key));
				msg = msg.replace(oldChar, newChar);
			}

			return msg;
		}catch(Exception e) {
			throw new UnkoException(e, "変換できませんでした。");
		}
	}

	private static Map<String, String> createConversionMap(String charset) throws UnsupportedEncodingException {
		Map<String, String> conversion = new HashMap<String, String>();

		if (charset.equals("UTF-8") || charset.equals("UTF8") || charset.equals("utf-8") || charset.equals("utf8")) {
			conversion.put("U+2212", "U+FF0D");
			conversion.put("U+301C", "U+FF5E");
			conversion.put("U+00A2", "U+FFE0");
			conversion.put("U+00A3", "U+FFE1");
			conversion.put("U+00AC", "U+FFE2");
			conversion.put("U+2014", "U+2015");
			conversion.put("U+2016", "U+2225");

		} else if(charset.equals("Shift-JIS") || charset.equals("Sjis") || charset.equals("shift-jis") || charset.equals("sjis")){
			conversion.put("U+FF0D", "U+2212");
			conversion.put("U+FF5E", "U+301C");
			conversion.put("U+FFE0", "U+00A2");
			conversion.put("U+FFE1", "U+00A3");
			conversion.put("U+FFE2", "U+00AC");
			conversion.put("U+2015", "U+2014");
			conversion.put("U+2225", "U+2016");
		}

		return conversion;
	}

    private static char toChar(String value) {
        return (char)Integer.parseInt(value.trim().substring("U+".length()), 16);
    }
}