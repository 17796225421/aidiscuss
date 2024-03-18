package com.example.model;

public class MicSwitchInfo {
    private boolean externMic;
    private boolean wireMic;
    private boolean virtualMic;

    public MicSwitchInfo() {
        this.externMic = false;
        this.wireMic = false;
        this.virtualMic = false;
    }

    // Getters and Setters
    public boolean isExternMic() {
        return externMic;
    }

    public void setExternMic(boolean externMic) {
        this.externMic = externMic;
    }

    public boolean isWireMic() {
        return wireMic;
    }

    public void setWireMic(boolean wireMic) {
        this.wireMic = wireMic;
    }

    public boolean isVirtualMic() {
        return virtualMic;
    }

    public void setVirtualMic(boolean virtualMic) {
        this.virtualMic = virtualMic;
    }
}