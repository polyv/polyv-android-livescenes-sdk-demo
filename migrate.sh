#!/bin/bash

# 配置变量
SOURCE_BRANCH="master"
TARGET_BRANCH="androidx"
TEMP_DIR="../project_androidx_temp_bak"
GRADLE_FILE="./polyvLiveCommonModul/build.gradle"

echo "🚀 开始执行 AndroidX 强行迁移合并流程..."

# --- 第一阶段：在 Master 完成转换 ---
git checkout $SOURCE_BRANCH
git pull origin $SOURCE_BRANCH

# --- 新增：自动升级 Glide 依赖 ---
echo "🛠️ 正在自动升级 Glide 依赖版本..."

if [ -f "$GRADLE_FILE" ]; then
    # 使用 sed 替换版本号：4.7.1 -> 4.10.0
    # 修改 okhttp3-integration
    sed -i '' 's/com.github.bumptech.glide:okhttp3-integration:4.7.1/com.github.bumptech.glide:okhttp3-integration:4.10.0/g' "$GRADLE_FILE"
    # 修改 compiler
    sed -i '' 's/com.github.bumptech.glide:compiler:4.7.1/com.github.bumptech.glide:compiler:4.10.0/g' "$GRADLE_FILE"
    
    echo "✅ Glide 依赖已尝试更新至 4.10.0"
else
    echo "⚠️ 警告：未找到文件 $GRADLE_FILE，跳过版本修改。"
fi
# -----------------------------

echo "----------------------------------------------------------------"
echo "👉 请在 Android Studio 执行: Refactor > Migrate to AndroidX"
echo "----------------------------------------------------------------"
read -p "⌛ 转换完成后，请按 [Enter] 键继续脚本..."

# 1. 物理备份（此时 master 有大量 modified 文件）
echo "📦 正在备份重构后的文件..."
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"
# 注意：使用 . 确保包含隐藏文件，排除 .git
cp -R . "$TEMP_DIR"
rm -rf "$TEMP_DIR/.git"

# 2. 重置 master 的状态，否则无法 checkout
echo "🧹 正在重置 $SOURCE_BRANCH 状态以便切换分支..."
git add .
git reset --hard HEAD

# --- 第二阶段：准备目标分支 ---
echo "🌿 切换到 $TARGET_BRANCH 并同步远程..."
git fetch origin

if git rev-parse --verify $TARGET_BRANCH >/dev/null 2>&1; then
    git checkout $TARGET_BRANCH
    if git rev-parse --verify origin/$TARGET_BRANCH >/dev/null 2>&1; then
        echo "🔄 强制对齐远程 origin/$TARGET_BRANCH..."
        git reset --hard origin/$TARGET_BRANCH
    fi
elif git rev-parse --verify origin/$TARGET_BRANCH >/dev/null 2>&1; then
    git checkout -b $TARGET_BRANCH origin/$TARGET_BRANCH
else
    git checkout -b $TARGET_BRANCH
fi

# --- 第三阶段：强行覆盖合并 ---
echo "🤝 执行形式合并..."
# 先进行一次 merge 建立关系
git merge $SOURCE_BRANCH --no-edit -X theirs || echo "通过物理覆盖解决冲突..."

# 物理清理并还原
rm -rf [!.]*
cp -R "$TEMP_DIR"/* .

# --- 第四阶段：提交 ---
echo "📝 执行提交..."
git add .

if [ "$(git rev-list $SOURCE_BRANCH..HEAD --count)" -gt 0 ]; then
    echo "🔄 Amend Commit..."
    git commit --amend --no-edit
else
    echo "🆕 新建迁移提交..."
    git commit -m "chore: migrate project to AndroidX"
fi

# 清理备份
rm -rf "$TEMP_DIR"

echo "✅ 流程成功完成！"