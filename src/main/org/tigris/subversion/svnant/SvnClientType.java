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

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;

import org.apache.tools.ant.BuildException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public enum SvnClientType {

    svnkit  ( SvnKitClientAdapterFactory  . class , SvnKitClientAdapterFactory  . SVNKIT_CLIENT      ),
    javahl  ( JhlClientAdapterFactory     . class , JhlClientAdapterFactory     . JAVAHL_CLIENT      ),
    cli     ( CmdLineClientAdapterFactory . class , CmdLineClientAdapterFactory . COMMANDLINE_CLIENT );
    
    private static final String MSG_MISSING_METHOD      = 
        "There's no method $%s for class %s !";
    
    private static final String MSG_UNACCESSIBLE_METHOD = 
        "The method $%s for class %s is not accessible !";
    
    private Class<? extends SVNClientAdapterFactory>   factoryclass;
    private String                                     clientname;
    private Boolean                                    available;
    
    SvnClientType( Class<? extends SVNClientAdapterFactory> clazz, String client ) {
        factoryclass    = clazz;
        clientname      = client;
        available       = null;
    }
    
    public ISVNClientAdapter createClient() throws BuildException {
        if( available == null ) {
            checkAvailability();
        }
        if( available.booleanValue() ) {
            ISVNClientAdapter result = null;
            try {
                result = SVNClientAdapterFactory.createSVNClient( clientname );
            } catch( Exception ex ) {
                throw new BuildException( ex );
            }
            return result;
        } else {
            throw new BuildException( String.format( "The svn client '%s' is not available !", this ) );
        }
    }
    
    private void checkAvailability() throws BuildException {
        available             = Boolean.FALSE;
        String msgunavailable = String.format( "The svn client '%s' is not available !", this );
        try {
            invoke( getMethod( factoryclass, "setup" ) );
            Method  issvnclientavailable = getMethod( SVNClientAdapterFactory.class, "isSVNClientAvailable", String.class );
            Boolean availablity          = invoke( issvnclientavailable, clientname );
            if( ! Boolean.TRUE.equals( availablity ) ) {
                throw new BuildException( msgunavailable );
            }
            available = Boolean.TRUE;
        } catch( BuildException ex ) {
            throw ex;
        } catch( RuntimeException ex ) {
            throw new BuildException( msgunavailable, ex.getCause() );
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T invoke( Method method, Object ... params ) {
        try {
            return (T) method.invoke( null, params );
        } catch( IllegalArgumentException   ex ) {
            throw new RuntimeException( ex );
        } catch( IllegalAccessException     ex ) {
            throw new RuntimeException( ex );
        } catch( InvocationTargetException  ex ) {
            throw new RuntimeException( ex.getCause() );
        }
    }
    
    private Method getMethod( Class<?> clazz, String name, Class<?> ... params ) throws BuildException {
        Method result = null;
        try {
            result = clazz.getDeclaredMethod( name, params );
        } catch( SecurityException     ex ) {
            throw new BuildException( String.format( MSG_UNACCESSIBLE_METHOD, name, factoryclass.getName() ) );
        } catch( NoSuchMethodException ex ) {
            throw new BuildException( String.format( MSG_MISSING_METHOD, name, factoryclass.getName() ) );
        }
        return result;
    }

} /* ENDENUM */
