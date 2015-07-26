package com.dianping.cat.report.page.database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseGroup {
	public static Map<String, List<String>> KEY_GROUPS = new HashMap<String, List<String>>() {
		private static final long serialVersionUID = 1L;

		{
			put("CPU", Arrays.asList("Load", "user", "sys", "wa", "idle"));
			put("Memory", Arrays.asList("used", "free", "swapTotal", "swapUsed", "swapFree"));
			put("DiskAndIO", Arrays.asList("diskAvail", "diskUsedRatio", "network_in", "network_out", "io_reads",
			      "io_writes", "iops", "io_util"));
			put("TransactionAndThread", Arrays.asList("Aborted_clients", "Aborted_connects", "Threadpool_idle_threads",
			      "Threadpool_threads", "Table_open_cache_hits", "Table_open_cache_misses", "Table_open_cache_overflows",
			      "REPDELAY", "RESPONSE_TIME", "COM_DELETE", "COM_INSERT", "COM_UPDATE", "COM_SELECT",
			      "CREATED_TMP_DISK_TABLES", "CREATED_TMP_TABLES", "QUESTIONS", "THREADS_RUNNING", "THREADS_CONNECTED",
			      "TPS"));
			put("InnoDB Info", Arrays.asList("Innodb_buffer_pool_pages_dirty", "Innodb_buffer_pool_pages_flushed",
			      "Innodb_buffer_pool_pages_LRU_flushed", "Innodb_buffer_pool_pages_free",
			      "Innodb_buffer_pool_pages_made_not_young", "Innodb_buffer_pool_pages_made_young",
			      "Innodb_buffer_pool_pages_misc", "Innodb_buffer_pool_pages_old", "Innodb_buffer_pool_pages_total",
			      "Innodb_buffer_pool_read_ahead_rnd", "Innodb_buffer_pool_read_ahead",
			      "Innodb_buffer_pool_read_ahead_evicted", "Innodb_buffer_pool_read_requests", "Innodb_buffer_pool_reads",
			      "Innodb_buffer_pool_write_requests", "Innodb_checkpoint_age", "Innodb_checkpoint_max_age",
			      "Innodb_data_fsyncs", "Innodb_data_pending_fsyncs", "Innodb_data_pending_reads",
			      "Innodb_data_pending_writes", "Innodb_history_list_length", "Innodb_ibuf_free_list",
			      "Innodb_ibuf_merged_delete_marks", "Innodb_ibuf_merged_deletes", "Innodb_ibuf_merged_inserts",
			      "Innodb_ibuf_merges", "Innodb_log_write_requests", "Innodb_log_writes", "Innodb_os_log_fsyncs",
			      "Innodb_os_log_pending_fsyncs", "Innodb_os_log_pending_writes", "Innodb_os_log_written",
			      "Innodb_pages_read", "Innodb_pages_written", "Innodb_available_undo_logs", "INNODB_DATA_READS",
			      "INNODB_DATA_WRITES", "INNODB_ROWS_DELETED", "INNODB_ROWS_INSERTED", "INNODB_ROWS_UPDATED"));
			put("LockAndWait", Arrays.asList("Innodb_deadlocks", "Innodb_mutex_os_waits", "Innodb_mutex_spin_rounds",
			      "Innodb_mutex_spin_waits", "Innodb_row_lock_current_waits", "Innodb_current_row_locks",
			      "Innodb_row_lock_time", "Innodb_row_lock_time_avg", "Innodb_row_lock_time_max", "Innodb_row_lock_waits",
			      "Innodb_s_lock_os_waits", "Innodb_s_lock_spin_rounds", "Innodb_s_lock_spin_waits",
			      "Innodb_x_lock_os_waits", "Innodb_x_lock_spin_rounds", "Innodb_x_lock_spin_waits",
			      "Table_locks_immediate", "Table_locks_waited", "Innodb_buffer_pool_wait_free", "Innodb_log_waits"));
		}
	};
}
