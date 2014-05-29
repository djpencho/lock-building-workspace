package org.jenkinsci.plugins.lockbuildingworkspace;

public class WorkspaceLock {

    private final String path;
    private String currentJob;

    public WorkspaceLock(String path) {
        this.path = path;
    }

    public synchronized boolean setCurrentJob(String currentJob) throws InterruptedException {

        if (currentJob == null) {
            throw new IllegalArgumentException("Null string is not a valid job name");
        }

        while (this.currentJob != null) {
            this.wait();
        }

        this.currentJob = currentJob;

        return true;
    }

    public synchronized boolean clear(String currentJob) {

        if (currentJob == null) {
            throw new IllegalArgumentException("Null string is not a valid job name");
        }

        if (currentJob.equals(this.currentJob)) {
            this.currentJob = null;

            this.notify();

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WorkspaceLock other = (WorkspaceLock) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }
}