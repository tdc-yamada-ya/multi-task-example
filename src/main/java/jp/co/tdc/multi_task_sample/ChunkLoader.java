package jp.co.tdc.multi_task_sample;

/**
 * タスクを実行するのに必要なチャンクデータを読み込むインタフェース
 *
 * @param <T> チャンクデータの型
 */
public interface ChunkLoader <T> {
	/**
	 * 次のチャンクデータを読み込む。
	 * 次のチャンクデータが存在しない場合は null を返す。
	 * @return チャンクデータ
	 * @throws Exception 読み込み中に例外が発生した場合にスローします。
	 */
	Chunk<T> nextChunk() throws Exception;

	/**
	 * もし後処理が必要であれば後処理を実行する。
	 * @throws Exception
	 * @throws Exception 後処理中に例外が発生した場合にスローします。
	 */
	void close() throws Exception;
}
