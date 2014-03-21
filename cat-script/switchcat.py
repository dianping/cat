#!/usr/bin/env python
#encoding=utf-8

import subprocess
import time
import urllib2

monitor = [{'group':'switch',
            'type':'cisco',
            'public':'dianpingP@ssword',
            'version':'2c',
            'ip':'172.25.21.88',
            'port':[10101,10102,10601,10602]
          }]

CISCO_IN_TRAFFIC_OID = '.1.3.6.1.2.1.2.2.1.10.'
CISCO_IN_COUNT_OID = '.1.3.6.1.2.1.2.2.1.11.'
CISCO_OUT_TRAFFIC_OID = '.1.3.6.1.2.1.2.2.1.16.'
CISCO_OUT_COUNT_OID = '.1.3.6.1.2.1.2.2.1.17.'
CISCO_NAME_OID = '.1.3.6.1.2.1.1.5'
CISCO_PORT_NAME_OID = '.1.3.6.1.2.1.2.2.1.2.'
SLEEP_TIME = 8 #s

SERVER = '10.128.120.38:2281'

if __name__ == '__main__':
    while True:
        try:
            for unit in monitor:
                if not unit.get('name'):
                    try:
                        cmd = 'snmpwalk -c '+unit['public']+' -v '+unit['version']+' '+unit['ip']+' '+CISCO_NAME_OID
                        p = subprocess.Popen(cmd,stdout=subprocess.PIPE,shell=True)
                        p.wait()
                        unit['name'] = p.stdout.read().split(': ')[1].strip()
                    except Exception, e:
                        print str(e)
                        unit['name'] = 'unknown'
                cmd = 'snmpget -c '+unit['public']+' -v '+unit['version']+' '+unit['ip']+' '
                if unit['type'] == 'cisco':
                    in_traffic_oid = CISCO_IN_TRAFFIC_OID
                    out_traffic_oid = CISCO_OUT_TRAFFIC_OID
                    in_count_oid = CISCO_IN_COUNT_OID
                    out_count_oid = CISCO_OUT_COUNT_OID
                else:
                    print 'The unit ' + unit['name'] + '\'s type can not resolved!! [' + unit['type'] + ']'
                    continue
                if not unit.get('portname'):
                    try:
                        cmd = 'snmpget -c '+unit['public']+' -v '+unit['version']+' '+unit['ip']+' '
                        if unit['type'] == 'cisco':
                            for p in unit['port']:
                                cmd += CISCO_PORT_NAME_OID + str(p) + ' '
                        else:
                            raise Exception('unknown device type!')
                        p = subprocess.Popen(cmd,stdout=subprocess.PIPE,shell=True)
                        p.wait()
                        unit['portname'] = p.stdout.read().split(': ')[1].strip()
                    except Exception, e:
                        print str(e)
                        unit['portname'] = unit['port']
                for port in unit['port']:
                    if not port:
                        continue
                    cmd += in_traffic_oid + str(port) + ' '
                    cmd += out_traffic_oid + str(port) + ' '
                    cmd += in_count_oid + str(port) + ' '
                    cmd += out_count_oid + str(port) + ' '
                p = subprocess.Popen(cmd,stdout=subprocess.PIPE,shell=True)
                p.wait()
                result = [r.split(': ')[1] for r in p.stdout.read().split('\n') if r]
                if not unit.get('last'):
                    unit['last'] = [int(r) for r in result]
                else:
                    now = [int(r) for r in result]
                    diff = []
                    i = 0
                    while i < len(now):
                        d = now[i]-unit['last'][i]
                        if d < 0:
                            d += 4294967296
                        diff.append(d)
                        i += 1
                    unit['last'] = now
                    print unit['last']
                    i = 0
                    while i < len(unit['port']):
                        try:
                            #in
                            print 'http://'+SERVER+'/cat/r/systemMonitor?group='+unit['group']+'&domain='+ \
                                    unit['name']+'&key='+str(unit['portname'][i])+'-in&op=sum&sum='+str(diff[::4][i])
                            #urllib2.urlopen('http://'+SERVER+'/cat/r/systemMonitor?group='+unit['group']+'&domain='+
                            #            unit['name']+'&key='+unit['name']+'-'+group['key']+'/in-traffic'+'&op=sum&sum='+str(data),timeout=0)
                            #out
                            print 'http://'+SERVER+'/cat/r/systemMonitor?group='+unit['group']+'&domain='+ \
                                    unit['name']+'&key='+str(unit['portname'][i])+'-in&op=sum&sum='+str(diff[1::4][i])
                            #urllib2.urlopen('http://'+SERVER+'/cat/r/systemMonitor?group='+unit['group']+'&domain='+
                            #            unit['name']+'&key='+unit['name']+'-'+group['key']+'/in-traffic'+'&op=sum&sum='+str(data),timeout=0)
                        except Exception, e:
                            print str(e)
                        i += 1
        except Exception, e:
            print 'exception!!! ' + str(e)

        time.sleep(SLEEP_TIME)
