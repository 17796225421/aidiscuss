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
            // 将discussName作为discuss.html的标题
            document.getElementById('discussTitle').textContent = data.discussName;
        })
        .catch(error => {
            console.error('获取讨论主题信息失败:', error);
        });
}

// 在页面加载完成后执行
window.onload = function() {
    const discussId = getDiscussIdFromUrl();
    if (discussId) {
        fetchDiscussInfo(discussId);
    } else {
        console.error('缺少discussId参数');
    }
};