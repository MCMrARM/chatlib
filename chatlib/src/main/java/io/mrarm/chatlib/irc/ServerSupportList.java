package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.ModeList;
import io.mrarm.chatlib.dto.NickPrefixList;

public class ServerSupportList {

    private NickPrefixList nickPrefixes = new NickPrefixList("@+");
    private ModeList nickPrefixModes = new ModeList("ov");
    private ModeList channelTypes = new ModeList("#");
    private ModeList channelModesList = new ModeList("b"); // has add&remove param
    private ModeList channelModesValueExactUnset = new ModeList("k"); // single value, has add&remove param
    private ModeList channelModesValue = new ModeList("l"); // single value, has add param
    private ModeList channelModesFlag = new ModeList("imnpst"); // no params

    public NickPrefixList getSupportedNickPrefixes() {
        synchronized (this) {
            return nickPrefixes;
        }
    }

    public ModeList getSupportedNickPrefixModes() {
        synchronized (this) {
            return nickPrefixModes;
        }
    }

    public ModeList getSupportedChannelTypes() {
        return channelTypes;
    }

    public ModeList getSupportedListChannelModes() {
        return channelModesList;
    }

    public ModeList getSupportedValueExactUnsetChannelModes() {
        return channelModesValueExactUnset;
    }

    public ModeList getSupportedValueChannelModes() {
        return channelModesValue;
    }

    public ModeList getSupportedFlagChannelModes() {
        return channelModesFlag;
    }

    public void setSupportedNickPrefixes(NickPrefixList supportedNickPrefixes) {
        synchronized (this) {
            this.nickPrefixes = supportedNickPrefixes;
        }
    }

    public void setSupportedNickPrefixModes(ModeList nickPrefixModes) {
        synchronized (this) {
            this.nickPrefixModes = nickPrefixModes;
        }
    }

    public void setSupportedChannelTypes(ModeList channelTypes) {
        this.channelTypes = channelTypes;
    }

    public void setSupportedChannelModes(ModeList a, ModeList b, ModeList c, ModeList d) {
        channelModesList = a;
        channelModesValueExactUnset = b;
        channelModesValue = c;
        channelModesFlag = d;
    }

}
