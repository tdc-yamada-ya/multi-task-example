package jp.co.tdc.multi_task_sample;

/**
 * チャンクデータの読み込み中およびタスク実行中に発生した例外のハンドリングを行うインタフェースです。
 */
public interface ExceptionHandler {
	/**
	 * 例外のハンドリングを行います。
	 * @param e 例外
	 */
	void handleException(Throwable e);
}
