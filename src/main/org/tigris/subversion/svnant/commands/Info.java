/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.tigris.subversion.svnant.commands;

import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;

import java.net.MalformedURLException;

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
    private String                target          = null;

    /** String prepended to new property names. */
    private String                propPrefix      = "svn.info.";

    /** Client adapter info object. */
    private ISVNInfo              info            = null;

    /**
     * Available directory properties.  Assumed to be a subset of
     * {@link #FILE_PROP_NAMES}.
     */
    private static final String[] DIR_PROP_NAMES  = { 
        "path", 
        "url", 
        "repourl", 
        "repouuid", 
        "rev", 
        "nodekind",
        "schedule", 
        "author", 
        "lastRev", 
        "lastDate" 
    };

    //TODO check the properties, the code and the documentation

    /** Available file properties. */
    private static final String[] FILE_PROP_NAMES = { 
        "path", 
        "name", 
        "url", 
        "repourl", 
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
     * {@inheritDoc}
     */
    public void execute() {
        Project theProject = getProject();
        try {
            this.info = acquireInfo();
            if( (this.info.getRevision() == null) || (SVNRevision.INVALID_REVISION.equals( this.info.getRevision() )) ) {
                throw new BuildException( this.target + " - Not a versioned resource" );
            }
            String[] propNames = (SVNNodeKind.DIR == this.info.getNodeKind() ? DIR_PROP_NAMES : FILE_PROP_NAMES);
            for( int i = 0; i < propNames.length; i++ ) {
                String value = getValue( propNames[i] );
                theProject.setProperty( propPrefix + propNames[i], value );
                verbose( "%s%s: %s", propPrefix, propNames[i], value );
            }
        } catch( SVNClientException e ) {
            throw new BuildException( "Failed to set 'info' properties", e );
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
    private ISVNInfo acquireInfo() throws SVNClientException {
        File targetAsFile = new File( Project.translatePath( this.target ) );
        if( targetAsFile.exists() ) {
            // Since the target exists locally, assume it's not a URL.
            return getClient().getInfo( targetAsFile );
        } else {
            try {
                SVNUrl url = new SVNUrl( this.target );
                return getClient().getInfo( url );
            } catch( MalformedURLException ex ) {
                // Since we don't have a valid URL with which to
                // contact the repository, assume the target is a
                // local file, even though it doesn't exist locally.
                return getClient().getInfo( targetAsFile );
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
    public String getValue( String propName ) {
        Object value = null;

        // ASSUMPTION: DIR_PROP_NAMES is a subset of FILE_PROP_NAMES.
        if( FILE_PROP_NAMES[0].equals( propName ) ) {
            value = this.info.getFile();
            if( value != null ) {
                value = ((File) value).getAbsolutePath();
            } else {
                // assume it's a remote info request; return last part of URL
                value = this.info.getUrl().getLastPathSegment();
            }
        } else if( FILE_PROP_NAMES[1].equals( propName ) ) {
            value = this.info.getFile();
            if( value != null ) {
                value = ((File) value).getName();
            } else {
                // as above
                value = this.info.getUrl().getLastPathSegment();
            }
        } else if( FILE_PROP_NAMES[2].equals( propName ) ) {
            value = this.info.getUrl();
        } else if( FILE_PROP_NAMES[3].equals( propName ) ) {
            value = this.info.getRepository();
        } else if( FILE_PROP_NAMES[4].equals( propName ) ) {
            value = this.info.getUuid();
        } else if( FILE_PROP_NAMES[5].equals( propName ) ) {
            value = this.info.getRevision();
        } else if( FILE_PROP_NAMES[6].equals( propName ) ) {
            value = this.info.getNodeKind();
        } else if( FILE_PROP_NAMES[7].equals( propName ) ) {
            value = this.info.getSchedule();
        } else if( FILE_PROP_NAMES[8].equals( propName ) ) {
            value = this.info.getLastCommitAuthor();
        } else if( FILE_PROP_NAMES[9].equals( propName ) ) {
            value = this.info.getLastChangedRevision();
        } else if( FILE_PROP_NAMES[10].equals( propName ) ) {
            value = this.info.getLastChangedDate();
        } else if( FILE_PROP_NAMES[11].equals( propName ) ) {
            value = this.info.getLastDateTextUpdate();
        } else if( FILE_PROP_NAMES[12].equals( propName ) ) {
            value = this.info.getLastDatePropsUpdate();
        } else if( FILE_PROP_NAMES[13].equals( propName ) ) {
            // ### FIXME: Implement checksum in svnClientAdapter.
            log( "    " + "Property '" + propName + "' not implemented", Project.MSG_WARN );
        } else {
            warning( "    Property '%s' not recognized", propName );
        }

        return (value == null ? "" : value.toString());
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrNotNull( "target", target );
    }

    /**
     * Set the path to the target WC file or directory, or to an URI.
     * @param target The target for which to retrieve info.
     */
    public void setTarget( String target ) {
        this.target = target;
    }

    /**
     * Sets whether or not we output the properties we set
     * @param verbose
     */
    public void setVerbose( boolean verbose ) {
        warning( "The attribute 'verbose' is no longer supported for the command 'info' as it's generally enabled using the Ant option -v !" );
    }

    /**
     * Sets the Ant property prefix.  The default is
     * <code>svn.info.</code>.
     *
     * @param propPrefix The text to prefix all property names with.
     */
    public void setPropPrefix( String propPrefix ) {
        if( propPrefix.endsWith( "." ) ) {
            this.propPrefix = propPrefix;
        } else {
            this.propPrefix = propPrefix + '.';
        }
    }
}
