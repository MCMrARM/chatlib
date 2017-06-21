package io.mrarm.chatlib.irc.cap;

public class CapabilityEntryPair {

    private String name;
    private String value;

    public CapabilityEntryPair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public CapabilityEntryPair(String val) {
        int iof = val.indexOf('=');
        if (iof != -1) {
            this.name = val.substring(0, iof);
            this.value = val.substring(iof + 1);
        } else {
            this.name = val;
        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (value != null)
            return name + "=" + value;
        return name;
    }
}
