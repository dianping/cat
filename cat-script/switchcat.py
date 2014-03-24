#!/usr/bin/env python
#encoding=utf-8

import subprocess
import time
import urllib2
import logging
from config import *


def init(switchs):
    logging.basicConfig(filename = LOGFILE, level = logging.INFO, \
                        format = '%(asctime)s - %(levelname)s: %(message)s')

    i = 0
    while i < len(switchs):
        if not switchs[i].get('type') or not switchs[i].get('public') or \
                not switchs[i].get('ip') or not switchs[i].get('port'):
            logging.warning('init: switch %s info is not enough.' % str(switchs[i]))
            del switchs[i]
            continue

        if switchs[i]['type'] not in OID.keys():
            logging.warning('init: the switch %s \'s type %s is not support.' % \
                    (str(switchs[i]), type))
            del switchs[i]
            continue

        if not switchs[i].get('version'):
            switchs[i]['version'] = '2c'

        if not switchs[i].get('group'):
            switchs[i]['group'] = 'switch'

        if not switchs[i].get('name'):
            switchs[i]['name'] = get_name(switchs[i]['ip'], switchs[i]['type'], \
                    switchs[i]['public'], switchs[i]['version'])

        switchs[i]['portname']= get_port_name(switchs[i]['ip'], switchs[i]['port'], \
                switchs[i]['type'], switchs[i]['public'], switchs[i]['version'])

        i += 1

    logging.info('init: succesed')


def _get_data(server, oid_array, public, version, cmd='snmpget'):
    cmd = cmd + ' -c %s -v %s %s' %  (public, version, server)
    for oid in oid_array:
        cmd += ' ' + oid

    try:
        p = subprocess.Popen(cmd,stdout=subprocess.PIPE,shell=True)
        p.wait()
        data = [line.split(': ')[1] for line in p.stdout.read().split('\n') if line]
        return data
    except Exception, e:
        logging.warning('_get_data exception: %s' % str(e))

    return None


def get_name(server, type, public, version):
    oid_array = [OID[type]['name']]

    data = _get_data(server, oid_array, public, version, 'snmpwalk')
    if not data:
        logging.warning('get_name error: cannot get server %s \'s name.' % server)
        return server

    return data[0]


def get_port_name(server, ports, type, public, version):
    oid_array = []
    for p in ports:
        oid_array.append('%s%d' % (OID[type]['port_name'], p))

    data = _get_data(server, oid_array, public, version)
    if not data:
        logging.warning('get_port_stat: cannot get server %s \'s port statistics.' % server)
        return [str(p) for p in ports]

    return data


def get_port_stat(server, ports, type, public, version):
    oid_array = []
    for p in ports:
        oid_array.append('%s%d' % (OID[type]['in_traffic'], p))
        oid_array.append('%s%d' % (OID[type]['out_traffic'], p))
        oid_array.append('%s%d' % (OID[type]['in_pkts'], p))
        oid_array.append('%s%d' % (OID[type]['out_pkts'], p))

    data = _get_data(server, oid_array, public, version)
    if not data:
        logging.warning('get_port_stat: cannot get server %s \'s port statistics.' % server)
        return None
    data = [int(d) for d in data]

    return data[::4], data[1::4], data[2::4], data[3::4]


def send_data(group, domain, key, data):
    url = 'http://%s/cat/r/systemMonitor?group=%s&domain=%s&key=%s&op=sum&sum=%d' % \
                (DATA_RECEIVER, group, domain, key, data)
    try:
        urllib2.urlopen(url, timeout=0)
    except Exception, e:
        logging.warning('send_data: %s %s %s %d' % (group, domain, key, data))
        return False
    return True


def netcat(switchs):

    while True:
        for sw in switchs:
            data = get_port_stat(sw['ip'], sw['port'], sw['type'], sw['public'], sw['version'])
            if not data:
                logging.warning('netcat: can not catch switch %s \'s data' % str(sw))
                continue

            last = sw.get('last')
            sw['last'] = data

            if not last:
                continue

            indiff = [d + 2**32 if d < 0 else d for d in map(lambda x: x[0] - x[1], zip(data[0], last[0]))]
            outdiff = [d + 2**32 if d < 0 else d for d in map(lambda x: x[0] - x[1], zip(data[1], last[1]))]

            i = 0
            while i < len(sw['port']):
                send_data(sw['group'], sw['name'], '-'.join([sw['portname'][i],'in']), indiff[i])
                send_data(sw['group'], sw['name'], '-'.join([sw['portname'][i],'out']), outdiff[i])
                i += 1

        time.sleep(INTERVAL_TIME)



if __name__ == '__main__':
    init(switchs)
    netcat(switchs)
