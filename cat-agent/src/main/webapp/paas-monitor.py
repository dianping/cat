#!/usr/bin/python
# -*- coding: utf-8 -*-
import subprocess
import json
import re
import urllib2
from datetime import datetime

COMMAND_PATTERN = "nsenter --target %s --mount --uts --ipc --net --pid -- %s"


def execute(command):
    p = subprocess.Popen(command, shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    p.wait()
    return p.returncode, p.stdout.read(), p.stderr.read()


def docker_command(pid, command):
    _, result, _ = execute(COMMAND_PATTERN % (pid, command))
    return result.strip()


def get_instance_ids():
    return [ele['Id'] for ele in json.loads(urllib2.urlopen('http://0.0.0.0:8090/containers/ps').read())]


def instance_inspect(instance_id):
    result = urllib2.urlopen("http://0.0.0.0:8090/containers/%s/json" % instance_id).read()
    return json.loads(result)


def instance_metric(instance_id):
    result = urllib2.urlopen("http://0.0.0.0:8090/containers/%s/metric" % instance_id).read()
    return json.loads(result)


def get_hostname(inspect_info):
    return inspect_info['Config']['Hostname']


def get_ip(inspect_info):
    return inspect_info['NetworkSettings']['IPAddress']


def get_created(inspect_info):
    created_time = datetime.strptime(re.sub(r'\..*Z', '', inspect_info['Created']), '%Y-%m-%dT%H:%M:%S')
    current_time = datetime.now()
    return int((current_time - created_time).total_seconds() - 8 * 3600)


def get_name(inspect_info):
    return re.sub(r'(^/|_.+$)', '', inspect_info['Name'])


def get_cpu_usage(metric_info):
    if 'current_usage' in metric_info['cpu_stats']['cpu_usage']:
        return metric_info['cpu_stats']['cpu_usage']['current_usage']
    return 0


def get_network_info(pid, network_name):
    flow = docker_command(pid, 'ifconfig %s' % network_name)

    total_error, total_dropped, total_collision = [0] * 3
    m = re.search(r'RX bytes:\s*(\d+).*?TX bytes:\s*(\d+)', flow, re.IGNORECASE)

    errors = re.findall(r'errors:\s*(\d+)', flow)
    drops = re.findall(r'dropped:\s*(\d+)', flow)
    collisions = re.findall(r'collisions:\s*(\d+)', flow)

    for error in errors:
        total_error += int(error)

    for drop in drops:
        total_dropped += int(drop)

    for collision in collisions:
        total_collision += int(collision)

    rx, tx = (m.group(1), m.group(2)) if m else (0, 0)

    return rx, tx, total_error, total_dropped, total_collision


def get_container_info(pid):
    disk_usage = docker_command(pid, 'df -h | grep "rootfs"')
    disk_usage = re.split(r'\s+', disk_usage.strip())[-2][:-1]
    disk_usage = int(disk_usage) * 1.0 / 100

    eth0_rx, eth0_tx, eth0_errors, eth0_dropped, eth0_collision = get_network_info(pid, 'eth0')
    lo_rx, lo_tx, lo_errors, lo_dropped, lo_collision = get_network_info(pid, 'lo')

    return disk_usage, eth0_rx, eth0_tx, eth0_errors, eth0_dropped, eth0_collision, \
           lo_rx, lo_tx, lo_errors, lo_dropped, lo_collision


def get_swap_usage(metric_info):
    return metric_info['memory_stats']['stats']['swap']


def get_memory_info(metric_info, instance_id):
    used, cached = metric_info['memory_stats']['stats']['rss'], metric_info['memory_stats']['stats']['cache']
    shared, buffered = [0, 0]
    total = -1
    try:
        result = json.loads(post('http://localhost:8090/containers/%s/cgroup' % instance_id,
                                 json.dumps({"ReadSubsystem": ["memory.limit_in_bytes"]}),
                                 {'Content-Type': 'application/json'}))

        if result[0]['Status'] == 0:
            total = result[0]['Out']
    except Exception, e:
        pass

    free = int(total) - int(used) - int(cached)

    return total, used, cached, free, shared, buffered


def get_process_info(pid):
    result = docker_command(pid, "top -b -n1 | grep Tasks | awk '{print $2,$4}'")
    return re.split(r'\s+', result.strip())


def get_number_of_user_connected(pid):
    result = docker_command(pid, "who | awk '{ print $1 }' | sort | uniq | wc -l")
    return result.strip()


def get_tcp_established(pid):
    result = docker_command(pid, 'netstat -anot | grep ESTABLISHED | wc -l')
    return result.strip()


def get_inodes_info(pid):
    result = docker_command(pid, "df -i | grep rootfs | awk '{print $2,$4}'")
    return re.split(r'\s+', result.strip())


def post(url, data, headers={}):
    req = urllib2.Request(url, headers=headers)
    opener = urllib2.build_opener()
    response = opener.open(req, data)
    return response.read()


def get_all_info(current_instance=None):
    instance_ids = get_instance_ids()
    for instance_id in instance_ids:
        if not instance_id:
            continue

        if current_instance and not instance_id.startswith(current_instance):
            continue

        inspect_info = instance_inspect(instance_id)
        metric_info = instance_metric(instance_id)
        pid = inspect_info['State']['Pid']

        disk_usage, eth0_rx, eth0_tx, eth0_errors, eth0_dropped, eth0_collision, lo_rx, lo_tx, \
        lo_errors, lo_dropped, lo_collision = get_container_info(pid)
        ip = get_ip(inspect_info)
        mem_total, mem_used, mem_cached, mem_free, mem_shared, mem_buffered = get_memory_info(metric_info, instance_id)
        process_total, process_running = get_process_info(pid)
        number_of_user_connected = get_number_of_user_connected(pid)
        tcp_established_num = get_tcp_established(pid)
        inodes_total, inodes_free = get_inodes_info(pid)

        m = [
            ('domain', '', get_name(inspect_info)),
            ('system_cpuUsage', 'avg', '%.3f' % (float(get_cpu_usage(metric_info)))),
            ('system_cachedMem', 'avg', mem_cached),
            ('system_totalMem', 'avg', mem_total),
            ('system_usedMem', 'avg', mem_used),
            ('system_freeMem', 'avg', mem_free),
            ('system_sharedMem', 'avg', mem_shared),
            ('system_buffersMem', 'avg', mem_buffered),
            ('system_/-usage', 'avg', disk_usage),
            ('system_swapUsage', 'avg', get_swap_usage(metric_info)),
            ('system_eth0-outFlow', 'sum', eth0_tx),
            ('system_eth0-inFlow', 'sum', eth0_rx),
            ('system_eth0-dropped', 'sum', eth0_dropped),
            ('system_eth0-errors', 'sum', eth0_errors),
            ('system_eth0-collisions', 'sum', eth0_collision),
            ('system_lo-outFlow', 'sum', lo_tx),
            ('system_lo-inFlow', 'sum', lo_rx),
            ('system_totalProcess', 'avg', process_total),
            ('system_runningProcess', 'avg', process_running),
            ('system_establishedTcp', 'avg', tcp_established_num),
            ('system_loginUsers', 'avg', number_of_user_connected),
            ('system_/-freeInodes', 'avg', "%.3f" % (float(inodes_free) / int(inodes_total))),

        ]

        print '\n'.join(['%s_%s%s=%s' % (k, ip, t and ':' + t, v) for k, t, v in m])


if __name__ == '__main__':
    import sys

    instance_id = None
    if len(sys.argv) > 1:
        instance_id = sys.argv[1]

        if instance_id == 'instance_ids':
            print '\n'.join(get_instance_ids())
            exit(0)

    get_all_info(instance_id)
