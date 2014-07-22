set -e
set -u

cd `dirname $0`

function kill_by_javaclass {
local javaclass=$1
jps=/usr/local/jdk/bin/jps

if [ ! -x $jps ];then
jps=jps
fi

$jps -lvm | awk -v javaclass=$javaclass '$2==javaclass{cmd=sprintf("kill -s TERM %s; sleep 1; kill -9 %s", $1, $1);system(cmd)}'
}

agent_class="com.dianping.cat.agent.monitor.CatAgent"
port=2436

#agent="paas" or "executors"
agent="executors"

if [ $# -ge 1 ];then
agent=$1
fi

kill_by_javaclass $agent_class

if [ -e WEB-INF/classes ];then
rm -rf classes
mv WEB-INF/classes ./
fi
if [ -e WEB-INF/lib ];then
rm -rf lib
mv WEB-INF/lib ./
fi

java=/usr/local/jdk/bin/java
if [ ! -x $java ];then
java=java
fi

echo "Starting cat-agent $agent_class $port `pwd`"

para=""
if [ "$agent" = "paas"  ];then
ip=$(sh -c "ifconfig br0 | awk -v FS='[ \t:]+' 'NR == 2 {print \$4}'")
para="-Dhost.ip=$ip"
fi

nohup $java -Xms128m -Xmx128m -classpath classes:"lib/*" -Dagent=$agent $para $agent_class $port /agent `pwd` >>/data/applogs/cat/agent-startup.log 2>&1 &
echo "Started"