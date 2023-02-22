package org.kanga.lucenetesting.command;

import org.kanga.lucenetesting.App;

public interface Command {
    public String getName();
    public void execute(App app, String[] args, int firstArg) throws Exception;
    public String[] getUsage();
}
