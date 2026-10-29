#!/bin/bash
set -euo pipefail          # 任何一步失败立即退出，未定义变量报错

# 定义变量
ZZYL_PATH=/opt/zzyl
ZZYL_PULL_PATH="${ZZYL_PATH}/zzyl-"
OLD_JAR="${ZZYL_PATH}/zzyl2.jar"
BACK_UP="${ZZYL_PATH}/backup"
SERVER_NAME=zzyl@node3
BACK_UP_FILE="${BACK_UP}/$(date +%Y%m%d%H%M%S).jar"

HEALTH_PATH=http://127.0.0.1:9997/actuator/health

# 拉取代码
cd "$ZZYL_PULL_PATH"
git pull

# 构建新.jar
cd qc-retirement
mvn -q -DskipTests clean package
NEW_JAR=$(find qc-service/target -maxdepth 1 -name '*.jar' ! -name '*original*' | head -1)

# 备份旧.jar，最多存三个
mkdir -p $BACK_UP
if [ -f "$OLD_JAR" ];then
	cp $OLD_JAR $BACK_UP_FILE
	ls -1t $BACK_UP 2>/dev/null | tail -n +4 | xargs -r rm -f
fi

# 启新闭旧
mv "$NEW_JAR" "$OLD_JAR"
systemctl daemon-reload
systemctl restart "$SERVER_NAME"


# 健康检查
sleep 5
for i in {1..10};do
	if curl -fs "$HEALTH_PATH" >/dev/null 2>&1;then
		echo "✅ 部署成功：$(date)"
		exit 0
	fi
	sleep 5
done

# 健康检查失败，回滚
if [ ! -f "$BACK_UP_FILE" ]; then
	  echo "❌ 没有可用备份，无法回滚，请人工介入"
	    exit 1
fi

echo "❌ 健康检查失败，回滚到上一版本"
systemctl stop "$SERVER_NAME"
mv $BACK_UP_FILE $OLD_JAR
systemctl start "$SERVER_NAME"
# 健康检查
sleep 5
for i in {1..10};do
	if curl -fs "$HEALTH_PATH" >/dev/null 2>&1;then	              
		echo "✅  部署成功：$(date)"				               
	       	exit 0					
	fi		
	sleep 5
done
echo "❌  回滚失败！！！"
exit 1
