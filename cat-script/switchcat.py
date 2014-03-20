#!/usr/bin/env python
#encoding=utf-8

import subprocess
import time
import urllib2

monitor = [{'name':'ctc',
            'type':'cisco',
            'public':'dianpingP@ssword',
            'version':'2c',
            'ip':'172.25.21.88',
            'group':[{
                'key':'to-outer',
                'port':[10101,10102,10601,10602],
                'catch':0b1111
            }]
          }]

CISCO_IN_TRAFFIC_OID = '.1.3.6.1.2.1.2.2.1.10.'
CISCO_IN_COUNT_OID = '.1.3.6.1.2.1.2.2.1.11.'
CISCO_OUT_TRAFFIC_OID = '.1.3.6.1.2.1.2.2.1.16.'
CISCO_OUT_COUNT_OID = '.1.3.6.1.2.1.2.2.1.17.'
SLEEP_TIME = 8 #s
SNMP_CMD = 'snmpget'

SERVER = '10.128.120.38:2281'
DATA_GROUP = 'TestGroup'
DATA_DOMAIN = 'net'

if __name__ == '__main__':
    while True:
        try:
            for unit in monitor:
                cmdpre = SNMP_CMD+' -c '+unit['public']+' -v '+unit['version']+' '+unit['ip']+' '
                if unit['type'] == 'cisco':
                    in_traffic_oid = CISCO_IN_TRAFFIC_OID
                    out_traffic_oid = CISCO_OUT_TRAFFIC_OID
                    in_count_oid = CISCO_IN_COUNT_OID
                    out_count_oid = CISCO_OUT_COUNT_OID
                else:
                    print 'The unit ' + unit['name'] + '\'s type can not resolved!! [' + unit['type'] + ']'
                    continue
                for group in unit['group']:
                    if not group:
                        continue
                    cmd = cmdpre
                    for port in group['port']:
                        cmd += in_traffic_oid + str(port) + ' '
                        cmd += out_traffic_oid + str(port) + ' '
                        cmd += in_count_oid + str(port) + ' '
                        cmd += out_count_oid + str(port) + ' '
                    p = subprocess.Popen(cmd,stdout=subprocess.PIPE,shell=True)
                    p.wait()
                    if not group.get('last'):
                        group['last'] = [int(r.split(': ')[1]) for r in p.stdout.read().split('\n') if r]
                    else:
                        now = [int(r.split(': ')[1]) for r in p.stdout.read().split('\n') if r]
                        diff = []
                        i = 0
                        while i < len(now):
                            d = now[i]-group['last'][i]
                            if d < 0:
                                d += 4294967296
                            diff.append(d)
                            i += 1
                        group['last'] = now
                        if group['catch'] & 0b1000:
                            data = sum(diff[::4])
                            print 'in-traffic:' + str(data)
                            try:
                                urllib2.urlopen('http://'+SERVER+'/cat/r/systemMonitor?group='+DATA_GROUP+'&domain='+
                                            DATA_DOMAIN+'&key='+unit['name']+'-'+group['key']+'/in-traffic'+'&op=sum&sum='+str(data),timeout=0)
                            except Exception, e:
                                pass
                        if group['catch'] & 0b0100:
                            data = sum(diff[1::4])
                            print 'out-traffic:' + str(data)
                            try:
                                response = urllib2.urlopen('http://'+SERVER+'/cat/r/systemMonitor?group='+DATA_GROUP+'&domain='+
                                            DATA_DOMAIN+'&key='+unit['name']+'-'+group['key']+'/out-traffic'+'&op=sum&sum='+str(data),timeout=0)
                            except Exception, e:
                                pass
                        if group['catch'] & 0b0010:
                            data = sum(diff[2::4])
                            print 'in-packets:' + str(data)
                            try:
                                response = urllib2.urlopen('http://'+SERVER+'/cat/r/systemMonitor?group='+DATA_GROUP+'&domain='+
                                            DATA_DOMAIN+'&key='+unit['name']+'-'+group['key']+'/in-packets'+'&op=sum&sum='+str(data),timeout=0)
                            except Exception, e:
                                pass
                        if group['catch'] & 0b0001:
                            data = sum(diff[3::4])
                            print 'out-packets:' + str(data)
                            try:
                                response = urllib2.urlopen('http://'+SERVER+'/cat/r/systemMonitor?group='+DATA_GROUP+'&domain='+
                                        DATA_DOMAIN+'&key='+unit['name']+'-'+group['key']+'/out-packets'+'&op=sum&sum='+str(data),timeout=0)
                            except Exception, e:
                                pass
        except Exception, e:
            print 'exception!!!'

        time.sleep(SLEEP_TIME)
