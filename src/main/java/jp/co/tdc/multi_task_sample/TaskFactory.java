package jp.co.tdc.multi_task_sample;

/**
 * タスクを生成するファクトリクラスです。
 *
 * @param <T> チャンクデータの型
 */
public interface TaskFactory <T> {
	/**
	 * タスクを生成します。
	 * @return タスク
	 */
	Task<T> createTask() throws Exception;
}
