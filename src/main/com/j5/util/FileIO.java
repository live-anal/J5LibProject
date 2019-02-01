package main.com.j5.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ファイルを読書きするクラス
 * DAT作成に使って良し、ログ出力に使って良し。
 */
public class FileIO {
	private File file;
	private int maxline;
	private List<FileWriteListener> listen;

	/**
	 * デフォルトコンストラクタ
	 * @param filepath 読書きするファイルパス
	 */
	public FileIO(String filepath) {
		this(new File(filepath));
	}

	/**
	 * デフォルトコンストラクタ
	 * @param file 読書きするファイル
	 */
	public FileIO(File file) {
		this.file = file;
		open();
		listen = new ArrayList<>();
	}

	/**
	 * リスナー追加メソッド
	 * ファイル書込み時に呼び出すリスナの設定
	 *
	 * @param lis FileWriteListenerインスタンス
	 */
	public void addListener(FileWriteListener lis){
		listen.add(lis);
	}

	/**
	 * Fileインスタンスゲッター
	 * @return
	 */
	public File getFile(){
		return file;
	}

	/**
	 * ファイルオープンメソッド
	 * ファイル・ディレクトリが存在しない場合は自動生成
	 *
	 * @return ファイルが開けたか否か
	 */
	private boolean open() {
		boolean f = false;
		maxline = 1;

		try {
			if (!file.exists()) {
				File dir = new File(file.getParent());
				if(dir.exists()) {
					dir.mkdirs();
				}

				file.createNewFile();
			} else {
				checkLine();
			}

			f = true;
		} catch (IOException e) {
			e.printStackTrace();
			f = false;
		}

		return f;
	}

	/**
	 * 行数確認メソッド
	 */
	private void checkLine(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			while(br.readLine() != null){
				updateMaxLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 行数加算メソッド
	 */
	synchronized protected void updateMaxLine(){
		updateMaxLine(1);
	}

	/**
	 * 行数加算メソッド
	 * @param n	maxline += 「n」;
	 */
	synchronized protected void updateMaxLine(int n){
		maxline += n;
	}

	/**
	 * 追記確認メソッド
	 * @return 追記有無
	 */
	public boolean checkNewText(){
		boolean f=false;

		if(this.file != null){
			String log = read();
			if(log != null && !log.equals("")){
				f=true;
			}
		}

		return f;
	}

	/**
	 * 行数取得メソッド
	 * @return 行数
	 */
	public int getLineSize(){
		return maxline;
	}

	/**
	 * ファイルサイズ確認メソッド
	 * @return file.length();
	 */
	public long size(){
		return file.length();
	}

	/**
	 * 最終行取得メソッド
	 * @return 最終行
	 */
	synchronized public String read(){
		return read(getLineSize());
	}

	/**
	 * 指定行取得メソッド
	 * @param line 取得したい行
	 * @return 読み込んだ行
	 */
	synchronized public String read(int line){
		String buf = null;
		int i = 0;

		try{
			BufferedReader br = new BufferedReader(new FileReader(file));

			while(i<line-1){
				br.readLine();
				i++;
			}

			buf = br.readLine();

			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		if(buf == null)
			buf = "";

		return buf;
	}

	/**
	 * 全行取得メソッド
	 * @return	全行を保持したListインスタンス
	 */
	synchronized public List<String> readAll(){
		LinkedList<String> buf = new LinkedList<>();

		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str;
			while((str = br.readLine()) != null){
				buf.add(str);
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		return buf;
	}

	/**
	 * 追記メソッド
	 * @param str 書込み内容
	 */
	synchronized public void write(String str){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file,true));
			bw.write(str);
			bw.flush();
			bw.close();

			int n = (str.length()-str.replaceAll("\r\n", "").length())/2;
			updateMaxLine(n);

			for(FileWriteListener l:listen)
				l.writed(str);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 改行付き追記メソッド
	 * @param str 書込み内容
	 */
	synchronized public void writeln(String str){
		write(str+"\r\n");
	}

	/**
	 * ファイル内容クリアメソッド
	 * deleteからのcleateを行う。
	 */
	synchronized public void clear(){
		if(file.exists()){
			file.delete();
		}
		open();
	}
}
