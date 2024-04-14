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
        discussInfoConnection(discussId);
    } else {
        console.error('缺少discussId参数');
    }

});

function displayDiscussName(discussInfo) {
    // 将discussName设置到页面上
    document.getElementById('discussName').textContent = discussInfo.discussName;
}

function displaySentenceList(discussInfo) {
    // 获取sentenceList容器
    const sentenceListContainer = document.getElementById('sentenceList');

    // 清空sentenceList容器
    sentenceListContainer.innerHTML = '';

    // 遍历sentenceList并生成HTML
    discussInfo.sentenceList.forEach(sentence => {
        const sentenceElement = document.createElement('div');
        sentenceElement.classList.add('sentence');

        const textElement = document.createElement('div');
        textElement.classList.add('text');
        textElement.textContent = sentence.text;

        const metaElement = document.createElement('div');
        metaElement.classList.add('meta');
        metaElement.textContent = `${sentence.summary} | ${sentence.beginTime} | ${sentence.micTypeEnum}`;

        sentenceElement.appendChild(textElement);
        sentenceElement.appendChild(metaElement);

        sentenceListContainer.appendChild(sentenceElement);
    });
}

function discussInfoConnection(discussId) {
    const socket = new SockJS('http://127.0.0.1:10002/ws');
    const stompClient = Stomp.over(socket);

    let isWaitingToSend = false;

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        sendRequest();

        stompClient.subscribe(`/topic/discussInfoConnection/${discussId}`, function (message) {
            if (message.body && message.body.trim() !== '') {
                console.log("discussInfo:" + message.body);
                const data = JSON.parse(message.body);
                const discussInfo = new DiscussInfo(data);

                displayDiscussName(discussInfo);

                displaySentenceList(discussInfo);

            }

            if (!isWaitingToSend) {
                isWaitingToSend = true;
                setTimeout(sendRequest, 1000);
            }
        });
    });

    function sendRequest() {
        stompClient.send(`/app/discussInfoConnection/${discussId}`, {}, JSON.stringify({}));
        isWaitingToSend = false;
    }
}