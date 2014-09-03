#!/bin/sh

# created on 2013-06-18
#yum install -y  zlib-devel openssl-devel perl cpio expat-devel gettext-devel crul-devl autoconf  perl-devel
#wget http://www.codemonkey.org.uk/projects/git-snapshots/git/git-latest.tar.gz
# tar -zxf git-1.7.2.2.tar.gz
# cd git-1.7.2.2
# make prefix=/usr/local all
# sudo make prefix=/usr/local install

source /root/.bash_profile

GIT_REPOSITORY=git@git.ufo.sogou-inc.com:passport/session-server.git
GIT_HOME=/search/build/passport-session.git-deploment
BUILD_HOME=/search/build/passport-session.build-deploment

UNPACK_HOME=/search/build/passport-session.`date '+%Y%m%d%H%M%S'`
TARGET_HOME=/search/passport/webapp
BRANCH=master

if [ ! -d "/search/build" ];then
        mkdir /search/build
fi

if [ ! -d "$GIT_HOME" ];then
        mkdir $GIT_HOME
fi

if [ ! -d "$BUILD_HOME" ];then
        mkdir $BUILD_HOME
fi

if [ ! -d "$TARGET_HOME" ];then
        mkdir $TARGET_HOME
fi


NUMBER=$#
i=1
while [[ $i -le $NUMBER ]]
do                        #将数组a[i]赋值为$1,即取得到第一个参数并将值存入a[1]
	if [[ $1  = "-b" ]]; then
		((i++))                       #数组后移一位,变为a[2]
		shift
		NUMBER=$NUMBER-1                    #使用shift命令将参数后移一位,即此时的$1为第二个参数
		BRANCH=$1
	fi
	((i++))                       #数组后移一位,变为a[2]
	shift                     #使用shift命令将参数后移一位,即此时的$1为第二个参数
done

echo '部署分支:'${BRANCH}

sleep 1

# update git & copy to build_home
rm -rf $GIT_HOME
git clone $GIT_REPOSITORY $GIT_HOME --depth 3
cd $GIT_HOME
git fetch
git checkout -b $BRANCH   origin/$BRANCH
read tag <<<  $(cd $GIT_HOME;git rev-parse HEAD)
rsync -az --delete $GIT_HOME/* $BUILD_HOME/


# mvn build & unpack to unpack_home
cd $BUILD_HOME
MVN=/usr/local/maven/bin/mvn
$MVN clean install -Dmaven.test.skip=true -Pprod


UNPACK_HOME=/search/build/passport-session.`date '+%Y%m%d'`.${tag}
mkdir -p $UNPACK_HOME
cp -r $BUILD_HOME/passport-session-web/target/passport-session-web/*  $UNPACK_HOME/

if [ ! -d "$UNPACK_HOME" ];then
        mkdir $UNPACK_HOME
fi

rm -rf $TARGET_HOME
ln -s $UNPACK_HOME  $TARGET_HOME


# restart resin
/usr/local/resin/bin/resin.sh stop
sleep 1
/usr/local/resin/bin/resin.sh start