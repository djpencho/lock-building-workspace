package org.jenkinsci.plugins.lockbuildingworkspace;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Alessandro Panza
 */
public class LockWorkspaceBuildWrapper extends BuildWrapper {

    private String ws;
    private String id;

    @DataBoundConstructor
    public LockWorkspaceBuildWrapper() {

    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {

        if ((getDescriptor()).isEnabled()) {
            FilePath workspace = build.getWorkspace();

            if (workspace != null) {

                this.ws = workspace.getRemote();
                this.id = build.getUrl();

                listener.getLogger().println("Check if someone is building on workspace [ " + ws + " ]");

                try {
                    BuildingWorkspaceHolder.getInstance().lock(ws, id);
                } catch (InterruptedException e) {
                    listener.fatalError(e.getMessage());
                }

                listener.getLogger().println("Workspace locked by this build, proceeding");

            }
        }

        return new EnvironmentImpl();
    }

    @Override
    public LockWorkspaceDescriptorImpl getDescriptor() {
        return (LockWorkspaceDescriptorImpl) super.getDescriptor();
    }

    class EnvironmentImpl extends Environment {

        public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {

            listener.getLogger().println("Unlocking the workspace");
            BuildingWorkspaceHolder.getInstance().unlock(ws, id);

            return super.tearDown(build, listener);
        }
    }

    /**
     *
     */
    @Extension
    public static final class LockWorkspaceDescriptorImpl extends BuildWrapperDescriptor {

        /**
         * 
         */
        private boolean enabled;

        /**
         * 
         */
        public LockWorkspaceDescriptorImpl() {
            load();
        }

        /**
         * 
         */
        public String getDisplayName() {
            return "Lock building workspace";
        }

        /**
         * 
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            enabled = formData.getBoolean("enabled");

            save();
            return super.configure(req, formData);
        }

        /**
         * 
         * @return
         */
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }
    }
}
