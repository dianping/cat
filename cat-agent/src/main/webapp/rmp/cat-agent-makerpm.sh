# pull cat-agent source and packge to RPM
# yum install rpm-build
# yum install rpmdevtools

set -e
set -u

if [ ! $# -eq 2 ];then
        echo "Usage: makerpm.sh tomcat/jboss version."
        exit 1
fi

container=$1
version=$2

if [ ! "$1" = "tomcat" ] && [ ! "$1" = "jboss" ]; then
	echo "Usage: makerpm.sh tomcat/jboss version."
        echo "Container can only be tomcat or jboss."
        exit 1
fi

CAT_ROOT=~/cat
CAT_DIR=~/cat/cat
TMP_DIR=~/tmp
RPM_SOURCE_NAME=cat-agent-$container-$version
CAT_AGENT_INSTALL_DIR_NAME=cat-agent

mkdir -p $CAT_ROOT
cd $CAT_ROOT
if [ ! -e $CAT_DIR ]; then
        git clone https://github.com/dianping/cat.git
fi
cd -

# pull lastest cat code
cd $CAT_DIR/cat-agent
git pull

# package it
mvn -Dmaven.test.skip clean package
cd -

# prepare agent code
mkdir -p $TMP_DIR
cd $TMP_DIR
rm -rf *
mkdir -p $RPM_SOURCE_NAME/$CAT_AGENT_INSTALL_DIR_NAME
unzip -d $RPM_SOURCE_NAME/$CAT_AGENT_INSTALL_DIR_NAME $CAT_DIR/cat-agent/target/cat-agent-*.war

tar czf $RPM_SOURCE_NAME.tar.gz $RPM_SOURCE_NAME


rpmdev-setuptree
cp $RPM_SOURCE_NAME.tar.gz ~/rpmbuild/SOURCES/
cp $CAT_DIR/cat-agent/src/main/rpm/cat-agent-$container.spec ~/rpmbuild/SPECS/

cd -

rpmbuild -bb ~/rpmbuild/SPECS/cat-agent-$container.spec
