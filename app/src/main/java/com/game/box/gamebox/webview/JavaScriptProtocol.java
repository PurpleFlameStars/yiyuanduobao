package com.game.box.gamebox.webview;

public interface JavaScriptProtocol {
    interface Share {
        int code_sharing = 1;
        String status_sharing = "sharing";
        int code_complete = 2;
        String status_complete = "complete";
        int code_cancel = 3;
        String status_cancel = "cancel";
        int code_fail = 4;
        String status_fail = "fail";

        String code = "code";
        String status = "status";
    }
}
