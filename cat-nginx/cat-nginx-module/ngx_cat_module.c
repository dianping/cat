

/*
 * Copyright (C) peng hu
 * Copyright (C) DianPing, Inc.
 */


#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_http.h>
#include <ngx_event.h>

#include <pthread.h>

#define CATPREFIX "X-CAT-"
#define CAT_ROOT_ID "X-CAT-ROOT-ID"
#define CAT_PARENT_ID "X-CAT-PARENT-ID"
#define CAT_ID "X-CAT-ID"
#define VARNISH_AGE "Age"

#define BUFSIZE 1000 
#define SENDOUTBUFSIZE 800
#define DEFAULT_MMAP_DAT_SIZE 1024*1024*1024
#define DEFAULT_MMAP_DAT "/data/appdatas/cat/mmap.dat"
#define DEFAULT_MMAP_IDX "/data/appdatas/cat/mmap.idx"

#define TAB "\t"
#define NEWLINE "\n"
#define MODULE_NAME "Nginx"


typedef struct {
	ngx_flag_t          enable;
} ngx_http_cat_t;

typedef struct {
	ngx_flag_t       enable;
	ngx_uint_t       port;
	ngx_socket_t     fd;
	ngx_uint_t       mmap_dat_size;
	ngx_str_t        mmap_idx;
	ngx_str_t 	 mmap_dat;
} ngx_proc_send_conf_t;

static ngx_int_t ngx_cat_filter_init(ngx_conf_t *cf);
static ngx_int_t ngx_cat_body_filter(ngx_http_request_t *r, ngx_chain_t *chain);
static ngx_int_t ngx_cat_header_filter(ngx_http_request_t *r);
static void * ngx_cat_create_conf(ngx_conf_t *cf);
static char * ngx_cat_merge_conf(ngx_conf_t *cf, void *parent, void *child);
static char * ngx_http_cat(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);

static void *ngx_proc_send_create_conf(ngx_conf_t *cf);
static char *ngx_proc_send_merge_conf(ngx_conf_t *cf, void *parent,
		void *child);
static ngx_int_t ngx_proc_send_prepare(ngx_cycle_t *cycle);
static ngx_int_t ngx_proc_send_process_init(ngx_cycle_t *cycle);
static ngx_int_t ngx_proc_send_loop(ngx_cycle_t *cycle);
static void ngx_proc_send_exit_process(ngx_cycle_t *cycle);
static void ngx_proc_send_accept(ngx_event_t *ev);
int createdir(const char *pathname, ngx_cycle_t *cycle);
void* send_info(void* arg);
int open_fifo(ngx_cycle_t *cycle);
void* recv_info();

int pipefd[2]; 
unsigned long start_upstream_sec = 0;
unsigned long start_upstream_msec = 0;
unsigned long response_start_msec = 0;
unsigned int send_times = 0;

char buf[BUFSIZE][SENDOUTBUFSIZE + 1];
int start = 0;
int end = 0;
int filenum = 0;
long* read_write_mmap = NULL; 
char* write_file = NULL; 
int cat_started = 0;

static ngx_command_t  ngx_cat_filter_commands[] = {

	{ ngx_string("Cat"),
		NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
			|NGX_CONF_TAKE1,
		ngx_http_cat,
		NGX_HTTP_LOC_CONF_OFFSET,
		0,
		NULL },

	ngx_null_command
};

static ngx_command_t ngx_proc_send_commands[] = {

	{ ngx_string("listen"),
		NGX_PROC_CONF|NGX_CONF_TAKE1,
		ngx_conf_set_num_slot,
		NGX_PROC_CONF_OFFSET,
		offsetof(ngx_proc_send_conf_t, port),
		NULL },

	{ ngx_string("mmap"),
		NGX_PROC_CONF|NGX_CONF_FLAG,
		ngx_conf_set_flag_slot,
		NGX_PROC_CONF_OFFSET,
		offsetof(ngx_proc_send_conf_t, enable),
		NULL },

	{ ngx_string("mmap_dat_size"),
		NGX_PROC_CONF|NGX_CONF_TAKE1,
		ngx_conf_set_num_slot,
		NGX_PROC_CONF_OFFSET,
		offsetof(ngx_proc_send_conf_t, mmap_dat_size),
		NULL },

	{ ngx_string("mmap_dat"),
		NGX_PROC_CONF|NGX_CONF_TAKE1,
		ngx_conf_set_str_slot,
		NGX_PROC_CONF_OFFSET,
		offsetof(ngx_proc_send_conf_t, mmap_dat),
		NULL },

	{ ngx_string("mmap_idx"),
		NGX_PROC_CONF|NGX_CONF_TAKE1,
		ngx_conf_set_str_slot,
		NGX_PROC_CONF_OFFSET,
		offsetof(ngx_proc_send_conf_t, mmap_idx),
		NULL },


	ngx_null_command
};

static ngx_http_module_t  ngx_cat_filter_module_ctx = {
	NULL,           /* preconfiguration */
	ngx_cat_filter_init,             /* postconfiguration */

	NULL,                                  /* create main configuration */
	NULL,                                  /* init main configuration */

	NULL,                                  /* create server configuration */
	NULL,                                  /* merge server configuration */

	ngx_cat_create_conf,             /* create location configuration */
	ngx_cat_merge_conf               /* merge location configuration */
};


ngx_module_t  ngx_cat_filter_module = {
	NGX_MODULE_V1,
	&ngx_cat_filter_module_ctx,      /* module context */
	ngx_cat_filter_commands,         /* module directives */
	NGX_HTTP_MODULE,                       /* module type */
	NULL,                                  /* init master */
	NULL,                                  /* init module */
	NULL,                                  /* init process */
	NULL,                                  /* init thread */
	NULL,                                  /* exit thread */
	NULL,                                  /* exit process */
	NULL,                                  /* exit master */
	NGX_MODULE_V1_PADDING
};


static ngx_http_output_body_filter_pt  ngx_http_next_body_filter;
static ngx_http_output_header_filter_pt  ngx_http_next_header_filter;

static ngx_proc_module_t ngx_proc_send_module_ctx = {
	ngx_string("send"),
	NULL,
	NULL,
	ngx_proc_send_create_conf,
	ngx_proc_send_merge_conf,
	ngx_proc_send_prepare,
	ngx_proc_send_process_init,
	ngx_proc_send_loop,
	ngx_proc_send_exit_process
};


ngx_module_t ngx_proc_send_module = {
	NGX_MODULE_V1,
	&ngx_proc_send_module_ctx,
	ngx_proc_send_commands,
	NGX_PROC_MODULE,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NGX_MODULE_V1_PADDING
};

	static ngx_int_t
ngx_cat_filter_init(ngx_conf_t *cf)
{
	ngx_http_next_body_filter = ngx_http_top_body_filter;
	ngx_http_top_body_filter = ngx_cat_body_filter;

	ngx_http_next_header_filter = ngx_http_top_header_filter;
	ngx_http_top_header_filter = ngx_cat_header_filter;

	return NGX_OK;
}

static ngx_int_t ngx_cat_header_filter(ngx_http_request_t *r){
	ngx_http_cat_t  *conf;
	conf = ngx_http_get_module_loc_conf(r, ngx_cat_filter_module);

	if(conf->enable == 1){
		response_start_msec = ngx_current_msec;
	}

	return ngx_http_next_header_filter(r);
}

void cpystr(char* buf, int* p, char* str, int len){
	ngx_memcpy(buf + *p, str, len);
	*p = *p + len;
}

void cpyint(char* buf, int* p, int num){
	sprintf(buf + *p, "%d", num);
	*p = strlen(buf);
}

void cpyuint(char* buf, int* p, unsigned int num){
	sprintf(buf + *p, "%u", num);
	*p = strlen(buf);
}

void cpylong(char* buf, int* p, long num){
	sprintf(buf + *p, "%ld", num);
	*p = strlen(buf);
}

void cpyulong(char* buf, int* p, unsigned long num){
	sprintf(buf + *p, "%lu", num);
	*p = strlen(buf);
}

	static ngx_int_t
ngx_cat_body_filter(ngx_http_request_t *r, ngx_chain_t *chain)
{
	ngx_http_cat_t  *conf;
	conf = ngx_http_get_module_loc_conf(r, ngx_cat_filter_module);

	if(conf->enable == 1){
		send_times ++;
		char buf[SENDOUTBUFSIZE];
		memset(buf, 0, SENDOUTBUFSIZE);
		int p = 0;

		if(r->upstream == NULL){
			cpystr(buf, &p, "\t\t\n", strlen("\t\t\n"));

			cpystr(buf, &p, MODULE_NAME, strlen(MODULE_NAME));
			cpystr(buf, &p, TAB, strlen(TAB));

			cpyuint(buf, &p, (unsigned int)r->headers_out.status);
			cpystr(buf, &p, TAB, strlen(TAB));

			cpystr(buf, &p, "http://", strlen("http://"));
			cpystr(buf, &p, (char*)r->headers_in.host->value.data, r->headers_in.host->value.len);
			cpystr(buf, &p, TAB, strlen(TAB));

			cpyint(buf, &p, (int)r->request_length);
			cpystr(buf, &p, TAB, strlen(TAB));
			
			cpystr(buf, &p, TAB, strlen(TAB));

			cpyint(buf, &p, (int)r->header_size);
			cpystr(buf, &p, TAB, strlen(TAB));

			cpystr(buf, &p, TAB, strlen(TAB));

			cpyuint(buf, &p, send_times);
			cpystr(buf, &p, TAB, strlen(TAB));
			send_times = 0;

			cpylong(buf, &p, (r->start_sec * 1000 + r->start_msec));
			cpystr(buf, &p, TAB, strlen(TAB));

			cpystr(buf, &p, TAB, strlen(TAB));

			cpystr(buf, &p, TAB, strlen(TAB));

			unsigned long time = ngx_current_msec;
			cpyulong(buf, &p, time);
			cpystr(buf, &p, TAB, strlen(TAB));

			cpystr(buf, &p, NEWLINE, strlen(NEWLINE));

			write(pipefd[1], buf, SENDOUTBUFSIZE);
		}
		else{
			if(r->upstream->state->response_length || r->upstream->state->status == NGX_HTTP_BAD_GATEWAY || r->upstream->state->status == NGX_HTTP_SERVICE_UNAVAILABLE || r->upstream->state->status == NGX_HTTP_GATEWAY_TIME_OUT){
				size_t i;
				int j = 0;
				for(i = 0; i < r->headers_out.headers.part.nelts; i++){
					if(!ngx_strncmp(((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].key.data, CAT_ROOT_ID, strlen(CAT_ROOT_ID))){
						cpystr(buf, &p, (char*)((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.data, ((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.len);
						cpystr(buf, &p, TAB, strlen(TAB));
						j ++;
					}
					else if(!ngx_strncmp(((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].key.data, CAT_PARENT_ID, strlen(CAT_PARENT_ID))){
						cpystr(buf, &p, (char*)((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.data, ((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.len);
						cpystr(buf, &p, TAB, strlen(TAB));
						j ++;
					}
					else if(!ngx_strncmp(((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].key.data, CAT_ID, strlen(CAT_ID))){
						cpystr(buf, &p, (char*)((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.data, ((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.len);
						cpystr(buf, &p, TAB, strlen(TAB));
						j ++;
					}
					else if(!ngx_strncmp(((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].key.data, VARNISH_AGE, strlen(VARNISH_AGE))){
						if(ngx_strncmp(((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.data, "0", ((ngx_table_elt_t*)r->headers_out.headers.part.elts)[i].value.len)){
							p = 0;
							memset(buf, 0, SENDOUTBUFSIZE);
							cpystr(buf, &p, "\t\t\n", strlen("\t\t\n"));
							break;
						}
					}
				}
				while ( j < 3 ){
					cpystr(buf, &p, TAB, strlen(TAB));
					j ++;
				}
				if( strncmp((buf + p - 1), TAB, strlen(TAB)) == 0 ){
					memset(buf + p - 1,'\n', 1);
				}

				cpystr(buf, &p, MODULE_NAME, strlen(MODULE_NAME));
				cpystr(buf, &p, TAB, strlen(TAB));

				cpyuint(buf, &p, (unsigned int)r->headers_out.status);
				cpystr(buf, &p, TAB, strlen(TAB));
				
				cpystr(buf, &p, "http://", strlen("http://"));
				cpystr(buf, &p, (char*)r->headers_in.host->value.data, r->headers_in.host->value.len);
				cpystr(buf, &p, (char*)r->uri.data, r->uri.len);
				cpystr(buf, &p, TAB, strlen(TAB));

				cpyint(buf, &p, (int)r->request_length);
				cpystr(buf, &p, TAB, strlen(TAB));

				cpystr(buf, &p, "http://", strlen("http://"));
				cpystr(buf, &p, (char*)r->upstream->peer.name->data, r->upstream->peer.name->len);
				cpystr(buf, &p, (char*)r->upstream->uri.data, r->upstream->uri.len);
				cpystr(buf, &p, TAB, strlen(TAB));

				cpyint(buf, &p, (int)r->header_size);
				cpystr(buf, &p, TAB, strlen(TAB));

				cpyuint(buf, &p, (unsigned int)r->upstream->state->response_length);
				cpystr(buf, &p, TAB, strlen(TAB));

				cpyuint(buf, &p, send_times - 1);
				cpystr(buf, &p, TAB, strlen(TAB));
				send_times = 0;

				cpylong(buf, &p, (r->start_sec * 1000 + r->start_msec));
				cpystr(buf, &p, TAB, strlen(TAB));

				cpyulong(buf, &p, start_upstream_sec * 1000 + start_upstream_msec);
				cpystr(buf, &p, TAB, strlen(TAB));

				cpyulong(buf, &p, response_start_msec);
				cpystr(buf, &p, TAB, strlen(TAB));
				response_start_msec = 0;

				cpyulong(buf, &p, start_upstream_sec * 1000 + start_upstream_msec + r->upstream->state->response_sec * 1000 + r->upstream->state->response_msec);
				cpystr(buf, &p, TAB, strlen(TAB));

				unsigned long time = ngx_current_msec;
				cpyulong(buf, &p, time);
				cpystr(buf, &p, TAB, strlen(TAB));

				cpystr(buf, &p, NEWLINE, strlen(NEWLINE));
				start_upstream_sec = 0;
				start_upstream_msec = 0;
				
				int n;
				n = write(pipefd[1], buf, SENDOUTBUFSIZE);
			}
			else{
				if(!start_upstream_sec){
					start_upstream_sec = r->upstream->state->response_sec;
					start_upstream_msec = r->upstream->state->response_msec;
				}
			}
		}
	}
	return ngx_http_next_body_filter(r, chain);
}

	static void *
ngx_cat_create_conf(ngx_conf_t *cf)
{
	ngx_http_cat_t  *conf;

	conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_cat_t));
	if (conf == NULL) {
		return NULL;
	}

	conf->enable = NGX_CONF_UNSET;
	return conf;
}


	static char *
ngx_cat_merge_conf(ngx_conf_t *cf, void *parent, void *child)
{
	ngx_http_cat_t *prev = parent;
	ngx_http_cat_t *conf = child;

	ngx_conf_merge_value(conf->enable, prev->enable, 0);
	return NGX_CONF_OK;
}


static char * ngx_http_cat(ngx_conf_t *cf, ngx_command_t *cmd, void *conf){
	ngx_http_cat_t	*cat = conf;
	if (pipe(pipefd) == -1) {
		perror("pipe");
		exit(EXIT_FAILURE);
	}
	cat->enable = 1;

	return NGX_CONF_OK;
}

	static void *
ngx_proc_send_create_conf(ngx_conf_t *cf)
{
	ngx_proc_send_conf_t  *pbcf;

	pbcf = ngx_pcalloc(cf->pool, sizeof(ngx_proc_send_conf_t));

	if (pbcf == NULL) {
		ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
				"send create proc conf error");
		return NULL;
	}

	pbcf->enable = NGX_CONF_UNSET;
	pbcf->port = NGX_CONF_UNSET_UINT;
	pbcf->mmap_dat_size = NGX_CONF_UNSET;

	return pbcf;
}


	static char *
ngx_proc_send_merge_conf(ngx_conf_t *cf, void *parent, void *child)
{
	ngx_proc_send_conf_t  *prev = parent;
	ngx_proc_send_conf_t  *conf = child;

	ngx_conf_merge_uint_value(conf->port, prev->port, 0);
	ngx_conf_merge_off_value(conf->enable, prev->enable, 0);

	return NGX_CONF_OK;
}


	static ngx_int_t
ngx_proc_send_prepare(ngx_cycle_t *cycle)
{
	ngx_proc_send_conf_t  *pbcf;

	pbcf = ngx_proc_get_conf(cycle->conf_ctx, ngx_proc_send_module);
	if (!pbcf->enable) {
		return NGX_DECLINED;
	}

	if (pbcf->port == 0) {
		return NGX_DECLINED;
	}

	return NGX_OK;
}

	static ngx_int_t
ngx_proc_send_process_init(ngx_cycle_t *cycle)
{
	int                       reuseaddr;
	ngx_event_t              *rev;
	ngx_socket_t              fd;
	ngx_connection_t         *c;
	struct sockaddr_in        sin;
	ngx_proc_send_conf_t  *pbcf;

	pbcf = ngx_proc_get_conf(cycle->conf_ctx, ngx_proc_send_module);
	fd = ngx_socket(AF_INET, SOCK_STREAM, 0);
	if (fd == -1) {
		ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "send socket error");
		return NGX_ERROR;
	}

	reuseaddr = 1;

	if (setsockopt(fd, SOL_SOCKET, SO_REUSEADDR,
				(const void *) &reuseaddr, sizeof(int))
			== -1)
	{
		ngx_log_error(NGX_LOG_EMERG, cycle->log, ngx_socket_errno,
				"send setsockopt(SO_REUSEADDR) failed");

		ngx_close_socket(fd);
		return NGX_ERROR;
	}
	if (ngx_nonblocking(fd) == -1) {
		ngx_log_error(NGX_LOG_EMERG, cycle->log, ngx_socket_errno,
				"send nonblocking failed");

		ngx_close_socket(fd);
		return NGX_ERROR;
	}

	sin.sin_family = AF_INET;
	sin.sin_addr.s_addr = htonl(INADDR_ANY);
	sin.sin_port = htons(pbcf->port);

	if (bind(fd, (struct sockaddr *) &sin, sizeof(sin)) == -1) {
		ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "send bind error");
		return NGX_ERROR;
	}

	if (listen(fd, 20) == -1) {
		ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "send listen error");
		return NGX_ERROR;
	}

	c = ngx_get_connection(fd, cycle->log);
	if (c == NULL) {
		ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "send no connection");
		return NGX_ERROR;
	}

	c->log = cycle->log;
	rev = c->read;
	rev->log = c->log;
	rev->accept = 1;
	rev->handler = ngx_proc_send_accept;

	if (ngx_add_event(rev, NGX_READ_EVENT, 0) == NGX_ERROR) {
		return NGX_ERROR;
	}

	pbcf->fd = fd;

	return NGX_OK;
}


	static ngx_int_t
ngx_proc_send_loop(ngx_cycle_t *cycle)
{
	pthread_t rtid;
	pthread_t stid;

	if(!cat_started){
		int err1 = pthread_create(&rtid, NULL, recv_info, NULL);
		if(err1){
			ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "create recv_info thread fail!" );
		}
		int err2 = pthread_create(&stid, NULL, send_info, cycle);
		if(err2){
			ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "create send_info thread fail!" );
		}
		cat_started = 1;
	}
	return NGX_OK;
}


	static void
ngx_proc_send_exit_process(ngx_cycle_t *cycle)
{
	ngx_proc_send_conf_t *pbcf;

	pbcf = ngx_proc_get_conf(cycle->conf_ctx, ngx_proc_send_module);

	ngx_close_socket(pbcf->fd);
	munmap(read_write_mmap, sizeof(long)*3);
	munmap(write_file, pbcf->mmap_dat_size);
}


	static void
ngx_proc_send_accept(ngx_event_t *ev)
{
	u_char             sa[NGX_SOCKADDRLEN];
	socklen_t          socklen;
	ngx_socket_t       s;
	ngx_connection_t  *lc;

	lc = ev->data;
	s = accept(lc->fd, (struct sockaddr *) sa, &socklen);
	if (s == -1) {
		return;
	}

	if (ngx_nonblocking(s) == -1) {
		goto finish;
	}

finish:
	ngx_close_socket(s);
}

void* send_info(void* arg){
	ngx_cycle_t *cycle= (ngx_cycle_t*)(arg);
	ngx_proc_send_conf_t  *pbcf;
	pbcf = ngx_proc_get_conf(cycle->conf_ctx, ngx_proc_send_module);

	open_fifo(cycle);
	while(1){
		if(start != end){
			int p = start;
			char out[SENDOUTBUFSIZE + 100];
			memset(out, 0, SENDOUTBUFSIZE + 100);
			sprintf(out, "%s\n", buf[p]);
			if(__sync_bool_compare_and_swap(&start, p, (p + 1) % BUFSIZE)){
				if(*(read_write_mmap + 1) >= *(read_write_mmap + 2) && *(read_write_mmap + 1) + (long)strlen(out) < (long)(pbcf->mmap_dat_size)){
					strcpy(write_file + *(read_write_mmap + 1), out);
					*(read_write_mmap + 1) = *(read_write_mmap + 1) + strlen(out);
				}
				else if(*(read_write_mmap + 1) >= *(read_write_mmap + 2) && *(read_write_mmap + 1) + (long)strlen(out) >= (long)(pbcf->mmap_dat_size)){
					if(((long)strlen(out) - ((long)(pbcf->mmap_dat_size) - *(read_write_mmap + 1)))< *(read_write_mmap + 2)){
						strncpy(write_file + *(read_write_mmap + 1), out, (long)(pbcf->mmap_dat_size) - *(read_write_mmap + 1));
						strcpy(write_file, out + (long)(pbcf->mmap_dat_size) - *(read_write_mmap + 1));
						*(read_write_mmap + 1) = strlen(out) - ((long)(pbcf->mmap_dat_size) - *(read_write_mmap + 1));
					}
				}
				else if(*(read_write_mmap + 1) < *(read_write_mmap + 2) && *(read_write_mmap + 1) + (long)strlen(out) < *(read_write_mmap + 2)){
					strcpy(write_file + *(read_write_mmap + 1), out);
					*(read_write_mmap + 1) = *(read_write_mmap + 1) + strlen(out);

				}
			}
		}
		else{
			usleep(10000);
		}
	}
	return NULL;
}

long get_file_size(char *filename) 
{ 
	struct stat f_stat; 

	if( stat( filename, &f_stat ) == -1 ){ 
		return -1; 
	} 

	return (long)f_stat.st_size; 
}

int createdir(const char *pathname, ngx_cycle_t *cycle){
	char dirname[256];
	strcpy(dirname, pathname);
	int i, len = strlen(dirname);

	len = strlen(dirname);
	for(i = 1; i < len; i++){
		if(dirname[i] == '/'){
			dirname[i] = 0;
			if(access(dirname, 0) != 0){
				if(mkdir(dirname, 0755) == -1){
					ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "mkdir fail! when create %s", pathname);
					return -1;
				}
			}
			dirname[i] = '/';
		}
	}
	return 0;
}


int open_fifo(ngx_cycle_t *cycle){

	ngx_proc_send_conf_t  *pbcf;
	pbcf = ngx_proc_get_conf(cycle->conf_ctx, ngx_proc_send_module);

	int fd = 0;
	int off_fd = 0;
	createdir((char*)pbcf->mmap_idx.data, cycle);
	createdir((char *)pbcf->mmap_dat.data, cycle);


	if(access((char*)pbcf->mmap_idx.data, 0) == -1){
		off_fd = open((char*)pbcf->mmap_idx.data, O_CREAT|O_RDWR, 0777);
		if(off_fd == -1){
			ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "create file %V failed", pbcf->mmap_idx);
			return -1;
		}
		lseek(off_fd, sizeof(long)*3-1, SEEK_SET);
		write(off_fd,"",1);
		read_write_mmap = (long*) mmap( NULL,sizeof(long)*3, PROT_READ|PROT_WRITE, MAP_SHARED, off_fd, 0);
		*read_write_mmap = (unsigned int)pbcf->mmap_dat_size;
		*(read_write_mmap + 1) = 0;
		*(read_write_mmap + 2) = 0;
	}
	else{
		off_fd = open((char*)pbcf->mmap_idx.data, O_CREAT|O_RDWR, 0777);
		if(off_fd == -1){
			ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "open file %V failed", pbcf->mmap_idx);
			return -1;
		}
		if(get_file_size((char*)pbcf->mmap_idx.data) < (long)sizeof(long)*3){
			lseek(off_fd, sizeof(long)*3-1, SEEK_SET);
			write(off_fd,"",1);
			read_write_mmap = (long*) mmap( NULL,sizeof(long)*2, PROT_READ|PROT_WRITE, MAP_SHARED, off_fd, 0);
			*read_write_mmap = (unsigned int)pbcf->mmap_dat_size;
			*(read_write_mmap + 1) = 0;
			*(read_write_mmap + 2) = 0;
		}
		read_write_mmap = (long*) mmap( NULL,sizeof(long)*3, PROT_READ|PROT_WRITE, MAP_SHARED, off_fd, 0);
		if( *read_write_mmap != (long)pbcf->mmap_dat_size ){
			*read_write_mmap = (unsigned int)pbcf->mmap_dat_size;
			*(read_write_mmap + 1) = 0;
			*(read_write_mmap + 2) = 0;
		}
	}
	close(off_fd);

	if(access((char *)pbcf->mmap_dat.data, 0) == -1){
		fd = open((char *)pbcf->mmap_dat.data, O_CREAT|O_RDWR, 0777);
		if(fd == -1){
			ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "open file %V failed", pbcf->mmap_dat);
			return -1;
		}
		lseek(fd, pbcf->mmap_dat_size - 1, SEEK_SET);
		write(fd,"",1);
		write_file = (char*) mmap( NULL, pbcf->mmap_dat_size, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);
	}
	else{
		fd = open((char *)pbcf->mmap_dat.data, O_CREAT|O_RDWR, 0777);
		if(fd == -1){
			ngx_log_error(NGX_LOG_ERR, cycle->log, 0, "open file %V failed", pbcf->mmap_dat);
			return -1;
		}
		if(get_file_size((char *)pbcf->mmap_dat.data) < (long)pbcf->mmap_dat_size){
			lseek(fd, pbcf->mmap_dat_size - 1, SEEK_SET);
			write(off_fd,"",1);
			read_write_mmap = (long*) mmap(NULL, pbcf->mmap_dat_size, PROT_READ|PROT_WRITE, MAP_SHARED, off_fd, 0);
		}
		write_file = (char*) mmap(NULL, pbcf->mmap_dat_size, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);
	}

	close(fd);
	return 0;
}

void* recv_info(){
	while(1){
		while(read(pipefd[0], buf[end], SENDOUTBUFSIZE) > 0){
			end = ( end + 1 ) % BUFSIZE;
			if(end == start){
				__sync_bool_compare_and_swap(&start, end, (end + 1) % BUFSIZE);
			}
		}
	}
	return 0;
}
