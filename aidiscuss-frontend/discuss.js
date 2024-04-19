document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const discussId = urlParams.get('discussId');
    if (discussId) {

        document.getElementById('getBackground').addEventListener('click', function () {
            const backgroundDiv = document.getElementById('background');
            if (backgroundDiv.style.display === 'block') {
                backgroundDiv.style.display = 'none';
            } else {
                fetch(`http://127.0.0.1:10002/getBackground/${discussId}`)
                    .then(response => response.json())
                    .then(data => {
                        backgroundDiv.innerHTML = '';
                        data.forEach((item, index) => {
                            const itemDiv = document.createElement("div");
                            updateTextIfNeeded(itemDiv, item);
                            itemDiv.contentEditable = "true";
                            itemDiv.onblur = function () {
                                if (itemDiv.innerText.trim() === '') {
                                    fetch(`http://127.0.0.1:10002/deleteBackground`, {
                                        method: 'POST',
                                        headers: {'Content-Type': 'application/json'},
                                        body: JSON.stringify({discussId, index})
                                    });
                                } else {
                                    fetch(`http://127.0.0.1:10002/updateBackground`, {
                                        method: 'POST',
                                        headers: {'Content-Type': 'application/json'},
                                        body: JSON.stringify({discussId, index, background: itemDiv.innerText.trim()})
                                    });
                                }
                            };
                            backgroundDiv.appendChild(itemDiv);
                        });
                        backgroundDiv.style.display = 'block';
                    });
            }
        });

        document.getElementById('addBackground').addEventListener('click', function () {
            const modal = document.getElementById('backgroundInputModal');
            modal.style.display = 'block';
        });

        document.getElementById('backgroundSubmitInput').addEventListener('click', function () {
            const userInput = document.getElementById('backgroundUserInput').value;
            const modal = document.getElementById('backgroundInputModal');
            modal.style.display = 'none';

            if (userInput !== '') {
                const backgroundData = {
                    discussId: discussId,
                    background: userInput
                };
                fetch(`http://127.0.0.1:10002/addBackground`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(backgroundData)
                })
                    .then(response => {
                        if (response.ok) {
                            console.log('背景添加成功');
                        }
                    })
                    .catch(error => console.error('添加背景失败', error));
            }
        });

        document.getElementById('backgroundCancelInput').addEventListener('click', function () {
            const modal = document.getElementById('backgroundInputModal');
            modal.style.display = 'none';
        });

        document.getElementById('getQuestionAnswerList').addEventListener('click', function () {
            const qaListDiv = document.getElementById('questionAnswerList');
            qaListDiv.style.display = qaListDiv.style.display === 'block' ? 'none' : 'block';
        });

        document.getElementById('askQuestion').addEventListener('click', function () {
            document.getElementById('questionInputModal').style.display = 'block';
        });

        document.getElementById('questionSubmitInput').addEventListener('click', function () {
            const questionInput = document.getElementById('questionUserInput').value;
            const modal = document.getElementById('questionInputModal');
            modal.style.display = 'none';

            if (questionInput !== '') {
                fetch(`http://127.0.0.1:10002/askQuestion`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({discussId, question: questionInput})
                })
                    .then(response => {
                        if (response.ok) {
                            console.log('问题提交成功');
                            document.getElementById('questionInputModal').style.display = 'none';
                        }
                    })
                    .catch(error => console.error('提交问题失败', error));
            }
        });

        document.getElementById('questionCancelInput').addEventListener('click', function () {
            document.getElementById('questionInputModal').style.display = 'none';
        });

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

function displayStartTimeList(startTimeList) {
    const startTimeListContainer = document.getElementById('startTimeList');
    const existingChildren = startTimeListContainer.children;
    startTimeList.forEach((startTime, index) => {
        const startTimeText = `startTime: ${startTime}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], startTimeText);
        } else {
            const startTimeElement = document.createElement('div');
            startTimeElement.innerText = startTimeText;
            startTimeListContainer.appendChild(startTimeElement);
        }
    });
}

function displayStopTimeList(stopTimeList) {
    const stopTimeListContainer = document.getElementById('stopTimeList');
    const existingChildren = stopTimeListContainer.children;
    stopTimeList.forEach((stopTime, index) => {
        const stopTimeText = `stopTime: ${stopTime}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], stopTimeText);
        } else {
            const stopTimeElement = document.createElement('div');
            stopTimeElement.innerText = stopTimeText;
            stopTimeListContainer.appendChild(stopTimeElement);
        }
    });
}

function displaySegmentSummaryList(segmentSummaryList) {
    const segmentSummaryListContainer = document.getElementById('segmentSummaryList');
    const existingChildren = segmentSummaryListContainer.children;
    segmentSummaryList.forEach((segmentSummary, index) => {
        const segmentSummaryText = `segmentSummary: ${segmentSummary}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentSummaryText);
        } else {
            const segmentSummaryElement = document.createElement('div');
            segmentSummaryElement.innerText = segmentSummaryText;
            segmentSummaryListContainer.appendChild(segmentSummaryElement);
        }
    });
}

function displaySegmentQuestionList(segmentQuestionList) {
    const segmentQuestionListContainer = document.getElementById('segmentQuestionList');
    const existingChildren = segmentQuestionListContainer.children;
    segmentQuestionList.forEach((segmentQuestion, index) => {
        const segmentQuestionText = `segmentQuestion: ${segmentQuestion}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentQuestionText);
        } else {
            const segmentQuestionElement = document.createElement('div');
            segmentQuestionElement.innerText = segmentQuestionText;
            segmentQuestionListContainer.appendChild(segmentQuestionElement);
        }
    });
}

function displaySegmentUnderstandList(segmentUnderstandList) {
    const segmentUnderstandListContainer = document.getElementById('segmentUnderstandList');
    const existingChildren = segmentUnderstandListContainer.children;
    segmentUnderstandList.forEach((segmentUnderstand, index) => {
        const segmentUnderstandText = `segmentUnderstand: ${segmentUnderstand}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentUnderstandText);
        } else {
            const segmentUnderstandElement = document.createElement('div');
            segmentUnderstandElement.innerText = segmentUnderstandText;
            segmentUnderstandListContainer.appendChild(segmentUnderstandElement);
        }
    });
}

function displaySegmentRemarkList(segmentRemarkList) {
    const segmentRemarkListContainer = document.getElementById('segmentRemarkList');
    const existingChildren = segmentRemarkListContainer.children;
    segmentRemarkList.forEach((segmentRemark, index) => {
        const segmentRemarkText = `segmentRemark: ${segmentRemark}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentRemarkText);
        } else {
            const segmentRemarkElement = document.createElement('div');
            segmentRemarkElement.innerText = segmentRemarkText;
            segmentRemarkListContainer.appendChild(segmentRemarkElement);
        }
    });
}

function displaySegmentRestateList(segmentRestateList) {
    const segmentRestateListContainer = document.getElementById('segmentRestateList');
    const existingChildren = segmentRestateListContainer.children;
    segmentRestateList.forEach((segmentRestate, index) => {
        const segmentRestateText = `segmentRestate: ${segmentRestate}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentRestateText);
        } else {
            const segmentRestateElement = document.createElement('div');
            segmentRestateElement.innerText = segmentRestateText;
            segmentRestateListContainer.appendChild(segmentRestateElement);
        }
    });
}

function displaySegmentAnalogyList(segmentAnalogyList) {
    const segmentAnalogyListContainer = document.getElementById('segmentAnalogyList');
    const existingChildren = segmentAnalogyListContainer.children;
    segmentAnalogyList.forEach((segmentAnalogy, index) => {
        const segmentAnalogyText = `segmentAnalogy: ${segmentAnalogy}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentAnalogyText);
        } else {
            const segmentAnalogyElement = document.createElement('div');
            segmentAnalogyElement.innerText = segmentAnalogyText;
            segmentAnalogyListContainer.appendChild(segmentAnalogyElement);
        }
    });
}

function displaySegmentContinueList(segmentContinueList) {
    const segmentContinueListContainer = document.getElementById('segmentContinueList');
    const existingChildren = segmentContinueListContainer.children;
    segmentContinueList.forEach((segmentContinue, index) => {
        const segmentContinueText = `segmentContinue: ${segmentContinue}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentContinueText);
        } else {
            const segmentContinueElement = document.createElement('div');
            segmentContinueElement.innerText = segmentContinueText;
            segmentContinueListContainer.appendChild(segmentContinueElement);
        }
    });
}

S

function displaySegmentMultiangleList(segmentMultiangleList) {
    const segmentMultiangleListContainer = document.getElementById('segmentMultiangleList');
    const existingChildren = segmentMultiangleListContainer.children;
    segmentMultiangleList.forEach((segmentMultiangle, index) => {
        const segmentMultiangleText = `segmentMultiangle: ${segmentMultiangle}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentMultiangleText);
        } else {
            const segmentMultiangleElement = document.createElement('div');
            segmentMultiangleElement.innerText = segmentMultiangleText;
            segmentMultiangleListContainer.appendChild(segmentMultiangleElement);
        }
    });
}

function displaySegmentLogicList(segmentLogicList) {
    const segmentLogicListContainer = document.getElementById('segmentLogicList');
    const existingChildren = segmentLogicListContainer.children;
    segmentLogicList.forEach((segmentLogic, index) => {
        const segmentLogicText = `segmentLogic: ${segmentLogic}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentLogicText);
        } else {
            const segmentLogicElement = document.createElement('div');
            segmentLogicElement.innerText = segmentLogicText;
            segmentLogicListContainer.appendChild(segmentLogicElement);
        }
    });
}

function displaySegmentManagingupList(segmentManagingupList) {
    const segmentManagingupListContainer = document.getElementById('segmentManagingupList');
    const existingChildren = segmentManagingupListContainer.children;
    segmentManagingupList.forEach((segmentManagingup, index) => {
        const segmentManagingupText = `segmentManagingup: ${segmentManagingup}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentManagingupText);
        } else {
            const segmentManagingupElement = document.createElement('div');
            segmentManagingupElement.innerText = segmentManagingupText;
            segmentManagingupListContainer.appendChild(segmentManagingupElement);
        }
    });
}

function displayTimeSlicedSummaryList(timeSlicedSummaryList) {
    const timeSlicedSummaryListContainer = document.getElementById('timeSlicedSummaryList');
    const existingChildren = timeSlicedSummaryListContainer.children;
    timeSlicedSummaryList.forEach((timeSlicedSummary, index) => {
        const timeSlicedSummaryText = `timeSlicedSummary: ${timeSlicedSummary}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], timeSlicedSummaryText);
        } else {
            const timeSlicedSummaryElement = document.createElement('div');
            timeSlicedSummaryElement.innerText = timeSlicedSummaryText;
            timeSlicedSummaryListContainer.appendChild(timeSlicedSummaryElement);
        }
    });
}

function displayKeyWordList(keyWordList) {
    const keyWordListContainer = document.getElementById('keyWordList');
    const existingChildren = keyWordListContainer.children;
    keyWordList.forEach((keyWord, index) => {
        const keyWordText = `keyWord: ${keyWord}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], keyWordText);
        } else {
            const keyWordElement = document.createElement('div');
            keyWordElement.innerText = keyWordText;
            keyWordListContainer.appendChild(keyWordElement);
        }
    });
}

function displayKeySentenceList(keySentenceList) {
    const keySentenceListContainer = document.getElementById('keySentenceList');
    const existingChildren = keySentenceListContainer.children;
    keySentenceList.forEach((keySentence, index) => {
        const keySentenceText = `keySentence: ${keySentence}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], keySentenceText);
        } else {
            const keySentenceElement = document.createElement('div');
            keySentenceElement.innerText = keySentenceText;
            keySentenceListContainer.appendChild(keySentenceElement);
        }
    });
}

function displayQuestionAnswerList(discussId, questionAnswerList) {
    const questionAnswerListContainer = document.getElementById('questionAnswerList');
    const existingChildren = questionAnswerListContainer.children;
    questionAnswerList.forEach((questionAnswer, index) => {
        let baseIndex = index * 3; // 每个问题答案组有三个元素：问题、答案和删除按钮
        let questionElement, answerElement, deleteButton;

        if (baseIndex < existingChildren.length) {
            questionElement = existingChildren[baseIndex];
            answerElement = existingChildren[baseIndex + 1];
            deleteButton = existingChildren[baseIndex + 2];
        } else {
            questionElement = document.createElement('div');
            questionElement.style.fontWeight = 'bold';
            questionAnswerListContainer.appendChild(questionElement);

            answerElement = document.createElement('div');
            answerElement.style.border = '1px solid #ccc';
            answerElement.style.padding = '10px';
            answerElement.style.marginTop = '5px';
            questionAnswerListContainer.appendChild(answerElement);

            deleteButton = document.createElement('button');
            deleteButton.innerText = '删除';
            questionAnswerListContainer.appendChild(deleteButton);
        }

        updateTextIfNeeded(questionElement, questionAnswer.question);
        updateTextIfNeeded(answerElement, questionAnswer.answer);

        deleteButton.onclick = function () {
            fetch(`http://127.0.0.1:10002/deleteQuestion`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({discussId, question: questionAnswer.question})
            })
                .then(response => {
                    if (response.ok) {
                        console.log('问题删除成功');
                        questionElement.remove();
                        answerElement.remove();
                        deleteButton.remove();
                    }
                })
                .catch(error => console.error('删除问题失败', error));
        };
    });

    // 清理超出的DOM元素
    for (let i = questionAnswerList.length * 3; i < existingChildren.length; i++) {
        questionAnswerListContainer.removeChild(existingChildren[i]);
    }
}


function displayDiscussInfo(discussInfo) {
    displayDiscussName(discussInfo.discussName);
    displaySentenceList(discussInfo.sentenceList);
    displayStartTimeList(discussInfo.startTimeList);
    displayStopTimeList(discussInfo.stopTimeList);
    displaySegmentSummaryList(discussInfo.segmentSummaryList);
    displaySegmentQuestionList(discussInfo.segmentQuestionList);
    displaySegmentUnderstandList(discussInfo.segmentUnderstandList);
    displaySegmentRemarkList(discussInfo.segmentRemarkList);
    displaySegmentRestateList(discussInfo.segmentRestateList);
    displaySegmentAnalogyList(discussInfo.segmentAnalogyList);
    displaySegmentContinueList(discussInfo.segmentContinueList);
    displaySegmentMultiangleList(discussInfo.segmentMultiangleList);
    displaySegmentLogicList(discussInfo.segmentLogicList);
    displaySegmentManagingupList(discussInfo.segmentManagingupList);
    displayTimeSlicedSummaryList(discussInfo.timeSlicedSummaryList);
    displayKeyWordList(discussInfo.keyWordList);
    displayKeySentenceList(discussInfo.keySentenceList);
    displayQuestionAnswerList(discussInfo.discussId, discussInfo.questionAnswerList);

}

function updateTextIfNeeded(element, newText) {
    if (element.innerText !== newText) {
        element.innerText = newText;
    }
}
