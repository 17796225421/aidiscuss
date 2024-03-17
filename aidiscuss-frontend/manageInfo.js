// 页面加载完成后自动调用获取管理信息的函数
window.onload = function() {
    getManageInfo();
    // 为创建讨论区按钮添加点击事件监听器
    document.getElementById('createDiscussBtn').addEventListener('click', createDiscuss);
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

// 创建讨论区的函数
function createDiscuss() {
    // 发送POST请求到指定的URL，不带discussName
    fetch('http://127.0.0.1:10002/createDiscuss', {
        method: 'POST', // 指定请求方法为POST
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({}) // 此处不需要提供discussName
    })
        .then(response => {
            console.log('讨论区创建成功');
        })
        .catch(error => {
            console.error('创建讨论区失败:', error);
        });
}