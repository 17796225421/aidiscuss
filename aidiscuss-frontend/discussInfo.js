class DiscussInfo {
    constructor(data) {
        this.discussId = data.discussId;
        this.discussName = data.discussName;
        this.realTimeSentence=data.realTimeSentence;
        this.sentenceList = data.sentenceList;
        this.startTimeList = data.startTimeList;
        this.stopTimeList = data.stopTimeList;
        this.segmentSummaryList = data.segmentSummaryList;
        this.segmentQuestionList = data.segmentQuestionList;
        this.segmentUnderstandList = data.segmentUnderstandList;
        this.segmentRemarkList = data.segmentRemarkList;
        this.segmentRestateList = data.segmentRestateList;
        this.segmentAnalogyList = data.segmentAnalogyList;
        this.segmentContinueList = data.segmentContinueList;
        this.segmentMultiangleList = data.segmentMultiangleList;
        this.segmentLogicList = data.segmentLogicList;
        this.segmentManagingupList = data.segmentManagingupList;
        this.timeSlicedSummaryList = data.timeSlicedSummaryList;
        this.keyWordList = data.keyWordList;
        this.backgroundList = data.backgroundList;
        this.questionAnswerList = data.questionAnswerList;
        this.discussStatus = data.discussStatus;
        this.segmentSummaryCursor = data.segmentSummaryCursor;
        this.segmentQuestionCursor = data.segmentQuestionCursor;
        this.segmentUnderstandCursor = data.segmentUnderstandCursor;
        this.segmentRemarkCursor = data.segmentRemarkCursor;
        this.segmentRestateCursor = data.segmentRestateCursor;
        this.segmentAnalogyCursor = data.segmentAnalogyCursor;
        this.segmentContinueCursor = data.segmentContinueCursor;
        this.segmentMultiangleCursor = data.segmentMultiangleCursor;
        this.segmentLogicCursor = data.segmentLogicCursor;
        this.segmentManagingupCursor = data.segmentManagingupCursor;
        this.timeSlicedSummaryCursor = data.timeSlicedSummaryCursor;
        this.keyWordCursor = data.keyWordCursor;
    }
}