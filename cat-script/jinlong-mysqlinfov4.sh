#!/bin/sh
#
#########################################################################################
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2 of the License, or (at your)
# option) any later version.
#
# This program is distributed in the hope that it will be useful, but
# along with this program; if not, write to the Free Software
# Foundation. My QQ Number:342597591  QQ Group Numer 195836960 Email:zxszcaijin@qq.com
# CHINA.
#
# An on-line copy of the GNU General Public License can be found
# http://www.fsf.org/copyleft/gpl.html.
########################################################################################

basedir=/data/monitor
globalLock=mon.lk
ip0=$(/sbin/ifconfig eth0|awk '/inet addr/ {print substr($2,6)}')
ip1=$(/sbin/ifconfig eth1|awk '/inet addr/ {print substr($2,6)}')
ip2=$(/sbin/ifconfig eth2|awk '/inet addr/ {print substr($2,6)}')
ip3=${ip0:-ip1}
ip=${ip3:-ip2}
hostname=$(hostname|awk 'BEGIN {IGNORECASE=1};{split($1,arr,".")}; arr[1] !~ /^db-/ {arr[1]="db-"arr[1]};{print arr[1](arr[2]?"%2E"arr[2]:"")}')"%5B${ip}%5D"
processname=$(basename $0)
logfile=${processname}.log
datetime=$(date +"%Y-%m-%d %H:%M:%S")
timestamp=$(date +"%s")
timestamp_old=$timestamp
difftimestamp=1
mysqlusername=mysqmuser
mysqlpassword=mysqlpassword
befor_1min_log=befor_1min.log
after_1min_log=after_1min.log
differ_1min_log=differ_1min.log
diskmeminfo_log=diskmeminfo.log
net_before_1min_log=net_befor_1min.log
net_after_1min_log=net_after_1min.log
differ_net_1min_log=differ_net_1min.log

dp=$(df -l|awk '/[ ]+\/data$/ {print arr[split($1,arr,"/")]}')

p=0

first=1
ncpu=$(awk '/processor/ {a++} END {print a}' /proc/cpuinfo)
HZ=100
#########################################################################################
###global static status##################################################################
Mysql_Not_Need_Diff=(Threads_running Threads_connected)


group=$hostname
domain="domain"
avg="avg"



function InitGlobalVariables(){
#The blow varibles we need monitor
#1.the system status
io_writes_old=-1 
io_reads_old=-1 
iops_old=-1
network_out_old=-1
network_in_old=-1 

Load=-1
MAX_Load=-1
diskAvail=-1
diskUsedRatio=-1
swapTotal=-1
swapUsed=-1
swapFree=-1
io_writes=-1 
io_reads=-1 
io_util=-1 
iops=-1
network_out=-1
network_in=-1 

MAX_io_writes=-1 
MAX_io_reads=-1 
MAX_io_util=-1 
MAX_iops=-1
MAX_network_out=-1
MAX_network_in=-1

#mysql global status need monitor
#2.the mysql status 

COM_DELETE_old=-1
COM_INSERT_old=-1 
COM_SELECT_old=-1 
COM_UPDATE_old=-1 
QPS_old=-1
CREATED_TMP_DISK_TABLES_old=-1
CREATED_TMP_TABLES_old=-1 
INNODB_DATA_READS_old=-1  
INNODB_DATA_WRITES_old=-1 
INNODB_ROWS_DELETED_old=-1 
INNODB_ROWS_INSERTED_old=-1
INNODB_ROWS_UPDATED_old=-1
QUESTIONS_old=-1
SLOW_QUERIES_old=-1
THREADS_CONNECTED_old=-1
THREADS_RUNNING_old=-1
TPS_old=-1
RESPONSE_TIME_old=-1
REPDELAY_old=-1
Total_Count_old=-1
Total_Time_old=-1

COM_DELETE=-1
COM_INSERT=-1 
COM_SELECT=-1 
COM_UPDATE=-1 
QPS=-1
CREATED_TMP_DISK_TABLES=-1
CREATED_TMP_TABLES=-1 
INNODB_DATA_READS=-1  
INNODB_DATA_WRITES=-1 
INNODB_ROWS_DELETED=-1 
INNODB_ROWS_INSERTED=-1  
INNODB_ROWS_UPDATED=-1 
QUESTIONS=-1  
SLOW_QUERIES=-1  
THREADS_CONNECTED=-1
THREADS_RUNNING=-1 
TPS=-1
RESPONSE_TIME=-1
REPDELAY=-1
Total_Count=-1
Total_Time=-1


MAX_COM_DELETE=-1
MAX_COM_INSERT=-1
MAX_COM_SELECT=-1
MAX_COM_UPDATE=-1
MAX_QPS=-1
MAX_CREATED_TMP_DISK_TABLES=-1
MAX_CREATED_TMP_TABLES=-1
MAX_INNODB_DATA_READS=-1
MAX_INNODB_DATA_WRITES=-1
MAX_INNODB_ROWS_DELETED=-1
MAX_INNODB_ROWS_INSERTED=-1
MAX_INNODB_ROWS_UPDATED=-1
MAX_QUESTIONS=-1
MAX_SLOW_QUERIES=-1
MAX_THREADS_CONNECTED=-1
MAX_THREADS_RUNNING=-1
MAX_TPS=-1
MAX_RESPONSE_TIME=-1
MAX_REPDELAY=-1
MAX_Total_Count=-1
MAX_Total_Time=-1
}

[[ ! -d $basedir ]] && mkdir -p $basedir

[[ -f $basedir/$globalLock ]] && exit 1


log_info(){
  echo $* >> $basedir/$logfile
}

msg_info(){
	echo "$datetime  $processname[$$] $*"
}

msg_error(){
        echo "$datetime  $processname[$$]error: $*"
        exit 1
}

cleanup(){
 rc=$?
 log_info $(msg_info "clean up the globalLock")
 [[ -f $basedir/$globalLock ]] && rm -f $basedir/$globalLock
 log_info $(msg_info "clean globalLock success")
 exit $rc
}

trap cleanup  15
trap '' 1 2 3 

sleep=1

while getopts "s:p:m:o:d:" arg;
do
 case $arg in 
 s) 
 sleep=$OPTARG;;
 p)
 p=$OPTARG;;
 m)
 m=$OPTARG;;
 o)
 o=$OPTARG;;
 d)
 d=$OPTARG;;
 ?)
 msg_error "The option error ";;
 esac
done

genGlobalLockFile(){
  log_info $(msg_info "get globalLock ")
  touch $basedir/$globalLock  && echo $$ > $basedir/$globalLock
  [[ $? -ne 0 ]] && msg_error "get globalLock error " 
  log_info $(msg_info "get globalLock Success ")
}

   mysql_mon_sql="select VARIABLE_NAME,VARIABLE_VALUE from GLOBAL_STATUS where VARIABLE_NAME in
   (
   'Com_select',
   'Com_insert',
   'Com_delete',
   'Com_update',
   'Innodb_data_reads',
   'Innodb_rows_inserted', 
   'Innodb_rows_updated',
   'Innodb_rows_deleted', 
   'Innodb_data_writes', 
   'Created_tmp_tables', 
   'Threads_running',
   'Threads_connected',
   'Slow_queries',
   'Questions',
   'Created_tmp_disk_tables'
   ) order by 1"
  
getMysqlGlobalStatus(){
   log_info $(msg_info "start get mysql global status info ")
## start make target mysql status logfile
   #if [[ ! -f $basedir/$befor_1min_log ]];then
   #echo "timestamp $timestamp" >  $basedir/$befor_1min_log
   #mysql -u${mysqlusername} -p${mysqlpassword} -N -s -e "show global status" >> $basedir/$befor_1min_log

   if [ $first -eq 0 ];
   then
     COM_DELETE_old=$COM_DELETE
     COM_INSERT_old=$COM_INSERT
     COM_SELECT_old=$COM_SELECT
     COM_UPDATE_old=$COM_UPDATE
     QPS_old=$QPS
     CREATED_TMP_DISK_TABLES_old=$CREATED_TMP_DISK_TABLES
     CREATED_TMP_TABLES_old=$CREATED_TMP_TABLES
     INNODB_DATA_READS_old=$INNODB_DATA_READS
     INNODB_DATA_WRITES_old=$INNODB_DATA_WRITES
     INNODB_ROWS_DELETED_old=$INNODB_ROWS_DELETED
     INNODB_ROWS_INSERTED_old=$INNODB_ROWS_INSERTED
     INNODB_ROWS_UPDATED_old=$INNODB_ROWS_UPDATED
     QUESTIONS_old=$QUESTIONS
     SLOW_QUERIES_old=$SLOW_QUERIES
     THREADS_CONNECTED_old=$THREADS_CONNECTED
     THREADS_RUNNING_old=$THREADS_RUNNING
     TPS_old=$TPS
     RESPONSE_TIME_old=$RESPONSE_TIME
     REPDELAY_old=$REPDELAY
     Total_Count_old=$Total_Count
     Total_Time_old=$Total_Time
   fi
        
   mysql_global_status_before=($(mysql -u${mysqlusername} -p${mysqlpassword} -Dinformation_schema -Nse "$mysql_mon_sql"))
   for ((i=0;i<${#mysql_global_status_before[@]};i=i+2));
   do
     case  ${mysql_global_status_before[$i] }  in
     COM_DELETE)
     COM_DELETE=${mysql_global_status_before[$i+1]} ;;
     COM_INSERT)
     COM_INSERT=${mysql_global_status_before[$i+1]} ;;
     COM_SELECT)
     COM_SELECT=${mysql_global_status_before[$i+1]} ;;
     COM_UPDATE)
     COM_UPDATE=${mysql_global_status_before[$i+1]} ;;
     CREATED_TMP_DISK_TABLES)
     CREATED_TMP_DISK_TABLES=${mysql_global_status_before[$i+1]} ;;
     CREATED_TMP_TABLES)
     CREATED_TMP_TABLES=${mysql_global_status_before[$i+1]} ;;
     INNODB_DATA_READS) 
     INNODB_DATA_READS=${mysql_global_status_before[$i+1]} ;; 
     INNODB_DATA_WRITES) 
     INNODB_DATA_WRITES=${mysql_global_status_before[$i+1]} ;; 
     INNODB_ROWS_DELETED)
     INNODB_ROWS_DELETED=${mysql_global_status_before[$i+1]} ;; 
     INNODB_ROWS_INSERTED)  
     INNODB_ROWS_INSERTED=${mysql_global_status_before[$i+1]} ;;
     INNODB_ROWS_UPDATED) 
     INNODB_ROWS_UPDATED=${mysql_global_status_before[$i+1]} ;;
     QUESTIONS)  
     QUESTIONS=${mysql_global_status_before[$i+1]} ;;
     SLOW_QUERIES)  SLOW_QUERIES=${mysql_global_status_before[$i+1]} ;;
     THREADS_CONNECTED) 
     THREADS_CONNECTED=${mysql_global_status_before[$i+1]} 
     THREADS_CONNECTED_old=$THREADS_CONNECTED;;
     THREADS_RUNNING)
     THREADS_RUNNING=${mysql_global_status_before[$i+1]} 
     THREADS_RUNNING_old=$THREADS_RUNNING;;
  esac
  done


   #mysql -u${mysqlusername} -p${mysqlpassword} -s -e "show slave status \G"|awk '/Seconds_Behind_Master/ {printf("RepDelay\t0\n")}' 
   #mysql -u${mysqlusername} -p${mysqlpassword} -qNsre "select 'Total_time', sum(total) ,'\nTotal_count',sum(count) from Information_Schema.Query_Response_Time \
   #                                                                       where time >='      0.000100'  and time<='      1.000000'"
  # elif [[ -f $basedir/$after_1min_log ]];then
  # mv  $basedir/$after_1min_log  $basedir/$befor_1min_log
  # fi #timestamp_old=$(awk '/timestamp/ {print $2}' $basedir/$befor_1min_log) #difftimestamp=$((timestamp - timestamp_old)) #[[ $difftimestamp -eq 0 ]] && difftimestamp=1 difftimestamp=$sleep
  
   #echo "timestamp $timestamp" > $basedir/$after_1min_log
   #mysql -u${mysqlusername} -p${mysqlpassword} -N -s -e "show global status" >> $basedir/$after_1min_log
   #mysql_status_after=$(mysql -u${mysqlusername} -p${mysqlpassword} -Dinformation_schema -Nse "$mysql_mon_sql")
   echo $mysql_status_before
   # mysql -u${mysqlusername} -p${mysqlpassword} -s -e "show slave status \G"|awk '/Seconds_Behind_Master/ {printf("RepDelay\t%d\n",$0?arr[split($0,arr,":")]:0)}' >> $basedir/$after_1min_log
   REPDELAY=$(mysql -u${mysqlusername} -p${mysqlpassword} -s -e " \
              show slave status \G"|awk '/Seconds_Behind_Master/ {printf("%d",$0?arr[split($0,arr,":")]:0)}')
   RESPONSE_TIMES_VARIABLES=($(mysql -u${mysqlusername} -p${mysqlpassword} -qNsre " \
                               select 'Total_time', sum(total) ,'\nTotal_count',sum(count) from Information_Schema.Query_Response_Time \
                               where time >='      0.000100'  and time<='      1.000000'"))

  for((i=0;i<${#RESPONSE_TIMES_VARIABLES[@]};i=i+1)); 
  do
     case ${RESPONSE_TIMES_VARIABLES[$i]} in 
     Total_time)
     Total_Time=${RESPONSE_TIMES_VARIABLES[$i+1]};;
     Total_count)
     Total_Count=${RESPONSE_TIMES_VARIABLES[$i+1]};;
    esac
  done

   if [ $first -eq 1 ];
   then
   MAX_THREADS_CONNECTED=$THREADS_CONNECTED;
   MAX_THREADS_RUNNING=$THREADS_RUNNING;
   MAX_REPDELAY=$REPDELAY
   fi 


   if [ $first -eq 0 ];
   then 
   [[ $REPDELAY -gt $MAX_REPDELAY ]] && MAX_REPDELAY=$REPDELAY
   MAX_RESPONSE_TIME=$(awk 'BEGIN{ response=1000*('$Total_Time'-'$Total_Time_old')/('$Total_Count'-'$Total_Count_old') ; \
                      if(response>'$MAX_RESPONSE_TIME'){print response } else {print '$MAX_RESPONSE_TIME'}}')

   [[ $(((COM_DELETE-COM_DELETE_old)/sleep)) -gt $MAX_COM_DELETE ]] && MAX_COM_DELETE=$(((COM_DELETE-COM_DELETE_old)/sleep))
   [[ $(((COM_INSERT-COM_INSERT_old)/sleep)) -gt $MAX_COM_INSERT ]] && MAX_COM_INSERT=$(((COM_INSERT-COM_INSERT_old)/sleep))
   [[ $(((COM_SELECT-COM_SELECT_old)/sleep)) -gt $MAX_COM_SELECT ]] && MAX_COM_SELECT=$(((COM_SELECT-COM_SELECT_old)/sleep))
   [[ $(((COM_UPDATE-COM_UPDATE_old)/sleep)) -gt $MAX_COM_UPDATE ]] && MAX_COM_UPDATE=$(((COM_UPDATE-COM_UPDATE_old)/sleep))
   [[ $(((CREATED_TMP_DISK_TABLES-CREATED_TMP_DISK_TABLES_old)/sleep)) -gt $MAX_CREATED_TMP_DISK_TABLES ]] && \
                                   MAX_CREATED_TMP_DISK_TABLES=$(((CREATED_TMP_DISK_TABLES-CREATED_TMP_DISK_TABLES_old)/sleep))
   [[ $(((CREATED_TMP_TABLES-CREATED_TMP_TABLES_old)/sleep)) -gt $MAX_CREATED_TMP_TABLES ]] && \
                                   MAX_CREATED_TMP_TABLES=$(((CREATED_TMP_TABLES-CREATED_TMP_TABLES_old)/sleep))
   [[ $(((INNODB_DATA_READS-INNODB_DATA_READS_old)/sleep)) -gt $MAX_INNODB_DATA_READS ]] && \
                                   MAX_INNODB_DATA_READS=$(((INNODB_DATA_READS-INNODB_DATA_READS_old)/sleep))
   [[ $(((INNODB_DATA_WRITES-INNODB_DATA_WRITES_old)/sleep)) -gt $MAX_INNODB_DATA_WRITES ]] && \
                                   MAX_INNODB_DATA_WRITES=$(((INNODB_DATA_WRITES-INNODB_DATA_WRITES_old)/sleep))
   [[ $(((INNODB_ROWS_DELETED-INNODB_ROWS_DELETED_old)/sleep)) -gt $MAX_INNODB_ROWS_DELETED ]] && \
                                   MAX_INNODB_ROWS_DELETED=$(((INNODB_ROWS_DELETED-INNODB_ROWS_DELETED_old)/sleep))
   [[ $(((INNODB_ROWS_INSERTED-INNODB_ROWS_INSERTED_old)/sleep)) -gt $MAX_INNODB_ROWS_INSERTED ]] && \
                                   MAX_INNODB_ROWS_INSERTED=$(((INNODB_ROWS_INSERTED-INNODB_ROWS_INSERTED_old)/sleep))
   [[ $(((INNODB_ROWS_UPDATED-INNODB_ROWS_UPDATED_old)/sleep)) -gt $MAX_INNODB_ROWS_UPDATED ]] && 
                                   MAX_INNODB_ROWS_UPDATED=$(((INNODB_ROWS_UPDATED-INNODB_ROWS_UPDATED_old)/sleep)) 
   [[ $(((QUESTIONS-QUESTIONS_old)/sleep)) -gt $MAX_QUESTIONS ]] && MAX_QUESTIONS=$(((QUESTIONS-QUESTIONS_old)/sleep))
   
   [[ $THREADS_RUNNING -gt $MAX_THREADS_RUNNING ]] && MAX_THREADS_RUNNING=$THREADS_RUNNING
   
   [[ $THREADS_CONNECTED -gt $MAX_THREADS_CONNECTED ]] && MAX_THREADS_CONNECTED=$THREADS_CONNECTED
   
   TPS=$((((COM_INSERT+COM_DELETE+COM_UPDATE)-(COM_INSERT_old+COM_DELETE_old+COM_UPDATE_old))/sleep))

   #echo "COM_INSERT COM_DELETE COM_UPDATE COM_INSERT_old COM__DELETE_old COM_UPDATE_old"
   #echo "$COM_INSERT $COM_DELETE $COM_UPDATE $COM_INSERT_old $COM_DELETE_old $COM_UPDATE_old"
   [[ $TPS -gt $MAX_TPS ]] && MAX_TPS=$TPS
   fi   
   
   if [[ $p -eq 1 &&  $m -eq 1 ]];
   then 
   if [ $first -eq 1 ];
   then 
   echo "delay  resp   del   ins  upd  sel  disktab  tmptab  innoreds inowrts inodels inoinss inoups qps thr thc tps "
   else 
   echo "$MAX_REPDELAY $MAX_RESPONSE_TIME $MAX_COM_DELETE $MAX_COM_INSERT $MAX_COM_UPDATE $MAX_COM_SELECT  \
         $MAX_CREATED_TMP_DISK_TABLES $MAX_CREATED_TMP_TABLES $MAX_INNODB_DATA_READS $MAX_INNODB_DATA_WRITES \
         $MAX_INNODB_ROWS_DELETED $MAX_INNODB_ROWS_INSERTED $MAX_INNODB_ROWS_UPDATED $MAX_QUESTIONS $MAX_THREADS_RUNNING \
         $MAX_THREADS_CONNECTED $MAX_TPS "
   fi
   fi
   
   log_info $(msg_info "end get mysql global status info  ")
}

#diffMysqlGlobalStatus(){
#   befor_file=$1
#   after_file=$2
#   differ_file=$3
#   getMysqlGlobalStatus
#   log_info $(msg_info "start diff the befor status file and after status file ")
#   [[ -f $differ_file ]] && rm -f $differ_file
#   awk -v difftime=$difftimestamp 'BEGIN {
#        Mysql_No_Need_Diff["Threads_running"]=1;
#        Mysql_No_Need_Diff["Threads_connected"]=1;
#        Mysql_No_Need_Diff["RepDelay"]=1;
#        Mysql_No_Need_Divid_By_Time["Total_time"]=1;
#        Mysql_No_Need_Divid_By_Time["Total_count"]=1;
#       }
#        NR==FNR {
#        status[$1]=$2
#        };
#        NR>FNR{
#        if($1 in Mysql_No_Need_Diff){
#           diff_status[$1]=$2
#        } else {
#           if($1 in Mysql_No_Need_Divid_By_Time){
#               diff_status[$1]=$2-status[$1]
#        }else
#           diff_status[$1]=($2-status[$1])/difftime
#               }
#              } 
#         END {
#           for (s in diff_status){
#           if(s=="timestamp") {
#            printf("%s\t%d\n", s ,diff_status[s])
#            } else {
#         printf("%s\t%d\n",s,diff_status[s])|"sort -nrk2"
#        }
#         }
#         }' $befor_file $after_file >$differ_file
#        
#    
#}


getDiskAndMemInfo(){
 log_info $(msg_info "get the disk usage function")
 disk_info_array=($(df -lk|awk '/\/data$/ {printf("diskAvail\t%d\ndiskUsedRatio\t%d\n",$4*1000,substr($5,0,index($5,"%")-1))}'))
 for ((i=0;i<=${#disk_info_array[@]};i=i+2));
 do
   case ${disk_info_array[i]} in 
   diskAvail)
     MAX_diskAvail=${disk_info_array[i+1]};;
   diskUsedRatio)
     MAX_diskUsedRatio=${disk_info_array[i+1]};;
  esac
 done
 

 Load=$(awk '{print $1}' /proc/loadavg) 
 MAX_Load=$(awk 'BEGIN{if('$Load' > '$MAX_Load'){print '$Load'}else{print '$MAX_Load'}} ')
   
 log_info $(msg_info "end the disk usage function")
 log_info $(msg_info "get the swap usage information")
 swapinfo_array=($(free -b|awk 'BEGIN{IGNORECASE=1} /swap:/{printf("swapTotal\t%-d\nswapUsed\t%-d\nswapFree	%-d\n",$2,$3,$4)}'))
 for ((i=0;i<=${#swapinfo_array[@]};i=i+1));
 do
  case ${swapinfo_array[$i]} in 
  swapTotal)
  MAX_swapTotal=${swapinfo_array[$i+1]};;
  swapUsed)
  MAX_swapUsed=${swapinfo_array[$i+1]};;
  swapFree)
  MAX_swapFree=${swapinfo_array[$i+1]};;
  esac
 done

 if [[ $p -eq 1 && $d -eq 1 ]];
   then 
    if [ $first -eq 1 ];
     then 
      echo "diskAvail diskUsedRatio swapTotal swapUsed swapFree  MAX_load"
    else
      echo "$diskAvail $diskUsedRatio $swapTotal $swapUsed $swapFree $MAX_Load"
     fi
  fi 
 
 log_info $(msg_info "end the swap usage function ")
}

getDiskNetBytes(){
 #if [[ ! -f $basedir/$net_before_1min_log ]] ;then
 # echo "timestamp $timestamp "> $basedir/$net_before_1min_log

#discard old collect information code
# awk 'ARGIND==1 && /eth0:/{
#                          printf("network_in    %d\nnetwork_out   %d\n",substr($1,6),$9)};
#     ARGIND == 2 && /'$dp'/{
#                          printf("io_reads\t%d\nio_writes\t%d\niops\t%d\n", $4,$8,$4+$8)}' /proc/net/dev /proc/diskstats >>$basedir/$net_before_1min_log


netinfo_array=($(awk 'ARGIND==1 && /eth0:/{ printf("network_in    %d\nnetwork_out   %d\n",substr($1,6),$9)};
     ARGIND==2 && /'$dp'/{ printf("io_reads\t%d\nio_writes\t%d\niops\t%d\nticks\t%d\n", $4,$8,$4+$8,$13)};
     ARGIND==3 &&/cpu / {cputime=($2+$3+$4+$5+$6+$7+$8)}; 
     END{printf("cputime\t%d\n"),cputime}' /proc/net/dev /proc/diskstats /proc/stat))



if [ $first -eq 0 ];
then 
  network_in_old=$network_in
  network_out_old=$network_out
  io_reads_old=$io_reads
  io_writes_old=$io_writes
  iops_old=$iops
  ticks_old=$ticks
  cputime_old=$cputime

fi

for ((i=0;i<${#netinfo_array[@]};i=i+2));
do 
  case ${netinfo_array[$i]} in 
  network_in)
  network_in=${netinfo_array[$i+1]};;
  network_out)
  network_out=${netinfo_array[$i+1]};;
  io_reads)
  io_reads=${netinfo_array[$i+1]};;
  io_writes)
  io_writes=${netinfo_array[$i+1]};;
  iops)
  iops=${netinfo_array[$i+1]};;
  ticks)
  ticks=${netinfo_array[$i+1]};;
  cputime)
  cputime=${netinfo_array[$i+1]};;
  esac
done

if [ $first -eq 0 ]; 
then
[[ $(((network_in-network_in_old)/sleep))  -gt $MAX_network_in ]] && MAX_network_in=$(((network_in-network_in_old)/$sleep))
[[ $(((network_out-network_out_old)/sleep)) -gt $MAX_network_out ]] && MAX_network_out=$(((network_out-network_out_old)/$sleep))
[[ $(((io_reads-io_reads_old)/sleep)) -gt $MAX_io_reads ]] && MAX_io_reads=$(((io_reads-io_reads_old)/$sleep))
[[ $(((io_writes-io_writes_old)/sleep)) -gt $MAX_io_writes ]] && MAX_io_writes=$(((io_writes-io_writes_old)/$sleep))
[[ $(((iops-iops_old)/sleep)) -gt $MAX_iops ]] && MAX_iops=$(((iops-iops_old)/sleep))

deltams=$((1000*(cputime-cputime_old)/ncpu/HZ))
io_util=$((100*(ticks-ticks_old)/deltams))

[[ $io_util -gt $MAX_io_util ]] && MAX_io_util=$io_util

fi
if [[ $p -eq 1 && $o -eq 1 ]];
then
if [ $first -eq 1 ];
then 
echo "MAX_network_in  MAX_network_out  MAX_io_reads  MAX_io_writes MAX_iops MAX_io_util"
else
echo -e "$MAX_network_in \t $MAX_network_out \t $MAX_io_reads  \t $MAX_io_writes \t  $MAX_iops  $MAX_io_util"
fi
fi
 
 #elif [[ -f $basedir/$net_after_1min_log ]];then
 #mv $basedir/$net_after_1min_log $basedir/$net_before_1min_log
 #fi

 #timestamp_old=$(awk '/timestamp/ {printf("%d\n",$2)}' $basedir/$net_before_1min_log)
 #difftimestamp=$((timestamp - timestamp_old))
 #[[ $difftimestamp -eq 0 ]] && difftimestamp=1


 #echo "timestamp $timestamp ">$basedir/$net_after_1min_log
#discard old information collect code
# awk 'ARGIND==1 && /eth0:/{
#                          printf("network_in    %d\nnetwork_out   %d\n",substr($1,6),$9)};
#      ARGIND == 2 && /'$dp'/{
#                          printf("io_reads\t%d\nio_writes\t%d\niops\t%d\n", $4,$8,$4+$8)}' /proc/net/dev /proc/diskstats  >>$basedir/$net_after_1min_log

#awk 'ARGIND==1 && /eth0:/{ printf("network_in    %d\nnetwork_out   %d\n",substr($1,6),$9)};
#     ARGIND==2 && /'$dp'/{ printf("io_reads\t%d\nio_writes\t%d\niops\t%d\nticks\t%d\n", $4,$8,$4+$8,$13)};
#     ARGIND==3 &&/cpu / {cputime=($2+$3+$4+$5+$6+$7+$8)}; 
#     END{printf("cputime\t%f\n"),cputime}' /proc/net/dev /proc/diskstats  /proc/stat   >>$basedir/$net_after_1min_log

}

function main(){
genGlobalLockFile 
#awk -v difftime=$difftimestamp  'BEGIN{
#    rawdata["ticks"]=1;
#    rawdata["cputime"]=1;
#    ticks=0;
#    deltams=0;
#    }
#    NR==FNR { 
#    net_status[$1]=$2
#    };
#    NR>FNR{
#    if (rawdata[$1]){
#    diff_net_status[$1]=($2-net_status[$1])
#    }else 
#    diff_net_status[$1]=($2-net_status[$1])/difftime
#    } END {
#    for(ns in diff_net_status){
#    if(rawdata[ns]){
#    if(ns == "ticks") ticks=diff_net_status[ns];
#    if(ns == "cputime") deltams=1000*(diff_net_status[ns]/'$ncpu'/'$HZ');
#    if(ticks>0 && deltams >0) {
#                                                       printstr_key=printstr_key $1 " "
#                                                       printstr_value=printstr_value sprintf("%s  ",$2)
#                                                       };
#                                                       ARGIND == 4 && exist(array,$1) {
#                                                       array[$1]=$2;
#                                                       if ($1 != "Total_time" && $1 !="Total_count")
#                                                       str=str sprintf("%s%%09%s%%09%s%%09%s%%09%s%%09%d%0A",group,domain,$1 == "Questions"?"QPS":$1,"avg",timestamp,$2)
#                                                       printstr_key=printstr_key sprintf("%s ",$1 == "Questions"?"QPS":$1) 
#                                                       printstr_value=printstr_value sprintf("%s  ",$2)
#                                                       }
#                                                       END{
#                                                       TPM=array["Com_insert"]+array["Com_update"]+array["Com_delete"]
#                                                       str=str sprintf("%s%%09%s%%09%s%%09%s%%09%s%%09%s%0A",group,domain,"TPS","avg",timestamp,TPM)
#                                                       str=str sprintf("%s%%09%s%%09%s%%09%s%%09%s%%09%f%0A",group,domain,"Response_time","avg",timestamp, \
#                                                                                                                   1000*array["Total_time"]/array["Total_count"])
#                                                       printstr_key=printstr_key "TPS Response_time "
#                                                       printstr_value=printstr_value sprintf("%s %f ",TPM,1000*array["Total_time"]/array["Total_count"])
#                                                       #print "http://cat-url-mysql/cat/r/monitor?op=batch&batch="str
#                                                        
#                                                       if ('$execute_cnt' == 1 ){ print printstr_key}
#                                                        print printstr_value
#                                                        
#                                                        }'  /proc/loadavg   $basedir/$diskmeminfo_log $basedir/$differ_net_1min_log $basedir/$differ_1min_log
local execute_cnt=0;
local iplist=(10.1.110.23 10.1.110.21 10.1.6.102)                                                        
local success_status_result='{"statusCode":"0"}'

local monall=(REPDELAY RESPONSE_TIME COM_DELETE COM_INSERT COM_UPDATE COM_SELECT \
              CREATED_TMP_DISK_TABLES CREATED_TMP_TABLES INNODB_DATA_READS INNODB_DATA_WRITES \
              INNODB_ROWS_DELETED INNODB_ROWS_INSERTED INNODB_ROWS_UPDATED QUESTIONS THREADS_RUNNING \
              THREADS_CONNECTED TPS diskAvail diskUsedRatio swapTotal swapUsed swapFree Load \
              network_in  network_out  io_reads   io_writes   iops  io_util \
             )



point=60
oristring="cat-url-mysql"
InitGlobalVariables
while  [ 1 ];
do
getDiskAndMemInfo
getMysqlGlobalStatus
getDiskNetBytes
execute_cnt=$((execute_cnt+1))
first=0
sleep $sleep

if [[ $sleep*$execute_cnt -lt $point ]];
then
  continue
fi

datetime=$(date +"%Y-%m-%d %H:%M:%S")
timestamp=$(date +"%s")000

str=""
for name in ${monall[@]};
do
  value=$(eval echo \${MAX_$name})
  str=$str"$group%09$domain%09$name%09$avg%09$timestamp%09$value%0A"
done



execute_cnt=0
InitGlobalVariables
first=1
#success_status_result='{"statusCode":"0"}'
#$test=$(curl -I -m 5 -o /dev/null -s -w %{http_code} "$allinfo")

#oristring="cat-url-mysql"

allinfo="http://cat-url-mysql/cat/r/monitor?op=batch&batch="$str
for ip in ${iplist[@]};do
  repstring=$ip
  allinfo=${allinfo/$oristring/$repstring}
  test=$(curl  -m 3  -s  "$allinfo")
  if [[ ! -z $test && $test == $success_status_result ]];
  then 
     break
  else
     oristring=$repstring
  fi

done

done
msg_error "not monitor server can use..."

}

main 
