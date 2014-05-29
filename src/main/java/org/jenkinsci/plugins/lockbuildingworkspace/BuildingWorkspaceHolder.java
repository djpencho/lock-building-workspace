package org.jenkinsci.plugins.lockbuildingworkspace;

import java.util.HashMap;
import java.util.Map;

public class BuildingWorkspaceHolder {

    private static class Reference {
        static final BuildingWorkspaceHolder instance = new BuildingWorkspaceHolder();
    }

    public static BuildingWorkspaceHolder getInstance() {
        return Reference.instance;
    }

    private volatile Map<String, WorkspaceLock> holder;

    private BuildingWorkspaceHolder() {
        holder = new HashMap<String, WorkspaceLock>();
    }

    public synchronized WorkspaceLock addNewWorkspaceToMonitor(String workspace) {

        if (holder.get(workspace) == null) {
            holder.put(workspace, new WorkspaceLock(workspace));
        }

        return holder.get(workspace);
    }

    public void lock(String workspacePath, String jobName) throws InterruptedException {

        WorkspaceLock workspaceLock = holder.get(workspacePath);
        if (workspaceLock == null) {
            workspaceLock = addNewWorkspaceToMonitor(workspacePath);
        }

        // Here the thread will wait
        workspaceLock.setCurrentJob(jobName);

    }

    public void unlock(String workspacePath, String jobName) {

        WorkspaceLock workspaceLock = holder.get(workspacePath);

        if (workspaceLock != null) {
            workspaceLock.clear(jobName);
        }
    }

}
