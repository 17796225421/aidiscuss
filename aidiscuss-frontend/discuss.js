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
    } else {
        console.error('缺少discussId参数');
    }

    const micOptions = document.getElementById('micOptions');
    micOptions.addEventListener('change', handleMicChange);
});

// 处理麦克风选项变更
function handleMicChange(event) {
    const value = event.target.value;
    let postData = {
        'externMic': null,
        'wireMic': null,
        'virtualMic': null
    };

    if (value === '外挂麦克风') {
        postData['externMic'] = true;
        postData['wireMic']=false;
        postData['virtualMic']=false;
    } else if (value === '有线耳机麦克风和虚拟麦克风') {
        postData['externMic']=false;
        postData['wireMic'] = true;
        postData['virtualMic'] = true;
    } else if (value === '关闭') {
        postData['externMic']=false;
        postData['wireMic']=false;
        postData['virtualMic']=false;
    }

    // 发送POST请求
    fetch('http://127.0.0.1:10002/micSwitch', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(postData),
    })
        .then(response => response.json())
        .then(data => {
            console.log('Success:', data);
        })
        .catch((error) => {
            console.error('Error:', error);
        });
}