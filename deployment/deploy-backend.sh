#!/bin/bash

# RAG-CSDN 后端部署脚本
# 使用方法：chmod +x deploy-backend.sh && ./deploy-backend.sh

set -e

echo "=========================================="
echo "RAG-CSDN 后端部署脚本"
echo "=========================================="

# 配置变量
SERVER_USER="ubuntu"
SERVER_HOST="your-server-ip"
SERVER_PATH="/home/ubuntu/rag-csdn-server"
JAR_NAME="rag-csdn-server-1.0.0.jar"

# 检查是否在项目根目录
if [ ! -d "rag-csdn-server" ]; then
    echo "错误：请在项目根目录运行此脚本"
    exit 1
fi

# 1. 构建项目
echo ""
echo "步骤 1/4: 构建项目..."
cd rag-csdn-server
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "构建失败！"
    exit 1
fi
echo "✓ 构建成功"

# 2. 备份旧版本
echo ""
echo "步骤 2/4: 备份服务器上的旧版本..."
ssh ${SERVER_USER}@${SERVER_HOST} "
    if [ -f ${SERVER_PATH}/${JAR_NAME} ]; then
        cp ${SERVER_PATH}/${JAR_NAME} ${SERVER_PATH}/${JAR_NAME}.backup.\$(date +%Y%m%d_%H%M%S)
        echo '✓ 已备份旧版本'
    else
        echo '✓ 无需备份（首次部署）'
    fi
"

# 3. 上传新版本
echo ""
echo "步骤 3/4: 上传新版本到服务器..."
scp target/${JAR_NAME} ${SERVER_USER}@${SERVER_HOST}:${SERVER_PATH}/
echo "✓ 上传成功"

# 4. 重启服务
echo ""
echo "步骤 4/4: 重启服务..."
ssh ${SERVER_USER}@${SERVER_HOST} "
    sudo systemctl restart rag-csdn
    sleep 3
    sudo systemctl status rag-csdn --no-pager
"

echo ""
echo "=========================================="
echo "部署完成！"
echo "=========================================="
echo ""
echo "查看日志："
echo "  ssh ${SERVER_USER}@${SERVER_HOST} 'sudo journalctl -u rag-csdn -f'"
echo ""
echo "检查服务状态："
echo "  ssh ${SERVER_USER}@${SERVER_HOST} 'sudo systemctl status rag-csdn'"
echo ""

