package rskupnik.edgar;

public final class CommandOutput {

    private String text;
    private boolean speak;

    public CommandOutput(String text, boolean speak) {
        this.text = text;
        this.speak = speak;
    }

    public String getText() {
        return text;
    }

    public boolean isSpeak() {
        return speak;
    }
}
