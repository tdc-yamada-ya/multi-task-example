package jp.co.tdc.multi_task_sample;

/**
 * タスクの入力となるチャンクデータ
 *
 * @param <T> チャンクデータの型
 */
public class Chunk <T> {
	private T data;

	/**
	 * 引数なしで初期化するコンストラクタです。
	 */
	public Chunk() {
	}

	/**
	 * チャンクデータを指定して初期化するコンストラクタです。
	 * @param data チャンクデータ
	 */
	public Chunk(T data) {
		this.data = data;
	}

	/**
	 * チャンクデータ
	 * @param data チャンクデータ
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * チャンクデータ
	 * @return チャンクデータ
	 */
	public T getData() {
		return data;
	}
}
