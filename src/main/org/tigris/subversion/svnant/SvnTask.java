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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.tigris.subversion.svnant.commands.Add;
import org.tigris.subversion.svnant.commands.Cat;
import org.tigris.subversion.svnant.commands.Checkout;
import org.tigris.subversion.svnant.commands.Cleanup;
import org.tigris.subversion.svnant.commands.Commit;
import org.tigris.subversion.svnant.commands.Copy;
import org.tigris.subversion.svnant.commands.CreateRepository;
import org.tigris.subversion.svnant.commands.Delete;
import org.tigris.subversion.svnant.commands.Diff;
import org.tigris.subversion.svnant.commands.Export;
import org.tigris.subversion.svnant.commands.Feedback;
import org.tigris.subversion.svnant.commands.Ignore;
import org.tigris.subversion.svnant.commands.Import;
import org.tigris.subversion.svnant.commands.Info;
import org.tigris.subversion.svnant.commands.Keywordsadd;
import org.tigris.subversion.svnant.commands.Keywordsremove;
import org.tigris.subversion.svnant.commands.Keywordsset;
import org.tigris.subversion.svnant.commands.Log;
import org.tigris.subversion.svnant.commands.Mkdir;
import org.tigris.subversion.svnant.commands.Move;
import org.tigris.subversion.svnant.commands.Propdel;
import org.tigris.subversion.svnant.commands.Propget;
import org.tigris.subversion.svnant.commands.Propset;
import org.tigris.subversion.svnant.commands.Revert;
import org.tigris.subversion.svnant.commands.SingleInfo;
import org.tigris.subversion.svnant.commands.Status;
import org.tigris.subversion.svnant.commands.SvnCommand;
import org.tigris.subversion.svnant.commands.Switch;
import org.tigris.subversion.svnant.commands.Update;
import org.tigris.subversion.svnant.commands.WcVersion;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Svn Task
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTask extends Task {

    private List commands        = new ArrayList();
    private List notifyListeners = new ArrayList();
    
    /**
     * {@inheritDoc}
     */
    public ProjectComponent getProjectComponent() {
        return this;
    }
    

    /**
     * @see org.tigris.subversion.svnant.ISvnAntProjectComponent#getJavahl()
     */
    public boolean getJavahl() {
        return SvnFacade.getJavahl( this );
    }

    /**
     * @see org.tigris.subversion.svnant.ISvnAntProjectComponent#getSvnKit()
     */
    public boolean getSvnKit() {
        return SvnFacade.getSvnKit( this );
    }

    /**
     * Sets the referred configuration to be used for the svn task.
     *
     * @param refid   The id of the configuration to be used for the svn task.
     */
    public void setRefid(String refid) {
        SvnFacade.setRefid( this, refid );
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        SvnFacade.setUsername( this, username );
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        SvnFacade.setPassword( this, password );
    }

    /**
     * set javahl to false to use command line interface
     * @param javahl
     */
    public void setJavahl(boolean javahl) {
        SvnFacade.setJavahl( this, javahl );
    }

    /**
     * set svnkit to false to use command line interface
     * @param svnkit
     */
    public void setSvnkit(boolean svnkit) {
        SvnFacade.setSvnKit( this, svnkit );
    }

    /**
     * @return dateFormatter used to parse/format revision dates
     */
    public String getDateFormatter() {
        return SvnFacade.getDateFormatter( this );
    }
    
    /**
     * set dateFormatter used to parse/format revision dates
     * @param dateFormatter
     */
    public void setDateFormatter(String dateFormatter) {
        SvnFacade.setDateFormatter( this, dateFormatter );
    }

    /**
     * @return dateTimeZone used to parse/format revision dates
     */
    public TimeZone getDateTimezone() {
        return SvnFacade.getDateTimezone( this );
    }
    
    /**
     * set dateTimezone used to parse/format revision dates
     * @param dateTimezone
     */
    public void setDateTimezone(String dateTimeZone) {
        SvnFacade.setDateTimezone( this, dateTimeZone );
    }

    /**
     * @return the failonerror
     */
    public boolean isFailonerror() {
        return SvnFacade.getFailonerror( this );
    }

    /**
     * @param failonerror the failonerror to set
     */
    public void setFailonerror(boolean failonerror) {
        SvnFacade.setFailonerror( this, failonerror );
    }

    public void addCheckout(Checkout a) {
        addCommand(a);
    }

    public void addSingleinfo(SingleInfo a) {
        addCommand(a);
    }

    public void addList(org.tigris.subversion.svnant.commands.List a) {
        addCommand(a);
    }

    public void addAdd(Add a) {
        addCommand(a);
    }

    public void addCleanup(Cleanup a) {
        addCommand(a);
    }
    
    public void addCommit(Commit a) {
        addCommand(a);
    }

    public void addCopy(Copy a) {
        addCommand(a);
    }

    public void addDelete(Delete a) {
        addCommand(a);
    }

    public void addExport(Export a) {
        addCommand(a);
    }

    /**
     * Add the info command to the list of commands to execute.
     */
    public void addInfo(Info a) {
        addCommand(a);
    }

    public void addImport(Import a) {
        addCommand(a);
    }
    
    public void addLog(Log a) {
        addCommand(a);
    }

    public void addMkdir(Mkdir a) {
        addCommand(a);
    }

    public void addMove(Move a) {
        addCommand(a);
    }

    public void addUpdate(Update a) {
        addCommand(a);
    }
    
    public void addPropset(Propset a) {
        addCommand(a);
    }
    
    public void addDiff(Diff a) {
        addCommand(a);
    }

    public void addKeywordsSet(Keywordsset a) {
        addCommand(a);
    }
    
    public void addKeywordsAdd(Keywordsadd a) {
        addCommand(a);
    }
    
    public void addKeywordsRemove(Keywordsremove a) {
        addCommand(a);
    }    

    public void addRevert(Revert a) {
        addCommand(a);
    }

    public void addCat(Cat a) {
        addCommand(a);
    }

    public void addPropdel(Propdel a) {
        addCommand(a);
    }
    
    public void addIgnore(Ignore a) {
        addCommand(a);
    }
    
    public void addCreateRepository(CreateRepository a) {
        addCommand(a);
    }
    
    public void addWcVersion(WcVersion a) {
        addCommand(a);
    }

    public void addStatus(Status a) {
        addCommand(a);
    }
    
    public void addSwitch(Switch a) {
        addCommand(a);
    }
    
    public void addPropget(Propget a) {
        addCommand(a);
    }
    

    private void addCommand(SvnCommand cmd) {
        cmd.setTask(this);
        commands.add(cmd);
    }
    
    public void addNotifyListener(ISVNNotifyListener notifyListener) {
        notifyListeners.add(notifyListener);
    }

    public void maybeConfigure() throws BuildException {
        super.maybeConfigure();
    }
    
    public void execute() throws BuildException {

        ISVNClientAdapter svnClient = SvnFacade.getClientAdapter(this);        

        for (int i = 0; i < notifyListeners.size();i++) {
            svnClient.addNotifyListener((ISVNNotifyListener)notifyListeners.get(i));
        }

        for (int i = 0; i < commands.size(); i++) {
            SvnCommand command = (SvnCommand) commands.get(i);
            Feedback feedback = new Feedback(command);
            svnClient.addNotifyListener(feedback);
            command.executeCommand(svnClient);
            svnClient.removeNotifyListener(feedback);
        }
        
    }

}
