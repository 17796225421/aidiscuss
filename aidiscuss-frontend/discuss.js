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
    const discussNameElement = document.getElementById('discussName');
    updateTextIfNeeded(discussNameElement, discussName);
}

function displaySentenceList(sentenceList) {
    const sentenceListContainer = document.getElementById('sentenceList');
    const existingChildren = sentenceListContainer.children;
    sentenceList.forEach((sentence, index) => {
        const sentenceText = `Text: ${sentence.text}, Summary: ${sentence.summary}, Begin Time: ${sentence.beginTime}, Mic Type: ${sentence.micTypeEnum}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], sentenceText);
        } else {
            const sentenceElement = document.createElement('div');
            sentenceElement.innerText = sentenceText;
            sentenceListContainer.appendChild(sentenceElement);
        }
    });
}

function displayDiscussInfo(discussInfo) {
    displayDiscussName(discussInfo.discussName);
    displaySentenceList(discussInfo.sentenceList);

}

function updateTextIfNeeded(element, newText) {
    if (element.innerText !== newText) {
        element.innerText = newText;
    }
}
