let discussInfo;
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
                                if (itemDiv.textContent.trim() === '') {
                                    fetch(`http://127.0.0.1:10002/deleteBackground`, {
                                        method: 'POST',
                                        headers: {'Content-Type': 'application/json'},
                                        body: JSON.stringify({discussId, index})
                                    });
                                } else {
                                    fetch(`http://127.0.0.1:10002/updateBackground`, {
                                        method: 'POST',
                                        headers: {'Content-Type': 'application/json'},
                                        body: JSON.stringify({discussId, index, background: itemDiv.textContent.trim()})
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

        // 获取所有tabItem和内容元素
        const tabItems = document.querySelectorAll('.tab-item');
        const tabContents = document.querySelectorAll('.tab-content');

        // 定义一个变量来保存定时器
        let hoverTimer = null;

        // 为每个tabItem添加鼠标悬停事件
        tabItems.forEach(item => {
            item.addEventListener('mouseover', () => {
                // 设置一个定时器,在1秒后触发点击事件
                hoverTimer = setTimeout(() => {
                    // 移除所有item的active类
                    tabItems.forEach(btn => btn.classList.remove('active'));
                    // 为当前item添加active类
                    item.classList.add('active');

                    // 隐藏所有tab内容
                    tabContents.forEach(content => content.style.display = 'none');
                    // 显示当前tab对应的内容,并保持逆序显示
                    const tabId = item.getAttribute('data-tab');
                    const currentContent = document.getElementById(tabId + 'List');
                    currentContent.style.display = 'flex';
                    currentContent.style.flexDirection = 'column-reverse';
                }, 0.2 * 1000);
            });

            // 当鼠标移出按钮时,清除定时器
            item.addEventListener('mouseout', () => {
                clearTimeout(hoverTimer);
            });
        });

        const resizer = document.querySelector('.resizer');
        const leftSide = document.querySelector('.left');
        leftSide.style.width = "500px";
        let isResizing = false;
        let x0 = leftSide.style.width;

        resizer.addEventListener('mousedown', function (e) {
            isResizing = true;
            x0 = e.clientX;
            document.addEventListener('mousemove', handleMouseMove);
            document.addEventListener('mouseup', () => {
                isResizing = false;
                document.removeEventListener('mousemove', handleMouseMove);
            });
        });

        function handleMouseMove(e) {
            if (!isResizing) return;
            const dx = e.clientX - x0;
            // 计算新的宽度
            const newWidth = x0 + dx;
            // 设置新的宽度
            leftSide.style.width = `${newWidth}px`;
        }

        const audioPlayers = document.querySelectorAll('audio');
        const playPauseBtn = document.getElementById('playPauseBtn');
        const seekSlider = document.getElementById('seekSlider');
        const timeDisplay = document.getElementById('timeDisplay');

        playPauseBtn.addEventListener('click', () => {
            updateAudio(discussId).then(() => {
                audioPlayers.forEach(player => {
                    if (player.paused) {
                        player.play();
                    } else {
                        player.pause();
                    }
                });
                if (playPauseBtn.textContent === '⏸️') {
                    playPauseBtn.textContent = '▶️';
                } else {
                    playPauseBtn.textContent = '⏸️';
                }
            });
        });

        audioPlayers.forEach(player => {
            player.addEventListener('timeupdate', () => {
                const progress = (player.currentTime / player.duration) * 100;
                seekSlider.value = progress;
                const currentTime = formatTime(player.currentTime);
                const duration = formatTime(player.duration);
                timeDisplay.textContent = `${currentTime} / ${duration}`;
            });
        });

        seekSlider.addEventListener('input', () => {
            audioPlayers.forEach(player => {
                const seekTime = player.duration * (seekSlider.value / 100);
                player.currentTime = seekTime;
            });
        });

        function formatTime(time) {
            const minutes = Math.floor(time / 60);
            const seconds = Math.floor(time % 60);
            return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }

        const noteButton = document.getElementById('note');
        const noteDialog = document.getElementById('noteDialog');
        const editor = new Quill('#editor');

        noteButton.addEventListener('click', function () {
            if (noteDialog.style.display === 'none') {
                noteDialog.style.display = 'block';
                editor.root.innerHTML = discussInfo.noteText;
            } else {
                noteDialog.style.display = 'none';
            }
        });

        $(function () {
            $("#noteDialog").draggable({
                handle: "#noteHeader"
            }).resizable({
                handles: "all"
            });
        });

        const noteHeader = document.getElementById('noteHeader');

        noteHeader.addEventListener('dblclick', function () {
            noteDialog.style.display = 'none';
        });

        function debounce(func, wait) {
            let timeout;
            return function (...args) {
                clearTimeout(timeout);
                timeout = setTimeout(() => {
                    func.apply(this, args);
                }, wait);
            };
        }

        const debouncedFetch = debounce(function () {
            fetch(`http://127.0.0.1:10002/postNoteText?discussId=${discussId}&text=${encodeURIComponent(editor.root.innerHTML)}`, {
                method: 'POST'
            });
        }, 500);

        editor.on('text-change', debouncedFetch);

        editor.root.addEventListener('paste', (event) => {
            const files = event.clipboardData.files;
            if (files && files.length > 0) {
                const file = files[0];
                if (file.type.startsWith('image/')) {
                    uploadImage(file);
                }
            }
        });

        function uploadImage(file) {
            const formData = new FormData();
            formData.append('image', file);

            fetch('http://127.0.0.1:10002/uploadImage', {
                method: 'POST',
                body: formData
            })
                .then(response => response.json())
                .then(data => {
                    console.log(data);
                    const range = editor.getSelection();
                    data.url='https://img2.imgtp.com/2024/05/03/K8luNaCV.png';
                    editor.insertEmbed(range.index, 'image', data.url);
                })
                .catch(error => {
                    console.error('图片上传失败:', error);
                });
        }

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
        sendRequest();

        stompClient.subscribe(`/topic/discussInfoConnection/${discussId}`, function (message) {
            if (message.body && message.body.trim() !== '') {
                console.log("discussInfo:" + message.body);
                const data = JSON.parse(message.body);
                discussInfo = new DiscussInfo(data);
                displayDiscussInfo();

                formatMarkdownContent();
            }

            if (!isWaitingToSend) {
                isWaitingToSend = true;
                setTimeout(sendRequest, 1 * 1000);
            }
        });
    });

    function sendRequest() {
        stompClient.send(`/app/discussInfoConnection/${discussId}`, {}, JSON.stringify({}));
        isWaitingToSend = false;
    }
}

function formatMarkdownContent() {
    const tabs = document.querySelectorAll('.tab-content.markdown');
    const renderer = new marked.Renderer();

    // 自定义列表的渲染方式
    renderer.list = function (body, ordered, start) {
        const type = ordered ? 'ol' : 'ul';
        const startAttr = (ordered && start !== 1) ? (` start="${start}"`) : '';
        return `<${type}${startAttr} style="margin-left: 5px; padding-left: 10px;">${body}</${type}>`;
    };

    marked.setOptions({
        renderer: renderer,
        breaks: true
    });

    tabs.forEach(tab => {
        const children = tab.children;
        for (let i = 0; i < children.length; i++) {
            const child = children[i];
            const formatHTML = marked.parse(child.innerHTML);
            if (child.innerHTML !== formatHTML) {
                child.innerHTML = formatHTML;
            }
        }
    });
}

function displayDiscussName(discussName) {
    const discussNameElement = document.getElementById('discussName');
    updateTextIfNeeded(discussNameElement, discussName);
}

function displayRealTimeSentence(realTimeSentence) {
    const realTimeSentenceElement = document.getElementById('realTimeSentence');
    updateTextIfNeeded(realTimeSentenceElement, realTimeSentence);
}

function displaySentenceList(discussId, sentenceList) {
    const sentenceListContainer = document.getElementById('sentenceList');
    const existingChildren = Array.from(sentenceListContainer.children).filter(child => child.className === 'sentence');
    sentenceList.forEach((sentence, index) => {
        let sentenceElement;
        if (index < existingChildren.length) {
            sentenceElement = existingChildren[index];
        } else {
            sentenceElement = document.createElement('div');
            sentenceElement.className = 'sentence';
            sentenceElement.style.display = 'flex';
            sentenceElement.style.alignItems = 'center';
            sentenceElement.style.justifyContent = 'space-between';

            const textElement = document.createElement('div');
            textElement.className = 'text';
            textElement.textContent = sentence.text;
            textElement.style.fontSize = '15px';
            textElement.style.padding = '1px';
            textElement.style.marginBottom = '5px';
            sentenceElement.appendChild(textElement);

            const summaryElement = document.createElement('div');
            summaryElement.className = 'summary';
            summaryElement.innerHTML = sentence.summary;
            summaryElement.style.opacity = "0.3";
            summaryElement.style.fontSize = '12px';
            summaryElement.style.padding = '1px';
            sentenceElement.appendChild(summaryElement);

            sentenceListContainer.appendChild(sentenceElement);

            // 只在创建新元素时添加点击事件监听
            sentenceElement.addEventListener('click', function () {
                const time = this.getAttribute('time');
                console.log('time: ' + time);

                const startTimeList = Array.from(document.querySelectorAll('.startTime')).map(el => el.getAttribute('time'));
                const stopTimeList = Array.from(document.querySelectorAll('.stopTime')).map(el => el.getAttribute('time'));

                let totalSeconds = 0;
                for (let i = 0; i < startTimeList.length; i++) {
                    const startTime = startTimeList[i];
                    const stopTime = stopTimeList[i] || time;

                    const seconds = getSecondsBetween(startTime, stopTime);
                    totalSeconds += seconds;
                }

                const audioPlayers = document.querySelectorAll('audio');
                const seekSlider = document.getElementById('seekSlider');

                updateAudio(discussId).then(r => {
                    audioPlayers.forEach(player => player.currentTime = totalSeconds);

                    const progress = (totalSeconds / audioPlayers[0].duration) * 100;
                    seekSlider.value = progress;
                });

                console.log(`从会议开始到选中的句子,会议已进行了 ${totalSeconds} 秒`);
            });
        }
        updateTextIfNeeded(sentenceElement.querySelector('.text'), sentence.text);
        updateTextIfNeeded(sentenceElement.querySelector('.summary'), sentence.summary);
        sentenceElement.setAttribute('time', sentence.beginTime);
        sentenceElement.setAttribute('micTypeEnum', sentence.micTypeEnum);

        if (sentence.score !== undefined) {
            const oldScore = sentenceElement.getAttribute('score');
            if (oldScore !== sentence.score.toString()) {
                const textElement = sentenceElement.querySelector('.text');
                if (sentence.score >= 1 && sentence.score <= 5) {
                    textElement.style.fontSize = (12 + sentence.score) + 'px';
                    textElement.style.opacity = (sentence.score * 2 * 0.1).toString();
                }
                sentenceElement.setAttribute('score', sentence.score);
            }
        } else {
            sentenceElement.setAttribute('score', '未知');
        }
    });
}

function getSecondsBetween(startTime, endTime) {
    const start = new Date(startTime);
    const end = new Date(endTime);

    const diffInMilliseconds = end - start;
    const diffInSeconds = Math.floor(diffInMilliseconds / 1000);

    return diffInSeconds;
}

function displayStartTimeList(startTimeList) {
    const sentenceListContainer = document.getElementById('sentenceList');
    const existingChildren = Array.from(sentenceListContainer.children);
    let startTimeIndex = 0;

    for (let i = 0; i < existingChildren.length && startTimeIndex < startTimeList.length; i++) {
        const timeText = existingChildren[i].getAttribute('time');
        if (startTimeList[startTimeIndex]) {
            const startTime = startTimeList[startTimeIndex];
            if (timeText > startTime) {
                if (i > 0 && existingChildren[i - 1].className === 'startTime') {
                    updateTextIfNeeded(existingChildren[i - 1], `开始时间: ${startTime}`);
                } else {
                    const startTimeDiv = document.createElement('div');
                    startTimeDiv.className = 'startTime';
                    startTimeDiv.textContent = `开始时间: ${startTime}`;
                    startTimeDiv.setAttribute('time', startTime);
                    startTimeDiv.style.color = 'lightgreen';
                    sentenceListContainer.insertBefore(startTimeDiv, existingChildren[i]);
                }
                startTimeIndex++;
            }
        }
    }
}

function displayStopTimeList(stopTimeList) {
    const sentenceListContainer = document.getElementById('sentenceList');
    const existingChildren = Array.from(sentenceListContainer.children);
    let stopTimeIndex = 0;

    for (let i = 0; i < existingChildren.length && stopTimeIndex < stopTimeList.length; i++) {
        const timeText = existingChildren[i].getAttribute('time');
        if (stopTimeList[stopTimeIndex]) {
            const stopTime = stopTimeList[stopTimeIndex];
            if (timeText > stopTime) {
                if (i > 0 && existingChildren[i - 1].className === 'stopTime') {
                    updateTextIfNeeded(existingChildren[i - 1], `停止时间: ${stopTime}`);
                } else {
                    const stopTimeDiv = document.createElement('div');
                    stopTimeDiv.className = 'stopTime';
                    stopTimeDiv.textContent = `停止时间: ${stopTime}`;
                    stopTimeDiv.setAttribute('time', stopTime);
                    stopTimeDiv.style.color = 'lightcoral';
                    sentenceListContainer.insertBefore(stopTimeDiv, existingChildren[i]);
                }
                stopTimeIndex++;
            }
        }
    }
}


function displaySegmentSummaryList(segmentSummaryList) {
    const segmentSummaryListContainer = document.getElementById('segmentSummaryList');
    const existingChildren = segmentSummaryListContainer.children;
    segmentSummaryList.forEach((segmentSummary, index) => {
        const segmentSummaryText = `${segmentSummary}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentSummaryText);
        } else {
            const segmentSummaryElement = document.createElement('div');
            segmentSummaryElement.textContent = segmentSummaryText;
            // 设置内边距和边框样式增加可读性
            segmentSummaryElement.style.padding = '5px';
            segmentSummaryElement.style.borderBottom = '1px solid #cccccc';
            segmentSummaryListContainer.appendChild(segmentSummaryElement);
        }
    });
}

function displaySegmentQuestionList(segmentQuestionList) {
    const segmentQuestionListContainer = document.getElementById('segmentQuestionList');
    const existingChildren = segmentQuestionListContainer.children;
    segmentQuestionList.forEach((segmentQuestion, index) => {
        const segmentQuestionText = `${segmentQuestion}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentQuestionText);
        } else {
            const segmentQuestionElement = document.createElement('div');
            segmentQuestionElement.textContent = segmentQuestionText;
            // 设置内边距和边框样式增加可读性
            segmentQuestionElement.style.padding = '10px';
            segmentQuestionElement.style.borderBottom = '1px solid #cccccc';
            segmentQuestionListContainer.appendChild(segmentQuestionElement);
        }
    });
}

function displaySegmentUnderstandList(segmentUnderstandList) {
    const segmentUnderstandListContainer = document.getElementById('segmentUnderstandList');
    const existingChildren = segmentUnderstandListContainer.children;
    segmentUnderstandList.forEach((segmentUnderstand, index) => {
        const segmentUnderstandText = `${segmentUnderstand}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentUnderstandText);
        } else {
            const segmentUnderstandElement = document.createElement('div');
            segmentUnderstandElement.textContent = segmentUnderstandText;
            // 设置内边距和边框样式增加可读性
            segmentUnderstandElement.style.padding = '10px';
            segmentUnderstandElement.style.borderBottom = '1px solid #cccccc';
            segmentUnderstandListContainer.appendChild(segmentUnderstandElement);
        }
    });
}

function displaySegmentRemarkList(segmentRemarkList) {
    const segmentRemarkListContainer = document.getElementById('segmentRemarkList');
    const existingChildren = segmentRemarkListContainer.children;
    segmentRemarkList.forEach((segmentRemark, index) => {
        const segmentRemarkText = `${segmentRemark}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentRemarkText);
        } else {
            const segmentRemarkElement = document.createElement('div');
            segmentRemarkElement.textContent = segmentRemarkText;
            // 设置内边距和边框样式增加可读性
            segmentRemarkElement.style.padding = '10px';
            segmentRemarkElement.style.borderBottom = '1px solid #cccccc';
            segmentRemarkListContainer.appendChild(segmentRemarkElement);
        }
    });
}

function displaySegmentRestateList(segmentRestateList) {
    const segmentRestateListContainer = document.getElementById('segmentRestateList');
    const existingChildren = segmentRestateListContainer.children;
    segmentRestateList.forEach((segmentRestate, index) => {
        const segmentRestateText = `${segmentRestate}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentRestateText);
        } else {
            const segmentRestateElement = document.createElement('div');
            segmentRestateElement.textContent = segmentRestateText;
            // 设置内边距和边框样式增加可读性
            segmentRestateElement.style.padding = '10px';
            segmentRestateElement.style.borderBottom = '1px solid #cccccc';
            segmentRestateListContainer.appendChild(segmentRestateElement);
        }
    });
}

function displaySegmentAnalogyList(segmentAnalogyList) {
    const segmentAnalogyListContainer = document.getElementById('segmentAnalogyList');
    const existingChildren = segmentAnalogyListContainer.children;
    segmentAnalogyList.forEach((segmentAnalogy, index) => {
        const segmentAnalogyText = `${segmentAnalogy}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentAnalogyText);
        } else {
            const segmentAnalogyElement = document.createElement('div');
            segmentAnalogyElement.textContent = segmentAnalogyText;
            // 设置内边距和边框样式增加可读性
            segmentAnalogyElement.style.padding = '10px';
            segmentAnalogyElement.style.borderBottom = '1px solid #cccccc';
            segmentAnalogyListContainer.appendChild(segmentAnalogyElement);
        }
    });
}

function displaySegmentContinueList(segmentContinueList) {
    const segmentContinueListContainer = document.getElementById('segmentContinueList');
    const existingChildren = segmentContinueListContainer.children;
    segmentContinueList.forEach((segmentContinue, index) => {
        const segmentContinueText = `${segmentContinue}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentContinueText);
        } else {
            const segmentContinueElement = document.createElement('div');
            segmentContinueElement.textContent = segmentContinueText;
            // 设置内边距和边框样式增加可读性
            segmentContinueElement.style.padding = '10px';
            segmentContinueElement.style.borderBottom = '1px solid #cccccc';
            segmentContinueListContainer.appendChild(segmentContinueElement);
        }
    });
}

function displaySegmentMultiangleList(segmentMultiangleList) {
    const segmentMultiangleListContainer = document.getElementById('segmentMultiangleList');
    const existingChildren = segmentMultiangleListContainer.children;
    segmentMultiangleList.forEach((segmentMultiangle, index) => {
        const segmentMultiangleText = `${segmentMultiangle}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentMultiangleText);
        } else {
            const segmentMultiangleElement = document.createElement('div');
            segmentMultiangleElement.textContent = segmentMultiangleText;
            // 设置内边距和边框样式增加可读性
            segmentMultiangleElement.style.padding = '10px';
            segmentMultiangleElement.style.borderBottom = '1px solid #cccccc';
            segmentMultiangleListContainer.appendChild(segmentMultiangleElement);
        }
    });
}

function displaySegmentLogicList(segmentLogicList) {
    const segmentLogicListContainer = document.getElementById('segmentLogicList');
    const existingChildren = segmentLogicListContainer.children;
    segmentLogicList.forEach((segmentLogic, index) => {
        const segmentLogicText = `${segmentLogic}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentLogicText);
        } else {
            const segmentLogicElement = document.createElement('div');
            segmentLogicElement.textContent = segmentLogicText;
            // 设置内边距和边框样式增加可读性
            segmentLogicElement.style.padding = '10px';
            segmentLogicElement.style.borderBottom = '1px solid #cccccc';
            segmentLogicListContainer.appendChild(segmentLogicElement);
        }
    });
}

function displaySegmentManagingupList(segmentManagingupList) {
    const segmentManagingupListContainer = document.getElementById('segmentManagingupList');
    const existingChildren = segmentManagingupListContainer.children;
    segmentManagingupList.forEach((segmentManagingup, index) => {
        const segmentManagingupText = `${segmentManagingup}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], segmentManagingupText);
        } else {
            const segmentManagingupElement = document.createElement('div');
            segmentManagingupElement.textContent = segmentManagingupText;
            // 设置内边距和边框样式增加可读性
            segmentManagingupElement.style.padding = '10px';
            segmentManagingupElement.style.borderBottom = '1px solid #cccccc';
            segmentManagingupListContainer.appendChild(segmentManagingupElement);
        }
    });
}

function displaySegmentDirectory(segmentDirectory) {
    if (segmentDirectory === "") {
        return;
    }
    const directory = JSON.parse(segmentDirectory);
    let directoryElement = document.getElementById('segmentDirectoryList');
    directoryElement.textContent = ''; // 清空现有内容

    const renderDirectory = (data, parentElement, level = 0) => {
        const ul = document.createElement('ul');
        ul.style.paddingLeft = "20px";
        data.forEach(item => {
            const li = document.createElement('li');
            li.innerText = item.dir;
            li.setAttribute("index", item.i);

            if (item.sub && item.sub.length > 0) {
                renderDirectory(item.sub, li, level + 1);
            }
            ul.appendChild(li);
        });
        parentElement.appendChild(ul);
    };

    renderDirectory(directory.data, directoryElement);

    directoryElement = document.getElementById('segmentDirectoryList');
    directoryElement.addEventListener('click', function (event) {
        let target = event.target;
        while (target !== directoryElement) {
            if (target.tagName === 'LI') {
                const index = parseInt(target.getAttribute('index'));
                const sentenceList = document.getElementById('sentenceList');
                const sentences = Array.from(sentenceList.querySelectorAll('.sentence')); // 只选取class为sentence的子元素
                const sentenceToScroll = sentences[index];
                if (sentenceToScroll) {
                    sentenceToScroll.scrollIntoView({behavior: 'smooth'});

                    // 设置过渡效果
                    sentenceToScroll.style.transition = "background-color 0.5s ease-in-out";

                    // 添加高亮
                    sentenceToScroll.style.backgroundColor = 'yellow';

                    // 一秒后移除高亮
                    setTimeout(function () {
                        sentenceToScroll.style.backgroundColor = '';
                    }, 1000);
                }
                return;
            }
            target = target.parentNode;
        }
    });
}

function displaySegmentUml(segmentUmlList) {
    let segmentUmlListContainer = document.getElementById('segmentUmlList');
    const existingChildren = segmentUmlListContainer.children;
    segmentUmlList.forEach((segmentUml, index) => {
        if (index < existingChildren.length) {
            const plantuml = existingChildren[index].getAttribute("plantuml");
            if (plantuml !== segmentUml) {
                let encodedUml = plantumlEncoder.encode(segmentUml);
                let imageUrl = "http://www.plantuml.com/plantuml/img/" + encodedUml;
                existingChildren[index].src = imageUrl;
            }
        } else {
            let encodedUml = plantumlEncoder.encode(segmentUml);
            let imageUrl = "http://www.plantuml.com/plantuml/img/" + encodedUml;
            const segmentUmlElement = document.createElement('img');
            segmentUmlElement.src = imageUrl;
            segmentUmlElement.setAttribute("plantuml", segmentUml);
            segmentUmlElement.addEventListener('click', () => {
                if (segmentUmlElement.style.zIndex === '9999') {
                    segmentUmlElement.style.position = '';
                    segmentUmlElement.style.top = '';
                    segmentUmlElement.style.left = '';
                    segmentUmlElement.style.transform = '';
                    segmentUmlElement.style.zIndex = '';
                } else {
                    segmentUmlElement.style.position = 'fixed';
                    segmentUmlElement.style.top = '50%';
                    segmentUmlElement.style.left = '50%';
                    segmentUmlElement.style.transform = 'translate(-50%, -50%) scale(1)';
                    segmentUmlElement.style.zIndex = '9999'; // 确保图片在最上层
                }
            });
            segmentUmlListContainer.appendChild(segmentUmlElement);
        }
    });
}

function displayTimeSlicedSummaryList(timeSlicedSummaryList) {
    const timeSlicedSummaryListContainer = document.getElementById('timeSlicedSummaryList');
    const existingChildren = timeSlicedSummaryListContainer.children;
    timeSlicedSummaryList.forEach((timeSlicedSummary, index) => {
        const timeSlicedSummaryText = `${timeSlicedSummary}`;
        if (index < existingChildren.length) {
            updateTextIfNeeded(existingChildren[index], timeSlicedSummaryText);
        } else {
            const timeSlicedSummaryElement = document.createElement('div');
            timeSlicedSummaryElement.textContent = timeSlicedSummaryText;
            // 设置内边距和边框样式增加可读性
            timeSlicedSummaryElement.style.padding = '10px';
            timeSlicedSummaryElement.style.borderBottom = '1px solid #cccccc';
            timeSlicedSummaryListContainer.appendChild(timeSlicedSummaryElement);
        }
    });
}

function displayKeyWordList(keyWordList) {
    const sentenceTexts = document.querySelectorAll('.sentence .text');
    keyWordList.forEach(keyWord => {
        if (keyWord.trim() === '') return; // 跳过空字符串
        sentenceTexts.forEach(sentenceText => {
            const regex = new RegExp(keyWord, 'gu');
            if (sentenceText.innerHTML.includes(keyWord) && !sentenceText.innerHTML.includes('<b>' + keyWord + '</b>')) {
                sentenceText.innerHTML = sentenceText.innerHTML.replace(regex, match => `<b>${match}</b>`);
            }
        });
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
            deleteButton.textContent = '删除';
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

function updateAudio(discussId) {
    const audioPlayer1 = document.getElementById('audioPlayer1');
    const audioUrl1 = `http://127.0.0.1:10002/audio/virtual/${discussId}`;
    const isPlaying1 = !audioPlayer1.paused;
    const currentTime1 = audioPlayer1.currentTime;

    const audioPlayer2 = document.getElementById('audioPlayer2');
    const audioUrl2 = `http://127.0.0.1:10002/audio/wire/${discussId}`;
    const isPlaying2 = !audioPlayer2.paused;
    const currentTime2 = audioPlayer2.currentTime;

    const audioPlayer3 = document.getElementById('audioPlayer3');
    const audioUrl3 = `http://127.0.0.1:10002/audio/extern/${discussId}`;
    const isPlaying3 = !audioPlayer3.paused;
    const currentTime3 = audioPlayer3.currentTime;

    const timeDisplay = document.getElementById('timeDisplay');

    return Promise.all([
        fetch(audioUrl1).then(response => response.blob()),
        fetch(audioUrl2).then(response => response.blob()),
        fetch(audioUrl3).then(response => response.blob())
    ]).then(([blob1, blob2, blob3]) => {
        const url1 = URL.createObjectURL(blob1);
        const url2 = URL.createObjectURL(blob2);
        const url3 = URL.createObjectURL(blob3);

        audioPlayer1.src = url1;
        audioPlayer2.src = url2;
        audioPlayer3.src = url3;

        let maxDuration = 0;
        const audios = [audioPlayer1, audioPlayer2, audioPlayer3];
        audios.forEach(audio => {
            audio.onloadedmetadata = () => {
                if (audio.duration > maxDuration) {
                    maxDuration = audio.duration;
                    const currentTime = formatTime(audio.currentTime);
                    const duration = formatTime(maxDuration);
                    timeDisplay.textContent = `${currentTime} / ${duration}`;
                }
            };
        });

        if (isPlaying1) {
            audioPlayer1.currentTime = currentTime1;
            audioPlayer1.play();
        } else {
            audioPlayer1.currentTime = currentTime1;
        }

        if (isPlaying2) {
            audioPlayer2.currentTime = currentTime2;
            audioPlayer2.play();
        } else {
            audioPlayer2.currentTime = currentTime2;
        }

        if (isPlaying3) {
            audioPlayer3.currentTime = currentTime3;
            audioPlayer3.play();
        } else {
            audioPlayer3.currentTime = currentTime3;
        }
    }).catch(error => {
        console.error('获取音频失败:', error);
    });
}

function formatTime(time) {
    const minutes = Math.floor(time / 60);
    const seconds = Math.floor(time % 60);
    return `${minutes}:${seconds < 10 ? '0' + seconds : seconds}`;
}


function displayDiscussInfo() {
    displayDiscussName(discussInfo.discussName);
    displayRealTimeSentence(discussInfo.realTimeSentence);
    displaySentenceList(discussInfo.discussId, discussInfo.sentenceList);
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
    displaySegmentDirectory(discussInfo.segmentDirectory);
    displaySegmentUml(discussInfo.segmentUmlList);
    displayTimeSlicedSummaryList(discussInfo.timeSlicedSummaryList);
    displayKeyWordList(discussInfo.keyWordList);
    displayQuestionAnswerList(discussInfo.discussId, discussInfo.questionAnswerList);
}

function updateTextIfNeeded(element, newText) {
    if (element && newText) {
        const cleanElement = element.innerText.replace(/[^a-zA-Z\u4e00-\u9fa5]/g, '');
        const cleanNewText = newText.replace(/[^a-zA-Z\u4e00-\u9fa5]/g, '');
        if (cleanElement !== cleanNewText) {
            element.innerText = newText;
        }
    }
}