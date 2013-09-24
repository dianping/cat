#!/usr/bin/perl

# (C) Maxim Dounin

# Tests for http proxy module.

###############################################################################

use warnings;
use strict;
use Sys::Mmap;

use Test::More;

BEGIN { use FindBin; chdir($FindBin::Bin); }

use lib 'lib';
use Test::Nginx;

###############################################################################

select STDERR; $| = 1;
select STDOUT; $| = 1;

my $dat = "/data/appdatas/cat/mmap_test.dat";
my $idx = "/data/appdatas/cat/mmap_test.idx";

my $t = Test::Nginx->new()->has(qw/http proxy/)->plan(4);

$t->write_file_expand('nginx.conf', <<'EOF');

%%TEST_GLOBALS%%

daemon         off;

events {
}

processes {
	process send {
		daytime on;
		listen 8890;
		mmap_dat_size 1024;
		mmap_dat  /data/appdatas/cat/mmap_test.dat;
		mmap_idx  /data/appdatas/cat/mmap_test.idx;
	}
}

http {
    %%TEST_GLOBALS_HTTP%%

    server {
	    listen       127.0.0.1:8080;
	    server_name  localhost;

	    location / {
		    Cat on;
		    proxy_pass http://127.0.0.1:8081;
		    proxy_read_timeout 1s;
		    proxy_set_header X-CAT-SOURCE "container";
	    }
    }
}

EOF

$t->run_daemon(\&http_daemon);
$t->run();

###############################################################################

like(http_get('/'), qr/SEE-THIS/, 'proxy request');
like(http_get('/multi'), qr/AND-THIS/, 'proxy request with multiple packets');

unlike(http_head('/'), qr/SEE-THIS/, 'proxy head request');
sleep(1);

like(file_exist(), qr/1/, 'file exist');


###############################################################################

sub http_daemon {
	my $server = IO::Socket::INET->new(
		Proto => 'tcp',
		LocalHost => '127.0.0.1:8081',
		Listen => 5,
		Reuse => 1
	)
		or die "Can't create listening socket: $!\n";

	while (my $client = $server->accept()) {
		$client->autoflush(1);

		my $headers = '';
		my $uri = '';

		while (<$client>) {
			$headers .= $_;
			last if (/^\x0d?\x0a?$/);
		}

		$uri = $1 if $headers =~ /^\S+\s+([^ ]+)\s+HTTP/i;

		if ($uri eq '/') {
			print $client <<'EOF';
HTTP/1.1 200 OK
Connection: close

EOF
			print $client "TEST-OK-IF-YOU-SEE-THIS"
			unless $headers =~ /^HEAD/i;

		} elsif ($uri eq '/multi') {

			print $client <<"EOF";
HTTP/1.1 200 OK
Connection: close

TEST-OK-IF-YOU-SEE-THIS
EOF

			select undef, undef, undef, 0.1;
			print $client 'AND-THIS';

		} else {

			print $client <<"EOF";
HTTP/1.1 404 Not Found
Connection: close

Oops, '$uri' not found
EOF
		}

		close $client;
	}
}

sub file_exist {
	my $foo;
	my @tags;
	open(FILE1, $dat);
	mmap($foo, 0, PROT_READ, MAP_SHARED, FILE1) or die "mmap: $!";
	@tags = $foo =~ /([^\n\t]+)/g;
	if (scalar(@tags) != 27){
		system("rm $dat");
		system("rm $idx");
		return 0;
	}
	
	munmap($foo) or die "munmap: $!";
	close(FILE1);
	if (-e $dat && -e $idx) {
		system("rm $dat");
		system("rm $idx");
		return 1;
	}
	return 0;
}

###############################################################################
