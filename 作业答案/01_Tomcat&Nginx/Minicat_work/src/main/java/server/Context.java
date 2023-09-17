package server;

import java.util.ArrayList;
import java.util.List;

public class Context {

    private String path;

    private List<Wrapper> wrappers = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Wrapper> getWrappers() {
        return wrappers;
    }

    public void setWrappers(List<Wrapper> wrappers) {
        this.wrappers = wrappers;
    }
}
