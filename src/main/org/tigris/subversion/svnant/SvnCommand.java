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
package org.tigris.subversion.svnant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.utils.StringUtils;


/**
 * All ant svn commands inherits from this abstract class
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 */
public abstract class SvnCommand extends ProjectComponent {
	
	protected SvnTask task;

	protected abstract void validateAttributes() throws SvnCommandValidationException;
	
	public abstract void execute(ISVNClientAdapter svnClient) throws SvnCommandException;

	public final void executeCommand(ISVNClientAdapter svnClient) throws BuildException
	{
		String[] nameSegments = StringUtils.split(getClass().getName(), ".");
		String className = "<" + nameSegments[nameSegments.length -1] + ">"; 
			
		logInfo(className + " started ...");
		try {
			validateAttributes();
			execute(svnClient);
		} catch (SvnCommandException ex) {
			if (this.task.isFailonerror()) {
				logInfo(className + " failed !");
				throw new BuildException(ex.getMessage(), ex.getCause());
			} else {
				logError(className + " failed :" + ex.getLocalizedMessage());				
			}
		} catch (SvnCommandValidationException e) {
			if (this.task.isFailonerror()) {
				logInfo(className + " failed !");
				throw new BuildException(e.getMessage());
			} else {
				logError(className + " failed :" + e.getLocalizedMessage());								
			}
		}
		logInfo(className + " finished.");
	}

	/**
	 * @return the task
	 */
	protected SvnTask getTask() {
		return task;
	}

	/**
	 * @param task the task to set
	 */
	protected void setTask(SvnTask task) {
		this.task = task;
	}

	/**
	 * Convert the revision string to SVNRevision.
	 * In case the date was supplied as revision, use proper dateFormatter
	 * @param revision
	 * @return SVNRevision constructed from given string or null if unable to do so 
	 */
	public SVNRevision getRevisionFrom(String revision)
	{
		try {
			return SVNRevision.getRevision(revision, new SimpleDateFormat(task.getDateFormatter()));
		} catch (ParseException e) {
			logWarning("Unable to parse revision string");
			return null;
		}
	}
	
	/**
	 * Ansewer a give date as string formatted according to current formatter
	 * @param aDate
	 * @return a String representation of the date
	 */
	public String getDateStringFor(Date aDate)
	{
		return new SimpleDateFormat(task.getDateFormatter()).format(aDate);
	}
	
	public void logVerbose(String message)
	{
		getProject().log(this.task, message, Project.MSG_VERBOSE);
	}

	public void logWarning(String message)
	{
		getProject().log(this.task, message, Project.MSG_WARN);
	}

	public void logError(String message)
	{
		getProject().log(this.task, message, Project.MSG_ERR);
	}

	public void logInfo(String message)
	{
		getProject().log(this.task, message, Project.MSG_INFO);
	}

	public void log(String message)
	{
		getProject().log(this.task, message, Project.MSG_INFO);
	}

	public void log(String message, int level)
	{
		getProject().log(this.task, message, level);
	}

	public static class SvnCommandException extends Exception
	{
		public SvnCommandException() {
			super();
		}

		public SvnCommandException(String arg0, Throwable arg1) {
			super(arg0, arg1);
		}

		public SvnCommandException(String arg0) {
			super(arg0);
		}

		public SvnCommandException(Throwable arg0) {
			super(arg0);
		}
		
	}
	
	public static class SvnCommandValidationException extends Exception
	{
		public SvnCommandValidationException() {
			super();
		}

		public SvnCommandValidationException(String message) {
			super(message);
		}
	}

}
