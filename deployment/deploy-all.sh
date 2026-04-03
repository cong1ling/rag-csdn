#!/bin/bash

# 一键部署脚本（前后端）
# 使用方法：chmod +x deploy-all.sh && ./deploy-all.sh

set -e

echo "=========================================="
echo "RAG-Bilibili 一键部署"
echo "=========================================="

# 询问部署选项
echo ""
echo "请选择部署选项："
echo "1) 仅部署后端"
echo "2) 仅部署前端"
echo "3) 部署前后端"
read -p "请输入选项 (1-3): " choice

case $choice in
    1)
        echo ""
        echo "开始部署后端..."
        ./deployment/deploy-backend.sh
        ;;
    2)
        echo ""
        echo "开始部署前端..."
        ./deployment/deploy-frontend.sh
        ;;
    3)
        echo ""
        echo "开始部署后端..."
        ./deployment/deploy-backend.sh

        echo ""
        echo "开始部署前端..."
        ./deployment/deploy-frontend.sh
        ;;
    *)
        echo "无效的选项"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "所有部署任务完成！"
echo "=========================================="
