package jp.co.tdc.multi_task_sample;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * マルチスレッドで並列にタスクを実行するサービスです。
 * 指定した時間内に指定した回数以上タスクが実行されないように待機する機能も持ちます。
 *
 * @param <T> タスクの入力となるチャンクデータの型
 */
public class TaskExecutor <T> {
	/** デフォルトのスレッドプールサイズ */
	private static final int DEFAULT_THREAD_POOL_SIZE = 10;

	/** スレッドプールのサイズ */
	private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

	/** タスクの実行回数 */
	private int executeCount = 0;

	/** タスクの実行関数をカウント開始した基準時間 */
	private long baseTimeInMillis = 0;

	/** 設定時間あたりのタスク実行数 */
	private int executingRate = 0;

	/** タスク実行数を計測するミリ秒単位の設定時間 */
	private int executingRateSpanInMillis = 0;

	/** 全タスクの完了を待機する際のミリ秒単位のタイムアウト時間 */
	private long timeoutInMillis = 0;

	/** チャンクローダー */
	private ChunkLoader<T> chunkLoader;

	/** タスクファクトリ */
	private TaskFactory<T> taskFactory;

	/** 例外のハンドラ */
	private ExceptionHandler exceptionHandler;

	/**
	 * コンストラクタ
	 */
	public TaskExecutor() {
		setThreadPoolSize(DEFAULT_THREAD_POOL_SIZE);
	}

	/**
	 * タスクの実行を開始して全てのタスクが完了するまでブロックします。
	 */
	public void execute() {
		resetExecuteCount();

		// 設定されたサイズでスレッドプールを作成し、全スレッドを開始します。
		int threadPoolSize = getThreadPoolSize();
		ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

		for (int i = 0; i < threadPoolSize; i++) {
			Callable<?> task = new WrappedTask();

			executorService.submit(task);
		}

		// 全スレッドが完了するまで待機します。
		executorService.shutdown();

		try {
			executorService.awaitTermination(getTimeoutInMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * タスクの実行回数とタスクの測定開始の基準時間をリセットします。
	 */
	private void resetExecuteCount() {
		executeCount = 0;
		baseTimeInMillis = System.currentTimeMillis();
	}

	/**
	 * タスク実行の内部的なラッパーです。
	 */
	private class WrappedTask implements Callable<Void> {
		public Void call() throws Exception {
			while(true) {
				try {
					Chunk<T> chunk = chunkLoader.nextChunk();

					if (chunk == null) {
						break;
					}

					tryAwaitNext();

					Task<T> task = taskFactory.createTask();

					task.execute(chunk);
				} catch(Throwable e) {
					if (exceptionHandler != null) {
						exceptionHandler.handleException(e);
					}

					break;
				}
			}

			return null;
		}
	}

	/**
	 * タスクの実行回数の規定回数を超えた場合はスリープを実施します。
	 */
	private synchronized void tryAwaitNext() {
		if (executingRate > 0 && executeCount >= executingRate) {
			awaitNext();
			resetExecuteCount();
		}

		executeCount++;
	}

	/**
	 * タスクの実行回数を設定した回数に抑えるため、スリープします。
	 */
	private void awaitNext() {
		long sleep = executingRateSpanInMillis - (System.currentTimeMillis() - baseTimeInMillis);

		if (sleep <= 0) {
			return;
		}

		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * スレッドプールサイズを設定します。
	 * @param threadPoolSize スレッドプールサイズ
	 */
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	/**
	 * スレッドプールサイズを取得します。
	 * @return スレッドプールサイズ
	 */
	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	/**
	 * 設定時間あたりのタスク実行数を設定します。
	 * @param executingRate 設定時間あたりのタスク実行数
	 */
	public void setExecutingRate(int executingRate) {
		this.executingRate = executingRate;
	}

	/**
	 * 設定時間あたりのタスク実行数を取得します。
	 * @return 設定時間あたりのタスク実行数
	 */
	public int getExecutingRate() {
		return executingRate;
	}

	/**
	 * タスク実行数を計測するミリ秒単位の設定時間を設定します。
	 * @param executingRateSpanInMillis タスク実行数を計測するミリ秒単位の設定時間
	 */
	public void setExecutingRateSpanInMillis(int executingRateSpanInMillis) {
		this.executingRateSpanInMillis = executingRateSpanInMillis;
	}

	/**
	 * タスク実行数を計測するミリ秒単位の設定時間を取得します。
	 * @return タスク実行数を計測するミリ秒単位の設定時間
	 */
	public int getExecutingRateSpanInMillis() {
		return executingRateSpanInMillis;
	}

	/**
	 * 全タスクの完了を待機する際のミリ秒単位のタイムアウト時間を設定する。
	 * @param timeoutInMillis 全タスクの完了を待機する際のミリ秒単位のタイムアウト時間
	 */
	public void setTimeoutInMillis(long timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
	}

	/**
	 * 全タスクの完了を待機する際のミリ秒単位のタイムアウト時間を取得する。
	 * @return 全タスクの完了を待機する際のミリ秒単位のタイムアウト時間
	 */
	public long getTimeoutInMillis() {
		return timeoutInMillis;
	}

	/**
	 * タスクの入力となるチャンクデータのローダーを設定します。
	 * @param chunkLoader チャンクローダー
	 */
	public void setChunkLoader(ChunkLoader<T> chunkLoader) {
		this.chunkLoader = chunkLoader;
	}

	/**
	 * タスクの入力となるチャンクデータのローダーを取得します。
	 * @return チャンクローダー
	 */
	public ChunkLoader<T> getChunkLoader() {
		return chunkLoader;
	}

	/**
	 * タスクファクトリーを設定します。
	 * @param taskFactory タスクファクトリー
	 */
	public void setTaskFactory(TaskFactory<T> taskFactory) {
		this.taskFactory = taskFactory;
	}

	/**
	 * タスクファクトリーを取得します。
	 * @return タスクファクトリー
	 */
	public TaskFactory<T> getTaskFactory() {
		return taskFactory;
	}

	/**
	 * 例外のハンドラを設定します。
	 * @param exceptionHandler 例外のハンドラ
	 */
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * 例外のハンドラを取得します。
	 * @return 例外のハンドラ
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}
}
