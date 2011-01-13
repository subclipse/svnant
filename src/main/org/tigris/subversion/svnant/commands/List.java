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

import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.apache.tools.ant.BuildException;

/**
 * svn list. List resources from a repository.
 * 
 * @author Daniel Kasmeroglu
 *         <a href="mailto:daniel.kasmeroglu@qvitech.com">daniel.kasmeroglu@qvitech.com</a>
 */
public class List extends SvnCommand {

    /** url to fetch the list from */
    private SVNUrl      url       = null;

    /** list recursively (complete tree) ? */
    private boolean     recurse   = false;

    /** revision to list */
    private SVNRevision revision  = SVNRevision.HEAD;

    /** list only the names (not the complete url) ? */
    private boolean     onlynames = false;

    /** the delimiter to separate each item */
    private String      delimiter = ",";

    /** list file resources. */
    private boolean     listFiles = true;

    /** list directory resources. */
    private boolean     listDirs  = true;

    /** the ant property we want to set with the listed content. */
    private String      property;

    /**
     * {@inheritDoc}
     */
    public void execute() {

        try {
            ISVNDirEntry[] content = getClient().getList( url, revision, recurse );
            int ignored = 0;
            for( int i = 0; i < content.length; i++ ) {
                if( content[i].getNodeKind() == SVNNodeKind.DIR ) {
                    if( !listDirs ) {
                        content[i] = null;
                        ignored++;
                    }
                } else if( content[i].getNodeKind() == SVNNodeKind.FILE ) {
                    if( !listFiles ) {
                        content[i] = null;
                        ignored++;
                    }
                } else {
                    content[i] = null;
                    ignored++;
                }
            }
            StringBuffer value = new StringBuffer();
            if( ignored < content.length ) {
                int pos = 0;
                for( int i = 0; i < content.length; i++ ) {
                    if( content[i] != null ) {
                        if( pos > 0 ) {
                            value.append( delimiter );
                        }
                        String path = content[i].getPath();
                        if( !onlynames ) {
                            path = url.appendPath( path ).toString();
                        }
                        value.append( path );
                        pos++;
                    }
                }
            }
            getProject().setProperty( property, value.toString() );
        } catch( SVNClientException e ) {
            throw new BuildException( "Can't list", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        if( url == null ) {
            throw new BuildException( "url must be set" );
        }
        if( revision == null ) {
            throw new BuildException( "Invalid revision. Revision should be a number, a date in the format as specified in dateFormatter attribute or HEAD, BASE, COMMITED or PREV" );
        }
        if( (delimiter == null) || (delimiter.length() == 0) ) {
            throw new BuildException( "delimiter is not allowed to be empty" );
        }
        if( (property == null) || (property.length() == 0) ) {
            throw new BuildException( "property is not allowed to be empty" );
        }
    }

    /**
     * Enables/disables the listing of file entries.
     * @param enable   <code>true</code> <=> List only file resources.
     */
    public void setListFiles( boolean enable ) {
        this.listFiles = enable;
    }

    /**
     * Enables/disables the listing of directory entries.
     * @param enable   <code>true</code> <=> List only directory resources.
     */
    public void setListDirs( boolean enable ) {
        this.listDirs = enable;
    }

    /**
     * Enables/disables the listing of names only.
     * @param enable   <code>true</code> <=> List only the names.
     */
    public void setOnlyNames( boolean enable ) {
        this.onlynames = enable;
    }

    /**
     * Changes the delimiter to be used for the list.
     * @param newdelimiter   The new delimiter to be used for the list.
     */
    public void setDelimiter( String newdelimiter ) {
        this.delimiter = newdelimiter;
    }

    /**
     * Sets the URL; required.
     * @param url The url to set
     */
    public void setUrl( SVNUrl url ) {
        this.url = url;
    }

    /**
     * Sets the revision
     * 
     * @param revision
     */
    public void setRevision( String revision ) {
        this.revision = getRevisionFrom( revision );
    }

    /**
     * @param property The property to set.
     */
    public void setProperty( String property ) {
        this.property = property;
    }

    /**
     * Enables recurse creation of the listing.
     * @param recurse   <code>true</code> <=> Recurse traversal of the listing.
     */
    public void setRecurse( boolean recurse ) {
        this.recurse = recurse;
    }

}
