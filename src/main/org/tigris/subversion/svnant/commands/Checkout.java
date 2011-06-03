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

import org.tigris.subversion.svnclientadapter.utils.Depth;

import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.types.EnumeratedAttribute;

import org.apache.tools.ant.BuildException;

import java.io.File;

/**
 * svn Checkout. Check out a working copy from a repository 
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 */
public class Checkout extends SvnCommand {

    private static final String MSG_USAGE_CONFLICT = "The option 'recurse' cannot be used together with 'depth' ! Ignoring 'recurse' !";

    /** url to checkout from */
    private SVNUrl          url      = null;

    /** checkout recursively ? */
    private Boolean         recurse  = null;

    /** destinaty directory. */
    private File            destPath = null;

    /** revision to checkout */
    private SVNRevision     revision = SVNRevision.HEAD;
    
    private boolean         force = false;
    
    private boolean         ignoreexternals = false;
    
    private CheckoutDepth   depth = null;

    /**
     * {@inheritDoc}
     */
    public void execute() {
        if( destPath == null ) {
            destPath = getProject().getBaseDir();
        }
        try {
            if( depth != null ) {
                getClient().checkout( url, destPath, revision, depth.getIntValue(), ignoreexternals, force );
            } else {
                getClient().checkout( url, destPath, revision, recurse == null ? true : recurse.booleanValue() );
            }
            
        } catch( SVNClientException e ) {
            throw new BuildException( "Can't checkout", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        if( destPath != null ) {
            SvnAntUtilities.attrNotNull( "destpath", destPath );
        }
        SvnAntUtilities.attrNotNull( "url", url );
        SvnAntUtilities.attrNotNull( "revision", revision );
        if( (depth != null) && (recurse != null) ) {
            warning( MSG_USAGE_CONFLICT );
        }
    }

    /**
     * if false, operate on single directory only 
     * @param recurse whether you want it to checkout files recursively.
     */
    public void setRecurse( boolean recurse ) {
        this.recurse = Boolean.valueOf( recurse );
    }
    
    /**
     * Enforces the execution of the commands.
     * 
     * @param force   <code>true</code> <=> Enforces the execution of the commands.
     */
    public void setForce( boolean force ) {
        this.force = force;
    }
    
    /**
     * Allows to disable the checkout of externals.
     * 
     * @param ignoreexternals   <code>true</code> <=> Disable the checkout of externals.
     */
    public void setIgnoreExternals( boolean ignoreexternals ) {
        this.ignoreexternals = ignoreexternals;
    }

    /**
     * Changes the depth to be used while checking out.
     * 
     * @param depth   One of the following values: <code>empty, files, immediates, infinity</code>.
     */
    public void setDepth( CheckoutDepth depth ) {
        this.depth = depth;
    }
    
    /**
     * Sets the URL; required.
     * @param url The url to set
     */
    public void setUrl( SVNUrl url ) {
        this.url = url;
    }

    /**
     * Sets the destination directory; required 
     * @param destPath destination directory for checkout.
     */
    public void setDestpath( File destPath ) {
        this.destPath = destPath;
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
     * EnumeratedAttribute covering the depth for the checkout.
     */
    public static class CheckoutDepth extends EnumeratedAttribute {

        private static final String[] VALUES = { "empty", "files", "immediates", "infinity" };
        private static final int[] IVALUES = { Depth.empty, Depth.files, Depth.immediates, Depth.infinity };

        /** 
         * {@inheritDoc} 
         */
        public String[] getValues() {
            return VALUES;
        }
        
        public int getIntValue() {
            for( int i = 0; i < VALUES.length; i++ ) {
                if( VALUES[i].equalsIgnoreCase( getValue() ) ) {
                    return IVALUES[i];
                }
            }
            throw new BuildException( "Unsupported 'depth' value: '" + getValue() + "'");
        }

    }

}
