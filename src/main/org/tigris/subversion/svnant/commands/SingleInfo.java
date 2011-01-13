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
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.tigris.subversion.svnant.SvnAntException;

import org.apache.tools.ant.types.EnumeratedAttribute;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;

import java.net.MalformedURLException;

/**
 * Similar command to <code>info</info> with the difference that this one selectively
 * fetches an information.
 * 
 * @author Daniel Kasmeroglu <a href="mailto:daniel.kasmeroglu@kasisoft.net">daniel.kasmeroglu@kasisoft.net</a>.
 */
public class SingleInfo extends SvnCommand {

    private static final String   PROP_PATH           = "path";
    private static final String   PROP_URL            = "url";
    private static final String   PROP_REPOURL        = "repourl";
    private static final String   PROP_REPOUUID       = "repouuid";
    private static final String   PROP_REV            = "revision";
    private static final String   PROP_NODEKIND       = "nodekind";
    private static final String   PROP_SCHEDULE       = "schedule";
    private static final String   PROP_AUTHOR         = "author";
    private static final String   PROP_LASTREV        = "lastRevision";
    private static final String   PROP_LASTDATE       = "lastDate";
    private static final String   PROP_NAME           = "name";
    private static final String   PROP_LASTTEXTUPDATE = "lastTextUpdate";
    private static final String   PROP_LASTPROPUPDATE = "lastPropUpdate";
    private static final String   PROP_CHECKSUM       = "checksum";

    private static final String[] PROP_ALL            = new String[] { 
        PROP_PATH, 
        PROP_URL, 
        PROP_REPOUUID, 
        PROP_REV,
        PROP_NODEKIND, 
        PROP_SCHEDULE, 
        PROP_AUTHOR, 
        PROP_LASTREV, 
        PROP_LASTDATE, 
        PROP_NAME,
        PROP_LASTTEXTUPDATE, 
        PROP_LASTPROPUPDATE, 
        PROP_CHECKSUM, 
        PROP_REPOURL 
    };

    private String                target              = null;
    private boolean               verbose             = false;
    private String                property            = null;
    private PropRequest           request             = null;

    /**
     * {@inheritDoc}
     */
    public void execute() throws SvnAntException {
        Project project = getProject();
        try {
            ISVNInfo info = acquireInfo();
            if( (info.getRevision() == null) || (SVNRevision.INVALID_REVISION.equals( info.getRevision() )) ) {
                throw new SvnAntException( target + " - Not a versioned resource" );
            }
            String value = getValue( info, request.getValue() );
            project.setProperty( property, value );
            info( verbose, property + " : " + value );
        } catch( Exception ex ) {
            throw new SvnAntException( "Failed to access subversion 'info' properties", ex );
        }
    }

    /**
     * Always contacts the repository. In the future, might want to allow for use of
     * <code>ISVNInfo.getInfoFromWorkingCopy()</code>, which uses only the meta data 
     * from the WC.
     *
     * @exception SVNClientException If ISVNInfo.getInfo(target) fails.
     */
    private ISVNInfo acquireInfo() throws SVNClientException {
        File asfile = new File( Project.translatePath( target ) );
        if( asfile.exists() ) {
            // Since the target exists locally, assume it's not a URL.
            return getClient().getInfo( asfile );
        } else {
            try {
                SVNUrl url = new SVNUrl( target );
                return getClient().getInfo( url );
            } catch( MalformedURLException ex ) {
                // Since we don't have a valid URL with which to
                // contact the repository, assume the target is a
                // local file, even though it doesn't exist locally.
                return getClient().getInfo( asfile );
            }
        }
    }

    /**
     * Retrieve a value for the named property. If the named property
     * is not recognized and in verbose mode, log a message
     * accordingly. Assumes that {@link #info} has already been
     * initialized (typically handled by invocation of {@link
     * #execute()}).
     *
     * @param info  The subversion information. Not <code>null</code>
     * @param key   Name of the property to retrieve a value for. Neither <code>null</code> nor empty.
     * 
     * @return   The value of the named property, or if not recognized, the empty string
     */
    private String getValue( ISVNInfo info, String key ) {
        Object value = null;
        if( PROP_PATH.equals( key ) ) {
            File file = info.getFile();
            if( file != null ) {
                value = file.getAbsolutePath();
            } else {
                // assume it's a remote info request; return last part of URL
                value = info.getUrl().getLastPathSegment();
            }
        } else if( PROP_NAME.equals( key ) ) {
            File file = info.getFile();
            if( file != null ) {
                value = file.getName();
            } else {
                // as above
                value = info.getUrl().getLastPathSegment();
            }
        } else if( PROP_REPOURL.equals( key ) ) {
            value = info.getRepository();
        } else if( PROP_URL.equals( key ) ) {
            value = info.getUrl();
        } else if( PROP_REPOUUID.equals( key ) ) {
            value = info.getUuid();
        } else if( PROP_REV.equals( key ) ) {
            value = info.getRevision();
        } else if( PROP_NODEKIND.equals( key ) ) {
            value = info.getNodeKind();
        } else if( PROP_SCHEDULE.equals( key ) ) {
            value = info.getSchedule();
        } else if( PROP_AUTHOR.equals( key ) ) {
            value = info.getLastCommitAuthor();
        } else if( PROP_LASTREV.equals( key ) ) {
            value = info.getLastChangedRevision();
        } else if( PROP_LASTDATE.equals( key ) ) {
            value = info.getLastChangedDate();
        } else if( PROP_LASTTEXTUPDATE.equals( key ) ) {
            value = info.getLastDateTextUpdate();
        } else if( PROP_LASTPROPUPDATE.equals( key ) ) {
            value = info.getLastDatePropsUpdate();
        } else if( PROP_CHECKSUM.equals( key ) ) {
            // ### FIXME: Implement checksum in svnClientAdapter.
            log( "    " + "Property '" + key + "' not implemented", Project.MSG_WARN );
        } else {
            info( verbose, "    " + "Property '" + key + "' not recognized" );
        }
        if( value == null ) {
            value = "";
        }
        return String.valueOf( value );
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        if( target == null ) {
            throw new BuildException( "the attribute 'target' must be set." );
        }
        if( property == null ) {
            throw new BuildException( "the attribute 'property' must be set." );
        }
        if( request == null ) {
            throw new BuildException( "the attribute 'request' must be set." );
        }
    }

    /**
     * Sets the path to the target WC file or directory, or to an URI.
     * 
     * @param newtarget    The target for which to retrieve info.
     */
    public void setTarget( String newtarget ) {
        target = newtarget;
        if( (target != null) && (target.length() == 0) ) {
            target = null;
        }
    }

    /**
     * Enables/disables the generation of verbose output.
     * 
     * @param enable    <code>true</code> <=> Enables verbose output.
     */
    public void setVerbose( boolean enable ) {
        verbose = enable;
    }

    /**
     * Changes the request of the desired property.
     *
     * @param newrequest   The request used to access a property.
     */
    public void setRequest( PropRequest newrequest ) {
        request = newrequest;
    }

    /**
     * Changes the ant property which will receive the value.
     * 
     * @param newproperty   The name of the property which will receive the value.
     */
    public void setProperty( String newproperty ) {
        property = newproperty;
        if( (property != null) && (property.length() == 0) ) {
            property = null;
        }
    }

    /**
     * EnumeratedAttribute covering the properties which may be requested.
     */
    public static class PropRequest extends EnumeratedAttribute {

        /** 
         * {@inheritDoc} 
         */
        public String[] getValues() {
            return PROP_ALL;
        }

    }

}
