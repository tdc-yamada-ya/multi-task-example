package jp.co.tdc.multi_task_sample;

/**
 * 渡されたチャンクデータを処理するタスククラスです。
 *
 * @param <T> チャンクデータの型
 */
public interface Task<T> {
	/**
	 * チャンクデータを処理します。
	 * @param chunk チャンクデータ
	 */
	void execute(Chunk<T> chunk) throws Exception;
}
