// 页面加载完成后自动调用获取管理信息的函数
window.onload = function () {
    getManageInfo();
    // 为创建讨论区按钮添加点击事件监听器
    document.getElementById('createDiscussBtn').addEventListener('click', createDiscuss);
    // 为开始讨论区按钮添加点击事件监听器
    document.getElementById('startDiscussBtn').addEventListener('click', startDiscuss);
    // 为停止讨论区按钮添加点击事件监听器
    document.getElementById('stopDiscussBtn').addEventListener('click', stopDiscuss);
    // 为打开讨论区按钮添加点击事件监听器
    document.getElementById('openDiscussBtn').addEventListener('click', openDiscuss);
    // 为结束讨论区按钮添加点击事件监听器
    document.getElementById('closeDiscussBtn').addEventListener('click', closeDiscuss); // 新增代码
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

            // 清空原有的选项
            discussList.innerHTML = '';

            // 遍历discusses数组,为每个元素创建一个下拉选项
            discusses.forEach(discuss => {
                const discussStatus = discuss.discussStatus;

                console.log(discussStatus);
                let discussStatusText = "";
                if (discussStatus === 0) {
                    discussStatusText = "已创建";
                } else if (discussStatus === 1) {
                    discussStatusText = "已开启";
                } else if (discussStatus === 2) {
                    discussStatusText = "已停止";
                }

                const option = document.createElement('option');
                option.text = discuss.discussName + " " + discussStatusText; // 设置选项的显示文本为discussName
                option.value = discuss.discussId; // 设置选项的value属性为discussId
                discussList.appendChild(option);
            });
        })
        .catch(error => {
            console.error('获取管理信息失败:', error);
        });
}

// 创建讨论区的函数
function createDiscuss() {
    // 发送POST请求到指定的URL,不带discussName
    fetch('http://127.0.0.1:10002/createDiscuss', {
        method: 'POST', // 指定请求方法为POST
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({}) // 此处不需要提供discussName
    })
        .then(response => {
            console.log('讨论区创建成功');
            getManageInfo();
        })
        .catch(error => {
            console.error('创建讨论区失败:', error);
        });
}

// 开始讨论区的函数
function startDiscuss() {
    // 获取当前选中的选项
    const selectedOption = document.getElementById('discussList').value;
    // 从选项的value中获取discussId
    const discussId = selectedOption;
    // 发送POST请求到指定的URL
    fetch('http://127.0.0.1:10002/startDiscuss', {
        method: 'POST', // 指定请求方法为POST
        headers: {
            'Content-Type': 'application/json'
        },
        body: discussId
    })
        .then(response => {
            console.log('开始讨论区成功');
            getManageInfo();
        })
        .catch(error => {
            console.error('开始讨论区失败:', error);
        });
}

// 停止讨论区的函数
function stopDiscuss() {
    // 获取当前选中的选项
    const selectedOption = document.getElementById('discussList').value;
    // 从选项的value中获取discussId
    const discussId = selectedOption;
    // 发送POST请求到指定的URL
    fetch('http://127.0.0.1:10002/stopDiscuss', {
        method: 'POST', // 指定请求方法为POST
        headers: {
            'Content-Type': 'application/json'
        },
        body: discussId
    })
        .then(response => {
            console.log('停止讨论区成功');
            getManageInfo();
        })
        .catch(error => {
            console.error('停止讨论区失败:', error);
        });
}

// 打开讨论区的函数
function openDiscuss() {
    // 获取当前选中的选项
    const selectedOption = document.getElementById('discussList').value;
    // 从选项的value中获取discussId
    const discussId = selectedOption;
    // 打开新的tab页面,URL为aidiscuss-frontend目录下的discuss.html,并传递discussId作为参数
    window.open(`./discuss.html?discussId=${discussId}`);
}

// 结束讨论区的函数
function closeDiscuss() {
    // 获取当前选中的选项
    const selectedOption = document.getElementById('discussList').value;
    // 从选项的value中获取discussId
    const discussId = selectedOption;
    // 发送POST请求到指定的URL以结束讨论区
    fetch('http://127.0.0.1:10002/closeDiscuss', {
        method: 'POST', // 指定请求方法为POST
        headers: {
            'Content-Type': 'application/json'
        },
        body: discussId
    })
        .then(response => {
            console.log('讨论区已结束'); // 可选的成功提示，实际中不需要处理响应
            getManageInfo();
        })
        .catch(error => {
            console.error('结束讨论区失败:', error); // 可选的错误处理
        });
}