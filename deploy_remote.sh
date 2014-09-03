#!/bin/sh

#要上线的分支
BRANCH=master
#要上线的机器

#HOST_LIST="10.136.55.189 10.136.55.188 10.136.30.67 10.136.26.54 10.136.131.77 10.136.131.76"
#HOST_LIST="10.136.24.115 10.136.24.116 10.136.24.117"

DEPLEMENT_SH="/search/script/deploment/deployment_online_session_server/session-duildCode-Deploment.sh"
DEPLEMENT_CMD="sh $DEPLEMENT_SH -b ${BRANCH}"

for loop in $HOST_LIST
do
    echo "按下任何键开始在$loop 上部署 $BRANCH 分支"
    read
    ssh root@$loop "mkdir -p /search/script/deploment/deployment_online_session_server/"
    ssh root@$loop "rm ${DEPLEMENT_SH}"
    scp $DEPLEMENT_SH  root@$loop:$DEPLEMENT_SH
    ssh root@$loop "${DEPLEMENT_CMD}"
    echo "在 $loop 部署 $BRANCH 结束"
done