package main.com.j5.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LinkedHashMapクラスを継承した拡張マップクラス
 * ListみたいにKeyやValueを番号で取得出来るようにした物。
 *
 * @param <K>
 * @param <V>
 */
public class OriginMap<K,V> extends LinkedHashMap<K,V>{

	/**
	 * デフォルトコンストラクタ
	 */
	public OriginMap(){
		super();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param map LinkedHashMapインスタンス
	 */
	public OriginMap(LinkedHashMap<K,V> map){
		super(map);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param map Mapインスタンス
	 */
	public OriginMap(Map<K,V> map){
		super(map);
	}

	/**
	 * i番目のキーゲッター
	 * @param i 指定番号
	 * @return 対象のキー
	 */
	public K getKey(int i){
		K buf = null;
		int cnt=0;

		if(i>-1){
			for(K k:keySet()){
				buf = k;
				if(++cnt>i) break;
			}
		}

		return buf;
	}

	/**
	 * i番目の値ゲッター
	 * @param i 指定番号
	 * @return 対象の値
	 */
	public V getValue(int i){
		return get(getKey(i));
	}

	/**
	 * 普通のgetValue(key)
	 * @param key キー
	 * @return バリュー
	 */
	public V getValue(K key){
		return get(key);
	}
}