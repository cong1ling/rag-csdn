#!/bin/bash

# 数据库备份脚本
# 使用方法：chmod +x backup-db.sh && ./backup-db.sh

# 配置
DB_USER="raguser"
DB_PASS="your_password"
DB_NAME="rag_bilibili"
BACKUP_DIR="/home/ubuntu/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_${DATE}.sql"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
echo "开始备份数据库 $DB_NAME..."
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_FILE

# 检查备份是否成功
if [ $? -eq 0 ]; then
    echo "备份成功: $BACKUP_FILE"

    # 压缩备份文件
    gzip $BACKUP_FILE
    echo "已压缩: ${BACKUP_FILE}.gz"

    # 删除 7 天前的备份
    find $BACKUP_DIR -name "${DB_NAME}_*.sql.gz" -mtime +7 -delete
    echo "已清理 7 天前的旧备份"
else
    echo "备份失败!"
    exit 1
fi

# 显示当前备份列表
echo "当前备份文件:"
ls -lh $BACKUP_DIR/${DB_NAME}_*.sql.gz
