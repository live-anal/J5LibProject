package main.com.j5.connect;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Imgurアップロードクラス
 */
public class Imgur {
	private String target = "https://api.imgur.com/3/image";
	private String client_id;

	/**
	 * コンストラクタ<br>
	 * 登録したImgurAPIのクライアントIDを設定する事。
	 *
	 * @param client_id クライアントID
	 */
	public Imgur(String client_id) {
		this.client_id = client_id;
	}

	/**
	 * アップロードメソッド<br>
	 * Json形式の結果が返るので手っ取り早くURLを取得したいなら取得結果を{@link Imgur#getURL(String)}に与えて整形すること。
	 *
	 * @param path 画像パス
	 * @return		Json結果
	 * @throws IOException なんらかの例外
	 */
	public String upload(String path) throws IOException {
		return upload(new File(path));
	}

	/**
	 * アップロードメソッド<br>
	 * Json形式の結果が返るので手っ取り早くURLを取得したいなら取得結果を{@link Imgur#getURL(String)}に与えて整形すること。
	 *
	 * @param file 画像のFileインスタンス
	 * @return		Json結果
	 * @throws IOException なんらかの例外
	 */
	public String upload(File file) throws IOException {
		return upload(new FileInputStream(file));
	}

	/**
	 * アップロードメソッド<br>
	 * Json形式の結果が返るので手っ取り早くURLを取得したいなら取得結果を{@link Imgur#getURL(String)}に与えて整形すること。
	 *
	 * @param is	画像のInputStream
	 * @return		Json結果
	 * @throws IOException なんらかの例外
	 */
	public String upload(InputStream is) throws IOException {
		URL url = new URL(target);

		HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

		con.setDoInput(true);
		con.setDoOutput(true);

		con.setRequestMethod("POST");
		con.addRequestProperty("Authorization", "Client-ID " + client_id);

		con.connect();

		DataOutputStream w = new DataOutputStream(con.getOutputStream());
		byte[] b = new byte[4096];
		int readByte = 0;
		while ((readByte = is.read(b)) != -1) {
			w.write(b, 0, readByte);
		}

		is.close();
		w.close();

		BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));

		String response = "";
		for (String line=r.readLine(); line!=null; line=r.readLine()) {
			response += line + "\n";
		}

		r.close();

		return response;
	}

	/**
	 * 取得したJson形式の文字列からURLを抜き出すメソッド
	 *
	 * @param json	upload()で取得した結果
	 * @return 画像URL
	 */
	public String getURL(String json) {
		json = json.substring(json.indexOf("\"link\":")+8);
		json = json.substring(0, json.indexOf("\""));

		return json.replaceAll("\\\\", "");
	}
}
