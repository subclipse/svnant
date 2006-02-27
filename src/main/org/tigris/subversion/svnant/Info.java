package org.tigris.subversion.svnant;

import java.io.File;

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
     * The target to retrieve properties for.  A <code>File</code>
     * object if representative of a WC path.  Otherwise, a
     * <code>SVNUrl</code> object.
     */
    private Object target = null;

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
            throw new BuildException("Failed to set info properties", e);
        }
    }

    /**
     * Always contacts the repository.  In the future, might want to
     * allow for use of
     * <code>ISVNInfo.getInfoFromWorkingCopy()</code>, which uses only
     * the meta data from the WC.
     */
    private ISVNInfo acquireInfo(ISVNClientAdapter svnClient, Object target)
        throws SVNClientException{
        return (target instanceof File ?
                svnClient.getInfo((File) target) :
                svnClient.getInfo((SVNUrl) target));
    }

    /**
     * Retrieve a value for the named property.
     *
     * @param propName Name of the property to retrieve a value for.
     * @return The value of the property, or <code>null
     */
    public String getValue(String propName) {
        String value = null;

        // ASSUMPTION: DIR_PROP_NAMES is a subset of FILE_PROP_NAMES.
        if (FILE_PROP_NAMES[0].equals(propName)) {
            value = this.info.getFile().getAbsolutePath();
        } else if (FILE_PROP_NAMES[1].equals(propName)) {
            value = this.info.getFile().getName();
        } else if (FILE_PROP_NAMES[2].equals(propName)) {
            value = this.info.getUrl().toString();
        } else if (FILE_PROP_NAMES[3].equals(propName)) {
            value = this.info.getUuid();
        } else if (FILE_PROP_NAMES[4].equals(propName)) {
            value = this.info.getRevision().toString();
        } else if (FILE_PROP_NAMES[5].equals(propName)) {
            value = this.info.getNodeKind().toString();
        } else if (FILE_PROP_NAMES[6].equals(propName)) {
            value = this.info.getSchedule().toString();
        } else if (FILE_PROP_NAMES[7].equals(propName)) {
            value = this.info.getLastCommitAuthor();
        } else if (FILE_PROP_NAMES[8].equals(propName)) {
            value = this.info.getLastChangedRevision().toString();
        } else if (FILE_PROP_NAMES[9].equals(propName)) {
            value = this.info.getLastChangedDate().toString();
        } else if (FILE_PROP_NAMES[10].equals(propName)) {
            value = this.info.getLastDateTextUpdate().toString();
        } else if (FILE_PROP_NAMES[11].equals(propName)) {
            value = this.info.getLastDatePropsUpdate().toString();
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

        return value;
    }

    /**
     * Validates the call to svn info
     */
    public void validateAttributes() {
        if (target == null) {
            throw new BuildException("target must be set to a file or " +
                                     "directory in your working copy, or " +
                                     "to an URI");
        }
    }

    /**
     * Set the path to the target WC file or directory, or to an URI.
     * @param file
     */
    public void setTarget(String target) {
        // Determine whether target is a WC path.
        String localTarget = Project.translatePath(target);
        File f = new File(localTarget);
        this.target = (f.exists() ? localTarget : target);
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
