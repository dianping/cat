package com.dianping.cat.report.page.database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseGroup {
	public static Map<String, List<String>> KEY_GROUPS = new HashMap<String, List<String>>() {
		private static final long serialVersionUID = 1L;

		{
			put("CPU", Arrays.asList("load", "usr", "sys", "wa", "idle"));
			put("Memory", Arrays.asList("used", "free", "swapTotal", "swapUsed", "swapFree"));
			put("DiskAndIO", Arrays.asList("diskAvail", "diskUsedRatio", "network_in", "network_out", "io_reads",
			      "io_writes", "iops", "io_util"));
			put("TransactionAndThread", Arrays.asList("aborted_clients", "aborted_connects", "thd_idle_thds", "thd_thds",
			      "tb_open_cache_hits", "tb_open_cache_miss", "tb_open_cache_overs", "delay", "response_time",
			      "com_delete", "com_insert", "com_update", "com_select", "com_kill", "com_create_index",
			      "com_drop_table", "com_drop_index", "com_alter_table", "cre_tmp_disk_tabs", "cre_tmp_tabs", "questions",
			      "thds_run", "thds_conn", "tps"));
			put("InnoDB Info", Arrays.asList("inn_bp_pgs_dirty", "inn_bp_pgs_flu", "inn_bp_pgs_lru_flu",
			      "inn_bp_pgs_free", "inn_bp_pgs_md_noty", "inn_bp_pgs_made_y", "inn_bp_pgs_misc", "inn_bp_pgs_old",
			      "inn_bp_pgs_tot", "inn_bp_read_ah_rnd", "inn_bp_read_ah", "inn_bp_read_ah_evi", "inn_bp_read_req",
			      "inn_bp_reads", "inn_bp_wri_req", "inn_ckp_age", "inn_ckp_max_age", "modified_age", "inn_data_fsyncs",
			      "inn_data_pfsyncs", "inn_data_preads", "inn_data_pwrites", "inn_his_list_len", "inn_ibuf_free_list",
			      "inn_ibuf_mer_del_mks", "inn_ibuf_mer_dels", "inn_ibuf_mer_ins", "inn_ibuf_mer", "inn_pgs_cre",
			      "inn_log_write_req", "inn_log_writes", "inn_os_log_fsyncs", "inn_os_log_pfsyncs", "inn_os_log_pwrites",
			      "inn_os_log_written", "inn_pread", "inn_pwritten", "inn_ava_ulogs", "inn_data_reads", "inn_data_writes",
			      "inn_data_written", "inn_rows_del", "inn_rows_inserted", "inn_rows_updated", "slow_queries",
			      "sort_merge_pas", "sort_range", "sort_rows", "sort_scan", "created_tmp_files", "queries",
			      "handler_commit", "handler_rollback", "bytes_received", "bytes_sent"));
			put("LockAndWait", Arrays.asList("inn_deadlocks", "inn_mutex_os_waits", "inn_mutex_spin_rounds",
			      "inn_mutex_spin_waits", "inn_row_lk_curr_waits", "inn_curr_row_lks", "inn_row_lk_time",
			      "inn_row_lk_time_avg", "inn_row_lk_time_max", "inn_row_lk_waits", "inn_s_lk_os_waits",
			      "inn_s_lk_spin_rounds", "inn_s_lk_spin_waits", "inn_x_lk_os_waits", "inn_x_lk_spin_rounds",
			      "inn_x_lk_spin_waits", "tb_lks_immediate", "tb_lks_waited", "inn_bp_wait_free", "inn_log_waits"));
		}
	};
}
