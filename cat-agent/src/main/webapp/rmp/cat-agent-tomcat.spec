#%define		name	value
%define __jar_repack 0


Name:		cat-agent-tomcat
Version:	1.0.5
Release:	1
Summary:	cat-agent-tomcat
Requires:	git
Requires(pre): /usr/sbin/useradd, /usr/bin/getent, /usr/sbin/usermod

Group:		Development/Tools
License:	GPL
Source0:	%{name}-%{version}.tar.gz
BuildRoot:	%(mktemp -ud %{_tmppath}/%{name}-%{version}-XXXXXXXXXX)


%description
A powerful customized J2EE web container (JBoss, Jetty, Tomcat)

%pre
# add user cat
/usr/bin/getent passwd cat || /usr/sbin/useradd -u 2200 cat
/usr/sbin/usermod -a -G nobody cat || true

%prep
%setup -q


%build


%install
[ -d $RPM_BUILD_ROOT ] && rm -rf $RPM_BUILD_ROOT/*

# where to install agent files
AGENT_INSTALL_DIR=$RPM_BUILD_ROOT/data/webapps/cat/cat-agent


# create agent directories
[ -d $AGENT_INSTALL_DIR ] || mkdir -p $AGENT_INSTALL_DIR

# copy agent files to corresponding directories
cp -r cat-agent/* $AGENT_INSTALL_DIR/
chmod +x $AGENT_INSTALL_DIR/startup.sh
/bin/bash $AGENT_INSTALL_DIR/startup.sh 


%post
# change required file permissions

APPLOGS_DIR=/data/applogs
APPDATAS_DIR=/data/appdatas
CAT_ROOT_DIR=/data/webapps/cat/
[ -d $APPLOGS_DIR ] && chown nobody:nobody $APPLOGS_DIR && chmod 775 $APPLOGS_DIR
[ -d $APPDATAS_DIR ] && chown nobody:nobody $APPDATAS_DIR && chmod 775 $APPDATAS_DIR
[ -d $CAT_ROOT_DIR ] && chown -R cat:cat $CAT_ROOT_DIR

# comment out Defaults requiretty to enabel sudo in scripts
awk 'BEGIN{result=""}{if(match($0, "^[^#]*Defaults[[:space:]]+requiretty")>0){result=sprintf("%s#%s\n",result,$0);}else{result=sprintf("%s%s\n",result,$0);}}END{print result > "/etc/sudoers"}' /etc/sudoers

AGENT_INSTALL_DIR=$RPM_BUILD_ROOT/data/webapps/cat/cat-agent
/bin/bash $AGENT_INSTALL_DIR/startup.sh

%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,cat,cat,-)
/data/webapps/cat/cat-agent
%doc


%changelog


%preun
/usr/local/jdk/bin/jps -lvm | awk '$2=="com.dianping.cat.agent.monitor.CatAgent"{cmd=sprintf("kill -9 %s", $1);system(cmd)}'


%postun
/usr/local/jdk/bin/jps -lvm | awk '$2=="com.dianping.cat.agent.monitor.CatAgent"{cmd=sprintf("kill -9 %s", $1);system(cmd)}'
rm -rf /data/webapps/cat/cat-agent
