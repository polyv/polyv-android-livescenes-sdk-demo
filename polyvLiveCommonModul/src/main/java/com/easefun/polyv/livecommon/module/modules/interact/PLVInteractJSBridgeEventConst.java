package com.easefun.polyv.livecommon.module.modules.interact;

/**
 * date: 2020/9/1
 * author: HWilliamgo
 * description:
 */
public class PLVInteractJSBridgeEventConst {
    public static final String KNOW_ANSWER_METHOD = "knowAnswer";

    //未定义的消息
    public static final String TEST_QUESTION_METHOD = "testQuestion";

    //关闭WebView
    public static final String CLOSE_WEB_VIEW_METHOD = "closeWebview";
    //WebView加载完成
    public static final String WEB_VIEW_LOAD_FINISHED = "initWebview";

    //答题卡
    public static final String ANSWER_SHEET_CHOOSE = "chooseAnswer";
    public static final String ANSWER_SHEET_RESULT = "hasChooseAnswer";
    public static final String ANSWER_SHEET_START = "updateNewQuestion";

    //问卷调查
    public static final String QUESTIONNAIRE_CHOOSE = "endQuestionnaireAnswer";
    public static final String QUESTIONNAIRE_START = "startQuestionNaire";
    public static final String QUESTIONNAIRE_STOP = "stopQuestionNaire";
    public static final String QUESTIONNAIRE_RESULT = "sendQuestionNaireResult";
    public static final String QUESTIONNAIRE_ACHIEVEMENT = "questionNaireAchievement";

    //抽奖
    public static final String LOTTERY_START = "startLottery";
    public static final String LOTTERY_STOP = "stopLottery";
    public static final String LOTTERY_CLOSE_WINNER = "closeLotteryWinner";
    public static final String ON_SEND_WIN_DATA = "sendWinData";
    public static final String ON_ABANDON_LOTTERY = "abandonLottery";

    //签到
    public static final String SIGN_START = "startSign";
    public static final String SIGN_STOP = "stopSign";
    public static final String SIGN_SUBMIT = "submitSign";

    //公告
    public static final String BULLETIN_SHOW = "bulletin";
    public static final String BULLETIN_REMOVE = "removeBulletin";
    //公告栏里的链接跳转
    public static final String BULLETIN_LINK_CLICK = "linkClick";


    //互动应用callback（签到，抽奖，答题，问卷）
    public static final String INTERACTIVE_CALLBACK = "interactiveEventCallback";
}
