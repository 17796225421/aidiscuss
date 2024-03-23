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
            // 修正：从data.micSwitchInfo中提取麦克风信息来创建MicSwitchInfo对象
            const micSwitchInfo = new MicSwitchInfo(
                data.micSwitchInfo.externMic,
                data.micSwitchInfo.wireMic,
                data.micSwitchInfo.virtualMic);

            // 创建DiscussInfo对象
            const discussInfo = new DiscussInfo(data.discussId, data.discussName, micSwitchInfo);

            // 设置discussTitle为discussName
            document.getElementById('discussTitle').textContent = discussInfo.discussName;

            // 根据MicSwitchInfo设置micOptions的选中项
            setMicOptions(micSwitchInfo);
        })
        .catch(error => {
            console.error('获取讨论主题信息失败:', error);
        });
}

// 根据MicSwitchInfo设置micOptions的选中项
function setMicOptions(micSwitchInfo) {
    const micOptions = document.getElementById('micOptions');
    if (micSwitchInfo.externMic) {
        micOptions.value = '外挂麦克风';
    } else if (micSwitchInfo.wireMic && micSwitchInfo.virtualMic) {
        micOptions.value = '有线耳机麦克风和虚拟麦克风';
    } else {
        micOptions.value = '关闭';
    }
}

// 添加选择条的事件监听
document.addEventListener('DOMContentLoaded', () => {
    const discussId = getDiscussIdFromUrl();
    if (discussId) {
        fetchDiscussInfo(discussId);
        externMicSentencesConnection(discussId);
        wireMicSentencesConnection(discussId);
        virtualMicSentencesConnection(discussId);
    } else {
        console.error('缺少discussId参数');
    }

    const micOptions = document.getElementById('micOptions');
    micOptions.addEventListener('change', function(event) {
        handleMicChange(event, discussId);
    });
});

// 处理麦克风选项变更
function handleMicChange(event,discussId) {
    const value = event.target.value;
    let discussInfo = {
        'discussId': discussId,
        'micSwitchInfo': null
    };
    let micSwitchInfo = new MicSwitchInfo();

    if (value === '外挂麦克风') {
        micSwitchInfo['externMic'] = true;
        micSwitchInfo['wireMic']=false;
        micSwitchInfo['virtualMic']=false;
    } else if (value === '有线耳机麦克风和虚拟麦克风') {
        micSwitchInfo['externMic']=false;
        micSwitchInfo['wireMic'] = true;
        micSwitchInfo['virtualMic'] = true;
    } else if (value === '关闭') {
        micSwitchInfo['externMic']=false;
        micSwitchInfo['wireMic']=false;
        micSwitchInfo['virtualMic']=false;
    }
    discussInfo.micSwitchInfo=micSwitchInfo;

    // 发送POST请求
    fetch('http://127.0.0.1:10002/micSwitch', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(discussInfo),
    })
        .then(response => response.json())
        .then(data => {
            console.log('Success:', data);
        })
        .catch((error) => {
            console.error('Error:', error);
        });
}

// 建立WebSocket连接,接收externMicSentences的推送
function externMicSentencesConnection(discussId) {
    const socket = new SockJS('http://127.0.0.1:10002/ws');
    const stompClient = Stomp.over(socket); // 使用stomp协议

    let isWaitingToSend = false; // 标记是否正在等待发送请求

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        sendRequest(); // 初始发送请求

        stompClient.subscribe(`/topic/externMicSentencesConnection/${discussId}`, function (message) {
            console.log('收到externMicSentences:', message.body);

            // 检查message.body是否为空或者空字符串
            if (message.body && message.body.trim() !== '') {
                // 如果message.body不为空，才执行以下逻辑

                // 将收到的消息体转换为JSON对象
                const externMicSentences = JSON.parse(message.body);

                // 将数据处理成指定格式的字符串
                  const displayText = externMicSentences.queue.map(item => item.text).join('\n');

                // 将处理后的字符串显示在externMicSentencesDisplay区域
                document.getElementById('externMicSentencesDisplay').textContent = displayText;
            }

            if (!isWaitingToSend) {
                isWaitingToSend = true;
                setTimeout(sendRequest, 1000); // 1秒后发送请求
            }
        });
    });


    function sendRequest() {
        stompClient.send(`/app/externMicSentencesConnection/${discussId}`, {}, JSON.stringify({/* 消息内容 */}));
        isWaitingToSend = false;
    }
}

// 建立WebSocket连接,接收wireMicSentences的推送
function wireMicSentencesConnection(discussId) {
    const socket = new SockJS('http://127.0.0.1:10002/ws');
    const stompClient = Stomp.over(socket); // 使用stomp协议

    let isWaitingToSend = false; // 标记是否正在等待发送请求

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        sendRequest(); // 初始发送请求

        stompClient.subscribe(`/topic/wireMicSentencesConnection/${discussId}`, function (message) {
            console.log('wireMicSentences:', message.body);

            // 检查message.body是否为空或者空字符串
            if (message.body && message.body.trim() !== '') {
                // 如果message.body不为空，才执行以下逻辑

                // 将收到的消息体转换为JSON对象
                const wireMicSentences = JSON.parse(message.body);

                // 将数据处理成指定格式的字符串
                  const displayText = wireMicSentences.queue.map(item => item.text).join('\n');

                // 将处理后的字符串显示在wireMicSentencesDisplay区域
                document.getElementById('wireMicSentencesDisplay').textContent = displayText;
            }

            if (!isWaitingToSend) {
                isWaitingToSend = true;
                setTimeout(sendRequest, 1000); // 1秒后发送请求
            }
        });
    });

    function sendRequest() {
        stompClient.send(`/app/wireMicSentencesConnection/${discussId}`, {}, JSON.stringify({/* 消息内容 */}));
        isWaitingToSend = false;
    }
}

// 建立WebSocket连接,接收virtualMicSentences的推送
function virtualMicSentencesConnection(discussId) {
    const socket = new SockJS('http://127.0.0.1:10002/ws');
    const stompClient = Stomp.over(socket); // 使用stomp协议

    let isWaitingToSend = false; // 标记是否正在等待发送请求

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        sendRequest(); // 初始发送请求

        stompClient.subscribe(`/topic/virtualMicSentencesConnection/${discussId}`, function (message) {
            console.log('收到virtualMicSentences:', message.body);

            // 检查message.body是否为空或者空字符串
            if (message.body && message.body.trim() !== '') {
                // 如果message.body不为空，才执行以下逻辑

                // 将收到的消息体转换为JSON对象
                const virtualMicSentences = JSON.parse(message.body);

                // 将数据处理成指定格式的字符串
                  const displayText = virtualMicSentences.queue.map(item => item.text).join('\n');

                // 将处理后的字符串显示在virtualMicSentencesDisplay区域
                document.getElementById('virtualMicSentencesDisplay').textContent = displayText;
            }

            if (!isWaitingToSend) {
                isWaitingToSend = true;
                setTimeout(sendRequest, 1000); // 1秒后发送请求
            }
        });
    });

    function sendRequest() {
        stompClient.send(`/app/virtualMicSentencesConnection/${discussId}`, {}, JSON.stringify({/* 消息内容 */}));
        isWaitingToSend = false;
    }
}