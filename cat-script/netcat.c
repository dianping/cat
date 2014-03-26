#include <stdlib.h>
#include <stdio.h>
#include <pcap.h>
#include <time.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <string.h>
#include <netinet/ip.h>
#include <netinet/if_ether.h>
#include <netinet/tcp.h>
#include <pthread.h>
#include <mysql.h>

//1MB
#define QUEUESIZE 1048576
#define DEALTHREADCOUNT 2
#define CITYIDMAX 500
#define CITYNAMEMAXLENGTH 100
#define IPINFOCOUNT 131865
#define MYSQLSERVER "127.0.0.1"
#define MYSQLSERVERPORT 3306
#define MYSQLUSER "root"
#define MYSQLPASS "12qwaszx"
#define MYSQLDB "DP"
#define INNERNETIPSEGSTART "10.1.0.0"
#define INNERNETIPSEGEND "10.8.0.0"

struct Conf
{
    char servIP[16];
    int servPort;
} g_conf;

struct Queue
{
	int r;
	int w;
	int count;
	u_char buf[QUEUESIZE];
	pthread_mutex_t mutex;
};

struct ThreadArg
{
	struct Queue queue;
} g_threadarg[DEALTHREADCOUNT];

pcap_t *g_phandle;
pthread_mutex_t g_sniffmutex;
pthread_cond_t g_sniffcond;
int g_sniffenable;
unsigned int push_pkt_error;

struct Stat
{
    struct City
    {
        char name[CITYNAMEMAXLENGTH];
        long count;
        long traffic;
    } city[CITYIDMAX];
    long all_in_count;
    long all_in_traffic;
    long all_out_count;
    long all_out_traffic;
    pthread_mutex_t mutex;
} g_stat;

struct IPINFO
{
    struct _IPINFO
    {
        unsigned int start;
        unsigned int end;
        short cityid;
    } info[IPINFOCOUNT];
    int count;
} g_ipinfo;

int init()
{
    MYSQL mysql;
    MYSQL_RES *res;
    MYSQL_ROW row;
    
    mysql_init(&mysql);
    if(NULL == mysql_real_connect(&mysql, MYSQLSERVER, MYSQLUSER,
                                  MYSQLPASS,MYSQLDB,MYSQLSERVERPORT,NULL,0))
    {
        fprintf(stderr,"Couldn't connect to engine!\n%s\n", mysql_error(&mysql));
        exit(1);
    }

    if(0 != mysql_set_character_set(&mysql, "utf8"))
    {
        fprintf(stderr,"mysql set character set error\n");
        exit(1);
    }
    
    char *sql = "select DP_CityIP.`StartIPHashValue`, \
                 DP_CityIP.`EndIPHashValue`, DP_CityIP.`CityID`, \
                 DP_CityIP.`Location` from DP_CityIP;";
    if(0 != mysql_query(&mysql, sql))
    {
        fprintf(stderr,"Query failed (%s)\n",mysql_error(&mysql));
        exit(1);
    }
    
    if(NULL == (res=mysql_use_result(&mysql))) 
    {
        fprintf(stderr,"Couldn't get result from \n");
        exit(1);
    }
    
    while(row = mysql_fetch_row(res))
    {
        if(g_ipinfo.count == IPINFOCOUNT)
            break;
        g_ipinfo.info[g_ipinfo.count].start = atoi(row[0]);
        g_ipinfo.info[g_ipinfo.count].end = atoi(row[1]);
        g_ipinfo.info[g_ipinfo.count].cityid = atoi(row[2]);
        if(g_stat.city[g_ipinfo.info[g_ipinfo.count].cityid].name[0] == 0)
        {
            strcpy(g_stat.city[g_ipinfo.info[g_ipinfo.count].cityid].name, row[3]);
        }
        g_ipinfo.count++;
    }
    
    mysql_free_result(res);
    mysql_close(&mysql);

    return 0;
}

int find_ipseg(unsigned int ip)
{
    int low, high, mid;
    low = 0;
    high = g_ipinfo.count-1;
    while(low <= high)
    {
        mid = (low+high) / 2;
        if(ip >= g_ipinfo.info[mid].start && ip <= g_ipinfo.info[mid].end)
            return mid;
        else if(ip < g_ipinfo.info[mid].start)
            high = mid - 1;
        else
            low = mid + 1;
    }

    return -1;
}

int get_pkt(struct Queue* q, u_char* pkt, int* pkt_len)
{
    int count;
    int r;
    int first;

	pthread_mutex_lock(&q->mutex);
    count = q->count;
    r = q->r;
    if(count == 0)
    {
        pthread_mutex_unlock(&q->mutex);
        return -1;
    }

    //count packet length
    if(r + 4 <= QUEUESIZE)
    {   
        *pkt_len = *((int*)(q->buf+r));
    }   
    else
    {   
        first = QUEUESIZE - r;
        memcpy(pkt_len,q->buf+r,first);
        memcpy(((u_char*)(pkt_len))+first,q->buf,4-first);
    }   
    q->count -= *pkt_len + 4;
    q->r = (q->r + 4 + *pkt_len) % QUEUESIZE;
	pthread_mutex_unlock(&q->mutex);

    r = (r + 4) % QUEUESIZE;
    if(r + *pkt_len <= QUEUESIZE)
    {   
        memcpy(pkt,q->buf+r,*pkt_len);
    }   
    else
    {   
        first = QUEUESIZE - r;
        memcpy(pkt,q->buf+r,first);
        memcpy(pkt+first,q->buf,*pkt_len-first);
    }   

    return 0;
}

int put_pkt(struct Queue* q, const u_char* pkt, int pkt_len)
{
    int count, w, first;

	pthread_mutex_lock(&q->mutex);
	count = q->count;
    w = q->w;
    //+4: 包前面会放一个4字节的整数标识包的长度
    if(count + pkt_len + 4 > QUEUESIZE)
    {
        pthread_mutex_unlock(&q->mutex);
        return -1;
    }
	//修改w
    q->w = (q->w + 4 + pkt_len) % QUEUESIZE;
    q->count += pkt_len + 4;

    if(w + 4 <= QUEUESIZE)
    {
        memcpy(q->buf+w,&pkt_len,4);
    }
    else
    {
        first = QUEUESIZE - w;
        memcpy(q->buf+w,&pkt_len,first);
        memcpy(q->buf,((u_char*)(&pkt_len))+first,4-first);
    }
    w = (w + 4) % QUEUESIZE;
    if(w + pkt_len <= QUEUESIZE)
    {
        memcpy(q->buf+w,pkt,pkt_len);
    }
    else
    {
        first = QUEUESIZE - w;
        memcpy(q->buf+w,pkt,first);
        memcpy(q->buf,pkt+first,pkt_len-first);
    }

	pthread_mutex_unlock(&q->mutex);
    return 0;
}

void push_packet(u_char* user,const struct pcap_pkthdr* pkthdr,const u_char* packet)
{
    struct iphdr* ip;
    struct tcphdr* tcp;
	ip = (struct iphdr *)(packet + sizeof(struct ethhdr));
	tcp = (struct tcphdr *)(packet + sizeof(struct ethhdr) + ip->ihl * 4);
	int porthash = ntohs(tcp->source) % DEALTHREADCOUNT;
	if(-1 == put_pkt(&g_threadarg[porthash].queue, packet+sizeof(struct ethhdr), 
                     pkthdr->len-sizeof(struct ethhdr)))
	{
		push_pkt_error++;
		printf("丢包数%d\n", push_pkt_error);
		usleep(200);
	}
}

void* deal_packet(void* arg)
{
	int ip_len;
    struct iphdr* ip;
    struct tcphdr* tcp;
    u_char tcp_flag;
	u_char buffer[1600];
	memset(buffer, 0, 1600);
	int sockfd;
	struct ThreadArg* ta = (struct ThreadArg*)arg;
	int data_len;
    char *find;
    unsigned int ip1,ip2,ip3,ip4;
    unsigned int xffip;
    int pos;
    in_addr_t inner_net_ip_start = htonl(inet_addr(INNERNETIPSEGSTART));
    in_addr_t inner_net_ip_end = htonl(inet_addr(INNERNETIPSEGEND));
    in_addr_t src, dst;

	while(1)
	{
		if(-1 == get_pkt(&ta->queue, buffer, &ip_len))
		{
			usleep(200);
			continue;
		}

		ip = (struct iphdr *)buffer;
		tcp = (struct tcphdr *)(buffer + ip->ihl * 4);
		ip_len = ntohs(ip->tot_len); 	//maybe shorter then pkt len, eth data shortest len is 46
		data_len = ip_len - ip->ihl * 4 - tcp->doff * 4;
        src = ntohl(ip->saddr);
        dst = ntohl(ip->daddr);

        if(dst >= inner_net_ip_start && dst <= inner_net_ip_end &&
                (src < inner_net_ip_start || src > inner_net_ip_end)) //in
        {
            pthread_mutex_lock(&g_stat.mutex);
            g_stat.all_in_count++;
            g_stat.all_in_traffic += ip_len;
            pthread_mutex_unlock(&g_stat.mutex);
        }
        else if(src >= inner_net_ip_start && src <= inner_net_ip_end &&
                (dst < inner_net_ip_start || dst > inner_net_ip_end)) //out
        {
            pthread_mutex_lock(&g_stat.mutex);
            g_stat.all_out_count++;
            g_stat.all_out_traffic += ip_len;
            pthread_mutex_unlock(&g_stat.mutex);
        }
        else
        {
            fprintf(stderr, "%s\n", inet_ntoa(addr));
        }
	}
}

//抓包
void* sniff(void* arg)
{
	while(1)
	{
		pcap_loop(g_phandle,-1,push_packet,NULL);

		pthread_mutex_lock(&g_sniffmutex);
		if(0 != pthread_cond_wait(&g_sniffcond, &g_sniffmutex))
		{
			fprintf(stderr, "sniff: cond wait error!\n");
		}
		pthread_mutex_unlock(&g_sniffmutex);
	}
}

void send_stat()
{
    long all_in_count, all_in_traffic;
    long all_out_count, all_out_traffic;
    int sndSock;
    struct sockaddr_in serverAddr;
    char buf[1024];
    int buflen = sizeof(buf) / sizeof(buf[0]);
    char content[100];
    int contentlen = sizeof(content) / sizeof(content[0]);
    int sndlen;
    char http_arg[100];

    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(g_conf.servPort);
    serverAddr.sin_addr.s_addr = inet_addr(g_conf.servIP);

    memset(http_arg, 0, sizeof(http_arg));
    sprintf(http_arg, "Host: %s:%d\n"
                      "Connection: keep-alive\n"
                      "Cache-Control: max-age=0\n"
                      "Content-Type: application/x-www-form-urlencoded\n\n",
                      g_conf.servIP, g_conf.servPort);

    while(1)
    {
        sleep(2);

        pthread_mutex_lock(&g_stat.mutex);
        all_in_count = g_stat.all_in_count;
        all_in_traffic = g_stat.all_in_traffic;
        all_out_count = g_stat.all_out_count;
        all_out_traffic = g_stat.all_out_traffic;
        g_stat.all_in_count = 0;
        g_stat.all_in_traffic = 0;
        g_stat.all_out_count = 0;
        g_stat.all_out_traffic = 0;
        pthread_mutex_unlock(&g_stat.mutex);

        printf("%u,%u\n", all_in_count, all_in_traffic);
        printf("%u,%u\n", all_out_count, all_out_traffic);
        printf("\n");

        if((sndSock = socket(AF_INET, SOCK_STREAM, 0)) < 0)
        {
            fprintf(stderr, "create send socket failed!\n");
            continue;
        };

        if(connect(sndSock, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) < 0)
        {
            fprintf(stderr, "connect to server failed!\n");
            close(sndSock);
            continue;
        }

        memset(buf, 0, buflen);
        memset(content, 0, contentlen);
        sprintf(content, "group=TestGroup&domain=net&key=traffic&op=sum&sum=%d", all_in_count);
        sprintf(buf, "GET /cat/r/systemMonitor?%s HTTP/1.1\n%s", content, http_arg);

        if((sndlen = send(sndSock, buf, strlen(buf), 0)) == -1 )
        {
            fprintf(stderr, "send data to server failed!\n");
            close(sndSock);
            continue;
        }

        close(sndSock);
    }
}

int main(int argc, char **argv)
{
    char *device = "eth5";
    char errbuf[1024];
	pthread_t tid[DEALTHREADCOUNT];
	pthread_t snifftid;
	int i;
    
    bpf_u_int32 ipaddress,ipmask;
    struct bpf_program fcode;

    printf("initing....\n");
    memset(&g_stat, 0, sizeof(g_stat));
    memset(&g_ipinfo, 0, sizeof(g_ipinfo));
    if(-1 == init())
    {
        fprintf(stderr, "init cityip error\n");
        exit(1);
    }
    printf("init completed.\n");

	memset(&g_threadarg, 0, sizeof(struct ThreadArg)*DEALTHREADCOUNT);
	for(i=0; i<DEALTHREADCOUNT; i++)
	{
		if(pthread_create(&tid[i], NULL, deal_packet, &g_threadarg[i]) != 0)
		{
			fprintf(stderr, "create packet deal thread %d error\n", i+1);
			exit(1);
		}
	}

    g_phandle=pcap_open_live(device,1600,1,100,errbuf);
    if(g_phandle==NULL){
        perror(errbuf);
        exit(1);
    }

	memset(&g_sniffcond, 0, sizeof(pthread_cond_t));
	memset(&g_sniffmutex, 0, sizeof(pthread_mutex_t));
	g_sniffenable = 1;
	if(pthread_create(&snifftid, NULL, sniff, NULL) != 0)
	{
		fprintf(stderr, "*** create sniff thread error\n");
        exit(1);
	}

    memset(&g_conf, 0, sizeof(g_conf));
    strcpy(g_conf.servIP, "127.0.0.1");
    g_conf.servPort = 8888;
	send_stat();
    
    return 0;
} 
