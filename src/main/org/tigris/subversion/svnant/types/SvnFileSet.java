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
package org.tigris.subversion.svnant.types;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;
import org.tigris.subversion.svnant.SvnTask;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JavaSvnClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

/**
 * This class implements a custom FileSet for ANT. It returns a set of files
 * based on the result of Subversion status command, as opposed to the list
 * of files actually on the file system. Two main differences:
 * <ol>
 * 	<li>Missing and deleted files are provided</li>
 *  <li>Internal subversion file (.svn) are skipped</li>
 * </ol>
 * 
 * This class is implemented as a subclass of org.apache.tools.ant.types.FileSet,
 * which is an opportunistic design, at best. However, it is necessary at the 
 * present time to go around practicality issues between ANT 1.6 (which does not
 * support custom filesets yet) and ANT 1.7 where a lot of changes are made to support 
 * custom filesets. For backward compatibility reasons, the new approach in ANT
 * will not be practical until much later, when we can safely assume that everyone 
 * is using at least ANT 1.7, and that the majority of the tasks based on filesets
 * have migrated to the new structure.
 * 
 * @author Jean-Pierre Fiset <a href="mailto:jp@fiset.ca">jp@fiset.ca</a>
 */
public class SvnFileSet extends org.apache.tools.ant.types.FileSet {

    /**
     * 'javahl' property for file selector. If set,
     * JAVAHL bindings are used, if available. Preempts
     * JavaSVN and command line.
     */
    private boolean javahl = true;
    
    /**
     * 'javasvn' property for file selector. If set,
     * JavaSVN client is used, if available. Preempts
     * command line, but not JAVAHL bindings.  
     */
    private boolean javasvn = true;

    /**
     * Constructor for FileSet.
     */
    public SvnFileSet() {
        super();
    }

    /**
     * Constructor for FileSet, with FileSet to shallowly clone.
     * @param fileset the fileset to clone
     */
    protected SvnFileSet(SvnFileSet fileset_) {
        this.javahl = fileset_.javahl;
        this.javahl = fileset_.javahl;
    }

    /**
     * Creates a deep clone of this instance, except for the nested
     * selectors (the list of selectors is a shallow clone of this
     * instance's list).
     * @return the cloned object
     */
    public synchronized Object clone() {
        SvnFileSet fs = (SvnFileSet) super.clone();
        return fs;
    }
    
    /**
     * Accessor method to 'javahl' property. If reset (false),
     * JavaHL is not used.
     * @param javahl_ New value for javahl property.
     */
    public void setJavahl(boolean javahl_) {
        javahl = javahl_;
    }

    /**
     * Accessor method to 'javasvn' property. If reset (false),
     * JavaSVN is not used.
     * @param javasvn_ New value for javasvn property.
     */
    public void setJavasvn(boolean javasvn_) {
        javasvn = javasvn_;
    }
    
	/**
	 * This method returns a SVN client adapter, based on the property set when the file selector
	 * was declared. More specifically, the 'javahl' and 'javasvn' flags are verified, as well as the
	 * availability of JAVAHL ad JavaSVN adapters, to decide what flavour to use.
	 * @return An instance of SVN client adapter that meets the specified constraints, if any.
	 * @throws BuildException Thrown in a situation where no adapter can fit the constraints.
	 */
	private ISVNClientAdapter getClientAdapter() throws BuildException {
	    ISVNClientAdapter svnClient;
	    
	    if( true == javahl && true == SvnTask.isJavahlAvailable() ) {
	        svnClient = SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
	        log("Using javahl");
	    }
	    else if( true == javasvn && true == SvnTask.isJavaSVNAvailable() ) {
	        svnClient = SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
	        log("Using javasvn");
	    }
	    else if( true == SvnTask.isCommandLineAvailable() ) {
	        svnClient = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
	        log("Using command line interface");
	    } 
	    else {
	        throw new BuildException("Cannot use javahl, JavaSVN nor command line svn client");
	    }
	    
	    return svnClient;
	}
	
    /**
     * Returns the directory scanner needed to access the files to process.
     * @return a <code>DirectoryScanner</code> instance.
     */
    public DirectoryScanner getDirectoryScanner(Project p) {
        if (isReference()) {
            return getRef(p).getDirectoryScanner(p);
        }
        
        File dir = getDir(p);
        boolean followSymlinks =  false;

        
        if (dir == null) {
            throw new BuildException("No directory specified for "
                                     + getDataTypeName() + ".");
        }
        if (!dir.exists()) {
            throw new BuildException(dir.getAbsolutePath() + " not found.");
        }
        if (!dir.isDirectory()) {
            throw new BuildException(dir.getAbsolutePath()
                                     + " is not a directory.");
        }
        DirectoryScanner ds = new SvnDirScanner( getClientAdapter() );
        setupDirectoryScanner(ds, p);
        ds.setFollowSymlinks(followSymlinks);
        ds.scan();
        return ds;
    }

    /**
     * This attribute is not supported by svnFileSet.
     * @throws BuildException always
     */
    public void setRefid(Reference r) throws BuildException {
    	throw (BuildException) notSupported("refid", true).fillInStackTrace();
    }

    /**
     * This attribute is not supported by svnFileSet.
     * @throws BuildException always
     */
    public void setFollowSymlinks(boolean followSymlinks) {
    	throw (BuildException) notSupported("followSymlinks", true).fillInStackTrace();
    }

    /**
     * Builds and returns an exception that can be used to report an attribute
     * that is not supported.
     * @param featureName_ The name of attribute or nested element
     * @param attribute_ True if the name refers to an attribute
     * @return An exception that can be thrown to report an invalid
     * usage of the receiver.
     */
    private BuildException notSupported(String featureName_, boolean attribute_)
    {
       StringBuffer message = new StringBuffer();
       message.append(getProject().getElementName(this))
              .append(" doesn't support the ");

       if (attribute_)
          message.append(featureName_).append(" attribute.");
       else
          message.append("nested ").append(featureName_).append(" element.");

       return new BuildException(message.toString());
    }
}
