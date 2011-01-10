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
package org.tigris.subversion.svnant.conditions;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.tigris.subversion.svnant.SvnAntException;
import org.tigris.subversion.svnant.SvnFacade;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * This condition works similar to the generally known <code>available</code> task shipped
 * with the ant distribution. The only difference is it's 
 * 
 * @author Daniel Kasmeroglu <a href="mailto:daniel.kasmeroglu">daniel.kasmeroglu@kasisoft.net</a>
 */
public class Available extends SvnCondition {

    private String  target  = null;
    private FileDir type    = null; 

    /**
     * {@inheritDoc}
     */
    protected void preconditions() throws BuildException {
        if ( target == null ) {
            throw new BuildException( "Missing attribute 'target'." );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean internalEval() throws SvnAntException {
        
        ISVNClientAdapter client = SvnFacade.getClientAdapter(this);

        // Retrieve info for the requested element
        ISVNInfo info = null;
        try {
            File asfile = new File( Project.translatePath( target ) );
            if ( asfile.exists() ) {
                // Since the target exists locally, assume it's not a URL.
                info = client.getInfo( asfile );
            } else {
                try {
                    SVNUrl url = new SVNUrl( target );
                    info       = client.getInfo(url);
                } catch ( MalformedURLException ex ) {
                    throw new SvnAntException( "The url '" + target + "' is not valid.", ex );
                }
            }
        } catch ( SVNClientException ex ) {
            // assume that it is not existant
            return false;
        }
        
        // No info -> not in repository
        if( null == info ) {
            return false;
        }
        
        // No revision -> not in repository
        if ( ( info.getRevision() == null ) || ( SVNRevision.INVALID_REVISION.equals( info.getRevision() ) ) ) {
            return false;
        }
        
        if ( type != null ) {
            if ( type.isDir() ) {
                return info.getNodeKind() == SVNNodeKind.DIR;
            } else /* if ( type.isFile() ) */ {
                return info.getNodeKind() == SVNNodeKind.FILE;
            }
        }
        
        // Assume it is...
        return true;
    }

    /**
     * Changes the current target to be tested.
     * 
     * @param newtarget    The new target to be set.
     */
    public void setTarget( String newtarget ) {
        target = newtarget;
        if ( ( target != null ) && ( target.length() == 0 ) ) {
            target = null;
        }
    }

    /**
     * Sets the type if a specific type is desired.
     *
     * @param newtype   The type that has to be used.
     */
    public void setType( FileDir newtype ) {
        type = newtype;
    }
    
    /**
     * EnumeratedAttribute covering the file types to be checked for, either
     * file or dir.
     */
    public static class FileDir extends EnumeratedAttribute {

        private static final String[] VALUES = { "file", "dir" };

        /** 
         * {@inheritDoc} 
         */
        public String[] getValues() {
            return VALUES;
        }

        /**
         * Indicate if the value specifies a directory.
         *
         * @return true if the value specifies a directory.
         */
        public boolean isDir() {
            return "dir".equalsIgnoreCase( getValue() );
        }

        /**
         * Indicate if the value specifies a file.
         *
         * @return true if the value specifies a file.
         */
        public boolean isFile() {
            return "file".equalsIgnoreCase( getValue() );
        }

    }

}
