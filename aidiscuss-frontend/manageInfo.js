// manageInfo.js

// 页面加载完成后自动调用获取管理信息的函数
window.onload = function() {
    getManageInfo();
};

// 获取管理信息
function getManageInfo() {
    // 发送GET请求到指定的URL
    fetch('http://127.0.0.1:10002/getManageInfo')
        .then(response => response.json()) // 将响应转换为JSON格式
        .then(data => {
            console.log(data);
            // 获取discusses数组
            const discusses = data.discusses;

            // 获取下拉列表元素
            const discussList = document.getElementById('discussList');

            // 遍历discusses数组，为每个元素创建一个下拉选项
            discusses.forEach(discuss => {
                const option = document.createElement('option');
                option.value = discuss.redisId;
                option.text = discuss.discussName;
                discussList.appendChild(option);
            });
        })
        .catch(error => {
            console.error('获取管理信息失败:', error);
        });
}