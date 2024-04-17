// 添加选择条的事件监听
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const discussId= urlParams.get('discussId');
    if (discussId) {
        discussInfoConnection(discussId);
    } else {
        console.error('缺少discussId参数');
    }

});

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
                displayDiscussInfo(discussInfo);
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

function displayDiscussName(discussName) {
    document.getElementById('discussName').innerText = discussName;
}

function displaySentenceList(sentenceList) {
    const sentenceListContainer = document.getElementById('sentenceList');
    sentenceListContainer.innerHTML = ''; // 清空旧的列表
    sentenceList.forEach(sentence => {
        const sentenceElement = document.createElement('div');
        sentenceElement.innerText = `Text: ${sentence.text}, Summary: ${sentence.summary}, Begin Time: ${sentence.beginTime}, Mic Type: ${sentence.micTypeEnum}`;
        sentenceListContainer.appendChild(sentenceElement);
    });
}

function displayDiscussInfo(discussInfo) {
    displayDiscussName(discussInfo.discussName);
    displaySentenceList(discussInfo.sentenceList);

}