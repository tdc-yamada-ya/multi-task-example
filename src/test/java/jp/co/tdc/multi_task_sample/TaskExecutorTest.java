package jp.co.tdc.multi_task_sample;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Test;

/**
 * {@link TaskExecutor} のテストクラス
 */
public class TaskExecutorTest {
	@Test
	public void test() throws Exception {
		TaskExecutor<String> taskExecutor;

		taskExecutor = new TaskExecutor<String>();
		taskExecutor.setExecutingRate(300);
		taskExecutor.setExecutingRateSpanInMillis(1000);
		taskExecutor.setTimeoutInMillis(50000);
		taskExecutor.setChunkLoader(new TextChunkLoader());
		taskExecutor.setTaskFactory(new TextTaskFactory());
		assertEquals(taskExecutor.getExecutingRate(), 300);
		assertEquals(taskExecutor.getExecutingRateSpanInMillis(), 1000);
		assertEquals(taskExecutor.getTimeoutInMillis(), 50000);
		taskExecutor.execute();

		taskExecutor = new TaskExecutor<String>();
		taskExecutor.setExecutingRate(300);
		taskExecutor.setExecutingRateSpanInMillis(1000);
		taskExecutor.setTimeoutInMillis(50000);
		taskExecutor.setChunkLoader(new ExceptionChunkLoader());
		taskExecutor.setTaskFactory(new TextTaskFactory());
		taskExecutor.setExceptionHandler(new ExceptionHandler() {
			public void handleException(Throwable e) {
				System.out.println(e);
			}
		});
		taskExecutor.execute();
	}

	class TextChunkLoader implements ChunkLoader<String> {
		private BufferedReader reader;

		public TextChunkLoader() {
			reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/data.txt")));
		}

		public Chunk<String> nextChunk() throws Exception {
			String line = reader.readLine();

			if (line == null) {
				return null;
			}

			return new Chunk<String>(line);
		}

		public void close() throws Exception {
			reader.close();
		}
	}

	class ExceptionChunkLoader implements ChunkLoader<String> {
		public Chunk<String> nextChunk() throws Exception {
			throw new Exception();
		}

		public void close() throws Exception {
		}
	}

	class TextTask implements Task<String> {
		public void execute(Chunk<String> chunk) throws Exception {
			System.out.println(Thread.currentThread().getName() + ": " + chunk.getData());
		}
	}

	class ExceptionTask implements Task<String> {
		public void execute(Chunk<String> chunk) throws Exception {
			throw new Exception();
		}
	}

	class TextTaskFactory implements TaskFactory<String> {
		public Task<String> createTask() {
			return new TextTask();
		}
	}

	class ExceptionTaskFactory implements TaskFactory<String> {
		public Task<String> createTask() throws Exception {
			return new ExceptionTask();
		}
	}
}
