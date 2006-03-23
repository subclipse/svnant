package org.tigris.subversion.svnant;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * svn info
 * @author Jeremy Whitlock
 * <a href="mailto:jwhitlock@collab.net">jwhitlock@collab.net</a>
 * @author Daniel Rall
 */
public class Info extends SvnCommand {

    /**
     * The target to retrieve properties for.
     */
    private String target = null;

    /** Whether or not to print the properties. */
    private boolean verbose = false;

    /** String prepended to new property names. */
    private String propPrefix = "svn.info.";

    /** Client adapter info object. */
    private ISVNInfo info = null;

    /**
     * Available directory properties.  Assumed to be a subset of
     * {@link #FILE_PROP_NAMES}.
     */
    private static final String[] DIR_PROP_NAMES = {
        "path", 
        "url", 
        "repouuid", 
        "rev", 
        "nodekind", 
        "schedule", 
        "author", 
        "lastRev", 
        "lastDate"
    };

    /** Available file properties. */
    private static final String[] FILE_PROP_NAMES = {
        "path", 
        "name",
        "url", 
        "repouuid", 
        "rev", 
        "nodekind", 
        "schedule", 
        "author", 
        "lastRev", 
        "lastDate", 
        "lastTextUpdate", 
        "lastPropUpdate", 
        "checksum"
    };

    /**
     * @see org.tigris.subversion.svnant.SvnCommand#execute(org.tigris.subversion.svnclientadapter.ISVNClientAdapter)
     */
    public void execute(ISVNClientAdapter svnClient) throws BuildException {
        if (verbose) {
            log("Svn: Info");
        }
        validateAttributes();

        Project project = super.getProject();
        try {
            this.info = acquireInfo(svnClient, this.target);
            String[] propNames = (SVNNodeKind.DIR == this.info.getNodeKind() ?
                                  DIR_PROP_NAMES : FILE_PROP_NAMES);
            for (int i = 0; i < propNames.length; i++) {
                String value = getValue(propNames[i]);
                project.setProperty(propPrefix + propNames[i], value);
                if (verbose) {
                    log("    " + propPrefix + propNames[i] + ": " + value,
                        Project.MSG_INFO);
                }
            }
        }
        catch (Exception e) {
            throw new BuildException("Failed to set 'info' properties", e);
        }
    }

    /**
     * Always contacts the repository.  In the future, might want to
     * allow for use of
     * <code>ISVNInfo.getInfoFromWorkingCopy()</code>, which uses only
     * the meta data from the WC.
     *
     * @exception SVNClientException If ISVNInfo.getInfo(target)
     * fails.
     */
    private ISVNInfo acquireInfo(ISVNClientAdapter svnClient, String target)
        throws SVNClientException {
        File targetAsFile = new File(Project.translatePath(target));
        if (targetAsFile.exists()) {
            // Since the target exists locally, assume it's not a URL.
            return svnClient.getInfo(targetAsFile);
        }
        else {
            try {
                SVNUrl url = new SVNUrl(target);
                return svnClient.getInfo(url);
            }
            catch (MalformedURLException malformedURL) {
                // Since we don't have a valid URL with which to
                // contact the repository, assume the target is a
                // local file, even though it doesn't exist locally.
                return svnClient.getInfo(targetAsFile);
            }
        }
    }

    /**
     * Retrieve a value for the named property.  If the named property
     * is not recognized and in verbose mode, log a message
     * accordingly.  Assumes that {@link #info} has already been
     * initialized (typically handled by invocation of {@link
     * #execute()}).
     *
     * @param propName Name of the property to retrieve a value for.
     * @return The value of the named property, or if not recognized,
     * the empty string
     */
    public String getValue(String propName) {
        Object value = null;

        // ASSUMPTION: DIR_PROP_NAMES is a subset of FILE_PROP_NAMES.
        if (FILE_PROP_NAMES[0].equals(propName)) {
            value = this.info.getFile();
            if (value != null) {
                value = ((File) value).getAbsolutePath();
            }
        } else if (FILE_PROP_NAMES[1].equals(propName)) {
            value = this.info.getFile();
            if (value != null) {
                value = ((File) value).getName();
            }
        } else if (FILE_PROP_NAMES[2].equals(propName)) {
            value = this.info.getUrl();
        } else if (FILE_PROP_NAMES[3].equals(propName)) {
            value = this.info.getUuid();
        } else if (FILE_PROP_NAMES[4].equals(propName)) {
            value = this.info.getRevision();
        } else if (FILE_PROP_NAMES[5].equals(propName)) {
            value = this.info.getNodeKind();
        } else if (FILE_PROP_NAMES[6].equals(propName)) {
            value = this.info.getSchedule();
        } else if (FILE_PROP_NAMES[7].equals(propName)) {
            value = this.info.getLastCommitAuthor();
        } else if (FILE_PROP_NAMES[8].equals(propName)) {
            value = this.info.getLastChangedRevision();
        } else if (FILE_PROP_NAMES[9].equals(propName)) {
            value = this.info.getLastChangedDate();
        } else if (FILE_PROP_NAMES[10].equals(propName)) {
            value = this.info.getLastDateTextUpdate();
        } else if (FILE_PROP_NAMES[11].equals(propName)) {
            value = this.info.getLastDatePropsUpdate();
        } else if (FILE_PROP_NAMES[12].equals(propName)) { 
            // ### FIXME: Implement checksum in svnClientAdapter.
            log("    " + "Property '" + propName + "' not implemented",
                Project.MSG_WARN);
        } else {
            if (verbose) {
                log("    " + "Property '" + propName + "' not recognized",
                    Project.MSG_INFO);
            }
        }

        return (value == null ? "" : value.toString());
    }

    /**
     * Validates the call to svn info
     */
    public void validateAttributes() {
        if (target == null) {
            throw new BuildException("target must be set to a file or " +
                                     "directory in your working copy, or " +
                                     "to a URI");
        }
    }

    /**
     * Set the path to the target WC file or directory, or to an URI.
     * @param target The target for which to retrieve info.
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Sets whether or not we output the properties we set
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Sets the Ant property prefix.  The default is
     * <code>svn.info.</code>.
     *
     * @param propPrefix The text to prefix all property names with.
     */
    public void setPropPrefix(String propPrefix) {
        if (propPrefix.endsWith(".")) {
            this.propPrefix = propPrefix;
        } else {
            this.propPrefix = propPrefix + '.';
        }
    }
}
