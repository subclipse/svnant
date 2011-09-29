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

import org.tigris.subversion.svnant.ConflictResolution;
import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.types.DataType;

import org.apache.tools.ant.Project;

import java.io.File;

/**
 * Settings to be used
 * 
 * @author Daniel Kasmeroglu <a href="mailto:daniel.kasmeroglu@kasisoft.net">daniel.kasmeroglu@kasisoft.net</a>
 */
public class SvnSetting extends DataType {

    private Project              project;
    private Boolean              javahl;
    private Boolean              svnkit;
    private String               username;
    private String               password;
    private String               dateformatter;
    private String               timezone;
    private Boolean              failonerror;
    private String               id;
    private String               sslpassword;
    private File                 sslclientcertpath;
    private Integer              sshport;
    private String               sshpassphrase;
    private File                 sshkeypath;
    private Boolean              reject;
    private File                 configdir;
    private ConflictResolution   conflictresolution;
    
    /**
     * Initialises this instance.
     * 
     * @param antproject   The Ant project this instance is related to. Not <code>null</code>.
     */
    public SvnSetting( Project antproject ) {
        project                 = antproject;
        javahl                  = null;
        svnkit                  = null;
        username                = null;
        password                = null;
        dateformatter           = null;
        timezone                = null;
        failonerror             = null;
        id                      = null;
        sslpassword             = null;
        sslclientcertpath       = null;
        sshport                 = null;
        sshpassphrase           = null;
        sshkeypath              = null;
        reject                  = null;
        configdir               = null;
        conflictresolution      = null;
    }
    
    /**
     * Changes the value for the conflict resolution handling.
     * 
     * @param resolution   The new value for the conflict resolution handling.
     */
    public void setConflictResolution( String resolution ) {
        try {
            conflictresolution = ConflictResolution.valueOf( resolution );
        } catch( IllegalArgumentException ex ) {
            SvnAntUtilities.attrInvalidValue( "conflictResolution", ConflictResolution.values(), resolution );
        }
    }
    
    /**
     * Returns the value used to specify the handling of conflict resolutions.
     * 
     * @return   The value used to specify the handling of conflict resolutions. Maybe <code>null</code>.
     */
    public ConflictResolution getConflictResolution() {
        return conflictresolution;
    }
    
    /**
     * Changes the location of the config directory.
     * 
     * @param config   The new location of the configuration directory.
     */
    public void setConfigDirectory( File config ) {
        configdir = config;
    }
    
    /**
     * Returns the location of the configuration directory.
     * 
     * @return   The location of the current configuration directory. Maybe <code>null</code>.
     */
    public File getConfigDirectory() {
        return configdir;
    }

    /**
     * Changes the handling of suspicious certificates.
     * 
     * @param newcertreject   <code>true</code> <=> Reject suspicious certificates.
     */
    public void setCertReject( Boolean newcertreject ) {
        reject = newcertreject;
    }
    
    /**
     * Returns <code>true</code> if suspicious certificates shall be rejected.
     * 
     * @return   <code>true</code> <=> Suspicious certificates shall be rejected.
     */
    public Boolean getCertReject() {
        return reject;
    }
    
    /**
     * Changes the password to be used for an SSL connection.
     * 
     * @param newpassword   The new password to be used. Maybe <code>null</code>.
     */
    public void setSSLPassword( String newpassword ) {
        sslpassword = newpassword;
    }
    
    /**
     * Returns the password to be used for an SSL connection.
     * 
     * @return   The password to be used for an SSL connection. Maybe <code>null</code>.
     */
    public String getSSLPassword() {
        return sslpassword;
    }
    
    /**
     * Changes the path for the SSL client certificate.
     * 
     * @param newclientcertpath   The new path for the SSL client certificate. Maybe <code>null</code>.
     */
    public void setSSLClientCertPath( File newclientcertpath ) {
        sslclientcertpath = newclientcertpath;
    }
    
    /**
     * Returns the path for the SSL client certificate.
     * 
     * @return   The path for the SSL client certificate. Maybe <code>null</code>.
     */
    public File getSSLClientCertPath() {
        return sslclientcertpath;
    }
    
    /**
     * Changes the port used for the SSH port.
     * 
     * @param newsshport   The new port for the SSH port.
     */
    public void setSSHPort( Integer newsshport ) {
        sshport = newsshport;
    }
    
    /**
     * Returns the path for the SSH port.
     * 
     * @return   The path for the SSH port.
     */
    public Integer getSSHPort() {
        return sshport;
    }
    
    /**
     * Changes the passphrase for the SSH encryption.
     * 
     * @param newsshpassphrase   The new passphrase for the SSH encryption. Maybe <code>null</code>.
     */
    public void setSSHPassphrase( String newsshpassphrase ) {
        sshpassphrase = newsshpassphrase;
    }
    
    /**
     * Returns the passphrase for the SSH encryption.
     * 
     * @return   The passphrase for the SSH encryption. Maybe <code>null</code>.
     */
    public String getSSHPassphrase() {
        return sshpassphrase;
    }
    
    /**
     * Changes the location of the SSH key path.
     * 
     * @param newsshkeypath   The location of the SSH key path. Maybe <code>null</code>.
     */
    public void setSSHKeyPath( File newsshkeypath ) {
        sshkeypath = newsshkeypath;
    }
    
    /**
     * Returns the location of the SSH key path.
     * 
     * @return   The location of the SSH key path. Maybe <code>null</code>.
     */
    public File getSSHKeyPath() {
        return sshkeypath;
    }
    
    /**
     * Sets the id for this settings. Makes this setting accessible within the project.
     *
     * @param newid   The new id for this settings.
     */
    public void setId( String newid ) {
        id = newid;
        project.addReference( id, this );
    }

    /**
     * Enables/disables the use of the command line client interface.
     *
     * @param enable   <code>true</code> <=> Enables the command line client interface.
     */
    public void setJavahl( boolean enable ) {
        javahl = enable ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Returns <code>true</code> if the java jni javahl client has to be used.
     *
     * @return   <code>true</code> <=> The java jni javahl client has to be used.
     *           Maybe <code>null</code>.
     */
    public Boolean getJavahl() {
        return javahl;
    }

    /**
     * Enables/disables the use of the svnkit client interface.
     *
     * @param enable   <code>true</code> <=> Enables the svnkit client interface.
     */
    public void setSvnKit( boolean enable ) {
        svnkit = enable ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Returns <code>true</code> if the java svnkit client has to be used.
     *
     * @return   <code>true</code> <=> The java jsvnkit client has to be used.
     *           Maybe <code>null</code>.
     */
    public Boolean getSvnKit() {
        return svnkit;
    }

    /**
     * Sets the username to access the repository.
     *
     * @param newusername   The username to access the repository. If not <code>null</code> the
     *                      value has to be not empty as well.
     */
    public void setUsername( String newusername ) {
        username = newusername;
    }

    /**
     * Returns the currently configured username.
     *
     * @return   The currently configured username. Maybe <code>null</code>.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the password to access the repository.
     *
     * @param newpassword   The password to access the repository. If not <code>null</code> the
     *                      value has to be not empty as well. Double double quotes will be treated
     *                      as an empty string.
     */
    public void setPassword( String newpassword ) {
        /** 
         * @todo [27-Apr-2009:KASI]   Base upon code within SvnTask but really doesn't look good.
         *                            Such things should be handle before calling this method. 
         */
        if( "\"\"".equals( newpassword ) ) {
            newpassword = "";
        }
        password = newpassword;
    }

    /**
     * Returns the currently configured password.
     *
     * @return   The currently configured password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Changes the formatting of dates (used to parse/format revision information). 
     *
     * @param newdateformatter  If <code>null</code> or empty the default format 
     *                          {@link #DEFAULT_DATEFORMATTER} is used.
     */
    public void setDateFormatter( String newdateformatter ) {
        dateformatter = newdateformatter;
    }

    /**
     * Returns the formatting pattern to parse/format revision dates.
     *
     * @return   The formatting pattern. Maybe <code>null</code>.
     */
    public String getDateFormatter() {
        return dateformatter;
    }

    /**
     * Changes the timezone to be used when parsing/formatting date information.
     *
     * @param newtimezone The timezone to be used. If <code>null</code> or empty the default
     *                    timezone {@link #DEFAULT_TIMEZONE} is being used.
     */
    public void setDateTimezone( String newtimezone ) {
        timezone = newtimezone;
    }

    /**
     * Returns the timezone used to parse/format revision dates.
     *
     * @return   The timezone. Maybe <code>null</code>.
     */
    public String getDateTimezone() {
        return timezone;
    }

    /**
     * Enables/disables the controlflow interruption in case of an error.
     *
     * @param enable    <code>true</code> <=> Cause a failure in case of an error. 
     */
    public void setFailonerror( boolean enable ) {
        failonerror = enable ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Returns <code>true</code> if a failure shall abort the build process.
     *
     * @return   <code>true</code> <=> A failure has to abort the build process.
     *           Maybe <code>null</code>.
     */
    public Boolean getFailonerror() {
        return failonerror;
    }

} /* ENDCLASS */
