// 从URL中获取discussId参数
function getDiscussIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('discussId');
}

// 发送GET请求获取讨论主题信息
function fetchDiscussInfo(discussId) {
    fetch(`http://127.0.0.1:10002/getDiscuss/${discussId}`)
        .then(response => response.json())
        .then(data => {

            // 创建DiscussInfo对象
            const discussInfo = new DiscussInfo(data.discussId, data.discussName, data.discussStatus);

            // 设置discussTitle为discussName
            document.getElementById('discussTitle').textContent = discussInfo.discussName;

        })
        .catch(error => {
            console.error('获取讨论主题信息失败:', error);
        });
}


// 添加选择条的事件监听
document.addEventListener('DOMContentLoaded', () => {
    const discussId = getDiscussIdFromUrl();
    if (discussId) {
        fetchDiscussInfo(discussId);
        sentenceListConnection(discussId);
    } else {
        console.error('缺少discussId参数');
    }

});

function sentenceListConnection(discussId) {
    const socket = new SockJS('http://127.0.0.1:10002/ws');
    const stompClient = Stomp.over(socket); // 使用stomp协议

    let isWaitingToSend = false; // 标记是否正在等待发送请求

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        sendRequest(); // 初始发送请求

        stompClient.subscribe(`/topic/sentenceListConnection/${discussId}`, function (message) {
            console.log('收到sentenceList:', message.body);

            // 检查message.body是否为空或者空字符串
            if (message.body && message.body.trim() !== '') {
                // 如果message.body不为空，才执行以下逻辑

                // 将收到的消息体转换为JSON对象
                const sentenceList = JSON.parse(message.body);

                // 将数据处理成指定格式的字符串
                const displayText = sentenceList.map(item => item.text).join('\n');
                document.getElementById('sentenceListDisplay').textContent = displayText;
            }

            if (!isWaitingToSend) {
                isWaitingToSend = true;
                setTimeout(sendRequest, 1000); // 1秒后发送请求
            }
        });
    });

    function sendRequest() {
        stompClient.send(`/app/sentenceListConnection/${discussId}`, {}, JSON.stringify({/* 消息内容 */}));
        isWaitingToSend = false;
    }
}