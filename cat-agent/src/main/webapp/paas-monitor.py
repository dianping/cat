#!/usr/bin/python
# -*- coding: utf-8 -*-
import subprocess
import json
import re
import urllib2
from datetime import datetime


def execute(command):
    p = subprocess.Popen(command, shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    p.wait()
    return p.returncode, p.stdout.read(), p.stderr.read()


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
    return re.sub(r'(^/|_\d+$)', '', inspect_info['Name'])


def get_cpu_usage(metric_info):
    if 'current_usage' in metric_info['cpu_stats']['cpu_usage']:
        return metric_info['cpu_stats']['cpu_usage']['current_usage']
    return 0


def get_container_info(inspect_info):
    pid = inspect_info['State']['Pid']
    command_pattern = "nsenter --target %s --mount --uts --ipc --net --pid -- ${command}" % pid
    _, disk_usage, _ = execute(command_pattern.replace('${command}', 'df -h | grep "rootfs"'))
    disk_usage = re.split(r'\s+', disk_usage.strip())[-2][:-1]
    disk_usage = int(disk_usage) * 1.0 / 100

    _, ssh_md5, _ = execute(command_pattern.replace('${command}', 'md5sum /usr/sbin/sshd'))
    ssh_md5 = re.split(r'\s+', ssh_md5.strip())[0]

    _, flow, _ = execute(command_pattern.replace('${command}', 'ifconfig eth0 | grep "RX bytes"'))

    rx = 0
    tx = 0
    m = re.search(r'RX bytes:\s*(\d+).*?TX bytes:\s*(\d+)', flow, re.IGNORECASE)

    if m:
        rx = m.group(1)
        tx = m.group(2)

    return disk_usage, ssh_md5, rx, tx


def get_swap_usage(metric_info):
    return metric_info['memory_stats']['stats']['swap']


def get_all_info(current_instance=None):
    instance_ids = get_instance_ids()
    for instance_id in instance_ids:
        if current_instance and not instance_id.startswith(current_instance):
            continue

        if not instance_id:
            continue
        inspect_info = instance_inspect(instance_id)
        metric_info = instance_metric(instance_id)
        disk_usage, ssh_md5, rx, tx = get_container_info(inspect_info)
        ip = get_ip(inspect_info)

        m = [
            ('domain', '', get_name(inspect_info)),
            ('system_cpu', 'avg', '%.3f' % (float(get_cpu_usage(metric_info)))),
            ('system_/-usage', 'avg', disk_usage),
            ('system_swap', 'avg', get_swap_usage(metric_info)),
            ('system_md5Change', 'avg', ssh_md5),
            ('system_uptime', 'avg', get_created(inspect_info)),
            ('system_eth0-out-flow', 'sum', tx),
            ('system_eth0-in-flow', 'sum', rx)
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
