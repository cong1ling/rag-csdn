#!/bin/bash

# RAG-CSDN 前端部署脚本（Cloudflare Pages）
# 使用方法：chmod +x deploy-frontend.sh && ./deploy-frontend.sh

set -e

echo "=========================================="
echo "RAG-CSDN 前端部署脚本"
echo "=========================================="

# 检查是否在项目根目录
if [ ! -d "rag-csdn-front" ]; then
    echo "错误：请在项目根目录运行此脚本"
    exit 1
fi

# 检查是否安装了 wrangler
if ! command -v wrangler &> /dev/null; then
    echo "错误：未安装 wrangler CLI"
    echo "请运行：npm install -g wrangler"
    exit 1
fi

# 检查是否已登录
if ! wrangler whoami &> /dev/null; then
    echo "请先登录 Cloudflare："
    wrangler login
fi

# 1. 安装依赖
echo ""
echo "步骤 1/3: 安装依赖..."
cd rag-csdn-front
npm install
echo "✓ 依赖安装完成"

# 2. 构建项目
echo ""
echo "步骤 2/3: 构建项目..."
npm run build
if [ $? -ne 0 ]; then
    echo "构建失败！"
    exit 1
fi
echo "✓ 构建成功"

# 3. 部署到 Cloudflare Pages
echo ""
echo "步骤 3/3: 部署到 Cloudflare Pages..."
wrangler pages deploy dist --project-name=rag-csdn

echo ""
echo "=========================================="
echo "部署完成！"
echo "=========================================="
echo ""
echo "访问你的应用："
echo "  https://rag-csdn.pages.dev"
echo ""

