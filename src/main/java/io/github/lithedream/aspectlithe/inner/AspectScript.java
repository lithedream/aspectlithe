package io.github.lithedream.aspectlithe.inner;

import java.io.Serializable;

public final class AspectScript implements Serializable {

    private String script;

    public AspectScript() {

    }

    public AspectScript(String script) {
        this.script = script;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}