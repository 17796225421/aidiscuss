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

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.send(`/app/externMicSentencesConnection/${discussId}`, {}, JSON.stringify({/* 消息内容 */}));
        stompClient.subscribe(`/topic/externMicSentencesConnection/${discussId}`, function (message) {
            const externMicSentences = message.body;
            console.log('收到externMicSentences:', externMicSentences);
            // TODO: 在这里可以对收到的数据进行处理,例如在页面上显示出来

            // 处理完毕，等待 1 秒钟后重新发送请求
            setTimeout(function() {
                stompClient.send(`/app/externMicSentencesConnection/${discussId}`, {}, JSON.stringify({/* 消息内容 */}));
            }, 1000);
        });
    });

}